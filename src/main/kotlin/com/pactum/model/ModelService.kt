package com.pactum.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.pactum.negotiationstate.NegotiationStateRepository
import com.pactum.chat.model.ChatApiInput
import com.pactum.negotiation.NegotiationModelTermService
import com.pactum.negotiation.NegotiationNotFoundException
import com.pactum.negotiation.NegotiationRepository
import com.pactum.negotiationasset.NegotiationAssetService
import com.pactum.negotiationfield.NegotiationFieldService
import com.pactum.negotiationstate.ChatStateNotAvailableException
import com.pactum.negotiationstate.ChatStateNotOpenedException
import com.pactum.utils.Utils
import org.json.simple.JSONObject
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
@Suppress("UNCHECKED_CAST")
class ModelService(
  private val objectMapper: ObjectMapper,
  private val negotiationStateRepository: NegotiationStateRepository,
  private val negotiationAssetService: NegotiationAssetService,
  private val negotiationRepository: NegotiationRepository,
  private val negotiationFieldService: NegotiationFieldService,
  private val negotiationModelTermService: NegotiationModelTermService,
  @Value("\${chat.secretStateId}") private val secretStateId: String
) {

  companion object {
    private const val INHERITANCE_KEY = "@include"
  }

  enum class GetModelStrategy {
    DOWNLOAD_IF_EMPTY, // download model file if model in db is empty
    NEVER_DOWNLOAD // do not download model file
  }

  fun getModel(
    modelId: String,
    modelKey: String,
    stateId: String,
    getModelStrategy: GetModelStrategy = GetModelStrategy.NEVER_DOWNLOAD
  ): Map<String, Any> {

    var models = if (!Utils.isSecretStateId(stateId, secretStateId)) {
      getModelFromDb(stateId)
    } else {
      // todo we need a fix for saladus state ids when model file is empty
      emptyMap()
    }
    if (getModelStrategy == GetModelStrategy.DOWNLOAD_IF_EMPTY && models.isEmpty()) {
      models = models + getModelFromFile(modelId, modelKey)
    }
    if (models.isEmpty()) throw ModelException("Model key $modelKey not found")
    return models
  }

  fun getChatParamsByStateId(chatApiInput: ChatApiInput): Map<String, Any> {
    val negotiation = negotiationRepository.findFirstByStateIdAndIsDeletedIsFalseOrderByCreateTimeDesc(chatApiInput.stateId)
      ?: throw NegotiationNotFoundException(chatApiInput.stateId)
    return getChatParams(
      chatApiInput.copy(modelId = negotiation.modelId, modelKey = negotiation.modelKey)
    )
  }

  fun getChatParams(input: ChatApiInput): Map<String, Any> {

    val negotiation = negotiationRepository.findFirstByStateIdAndIsDeletedIsFalseOrderByCreateTimeDesc(input.stateId)
    when {
      input.readOnly -> negotiationStateRepository.findByStateId(input.stateId)
        ?: throw ChatStateNotOpenedException()
      negotiation != null && !negotiation.isVisibleSupplier -> throw ChatStateNotAvailableException()
    }

    val model = getModel(input.modelId!!, input.modelKey!!, input.stateId, GetModelStrategy.DOWNLOAD_IF_EMPTY)
    return model["chatParams"] as? HashMap<String, Any> ?: emptyMap()
  }

  fun getModelFromFile(modelId: String, modelKey: String): Map<String, Any> {
    val jsonString = negotiationAssetService.getChatModel(modelId)
    if (jsonString == null || jsonString.isBlank())
      throw ModelException("Model id $modelId not found or empty")

    val json = objectMapper.readValue(jsonString, JSONObject::class.java)
    val models = json["models"] as? HashMap<String, Any>
      ?: throw ModelException("Model id $modelId must contain models[]")

    return if (models.containsKey(modelKey)) {
      val model = models[modelKey] as HashMap<String, Any>
      return if (model.containsKey(INHERITANCE_KEY)) {
        getModelWithInheritance(models, model)
      } else {
        model
      }
    } else {
      emptyMap()
    }
  }

  private fun getModelWithInheritance(models: Map<String, Any>, model: Map<String, Any>): Map<String, Any> {
    val newModel = HashMap<String, Any>()
    val inheritedModelKeys = model[INHERITANCE_KEY] as List<String>
    for (inheritedModelKey in inheritedModelKeys) {
      val parent = models[inheritedModelKey] as HashMap<String, Any>
      parent.forEach { (key, value) -> newModel[key] = value }
    }
    model.forEach { (key, value) -> newModel[key] = value }
    newModel.remove(INHERITANCE_KEY)
    return newModel
  }

  private fun getModelFromDb(stateId: String): Map<String, Any> {
    val negotiation = negotiationRepository.findFirstByStateIdAndIsDeletedIsFalseOrderByCreateTimeDesc(stateId)
    return if (negotiation != null) {
      negotiationModelTermService.getModels(negotiation)
    } else {
      emptyMap()
    }
  }
}
