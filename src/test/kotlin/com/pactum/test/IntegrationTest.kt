package com.pactum.test

import com.pactum.test.fixture.FixtureAnnotationTestExecutionListener
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestExecutionListeners

@ContextConfiguration(initializers = [DbContainerInitializer::class])
@TestExecutionListeners(
  listeners = [FixtureAnnotationTestExecutionListener::class],
  mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Cleanup
@Import(TestConfiguration::class)
annotation class IntegrationTest
