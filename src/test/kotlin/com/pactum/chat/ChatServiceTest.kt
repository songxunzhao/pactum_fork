package com.pactum.chat

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.nhaarman.mockitokotlin2.eq
import com.pactum.chat.mindmup.MindMupResponse
import com.pactum.chat.mindmup.MindMupResponseFixture.Companion.mindMupInvalidModelValueResponseFixture
import com.pactum.chat.mindmup.MindMupResponseFixture.Companion.mindMupModelInFormulaResponseFixture
import com.pactum.chat.mindmup.MindMupResponseFixture.Companion.mindMupMultipleModelValuesResponseFixture
import com.pactum.chat.mindmup.MindMupResponseFixture.Companion.mindMupResponseFixture
import com.pactum.chat.mindmup.MindMupService
import com.pactum.chat.model.NegotiationState
import com.pactum.chat.model.ConditionalUserInputStep
import com.pactum.chat.model.TextStep
import com.pactum.chat.model.ChatApiInput
import com.pactum.chat.model.KeyValueWithStrings
import com.pactum.docusign.DocuSignService
import com.pactum.docusign.envelopeSummaryFixture
import com.pactum.model.ModelResponseFixture.Companion.modelsResponseFixture
import com.pactum.model.ModelService
import com.pactum.negotiation.NegotiationNotFoundException
import com.pactum.negotiation.NegotiationRepository
import com.pactum.negotiation.model.Negotiation
import com.pactum.negotiationstate.NegotiationStateRepository
import com.pactum.state.NegotiationStateService
import com.pactum.test.UnitTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Ignore
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.thymeleaf.ITemplateEngine
import org.xhtmlrenderer.pdf.ITextRenderer

@UnitTest
class ChatServiceTest {

  private val negotiationStateRepository: NegotiationStateRepository = mock()
  private val negotiationRepository: NegotiationRepository = mock()
  private val mindMupService: MindMupService = mock()
  private val defaultflowId = "1fbjO6mJVyCCMOZdQO90qLfXCxghoX4Ps"
  private val secretStateId = "secret"
  private val baseUrl = "https://www.pactum.com"
  private val templateEngine: ITemplateEngine = mock()
  private val docuSignService: DocuSignService = mock()
  private val textRenderer = ITextRenderer()
  private val negotiationStateService: NegotiationStateService = mock()
  private val modelService: ModelService = mock()

  private val chatService =
    ChatService(
      mindMupService,
      negotiationStateService,
      negotiationStateRepository,
      modelService,
      negotiationRepository,
      templateEngine,
      docuSignService,
      textRenderer,
      defaultflowId,
      baseUrl
    )

