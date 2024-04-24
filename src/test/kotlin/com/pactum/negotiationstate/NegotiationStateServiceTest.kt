package com.pactum.negotiationstate

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.pactum.chat.mindmup.MindMupResponseFixture
import com.pactum.chat.mindmup.State
import com.pactum.chat.model.Chat
import com.pactum.chat.model.NegotiationState
import com.pactum.chat.model.ConditionalTextStep
import com.pactum.chat.model.ConditionalUserInputStep
import com.pactum.chat.model.Step
import com.pactum.chat.model.TextStep
import com.pactum.chat.model.TriggerStep
import com.pactum.chat.model.Trimmable
import com.pactum.chat.model.UserInputStep
import com.pactum.chat.model.ValueStep
import com.pactum.chat.model.ChatApiInput
import com.pactum.chat.model.StepIDGenerator
import com.pactum.client.ClientRepository
import com.pactum.negotiationasset.NegotiationAssetRepository
import com.pactum.embedded.KotlinExpressionEval
import com.pactum.embedded.KotlinTriggerEval
import com.pactum.embedded.ScriptEngine
import com.pactum.model.ModelResponseFixture
import com.pactum.model.ModelService
import com.pactum.negotiation.NegotiationNotFoundException
import com.pactum.negotiation.NegotiationRepository
import com.pactum.negotiation.NegotiationService
import com.pactum.negotiation.model.Negotiation
import com.pactum.negotiationfield.NegotiationFieldService
import com.pactum.state.NegotiationStateService
import com.pactum.test.UnitTest
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@UnitTest
class NegotiationStateServiceTest {
  private val negotiationStateRepository: NegotiationStateRepository = mock()
  private val negotiationFieldService: NegotiationFieldService = mock()
  private val negotiationRepository: NegotiationRepository = mock()
  private val negotiationAssetRepository: NegotiationAssetRepository = mock()
  private val clientRepository: ClientRepository = mock()
  private val negotiationService: NegotiationService = mock()
  private val modelService: ModelService = mock()
  private val expressionEval = KotlinExpressionEval()
  private val triggerEval = KotlinTriggerEval()
  private val secretState = "secret"
  private val defaultFlowId = "default"
  private val demoClienTag = "demo"

  private val stateService =
    NegotiationStateService(
      negotiationStateRepository,
      negotiationRepository,
      negotiationAssetRepository,
      negotiationFieldService,
      clientRepository,
      expressionEval,
      triggerEval,
      negotiationService,
      modelService,
      demoClienTag,
      secretState,
      defaultFlowId
    )

  @AfterEach
  fun `reset script engine to KTS`() {
    stateService.setScriptEngine(ScriptEngine.KTS)
  }

  @Test
  fun `should return the initial step from the secret state when stepId is null`() {
    val flowId = "1Simple"
    val stateId = "123".plus(secretState).plus("345")
    val chat = MindMupResponseFixture.mindMupResponseFixture().toChat()

    val step = stateService.getChatStep(
      chat,
      ChatApiInput(stateId = stateId, flowId = flowId)
    ).last()
    val chatStep = (chat.steps[0] as Trimmable).trim()
    assertThat(step).isEqualTo(chatStep)
  }

  @Test
  fun `should throw exception if negotiation not found`() {
    val flowId = "1Simple"
    val chat = MindMupResponseFixture.mindMupResponseFixture().toChat()
    val stateId = "1Si1Ale"

    whenever(negotiationStateRepository.findByStateId(stateId)).thenReturn(null)
    whenever(negotiationRepository.findFirstByStateIdAndIsDeletedIsFalseOrderByCreateTimeDesc(stateId)).thenReturn(null)

    assertThrows<NegotiationNotFoundException> {
      stateService.getChatStep(
        chat,
        ChatApiInput(stateId = stateId, flowId = flowId, shouldCreateNegotiationIfNotFound = false)
      )
    }
  }

  @Test
  fun `should throw exception with invalid state id`() {
    val flowId = "1Simple"
    val chat = MindMupResponseFixture.mindMupResponseFixture().toChat()
    val stateId = "stateId"

    whenever(negotiationStateRepository.findByStateId(stateId)).thenReturn(null)
    whenever(negotiationRepository.findFirstByStateIdAndIsDeletedIsFalseOrderByCreateTimeDesc(stateId)).thenReturn(null)

    assertThrows<ChatStateNotFoundException> {
      stateService.getChatStep(
        chat,
        ChatApiInput(stateId = stateId, flowId = flowId)
      )
    }
  }

  @Test
  fun `should extract the initial step if the chat state is not found`() {
    val flowId = "1Simple"
    val chat = MindMupResponseFixture.mindMupResponseFixture().toChat()
    val stateId = "1Si1Ale"

    whenever(negotiationStateRepository.findByStateId(stateId)).thenReturn(null)
    whenever(negotiationRepository.findFirstByStateIdAndIsDeletedIsFalseOrderByCreateTimeDesc(stateId))
      .thenReturn(Negotiation.empty(0))

    val step = stateService.getChatStep(
      chat,
      ChatApiInput(stateId = stateId, flowId = flowId)
    ).last()

    val chatStep = (chat.steps[0] as Trimmable).trim()

    assertThat(step).isEqualTo(chatStep)
    verify(negotiationStateRepository).save(any<NegotiationState>())
  }

