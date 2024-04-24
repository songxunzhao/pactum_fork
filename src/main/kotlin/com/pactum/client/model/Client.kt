package com.pactum.client.model

import com.pactum.negotiation.model.NegotiationStatusField
import org.springframework.data.annotation.Id

data class Client(
  @Id
  val id: Long? = null,
  val tag: String,
  val name: String,
  val dashboardUrl: String? = null,
  val negotiationFieldsConfig: String? = null,
  val negotiationStatusFields: String? = null,
  val negotiationSummaryFields: String? = null
) {
  companion object {
    fun create(tag: String, name: String): Client {
      return Client(tag = tag, name = name)
    }
  }

  fun getConfig() = ClientConfig(
    NegotiationFieldConfig.fromJson(negotiationFieldsConfig),
    NegotiationStatusField.fromJson(negotiationStatusFields),
    NegotiationSummaryFields.fromJson(negotiationSummaryFields),
    dashboardUrl
  )

  data class ApiEntity(
    val id: Long? = null,
    val tag: String,
    val name: String
  )

  fun toApiEntity(): ApiEntity {
    return ApiEntity(id, tag, name)
  }
}
