package com.pactum.utils

import com.pactum.chat.model.ChatApiInput
import com.pactum.negotiation.model.Negotiation
import com.pactum.negotiation.summary.model.ExtraValue
import com.pactum.negotiation.summary.model.ExtraValueFormat
import com.pactum.test.UnitTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant

@UnitTest
class UtilsTest {

  @Test
  fun `stateId is valid`() {
    val flowId = "qwertyuiop"
    val stateId = "qwe123ABCop"

    assertThat(Utils.isValidStateId(stateId, flowId)).isTrue()
  }

  @Test
  fun `stateId is not valid`() {
    val flowId = "qwertyuiop"
    val stateId = "invalid"

    assertThat(Utils.isValidStateId(stateId, flowId)).isFalse()
  }

  @Test
  fun `can generate stateId by flowId`() {
    val flowId = "qwertyuiop"
    val generatedStateId = Utils.generateStateId(flowId)

    assertThat(generatedStateId.startsWith("qwe")).isTrue()
    assertThat(generatedStateId.endsWith("op")).isTrue()
  }

  @Test
  fun `can generate read-only link`() {
    val baseUrl = "https://www.pactum.com"
    val flowId = "qwertyuiop"
    val stateId = "123"
    val modelId = "qwe"
    val modelKey = "asd"
    val chatHolder = ChatApiInput(flowId, stateId, modelId, modelKey, readOnly = true)
    val generatedChatLink = Utils.generateChatLink(baseUrl, chatHolder)

    assertThat(generatedChatLink).isEqualTo("$baseUrl/models/$modelId/chat/$flowId/$modelKey/$stateId/read-only")
  }

  @Test
  fun `can get extra value if is link`() {
    val label = "label"
    val body = "https://www.pactum.com"
    val type = ExtraValueFormat.LINK
    assertThat(Utils.getExtraValue(label, type, body)).isEqualTo(ExtraValue(label, body, type))
  }

  @Test
  fun `can get extra value if is instant`() {
    val label = "label"
    val body = Instant.now()
    val type = ExtraValueFormat.TIME
    assertThat(Utils.getExtraValue(label, type, body)).isEqualTo(ExtraValue(label, body.toEpochMilli(), type))
  }

  @Test
  fun `can get extra value if is String`() {
    val label = "label"
    val body = "string value"
    val type = ExtraValueFormat.TEXT
    assertThat(Utils.getExtraValue(label, type, body)).isEqualTo(ExtraValue(label, body, type))
  }

  @Test
  fun `can get extra value if is Integer`() {
    val label = "label"
    val body = 1L
    val type = ExtraValueFormat.NUMBER
    assertThat(Utils.getExtraValue(label, type, body)).isEqualTo(ExtraValue(label, 1L, type))
  }

  @Test
  fun `can get extra value if is Double`() {
    val label = "label"
    val body = 1.2
    val type = ExtraValueFormat.NUMBER
    assertThat(Utils.getExtraValue(label, type, body)).isEqualTo(ExtraValue(label, 1.2, type))
  }

  @Test
  fun `can get extra value if is percent`() {
    val label = "label"
    val body = 12.65
    val type = ExtraValueFormat.PERCENT
    assertThat(Utils.getExtraValue(label, type, body)).isEqualTo(ExtraValue(label, "12.65%", type))
  }

  @Test
  fun `can get extra value if is currency`() {
    val label = "label"
    val body = 1213.12
    val type = ExtraValueFormat.CURRENCY
    assertThat(Utils.getExtraValue(label, type, body)).isEqualTo(ExtraValue(label, "$1,213.12", type))
  }

  @Test
  fun `can get extra value if is boolean`() {
    val label = "label"
    val body = false
    val type = ExtraValueFormat.BOOLEAN
    assertThat(Utils.getExtraValue(label, type, body)).isEqualTo(ExtraValue(label, false, type))
  }

  @Test
  fun `can get extra value if is null`() {
    val label = "label"
    val body = null
    val type = ExtraValueFormat.EMPTY
    assertThat(Utils.getExtraValue(label, type, body)).isEqualTo(ExtraValue(label, null, type))
  }

  @Test
  fun `can convert valid string to double`() {
    val label = "2.022"
    assertThat(Utils.toDouble(label)).isEqualTo(2.022)
  }

  @Test
  fun `can not convert invalid string to double`() {
    val label = "hello"
    assertThat(Utils.toDouble(label)).isNull()
  }

  @Test
  fun `can round double to 2 decimals`() {
    val label = 2.02245632
    assertThat(Utils.round(label)).isEqualTo(2.02)
  }

  @Test
  fun `can format string to percentage`() {
    val label = 2.51
    assertThat(Utils.format(label, ExtraValueFormat.PERCENT)).isEqualTo("$label%")
  }

  @Test
  fun `can format string to currency`() {
    val label = 1123.51
    assertThat(Utils.format(label, ExtraValueFormat.CURRENCY)).isEqualTo("$1,123.51")
  }

  @Test
  fun `flowId is defaultFlowId`() {
    val flowId = "123"
    val defaultFlowId = "123"
    assertThat(Utils.isDefaultFlowId(flowId, defaultFlowId)).isTrue()
  }

  @Test
  fun `flowId is not defaultFlowId`() {
    val flowId = "1234"
    val defaultFlowId = "123"
    assertThat(Utils.isDefaultFlowId(flowId, defaultFlowId)).isFalse()
  }

  @Test
  fun `stateId is secretStateId`() {
    val stateId = "secret123"
    val secretStateId = "secret"
    assertThat(Utils.isSecretStateId(stateId, secretStateId)).isTrue()
  }

  @Test
  fun `stateId is not secretStateId`() {
    val stateId = "apple123"
    val secretStateId = "secret"
    assertThat(Utils.isSecretStateId(stateId, secretStateId)).isFalse()
  }

  @Test
  fun `can cast object to map`() {
    val negotiation = Negotiation.ApiEntity(1, "stateId", "https://www.pactum.com")
    val map = Utils.cast<Map<String, Any>>(negotiation)
    assertThat(map["id"]).isEqualTo(1L)
    assertThat(map["stateId"]).isEqualTo("stateId")
    assertThat(map["chatUrl"]).isEqualTo("https://www.pactum.com")
  }
}