  @Test
  fun `should return the appropriate step from simple chat and state`() {
    val flowId = "1Simple"
    val stateId = "1Si1Ale"
    val stepId = "2.f745.dc70c5aaf-4010.f36e69ad1"
    val state = """
      {
        "currentStep":{
          "@class":".TextStep",
          "id":"1",
          "evalExpression":"",
          "message":"Hi!",
          "trigger":"2.f745.dc70c5aaf-4010.f36e69ad1"
        },
        "previousStep":null,
        "renderedSteps": [
          {
            "@class":".TextStep",
            "id":"1",
            "evalExpression":"",
            "message":"Hi!",
            "trigger":"2.f745.dc70c5aaf-4010.f36e69ad1"
          }
        ]
      }
    """
    val chatState = NegotiationState(stateId = stateId, state = state)

    whenever(negotiationStateRepository.findByStateId(stateId)).thenReturn(chatState)
    whenever(negotiationStateRepository.save<NegotiationState>(any())).thenAnswer { i -> i.arguments[0] }
    whenever(negotiationRepository.findFirstByStateIdAndIsDeletedIsFalseOrderByCreateTimeDesc(stateId))
      .thenReturn(Negotiation.empty(0))

    val chat = MindMupResponseFixture.mindMupResponseFixture().toChat()

    val step = stateService.getChatStep(
      chat,
      ChatApiInput(stateId = stateId, stepId = stepId, flowId = flowId)
    ).last()

    assertThat(step.id).isEqualTo(stepId)
  }

  @Test
  fun `should replace chat variables when getting next step`() {
    val flowId = "flowId"
    val stateId = "flo111AAAId"
    val stepId = "2"
    val state = """
      {
        "currentStep":{
          "@class":".UserInputStep",
          "id":"{name}",
          "evalExpression":"",
          "user":true,
          "message":"Firstname Lastname",
          "value":"Firstname Lastname",
          "trigger":"2"
        },
        "previousStep":null,
        "renderedSteps": [
          {
            "@class":".TextStep",
            "id":"1",
            "evalExpression":"",
            "message":"Hi! Please enter your name",
            "trigger":"2"
          },
          {
            "@class":".UserInputStep",
            "id":"{name}",
            "evalExpression":"",
            "user":true,
            "message":"Firstname Lastname",
            "value":"Firstname Lastname",
            "trigger":"2"
          }
        ]
      }
    """
    val chatState = NegotiationState(stateId = stateId, state = state)

    whenever(negotiationStateRepository.findByStateId(stateId)).thenReturn(chatState)
    whenever(negotiationStateRepository.save<NegotiationState>(any())).thenAnswer { i -> i.arguments[0] }
    whenever(negotiationRepository.findFirstByStateIdAndIsDeletedIsFalseOrderByCreateTimeDesc(stateId))
      .thenReturn(Negotiation.empty(0))

    val chat = Chat(
      steps = listOf(
        TextStep(
          id = "1",
          message = "Hi!",
          trigger = "2"
        ),
        TextStep(
          id = "2",
          message = "Your name is {name}. Also, you previously entered {previousValue}",
          end = true
        )
      )
    )

    val step = stateService.getChatStep(
      chat,
      ChatApiInput(stateId = stateId, stepId = stepId, flowId = flowId)
    )[0]

    assertThat(step.id).isEqualTo("2")
    assertThat((step as TextStep).message).isEqualTo(
      "Your name is Firstname Lastname. Also, you previously entered Firstname Lastname"
    )
  }

  @Test
  fun `should return the current step if the step id corresponds to it`() {
    val flowId = "1Simple"
    val stateId = "1Si1Ale"
    val stepId = "1"
    val state = """
      {
        "currentStep":{
          "@class":".TextStep",
          "id":"1",
          "evalExpression":"",
          "message":"Hi!",
          "trigger":"2.f745.dc70c5aaf-4010.f36e69ad1"
        },
        "previousStep":null,
        "renderedSteps": [
          {
            "@class":".TextStep",
            "id":"1",
            "evalExpression":"",
            "message":"Hi!",
            "trigger":"2.f745.dc70c5aaf-4010.f36e69ad1"
          }
        ]
      }
    """
    val chatState = NegotiationState(stateId = stateId, state = state)

    whenever(negotiationStateRepository.findByStateId(stateId)).thenReturn(chatState)
    whenever(negotiationStateRepository.save<NegotiationState>(any())).thenAnswer { i -> i.arguments[0] }
    whenever(negotiationRepository.findFirstByStateIdAndIsDeletedIsFalseOrderByCreateTimeDesc(stateId))
      .thenReturn(Negotiation.empty(0))

    val chat = MindMupResponseFixture.mindMupResponseFixture().toChat()

    val step = stateService.getChatStep(
      chat,
      ChatApiInput(stateId = stateId, stepId = stepId, flowId = flowId)
    ).last()

    assertThat(step.id).isEqualTo(stepId)
  }

