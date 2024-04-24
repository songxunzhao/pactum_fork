package com.pactum.test

import com.pactum.config.JdbcPersistenceConfiguration
import com.pactum.test.fixture.FixtureAnnotationTestExecutionListener
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS

@Target(CLASS)
@Retention(RUNTIME)
@TestInstance(Lifecycle.PER_CLASS)
@DataJdbcTest(
  includeFilters = [ComponentScan.Filter(
    type = FilterType.ASSIGNABLE_TYPE,
    classes = [JdbcPersistenceConfiguration::class]
  )]
)
@AutoConfigureTestDatabase(replace = NONE)
@AutoConfigureJsonTesters
@ContextConfiguration(
  initializers = [DbContainerInitializer::class]
)
@TestExecutionListeners(listeners = [FixtureAnnotationTestExecutionListener::class], mergeMode = MERGE_WITH_DEFAULTS)
@Cleanup
@Import(TestConfiguration::class)
annotation class RepositoryTest
