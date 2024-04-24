package com.pactum.negotiation.batch

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.whenever
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.pactum.api.GenericOkResponse
import com.pactum.client.model.NegotiationFieldConfigType
import com.pactum.negotiation.batch.action.CreateUpdateBatchService
import com.pactum.negotiation.batch.model.BatchActionReq
import com.pactum.negotiation.batch.model.BatchActionType
import com.pactum.negotiation.batch.model.CreateBatchOfNegotiationsReq
import com.pactum.negotiation.batch.model.CreateNegotiationItem
import com.pactum.negotiation.model.Negotiation
import com.pactum.negotiation.model.NegotiationCreatedUpdatedPubSubMessage
import com.pactum.negotiation.model.ReloadNegotiationModelReq
import com.pactum.negotiation.model.UpdateNegotiationReq
import com.pactum.negotiationfield.model.NegotiationField
import com.pactum.token.TokenService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.ComponentScan.Filter
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(NegotiationBatchController::class, excludeFilters = [Filter(EnableWebSecurity::class)])
class NegotiationBatchControllerTest {

  @Autowired
  lateinit var mockMvc: MockMvc

  @MockBean
  lateinit var negotiationBatchService: NegotiationBatchService

  @MockBean
  lateinit var tokenService: TokenService

  @Test
  @WithMockUser
  fun `can create new batch of negotiations`() {

    val clientTag = "tag"
    val flowId = "abcd"
    val modelId = "qwrr"
    val modelKey = "zxcv"
    val status = "newstatus"
    val attributes = mapOf(
      "vendorId" to "1",
      "attr1" to true,
      "attr2" to 2.5
    )
    val items = listOf(CreateNegotiationItem(flowId, modelId, modelKey, attributes, status))
    val req = CreateBatchOfNegotiationsReq(clientTag, null, items)
    val batchActionReq = BatchActionReq(BatchActionType.CREATE_UPDATE_BATCH, req)

    val result = CreateUpdateBatchService.BatchOfNegotiationsHolder(
      created = listOf(
        NegotiationCreatedUpdatedPubSubMessage(
          "1",
          "stateId",
          "",
          "",
          clientTag,
          status
        )
      ),
      emptyList()
    )
    reset(negotiationBatchService)

    whenever(negotiationBatchService.batchAction(eq(batchActionReq), any())).thenReturn(GenericOkResponse(result))

    mockMvc.perform(
      post("/api/v1/negotiation/batch-action")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jacksonObjectMapper().writeValueAsString(batchActionReq))
        .characterEncoding("utf-8")
        .with(SecurityMockMvcRequestPostProcessors.csrf())
    )
      .andExpect(status().isOk)
      .andExpect(jsonPath("$.created[0].vendorId").value("1"))
      .andExpect(jsonPath("$.created[0].stateId").value("stateId"))
      .andExpect(jsonPath("$.created[0].clientTag").value(clientTag))
      .andExpect(jsonPath("$.created[0].status").value("newstatus"))
      .andExpect(jsonPath("$.updated").isEmpty)
  }

  @Test
  @WithMockUser
  fun `can create new batch of negotiations without optional parameters`() {

    val clientTag = "tag"
    val flowId = "abcd"
    val modelId = "qwrr"
    val modelKey = "zxcv"

    val items = listOf(CreateNegotiationItem(flowId, modelId, modelKey, null, null))
    val req = CreateBatchOfNegotiationsReq(clientTag, null, items)
    val batchActionReq = BatchActionReq(BatchActionType.CREATE_UPDATE_BATCH, req)
    val result = CreateUpdateBatchService.BatchOfNegotiationsHolder(
      created = listOf(
        NegotiationCreatedUpdatedPubSubMessage(
          "1",
          "stateId",
          "",
          "",
          clientTag,
          null
        )
      ),
      emptyList()
    )
    reset(negotiationBatchService)

    whenever(negotiationBatchService.batchAction(eq(batchActionReq), any())).thenReturn(GenericOkResponse(result))

    mockMvc.perform(
      post("/api/v1/negotiation/batch-action")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jacksonObjectMapper().writeValueAsString(batchActionReq))
        .characterEncoding("utf-8")
        .with(SecurityMockMvcRequestPostProcessors.csrf())
    )
      .andExpect(status().isOk)
      .andExpect(jsonPath("$.created[0].vendorId").value("1"))
      .andExpect(jsonPath("$.created[0].stateId").value("stateId"))
      .andExpect(jsonPath("$.created[0].clientTag").value(clientTag))
      .andExpect(jsonPath("$.updated").isEmpty)
  }

  @Test
  @WithMockUser
  fun `can reload a negotiation model if admin`() {

    val negotiationId = 123L

    val req = ReloadNegotiationModelReq(listOf(negotiationId))
    val reloaded = Negotiation.ApiEntity(1, "stateId", "https://www.pactum.com/chatUrl1")

    val batchActionReq = BatchActionReq(BatchActionType.RELOAD_MODEL, req)
    whenever(negotiationBatchService.batchAction(eq(batchActionReq), any())).thenReturn(GenericOkResponse(listOf(reloaded)))

    mockMvc.perform(
      post("/api/v1/negotiation/batch-action")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jacksonObjectMapper().writeValueAsString(batchActionReq))
        .characterEncoding("utf-8")
        .with(SecurityMockMvcRequestPostProcessors.csrf())
    )
      .andExpect(status().isOk)
      .andExpect(jsonPath("$[0].id").value(1))
      .andExpect(jsonPath("$[0].stateId").value("stateId"))
      .andExpect(jsonPath("$[0].chatUrl").value("https://www.pactum.com/chatUrl1"))
  }

  @Test
  @WithMockUser
  fun `can update status of list of negotiations`() {

    val negId1 = 1L
    val negId2 = 2L
    val status = "ONGOING"
    val comment = "updated to Ongoing"
    val req = UpdateNegotiationReq(
      status = status,
      comment = comment
    )
    val batchReq = mapOf(
      "$negId1" to req,
      "$negId2" to req
    )
    val batchActionReq = BatchActionReq(BatchActionType.UPDATE_STATUS, batchReq)
    val updated1 = Negotiation.ApiEntity(negId1, "stateId1", "https://www.pactum.com/chatUrl1")
    val updated2 = Negotiation.ApiEntity(negId2, "stateId2", "https://www.pactum.com/chatUrl2")
    val result = listOf(updated1, updated2)

    reset(negotiationBatchService)

    whenever(negotiationBatchService.batchAction(eq(batchActionReq), any())).thenReturn(GenericOkResponse(result))

    mockMvc.perform(
      post("/api/v1/negotiation/batch-action")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jacksonObjectMapper().writeValueAsString(batchActionReq))
        .characterEncoding("utf-8")
        .with(SecurityMockMvcRequestPostProcessors.csrf())
    )
      .andExpect(status().isOk)
      .andExpect(jsonPath("$[0].id").value(negId1))
      .andExpect(jsonPath("$[0].stateId").value("stateId1"))
      .andExpect(jsonPath("$[0].chatUrl").value("https://www.pactum.com/chatUrl1"))
      .andExpect(jsonPath("$[1].id").value(negId2))
      .andExpect(jsonPath("$[1].stateId").value("stateId2"))
      .andExpect(jsonPath("$[1].chatUrl").value("https://www.pactum.com/chatUrl2"))
  }

  @Test
  @WithMockUser
  fun `can update client visibility of list of negotiations`() {

    val negId1 = 1L
    val negId2 = 2L
    val comment = "updated isVisibleClient = true"
    val req = UpdateNegotiationReq(
      isVisibleClient = true,
      comment = comment
    )
    val batchReq = mapOf(
      "$negId1" to req,
      "$negId2" to req
    )
    val batchActionReq = BatchActionReq(BatchActionType.UPDATE_CLIENT_VISIBILITY, batchReq)
    val updated1 = Negotiation.ApiEntity(negId1, "stateId1", "https://www.pactum.com/chatUrl1")
    val updated2 = Negotiation.ApiEntity(negId2, "stateId2", "https://www.pactum.com/chatUrl2")
    val result = listOf(updated1, updated2)

    reset(negotiationBatchService)

    whenever(negotiationBatchService.batchAction(eq(batchActionReq), any())).thenReturn(GenericOkResponse(result))

    mockMvc.perform(
      post("/api/v1/negotiation/batch-action")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jacksonObjectMapper().writeValueAsString(batchActionReq))
        .characterEncoding("utf-8")
        .with(SecurityMockMvcRequestPostProcessors.csrf())
    )
      .andExpect(status().isOk)
      .andExpect(jsonPath("$[0].id").value(negId1))
      .andExpect(jsonPath("$[0].stateId").value("stateId1"))
      .andExpect(jsonPath("$[0].chatUrl").value("https://www.pactum.com/chatUrl1"))
      .andExpect(jsonPath("$[1].id").value(negId2))
      .andExpect(jsonPath("$[1].stateId").value("stateId2"))
      .andExpect(jsonPath("$[1].chatUrl").value("https://www.pactum.com/chatUrl2"))
  }

  @Test
  @WithMockUser
  fun `can update supplier visibility of list of negotiations`() {

    val negId1 = 1L
    val negId2 = 2L
    val comment = "updated isVisibleSupplier = true"
    val req = UpdateNegotiationReq(
      isVisibleSupplier = true,
      comment = comment
    )
    val batchReq = mapOf(
      "$negId1" to req,
      "$negId2" to req
    )
    val batchActionReq = BatchActionReq(BatchActionType.UPDATE_SUPPLIER_VISIBILITY, batchReq)
    val updated1 = Negotiation.ApiEntity(negId1, "stateId1", "https://www.pactum.com/chatUrl1")
    val updated2 = Negotiation.ApiEntity(negId2, "stateId2", "https://www.pactum.com/chatUrl2")
    val result = listOf(updated1, updated2)

    reset(negotiationBatchService)

    whenever(negotiationBatchService.batchAction(eq(batchActionReq), any())).thenReturn(GenericOkResponse(result))

    mockMvc.perform(
      post("/api/v1/negotiation/batch-action")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jacksonObjectMapper().writeValueAsString(batchActionReq))
        .characterEncoding("utf-8")
        .with(SecurityMockMvcRequestPostProcessors.csrf())
    )
      .andExpect(status().isOk)
      .andExpect(jsonPath("$[0].id").value(negId1))
      .andExpect(jsonPath("$[0].stateId").value("stateId1"))
      .andExpect(jsonPath("$[0].chatUrl").value("https://www.pactum.com/chatUrl1"))
      .andExpect(jsonPath("$[1].id").value(negId2))
      .andExpect(jsonPath("$[1].stateId").value("stateId2"))
      .andExpect(jsonPath("$[1].chatUrl").value("https://www.pactum.com/chatUrl2"))
  }

  @Test
  @WithMockUser
  fun `can update fields of list of negotiations`() {

    val negId1 = 1L
    val negId2 = 2L
    val comment = "updated fields"
    val list = listOf(
      NegotiationField.ApiEntity(NegotiationFieldConfigType.MODEL, "attr1", "value1"),
      NegotiationField.ApiEntity(NegotiationFieldConfigType.OTHER, "attr2", "value2")
    )
    val req = UpdateNegotiationReq(
      fields = list,
      comment = comment
    )
    val batchReq = mapOf(
      "$negId1" to req,
      "$negId2" to req
    )
    val batchActionReq = BatchActionReq(BatchActionType.UPDATE_FIELDS, batchReq)
    val updated1 = Negotiation.ApiEntity(negId1, "stateId1", "https://www.pactum.com/chatUrl1")
    val updated2 = Negotiation.ApiEntity(negId2, "stateId2", "https://www.pactum.com/chatUrl2")
    val result = listOf(updated1, updated2)

    reset(negotiationBatchService)

    whenever(negotiationBatchService.batchAction(eq(batchActionReq), any())).thenReturn(GenericOkResponse(result))

    mockMvc.perform(
      post("/api/v1/negotiation/batch-action")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jacksonObjectMapper().writeValueAsString(batchActionReq))
        .characterEncoding("utf-8")
        .with(SecurityMockMvcRequestPostProcessors.csrf())
    )
      .andExpect(status().isOk)
      .andExpect(jsonPath("$[0].id").value(negId1))
      .andExpect(jsonPath("$[0].stateId").value("stateId1"))
      .andExpect(jsonPath("$[0].chatUrl").value("https://www.pactum.com/chatUrl1"))
      .andExpect(jsonPath("$[1].id").value(negId2))
      .andExpect(jsonPath("$[1].stateId").value("stateId2"))
      .andExpect(jsonPath("$[1].chatUrl").value("https://www.pactum.com/chatUrl2"))
  }
}