  @Test
  fun `should return all rendered steps except value steps when flowId is not null and stepId is null`() {
    val flowId = "1Simple"
    val stateId = "1Si1Ale"
    val state = """
      {
        "currentStep":{
          "@class":".TextStep",
          "id":"1",
          "evalExpression":"",
          "message":"Hi!",
          "trigger":"2.f745.dc70c5aaf-4010.f36e69ad1"
        },
        "previousStep":null,
        "renderedSteps": [
          {
            "@class":".TextStep",
            "id":"1",
            "evalExpression":"This is test eval expression",
            "message":"Hi!",
            "trigger":"2.f745.dc70c5aaf-4010.f36e69ad1"
          },
          {
            "@class":".ValueStep",
            "id":"{value}",
            "value":"Some value"
          }
        ]
      }
    """
    val chatState = NegotiationState(stateId = stateId, state = state)

    whenever(negotiationStateRepository.findByStateId(stateId)).thenReturn(chatState)
    whenever(negotiationStateRepository.save<NegotiationState>(any())).thenAnswer { i -> i.arguments[0] }

    val chat = MindMupResponseFixture.mindMupResponseFixture().toChat()

    val steps = stateService.getChatStep(
      chat,
      ChatApiInput(stateId = stateId, flowId = flowId)
    )

    assertThat(steps).isNotEmpty()
    assertThat(steps.size).isEqualTo(1)
    assertThat(steps[0] is TextStep).isTrue()
    assertThat((steps[0] as Step).evalExpression).isNullOrEmpty()
  }

  @Test
  fun `should evaluate expression and add new value steps into state`() {
    val flowId = "28EvaluateNotesAsKotlin"
    val stateId = "28E1Ain"
    val stepId = "5.f745.dc70c5aaf-4010.f36e69ad1"
    val state = """
      {
        "currentStep":{
          "@class":".TextStep",
          "id":"{terms}",
          "evalExpression":"",
          "value":{"term1":1},
          "trigger":"5.f745.dc70c5aaf-4010.f36e69ad1",
          "label":"Option A",
          "user":true,
          "message":"Option A"
        },
        "renderedSteps":[{
          "@class":".TextStep",
          "id":"1",
          "evalExpression":"",
          "message":"Hi!",
          "trigger":"2.f745.dc70c5aaf-4010.f36e69ad1"
        },{
          "@class":".TextStep",
          "id":"2.f745.dc70c5aaf-4010.f36e69ad1",
          "evalExpression":"",
          "message":"Choose!",
          "trigger":"{terms}"
        },{
          "@class":".TextStep",
          "id":"{terms}",
          "evalExpression":"",
          "value":{"term1":1},
          "trigger":"5.f745.dc70c5aaf-4010.f36e69ad1",
          "label":"Option A",
          "user":true,
          "message":"Option A"
        }]}
    """
    reset(negotiationStateRepository)

    val mindMupResponse = MindMupResponseFixture.mindMupKotlinEvalExpressionFixture()
    val chat = mindMupResponse.toChat()
    val chatState = NegotiationState(stateId = stateId, state = state)

    whenever(negotiationStateRepository.findByStateId(stateId)).thenReturn(chatState)
    whenever(negotiationStateRepository.save<NegotiationState>(any())).thenAnswer { i -> i.arguments[0] }
    whenever(negotiationRepository.findFirstByStateIdAndIsDeletedIsFalseOrderByCreateTimeDesc(stateId))
      .thenReturn(Negotiation.empty(0))

    val steps = stateService.getChatStep(
      chat,
      ChatApiInput(stateId = stateId, stepId = stepId, flowId = flowId)
    )

    argumentCaptor<NegotiationState>().apply {
      verify(negotiationStateRepository).save(capture())
      val lastValueStep = jacksonObjectMapper().readValue(
        firstValue.state,
        State::class.java
      ).renderedSteps.findLast { it is ValueStep } as ValueStep
      assertThat(lastValueStep.value).isEqualTo(mapOf("term1" to 2))
    }

    val step = steps.last() as TextStep
    assertThat(step.id).isEqualTo(stepId)
    assertThat(step.message).isEqualTo("Thanks! 2")
  }

  @Test
  fun `should evaluate expression which contains JS array reduce method`() {
    val flowId = "28EvaluateNotesAsKotlin"
    val stateId = "28E1Ain"
    val stepId = "122.612b.42960491-4fd8.76509c8ba"
    val state = """
      {
        "currentStep": {
          "id": "{cart}",
          "user": true,
          "value": [
            {"name": "Guitar", "price": 100},
            {"name": "Piano", "price": 300}
          ],
          "@class": ".TextStep",
          "message": "Guitar, Piano",
          "trigger": "122.612b.42960491-4fd8.76509c8ba",
          "evalExpression": ""
        },
        "previousStep": {
          "id": "120.7e96.caf729688-8c73.187d5840d",
          "user": false,
          "value": null,
          "@class": ".TextStep",
          "message": "What do you want to buy?",
          "trigger": "{cart}",
          "evalExpression": ""
        },
        "renderedSteps": [{
          "id": "120.7e96.caf729688-8c73.187d5840d",
          "user": false,
          "value": null,
          "@class": ".TextStep",
          "message": "What do you want to buy?",
          "trigger": "{cart}",
          "evalExpression": ""
        }, {
          "id": "{cart}",
          "user": true,
          "value": [
            {"name": "Guitar", "price": 100},
            {"name": "Piano", "price": 300}
          ],
          "@class": ".TextStep",
          "message": "Guitar, Piano",
          "trigger": "122.612b.42960491-4fd8.76509c8ba",
          "evalExpression": ""
        }]}
    """
    reset(negotiationStateRepository)

    val mindMupResponse = MindMupResponseFixture.mindMupJSONValuesInMultipleChoiceFixture()
    val chat = mindMupResponse.toChat()
    val chatState = NegotiationState(stateId = stateId, state = state)

    whenever(negotiationStateRepository.findByStateId(stateId)).thenReturn(chatState)
    whenever(negotiationStateRepository.save<NegotiationState>(any())).thenAnswer { i -> i.arguments[0] }
    whenever(negotiationRepository.findFirstByStateIdAndIsDeletedIsFalseOrderByCreateTimeDesc(stateId))
      .thenReturn(Negotiation.empty(0))

    stateService.setScriptEngine(ScriptEngine.JS)

    val steps = stateService.getChatStep(
      chat,
      ChatApiInput(stateId = stateId, stepId = stepId, flowId = flowId)
    )

    val step = steps.last() as TextStep
    assertThat(step.id).isEqualTo(stepId)
    assertThat(step.message).contains("Total price: 400")
    assertThat(step.message).contains("Guitar\t \$100")
    assertThat(step.message).contains("Piano\t \$300")
  }

