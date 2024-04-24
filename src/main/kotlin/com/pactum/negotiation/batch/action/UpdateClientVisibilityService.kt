package com.pactum.negotiation.batch.action

import com.pactum.api.GenericOkResponse
import com.pactum.negotiation.NegotiationRepository
import com.pactum.negotiation.NegotiationService
import com.pactum.negotiation.model.Negotiation
import com.pactum.negotiation.model.UpdateNegotiationReq
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class UpdateClientVisibilityService(
  private val negotiationRepository: NegotiationRepository,
  private val negotiationService: NegotiationService,
  @Value("\${server.baseUrl}") private val baseUrl: String
) {

  fun updateClientVisibility(req: Map<String, UpdateNegotiationReq>): GenericOkResponse {
    val list = mutableListOf<Negotiation.ApiEntity>()
    req.forEach {
      if (it.value.isVisibleClient != null) {
        val negotiationOptional = negotiationRepository.findById(it.key.toLong())
        if (negotiationOptional.isPresent) {
          val updated = negotiationService.updateNegotiationClientVisibility(
            negotiationOptional.get(),
            it.value.isVisibleClient!!,
            it.value.comment
          )
          list.add(updated.toApiEntity(baseUrl))
        }
      }
    }
    return GenericOkResponse(list)
  }
}
