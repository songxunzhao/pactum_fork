package com.pactum.chat

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.pactum.chat.mindmup.MindMupService
import com.pactum.chat.mindmup.State
import com.pactum.chat.model.BaseStep
import com.pactum.chat.model.Chat
import com.pactum.chat.model.ChatHolder
import com.pactum.chat.model.ChatApiInput
import com.pactum.chat.model.ChatVariableReplaceable
import com.pactum.docusign.DocuSignService
import com.pactum.embedded.ScriptEngine
import com.pactum.model.ModelService
import com.pactum.negotiation.NegotiationNotFoundException
import com.pactum.negotiation.NegotiationRepository
import com.pactum.negotiationstate.ChatStateNotFoundException
import com.pactum.negotiationstate.ChatStateNotOpenedException
import com.pactum.negotiationstate.ChatStateNotAvailableException
import com.pactum.negotiationstate.NegotiationStateRepository
import com.pactum.state.NegotiationStateService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.json.JsonParserFactory
import org.springframework.stereotype.Service
import org.thymeleaf.ITemplateEngine
import org.thymeleaf.context.Context
import org.xhtmlrenderer.pdf.ITextRenderer
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.util.Base64

@Service
class ChatService(
  private val mindMupService: MindMupService,
  private val negotiationStateService: NegotiationStateService,
  private val negotiationStateRepository: NegotiationStateRepository,
  private val modelService: ModelService,
  private val negotiationRepository: NegotiationRepository,
  private val templateEngine: ITemplateEngine,
  private val docuSignService: DocuSignService,
  private val textRenderer: ITextRenderer,
  @Value("\${chat.defaultFlowId}") private val defaultFlowId: String,
  @Value("\${server.baseUrl}") val baseUrl: String
) {

  internal fun getChat(chatHolder: ChatHolder): Chat {
    val chat = mindMupService.getMup(chatHolder.flowId).toChat()
    val model = mapOf(
      "flowId" to chatHolder.flowId,
      "stateId" to chatHolder.stateId
    )
    return chat.replaceModelVariables(model)
  }

  internal fun getChatWithModel(chatHolder: ChatHolder): Chat {
    val model = modelService.getModel(
      chatHolder.modelId!!,
      chatHolder.modelKey!!,
      chatHolder.stateId,
      ModelService.GetModelStrategy.DOWNLOAD_IF_EMPTY
    )
    val chat = mindMupService.getMup(chatHolder.flowId).toChat()
    return insertModelIntoChat(chat, model)
  }

  private fun insertModelIntoChat(chat: Chat, model: Map<String, Any>): Chat {
    val chatWithModelVariablesReplaced = chat.replaceModelVariables(model)

    @Suppress("UNCHECKED_CAST")
    return chatWithModelVariablesReplaced.copy(params = model["chatParams"] as? Map<String, Any>)
  }

  fun getChatStep(chatApiInput: ChatApiInput): List<BaseStep> {

    val negotiation = negotiationRepository.findFirstByStateIdAndIsDeletedIsFalseOrderByCreateTimeDesc(
      chatApiInput.stateId
    )
    if (!chatApiInput.readOnly && negotiation != null && !negotiation.isVisibleSupplier) {
      throw ChatStateNotAvailableException()
    }

    val chat = if (chatApiInput.modelKey == null || chatApiInput.modelKey == "null") {
      this.getChat(chatApiInput)
    } else {
      this.getChatWithModel(chatApiInput)
    }

    val engine = chat.params?.get("scriptEngine")
    engine?.let {
      val scriptEngine = ScriptEngine.valueOf(it as String)
      negotiationStateService.setScriptEngine(scriptEngine)
    }

    if (chatApiInput.readOnly) {
      val chatState = negotiationStateRepository.findByStateId(chatApiInput.stateId)
        ?: throw ChatStateNotOpenedException()
      val state = jacksonObjectMapper().readValue(chatState.state, State::class.java)
      val chatVariables = state.getChatVariableValues()
      val steps = negotiationStateService.getReadOnlySteps(chatApiInput.stateId)
      return steps.map { step ->
        if (step is ChatVariableReplaceable) {
          step.replaceChatVariables(chatVariables)
        } else {
          step
        }
      }
    }

    return if (chatApiInput.value != null) {
      negotiationStateService.updateCurrentStep(chat, chatApiInput)
    } else {
      negotiationStateService.getChatStep(chat, chatApiInput)
    }
  }

  fun getChatStepByStateId(chatApiInput: ChatApiInput): List<BaseStep> {
    val negotiation = negotiationRepository.findFirstByStateIdAndIsDeletedIsFalseOrderByCreateTimeDesc(chatApiInput.stateId)
      ?: throw NegotiationNotFoundException(chatApiInput.stateId)
    return getChatStep(
      chatApiInput.copy(flowId = negotiation.flowId, modelId = negotiation.modelId, modelKey = negotiation.modelKey)
    )
  }

  fun getContractSigningUrl(stateId: String): String {
    val documentBase64 = getDemoContractPDFBase64(stateId)
    val envelope = docuSignService.createAndSendEnvelope(documentBase64)
    return docuSignService.getUrlToRecipientViewUI(envelope.envelopeId)
  }

  fun getDemoContractHTML(stateId: String): String {
    return getFilledDemoContractHTML(getContractTemplateValues(stateId))
  }

  internal fun getDemoContractPDFBase64(stateId: String): String {
    return getPDFBase64(getDemoContractHTML(stateId))
  }

  internal fun getContractTemplateValues(stateId: String): Map<String, Any> {
    val jsonParser = JsonParserFactory.getJsonParser()

    val variables =
      negotiationStateRepository.getAllVariablesByStateId(stateId) ?: throw ChatStateNotFoundException(stateId)

    val termsItem =
      variables.findLast { it.key == "terms" } ?: error("Could not find any terms for stateId: $stateId")
    val terms = jsonParser.parseMap(termsItem.value)
    val dateStart = LocalDate.now()
    val duration =
      ((terms["contractDurationYears"] ?: error("Missing contractDurationYears term")) as Int).toLong()
    terms["dateStart"] = dateStart.toString()
    terms["dateEnd"] = dateStart.plusYears(duration).toString()
    terms["stateId"] = stateId
    terms["baseUrl"] = baseUrl
    terms["defaultflowId"] = defaultFlowId

    return terms
  }

  internal fun getFilledDemoContractHTML(data: Map<String, Any>): String {

    val context = Context()

    for ((key, value) in data) {
      context.setVariable(key, value)
    }

    // Get the plain HTML with the resolved variables
    return templateEngine.process("demoContract", context)
  }

  internal fun getPDFBase64(fullHTML: String): String {
    val os = ByteArrayOutputStream()
    textRenderer.setDocumentFromString(fullHTML)
    textRenderer.layout()
    textRenderer.createPDF(os)
    val pdfAsBytes = os.toByteArray()
    os.close()
    return Base64.getEncoder().encodeToString(pdfAsBytes)
  }
}
