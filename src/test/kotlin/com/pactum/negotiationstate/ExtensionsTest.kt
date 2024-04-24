package com.pactum.negotiationstate

import com.pactum.chat.VariableException
import com.pactum.chat.getValueUsingKeyPath
import com.pactum.test.UnitTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

@UnitTest
class ExtensionsTest {

  @Test
  fun `can insert into Map by keyPath which exists`() {
    val map = mapOf(
      "variable" to mapOf(
        "property1" to mapOf(
          "property2" to "value2"
        )
      )
    )

    val keyPath = "variable.property1.property2"

    val newValue = "newValue2"
    val newMap = map.insertIntoKeyPathAndCoalesceIfPossible(keyPath, newValue)

    Assertions.assertThat(newMap.getValueUsingKeyPath(keyPath)).isEqualTo(newValue)
  }

  @Test
  fun `can insert into Map by keyPath which doesn't exists`() {
    val map = mapOf(
      "variable" to mapOf(
        "property1" to mapOf(
          "property2" to "value2"
        )
      )
    )

    val keyPath = "variable.property1.property3"

    val value = "value3"
    val newMap = map.insertIntoKeyPathAndCoalesceIfPossible(keyPath, value)

    Assertions.assertThat(newMap.getValueUsingKeyPath(keyPath)).isEqualTo(value)
  }

  @Test
  fun `inserting by keyPath will coalesce value with existing value when both are Map objects`() {
    val map = mapOf(
      "variable" to mapOf(
        "property1" to mapOf(
          "property2" to "value2"
        )
      )
    )

    val keyPath = "variable.property1"

    val value = mapOf(
      "property3" to "value3"
    )
    val newMap = map.insertIntoKeyPathAndCoalesceIfPossible(keyPath, value)

    val expectedValue = mapOf(
      "property2" to "value2",
      "property3" to "value3"
    )

    Assertions.assertThat(newMap.getValueUsingKeyPath(keyPath)).isEqualTo(expectedValue)
  }

  @Test
  fun `inserting by keyPath will overwrite when existing value is not Map object`() {
    val map = mapOf(
      "variable" to mapOf(
        "property1" to mapOf(
          "property2" to "value2"
        )
      )
    )

    val keyPath = "variable.property1.property2"

    val value = mapOf(
      "property3" to "value3"
    )
    val newMap = map.insertIntoKeyPathAndCoalesceIfPossible(keyPath, value)

    Assertions.assertThat(newMap.getValueUsingKeyPath(keyPath)).isEqualTo(value)
  }

  @Test
  fun `inserting by keyPath will overwrite when incoming value is not Map object`() {
    val map = mapOf(
      "variable" to mapOf(
        "property1" to mapOf(
          "property2" to "value2"
        )
      )
    )

    val keyPath = "variable.property1"

    val value = "newValue1"
    val newMap = map.insertIntoKeyPathAndCoalesceIfPossible(keyPath, value)

    Assertions.assertThat(newMap.getValueUsingKeyPath(keyPath)).isEqualTo(value)
  }

  @Test
  fun `throws error while inserting into Map by keyPath where value isn't Map`() {
    val map = mapOf(
      "variable" to mapOf(
        "property1" to mapOf(
          "property2" to "value2"
        )
      )
    )

    val keyPath = "variable.property1.property2.property4"

    val value = "property4"

    Assertions.assertThatExceptionOfType(VariableException::class.java).isThrownBy {
      map.insertIntoKeyPathAndCoalesceIfPossible(keyPath, value)
    }.withMessage("Could not insert into keyPath: $keyPath")
  }
}
