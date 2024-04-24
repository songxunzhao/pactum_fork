package com.pactum.config

import org.apache.catalina.connector.Connector
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory

import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TomcatConfiguration {

  @Bean
  fun containerCustomizer(): WebServerFactoryCustomizer<TomcatServletWebServerFactory> {
    return EmbeddedTomcatCustomizer()
  }

  private class EmbeddedTomcatCustomizer : WebServerFactoryCustomizer<TomcatServletWebServerFactory> {
    override fun customize(factory: TomcatServletWebServerFactory) {
      factory.addConnectorCustomizers(
        TomcatConnectorCustomizer { connector: Connector ->
          connector.setProperty("relaxedQueryChars", "[]")
        }
      )
    }
  }
}
