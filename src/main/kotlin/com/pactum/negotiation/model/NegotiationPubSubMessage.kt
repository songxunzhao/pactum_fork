package com.pactum.negotiation.model

interface NegotiationPubSubMessage

data class NegotiationTermChangedPubSubMessage(
  val term: String,
  val previousValue: Any?,
  val value: Any,
  val stateId: String,
  val clientTag: String,
  val chatStartTime: Long? = null,
  val chatEndTime: Long? = null,
  val vendorId: String? = null
) : NegotiationPubSubMessage

data class NegotiationCreatedUpdatedPubSubMessage(
  val vendorId: String,
  val stateId: String,
  val chatUrl: String,
  val chatUrlReadOnly: String,
  val clientTag: String,
  val status: String? = null
) : NegotiationPubSubMessage

enum class NegotiationPubSubEvent {
  NEGOTIATION_CREATED,
  TERM_CHANGED,
}
