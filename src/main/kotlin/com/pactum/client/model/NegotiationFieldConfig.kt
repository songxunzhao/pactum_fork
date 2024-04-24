package com.pactum.client.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.pactum.negotiation.summary.model.ExtraValueFormat

@JsonIgnoreProperties(ignoreUnknown = true)
data class NegotiationFieldConfig(
  val type: NegotiationFieldConfigType,
  val label: String,
  val format: ExtraValueFormat,
  val attribute: String,
  val properties: Byte
) {
  companion object {
    fun fromJson(json: String?): List<NegotiationFieldConfig> {
      return try {
        jacksonObjectMapper().readValue(json!!)
      } catch (e: Exception) {
        empty()
      }
    }
    fun empty(): List<NegotiationFieldConfig> {
      return emptyList()
    }
  }
}

enum class NegotiationFieldConfigType {
  MODEL,
  TERM,
  OTHER
}
