package com.pactum.negotiation.summary.model

data class NegotiationsSummary(
  val totalCount: Int,
  val openedCount: Int,
  val finishedCount: Int,
  val extra: List<ExtraValue>
)

data class ExtraValue(
  val label: String,
  val value: Any?,
  val type: ExtraValueFormat
)

enum class ExtraValueFormat {
  TIME,
  LINK,
  TEXT,
  NUMBER,
  PERCENT,
  CURRENCY,
  BOOLEAN,
  EMPTY
}
