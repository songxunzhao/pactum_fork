package com.pactum.embedded

import com.pactum.chat.model.TextStep
import com.pactum.test.UnitTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@UnitTest
class KotlinScriptTest {
  @Test
  fun `Eval kotlin expression with dynamic context`() {
    val previousValues = mapOf(
      "123" to mapOf("paymentDue" to 60),
      "234" to mapOf("exclusivityPeriod" to 2),
      "555" to mapOf("deliveryPenalty" to 50)
    )
    val model = mapOf(
      "large_limit" to 7.2,
      "email" to "puhkekeskus@paekalda.ee"
    )
    val values = mutableMapOf<String, Any?> ()

    val expression = """
      | values["{is_small_extra}"]=0
      | values["{is_large_extra}"]=0
      | values["{extra_commission}"]=model["large_limit"]
    """.trimMargin()

    val context = listOf(
      ContextValue("previousValues", "Map<String, Any>", previousValues),
      ContextValue("values", "MutableMap<String, Any?>", values),
      ContextValue("model", "Map<String, Any>", model)
    )
    KotlinScript().evalInContext(context, expression)
    assertThat(values).containsKey("{is_small_extra}")
    assertThat(values).containsEntry("{extra_commission}", 7.2)
  }

  @Test
  fun `Eval kotlin value trigger`() {
    val model = mapOf(
      "large_limit" to 7.2,
      "email" to "puhkekeskus@paekalda.ee"
    )

    val expression = """
      (value as String).toInt() > model["large_limit"] as Double
    """.trimMargin()

    val context = listOf(
      ContextValue("value", "Any", "123"),
      ContextValue("model", "Map<String, Any>", model)
    )
    val result = KotlinScript().evalInContext(context, expression)

    assertThat(result).isEqualTo(true)
  }

  @Test
  fun `Eval kotlin value trigger with custom object`() {
    val step = TextStep(id = "123", evalExpression = "", message = "blablabla", value = "", trigger = "", end = false)
    val context = listOf(
      ContextValue("step", "com.pactum.chat.model.TextStep", step)
    )

    val expression = """
      step.end == false
    """.trimMargin()
    val result = KotlinScript().evalInContext(context, expression)
    assertThat(result).isEqualTo(true)
  }
}