  @Test
  fun `should return step with evaluated trigger with value update`() {
    val flowId = "7BackwardLinksWithConditionals"
    val stateId = "7Ba1Bls"
    val stepId = "{number}"
    val value = "123"
    val triggerValue = "15.306b.5891d1449-d168.a8ecf6009"
    val state = """
      {
        "currentStep":{
          "@class":".ConditionalUserInputStep",
          "id":"{number}",
          "evalExpression":"",
          "user":true,
          "message":"123",
          "trigger":{
            "(value as String).toInt() >= 100":"15.306b.5891d1449-d168.a8ecf6009",
            "(value as String).toInt() < 100":"6.f745.dc70c5aaf-4010.f36e69ad1"
          }
        },
        "renderedSteps":[
          {
            "@class":".ConditionalUserInputStep",
            "id":"{number}",
            "evalExpression":"",
            "user":true,
            "message":"123",
            "value":123,
            "trigger":{
              "(value as String).toInt() >= 100":"15.306b.5891d1449-d168.a8ecf6009",
              "(value as String).toInt() < 100":"6.f745.dc70c5aaf-4010.f36e69ad1"
            }
          }
        ]
      }
    """
    val mindMupResponse = MindMupResponseFixture.mindMupConditionalsResponseFixture()
    val chat = mindMupResponse.toChat()
    val chatState = NegotiationState(stateId = stateId, state = state)

    whenever(negotiationStateRepository.findByStateId(stateId)).thenReturn(chatState)
    whenever(negotiationStateRepository.save<NegotiationState>(any())).thenAnswer { i -> i.arguments[0] }
    whenever(negotiationRepository.findFirstByStateIdAndIsDeletedIsFalseOrderByCreateTimeDesc(stateId))
      .thenReturn(Negotiation.empty(0))

    val step = stateService.updateCurrentStep(
      chat,
      ChatApiInput(stateId = stateId, stepId = stepId, value = value, flowId = flowId)
    ).last()
    val triggerStep = step as TriggerStep

    assertThat(triggerStep.trigger).isEqualTo(triggerValue)
    assertThat(triggerStep).isInstanceOf(UserInputStep::class.java)
    assertThat((triggerStep as UserInputStep).value).isEqualTo(value)
  }

  @Test
  fun `should return step with evaluated trigger with no value update `() {

    val flowId = "29ConditionalTextStep"
    val stateId = "29C1Aep"
    val stepId = "10.d290.32107c65c-3a6a.f83c0e1e2"
    val triggerValue = "11.d290.32107c65c-3a6a.f83c0e1e2"
    val state = """
      {
        "currentStep":{
          "@class":".TextStep",
          "id": "15.d290.32107c65c-3a6a.f83c0e1e2",
          "message": "",
          "trigger": "10.d290.32107c65c-3a6a.f83c0e1e2"
        },
        "renderedSteps":[
          {
            "@class":".TextStep",
            "id": "15.d290.32107c65c-3a6a.f83c0e1e2",
            "message": "",
            "trigger": "10.d290.32107c65c-3a6a.f83c0e1e2"
          }, {
            "@class": ".TextStep",
            "id": "{multiple_choice1}",
            "message": "5",
            "value": "5"
          }
        ]
      }
    """
    val mindMupResponse = MindMupResponseFixture.mindMupConditionalTextStepFixture()
    val chat = mindMupResponse.toChat()
    val chatState = NegotiationState(stateId = stateId, state = state)

    whenever(negotiationStateRepository.findByStateId(stateId)).thenReturn(chatState)
    whenever(negotiationStateRepository.save<NegotiationState>(any())).thenAnswer { i -> i.arguments[0] }
    whenever(negotiationRepository.findFirstByStateIdAndIsDeletedIsFalseOrderByCreateTimeDesc(stateId))
      .thenReturn(Negotiation.empty(0))

    stateService.setScriptEngine(ScriptEngine.JS)

    val step = stateService.getChatStep(
      chat,
      ChatApiInput(stateId = stateId, stepId = stepId, flowId = flowId)
    ).last()
    val triggerStep = step as TriggerStep

    assertThat(triggerStep.trigger).isEqualTo(triggerValue)
  }

