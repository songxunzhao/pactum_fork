package com.pactum.client.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.pactum.negotiation.summary.model.ExtraValueFormat

@JsonIgnoreProperties(ignoreUnknown = true)
data class NegotiationSummaryFields(
  val admin: NegotiationSummaryList,
  val client: NegotiationSummaryList
) {
  companion object {
    fun fromJson(json: String?): NegotiationSummaryFields {
      return try {
        jacksonObjectMapper().readValue(json!!)
      } catch (e: Exception) {
        empty()
      }
    }
    fun empty(): NegotiationSummaryFields {
      return NegotiationSummaryFields(NegotiationSummaryList.empty(), NegotiationSummaryList.empty())
    }
  }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class NegotiationSummaryList(
  val summary: List<NegotiationSummaryField>
) {
  companion object {
    fun empty(): NegotiationSummaryList {
      return NegotiationSummaryList(emptyList())
    }
  }
}

data class NegotiationSummaryField(
  val key: String,
  val label: String,
  val operation: ExtraSummaryOperation,
  val type: ExtraValueFormat
)

enum class ExtraSummaryOperation {
  AVE,
  SUM,
  COUNT
}
