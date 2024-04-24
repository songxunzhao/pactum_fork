package com.pactum.negotiation

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.pactum.audit.AuditEventService
import com.pactum.auth.AccessDeniedException
import com.pactum.auth.model.Role
import com.pactum.negotiationstate.NegotiationStateRepository
import com.pactum.client.ClientRepository
import com.pactum.client.ClientService
import com.pactum.client.model.NegotiationFieldConfigType
import com.pactum.extract.ExtractService
import com.pactum.model.ModelService
import com.pactum.negotiation.model.CreateNegotiationReq
import com.pactum.negotiation.model.Negotiation
import com.pactum.negotiation.model.NegotiationDetails
import com.pactum.negotiation.model.NegotiationTermChangedPubSubMessage
import com.pactum.negotiation.model.NegotiationPubSubEvent
import com.pactum.negotiation.model.NegotiationPubSubMessage
import com.pactum.negotiation.model.ReloadNegotiationModelReq
import com.pactum.negotiation.summary.model.ExtraValue
import com.pactum.negotiation.summary.model.ExtraValueFormat
import com.pactum.utils.JsonHelper
import com.pactum.utils.SentryHelper
import com.pactum.auth.SessionHelper
import com.pactum.negotiation.model.NegotiationAuditEvent
import com.pactum.api.GenericCreatedResponse
import com.pactum.api.GenericNoContentResponse
import com.pactum.api.GenericOkResponse
import com.pactum.client.model.Client
import com.pactum.negotiation.model.UpdateNegotiationReq
import com.pactum.negotiationfield.NegotiationFieldService
import com.pactum.pubsub.PubSubService
import com.pactum.pubsub.PubSubTopic
import com.pactum.utils.Utils
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class NegotiationService(
  private val negotiationRepository: NegotiationRepository,
  @Value("\${server.baseUrl}") private val baseUrl: String,
  private val clientRepository: ClientRepository,
  private val extractService: ExtractService,
  private val auditEventService: AuditEventService,
  private val negotiationFieldService: NegotiationFieldService,
  private val negotiationModelTermService: NegotiationModelTermService,
  private val pubSubService: PubSubService,
  private val clientService: ClientService,
  private val modelService: ModelService,
  private val negotiationStateRepository: NegotiationStateRepository
) {

  fun createNegotiations(req: CreateNegotiationReq): GenericCreatedResponse {

    val list = req.modelKeys.map { modelKey ->
      val entity = trySaveNegotiation(Negotiation.create(req.clientId, req.flowId, req.modelId, modelKey))
      logOperation(NegotiationAuditEvent.NEGOTIATION_CREATED, entity)
      entity.toApiEntity(baseUrl)
    }
    return GenericCreatedResponse(list)
  }

  fun updateNegotiation(id: Long, req: UpdateNegotiationReq): GenericOkResponse {
    val negotiationOptional = negotiationRepository.findById(id)
    if (negotiationOptional.isPresent) {
      val oldNegotiation = negotiationOptional.get()
      var status = oldNegotiation.status
      if (!req.status.isNullOrEmpty()) {
        status = req.status
        val clientOptional = clientRepository.findById(oldNegotiation.clientId)
        val statuses = clientOptional.get().getConfig().negotiationStatuses
        if (!statuses.containsKey(status)) {
          SentryHelper.report(NegotiationStatusNotFoundException(status), mapOf("stateId" to oldNegotiation.stateId))
        }
      }
      val updatedNegotiation = oldNegotiation.copy(
        status = status,
        isVisibleClient = req.isVisibleClient ?: oldNegotiation.isVisibleClient,
        isVisibleSupplier = req.isVisibleSupplier ?: oldNegotiation.isVisibleSupplier
      )
      trySaveNegotiation(updatedNegotiation)
      if (req.fields != null) {
        negotiationFieldService.setFieldsForNegotiation(oldNegotiation.id!!, req.fields, req.comment)
      }
      logOperation(
        NegotiationAuditEvent.NEGOTIATION_UPDATED,
        updatedNegotiation,
        oldNegotiation,
        mapOf("comment" to req.comment)
      )
      return GenericOkResponse(updatedNegotiation.toApiEntity(baseUrl))
    }
    throw NegotiationIdNotFoundException(id)
  }

  fun updateNegotiationStatus(oldNegotiation: Negotiation, status: String, comment: String?): Negotiation {
    val clientOptional = clientRepository.findById(oldNegotiation.clientId)
    val statuses = clientOptional.get().getConfig().negotiationStatuses
    if (!statuses.containsKey(status)) {
      SentryHelper.report(NegotiationStatusNotFoundException(status), mapOf("stateId" to oldNegotiation.stateId))
    }
    val updatedNegotiation = oldNegotiation.copy(status = status)
    trySaveNegotiation(updatedNegotiation)
    logOperation(
      NegotiationAuditEvent.NEGOTIATION_UPDATED,
      updatedNegotiation,
      oldNegotiation,
      mapOf("comment" to comment)
    )
    return updatedNegotiation
  }

  fun updateNegotiationClientVisibility(oldNegotiation: Negotiation, visible: Boolean, comment: String?): Negotiation {
    val updatedNegotiation = oldNegotiation.copy(isVisibleClient = visible)
    trySaveNegotiation(updatedNegotiation)
    logOperation(
      NegotiationAuditEvent.NEGOTIATION_UPDATED,
      updatedNegotiation,
      oldNegotiation,
      mapOf("comment" to comment)
    )
    return updatedNegotiation
  }

  fun updateNegotiationSupplierVisibility(oldNegotiation: Negotiation, visible: Boolean, comment: String?): Negotiation {
    val updatedNegotiation = oldNegotiation.copy(isVisibleSupplier = visible)
    trySaveNegotiation(updatedNegotiation)
    logOperation(
      NegotiationAuditEvent.NEGOTIATION_UPDATED,
      updatedNegotiation,
      oldNegotiation,
      mapOf("comment" to comment)
    )
    return updatedNegotiation
  }

  fun deleteNegotiation(id: Long): GenericNoContentResponse {
    val negotiationOptional = negotiationRepository.findById(id)
    if (negotiationOptional.isPresent) {
      val negotiation = negotiationOptional.get()
      trySaveNegotiation(negotiation.copy(isDeleted = true))
      logOperation(NegotiationAuditEvent.NEGOTIATION_DELETED, negotiation)
      return GenericNoContentResponse()
    }
    throw NegotiationIdNotFoundException(id)
  }

  fun updateWithPublish(originalModel: Negotiation, newModel: Negotiation) {
    val oldTerms = negotiationModelTermService.getTerms(originalModel)
    val newTerms = JsonHelper.convertToMapOrEmpty(newModel.terms)
    val updatedTerms = newTerms.filter { (key, value) -> oldTerms.getOrDefault(key, "") != value }
    val updatedModel = newModel.copy(
      terms = jacksonObjectMapper().writeValueAsString(oldTerms + newTerms)
    )
    trySaveNegotiation(updatedModel)
    logOperation(NegotiationAuditEvent.NEGOTIATION_UPDATED, updatedModel, originalModel)

    val clientTag = clientService.getTagById(updatedModel.clientId)!!
    val messages = updatedTerms.map {
      val chatStartTime = negotiationStateRepository.findChatOpenTimeByStateId(updatedModel.stateId)
      val chatLastUpdateTime = negotiationStateRepository.findChatLastUpdateTimeByStateId(updatedModel.stateId)
      NegotiationTermChangedPubSubMessage(
        term = it.key,
        previousValue = oldTerms.getOrDefault(it.key, null),
        value = it.value,
        stateId = updatedModel.stateId,
        clientTag = clientTag,
        chatStartTime = chatStartTime?.toEpochMilli(),
        chatEndTime = chatLastUpdateTime?.toEpochMilli(),
        vendorId = updatedModel.modelKey
      )
    }
    publish(messages, clientTag, NegotiationPubSubEvent.TERM_CHANGED)
  }

  fun publish(messages: List<NegotiationPubSubMessage>, clientTag: String, event: NegotiationPubSubEvent) {

    if (messages.isEmpty())
      return

    val attributes = mapOf(
      "clientTag" to clientTag,
      "eventType" to event.name
    )
    pubSubService.publishMany(PubSubTopic.NEGOTIATION_EVENT, messages, attributes)
  }

  fun reloadNegotiationModel(req: ReloadNegotiationModelReq): GenericOkResponse {

    val list = mutableListOf<Negotiation.ApiEntity>()
    negotiationRepository.findAllById(req.negotiationIds).forEach { negotiation ->
      val modelMap = modelService.getModelFromFile(negotiation.modelId!!, negotiation.modelKey!!)
      if (modelMap.isNotEmpty()) {
        val updated = negotiation.copy(modelAttributes = jacksonObjectMapper().writeValueAsString(modelMap))
        trySaveNegotiation(updated)
        logOperation(NegotiationAuditEvent.NEGOTIATION_RELOAD_MODEL, updated, negotiation)
        list.add(updated.toApiEntity(baseUrl))
      }
    }

    return GenericOkResponse(list)
  }

  fun getNegotiationsByClientId(clientId: Long): List<Map<String, Any>> {
    val negotiations = negotiationRepository.findByClientIdAndIsDeletedIsFalseOrderByCreateTimeDesc(clientId)
    val client = clientRepository.findById(clientId).get()
    return negotiations
      .map {
        insertExtraFields(getDetails(client, it), baseUrl, Role.Admin)
      }
  }

  fun getNegotiationsForClient(): List<Map<String, Any>> {
    val clientTag = SessionHelper.getLoggedInUserClientTag() ?: throw AccessDeniedException()
    val client = clientRepository.findFirstByTag(clientTag)!!
    val negotiations = negotiationRepository.findByClientIdAndIsDeletedIsFalseOrderByCreateTimeDesc(client.id!!)
    return negotiations
      .filter { it.isVisibleSupplier }
      .map {
        insertExtraFields(getDetails(client, it), baseUrl, Role.Client)
      }
  }

  private fun getDetails(client: Client, negotiation: Negotiation): NegotiationDetails {
    val fields = negotiationFieldService.getFieldsForNegotiation(negotiation.id!!)
    return NegotiationDetails(negotiation, client, fields)
  }

  private fun insertExtraFields(details: NegotiationDetails, baseUrl: String, role: Role): Map<String, Any> {

    val map = Utils.cast<MutableMap<String, Any>>(details.toApiEntity(baseUrl))

    val modelMap = JsonHelper.convertToMapOrEmpty(details.negotiation.modelAttributes).toMutableMap()
    modelMap += JsonHelper.flattenAsMap(details.negotiation.modelAttributes)
    modelMap += negotiationFieldService.filterModelFields(details.fields)

    val termsMap = extractService.getTerms(details.negotiation).toMutableMap()
    termsMap += JsonHelper.flattenAsMap(termsMap)
    termsMap += negotiationFieldService.filterTermFields(details.fields)

    val thisRoleFields = details.client.getConfig().filterFieldsByRole(role)
    val otherFields = negotiationFieldService.filterOtherFields(details.fields)

    val extraList = mutableListOf<ExtraValue>()
    extraList.addAll(
      thisRoleFields.map {
        when (it.type) {
          NegotiationFieldConfigType.TERM -> Utils.getExtraValue(it.label, it.format, termsMap[it.attribute])
          NegotiationFieldConfigType.MODEL -> Utils.getExtraValue(it.label, it.format, modelMap[it.attribute])
          NegotiationFieldConfigType.OTHER -> Utils.getExtraValue(it.label, ExtraValueFormat.TEXT, otherFields[it.attribute])
        }
      }
    )
    map["extra"] = extraList
    return map
  }

  fun trySaveNegotiation(negotiation: Negotiation, retry: Boolean = true): Negotiation {
    return try {
      val uniqueNegotiation = createUniqueNegotiation(negotiation)
      negotiationRepository.save(uniqueNegotiation)
    } catch (e: Exception) {
      logger.error(e.localizedMessage, e)
      if (Utils.isDbInsertException(e)) {
        SentryHelper.report(e, mapOf("stateId" to negotiation.stateId))
        negotiationRepository.resolveInsertException()
      }
      if (!retry)
        throw e
      trySaveNegotiation(negotiation, false)
    }
  }

  private fun createUniqueNegotiation(negotiation: Negotiation): Negotiation {
    if (negotiation.id != null) {
      // it will update this negotiation
      return negotiation
    }
    var uniqueNegotiation = negotiation
    while (negotiationRepository.findFirstByStateIdAndIsDeletedIsFalseOrderByCreateTimeDesc(uniqueNegotiation.stateId) != null) {
      uniqueNegotiation = Negotiation.create(
        uniqueNegotiation.clientId,
        uniqueNegotiation.flowId,
        uniqueNegotiation.modelId,
        uniqueNegotiation.modelKey
      )
    }
    return uniqueNegotiation
  }

  fun logOperation(
    event: NegotiationAuditEvent,
    entity: Negotiation,
    previousEntity: Negotiation? = null,
    extraData: Map<String, Any?> = mapOf()
  ) {
    auditEventService.addEntityAuditEvent(event, entity.id!!, entity, previousEntity, extraData)
  }
}
