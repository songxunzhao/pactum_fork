package com.pactum.chat

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nhaarman.mockitokotlin2.whenever
import com.pactum.chat.mindmup.MindMupResponseFixture.Companion.mindMupConditionalsResponseFixture
import com.pactum.chat.mindmup.MindMupResponseFixture.Companion.mindMupResponseFixture
import com.pactum.chat.mindmup.MindMupService
import com.pactum.chat.mindmup.State
import com.pactum.chat.model.NegotiationState
import com.pactum.chat.model.BaseStep
import com.pactum.chat.model.TextStep
import com.pactum.chat.model.Trimmable
import com.pactum.chat.model.ChatApiInput
import com.pactum.client.ClientRepository
import com.pactum.client.model.Client
import com.pactum.negotiation.NegotiationRepository
import com.pactum.negotiation.model.Negotiation
import com.pactum.negotiationstate.ChatStepException
import com.pactum.negotiationstate.NegotiationStateRepository
import com.pactum.test.IntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean

@IntegrationTest
class ChatServiceIntegrationTest {

  @Autowired
  private lateinit var chatService: ChatService

  @Autowired
  private lateinit var negotiationStateRepository: NegotiationStateRepository

  @Autowired
  private lateinit var negotiationRepository: NegotiationRepository

  @Autowired
  private lateinit var clientRepository: ClientRepository

  @MockBean
  private lateinit var mindMupService: MindMupService

  @AfterEach
  fun `clean up`() {
    negotiationStateRepository.deleteAll()
    negotiationRepository.deleteAll()
    clientRepository.deleteAll()
  }

  @Test
  fun `validates if next step is correctly`() {
    val flowId = "1Simple"
    val stateId = "1Si1Ale"
    val state = """
      { 
        "currentStep": {
          "id": "1",
          "message": "",
          "@class": ".TextStep",
          "trigger": "2.f745.dc70c5aaf-4010.f36e69ad1",
          "nextId": "2.f745.dc70c5aaf-4010.f36e69ad1"
        },
        "previousStep": {
          "id": "0",
          "message": "",
          "@class": ".TextStep",
          "trigger": "1"
        },
        "renderedSteps": []
      }
    """
    val stepId = "2.f745.dc70c5aaf-4010.f36e69ad1"

    whenever(mindMupService.getMup(flowId)).thenReturn(mindMupResponseFixture())
    val clientId = clientRepository.save(Client.create("tag", "name")).id!!
    val negId = negotiationRepository.save(Negotiation.create(clientId, flowId, stateId)).id!!
    negotiationStateRepository.save(NegotiationState(stateId = stateId, state = state, negotiationId = negId))

    val step = chatService.getChatStep(
      ChatApiInput(flowId = flowId, stateId = stateId, stepId = stepId)
    ).last()

    assertThat(step.id).isEqualTo(stepId)
  }

  @Test
  fun `can fill the demo template`() {
    val inputVariables = mapOf(
      "dateStart" to "2019-01-01",
      "dateEnd" to "2020-01-01",
      "pricePremium" to 1.0,
      "priceRegular" to 2.0,
      "priceWeekend" to 3,
      "paymentDue" to 4,
      "exclusivityPeriod" to 5,
      "deliveryPenalty" to 6,
      "cancellationFee" to 7
    )

    val resultHTML = chatService.getFilledDemoContractHTML(inputVariables)

    assertThat(resultHTML.contains("}]]")).isEqualTo(false)
    assertThat(resultHTML.contains("2019-01-01")).isEqualTo(true)
  }

  @Test
  fun `can get demo contract PDF base 64`() {
    val stateId = "uej5ythrsa"
    val state = """
    {
      "renderedSteps": [
        {
          "id": "test1",
          "variable": "{terms}",
          "value": {
            "exclusivityPeriod": 2, 
            "paymentDue": 60, 
            "deliveryPenalty": 100, 
            "cancellationFee": 100, 
            "pricePremium": "1", 
            "priceRegular": 22.6, 
            "priceWeekend": 10,
            "contractDurationYears": 2
          }
        }
      ]
    }"""

    negotiationStateRepository.save(NegotiationState(stateId = stateId, state = state))

    println(chatService.getDemoContractPDFBase64(stateId))
  }

  @Test
  fun `validates if next step is evaluated correctly`() {
    val flowId = "1Simple"
    val stateId = "1Si1Ale"
    val state = """
      { 
        "currentStep": {
          "id": "1",
          "message": "",
          "@class": ".TextStep",
          "trigger": "2.f745.dc70c5aaf-4010.f36e69ad1",
          "nextId": "2.f745.dc70c5aaf-4010.f36e69ad1"
        },
        "previousStep": {
          "id": "0",
          "message": "",
          "@class": ".TextStep",
          "trigger": "1"
        },
        "renderedSteps": []
      }
    """
    val stepId = "2.f745.dc70c5aaf-4010.f36e69ad1"

    whenever(mindMupService.getMup(flowId)).thenReturn(mindMupResponseFixture())

    val clientId = clientRepository.save(Client.create("tag", "name")).id!!
    val negId = negotiationRepository.save(Negotiation.create(clientId, flowId, stateId)).id!!
    negotiationStateRepository.save(NegotiationState(stateId = stateId, state = state, negotiationId = negId))

    val step = chatService.getChatStep(
      ChatApiInput(flowId = flowId, stateId = stateId, stepId = stepId)
    ).last()

    assertThat(step.id).isEqualTo(stepId)
  }

