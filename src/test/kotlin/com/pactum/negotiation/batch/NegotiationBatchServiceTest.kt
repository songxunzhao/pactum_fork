package com.pactum.negotiation.batch

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.pactum.api.GenericOkResponse
import com.pactum.auth.model.Role
import com.pactum.client.ClientNotFoundException
import com.pactum.negotiation.batch.model.CreateBatchOfNegotiationsReq
import com.pactum.negotiation.batch.model.CreateNegotiationItem
import com.pactum.negotiation.model.Negotiation
import com.pactum.negotiation.model.UpdateNegotiationReq
import com.pactum.test.UnitTest
import com.pactum.auth.SessionHelper
import com.pactum.client.model.NegotiationFieldConfigType
import com.pactum.negotiation.NegotiationService
import com.pactum.negotiation.batch.action.CreateUpdateBatchService
import com.pactum.negotiation.batch.action.UpdateClientVisibilityService
import com.pactum.negotiation.batch.action.UpdateFieldsService
import com.pactum.negotiation.batch.action.UpdateStatusService
import com.pactum.negotiation.batch.action.UpdateSupplierVisibilityService
import com.pactum.negotiation.batch.model.BatchActionReq
import com.pactum.negotiation.batch.model.BatchActionType
import com.pactum.negotiation.model.NegotiationCreatedUpdatedPubSubMessage
import com.pactum.negotiation.model.ReloadNegotiationModelReq
import com.pactum.negotiationfield.model.NegotiationField
import org.springframework.http.HttpStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@UnitTest
class NegotiationBatchServiceTest {

  private val negotiationService: NegotiationService = mock()
  private val createUpdateBatchService: CreateUpdateBatchService = mock()
  private val updateStatusService: UpdateStatusService = mock()
  private val updateClientVisibilityService: UpdateClientVisibilityService = mock()
  private val updateSupplierVisibilityService: UpdateSupplierVisibilityService = mock()
  private val updateFieldsService: UpdateFieldsService = mock()
  private val baseUrl = "https://www.pactum.com"

  private val negotiationBatchService = NegotiationBatchService(
    negotiationService,
    createUpdateBatchService,
    updateStatusService,
    updateClientVisibilityService,
    updateSupplierVisibilityService,
    updateFieldsService
  )

  @Test
  fun `can create a new batch of negotiations`() {
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
    val result = CreateUpdateBatchService.BatchOfNegotiationsHolder(
      emptyList(),
      listOf(
        NegotiationCreatedUpdatedPubSubMessage(
          "1",
          "stateId",
          "",
          "",
          clientTag,
          status
        )
      )
    )
    val items = listOf(CreateNegotiationItem(flowId, modelId, modelKey, attributes, status))
    val req = CreateBatchOfNegotiationsReq(clientTag, null, items)
    val batchActionReq = BatchActionReq(BatchActionType.CREATE_UPDATE_BATCH, req)

    whenever(createUpdateBatchService.createUpdate(req)).thenReturn(GenericOkResponse(result))

    SessionHelper.setLoggedInUser(token, email, listOf(Role.Admin))

    val resp = negotiationBatchService.batchAction(batchActionReq, mapOf())
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
    val modelKey1 = "asd1"
    val modelKey2 = "asd2"
    val email = "backend@pactum.com"
    val token = "avalidtoken"
    val attributes = mapOf(
      "vendorId" to "1",
      "attr1" to true,
      "attr2" to 2.5
    )

    val items = listOf(
      CreateNegotiationItem(flowId, modelId, modelKey1, attributes, null),
      CreateNegotiationItem(flowId, modelId, modelKey2, attributes, null)
    )
    val req = CreateBatchOfNegotiationsReq(clientTag, null, items)
    val batchActionReq = BatchActionReq(BatchActionType.CREATE_UPDATE_BATCH, req)

    whenever(createUpdateBatchService.createUpdate(req)).thenThrow(ClientNotFoundException::class.java)

    SessionHelper.setLoggedInUser(token, email, listOf(Role.Admin))

    assertThrows<ClientNotFoundException> {
      negotiationBatchService.batchAction(batchActionReq, mapOf())
    }
  }

