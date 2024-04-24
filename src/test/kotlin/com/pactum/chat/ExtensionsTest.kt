package com.pactum.chat

import com.pactum.test.UnitTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

@UnitTest
class ExtensionsTest {
  @Test
  fun `can get value from Map by keyPath`() {
    val expectedValue = "value"

    val map = mapOf(
      "variable" to mapOf(
        "property1" to mapOf(
          "property2" to expectedValue
        )
      )
    )

    Assertions.assertThat(map.getValueUsingKeyPath("variable.property1.property2")).isEqualTo(expectedValue)
  }
}
