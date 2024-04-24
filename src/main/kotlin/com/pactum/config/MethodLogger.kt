package com.pactum.config

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.pactum.utils.JsonHelper
import mu.KLogging
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.stereotype.Component
import org.springframework.util.StopWatch
import java.util.concurrent.TimeUnit

@Retention(AnnotationRetention.RUNTIME)
annotation class Loggable(val includeResults: Boolean = false, val includeArgs: Boolean = false)

@Aspect
@Component
class MethodLogger {
  companion object : KLogging()

  @Around("@annotation(com.pactum.config.Loggable)")
  fun loggableMethods(proceedingJoinPoint: ProceedingJoinPoint): Any? {
    val methodSignature = proceedingJoinPoint.signature as MethodSignature
    val loggable = methodSignature.method.getAnnotation(Loggable::class.java)
    return logJoinPoint(proceedingJoinPoint, loggable.includeResults, loggable.includeArgs)
  }

  @Around("@annotation(org.springframework.data.jdbc.repository.query.Query)")
  fun databaseQuery(proceedingJoinPoint: ProceedingJoinPoint): Any? {
    return logJoinPoint(proceedingJoinPoint)
  }

  private fun logJoinPoint(
    proceedingJoinPoint: ProceedingJoinPoint,
    shouldIncludeResult: Boolean = false,
    shouldIncludeArgs: Boolean = false
  ): Any? {
    val args = proceedingJoinPoint.args
    val methodSignature = proceedingJoinPoint.signature as MethodSignature
    val className = methodSignature.declaringType.simpleName
    val methodName = methodSignature.name
    val id = "$className.$methodName()"
    if (shouldNotLog(id)) return proceedingJoinPoint.proceed()
    val stopWatch = StopWatch(id)
    stopWatch.start(methodName)
    val result = proceedingJoinPoint.proceed()
    stopWatch.stop()
    logger.info {
      Log(
        id = stopWatch.id,
        args = if (shouldIncludeArgs) args else arrayOf("excluded"),
        result = if (shouldIncludeResult) result else "excluded",
        totalTimeMicros = TimeUnit.NANOSECONDS.toMicros(stopWatch.totalTimeNanos)
      ).toString()
    }
    return result
  }

  private fun shouldNotLog(id: String): Boolean {
    return when (id) {
      "HealthController.getHealth()" -> true
      else -> false
    }
  }

  private data class Log(
    val id: String,
    val args: Array<out Any?>?,
    val result: Any?,
    val totalTimeMicros: Long
  ) {
    override fun toString(): String {
      val args = args?.map { JsonHelper.getObjectOrJson(it) }?.toTypedArray()
      val result = JsonHelper.getObjectOrJson(result)
      val innerLog = Log(id, args, result, totalTimeMicros)
      return jacksonObjectMapper().writeValueAsString(innerLog)
    }
  }
}