  @Test
  fun `can reload models of a negotiation list`() {
    val negId1 = 1L
    val negId2 = 2L
    val stateId1 = "stateId1"
    val stateId2 = "stateId2"
    val email = "backend@pactum.com"
    val token = "avalidtoken"

    val neg1 = Negotiation.ApiEntity(negId1, stateId1, "")
    val neg2 = Negotiation.ApiEntity(negId2, stateId2, "")

    val req = ReloadNegotiationModelReq(listOf(negId1, negId2))
    val batchActionReq = BatchActionReq(BatchActionType.RELOAD_MODEL, req)

    whenever(negotiationService.reloadNegotiationModel(req)).thenReturn(GenericOkResponse(listOf(neg1, neg2)))

    SessionHelper.setLoggedInUser(token, email, listOf(Role.Admin))

    val resp = negotiationBatchService.batchAction(batchActionReq, mapOf())
    assertThat(resp.statusCode).isEqualTo(HttpStatus.OK)
    @Suppress("UNCHECKED_CAST")
    val body = resp.body as List<Negotiation.ApiEntity>
    assertThat(body.size).isEqualTo(2)
    assertThat(body[0].id).isEqualTo(negId1)
    assertThat(body[0].stateId).isEqualTo(stateId1)
    assertThat(body[1].id).isEqualTo(negId2)
    assertThat(body[1].stateId).isEqualTo(stateId2)
  }

  @Test
  fun `can update status of list of negotiations`() {
    val negId1 = 1L
    val negId2 = 2L
    val stateId1 = "stateId1"
    val stateId2 = "stateId2"
    val email = "backend@pactum.com"
    val token = "avalidtoken"
    val status = "ONGOING"

    val neg1 = Negotiation.ApiEntity(negId1, stateId1, "")
    val neg2 = Negotiation.ApiEntity(negId2, stateId2, "")
    val req = UpdateNegotiationReq(status = status, comment = "comment")
    val batchReq = mapOf(
      "$negId1" to req,
      "$negId2" to req
    )
    val batchActionReq = BatchActionReq(BatchActionType.UPDATE_STATUS, batchReq)

    whenever(updateStatusService.updateStatus(batchReq)).thenReturn(GenericOkResponse(listOf(neg1, neg2)))

    SessionHelper.setLoggedInUser(token, email, listOf(Role.Admin))

    val resp = negotiationBatchService.batchAction(batchActionReq, mapOf())
    assertThat(resp.statusCode).isEqualTo(HttpStatus.OK)
    @Suppress("UNCHECKED_CAST")
    val body = resp.body as List<Negotiation.ApiEntity>
    assertThat(body.size).isEqualTo(2)
    assertThat(body[0].id).isEqualTo(negId1)
    assertThat(body[0].stateId).isEqualTo(stateId1)
    assertThat(body[1].id).isEqualTo(negId2)
    assertThat(body[1].stateId).isEqualTo(stateId2)
  }

  @Test
  fun `can update client visibility of list of negotiations`() {
    val negId1 = 1L
    val negId2 = 2L
    val stateId1 = "stateId1"
    val stateId2 = "stateId2"
    val email = "backend@pactum.com"
    val token = "avalidtoken"

    val neg1 = Negotiation.ApiEntity(negId1, stateId1, "")
    val neg2 = Negotiation.ApiEntity(negId2, stateId2, "")
    val req = UpdateNegotiationReq(isVisibleClient = true, comment = "comment")
    val batchReq = mapOf(
      "$negId1" to req,
      "$negId2" to req
    )
    val batchActionReq = BatchActionReq(BatchActionType.UPDATE_CLIENT_VISIBILITY, batchReq)

    whenever(updateClientVisibilityService.updateClientVisibility(batchReq)).thenReturn(GenericOkResponse(listOf(neg1, neg2)))

    SessionHelper.setLoggedInUser(token, email, listOf(Role.Admin))

    val resp = negotiationBatchService.batchAction(batchActionReq, mapOf())
    assertThat(resp.statusCode).isEqualTo(HttpStatus.OK)
    @Suppress("UNCHECKED_CAST")
    val body = resp.body as List<Negotiation.ApiEntity>
    assertThat(body.size).isEqualTo(2)
    assertThat(body[0].id).isEqualTo(negId1)
    assertThat(body[0].stateId).isEqualTo(stateId1)
    assertThat(body[1].id).isEqualTo(negId2)
    assertThat(body[1].stateId).isEqualTo(stateId2)
  }

