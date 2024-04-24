package com.pactum.negotiation

import com.pactum.negotiation.model.Negotiation
import com.pactum.utils.JsonHelper
import com.pactum.negotiationfield.NegotiationFieldService
import org.springframework.stereotype.Service

@Service
class NegotiationModelTermService(
  private val negotiationFieldService: NegotiationFieldService
) {

  fun getModels(negotiation: Negotiation): Map<String, Any> {
    val fields = negotiationFieldService.getFieldsForNegotiation(negotiation.id!!)
    return JsonHelper.convertToMapOrEmpty(negotiation.modelAttributes) + negotiationFieldService.filterModelFields(fields)
  }

  fun getTerms(negotiation: Negotiation): Map<String, Any> {
    val fields = negotiationFieldService.getFieldsForNegotiation(negotiation.id!!)
    return JsonHelper.convertToMapOrEmpty(negotiation.terms) + negotiationFieldService.filterTermFields(fields)
  }
}