  @Test
  fun `can get chat from mindmup filename`() {
    val id = "1Simple"
    val mindMupResponse = mindMupResponseFixture()
    whenever(mindMupService.getMup(id)).thenReturn(mindMupResponse)

    val chat = chatService.getChat(
      ChatApiInput(flowId = id)
    )

    assertThat(chat.steps.size).isEqualTo(6)

    assertThat(chat.steps[0].id).isEqualTo("1")
    assertThat(chat.steps[1].id).isEqualTo("2.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat(chat.steps[2].id).isEqualTo("bd2a3223-1f24-3704-821e-94c7dab164f5")
    assertThat(chat.steps[3].id).isEqualTo("5.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat(chat.steps[4].id).isEqualTo("6.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat(chat.steps[5].id).isEqualTo("9760dbb1-4058-3d9e-ae29-8dc72fe08624")
  }

  @Test
  fun `can get chat from MindMup using flowId and model`() {
    val flowId = "Ndsaopqdwq"
    val modelId = "modelId"
    val modelKey = "1erewfdsa"
    val stateId = "stateId"

    val mindMupResponse = mindMupMultipleModelValuesResponseFixture()
    val model = modelsResponseFixture(modelKey)

    val negotiation = Negotiation.create(1, flowId, modelId, modelKey, model)
    whenever(negotiationRepository.findFirstByStateIdAndIsDeletedIsFalseOrderByCreateTimeDesc(stateId)).thenReturn(negotiation)
    whenever(mindMupService.getMup(flowId)).thenReturn(mindMupResponse)
    whenever(modelService.getModel(modelId, modelKey, stateId, ModelService.GetModelStrategy.DOWNLOAD_IF_EMPTY)).thenReturn(model)

    val resultChat = chatService.getChatWithModel(
      ChatApiInput(flowId = flowId, modelId = modelId, modelKey = modelKey, stateId = stateId)
    )

    assertThat(resultChat.steps.size).isEqualTo(6)

    val firstResultedTextStep = resultChat.steps[0] as TextStep
    val fourthResultedTextStep = resultChat.steps[3] as TextStep
    val fifthResultedTextStep = resultChat.steps[4] as TextStep

    assertThat(firstResultedTextStep.message.replace("\"", "")).isEqualTo(
      jacksonObjectMapper().writeValueAsString("Hi John!").replace("\"", "")
    )
    assertThat(fourthResultedTextStep.message).isEqualTo("Thanks {variable}!")
    assertThat(fifthResultedTextStep.message.replace("\"", ""))
      .isEqualTo("2 is a number, John.".replace("\"", ""))
  }

  @Test
  @Ignore
  @Disabled
  fun `can get chat step from chat with model variables`() {
    val flowId = "Ndsaopqdwq"
    val modelId = "modelId"
    val modelKey = "1erewfdsa"
    val stateId = "Nds1Awq"
    val stepId = "1"

    val mindMupResponse = mindMupMultipleModelValuesResponseFixture()

    whenever(mindMupService.getMup(flowId)).thenReturn(mindMupResponse)
    whenever(negotiationStateRepository.save<NegotiationState>(any())).thenAnswer { i -> i.arguments[0] }

    reset(negotiationStateRepository)

    val resultStep = chatService.getChatStep(
      ChatApiInput(flowId = flowId, modelId = modelId, modelKey = modelKey, stateId = stateId)
    ).last() as TextStep

    assertThat(resultStep.id).isEqualTo(stepId)
    assertThat(resultStep.message).isEqualTo("Hi John!")

    argumentCaptor<NegotiationState>().apply {
      verify(negotiationStateRepository).save(capture())
      assertThat(firstValue.stateId).isEqualTo(stateId)
    }
  }

  @Test
  @Ignore
  @Disabled
  fun `can get chat step by state id`() {
    val flowId = "Ndsaopqdwq"
    val modelId = "modelId"
    val modelKey = "1erewfdsa"
    val stateId = "Nds1Awq"
    val stepId = "1"

    val mindMupResponse = mindMupMultipleModelValuesResponseFixture()

    whenever(mindMupService.getMup(flowId)).thenReturn(mindMupResponse)
    whenever(negotiationStateRepository.save<NegotiationState>(any())).thenAnswer { i -> i.arguments[0] }
    whenever(negotiationRepository.findFirstByStateIdAndIsDeletedIsFalseOrderByCreateTimeDesc(stateId)).thenReturn(
      Negotiation.create(1, flowId, modelId, modelKey)
    )
    whenever(negotiationRepository.findFirstByStateIdAndIsDeletedIsFalseOrderByCreateTimeDesc(stateId)).thenReturn(
      Negotiation.create(1, flowId, modelId, modelKey)
    )

    reset(negotiationStateRepository)

    val resultStep = chatService.getChatStepByStateId(
      ChatApiInput(stateId = stateId, stepId = stepId)
    ).last() as TextStep

    assertThat(resultStep.id).isEqualTo(stepId)
    assertThat(resultStep.message).isEqualTo("Hi John!")

    argumentCaptor<NegotiationState>().apply {
      verify(negotiationStateRepository).save(capture())
      assertThat(firstValue.stateId).isEqualTo(stateId)
    }
  }

  @Test
  fun `throws exception if state id is invalid`() {
    val stateId = "Nds1Awq"
    val stepId = "1"

    whenever(negotiationRepository.findFirstByStateIdAndIsDeletedIsFalseOrderByCreateTimeDesc(stateId)).thenReturn(null)

    reset(negotiationStateRepository)

    assertThrows<NegotiationNotFoundException> {
      chatService.getChatStepByStateId(
        ChatApiInput(stateId = stateId, stepId = stepId)
      )
    }
  }

  @Test
  fun `throws error when model value is invalid`() {
    val flowId = "Ndsaopqdwq"
    val modelId = "modelId"
    val modelKey = "modelKey"

    val mindMupResponse = mindMupInvalidModelValueResponseFixture()

    whenever(mindMupService.getMup(flowId)).thenReturn(mindMupResponse)

    assertThrows<VariableException> {
      chatService.getChatWithModel(
        ChatApiInput(flowId = flowId, modelId = modelId, modelKey = modelKey, readOnly = true)
      )
    }
  }

  @Test
  fun `can get replace model variables in formulas`() {
    val flowId = "flowId"
    val modelId = "modelId"
    val modelKey = "formula"
    val stateId = "stateId"

    val mindMupResponse = mindMupModelInFormulaResponseFixture()
    val model = modelsResponseFixture(modelKey)
    val negotiation = Negotiation.create(1, flowId, modelId, modelKey, model)
    whenever(negotiationRepository.findFirstByStateIdAndIsDeletedIsFalseOrderByCreateTimeDesc(stateId)).thenReturn(negotiation)
    whenever(mindMupService.getMup(flowId)).thenReturn(mindMupResponse)
    whenever(modelService.getModel(modelId, modelKey, stateId, ModelService.GetModelStrategy.DOWNLOAD_IF_EMPTY)).thenReturn(model)

    val resultChat = chatService.getChatWithModel(
      ChatApiInput(flowId = flowId, modelId = modelId, modelKey = modelKey, stateId = stateId)
    )

    assertThat((resultChat.steps[0] as TextStep).message.replace("\"", "")).isEqualTo(
      jacksonObjectMapper().writeValueAsString("Hello, Tanel! Type in a price:").replace("\"", "")
    )
    assertThat((resultChat.steps[1] as ConditionalUserInputStep).trigger).isEqualTo(
      mapOf(
        """steps["{price}"].value < 10""" to "3.f745.dc70c5aaf-4010.f36e69ad1",
        """steps["{price}"].value >= 10""" to "4.f745.dc70c5aaf-4010.f36e69ad1"
      )
    )
    assertThat((resultChat.steps[2] as TextStep).message).isEqualTo("{price} is smaller than 10.")
    assertThat((resultChat.steps[3] as TextStep).message).isEqualTo(
      "{price} larger than or equal to 10."
    )
    assertThat(resultChat.steps.size).isEqualTo(4)
  }

  @Test
  fun `can get chat parameters from model`() {
    val flowId = "flowId"
    val modelId = "modelId"
    val modelKey = "modelKey"
    val stateId = "stateId"

    val model = mapOf(
      "chatParams" to mapOf(
        "property" to "value",
        "integerProperty" to 1
      )
    )

    val mindMupResponse = MindMupResponse("", sortedMapOf(), listOf())
    val negotiation = Negotiation.create(1, flowId, modelId, modelKey, model)
    whenever(negotiationRepository.findFirstByStateIdAndIsDeletedIsFalseOrderByCreateTimeDesc(stateId)).thenReturn(negotiation)
    whenever(mindMupService.getMup(flowId)).thenReturn(mindMupResponse)
    whenever(modelService.getModel(modelId, modelKey, stateId, ModelService.GetModelStrategy.DOWNLOAD_IF_EMPTY)).thenReturn(model)

    val chatParameters = chatService.getChatWithModel(
      ChatApiInput(flowId = flowId, modelId = modelId, modelKey = modelKey, stateId = stateId)
    ).params
    assertThat(chatParameters).isEqualTo(model["chatParams"])
  }

  @Test
  @Disabled
  fun `Appends the session id to the secret state id`() {
    val flowId = "Ndsaopqdwq"
    val modelId = "modelId"
    val modelKey = "1erewfdsa"
    val stateId = "secret"
    val sessionId = "1234"
    val stepId = "1"

    val mindMupResponse = mindMupMultipleModelValuesResponseFixture()

    reset(negotiationStateRepository)

    whenever(mindMupService.getMup(flowId)).thenReturn(mindMupResponse)
    whenever(negotiationRepository.findFirstByStateIdAndIsDeletedIsFalseOrderByCreateTimeDesc(stateId)).thenReturn(
      Negotiation.empty(0)
    )
    whenever(negotiationStateRepository.findByStateId(stateId)).thenReturn(null)

    val resultStep = chatService.getChatStep(
      ChatApiInput(
        flowId = flowId,
        modelId = modelId,
        modelKey = modelKey,
        stateId = ChatApiInput.createNewStateId(stateId, sessionId, secretStateId)
      )
    ).last() as TextStep

    assertThat(resultStep.id).isEqualTo(stepId)
    assertThat(resultStep.message).isEqualTo("Hi John!")

    argumentCaptor<NegotiationState>().apply {
      verify(negotiationStateRepository).save(capture())
      assertThat(firstValue.stateId).isEqualTo("secret1234")
    }
  }

  @Test
  fun `can get proper Contract template terms using stateId`() {
    val stateId = "state1"

    val mockResponse = listOf(
      KeyValueWithStrings(
        key = "terms",
        value = """{
        "exclusivityPeriod": 4, 
        "paymentDue": 62, 
        "deliveryPenalty": 102, 
        "cancellationFee": 102, 
        "pricePremium": "3", 
        "priceRegular": 24.6, 
        "priceWeekend": 12,
        "contractDurationYears": 3
      }"""
      ),
      KeyValueWithStrings(
        key = "terms",
        value = """{
          "exclusivityPeriod": 3, 
          "paymentDue": 61, 
          "deliveryPenalty": 101, 
          "cancellationFee": 101, 
          "pricePremium": "2", 
          "priceRegular": 23.6, 
          "priceWeekend": 11,
          "contractDurationYears": 2
        }"""
      ),
      KeyValueWithStrings(
        key = "terms",
        value = """{
          "exclusivityPeriod": 2, 
          "paymentDue": 60, 
          "deliveryPenalty": 100, 
          "cancellationFee": 100, 
          "pricePremium": "1", 
          "priceRegular": 22.6, 
          "priceWeekend": 10,
          "contractDurationYears": 1
        }"""
      )
    )

    whenever(negotiationStateRepository.getAllVariablesByStateId(stateId)).thenReturn(mockResponse)

    val contractTemplateValues = chatService.getContractTemplateValues(stateId)

    assertThat(contractTemplateValues["exclusivityPeriod"]).isEqualTo(2)
    assertThat(contractTemplateValues["paymentDue"]).isEqualTo(60)
    assertThat(contractTemplateValues["deliveryPenalty"]).isEqualTo(100)
    assertThat(contractTemplateValues["cancellationFee"]).isEqualTo(100)
    assertThat(contractTemplateValues["pricePremium"]).isEqualTo("1")
    assertThat(contractTemplateValues["priceRegular"]).isEqualTo(22.6)
    assertThat(contractTemplateValues["priceWeekend"]).isEqualTo(10)
  }

  @Test
  fun `can generate PDF using html string`() {
    val fullHTML = """
      <!DOCTYPE html>
      <html>
      
      <head>
        <meta charset="UTF-8"></meta>
        <title> Contract </title>
      </head>
      
      <body>
      
      <h2>Signatures</h2>
      
      <p>By: _____________________________ (Signature)</p>
      </body>
      
      </html>
    """

    val expectedResult = "JVBERi0xLj"
    println(chatService.getPDFBase64(fullHTML))
    assertThat(chatService.getPDFBase64(fullHTML).substring(0, 10)).isEqualTo(expectedResult)
  }

  @Test
  fun getContractSigningUrl() {
    val stateId = "123abc"
    val url = "https://demo.docusign.net/Signing/MTRedeem/v1"
    val envelopeSummary = envelopeSummaryFixture()
    whenever(templateEngine.process(eq("demoContract"), any())).thenReturn("<span>template</span>")
    whenever(docuSignService.createAndSendEnvelope(any())).thenReturn(envelopeSummary)
    whenever(docuSignService.getUrlToRecipientViewUI(envelopeSummary.envelopeId)).thenReturn(url)
    whenever(negotiationStateRepository.getAllVariablesByStateId(stateId)).thenReturn(
      listOf(
        KeyValueWithStrings(
          "terms",
          """{ "contractDurationYears": 2 }"""
        )
      )
    )

    val returnedUrl = chatService.getContractSigningUrl(stateId)

    assertThat(returnedUrl).isEqualTo(url)
  }
}
