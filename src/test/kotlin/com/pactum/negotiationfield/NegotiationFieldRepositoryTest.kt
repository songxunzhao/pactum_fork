package com.pactum.negotiationfield

import com.pactum.client.ClientRepository
import com.pactum.client.model.Client
import com.pactum.negotiation.NegotiationRepository
import com.pactum.negotiation.model.Negotiation
import com.pactum.negotiationfield.model.NegotiationField
import com.pactum.test.RepositoryTest
import com.pactum.test.TestClockHolder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@RepositoryTest
class NegotiationFieldRepositoryTest @Autowired constructor(
  private val negotiationRepository: NegotiationRepository,
  private val negotiationFieldRepository: NegotiationFieldRepository,
  private val clientRepository: ClientRepository
) {

  private lateinit var client: Client
  private lateinit var negotiation1: Negotiation
  private lateinit var negotiation2: Negotiation

  @Test
  fun `finds all by negotiation ID`() {
    client = clientRepository.save(Client.create("tag", "name"))
    negotiation1 = negotiationRepository.save(Negotiation.create(client.id!!, "a", "b"))
    negotiation2 = negotiationRepository.save(Negotiation.create(client.id!!, "a", "b"))

    val time = TestClockHolder.NOW

    val field1 = NegotiationField(null, negotiation1.id!!, "a", "b", "c", time)
    val field2 = NegotiationField(null, negotiation2.id!!, "a1", "b2", "c3", time)

    negotiationFieldRepository.save(field1)
    val savedField2 = negotiationFieldRepository.save(field2)

    val foundFields = negotiationFieldRepository.findAllByNegotiationId(negotiation2.id!!)

    assertThat(foundFields.size).isEqualTo(1)
    assertThat(foundFields[0]).isEqualTo(savedField2)
  }
}
