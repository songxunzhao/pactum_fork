package com.pactum.negotiation.model

import com.pactum.chat.model.ChatApiInput
import com.pactum.client.model.Client
import com.pactum.negotiationfield.model.NegotiationField
import com.pactum.utils.Utils

data class NegotiationDetails(
  val negotiation: Negotiation,
  val client: Client,
  val fields: List<NegotiationField>
) {
  data class ApiEntity(
    val id: Long?,
    val stateId: String,
    val clientTag: String? = null,
    val clientName: String? = null,
    val status: String? = null,
    val link: String? = null,
    val createTime: Long,
    val isVisibleClient: Boolean,
    val isVisibleSupplier: Boolean
  )

  fun toApiEntity(baseUrl: String): ApiEntity {
    val chatLink = Utils.generateChatLink(
      baseUrl,
      ChatApiInput(
        negotiation.flowId,
        negotiation.stateId,
        negotiation.modelId,
        negotiation.modelKey,
        true,
        "",
        null,
        null
      )
    )
    val statusField = client.getConfig().negotiationStatuses
    val status = statusField[negotiation.status]?.label ?: ""
    return ApiEntity(
      negotiation.id,
      negotiation.stateId,
      client.tag,
      client.name,
      status,
      chatLink,
      negotiation.createTime.toEpochMilli(),
      negotiation.isVisibleClient,
      negotiation.isVisibleSupplier
    )
  }
}
