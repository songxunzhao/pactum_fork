package com.pactum.chat.mindmup

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.pactum.chat.model.ValueStep
import com.pactum.test.UnitTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

@UnitTest
class StateTest {
  @Test
  fun `Load value step with chat variable values from jackson`() {
    val jsonString = "{\"currentStep\": {\"id\": \"5.7713.2a8665801-0675.97d4b3c19\", \"user\": false, " +
      "\"value\": null, \"@class\": \".TextStep\", \"message\": \"Hello again!\\n\\nFrom model file: " +
      "[{\\\"number_of_units\\\":\\\"48807\\\",\\\"item_id\\\":\\\"0\\\"}]\\n\\nAfter assignment to local variable: " +
      "[{\\\"number_of_units\\\":\\\"48807\\\",\\\"item_id\\\":\\\"0\\\"}]\", " +
      "\"trigger\": \"6.3fdb.f7b7ad7a-620d.d8354c7bd\", " +
      "\"evalExpression\": \"var items = [{\\\"number_of_units\\\":\\\"48807\\\",\\\"item_id\\\":\\\"0\\\"}]\\n" +
      "values[\\\"{items}\\\"] = items\"}, \"previousStep\": {\"id\": \"1\", \"user\": false, \"value\": null, " +
      "\"@class\": \".TextStep\", \"message\": \"Hello, Everglades Foods Inc!\", " +
      "\"trigger\": \"5.7713.2a8665801-0675.97d4b3c19\", \"evalExpression\": \"\"}, " +
      "\"renderedSteps\": [{\"id\": \"1\", \"user\": false, \"value\": null, \"@class\": \".TextStep\", " +
      "\"message\": \"Hello, Everglades Foods Inc!\", \"trigger\": \"5.7713.2a8665801-0675.97d4b3c19\", " +
      "\"evalExpression\": \"\"}, {\"id\": \"{items}\", " +
      "\"value\": [{\"item_id\": \"0\", \"number_of_units\": \"48807\"}], \"@class\": \".ValueStep\"}, " +
      "{\"id\": \"5.7713.2a8665801-0675.97d4b3c19\", \"user\": false, \"value\": null, " +
      "\"@class\": \".TextStep\", \"message\": \"Hello again!\\n\\nFrom model file: " +
      "[{\\\"number_of_units\\\":\\\"48807\\\",\\\"item_id\\\":\\\"0\\\"}]\\n\\nAfter assignment to local variable: " +
      "[{\\\"number_of_units\\\":\\\"48807\\\",\\\"item_id\\\":\\\"0\\\"}]\", " +
      "\"trigger\": \"6.3fdb.f7b7ad7a-620d.d8354c7bd\", " +
      "\"evalExpression\": \"var items = [{\\\"number_of_units\\\":\\\"48807\\\",\\\"item_id\\\":\\\"0\\\"}]\\n" +
      "values[\\\"{items}\\\"] = items\"}], \"chatVariableValues\": {\"items\": " +
      "[{\"item_id\": \"0\", \"number_of_units\": \"48807\"}]}}"
    val state = jacksonObjectMapper().readValue(jsonString, State::class.java)
    Assertions.assertThat((state.renderedSteps[1] as ValueStep).value).isEqualTo(
      listOf(mapOf("item_id" to "0", "number_of_units" to "48807"))
    )
  }
}
