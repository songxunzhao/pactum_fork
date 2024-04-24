package com.pactum.config

import com.pactum.pubsub.PubSubImpl
import com.pactum.pubsub.PubSubImplBase
import com.pactum.pubsub.PubSubTestImpl
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.gcp.pubsub.PubSubAdmin
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("!test")
class PubSubConfiguration(
  @Value("\${server.env}") val appEnv: String,
  @Value("\${server.baseUrl}") val baseUrl: String,
  private val pubSubTemplate: PubSubTemplate,
  private val pubSubAdmin: PubSubAdmin
) {

  @Bean
  fun realPubSubService(): PubSubImplBase {
    return PubSubImpl(appEnv, baseUrl, pubSubTemplate, pubSubAdmin)
  }
}

@Configuration
@Profile("test")
class PubSubTestConfiguration {

  @Bean
  fun testPubSubService(): PubSubImplBase {
    return PubSubTestImpl()
  }
}
