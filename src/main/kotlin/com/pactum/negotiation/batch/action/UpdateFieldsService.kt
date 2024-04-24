package com.pactum.negotiation.batch.action

import com.pactum.api.GenericOkResponse
import com.pactum.negotiation.NegotiationRepository
import com.pactum.negotiation.model.Negotiation
import com.pactum.negotiation.model.UpdateNegotiationReq
import com.pactum.negotiationfield.NegotiationFieldService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class UpdateFieldsService(
  private val negotiationRepository: NegotiationRepository,
  private val negotiationFieldService: NegotiationFieldService,
  @Value("\${server.baseUrl}") private val baseUrl: String
) {

  fun updateFields(req: Map<String, UpdateNegotiationReq>): GenericOkResponse {
    val list = mutableListOf<Negotiation.ApiEntity>()
    req.forEach {
      if (it.value.fields != null) {
        negotiationFieldService.setFieldsForNegotiation(
          it.key.toLong(),
          it.value.fields!!,
          it.value.comment,
          NegotiationFieldService.Behaviour.ADD_UPDATE
        )
        val negotiationOptional = negotiationRepository.findById(it.key.toLong())
        if (negotiationOptional.isPresent) {
          list.add(negotiationOptional.get().toApiEntity(baseUrl))
        }
      }
    }
    return GenericOkResponse(list)
  }
}
