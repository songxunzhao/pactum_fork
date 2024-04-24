package com.pactum.negotiation.model

import com.pactum.negotiationfield.model.NegotiationField

data class CreateNegotiationReq(
  val clientId: Long,
  val flowId: String,
  val modelId: String,
  val modelKeys: List<String>
)

data class ReloadNegotiationModelReq(
  val negotiationIds: List<Long>
)

data class UpdateNegotiationReq(
  val status: String? = null,
  val isVisibleClient: Boolean? = null,
  val isVisibleSupplier: Boolean? = null,
  val comment: String? = null,
  val fields: List<NegotiationField.ApiEntity>? = null
)