  @Test
  fun `Should return the current step when the step id corresponds to it`() {
    val flowId = "1Simple"
    val stateId = "1Si1Ale"
    val state = """
      { 
        "currentStep": {
          "id": "1",
          "message": "Current step",
          "@class": ".TextStep",
          "trigger": "2.f745.dc70c5aaf-4010.f36e69ad1",
          "nextId": "2.f745.dc70c5aaf-4010.f36e69ad1"
        },
        "previousStep": {
          "id": "0",
          "message": "Previous step",
          "@class": ".TextStep",
          "trigger": "1"
        },
        "renderedSteps": []
      }
    """
    val stepId = "1"

    whenever(mindMupService.getMup(flowId)).thenReturn(mindMupResponseFixture())
    val clientId = clientRepository.save(Client.create("tag", "name")).id!!
    val negId = negotiationRepository.save(Negotiation.create(clientId, flowId, stateId)).id!!
    negotiationStateRepository.save(NegotiationState(stateId = stateId, state = state, negotiationId = negId))

    val step = chatService.getChatStep(
      ChatApiInput(flowId = flowId, stateId = stateId, stepId = stepId)
    ).last()

    assertThat(step.id).isEqualTo(stepId)
  }

  @Test
  fun `throws exception if step is not reachable`() {
    val flowId = "1Simple"
    val stateId = "1Si1Ale"
    val state = """
      { 
        "currentStep": {
          "id": "1",
          "message": "",
          "@class": ".TextStep",
          "trigger": "2.f745.dc70c5aaf-4010.f36e69ad1"
        },
        "previousStep": {
          "id": "0",
          "message": "",
          "@class": ".TextStep",
          "trigger": "1"
        },
        "renderedSteps": []
      }
    """
    val stepId = "2"

    whenever(mindMupService.getMup(flowId)).thenReturn(mindMupResponseFixture())

    negotiationStateRepository.save(NegotiationState(stateId = stateId, state = state))

    assertThatExceptionOfType(ChatStepException::class.java).isThrownBy {
      chatService.getChatStep(
        ChatApiInput(flowId = flowId, stateId = stateId, stepId = stepId)
      )
    }.withMessage("Invalid chat step ID: 2")
  }

  @Test
  fun `should add the state into the database if it does not exist and get the first step`() {
    val flowId = "7BackwardLinksWithConditionals"
    val stateId = "7Ba1Als"

    val mindMupResponse = mindMupConditionalsResponseFixture()

    whenever(mindMupService.getMup(flowId)).thenReturn(mindMupResponse)
    val clientId = clientRepository.save(Client.create("tag", "name")).id!!
    val negId = negotiationRepository.save(Negotiation.create(clientId, flowId, stateId)).id!!

    val step = chatService.getChatStep(
      ChatApiInput(flowId = flowId, stateId = stateId)
    ).last()
    assertThat(step.id).isEqualTo(mindMupResponse.toChat().steps[0].id)

    val chatState = negotiationStateRepository.findByStateId(stateId)!!
    val state = jacksonObjectMapper().readValue(chatState.state, State::class.java)
    val trimStep = fun(it: BaseStep): BaseStep { return if (it is Trimmable) it.trim() else it }
    val currentStep = state.currentStep.let(trimStep)
    val renderedStep = state.renderedSteps[0].let(trimStep)

    negotiationStateRepository.save(NegotiationState(stateId = stateId, state = chatState.state, negotiationId = negId))

    assertThat(currentStep).isEqualTo(step)
    assertThat(renderedStep).isEqualTo(step)
  }

  @Test
  fun `Should return the read only steps with null trigger in the last step`() {
    val flowId = "1Simple"
    val stateId = "1Si1Ale"
    val state = """
      { 
        "currentStep": {
          "id": "1",
          "message": "",
          "@class": ".TextStep",
          "trigger": "2.f745.dc70c5aaf-4010.f36e69ad1"
        },
        "previousStep": {
          "id": "0",
          "message": "",
          "@class": ".TextStep",
          "trigger": "1"
        },
        "renderedSteps": [
          {
            "id": "0",
            "message": "",
            "@class": ".TextStep",
            "trigger": "1"
          },
          {
          "id": "1",
          "message": "",
          "@class": ".TextStep",
          "trigger": "2.f745.dc70c5aaf-4010.f36e69ad1"
          }
        ]
      }
    """
    val stepId = null

    whenever(mindMupService.getMup(flowId)).thenReturn(mindMupResponseFixture())
    negotiationStateRepository.deleteAll()
    negotiationStateRepository.save(NegotiationState(stateId = stateId, state = state))

    val steps = chatService.getChatStep(
      ChatApiInput(flowId = flowId, stateId = stateId, stepId = stepId, readOnly = true)
    )
    val lastStep = steps.last() as TextStep

    assertThat(steps.size).isEqualTo(2)
    assertThat(lastStep.trigger).isNull()
  }
}
