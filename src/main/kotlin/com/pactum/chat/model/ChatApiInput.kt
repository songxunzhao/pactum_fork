package com.pactum.chat.model

import com.pactum.utils.Utils

interface ChatHolder {
  val flowId: String
  val stateId: String
  val modelId: String?
  val modelKey: String?
  val readOnly: Boolean
}

interface StateHolder {
  val state: String
  val stepId: String?
  val value: String?
}

data class ChatApiInput(
  override val flowId: String = "",
  override val stateId: String = "",
  override val modelId: String? = null,
  override val modelKey: String? = null,
  override val readOnly: Boolean = false,
  override val state: String = "",
  override val stepId: String? = null,
  override val value: String? = null,
  val shouldCreateNegotiationIfNotFound: Boolean = true
) : ChatHolder, StateHolder {
  companion object {
    fun createNewStateId(stateId: String, sessionId: String, secretStateId: String): String {
      return if (Utils.isSecretStateId(stateId, secretStateId))
        stateId.plus(sessionId)
      else
        stateId
    }
  }
}