  @Test
  fun `can update supplier visibility of list of negotiations`() {
    val negId1 = 1L
    val negId2 = 2L
    val stateId1 = "stateId1"
    val stateId2 = "stateId2"
    val email = "backend@pactum.com"
    val token = "avalidtoken"

    val neg1 = Negotiation.ApiEntity(negId1, stateId1, "")
    val neg2 = Negotiation.ApiEntity(negId2, stateId2, "")
    val req = UpdateNegotiationReq(isVisibleSupplier = true, comment = "comment")
    val batchReq = mapOf(
      "$negId1" to req,
      "$negId2" to req
    )
    val batchActionReq = BatchActionReq(BatchActionType.UPDATE_SUPPLIER_VISIBILITY, batchReq)

    whenever(updateSupplierVisibilityService.updateSupplierVisibility(batchReq)).thenReturn(GenericOkResponse(listOf(neg1, neg2)))

    SessionHelper.setLoggedInUser(token, email, listOf(Role.Admin))

    val resp = negotiationBatchService.batchAction(batchActionReq, mapOf())
    assertThat(resp.statusCode).isEqualTo(HttpStatus.OK)
    @Suppress("UNCHECKED_CAST")
    val body = resp.body as List<Negotiation.ApiEntity>
    assertThat(body.size).isEqualTo(2)
    assertThat(body[0].id).isEqualTo(negId1)
    assertThat(body[0].stateId).isEqualTo(stateId1)
    assertThat(body[1].id).isEqualTo(negId2)
    assertThat(body[1].stateId).isEqualTo(stateId2)
  }

  @Test
  fun `can update fields of list of negotiations`() {
    val negId1 = 1L
    val negId2 = 2L
    val stateId1 = "stateId1"
    val stateId2 = "stateId2"
    val email = "backend@pactum.com"
    val token = "avalidtoken"

    val neg1 = Negotiation.ApiEntity(negId1, stateId1, "")
    val neg2 = Negotiation.ApiEntity(negId2, stateId2, "")

    val list = listOf(
      NegotiationField.ApiEntity(NegotiationFieldConfigType.MODEL, "attr1", "value1"),
      NegotiationField.ApiEntity(NegotiationFieldConfigType.OTHER, "attr2", "value2")
    )

    val req = UpdateNegotiationReq(fields = list, comment = "comment")
    val batchReq = mapOf(
      "$negId1" to req,
      "$negId2" to req
    )
    val batchActionReq = BatchActionReq(BatchActionType.UPDATE_FIELDS, batchReq)

    whenever(updateFieldsService.updateFields(batchReq)).thenReturn(GenericOkResponse(listOf(neg1, neg2)))

    SessionHelper.setLoggedInUser(token, email, listOf(Role.Admin))

    val resp = negotiationBatchService.batchAction(batchActionReq, mapOf())
    assertThat(resp.statusCode).isEqualTo(HttpStatus.OK)
    @Suppress("UNCHECKED_CAST")
    val body = resp.body as List<Negotiation.ApiEntity>
    assertThat(body.size).isEqualTo(2)
    assertThat(body[0].id).isEqualTo(negId1)
    assertThat(body[0].stateId).isEqualTo(stateId1)
    assertThat(body[1].id).isEqualTo(negId2)
    assertThat(body[1].stateId).isEqualTo(stateId2)
  }
}
