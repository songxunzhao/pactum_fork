package com.pactum.exception

import com.pactum.utils.SentryHelper
import mu.KLogging
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
class GlobalExceptionHandler {
  companion object : KLogging()

  @ExceptionHandler(Exception::class)
  fun handle(ex: Exception, request: WebRequest): ResponseEntity<ApiError> {
    val apiError = ex.toApiError()
    logger.error(apiError.message, ex)

    return when (ex) {
      is ClientFaultException -> {
        ResponseEntity(apiError, ex.statusCode)
      }
      else -> {
        SentryHelper.report(ex, mapOf("description" to request.getDescription(true)))
        ResponseEntity(apiError, HttpStatus.INTERNAL_SERVER_ERROR)
      }
    }
  }
}

data class ApiError(
  val name: String,
  val message: String
)
