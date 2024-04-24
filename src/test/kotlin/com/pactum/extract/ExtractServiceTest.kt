package com.pactum.extract

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.pactum.negotiationstate.NegotiationStateRepository
import com.pactum.chat.model.NegotiationState
import com.pactum.chat.model.NegotiationHistoryItem
import com.pactum.negotiation.NegotiationModelTermService
import com.pactum.negotiation.NegotiationRepository
import com.pactum.negotiation.model.Negotiation
import com.pactum.test.UnitTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import java.time.Instant

@UnitTest
class ExtractServiceTest {

  private val negotiationStateRepository: NegotiationStateRepository = mock()
  private val negotiationRepository: NegotiationRepository = mock()
  private val negotiationModelTermService: NegotiationModelTermService = mock()
  private val secretStateId = "secret"
  private val baseUrl = "https://www.pactum.com"

  private val extractService =
    ExtractService(
      negotiationStateRepository,
      negotiationRepository,
      negotiationModelTermService,
      secretStateId,
      baseUrl
    )

  @Test
  fun `can get state for terms by flowIds`() {
    val stateId = "asd"
    val flowId = "Nddqdqdqdq"
    val modelId = "modelId"
    val modelKey = "tefsada"
    val chatStartTime = Instant.parse("2019-08-12T21:03:33.123Z")
    val chatLastUpdateTime = Instant.parse("2019-09-12T21:03:33.123Z")
    val chatLink = "https://www.pactum.com/models/modelId/chat/Nddqdqdqdq/tefsada/asd/read-only"

    val termMap = mapOf("{term1}" to "Payment due date from 60 days to 7 days")
    val negotiation = Negotiation.create(1, flowId, modelId, modelKey, null).copy(
      id = 1,
      stateId = stateId,
      terms = jacksonObjectMapper().writeValueAsString(termMap),
      chatStartTime = chatStartTime,
      chatUpdateTime = chatLastUpdateTime
    )

    whenever(negotiationRepository.findByFlowIds(listOf(flowId, flowId))).thenReturn(listOf(negotiation))
    whenever(negotiationModelTermService.getTerms(negotiation)).thenReturn(termMap)

    val map = mutableMapOf<String, Any>()
    map["flowId"] = flowId
    map["modelId"] = modelId
    map["modelKey"] = modelKey
    map["chatStartTime"] = chatStartTime
    map["stateId"] = stateId
    map["chatLastUpdateTime"] = chatLastUpdateTime
    map["chatLink"] = chatLink
    map["duration"] = 44640.0
    map["{term1}"] = "Payment due date from 60 days to 7 days"
    val list = listOf<Map<String, Any>>(
      map
    )

    val response = extractService.getChatsTermsByFlowIds("$flowId,$flowId")
    assertThat(response).isEqualTo(list)
  }

  @Test
  fun `can get state for terms by stateIds`() {
    val stateId = "asd"
    val flowId = "Nddqdqdqdq"
    val modelId = "modelId"
    val modelKey = "tefsada"
    val chatStartTime = Instant.parse("2019-08-12T21:03:33.123Z")
    val chatLastUpdateTime = Instant.parse("2019-09-12T21:03:33.123Z")
    val chatLink = "https://www.pactum.com/models/modelId/chat/Nddqdqdqdq/tefsada/asd/read-only"

    val termMap = mapOf("{term1}" to "Payment due date from 60 days to 7 days")
    val negotiation = Negotiation.create(1, flowId, modelId, modelKey, null).copy(
      id = 1,
      stateId = stateId,
      terms = jacksonObjectMapper().writeValueAsString(termMap),
      chatStartTime = chatStartTime,
      chatUpdateTime = chatLastUpdateTime
    )

    whenever(negotiationRepository.findByStateIds(listOf(stateId, stateId))).thenReturn(listOf(negotiation))
    whenever(negotiationModelTermService.getTerms(negotiation)).thenReturn(termMap)

    val map = mutableMapOf<String, Any>()
    map["flowId"] = flowId
    map["modelId"] = modelId
    map["modelKey"] = modelKey
    map["chatStartTime"] = chatStartTime
    map["stateId"] = stateId
    map["chatLastUpdateTime"] = chatLastUpdateTime
    map["chatLink"] = chatLink
    map["duration"] = 44640.0
    map["{term1}"] = "Payment due date from 60 days to 7 days"
    val list = listOf<Map<String, Any>>(
      map
    )

    val response = extractService.getChatsTermsByStateIds("$stateId,$stateId")
    assertThat(response).isEqualTo(list)
  }

