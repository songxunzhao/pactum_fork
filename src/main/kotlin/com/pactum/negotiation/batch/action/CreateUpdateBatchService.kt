package com.pactum.negotiation.batch.action

import com.pactum.chat.model.ChatApiInput
import com.pactum.client.ClientNotFoundException
import com.pactum.client.ClientRepository
import com.pactum.api.GenericOkResponse
import com.pactum.negotiation.NegotiationRepository
import com.pactum.negotiation.NegotiationService
import com.pactum.negotiation.batch.model.CreateBatchOfNegotiationsReq
import com.pactum.negotiation.model.Negotiation
import com.pactum.negotiation.model.NegotiationAuditEvent
import com.pactum.negotiation.model.NegotiationCreatedUpdatedPubSubMessage
import com.pactum.negotiation.model.NegotiationPubSubEvent
import com.pactum.utils.Utils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class CreateUpdateBatchService(
  private val negotiationRepository: NegotiationRepository,
  private val negotiationService: NegotiationService,
  @Value("\${server.baseUrl}") private val baseUrl: String,
  private val clientRepository: ClientRepository
) {

  fun createUpdate(req: CreateBatchOfNegotiationsReq): GenericOkResponse {
    val all = innerCreateUpdateBatchOfNegotiations(req)
    negotiationService.publish(all.created, req.clientTag, NegotiationPubSubEvent.NEGOTIATION_CREATED)
    return GenericOkResponse(all)
  }

  private fun innerCreateUpdateBatchOfNegotiations(
    req: CreateBatchOfNegotiationsReq
  ): BatchOfNegotiationsHolder {

    val clientId = clientRepository.findFirstByTag(req.clientTag)?.id ?: throw ClientNotFoundException(req.clientTag)
    val created = mutableListOf<NegotiationCreatedUpdatedPubSubMessage>()
    val updated = mutableListOf<NegotiationCreatedUpdatedPubSubMessage>()

    for (item in req.batch) {
      val oldNegotiations = negotiationRepository.findByClientIdAndFlowIdAndModelIdAndModelKeyAndIsDeletedIsFalse(
        clientId,
        item.flowId,
        item.modelId,
        item.modelKey
      )

      val isNegotiationStarted = oldNegotiations.any { !it.terms.isNullOrEmpty() }
      if (isNegotiationStarted) {
        // dont overwrite model attributes
        continue
      }
      // overwrite model attributes on all negotiations of this vendor
      var shouldCreateNegotiation = true
      for (oldNegotiation in oldNegotiations) {
        val updatedNegotiation = negotiationService.trySaveNegotiation(
          Negotiation.copy(oldNegotiation, item.flowId, item.modelId, item.modelKey, item.modelAttributes)
        )
        negotiationService.logOperation(
          NegotiationAuditEvent.NEGOTIATION_UPDATED,
          updatedNegotiation,
          oldNegotiation
        )
        val pubsubMessage = getPubSubMessage(updatedNegotiation, req.clientTag, req.customUrl)
        updated.add(pubsubMessage)
        shouldCreateNegotiation = false
      }
      if (shouldCreateNegotiation) {
        // negotiation object is not created yet
        val createdNegotiation = negotiationService.trySaveNegotiation(
          Negotiation.create(clientId, item.flowId, item.modelId, item.modelKey, item.modelAttributes, item.status)
        )
        negotiationService.logOperation(NegotiationAuditEvent.NEGOTIATION_CREATED, createdNegotiation)
        val pubsubMessage = getPubSubMessage(createdNegotiation, req.clientTag, req.customUrl)
        created.add(pubsubMessage)
      }
    }

    return BatchOfNegotiationsHolder(created, updated)
  }

  private fun getPubSubMessage(
    createdNegotiation: Negotiation,
    clientTag: String,
    customUrl: String?
  ): NegotiationCreatedUpdatedPubSubMessage {
    val chatHolder = ChatApiInput(
      flowId = createdNegotiation.flowId,
      modelId = createdNegotiation.modelId,
      modelKey = createdNegotiation.modelKey,
      stateId = createdNegotiation.stateId,
      readOnly = false
    )
    return NegotiationCreatedUpdatedPubSubMessage(
      vendorId = createdNegotiation.modelKey ?: "",
      stateId = createdNegotiation.stateId,
      chatUrl = Utils.generateChatLink(customUrl ?: baseUrl, chatHolder),
      chatUrlReadOnly = Utils.generateChatLink(customUrl ?: baseUrl, chatHolder.copy(readOnly = true)),
      clientTag = clientTag,
      status = createdNegotiation.status
    )
  }

  data class BatchOfNegotiationsHolder(
    val created: List<NegotiationCreatedUpdatedPubSubMessage>,
    val updated: List<NegotiationCreatedUpdatedPubSubMessage>
  )
}
