package com.pactum.model

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.whenever
import com.pactum.negotiationstate.NegotiationStateRepository
import com.pactum.chat.model.ChatApiInput
import com.pactum.chat.model.NegotiationState
import com.pactum.negotiationasset.NegotiationAssetService
import com.pactum.negotiationstate.ChatStateNotAvailableException
import com.pactum.google.GoogleDriveService
import com.pactum.model.ModelResponseFixture.Companion.modelsFixture
import com.pactum.negotiation.NegotiationModelTermService
import com.pactum.negotiation.NegotiationRepository
import com.pactum.negotiation.model.Negotiation
import com.pactum.negotiationfield.NegotiationFieldService
import com.pactum.negotiationstate.ChatStateNotOpenedException
import com.pactum.test.UnitTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@UnitTest
class ModelServiceTest {

  private val googleDriveService: GoogleDriveService = mock()
  private val objectMapper = jacksonObjectMapper()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
  private val defaultmodelId = ""
  private val secretStateId = "secret"
  private val negotiationStateRepository: NegotiationStateRepository = mock()
  private val negotiationAssetService: NegotiationAssetService = mock()
  private val negotiationFieldService: NegotiationFieldService = mock()
  private val negotiationModelTermService: NegotiationModelTermService = mock()
  private val negotiationRepository: NegotiationRepository = mock()
  private val modelService =
    ModelService(
      objectMapper,
      negotiationStateRepository,
      negotiationAssetService,
      negotiationRepository,
      negotiationFieldService,
      negotiationModelTermService,
      secretStateId
    )

  @BeforeEach
  fun `clean up`() {
    reset(negotiationAssetService)
    reset(googleDriveService)
    reset(negotiationRepository)
    reset(negotiationStateRepository)
  }

  @Test
  fun `can get a model`() {
    val modelId = "modelId"
    val modelKey = "1erewfdsa"
    val stateId = "stateId"
    val models = modelsFixture("1Name")

    whenever(negotiationAssetService.getChatModel(modelId)).thenReturn(models)

    val result = modelService.getModel(modelId, modelKey, stateId, ModelService.GetModelStrategy.DOWNLOAD_IF_EMPTY)

    assertThat(result.size).isEqualTo(5)
    assertThat(result["name"]).isEqualTo("John")
    assertThat(result["integer"]).isEqualTo(2)
    assertThat(result["decimalPointNumbers"]).isEqualTo(2.0)
    assertThat(result["negativeNumber"]).isEqualTo(-2)
    assertThat(result["specialCharacters"]).isEqualTo("*_?/@")
  }

  @Test
  fun `get model fails for invalid modelId`() {
    val modelId = "Not a valid modelId"
    val modelKey = "modelKey"
    val stateId = "stateId"

    assertThrows<ModelException> {
      modelService.getModel(modelId, modelKey, stateId)
    }
  }

  @Test
  fun `get model fails for invalid modelKey`() {
    val modelId = "modelId"
    val modelKey = "Not a valid ModelKey"
    val stateId = "stateId"
    val models = modelsFixture("1Name")

    whenever(googleDriveService.getContent(modelId)).thenReturn(models)

    assertThrows<ModelException> {
      modelService.getModel(modelId, modelKey, stateId)
    }
  }

  @Test
  fun `get a model that inherits properties`() {
    val modelKey = "child"
    val models = modelsFixture("2Inheritance")
    val stateId = "stateId"

    whenever(negotiationAssetService.getChatModel(defaultmodelId)).thenReturn(models)

    val result = modelService.getModel(defaultmodelId, modelKey, stateId, ModelService.GetModelStrategy.DOWNLOAD_IF_EMPTY)

    assertThat(result.size).isEqualTo(5)
    assertThat(result["name"]).isEqualTo("John")
    assertThat(result["company"]).isEqualTo("Microsoft")
    assertThat(result["limit"]).isEqualTo(100.00)
    assertThat(result["parent1Variable"]).isEqualTo(3)
    assertThat(result["parent2Variable"]).isEqualTo(1)
    assertThat(result["@include"]).isNull()
  }

