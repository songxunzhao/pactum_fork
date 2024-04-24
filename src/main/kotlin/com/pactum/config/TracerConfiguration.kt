package com.pactum.config

import datadog.opentracing.DDTracer
import io.micrometer.core.aop.TimedAspect
import io.micrometer.core.instrument.ImmutableTag
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import io.opentracing.Tracer
import io.opentracing.contrib.spring.cloud.jdbc.JdbcAutoConfiguration
import org.springframework.boot.actuate.metrics.web.servlet.WebMvcTagsProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerMapping
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Configuration
@Import(JdbcAutoConfiguration::class)
class TracerConfiguration {
  @Bean
  fun ddTracer(): Tracer {
    val tracer = DDTracer()
    datadog.trace.api.GlobalTracer.registerIfAbsent(tracer)
    return tracer
  }

  @Bean
  fun webMvcTagsProvider(): WebMvcTagsProvider? {
    return object : WebMvcTagsProvider {
      override fun getTags(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any?,
        exception: Throwable?
      ): Iterable<Tag> {
        val tags = mutableListOf<Tag>()
        val uri = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE) as? String?
        if (uri != null) {
          tags.add(ImmutableTag("uri", uri))
        }

        if (handler is HandlerMethod) {
          tags.add(ImmutableTag("operation_name", handler.method.name))
        }

        return tags
      }

      override fun getLongRequestTags(request: HttpServletRequest, handler: Any): Iterable<Tag> {
        return ArrayList<Tag>()
      }
    }
  }

  @Bean
  fun timedAspect(registry: MeterRegistry): TimedAspect {
    return TimedAspect(registry)
  }
}
