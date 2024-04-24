package com.pactum.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@Configuration
class ThreadPoolConfiguration {

  @Bean
  fun asyncExecutor(): Executor {
    return Executors.newFixedThreadPool(10)
  }
}