  @Test
  fun `should return step with evaluated trigger from conditional chat and model`() {
    val flowId = "7BackwardLinksWithConditionals"
    val modelKey = "formula"
    val stateId = "7Ba1Als"
    val stepId = "{price}"
    val value = "4"
    val triggerValue = "3.f745.dc70c5aaf-4010.f36e69ad1"
    val state = """
      {
        "currentStep":{
          "@class":".ConditionalUserInputStep",
          "id":"{price}",
          "evalExpression":"",
          "user":true,
          "trigger": {
            "(steps[\"{price}\"]?.value as String).toInt() < 7.2": "3.f745.dc70c5aaf-4010.f36e69ad1",
            "(steps[\"{price}\"]?.value as String).toInt() >= 7.2": "4.f745.dc70c5aaf-4010.f36e69ad1"
          }
        },
        "renderedSteps":[
          {
            "@class":".TextStep",
            "id":"1",
            "evalExpression":"",
            "message":"Hello, Reelika! Type in a price:",
            "trigger":"{price}"
          },{
            "@class":".ConditionalUserInputStep",
            "id":"{price}",
            "evalExpression":"",
            "user":true,
            "trigger": {
              "(steps[\"{price}\"]?.value as String).toInt() < 7.2": "3.f745.dc70c5aaf-4010.f36e69ad1",
              "(steps[\"{price}\"]?.value as String).toInt() >= 7.2": "4.f745.dc70c5aaf-4010.f36e69ad1"
            }
          }
        ]
      }
    """

    val mindMupResponse = MindMupResponseFixture.mindMupModelInFormulaResponseFixture()
    val model = ModelResponseFixture.modelsResponseFixture(modelKey)

    val chat = mindMupResponse.toChat().replaceModelVariables(model)
    val chatState = NegotiationState(stateId = stateId, state = state)

    whenever(negotiationStateRepository.findByStateId(stateId)).thenReturn(chatState)
    whenever(negotiationStateRepository.save<NegotiationState>(any())).thenAnswer { i -> i.arguments[0] }
    whenever(negotiationRepository.findFirstByStateIdAndIsDeletedIsFalseOrderByCreateTimeDesc(stateId))
      .thenReturn(Negotiation.empty(0))

    val step = stateService.updateCurrentStep(
      chat,
      ChatApiInput(stateId = stateId, stepId = stepId, value = value, flowId = flowId)
    ).last()
    assertThat(step.id).isEqualTo(stepId)

    val triggerStep = step as TriggerStep
    assertThat(triggerStep.trigger).isEqualTo(triggerValue)
    assertThat(triggerStep).isInstanceOf(UserInputStep::class.java)
    assertThat((triggerStep as UserInputStep).value).isEqualTo(value)
  }

  @Test
  fun `should throw exception if the state id is blacklisted`() {
    val flowId = "1Simple"
    val chat = MindMupResponseFixture.mindMupResponseFixture().toChat()
    val stateId = "1Si1Ale"

    whenever(negotiationStateRepository.findByStateId(stateId)).thenReturn(null)
    whenever(negotiationRepository.findFirstByStateIdAndIsDeletedIsFalseOrderByCreateTimeDesc(stateId)).thenReturn(
      Negotiation.empty(1).copy(
        isVisibleSupplier = false
      )
    )

    Assertions.assertThatExceptionOfType(ChatStateNotAvailableException::class.java).isThrownBy {
      stateService.getChatStep(
        chat,
        ChatApiInput(stateId = stateId, flowId = flowId)
      )
    }
  }

  @Test
  fun `should throw exception if the state id is invalid`() {
    val flowId = "1Simple"
    val chat = MindMupResponseFixture.mindMupResponseFixture().toChat()
    val stateId = "1235244"

    whenever(negotiationStateRepository.findByStateId(stateId)).thenReturn(null)

    Assertions.assertThatExceptionOfType(ChatStateNotFoundException::class.java).isThrownBy {
      stateService.getChatStep(
        chat,
        ChatApiInput(stateId = stateId, flowId = flowId)
      )
    }
  }

  @Test
  fun `updating current step should fail if the state id is invalid`() {
    val flowId = "7BackwardLinksWithConditionals"
    val stateId = "12313123"
    val stepId = "{number}"
    val state = """
      {
        "currentStep":{
          "@class":".ConditionalUserInputStep",
          "id":"{number}",
          "evalExpression":"",
          "user":true,
          "message":"123",
          "trigger":{
            "(value as String).toInt() >= 100":"15.306b.5891d1449-d168.a8ecf6009",
            "(value as String).toInt() < 100":"6.f745.dc70c5aaf-4010.f36e69ad1"
          }
        },
        "renderedSteps":[
          {
            "@class":".ConditionalUserInputStep",
            "id":"{number}",
            "evalExpression":"",
            "user":true,
            "message":"123",
            "value":123,
            "trigger":{
              "(value as String).toInt() >= 100":"15.306b.5891d1449-d168.a8ecf6009",
              "(value as String).toInt() < 100":"6.f745.dc70c5aaf-4010.f36e69ad1"
            }
          }
        ]
      }
    """
    val mindMupResponse = MindMupResponseFixture.mindMupConditionalsResponseFixture()
    val chat = mindMupResponse.toChat()
    val chatState = NegotiationState(stateId = stateId, state = state)

    whenever(negotiationStateRepository.findByStateId(stateId)).thenReturn(chatState)
    whenever(negotiationStateRepository.save<NegotiationState>(any())).thenAnswer { i -> i.arguments[0] }

    Assertions.assertThatExceptionOfType(ChatStateNotFoundException::class.java).isThrownBy {
      stateService.updateCurrentStep(
        chat,
        ChatApiInput(stateId = stateId, stepId = stepId, value = "123", flowId = flowId)
      )
    }
  }

