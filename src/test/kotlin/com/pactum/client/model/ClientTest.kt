package com.pactum.client.model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.pactum.negotiation.model.NegotiationStatusField
import com.pactum.negotiation.summary.model.ExtraValueFormat
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ClientTest {
  @Test
  fun `can get ClientConfig`() {
    val fieldsConfig = listOf(
      NegotiationFieldConfig(
        NegotiationFieldConfigType.MODEL,
        "Ongoing",
        ExtraValueFormat.TEXT,
        "status",
        3
      )
    )
    val statusFields = mapOf(
      "ONGOING" to NegotiationStatusField("on going"),
      "AGREEMENT_REACHED" to NegotiationStatusField("agreement reached")
    )
    val summaryFields = NegotiationSummaryFields(
      NegotiationSummaryList(
        listOf(NegotiationSummaryField("key1", "label1", ExtraSummaryOperation.AVE, ExtraValueFormat.CURRENCY))
      ),
      NegotiationSummaryList(
        listOf(NegotiationSummaryField("key2", "label2", ExtraSummaryOperation.COUNT, ExtraValueFormat.LINK))
      )
    )
    val fieldsConfigJson = jacksonObjectMapper().writeValueAsString(fieldsConfig)
    val statusFieldsJson = jacksonObjectMapper().writeValueAsString(statusFields)
    val summaryFieldsJson = jacksonObjectMapper().writeValueAsString(summaryFields)
    val client = Client(
      tag = "tag",
      name = "name",
      dashboardUrl = "foobar",
      negotiationFieldsConfig = fieldsConfigJson,
      negotiationStatusFields = statusFieldsJson,
      negotiationSummaryFields = summaryFieldsJson
    )

    val resultConfig = client.getConfig()

    assertThat(resultConfig.negotiationFields).isEqualTo(fieldsConfig)
    assertThat(resultConfig.negotiationStatuses).isEqualTo(statusFields)
    assertThat(resultConfig.negotiationSummaryFields).isEqualTo(summaryFields)
    assertThat(resultConfig.dashboardUrl).isEqualTo("foobar")
  }

  @Test
  fun `constructor default arguments`() {
    val client = Client(tag = "tag", name = "name")

    assertThat(client).isEqualTo(
      Client(
        id = null,
        tag = "tag",
        name = "name",
        dashboardUrl = null,
        negotiationFieldsConfig = null,
        negotiationStatusFields = null,
        negotiationSummaryFields = null
      )
    )
  }

  @Test
  fun `companion object create`() {
    val client = Client.create("tag", "name")

    assertThat(client).isEqualTo(Client(tag = "tag", name = "name"))
  }
}
