package com.pactum.negotiation

import com.pactum.client.model.Client
import com.pactum.client.ClientRepository
import com.pactum.negotiation.model.Negotiation
import com.pactum.test.RepositoryTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant

@RepositoryTest
class NegotiationRepositoryTest @Autowired constructor(
  private val negotiationRepository: NegotiationRepository,
  private val clientRepository: ClientRepository
) {

  @Test
  fun `finds by state id`() {

    val tag = "tag"
    val name = "name"
    val flowId = "abc"
    val modelId = "asd"
    val modelKey = "123"
    val client = Client.create(tag, name)
    val savedClient = clientRepository.save(client)
    val clientId = savedClient.id!!

    val negotiation = Negotiation.create(clientId, flowId, modelId, modelKey)
    val savedNegotiation = negotiationRepository.save(negotiation)

    val foundNegotiation = negotiationRepository.findFirstByStateIdAndIsDeletedIsFalseOrderByCreateTimeDesc(
      savedNegotiation.stateId
    )

    assertThat(foundNegotiation!!.clientId).isEqualTo(clientId)
  }

  @Test
  fun `finds by state ids`() {

    val tag = "tag"
    val name = "name"
    val flowId = "abc"
    val modelId = "asd"
    val modelKey = "123"
    val client = Client.create(tag, name)
    val savedClient = clientRepository.save(client)
    val clientId = savedClient.id!!

    val negotiation = Negotiation.create(clientId, flowId, modelId, modelKey)
    val savedNegotiation = negotiationRepository.save(negotiation)

    val foundNegotiation = negotiationRepository.findByStateIds(listOf(savedNegotiation.stateId))

    assertThat(foundNegotiation[0].clientId).isEqualTo(clientId)
  }

  @Test
  fun `finds by flow ids`() {

    val tag = "tag"
    val name = "name"
    val flowId = "abc"
    val modelId = "asd"
    val modelKey = "123"
    val client = Client.create(tag, name)
    val savedClient = clientRepository.save(client)
    val clientId = savedClient.id!!

    val negotiation = Negotiation.create(clientId, flowId, modelId, modelKey)
    val savedNegotiation = negotiationRepository.save(negotiation)

    val foundNegotiation = negotiationRepository.findByFlowIds(listOf(savedNegotiation.flowId))

    assertThat(foundNegotiation[0].clientId).isEqualTo(clientId)
  }

  @Test
  fun `finds by client id`() {

    val tag = "tag"
    val name = "name"
    val flowId = "abc"
    val modelId = "asd"
    val modelKey = "123"
    val client = Client.create(tag, name)
    val savedClient = clientRepository.save(client)
    val clientId = savedClient.id!!

    val negotiation = Negotiation.create(clientId, flowId, modelId, modelKey)
    negotiationRepository.save(negotiation)

    val foundNegotiation = negotiationRepository.findByClientIdAndIsDeletedIsFalseOrderByCreateTimeDesc(clientId)

    assertThat(foundNegotiation).size().isEqualTo(1)
    assertThat(foundNegotiation[0].clientId).isEqualTo(clientId)
  }

  @Test
  fun `finds by client id and flow id`() {

    val tag = "tag"
    val name = "name"
    val flowId = "abc"
    val modelId = "asd"
    val modelKey = "123"
    val client = Client.create(tag, name)
    val savedClient = clientRepository.save(client)
    val clientId = savedClient.id!!

    val negotiation = Negotiation.create(clientId, flowId, modelId, modelKey)
    negotiationRepository.save(negotiation)

    val foundNegotiation = negotiationRepository.findByClientIdAndFlowIdAndIsDeletedIsFalse(clientId, flowId)

    assertThat(foundNegotiation[0].clientId).isEqualTo(clientId)
  }

  @Test
  fun `finds by client id and flow id and model id and model key`() {

    val tag = "tag"
    val name = "name"
    val flowId = "abc"
    val modelId = "asd"
    val modelKey = "123"
    val client = Client.create(tag, name)
    val savedClient = clientRepository.save(client)
    val clientId = savedClient.id!!

    val negotiation = Negotiation.create(clientId, flowId, modelId, modelKey)
    negotiationRepository.save(negotiation)

    val foundNegotiation =
      negotiationRepository.findByClientIdAndFlowIdAndModelIdAndModelKeyAndIsDeletedIsFalse(clientId, flowId, modelId, modelKey)

    assertThat(foundNegotiation[0].clientId).isEqualTo(clientId)
  }

  @Test
  fun `can resolve insert exception`() {
    val tag = "tag"
    val name = "name"
    val flowId = "abc"
    val modelId = "asd"
    val modelKey = "123"
    val stateId = "678"
    val client = Client.create(tag, name)
    val savedClient = clientRepository.save(client)
    val clientId = savedClient.id!!

    val negotiation = Negotiation(
      clientId = clientId,
      flowId = flowId,
      modelId = modelId,
      modelKey = modelKey,
      stateId = stateId,
      status = "CREATED",
      createTime = Instant.now()
    )

    negotiationRepository.save(negotiation)
    val found = negotiationRepository.resolveInsertException()
    assertThat(found).isNotNull()
  }
}
