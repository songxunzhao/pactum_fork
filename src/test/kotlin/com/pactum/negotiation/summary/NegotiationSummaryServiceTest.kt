package com.pactum.negotiation.summary

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.pactum.auth.AccessDeniedException
import com.pactum.auth.model.Role
import com.pactum.negotiationstate.NegotiationStateRepository
import com.pactum.chat.model.NegotiationState
import com.pactum.client.ClientRepository
import com.pactum.client.model.Client
import com.pactum.extract.ExtractService
import com.pactum.negotiation.NegotiationRepository
import com.pactum.negotiation.model.Negotiation
import com.pactum.test.UnitTest
import com.pactum.auth.SessionHelper
import com.pactum.client.model.ExtraSummaryOperation
import com.pactum.client.model.NegotiationSummaryField
import com.pactum.client.model.NegotiationSummaryFields
import com.pactum.client.model.NegotiationSummaryList
import com.pactum.negotiation.summary.model.ExtraValueFormat
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.util.Optional

@UnitTest
class NegotiationSummaryServiceTest {

  private val negotiationRepository: NegotiationRepository = mock()
  private val negotiationStateRepository: NegotiationStateRepository = mock()
  private val clientRepository: ClientRepository = mock()
  private val extractService: ExtractService = mock()
  private val baseUrl = "https://www.pactum.com"

  private val negotiationSummaryService = NegotiationSummaryService(
    negotiationRepository,
    negotiationStateRepository,
    baseUrl,
    clientRepository,
    extractService
  )

  @Test
  fun `can get negotiations summary if is admin`() {
    val email = "backend@pactum.com"
    val token = "avalidtoken"
    val clientId = 1L
    val id = 0L
    val stateId = "abc"
    val status = "CREATED"
    val flowId = "qwe"
    val modelId = "123"
    val modelKey = "123"
    val flowVersionId = null
    val modelVersionId = null
    val clientTag = "tag"
    val clientName = "name"
    val summary = """
      {"admin": {"summary": [{"key": "term1", "type": "TEXT", "label": "Label1", "operation": "COUNT"},
{"key": "term2", "type": "PERCENT", "label": "Label2", "operation": "AVE"},
 {"key": "term3", "type": "CURRENCY", "label": "Label3", "operation": "SUM"}]}, "client": {"summary": []}}
    """.trimMargin()
    val client = Client(clientId, clientTag, clientName, negotiationSummaryFields = summary)
    val termsMap = mapOf(
      "term1" to "hello",
      "term2" to "23.12",
      "term3" to "$1198.98"
    )
    SessionHelper.setLoggedInUser(token, email, listOf(Role.Admin))

    val negotiation = Negotiation(id, clientId, stateId, status,
      flowId, modelId, modelKey, flowVersionId, modelVersionId, Instant.now())
    val negotiationStates = listOf(NegotiationState.new(stateId, "{}", negotiation.id))

    whenever(negotiationRepository.findByClientIdAndIsDeletedIsFalseOrderByCreateTimeDesc(clientId)).thenReturn(
      listOf(negotiation, negotiation.copy(status = null))
    )
    whenever(clientRepository.findById(clientId)).thenReturn(Optional.of(client))
    whenever(extractService.getChatsTermsByStateIds(stateId)).thenReturn(listOf(termsMap))
    whenever(negotiationStateRepository.getStatesByStateId(stateId)).thenReturn(negotiationStates)
    val result = negotiationSummaryService.getNegotiationsSummaryByClientId(clientId)

    assertThat(result.totalCount).isEqualTo(2)
    assertThat(result.openedCount).isEqualTo(2)
    assertThat(result.finishedCount).isEqualTo(0)
    assertThat(result.extra[0].value).isEqualTo("2")
    assertThat(result.extra[1].value).isEqualTo("23.12%")
    assertThat(result.extra[2].value).isEqualTo("$2,397.96")
  }

  @Test
  fun `can get negotiations summary of this client if not admin and client tag is set`() {
    val email = "backend@pactum.com"
    val token = "avalidtoken"
    val clientId = 1L
    val stateId = "abc"
    val flowId = "qwe"
    val modelId = "123"
    val modelKey = "123"
    val thisClientTag = "tag"
    val clientName = "name"
    val summary = NegotiationSummaryFields(
      NegotiationSummaryList.empty(),
      NegotiationSummaryList(
        listOf(NegotiationSummaryField("term1", "label1", ExtraSummaryOperation.COUNT, ExtraValueFormat.LINK))
      )
    )
    val client = Client(
      clientId,
      thisClientTag,
      clientName,
      negotiationSummaryFields = jacksonObjectMapper().writeValueAsString(summary)
    )
    val termsMap: Map<String, Any> = mapOf(
      "term1" to 1,
      "term2" to true,
      "status" to "Agreement reached"
    )

    SessionHelper.setLoggedInUser(token, email, listOf(Role.Client)).apply {
      clientTag = thisClientTag
    }

    val negotiation = Negotiation.create(clientId, flowId, modelId, modelKey).copy(
      isVisibleSupplier = true,
      stateId = stateId
    )

    whenever(clientRepository.findFirstByTag(thisClientTag)).thenReturn(client)
    whenever(negotiationRepository.findByClientIdAndIsDeletedIsFalseOrderByCreateTimeDesc(clientId))
      .thenReturn(listOf(negotiation))
    whenever(clientRepository.findById(clientId)).thenReturn(Optional.of(client))
    whenever(extractService.getChatsTermsByStateIds(stateId)).thenReturn(listOf(termsMap))
    whenever(negotiationStateRepository.getStatesByStateId(stateId)).thenReturn(emptyList())

    val result = negotiationSummaryService.getNegotiationsSummaryForClient()

    assertThat(result.totalCount).isEqualTo(1)
    assertThat(result.openedCount).isEqualTo(0)
    assertThat(result.finishedCount).isEqualTo(1)
    assertThat(result.extra[0].label).isEqualTo("label1")
    assertThat(result.extra[0].value).isEqualTo("1")
  }

  @Test
  fun `can not get negotiations summary if not admin and clientTag is not set`() {
    val email = "backend@pactum.com"
    val token = "avalidtoken"

    SessionHelper.setLoggedInUser(token, email, listOf(Role.Client)).apply {
      clientTag = null
    }

    assertThrows<AccessDeniedException> {
      negotiationSummaryService.getNegotiationsSummaryForClient()
    }
  }
}