  @Test
  fun `should throw invalid step id exception if the step id is not the initial step id`() {
    val flowId = "1Simple"
    val chat = MindMupResponseFixture.mindMupResponseFixture().toChat()
    val stateId = "someState"
    val stepId = "invalid step"

    whenever(negotiationStateRepository.findByStateId(stateId)).thenReturn(null)

    Assertions.assertThatExceptionOfType(ChatStepException::class.java).isThrownBy {
      stateService.getChatStep(
        chat,
        ChatApiInput(stateId = stateId, stepId = stepId, flowId = flowId)
      )
    }.withMessage("Invalid chat step ID: $stepId")
  }

  @Test
  fun `should insert into parent variable of a nested variable`() {
    val flowId = "flowId"
    val stateId = "flo111AAAId"
    val stepId = "123123"

    val state = """
      {
        "currentStep":{
          "@class":".UserInputStep",
          "id": "123123",
          "variable": "{mainVariable.property1.property2.property4}",
          "evalExpression":"",
          "user":true,
          "trigger":"show-nested-variable"
        },
        "previousStep":null,
        "renderedSteps": [
          {
            "@class":".TextStep",
            "id": "123122",
            "variable": "{mainVariable}",
            "value": {
              "property1":{
                "property2": {
                  "property3":"value3"
                }
              }
            },
            "evalExpression":"",
            "message":"Hi! Please enter your name",
            "trigger":"2"
          },
          {
            "@class":".UserInputStep",
            "id": "123123",
            "variable": "{mainVariable.property1.property2.property4}",
            "evalExpression":"",
            "user":true,
            "trigger":"show-nested-variable"
          }
        ]
      }
    """

    val chatState = NegotiationState(stateId = stateId, state = state)

    reset(negotiationStateRepository)

    whenever(negotiationStateRepository.findByStateId(stateId)).thenReturn(chatState)
    whenever(negotiationStateRepository.save<NegotiationState>(any())).thenAnswer { i -> i.arguments[0] }
    whenever(negotiationRepository.findFirstByStateIdAndIsDeletedIsFalseOrderByCreateTimeDesc(stateId))
      .thenReturn(Negotiation.empty(0))

    val chat = Chat(
      steps = listOf(
        TextStep(
          id = "show-nested-variable",
          message = "You just entered {mainVariable.property1.property2.property4}",
          end = true
        )
      )
    )

    val inputValue = "\"value4\""
    stateService.updateCurrentStep(
      chat,
      ChatApiInput(stateId = stateId, stepId = stepId, value = inputValue, flowId = flowId)
    )[0]

    argumentCaptor<NegotiationState>().apply {
      verify(negotiationStateRepository).save(capture())
      val lastValueStep = jacksonObjectMapper().readValue(
        firstValue.state,
        State::class.java
      ).renderedSteps.findLast { it is ValueStep } as ValueStep
      assertThat(lastValueStep.id).isEqualTo(StepIDGenerator.idFrom("{mainVariable}"))
      assertThat(lastValueStep.variable).isEqualTo("{mainVariable}")
      assertThat(lastValueStep.value).isEqualTo(
        mapOf(
          "property1" to mapOf(
            "property2" to mapOf(
              "property3" to "value3",
              "property4" to "value4"
            )
          )
        )
      )
    }
  }

  @Test
  fun `should coalesce new values with old values if both are Maps`() {
    val flowId = "flowId"
    val stateId = "flo111AAAId"
    val stepId = "123123"

    val state = """
      {
        "currentStep":{
          "@class":".OptionsStep",
          "id": "123123",
          "variable": "{variable}",
          "evalExpression":"",
          "options":[
            {
              "@class":".JsonOption",
              "label":"Option A",
              "value":{
                "property1":"newValueFromOptionA"
              },
              "trigger":"show-values"
            },
            {
              "@class":".JsonOption",
              "label":"Option B",
              "value":{
                "property1":"newValueFromOptionB"
              },
              "trigger":"show-values"
            }
          ]
        },
        "previousStep":null,
        "renderedSteps": [
          {
            "@class":".TextStep",
            "id":"123122",
            "variable": "{variable}",
            "value": {
              "property1":"value1",
              "property2":"value2"
            },
            "message":"Option A",
            "user":true,
            "evalExpression":"",
            "trigger":"update-options"
          },
          {
            "@class":".OptionsStep",
            "id":"{terms}",
            "evalExpression":"",
            "options":[
              {
                "@class":".JsonOption",
                "label":"Option A",
                "value":{
                  "property1":"newValueFromOptionA"
                },
                "trigger":"show-values"
              },
              {
                "@class":".JsonOption",
                "label":"Option B",
                "value":{
                  "property1":"newValueFromOptionB"
                },
                "trigger":"show-values"
              }
            ]
          }
        ]
      }
    """

    val chatState = NegotiationState(stateId = stateId, state = state)

    reset(negotiationStateRepository)

    whenever(negotiationStateRepository.findByStateId(stateId)).thenReturn(chatState)
    whenever(negotiationStateRepository.save<NegotiationState>(any())).thenAnswer { i -> i.arguments[0] }
    whenever(negotiationRepository.findFirstByStateIdAndIsDeletedIsFalseOrderByCreateTimeDesc(stateId))
      .thenReturn(Negotiation.empty(0))

    val chat = Chat(
      steps = listOf(
        TextStep(
          id = "show-values",
          message = "The value of property1 was updated to {variable.property1}",
          end = true
        )
      )
    )

    val inputValue = """{ "property1":"newValueFromOptionA" }"""
    stateService.updateCurrentStep(
      chat,
      ChatApiInput(stateId = stateId, stepId = stepId, value = inputValue, flowId = flowId)
    )[0]

    argumentCaptor<NegotiationState>().apply {
      verify(negotiationStateRepository).save(capture())
      val lastValueStep = jacksonObjectMapper().readValue(
        firstValue.state,
        State::class.java
      ).renderedSteps.last() as TextStep
      assertThat(lastValueStep.id).isEqualTo(stepId)
      assertThat(lastValueStep.value).isEqualTo(
        mapOf(
          "property1" to "newValueFromOptionA",
          "property2" to "value2"
        )
      )
    }
  }

