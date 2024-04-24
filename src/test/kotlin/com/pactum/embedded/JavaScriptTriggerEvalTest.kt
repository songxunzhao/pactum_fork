package com.pactum.embedded

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.pactum.chat.mindmup.State
import com.pactum.test.UnitTest
import junit.framework.TestCase.assertFalse
import org.junit.jupiter.api.Test

@UnitTest
class JavaScriptTriggerEvalTest {
  @Test
  fun `evaluate javascript trigger and return false`() {
    val stateStr = """
      {
        "currentStep":{
          "@class":".ConditionalUserInputStep",
          "id":"{number}",
          "evalExpression":"",
          "user":true,
          "message":"123",
          "value": "4",
          "trigger":{
            "value >= 100":"15.306b.5891d1449-d168.a8ecf6009",
            "value < 100":"6.f745.dc70c5aaf-4010.f36e69ad1"
          }
        },
        "renderedSteps":[
          {
            "@class":".ConditionalUserInputStep",
            "id":"{number}",
            "evalExpression":"",
            "user":true,
            "message":"123",
            "value":123,
            "trigger":{
              "value >= 100":"15.306b.5891d1449-d168.a8ecf6009",
              "value < 100":"6.f745.dc70c5aaf-4010.f36e69ad1"
            }
          }
        ]
      }
    """

    val expression = "value >= 100"
    val state = jacksonObjectMapper().readValue(stateStr, State::class.java)
    val triggerEval = JavaScriptTriggerEval()

    assertFalse(triggerEval.eval(state, expression))
  }
}
