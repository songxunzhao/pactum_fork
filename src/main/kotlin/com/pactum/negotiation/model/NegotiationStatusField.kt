package com.pactum.negotiation.model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

data class NegotiationStatusField(
  val label: String
) {
  companion object {
    fun fromJson(json: String?): Map<String, NegotiationStatusField> {
      return try {
        jacksonObjectMapper().readValue(json!!)
      } catch (e: Exception) {
        emptyMap()
      }
    }
  }
}