  @Test
  fun `get chat params`() {
    val modelId = "modelId"
    val modelKey = "1"
    val stateId = "stateId"
    val fileName = "3ChatParams"
    val flowId = "asdasdasdasd"
    val allModels = modelsFixture(fileName)

    whenever(negotiationRepository.findFirstByStateIdAndIsDeletedIsFalseOrderByCreateTimeDesc(stateId)).thenReturn(
      Negotiation.create(1, flowId, modelId, modelKey).copy(
        stateId = stateId,
        isVisibleSupplier = true
      )
    )
    whenever(negotiationAssetService.getChatModel(modelId)).thenReturn(allModels)

    val result = modelService.getChatParams(
      ChatApiInput(modelId = modelId, modelKey = modelKey, stateId = stateId)
    )
    assertThat(result.size).isEqualTo(4)
    assertThat(result["name"]).isEqualTo("Walmart")
    assertThat(result["secure"]).isEqualTo(false)
    assertThat(result["scriptEngine"]).isEqualTo("JS")
    assertThat(result["botAvatar"]).isEqualTo("somebase64image")
  }

  @Test
  fun `get chat params fails if stateid is blacklisted`() {
    val modelId = "modelId"
    val modelKey = "1"
    val stateId = "stateId"
    val fileName = "3ChatParams"
    val models = modelsFixture(fileName)

    whenever(googleDriveService.getContent(modelId)).thenReturn(models)
    whenever(negotiationRepository.findFirstByStateIdAndIsDeletedIsFalseOrderByCreateTimeDesc(stateId)).thenReturn(
      Negotiation.empty(1).copy(
        isVisibleSupplier = false
      )
    )
    assertThrows<ChatStateNotAvailableException> {
      modelService.getChatParams(
        ChatApiInput(modelId = modelId, modelKey = modelKey, stateId = stateId)
      )
    }
  }

  @Test
  fun `get chat params if stateid is blacklisted and is read-only`() {
    val modelId = "modelId"
    val modelKey = "1"
    val stateId = "stateId"
    val fileName = "3ChatParams"
    val flowId = "flowId"
    val allModels = modelsFixture(fileName)

    val negotiation = Negotiation.create(1, flowId, modelId, modelKey)
    whenever(negotiationStateRepository.findByStateId(stateId)).thenReturn(NegotiationState(state = "", stateId = stateId))
    whenever(negotiationRepository.findFirstByStateIdAndIsDeletedIsFalseOrderByCreateTimeDesc(stateId)).thenReturn(
      negotiation.copy(
        stateId = stateId,
        isVisibleSupplier = false
      )
    )
    whenever(negotiationAssetService.getChatModel(modelId)).thenReturn(allModels)

    val result = modelService.getChatParams(
      ChatApiInput(modelId = modelId, modelKey = modelKey, stateId = stateId, readOnly = true)
    )
    assertThat(result.size).isEqualTo(4)
    assertThat(result["name"]).isEqualTo("Walmart")
    assertThat(result["secure"]).isEqualTo(false)
    assertThat(result["scriptEngine"]).isEqualTo("JS")
    assertThat(result["botAvatar"]).isEqualTo("somebase64image")
  }

  @Test
  fun `throws exception if is read-only and state not opened yet`() {
    val modelId = "modelId"
    val modelKey = "1"
    val stateId = "stateId"
    val fileName = "3ChatParams"
    val models = modelsFixture(fileName)

    whenever(googleDriveService.getContent(modelId)).thenReturn(models)
    whenever(negotiationStateRepository.findByStateId(stateId)).thenThrow(ChatStateNotOpenedException::class.java)
    assertThrows<ChatStateNotOpenedException> {
      modelService.getChatParams(
        ChatApiInput(modelId = modelId, modelKey = modelKey, stateId = stateId, readOnly = true)
      )
    }
  }

  @Test
  fun `get chat params by state id`() {
    val modelId = "modelId"
    val modelKey = "1"
    val stateId = "stateId"
    val fileName = "3ChatParams"
    val flowId = "asdasdasdasd"
    val allModels = modelsFixture(fileName)

    val negotiation = Negotiation.create(1, flowId, modelId, modelKey).copy(id = 1)
    whenever(negotiationRepository.findFirstByStateIdAndIsDeletedIsFalseOrderByCreateTimeDesc(stateId)).thenReturn(
      negotiation.copy(
        stateId = stateId,
        isVisibleSupplier = true
      )
    )
    whenever(negotiationAssetService.getChatModel(modelId)).thenReturn(allModels)

    val result = modelService.getChatParamsByStateId(
      ChatApiInput(stateId = stateId, modelId = modelId, modelKey = modelKey)
    )
    assertThat(result.size).isEqualTo(4)
    assertThat(result["name"]).isEqualTo("Walmart")
    assertThat(result["secure"]).isEqualTo(false)
    assertThat(result["scriptEngine"]).isEqualTo("JS")
    assertThat(result["botAvatar"]).isEqualTo("somebase64image")
  }
}
