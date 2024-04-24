package com.pactum.chat.model

import com.pactum.utils.JsonHelper
import java.time.Instant

data class NegotiationHistoryItem(
  val id: String,
  val flowId: String,
  val stateId: String,
  val time: Instant?,
  val message: String,
  val user: Boolean,
  var timeSincePreviousSec: Long? = null
) {
  companion object {
    fun createFromNegotiationState(negotiationState: NegotiationState, flowId: String): NegotiationHistoryItem {
      val jsonState = JsonHelper.toJsonObject(negotiationState.state)
      val currentStep = jsonState["currentStep"] as Map<*, *>
      return NegotiationHistoryItem(
        id = currentStep["id"] as String,
        flowId = flowId,
        stateId = negotiationState.stateId,
        time = negotiationState.time,
        message = currentStep["message"] as? String ?: "",
        user = currentStep["user"] as? Boolean ?: false
      )
    }
  }
}
