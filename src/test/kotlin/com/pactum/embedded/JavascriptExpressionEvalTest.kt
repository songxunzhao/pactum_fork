package com.pactum.embedded

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.pactum.chat.mindmup.State
import com.pactum.chat.model.ValueStep
import com.pactum.test.UnitTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@UnitTest
class JavascriptExpressionEvalTest {
  @Test
  fun `evaluate JS expression and return new ValueSteps`() {
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

    val expression = """var term1 = previousValues["{terms}"]["term1"] + 1;"""
    val expression2 = """values["{terms}"] = { "term1" : term1 };"""
    val state = jacksonObjectMapper().readValue(stateStr, State::class.java)
    val expressionEval = JavaScriptExpressionEval()
    expressionEval.state = state

    val newSteps = expressionEval.eval(expression + "\n" + expression2)
    assertThat(newSteps).isNotEmpty()
    assertThat(newSteps[0]).hasFieldOrPropertyWithValue("variable", "{terms}")
    assertThat(newSteps[0]).hasFieldOrPropertyWithValue("variable", "{terms}")
  }

  @Test
  fun `evaluate JS array expression and return a ValueStep with array value`() {
    val expression = """var items = [{"number_of_units": "48807","item_id": "0"}]"""
    val expression2 = """values["{items}"] = items"""
    val expressionEval = JavaScriptExpressionEval()

    val newSteps = expressionEval.eval(expression + "\n" + expression2)
    assertThat(newSteps).isNotEmpty()
    println((newSteps[0] as ValueStep).value!!::class.java)
    assertThat((newSteps[0] as ValueStep).value).isEqualTo(
      listOf(mapOf("item_id" to "0", "number_of_units" to "48807"))
    )
  }

  @Test
  fun `evaluate JS function expression and return a ValueStep with null value`() {
    val expression = """values["{items}"] = function () {}"""
    val expressionEval = JavaScriptExpressionEval()
    val newSteps = expressionEval.eval(expression)
    assertThat(newSteps).isNotEmpty()
    assertThat((newSteps[0] as ValueStep).value).isNull()
  }

  @Test
  fun `evaluate JS null expression and return a ValueStep with null value`() {
    val expression = """values["{items}"] = null"""
    val expressionEval = JavaScriptExpressionEval()
    val newSteps = expressionEval.eval(expression)
    assertThat(newSteps).isNotEmpty()
    assertThat((newSteps[0] as ValueStep).value).isNull()
  }

  @Test
  fun `evaluate JS expression with html content`() {
    val expression = """values["{items}"] = 
|'\n TEST 4 NEW LINES: \n \n \n \n<b>BOLD</b> AND TEST QUOTATION MARKS: "" and other signs';""".trimMargin()
    val expressionEval = JavaScriptExpressionEval()
    val newSteps = expressionEval.eval(expression)
    assertThat(newSteps).isNotEmpty()
    assertThat((newSteps[0] as ValueStep).value).isEqualTo(
      "\n TEST 4 NEW LINES: \n \n \n \n<b>BOLD</b> AND TEST QUOTATION MARKS: \"\" and other signs"
    )
  }

  @Test
  fun `evaluate array_reduce`() {
    val stateJson = """
      {
        "currentStep": {
          "id": "{cart}", 
          "user": true, 
          "value": [
            {"name": "Guitar", "price": 100}, 
            {"name": "Piano", "price": 300}
          ], 
          "@class": ".TextStep", 
          "message": "Guitar, Piano", 
          "trigger": "122.612b.42960491-4fd8.76509c8ba", 
          "evalExpression": ""
        },
        "renderedSteps": [{
          "id": "{cart}", 
          "user": true, 
          "value": [
            {"name": "Guitar", "price": 100}, 
            {"name": "Piano", "price": 300}
          ], 
          "@class": ".TextStep", 
          "message": "Guitar, Piano", 
          "trigger": "122.612b.42960491-4fd8.76509c8ba", 
          "evalExpression": ""
        }]}
    """
    val state = jacksonObjectMapper().readValue<State>(stateJson)
    val expressionEval = JavaScriptExpressionEval()
    expressionEval.state = state
    val newSteps = expressionEval.eval(
      """
      values['{totalPrice}'] = previousValues['{cart}'].reduce((totalPrice, item) => totalPrice + item.price, 0);
      """
    )
    assertThat(newSteps).isNotEmpty()
    assertThat((newSteps[0] as ValueStep).value).isEqualTo(400)
  }

  @Test
  fun `evaluate array includes`() {
    val stringIncludeExpression = """
      if("test test test".includes("test")) {
        values["{stringTest}"] = 1; 
      }
      """
    val arrayIncludeExpression = """
      if([1,2,3,4].includes(1)) {
        values["{arrayTest}"] = 1;
      }
    """.trimIndent()
    val expressionEval = JavaScriptExpressionEval()
    val newSteps = expressionEval.eval(stringIncludeExpression + arrayIncludeExpression)
    assertThat(newSteps).isNotEmpty()
    assertThat((newSteps[0] as ValueStep).value).isEqualTo(1)
    assertThat((newSteps[1] as ValueStep).value).isEqualTo(1)
  }
}
