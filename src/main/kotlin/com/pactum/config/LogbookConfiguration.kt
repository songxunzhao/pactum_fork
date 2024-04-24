package com.pactum.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.zalando.logbook.Correlation
import org.zalando.logbook.DefaultHttpLogWriter
import org.zalando.logbook.HttpLogWriter
import org.zalando.logbook.HttpRequest
import org.zalando.logbook.HttpResponse
import org.zalando.logbook.Precorrelation
import org.zalando.logbook.Sink
import org.zalando.logbook.json.JsonHttpLogFormatter

@Configuration
class LogbookConfiguration {
  @Bean
  fun sink(): Sink {
    return CustomSink(DefaultHttpLogWriter())
  }

  // Follows DefaultSink implementation, with customized response write method
  class CustomSink(private val writer: HttpLogWriter) : Sink {
    private val formatter = CustomHttpLogFormatter()

    override fun isActive(): Boolean {
      return this.writer.isActive
    }

    override fun write(precorrelation: Precorrelation, request: HttpRequest) {
      writer.write(precorrelation, formatter.formatRequest(precorrelation, request))
    }

    override fun write(correlation: Correlation, request: HttpRequest, response: HttpResponse) {
      writer.write(correlation, formatter.formatResponse(correlation, request, response))
    }
  }

  // Extends JsonHttpLogFormatter implementation, with customized response format method,
  // which adds some additional content from the correlated request
  class CustomHttpLogFormatter() {
    private val jsonFormatter = JsonHttpLogFormatter(ObjectMapper())

    fun formatRequest(precorrelation: Precorrelation, request: HttpRequest): String {
      return jsonFormatter.format(jsonFormatter.prepare(precorrelation, request))
    }

    fun formatResponse(correlation: Correlation, request: HttpRequest, response: HttpResponse): String {
      val jsonContent: MutableMap<String, Any> = jsonFormatter.prepare(correlation, response)
      jsonContent["requestMethod"] = request.method
      jsonContent["requestUri"] = request.requestUri
      jsonFormatter.prepareHeaders(request).ifPresent { headers ->
        jsonContent["requestHeaders"] = headers
      }

      return jsonFormatter.format(jsonContent)
    }
  }
}
