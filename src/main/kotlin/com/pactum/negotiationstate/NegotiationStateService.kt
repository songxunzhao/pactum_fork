package com.pactum.state

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.pactum.negotiationstate.NegotiationStateRepository
import com.pactum.chat.mindmup.State
import com.pactum.chat.model.BaseStep
import com.pactum.chat.model.Chat
import com.pactum.chat.model.NegotiationState
import com.pactum.chat.model.ChatVariableReplaceable
import com.pactum.chat.model.ConditionTriggerStep
import com.pactum.chat.model.SimpleTriggerStep
import com.pactum.chat.model.Step
import com.pactum.chat.model.TextStep
import com.pactum.chat.model.Trimmable
import com.pactum.chat.model.ValueHolder
import com.pactum.chat.model.ValueStep
import com.pactum.chat.model.ValueInputable
import com.pactum.chat.model.extractChatVariableName
import com.pactum.chat.model.extractMainVariableAndKeyPath
import com.pactum.chat.model.isNestedChatVariable
import com.pactum.chat.model.isChatVariable
import com.pactum.chat.model.ChatApiInput
import com.pactum.chat.model.VariableHolder
import com.pactum.embedded.ExpressionEval
import com.pactum.embedded.JavaScriptExpressionEval
import com.pactum.embedded.JavaScriptTriggerEval
import com.pactum.embedded.KotlinExpressionEval
import com.pactum.embedded.KotlinTriggerEval
import com.pactum.embedded.ScriptEngine
import com.pactum.embedded.TriggerEval
import com.pactum.chat.VariableException
import com.pactum.chat.model.StepIDGenerator
import com.pactum.client.ClientRepository
import com.pactum.model.ModelService
import com.pactum.negotiation.NegotiationNotFoundException
import com.pactum.negotiationasset.NegotiationAssetRepository
import com.pactum.negotiationasset.model.NegotiationAssetType
import com.pactum.negotiation.NegotiationRepository
import com.pactum.negotiation.NegotiationService
import com.pactum.negotiation.NegotiationStatusNotFoundException
import com.pactum.negotiation.model.Negotiation
import com.pactum.negotiationfield.NegotiationFieldService
import com.pactum.negotiationstate.ChatStateNotAvailableException
import com.pactum.negotiationstate.ChatStateNotFoundException
import com.pactum.negotiationstate.ChatStepException
import com.pactum.negotiationstate.insertIntoKeyPathAndCoalesceIfPossible
import com.pactum.utils.SentryHelper
import com.pactum.utils.Utils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import kotlin.collections.ArrayList

