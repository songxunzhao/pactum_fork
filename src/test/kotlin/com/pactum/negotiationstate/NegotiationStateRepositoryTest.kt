package com.pactum.negotiationstate

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.pactum.chat.model.NegotiationState
import com.pactum.client.ClientRepository
import com.pactum.client.model.Client
import com.pactum.negotiation.NegotiationRepository
import com.pactum.negotiation.model.Negotiation
import com.pactum.test.RepositoryTest
import com.pactum.utils.JsonHelper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@RepositoryTest
class NegotiationStateRepositoryTest @Autowired constructor(
  private val negotiationStateRepository: NegotiationStateRepository,
  private val negotiationRepository: NegotiationRepository,
  private val clientRepository: ClientRepository
) {

  @Test
  fun `finds state by state id`() {
    val chatState = NegotiationState(
      stateId = "stateId",
      state = "{}"
    )
    val savedChatState = negotiationStateRepository.save(chatState)

    val foundChatState = negotiationStateRepository.findByStateId(chatState.stateId)

    assertThat(foundChatState).isEqualToComparingOnlyGivenFields(savedChatState)
    assertThat(foundChatState!!.time).isNotNull()
  }

  @Test
  fun `finds list of state IDs by chat id`() {
    val flowId = "vsre45g"
    val modelId = "asdads"
    val modelKey = "123"

    val client = Client.create("tag", "name")
    val savedClient = clientRepository.save(client)
    val negotiation = Negotiation.create(savedClient.id!!, flowId, modelId, modelKey)
    val savedNegotiation = negotiationRepository.save(negotiation)

    val chatState = NegotiationState(
      stateId = "r23qwefaabc",
      state = "{}",
      negotiationId = savedNegotiation.id
    )
    val chatState2 = NegotiationState(
      stateId = "abc123",
      state = "{}",
      negotiationId = savedNegotiation.id
    )
    negotiationStateRepository.saveAll(listOf(chatState, chatState2))

    val foundChatStateId = negotiationStateRepository.getAllStateIdsByFlowId(flowId)
    assertThat(foundChatStateId).contains(chatState.stateId, chatState2.stateId)

    val filteredChatStateId = negotiationStateRepository.getAllStateIdsByFlowId(flowId, "abc")
    assertThat(filteredChatStateId).contains(chatState.stateId)
    assertThat(filteredChatStateId).doesNotContain(chatState2.stateId)
  }

  @Test
  fun `finds list of states by chat id in proper order`() {
    val flowId = "vsre45g"
    val modelId = "asdads"
    val modelKey = "123"

    val client = Client.create("tag", "name")
    val savedClient = clientRepository.save(client)
    val negotiation = Negotiation.create(savedClient.id!!, flowId, modelId, modelKey)
    val savedNegotiation = negotiationRepository.save(negotiation)

    val chatState1 = NegotiationState(
      stateId = "abc123",
      state = "{}",
      negotiationId = savedNegotiation.id
    )
    val chatState2 = NegotiationState(
      stateId = "def456",
      state = "{}",
      negotiationId = savedNegotiation.id
    )
    val chatState3 = NegotiationState(
      stateId = "saladus",
      state = "{}",
      negotiationId = savedNegotiation.id
    )
    val chatState4 = NegotiationState(
      stateId = "saladus123",
      state = "{}",
      negotiationId = savedNegotiation.id
    )

    negotiationStateRepository.saveAll(listOf(chatState1, chatState2, chatState3, chatState4))
    val foundStates = negotiationStateRepository.getStatesByFlowId(flowId)
    assertThat(foundStates).hasSize(4)
    assertThat(foundStates[0]).isEqualToComparingOnlyGivenFields(chatState1)
    assertThat(foundStates[1]).isEqualToComparingOnlyGivenFields(chatState2)
    assertThat(foundStates[2]).isEqualToComparingOnlyGivenFields(chatState3)

    val foundPublicStates = negotiationStateRepository.getStatesByFlowId(flowId, "saladus")
    assertThat(foundPublicStates).hasSize(2)
    assertThat(foundPublicStates[0]).isEqualToComparingOnlyGivenFields(chatState1)
    assertThat(foundPublicStates[1]).isEqualToComparingOnlyGivenFields(chatState2)
  }

  @Test
  fun `finds all variables from state by chat id`() {
    val chatState = NegotiationState(
      stateId = "stateId",
      state = """
    {
      "renderedSteps": [
        {
          "id": "test1",
          "variable": "{term1}",
          "key": "Jk17eaSKUqU5tVJPfToTghg2",
          "delay": 1000,
          "value": "Payment due date from 60 days to 7 days",
          "metadata": {
            "timestamp": "2019-09-27T08:12:47.796Z"
          }
        },
        {
          "id": "test2",
          "variable": "{term2}",
          "value": "Exclusivity period from 2 year to 3 years"
        },
        {
          "id": "132.c18b.d2cabf514-872f.e4f65d612",
          "message": "Good to know. Your preferences are longer exclusivity period and faster payment."
        },
        {
          "id": "test3",
          "variable": "{4h_transfer}",
          "value": "9"
        }
      ]
    }
      """
    )
    negotiationStateRepository.save(chatState)

    val expectedResult00 = "term1"
    val expectedResult22 = "9"

    val foundKeyValueWithStringsList = negotiationStateRepository.getAllVariablesByStateId(chatState.stateId)

    if (foundKeyValueWithStringsList != null) {
      assertThat(foundKeyValueWithStringsList.first().key).isEqualTo(expectedResult00)
      assertThat(foundKeyValueWithStringsList.last().value).isEqualTo(expectedResult22)
    } else {
      assertThat(1).isEqualTo(2)
    }
  }

  @Test
  fun `find state for terms`() {
    val stateId = "456"
    val chatState = NegotiationState(
      stateId = stateId,
      state = """
    {
      "renderedSteps": [
        {
          "id": "{term1}",
          "key": "Jk17eaSKUqU5tVJPfToTghg2",
          "delay": 1000,
          "value": "Payment due date from 60 days to 7 days",
          "metadata": {
            "timestamp": "2019-09-27T08:12:47.796Z"
          }
        },
        {
          "id": "{term2}",
          "value": "Exclusivity period from 2 year to 3 years"
        },
        {
          "id": "132.c18b.d2cabf514-872f.e4f65d612",
          "message": "Good to know. Your preferences are longer exclusivity period and faster payment."
        },
        {
          "id": "{4h_transfer}",
          "value": "9"
        }
      ]
    }
      """
    )
    negotiationStateRepository.save(chatState)

    var state = negotiationStateRepository.findByStateId("invalid stated id")
    assert(state == null)
    state = negotiationStateRepository.findByStateId(stateId)
    val json = JsonHelper.toJsonObject(state?.state!!)
    val renderedSteps = json["renderedSteps"] as ArrayList<*>
    assert(renderedSteps.size == 4)
    val stepJson = JsonHelper.toJsonObject(jacksonObjectMapper().writeValueAsString(renderedSteps[3]))
    assert(stepJson["id"]!!.equals("{4h_transfer}"))
    assert(stepJson["value"]!!.equals("9"))
  }
}
