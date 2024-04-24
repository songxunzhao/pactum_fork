package com.pactum.utils

import com.pactum.negotiation.summary.model.ExtraValueFormat
import com.pactum.test.UnitTest
import org.assertj.core.api.Assertions
import org.json.simple.JSONObject
import org.junit.jupiter.api.Test

@UnitTest
class JsonHelperTest {

  @Test
  fun `string is json object`() {
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
        }
      }
    """

    Assertions.assertThat(JsonHelper.isJsonObject(stateStr)).isTrue()
  }

  @Test
  fun `null is not json object`() {
    Assertions.assertThat(JsonHelper.isJsonObject(null)).isFalse()
  }

  @Test
  fun `string is not json object`() {
    val stateStr = """
      hello
    """

    Assertions.assertThat(JsonHelper.isJsonObject(stateStr)).isFalse()
  }

  @Test
  fun `string to json object works`() {
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
        }
      }
    """

    Assertions.assertThat(JsonHelper.toJsonObject(stateStr)).isInstanceOf(JSONObject::class.java)
  }

  @Test
  fun `can get Object if it serializable`() {
    val obj = ExtraValueFormat.EMPTY
    Assertions.assertThat(JsonHelper.getObjectOrJson(obj)).isEqualTo(obj)
  }

  @Test
  fun `can get null if it null`() {
    val obj = null
    Assertions.assertThat(JsonHelper.getObjectOrJson(obj)).isEqualTo(obj)
  }

  @Test
  fun `can convert string json to map`() {
    val json = """{"a": 1, "b": {"c": true}}"""
    val map = JsonHelper.convertToMapOrEmpty(json)
    Assertions.assertThat(map.size).isEqualTo(2)
    Assertions.assertThat(map["a"]).isEqualTo(1)
    Assertions.assertThat(map["b"]).isInstanceOf(Map::class.java)
    val b = map["b"] as Map<*, *>
    Assertions.assertThat(b["c"]).isEqualTo(true)
  }

  @Test
  fun `can convert null to empty map`() {
    val json = "null"
    val map = JsonHelper.convertToMapOrEmpty(json)
    Assertions.assertThat(map.size).isEqualTo(0)
  }

  @Test
  fun `can flatten json string`() {
    val json = """
      {
        "outer": "value1",
        "inner": {
          "key": "value2"
        }
      }
    """.trimIndent()
    val map = JsonHelper.flattenAsMap(json)
    Assertions.assertThat(map.size).isEqualTo(2)
    Assertions.assertThat(map["outer"]).isEqualTo("value1")
    Assertions.assertThat(map["inner.key"]).isEqualTo("value2")
  }

  @Test
  fun `can flatten map`() {
    val map = mapOf(
      "outer" to "value1",
      "inner" to mapOf(
        "key" to "value2"
      )
    )
    val flattened = JsonHelper.flattenAsMap(map)
    Assertions.assertThat(flattened.size).isEqualTo(2)
    Assertions.assertThat(flattened["outer"]).isEqualTo("value1")
    Assertions.assertThat(flattened["inner.key"]).isEqualTo("value2")
  }
}
