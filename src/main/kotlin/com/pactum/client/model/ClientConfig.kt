package com.pactum.client.model

import com.pactum.auth.model.Role
import com.pactum.negotiation.model.NegotiationStatusField
import kotlin.experimental.and

data class ClientConfig(
  val negotiationFields: List<NegotiationFieldConfig>,
  val negotiationStatuses: Map<String, NegotiationStatusField>,
  val negotiationSummaryFields: NegotiationSummaryFields,
  val dashboardUrl: String?
) {
  fun filterFieldsByRole(role: Role): List<NegotiationFieldConfig> {
    return negotiationFields.filter {
      it.properties.and(role.property) == role.property
    }
  }

  fun filterSummaryByRole(role: Role): NegotiationSummaryList {
    return when (role) {
      Role.Admin -> negotiationSummaryFields.admin
      Role.Client -> negotiationSummaryFields.client
      else -> NegotiationSummaryList.empty()
    }
  }
}
