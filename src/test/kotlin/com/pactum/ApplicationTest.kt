package com.pactum

import com.pactum.test.DbContainerInitializer
import com.pactum.test.TestConfiguration
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration

@SpringBootTest
@ContextConfiguration(initializers = [DbContainerInitializer::class])
@Import(TestConfiguration::class)
class ApplicationTest {

  @Test
  fun contextLoads() {
  }
}
