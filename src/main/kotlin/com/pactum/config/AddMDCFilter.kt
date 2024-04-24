package com.pactum.config

import io.opentracing.util.GlobalTracer
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.util.UUID
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class AddMDCFilter(
  @Value("\${server.env}") private val appEnv: String,
  @Value("\${server.service}") private val serviceName: String
) : Filter {

  companion object {
    const val MDC_TRACE_ID_KEY = "trace_id"
    const val MDC_DD_TRACE_ID_KEY = "dd.trace_id"
    const val MDC_DD_SPAN_ID_KEY = "dd.span_id"
    const val MDC_ENV_KEY = "env"
    const val MDC_SERVICE_NAME_KEY = "service"
  }

  override fun doFilter(req: ServletRequest, res: ServletResponse, chain: FilterChain) {
    val span = GlobalTracer.get().activeSpan()

    try {
      MDC.put(MDC_TRACE_ID_KEY, UUID.randomUUID().toString())
      if (span != null) {
        MDC.put(MDC_DD_TRACE_ID_KEY, span.context().toTraceId())
        MDC.put(MDC_DD_SPAN_ID_KEY, span.context().toSpanId())
      }

      MDC.put(MDC_ENV_KEY, appEnv)
      MDC.put(MDC_SERVICE_NAME_KEY, serviceName)
      chain.doFilter(req, res)
    } finally {
      MDC.remove(MDC_TRACE_ID_KEY)
      MDC.remove(MDC_DD_TRACE_ID_KEY)
      MDC.remove(MDC_DD_SPAN_ID_KEY)
    }
  }
}