  @Test
  fun `can get history for by flowIds`() {
    val stateId = "asd"
    val flowId = "Nddqdqdqdq"

    val step1 = NegotiationState(
      stateId = stateId,
      time = Instant.parse("2019-08-12T21:03:33.123Z"),
      state = """{
        "currentStep": { "id": "1", "user": false, "message": "Hello"}
        }"""
    )
    val step2 = NegotiationState(
      stateId = stateId,
      time = Instant.parse("2019-08-12T21:03:44.123Z"),
      state = """{
        "currentStep": { "id": "2", "user": true, "message": "OK"}
        }"""
    )

    whenever(negotiationStateRepository.getStatesByFlowId(eq(flowId), anyString())).thenReturn(
      listOf(step1, step2)
    )

    val response = extractService.getChatsHistoryByFlowIds("$flowId,$flowId")
    assertThat(response).isEqualTo(
      listOf(
        NegotiationHistoryItem(
          id = "1",
          flowId = flowId,
          stateId = stateId,
          time = step1.time,
          message = "Hello",
          user = false,
          timeSincePreviousSec = null
        ),
        NegotiationHistoryItem(
          id = "2",
          stateId = stateId,
          flowId = flowId,
          time = step2.time,
          message = "OK",
          user = true,
          timeSincePreviousSec = 11
        ),
        NegotiationHistoryItem(
          id = "1",
          flowId = flowId,
          stateId = stateId,
          time = step1.time,
          message = "Hello",
          user = false,
          timeSincePreviousSec = null
        ),
        NegotiationHistoryItem(
          id = "2",
          stateId = stateId,
          flowId = flowId,
          time = step2.time,
          message = "OK",
          user = true,
          timeSincePreviousSec = 11
        )
      )
    )
  }

  @Test
  fun `can get history for by stateIds`() {
    val stateId = "asd"
    val flowId = "Nddqdqdqdq"

    val step1 = NegotiationState(
      stateId = stateId,
      time = Instant.parse("2019-08-12T21:03:33.123Z"),
      state = """{
        "currentStep": { "id": "1", "user": false, "message": "Hello"}
        }"""
    )
    val step2 = NegotiationState(
      stateId = stateId,
      time = Instant.parse("2019-08-12T21:03:44.123Z"),
      state = """{
        "currentStep": { "id": "2", "user": true, "message": "OK"}
        }"""
    )

    whenever(negotiationStateRepository.getStatesByStateId(eq(stateId), anyString())).thenReturn(
      listOf(step1, step2)
    )
    val returnNegotiation = Negotiation(
      clientId = 0,
      stateId = stateId,
      flowId = flowId,
      createTime = Instant.now(),
      modelId = "0",
      modelKey = "0"
    )

    whenever(
      negotiationRepository.findFirstByStateIdAndIsDeletedIsFalseOrderByCreateTimeDesc(stateId)
    ).thenReturn(returnNegotiation)

    val response = extractService.getChatsHistoryByStateIds("$stateId,$stateId")
    assertThat(response).isEqualTo(
      listOf(
        NegotiationHistoryItem(
          id = "1",
          flowId = flowId,
          stateId = stateId,
          time = step1.time,
          message = "Hello",
          user = false,
          timeSincePreviousSec = null
        ),
        NegotiationHistoryItem(
          id = "2",
          stateId = stateId,
          flowId = flowId,
          time = step2.time,
          message = "OK",
          user = true,
          timeSincePreviousSec = 11
        ),
        NegotiationHistoryItem(
          id = "1",
          flowId = flowId,
          stateId = stateId,
          time = step1.time,
          message = "Hello",
          user = false,
          timeSincePreviousSec = null
        ),
        NegotiationHistoryItem(
          id = "2",
          stateId = stateId,
          flowId = flowId,
          time = step2.time,
          message = "OK",
          user = true,
          timeSincePreviousSec = 11
        )
      )
    )
  }
}
