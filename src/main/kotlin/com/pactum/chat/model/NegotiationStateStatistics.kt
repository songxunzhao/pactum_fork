package com.pactum.chat.model

import java.time.Instant

data class NegotiationStateStatistics(
  val stateId: String,
  val flowId: String,
  val modelId: String?,
  val modelKey: String?,
  val chatStartTime: Instant,
  val chatLastUpdateTime: Instant,
  val completedSteps: Int,
  val chatEnd: Boolean,
  val chatLink: String
)
