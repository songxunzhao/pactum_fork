package com.pactum.negotiation.batch.model

data class CreateBatchOfNegotiationsReq(
  val clientTag: String,
  val customUrl: String? = null,
  val batch: List<CreateNegotiationItem>
)

data class CreateNegotiationItem(
  val flowId: String,
  val modelId: String,
  val modelKey: String,
  val modelAttributes: Map<String, Any>?,
  val status: String?
)

data class BatchActionReq(
  val action: BatchActionType,
  val body: Any
) {
  override fun equals(other: Any?): Boolean {
    return action == (other as? BatchActionReq)?.action
  }

  override fun hashCode(): Int {
    return 31 * action.ordinal
  }
}
