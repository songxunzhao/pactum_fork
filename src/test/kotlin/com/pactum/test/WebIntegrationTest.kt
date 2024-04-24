package com.pactum.test

import com.pactum.test.fixture.FixtureAnnotationTestExecutionListener
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestExecutionListeners

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = [DbContainerInitializer::class])
@TestExecutionListeners(
  listeners = [FixtureAnnotationTestExecutionListener::class],
  mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
@Cleanup
@Import(TestConfiguration::class)
annotation class WebIntegrationTest
