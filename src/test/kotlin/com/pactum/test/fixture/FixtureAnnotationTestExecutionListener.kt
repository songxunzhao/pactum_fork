package com.pactum.test.fixture

import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.test.context.TestContext
import org.springframework.test.context.support.AbstractTestExecutionListener
import kotlin.reflect.full.createInstance

class FixtureAnnotationTestExecutionListener : AbstractTestExecutionListener() {

  override fun beforeTestExecution(testContext: TestContext) {
    val template = testContext.applicationContext.getBean(NamedParameterJdbcTemplate::class.java)
    val annotations = AnnotatedElementUtils.getMergedRepeatableAnnotations(
      testContext.testMethod,
      With::class.java,
      WithAll::class.java
    )
    val fixtures = annotations.map { it.value.objectInstance ?: it.value.createInstance() }
    fixtures.forEach { it.apply(template) }
  }
}