  @Test
  fun `should evaluate eval expression first`() {
    val flowId = "flowId"
    val stateId = "flo111AAAId"
    val stepId = "evaluate-expression"

    val state = """
      {
        "currentStep": {
          "@class":".TextStep",
          "id":"1",
          "message":"Hi",
          "trigger":"evaluate-expression"
        },
        "previousStep":null,
        "renderedSteps":[
          {
            "@class":".TextStep",
            "id":"1",
            "message":"Hi",
            "trigger":"evaluate-expression"
          }
        ]
      }
    """

    val chatState = NegotiationState(stateId = stateId, state = state)

    reset(negotiationStateRepository)

    whenever(negotiationStateRepository.findByStateId(stateId)).thenReturn(chatState)
    whenever(negotiationStateRepository.save<NegotiationState>(any())).thenAnswer { i -> i.arguments[0] }
    whenever(negotiationRepository.findFirstByStateIdAndIsDeletedIsFalseOrderByCreateTimeDesc(stateId))
      .thenReturn(Negotiation.empty(0))

    stateService.setScriptEngine(ScriptEngine.JS)

    val chat = Chat(
      steps = listOf(
        ConditionalTextStep(
          id = "evaluate-expression",
          evalExpression = """
            values["{variable}"] = "requiredInput";
          """,
          message = "This eval expression should be evaluated before and the effect should be considered for the " +
            "conditional trigger",
          trigger = mapOf(
            """ steps["{variable}"].value === "requiredInput" """ to "requiredTrigger",
            """ steps["{variable}"].value === "otherInput" """ to "otherTrigger"
          )
        )
      )
    )

    val steps = stateService.getChatStep(
      chat,
      ChatApiInput(stateId = stateId, stepId = stepId, flowId = flowId)
    )

    val step = steps.last() as TextStep
    assertThat(step.trigger).isEqualTo("requiredTrigger")
  }

  @Test
  fun `should set script engine and evaluate JS trigger`() {

    val flowId = "7BackwardLinksWithConditionals"
    val stateId = "7Ba1Bls"
    val stepId = "{number}"
    val triggerValue = "15.306b.5891d1449-d168.a8ecf6009"
    val state = """
      {
        "currentStep":{
          "@class":".ConditionalUserInputStep",
          "id":"{number}",
          "evalExpression":"",
          "user":true,
          "message":"123",
          "trigger":{
            "value >= 100":"15.306b.5891d1449-d168.a8ecf6009",
            "value < 100":"6.f745.dc70c5aaf-4010.f36e69ad1"
          }
        },
        "renderedSteps":[
          {
            "@class":".ConditionalUserInputStep",
            "id":"{number}",
            "evalExpression":"",
            "user":true,
            "message":"123",
            "value":123,
            "trigger":{
              "value >= 100":"15.306b.5891d1449-d168.a8ecf6009",
              "value < 100":"6.f745.dc70c5aaf-4010.f36e69ad1"
            }
          }
        ]
      }
    """
    val mindMupResponse = MindMupResponseFixture.mindMupConditionalsResponseFixture()
    val chat = mindMupResponse.toChat()
    val chatState = NegotiationState(stateId = stateId, state = state)

    whenever(negotiationStateRepository.findByStateId(stateId)).thenReturn(chatState)
    whenever(negotiationStateRepository.save<NegotiationState>(any())).thenAnswer { i -> i.arguments[0] }
    whenever(negotiationRepository.findFirstByStateIdAndIsDeletedIsFalseOrderByCreateTimeDesc(stateId))
      .thenReturn(Negotiation.empty(0))

    stateService.setScriptEngine(ScriptEngine.JS)

    val step = stateService.updateCurrentStep(
      chat,
      ChatApiInput(stateId = stateId, stepId = stepId, value = "123", flowId = flowId)
    ).last()
    val triggerStep = step as TriggerStep

    assertThat(triggerStep.trigger).isEqualTo(triggerValue)
  }

