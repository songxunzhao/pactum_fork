package com.pactum.test

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.data.auditing.DateTimeProvider
import java.time.Clock
import java.util.Optional

@TestConfiguration
class TestConfiguration {

  @Bean
  fun clock(): Clock {
    return TestClockHolder.CLOCK
  }

  @Bean
  fun dateTimeProvider(): DateTimeProvider {
    return DateTimeProvider { Optional.of(TestClockHolder.NOW) }
  }
}
