package com.pactum.chat.model

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.ReadOnlyProperty
import java.time.Instant

data class NegotiationState(
  @Id
  val id: Long? = null,
  val stateId: String,
  val state: String,
  @ReadOnlyProperty
  val time: Instant? = null,
  val negotiationId: Long? = null
) {
  companion object {
    fun new(
      stateId: String,
      state: String = "{}",
      negotiationId: Long?
    ): NegotiationState =
      NegotiationState(
        id = null,
        state = state,
        time = Instant.now(),
        negotiationId = negotiationId,
        stateId = stateId
      )
  }
}
