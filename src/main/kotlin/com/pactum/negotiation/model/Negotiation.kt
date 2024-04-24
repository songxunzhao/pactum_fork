package com.pactum.negotiation.model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.pactum.chat.model.ChatApiInput
import com.pactum.utils.Utils
import org.springframework.data.annotation.Id
import java.time.Instant

data class Negotiation(
  @Id
  val id: Long? = null,
  val clientId: Long,
  val stateId: String,
  val status: String? = null,
  val flowId: String,
  val modelId: String?,
  val modelKey: String?,
  val flowVersionId: Long? = null,
  val modelVersionId: Long? = null,
  val createTime: Instant,
  val terms: String? = null,
  val modelAttributes: String? = null,
  val chatStartTime: Instant? = null,
  val chatUpdateTime: Instant? = null,
  val isVisibleClient: Boolean = true,
  val isVisibleSupplier: Boolean = true,
  val isDeleted: Boolean = false
) {
  companion object {
    fun create(
      clientId: Long,
      flowId: String,
      modelId: String?,
      modelKey: String?,
      modelAttributes: Map<String, Any>? = null,
      status: String? = null
    ): Negotiation {
      val stateId = Utils.generateStateId(flowId)
      return Negotiation(
        clientId = clientId,
        stateId = stateId,
        flowId = flowId,
        modelId = modelId,
        modelKey = modelKey,
        createTime = Instant.now(),
        modelAttributes = jacksonObjectMapper().writeValueAsString(modelAttributes) ?: null,
        status = status
      )
    }

    fun copy(
      oldNegotiation: Negotiation,
      flowId: String,
      modelId: String?,
      modelKey: String?,
      modelAttributes: Map<String, Any>? = null
    ): Negotiation {
      return Negotiation(
        oldNegotiation.id, oldNegotiation.clientId, oldNegotiation.stateId, oldNegotiation.status,
        flowId, modelId, modelKey, oldNegotiation.flowVersionId, oldNegotiation.modelVersionId, oldNegotiation.createTime,
        oldNegotiation.terms, jacksonObjectMapper().writeValueAsString(modelAttributes) ?: null,
        oldNegotiation.chatStartTime, oldNegotiation.chatUpdateTime
      )
    }

    fun create(clientId: Long, flowId: String, stateId: String): Negotiation {
      return Negotiation(
        null, clientId, stateId, null,
        flowId, null, null, null, null, Instant.now()
      )
    }

    fun empty(id: Long): Negotiation {
      return Negotiation(
        id, 0, "", "", "",
        "", "", null, null, Instant.now()
      )
    }
  }

  data class ApiEntity(
    val id: Long? = null,
    val stateId: String,
    val chatUrl: String
  )

  fun toApiEntity(baseUrl: String): ApiEntity {
    val chatHolder = ChatApiInput(
      flowId = flowId,
      modelId = modelId,
      modelKey = modelKey,
      stateId = stateId,
      readOnly = true
    )
    return ApiEntity(id, stateId, Utils.generateChatLink(baseUrl, chatHolder))
  }
}