@Service
class NegotiationStateService(
  private val negotiationStateRepository: NegotiationStateRepository,
  private val negotiationRepository: NegotiationRepository,
  private val negotiationAssetRepository: NegotiationAssetRepository,
  private val negotiationFieldService: NegotiationFieldService,
  private val clientRepository: ClientRepository,
  private var expressionEval: ExpressionEval,
  private var triggerEval: TriggerEval,
  private var negotiationService: NegotiationService,
  private val modelService: ModelService,
  @Value("\${chat.pactumClientTag}") private val pactumClientTag: String,
  @Value("\${chat.secretStateId}") private val secretStateId: String,
  @Value("\${chat.defaultFlowId}") private val defaultFlowId: String
) {

  fun setScriptEngine(scriptEngine: ScriptEngine) {
    when (scriptEngine) {
      ScriptEngine.KTS -> {
        expressionEval = KotlinExpressionEval()
        triggerEval = KotlinTriggerEval()
      }
      ScriptEngine.JS -> {
        expressionEval = JavaScriptExpressionEval()
        triggerEval = JavaScriptTriggerEval()
      }
    }
  }

  fun updateCurrentStep(chat: Chat, chatApiInput: ChatApiInput): List<Step> {
    val chatState = negotiationStateRepository.findByStateId(chatApiInput.stateId)
      ?: throw ChatStateNotFoundException(chatApiInput.stateId)
    val newTerms: MutableMap<String, Any> = mutableMapOf()

    var state = jacksonObjectMapper().readValue(chatState.state, State::class.java)

    val stepId = chatApiInput.stepId!!
    validateIsCurrentStep(state, stepId)

    var currentStep = state.currentStep

    if (currentStep !is ValueInputable) {
      return listOf(currentStep)
    }

    currentStep = currentStep.inputValueWithJson(chatApiInput.value!!)

    val currentStepVariable = (currentStep as? VariableHolder)?.variable

    if (currentStepVariable != null && !isNestedChatVariable(currentStepVariable)) {
      val variableName = extractChatVariableName(currentStepVariable)
      val chatVariables = state.getChatVariableValues()
      val previousValue = chatVariables[variableName]
      val currentValue = (currentStep as ValueHolder).value
      if (currentValue != null && isChatVariable(currentStepVariable)) {
        newTerms[variableName] = currentValue
      }

      if (previousValue != null && currentValue is Map<*, *> && previousValue is Map<*, *>) {
        val coalescedValue = previousValue + currentValue
        newTerms[variableName] = coalescedValue
        currentStep = currentStep.updateValue(coalescedValue) as Step
      }
    }
    state = state.replaceCurrentStep(currentStep)

    if (currentStep is ConditionTriggerStep) {
      currentStep = currentStep.toSimpleTriggerStep(currentStep.nextStepId(state, triggerEval))
      state = state.replaceCurrentStep(currentStep)
    }

    if (currentStepVariable != null && isNestedChatVariable(currentStepVariable)) {
      val nestedVariable = extractChatVariableName(currentStepVariable)
      val (mainVariable, keyPath) = extractMainVariableAndKeyPath(nestedVariable)
      val chatVariables = state.getChatVariableValues()

      if (!chatVariables.containsKey(mainVariable)) {
        throw VariableException("Chat variable $mainVariable for nested variable $stepId does not exist")
      }

      @Suppress("UNCHECKED_CAST")
      val mainVariableValue = chatVariables[mainVariable] as? Map<String, Any>
        ?: throw VariableException("Could not insert value into nested variable $stepId")

      val updatedValue =
        mainVariableValue.insertIntoKeyPathAndCoalesceIfPossible(keyPath, (currentStep as ValueHolder).value!!)
      val stepWithUpdatedValue = ValueStep(
        id = StepIDGenerator.idFrom("{$mainVariable}"),
        variable = "{$mainVariable}",
        value = updatedValue
      )
      newTerms[mainVariable] = updatedValue
      state = state.addValueStep(stepWithUpdatedValue)
    }

    persistState(chatApiInput, state, newTerms)
    return listOf(currentStep)
  }

  fun getChatStep(chat: Chat, chatApiInput: ChatApiInput): List<BaseStep> {
    val chatState = negotiationStateRepository.findByStateId(chatApiInput.stateId)
    val state: State? = if (chatState != null) {
      jacksonObjectMapper().readValue(chatState.state, State::class.java)
    } else null

    return if (Utils.isSecretStateId(chatApiInput.stateId, secretStateId)) {
      getSecretChatStep(state, chat, chatApiInput)
    } else {
      getNormalChatStep(state, chat, chatApiInput)
    }
  }

  private fun getNormalChatStep(
    state: State?,
    chat: Chat,
    chatApiInput: ChatApiInput
  ): List<BaseStep> {
    if (state != null && chatApiInput.stepId.isNullOrBlank())
      return trimSteps(state.renderedSteps)

    val step = if (state == null) {
      getInitialStep(chat, chatApiInput.stepId)
    } else {
      getNextStep(chat, chatApiInput.stepId!!, state)
    }

    val renderSteps = toRenderSteps(step, state)
    persistNewRenderSteps(renderSteps, state, chatApiInput)
    return trimSteps(renderSteps)
  }

  private fun getSecretChatStep(
    state: State?,
    chat: Chat,
    chatApiInput: ChatApiInput
  ): List<BaseStep> {
    val step = if (state == null || chatApiInput.stepId.isNullOrBlank()) {
      getInitialStep(chat, chatApiInput.stepId)
    } else {
      getNextStep(chat, chatApiInput.stepId, state)
    }

    val renderSteps = toRenderSteps(step, state)
    persistNewRenderSteps(renderSteps, state, chatApiInput)
    return trimSteps(renderSteps)
  }

  private fun toRenderSteps(step: Step, state: State?): ArrayList<BaseStep> {
    var newStep = step

    val steps = if (!newStep.evalExpression.isNullOrBlank()) {
      expressionEval.state = state!!
      expressionEval.eval(newStep.evalExpression!!)
    } else {
      arrayListOf()
    }

    // If we can evaluate conditional trigger without user input, then process it
    if (newStep is ConditionTriggerStep && newStep !is ValueInputable && state != null) {
      val updatedState = state.copy(renderedSteps = state.renderedSteps + steps)
      newStep = newStep.toSimpleTriggerStep(newStep.nextStepId(updatedState, triggerEval))
    }

    if (newStep is ChatVariableReplaceable && state != null) {
      val updatedState = state.copy(renderedSteps = state.renderedSteps + steps)

      val previousStep = updatedState.currentStep
      val chatVariables = if (previousStep is ValueHolder && previousStep.value != null) {
        updatedState.getChatVariableValues().plus(Pair("previousValue", previousStep.value))
      } else {
        updatedState.getChatVariableValues()
      }
      newStep = newStep.replaceChatVariables(chatVariables)
    }
    steps.add(newStep)
    return steps
  }

  private fun persistNewRenderSteps(
    steps: List<BaseStep>,
    state: State?,
    chatApiInput: ChatApiInput
  ) {
    val newState = if (state == null) {
      val lastStep = steps.last() as Step
      State.new(null, lastStep, steps)
    } else {
      state.pushNewStep(steps)
    }

    val newTerms = steps
      .filterIsInstance<ValueStep>()
      .filter { !it.variable.isNullOrBlank() }
      .associateBy({ extractChatVariableName(it.variable!!) }, { it.value ?: "" }).toMap()

    persistState(chatApiInput, newState, newTerms)
  }

  // Get steps
  private fun getNextStep(
    chat: Chat,
    stepId: String,
    state: State
  ): Step {

    validateIfStepReachable(state, stepId)
    return chat.findStepById(stepId)!!
  }

  private fun getInitialStep(chat: Chat, stepId: String?): Step {
    val firstStep = chat.getFirstStep()!!
    validateInitialStep(stepId, firstStep)
    return firstStep
  }

  private fun trimSteps(steps: List<BaseStep>): List<BaseStep> {
    return steps.filter { it is Trimmable }.map { (it as Trimmable).trim() }
  }

  // Validators
  private fun validateInitialStep(stepId: String?, initialStep: Step) {
    if (!stepId.isNullOrBlank() && stepId != initialStep.id) {
      throw ChatStepException("Invalid chat step ID: $stepId")
    }
  }

  private fun validateIfStepReachable(state: State, stepId: String?) {
    if (stepId.isNullOrBlank() || state.currentStep !is SimpleTriggerStep ||
      !isCurrentOrNextStep(state.currentStep, stepId)) {
      throw ChatStepException("Invalid chat step ID: $stepId")
    }
  }

  private fun isCurrentOrNextStep(currentStep: SimpleTriggerStep, stepId: String) =
    currentStep.id == stepId || currentStep.trigger == stepId

  private fun validateIsCurrentStep(state: State, stepId: String) {
    if (stepId.isBlank() || state.currentStep.id != stepId) {
      throw ChatStepException("Invalid chat step ID: $stepId")
    }
  }

  private fun persistState(
    chatApiInput: ChatApiInput,
    state: State,
    newTerms: Map<String, Any> = mapOf()
  ) {
    val stateJson = jacksonObjectMapper().writeValueAsString(state)
    val flowVersionId =
      negotiationAssetRepository.findLatestByDriveId(chatApiInput.flowId, NegotiationAssetType.FLOW.name)?.id
    val modelVersionId = chatApiInput.modelId?.let { modelId ->
      negotiationAssetRepository.findLatestByDriveId(modelId, NegotiationAssetType.MODEL.name)?.id
    }

    when {
      Utils.isDefaultFlowId(chatApiInput.flowId, defaultFlowId) -> persistDefaultChat(chatApiInput.stateId, stateJson)
      Utils.isSecretStateId(chatApiInput.stateId, secretStateId) -> persistTestChats(chatApiInput, stateJson)
      !Utils.isValidStateId(chatApiInput.stateId, chatApiInput.flowId) -> throw ChatStateNotFoundException(chatApiInput.stateId)
      else -> persistActualChats(
        chatApiInput,
        stateJson,
        flowVersionId,
        modelVersionId,
        newTerms,
        chatApiInput.shouldCreateNegotiationIfNotFound
      )
    }
  }

  private fun persistDefaultChat(stateId: String, stateJson: String) {
    val clientId = clientRepository.findFirstByTag(pactumClientTag)?.id
    clientId?.let {
      val negotiations = negotiationRepository.findByClientIdAndFlowIdAndIsDeletedIsFalse(it, defaultFlowId)
      if (negotiations.isNotEmpty()) {
        val negotiationState = NegotiationState.new(stateId, stateJson, negotiations[0].id)
        negotiationStateRepository.save(negotiationState)
      }
    }
  }

  private fun persistTestChats(chatApiInput: ChatApiInput, stateJson: String) {
    val clientId = clientRepository.findFirstByTag(pactumClientTag)?.id
    clientId?.let {
      val negotiation = negotiationRepository.findFirstByStateIdAndIsDeletedIsFalseOrderByCreateTimeDesc(chatApiInput.stateId)
      val negotiationId = if (negotiation == null) {
        val model = modelService.getModel(
          chatApiInput.modelId!!,
          chatApiInput.modelKey!!,
          chatApiInput.stateId,
          ModelService.GetModelStrategy.DOWNLOAD_IF_EMPTY
        )
        negotiationRepository.save(
          Negotiation(
            clientId = clientId,
            flowId = chatApiInput.flowId,
            modelId = chatApiInput.modelId,
            modelKey = chatApiInput.modelKey,
            modelAttributes = jacksonObjectMapper().writeValueAsString(model),
            stateId = chatApiInput.stateId,
            createTime = Instant.now()
          )
        ).id
      } else {
        negotiation.id
      }
      val negotiationState = NegotiationState.new(chatApiInput.stateId, stateJson, negotiationId)
      negotiationStateRepository.save(negotiationState)
    }
  }

  private fun persistActualChats(
    chatApiInput: ChatApiInput,
    stateJson: String,
    flowVersionId: Long?,
    modelVersionId: Long?,
    newTerms: Map<String, Any>,
    shouldCreateNegotiationIfNotFound: Boolean
  ) {
    var negotiation = negotiationRepository.findFirstByStateIdAndIsDeletedIsFalseOrderByCreateTimeDesc(chatApiInput.stateId)
    if (negotiation == null) {
      if (shouldCreateNegotiationIfNotFound) {
        val clientId = clientRepository.findFirstByTag(pactumClientTag)?.id!!
        val model = modelService.getModel(
          chatApiInput.modelId!!,
          chatApiInput.modelKey!!,
          chatApiInput.stateId,
          ModelService.GetModelStrategy.DOWNLOAD_IF_EMPTY
        )
        negotiation = negotiationRepository.save(
          Negotiation(
            clientId = clientId,
            flowId = chatApiInput.flowId,
            modelId = chatApiInput.modelId,
            modelKey = chatApiInput.modelKey,
            modelAttributes = jacksonObjectMapper().writeValueAsString(model),
            stateId = chatApiInput.stateId,
            createTime = Instant.now()
          )
        )
      } else {
        throw NegotiationNotFoundException(chatApiInput.stateId)
      }
    }

    if (!negotiation.isVisibleSupplier) {
      throw ChatStateNotAvailableException()
    }

    val negotiationState = NegotiationState.new(chatApiInput.stateId, stateJson, negotiation.id)
    negotiationStateRepository.save(negotiationState)
    val newStatusTerm = newTerms["status"] as? String
    var status = negotiation.status
    if (newStatusTerm != null) {
      val clientOptional = clientRepository.findById(negotiation.clientId)
      val convertedNewStatusTerm = newStatusTerm.replace(" ", "_").toUpperCase()
      val map = clientOptional.get().getConfig().negotiationStatuses
      status = newStatusTerm
      if (!map.containsKey(convertedNewStatusTerm)) {
        SentryHelper.report(NegotiationStatusNotFoundException(newStatusTerm), mapOf("stateId" to negotiation.stateId))
      }
    }
    val updatedNegotiation = negotiation.copy(
      flowVersionId = flowVersionId,
      modelVersionId = modelVersionId,
      chatStartTime = negotiation.chatStartTime ?: Instant.now(),
      chatUpdateTime = Instant.now(),
      status = status
    )
    negotiationService.updateWithPublish(negotiation, updatedNegotiation)
  }

  fun getReadOnlySteps(stateId: String): List<BaseStep> {
    val chatState = negotiationStateRepository.findByStateId(stateId) ?: return emptyList()

    val state = jacksonObjectMapper().readValue(chatState.state, State::class.java)
    val steps = state.renderedSteps.toMutableList()

    val lastStep = if (steps.last() is TextStep) {
      (steps.last() as TextStep).copy(trigger = null)
    } else {
      steps.last()
    }
    steps[steps.lastIndex] = lastStep
    return trimSteps(steps)
  }
}
