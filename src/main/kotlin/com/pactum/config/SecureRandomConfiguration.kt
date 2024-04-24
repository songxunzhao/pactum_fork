package com.pactum.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.security.SecureRandom

@Configuration
class SecureRandomConfiguration {

  @Bean
  fun secureRandom(): SecureRandom = SecureRandom()
}
