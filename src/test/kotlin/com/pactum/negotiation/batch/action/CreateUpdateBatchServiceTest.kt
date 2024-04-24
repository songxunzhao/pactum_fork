package com.pactum.negotiation.batch.action

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.pactum.auth.model.Role
import com.pactum.client.ClientNotFoundException
import com.pactum.client.ClientRepository
import com.pactum.client.model.Client
import com.pactum.negotiation.NegotiationRepository
import com.pactum.negotiation.batch.model.CreateBatchOfNegotiationsReq
import com.pactum.negotiation.batch.model.CreateNegotiationItem
import com.pactum.negotiation.model.Negotiation
import com.pactum.test.UnitTest
import com.pactum.auth.SessionHelper
import com.pactum.negotiation.NegotiationService
import org.springframework.http.HttpStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@UnitTest
class CreateUpdateBatchServiceTest {

  private val negotiationRepository: NegotiationRepository = mock()
  private val clientRepository: ClientRepository = mock()
  private val negotiationService: NegotiationService = mock()
  private val baseUrl = "https://www.pactum.com"

  private val createUpdateBatchService = CreateUpdateBatchService(
    negotiationRepository,
    negotiationService,
    baseUrl,
    clientRepository
  )

  @Test
  fun `can create a new batch of negotiations`() {
    val clientId = 0L
    val clientTag = "tag"
    val flowId = "123"
    val modelId = "qwr"
    val modelKey = "asd"
    val email = "backend@pactum.com"
    val token = "avalidtoken"
    val status = "foobar"
    val attributes = mapOf(
      "vendorId" to "1",
      "attr1" to true,
      "attr2" to 2.5
    )

    val items = listOf(CreateNegotiationItem(flowId, modelId, modelKey, attributes, status))
    val req = CreateBatchOfNegotiationsReq(clientTag, null, items)
    val negotiation = Negotiation.create(clientId, flowId, modelId, modelKey, attributes, status)
    val client = Client(clientId, clientTag, "name")

    whenever(clientRepository.findFirstByTag(clientTag)).thenReturn(client)
    whenever(
      negotiationRepository.findByClientIdAndFlowIdAndModelIdAndModelKeyAndIsDeletedIsFalse(clientId, flowId, modelId, modelKey)
    ).thenReturn(listOf(negotiation))
    whenever(negotiationService.trySaveNegotiation(negotiation)).thenReturn(negotiation)

    SessionHelper.setLoggedInUser(token, email, listOf(Role.Admin))

    val resp = createUpdateBatchService.createUpdate(req)
    assertThat(resp.statusCode).isEqualTo(HttpStatus.OK)
    val body = resp.body as CreateUpdateBatchService.BatchOfNegotiationsHolder
    assertThat(body.updated.size).isEqualTo(1)
    assertThat(body.updated[0].clientTag).isEqualTo(clientTag)
    assertThat(body.updated[0].status).isEqualTo(status)
    assertThat(body.created.size).isEqualTo(0)
  }

  @Test
  fun `can not create a new batch of negotiations if client tag is wrong`() {
    val clientTag = "tag"
    val flowId = "123"
    val modelId = "qwr"
    val modelKey = "asd"
    val email = "backend@pactum.com"
    val token = "avalidtoken"
    val attributes = mapOf(
      "vendorId" to "1",
      "attr1" to true,
      "attr2" to 2.5
    )

    val items = listOf(CreateNegotiationItem(flowId, modelId, modelKey, attributes, null))
    val req = CreateBatchOfNegotiationsReq(clientTag, null, items)

    whenever(clientRepository.findFirstByTag(clientTag)).thenReturn(null)

    SessionHelper.setLoggedInUser(token, email, listOf(Role.Admin))

    assertThrows<ClientNotFoundException> {
      createUpdateBatchService.createUpdate(req)
    }
  }
}
