package com.pactum.negotiationfield

import com.pactum.negotiationfield.model.NegotiationField
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface NegotiationFieldRepository : CrudRepository<NegotiationField, Long> {
  fun findAllByNegotiationId(negotiationId: Long): List<NegotiationField>
}