  @Test
  fun `should set script engine and evaluate KTS trigger`() {

    val flowId = "7BackwardLinksWithConditionals"
    val stateId = "7Ba1Bls"
    val stepId = "{number}"
    val triggerValue = "15.306b.5891d1449-d168.a8ecf6009"
    val state = """
      {
        "currentStep":{
          "@class":".ConditionalUserInputStep",
          "id":"{number}",
          "evalExpression":"",
          "user":true,
          "message":"123",
          "trigger":{
            "(value as String).toInt() >= 100":"15.306b.5891d1449-d168.a8ecf6009",
            "(value as String).toInt() < 100":"6.f745.dc70c5aaf-4010.f36e69ad1"
          }
        },
        "renderedSteps":[
          {
            "@class":".ConditionalUserInputStep",
            "id":"{number}",
            "evalExpression":"",
            "user":true,
            "message":"123",
            "value":123,
            "trigger":{
              "(value as String).toInt() >= 100":"15.306b.5891d1449-d168.a8ecf6009",
              "(value as String).toInt() < 100":"6.f745.dc70c5aaf-4010.f36e69ad1"
            }
          }
        ]
      }
    """
    val mindMupResponse = MindMupResponseFixture.mindMupConditionalsResponseFixture()
    val chat = mindMupResponse.toChat()
    val chatState = NegotiationState(stateId = stateId, state = state)

    whenever(negotiationStateRepository.findByStateId(stateId)).thenReturn(chatState)
    whenever(negotiationStateRepository.save<NegotiationState>(any())).thenAnswer { i -> i.arguments[0] }
    whenever(negotiationRepository.findFirstByStateIdAndIsDeletedIsFalseOrderByCreateTimeDesc(stateId))
      .thenReturn(Negotiation.empty(0))

    stateService.setScriptEngine(ScriptEngine.KTS)

    val step = stateService.updateCurrentStep(
      chat,
      ChatApiInput(stateId = stateId, stepId = stepId, value = "123", flowId = flowId)
    ).last()
    val triggerStep = step as TriggerStep

    assertThat(triggerStep.trigger).isEqualTo(triggerValue)
  }

  @Test
  fun `Should map the chat variable values correctly`() {
    val flowId = "30NestedPreviousVariables"
    val stateId = "30N1Bes"
    val stepId = "5.7713.2a8665801-0675.97d4b3c19"

    val state = """
      {
        "currentStep": {
          "id": "1",
          "user": false,
          "value": null,
          "@class": ".TextStep",
          "message": "Hello, Everglades Foods Inc!",
          "trigger": "5.7713.2a8665801-0675.97d4b3c19",
          "evalExpression": ""
        },
        "previousStep": null,
        "renderedSteps": [
          {
            "id": "1",
            "user": false,
            "value": null,
            "@class": ".TextStep",
            "message": "Hello, Everglades Foods Inc!",
            "trigger": "5.7713.2a8665801-0675.97d4b3c19",
            "evalExpression": ""
          }],
        "chatVariableValues": {}
        }
    """
    val mindMupResponse = MindMupResponseFixture.mindMupAssignLocalVariableFixture()
    val chat = mindMupResponse.toChat().replaceModelVariables(
      ModelResponseFixture.modelsResponseFixture("walmart")
    )
    val chatState = NegotiationState(stateId = stateId, state = state)

    reset(negotiationStateRepository)

    whenever(negotiationStateRepository.findByStateId(stateId)).thenReturn(chatState)
    whenever(negotiationStateRepository.save(chatState)).thenReturn(null)
    whenever(negotiationRepository.findFirstByStateIdAndIsDeletedIsFalseOrderByCreateTimeDesc(stateId)).thenReturn(
      Negotiation.empty(0)
    )

    stateService.setScriptEngine(ScriptEngine.JS)

    val nextStep = stateService.getChatStep(
      chat,
      ChatApiInput(stateId = stateId, stepId = stepId, flowId = flowId)
    ).last() as TextStep

    val expectedMessage = "After assignment to local variable: [{\"number_of_units\":\"48807\",\"item_id\":\"0\"}]"
    assertThat(nextStep.id).isEqualTo(stepId)
    assertThat(nextStep.message).contains(expectedMessage)
  }

  @Test
  fun `Should return empty list if the chat has not been opened and is read only`() {
    val stateId = "123".plus(secretState).plus("345")

    reset(negotiationStateRepository)
    val steps = stateService.getReadOnlySteps(stateId)
    assertThat(steps.size).isEqualTo(0)
  }

  @Test
  fun `Should return the rendered steps when it is read only and change the trigger of the last step to null`() {

    val stateId = "7Ba1Bls"
    val state = """
      {
        "currentStep":{
          "@class":".ConditionalUserInputStep",
          "id":"{number}",
          "evalExpression":"",
          "user":true,
          "message":"123",
          "trigger":{
            "(value as String).toInt() >= 100":"15.306b.5891d1449-d168.a8ecf6009",
            "(value as String).toInt() < 100":"6.f745.dc70c5aaf-4010.f36e69ad1"
          }
        },
        "renderedSteps":[
          {
            "@class":".ConditionalUserInputStep",
            "id":"{number}",
            "evalExpression":"",
            "user":true,
            "message":"123",
            "value":123,
            "trigger":{
              "(value as String).toInt() >= 100":"15.306b.5891d1449-d168.a8ecf6009",
              "(value as String).toInt() < 100":"6.f745.dc70c5aaf-4010.f36e69ad1"
            }
          }
        ]
      }
    """

    reset(negotiationStateRepository)
    whenever(negotiationStateRepository.findByStateId(stateId))
      .thenReturn(NegotiationState(stateId = stateId, state = state))

    val step = stateService.getReadOnlySteps(stateId).last() as ConditionalUserInputStep

    assertThat(step.id).isEqualTo("{number}")
    assertThat(step.trigger).isNull()
  }
}
