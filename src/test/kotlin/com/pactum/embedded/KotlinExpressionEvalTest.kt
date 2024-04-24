package com.pactum.embedded

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.pactum.chat.mindmup.State
import com.pactum.test.UnitTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

@UnitTest
class KotlinExpressionEvalTest {
  @Test
  fun `evaluate kotlin expression and return new ValueSteps`() {
    val stateStr = """
      {
        "currentStep":{
            "@class":".TextStep",
            "id":"{terms}",
            "evalExpression":"",
            "value":{"term1":1},
            "trigger":"5.f745.dc70c5aaf-4010.f36e69ad1",
            "label":"Option A",
            "user":true,
            "message":"Option A"
          },
          "renderedSteps":[{
            "@class":".TextStep",
            "id":"1",
            "evalExpression":"",
            "message":"Hi!",
            "trigger":"2.f745.dc70c5aaf-4010.f36e69ad1"
          },{
            "@class":".TextStep",
            "id":"2.f745.dc70c5aaf-4010.f36e69ad1",
            "evalExpression":"",
            "message":"Choose!",
            "trigger":"{terms}"
          },{
            "@class":".TextStep",
            "id":"{terms}",
            "evalExpression":"",
            "value":{"term1":1},
            "trigger":"5.f745.dc70c5aaf-4010.f36e69ad1",
            "label":"Option A",
            "user":true,
            "message":"Option A"
          }]}
    """
    val variable = "{terms}"
    val expression = """values["{terms}"] = mapOf(
      "term1" to (previousValues["{terms}"] as Map<*, Int>)["term1"]!! + 1)"""
    val state = jacksonObjectMapper().readValue(stateStr, State::class.java)
    val expressionEval = KotlinExpressionEval()
    expressionEval.state = state

    val newSteps = expressionEval.eval(expression)
    Assertions.assertThat(newSteps).isNotEmpty()
    Assertions.assertThat(newSteps[0]).hasFieldOrPropertyWithValue("variable", variable)
  }
}
