package com.pactum.utils

import datadog.trace.api.DDTags
import io.opentracing.Span
import io.opentracing.Tracer

fun Tracer.trace(operationName: String, handler: (span: Span) -> Any): Any {
  val span = buildSpan(operationName).start()
  val scope = activateSpan(span)
  try {
    val result = handler(span)
    scope.close()
    span.finish()
    return result
  } catch (e: Exception) {
    span.setTag(DDTags.ERROR_MSG, e.message)
    span.setTag(DDTags.ERROR_STACK, e.stackTraceToString())
    scope.close()
    span.finish()
    throw e
  }
}
