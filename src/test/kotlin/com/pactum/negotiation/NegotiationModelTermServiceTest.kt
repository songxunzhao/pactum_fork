package com.pactum.negotiation

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.whenever
import com.pactum.client.model.NegotiationFieldConfigType
import com.pactum.negotiation.model.Negotiation
import com.pactum.test.UnitTest
import com.pactum.negotiationfield.NegotiationFieldService
import com.pactum.negotiationfield.model.NegotiationField
import com.pactum.negotiationfield.model.createMock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant

@UnitTest
class NegotiationModelTermServiceTest {

  private val negotiationFieldService: NegotiationFieldService = mock()
  private val negotiationModelTermService = NegotiationModelTermService(
    negotiationFieldService
  )

  @Test
  fun `can get terms from negotiation`() {

    val id = 1L
    val negotiationTerms: Map<String, Any> = mapOf("a" to 123)
    val allFieldsList = listOf(
      NegotiationField.createMock(id, NegotiationFieldConfigType.TERM, "b", "val"),
      NegotiationField.createMock(id, NegotiationFieldConfigType.MODEL, "c", "val")
    )
    val resultMap = mapOf("b" to "val")

    val negotiation = Negotiation(
      id = id,
      stateId = "abc",
      modelKey = "1",
      clientId = 1,
      createTime = Instant.parse("2019-08-12T21:03:33.123Z"),
      flowId = "1",
      modelId = "1",
      status = "",
      terms = jacksonObjectMapper().writeValueAsString(negotiationTerms)
    )

    whenever(negotiationFieldService.getFieldsForNegotiation(id)).thenReturn(allFieldsList)
    whenever(negotiationFieldService.filterTermFields(allFieldsList)).thenReturn(resultMap)

    val terms = negotiationModelTermService.getTerms(negotiation)
    assertThat(terms["a"]).isEqualTo(123)
    assertThat(terms["b"]).isEqualTo("val")
  }

  @Test
  fun `can get model from negotiation`() {

    val id = 1L
    val negotiationModels: Map<String, Any> = mapOf("a" to 123)
    val allFieldsList = listOf(
      NegotiationField.createMock(id, NegotiationFieldConfigType.TERM, "b", "val"),
      NegotiationField.createMock(id, NegotiationFieldConfigType.MODEL, "c", "val")
    )
    val resultMap = mapOf("c" to "val")

    val negotiation = Negotiation(
      id = id,
      stateId = "abc",
      modelKey = "1",
      clientId = 1,
      createTime = Instant.parse("2019-08-12T21:03:33.123Z"),
      flowId = "1",
      modelId = "1",
      status = "",
      modelAttributes = jacksonObjectMapper().writeValueAsString(negotiationModels)
    )

    whenever(negotiationFieldService.getFieldsForNegotiation(id)).thenReturn(allFieldsList)
    whenever(negotiationFieldService.filterModelFields(allFieldsList)).thenReturn(resultMap)

    val models = negotiationModelTermService.getModels(negotiation)
    assertThat(models["a"]).isEqualTo(123)
    assertThat(models["c"]).isEqualTo("val")
  }
}
