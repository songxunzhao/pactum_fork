package com.pactum.negotiation

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nhaarman.mockitokotlin2.whenever
import com.pactum.api.GenericCreatedResponse
import com.pactum.api.GenericNoContentResponse
import com.pactum.api.GenericOkResponse
import com.pactum.negotiation.model.CreateNegotiationReq
import com.pactum.negotiation.model.Negotiation
import com.pactum.negotiation.model.UpdateNegotiationReq
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant

@WebMvcTest(NegotiationController::class, excludeFilters = [Filter(EnableWebSecurity::class)])
class NegotiationControllerTest {

  @Autowired
  lateinit var mockMvc: MockMvc

  @MockBean
  lateinit var negotiationService: NegotiationService

  @MockBean
  lateinit var tokenService: TokenService

  @Test
  @WithMockUser
  fun `can create new negotiations if admin`() {

    val clientId = 0L
    val flowId = "abcd"
    val modelId = "qwrr"
    val modelKey = "zxcv"

    val req = CreateNegotiationReq(clientId, flowId, modelId, listOf(modelKey))
    val created = Negotiation(
      id = 1,
      clientId = clientId,
      flowId = flowId,
      modelId = modelId,
      modelKey = modelKey,
      stateId = "stateId",
      createTime = Instant.now()
    )

    whenever(negotiationService.createNegotiations(req)).thenReturn(GenericCreatedResponse(listOf(created)))

    mockMvc.perform(
      post("/api/v1/negotiation")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jacksonObjectMapper().writeValueAsString(req))
        .characterEncoding("utf-8")
        .with(SecurityMockMvcRequestPostProcessors.csrf())
    )
      .andExpect(status().isCreated)
      .andExpect(jsonPath("$[0].id").value(1))
      .andExpect(jsonPath("$[0].clientId").value(clientId))
      .andExpect(jsonPath("$[0].flowId").value(flowId))
      .andExpect(jsonPath("$[0].modelId").value(modelId))
      .andExpect(jsonPath("$[0].modelKey").value(modelKey))
      .andExpect(jsonPath("$[0].stateId").value("stateId"))
  }

  @Test
  @WithMockUser
  fun `can get list of negotiations`() {
    val stateId = "abcd"
    val tag = "tag"
    val name = "name"
    val status = "CREATED"
    val link = "https://www.pactum.com"
    val detail = mapOf(
      "id" to 0,
      "stateId" to stateId,
      "tag" to tag,
      "name" to name,
      "status" to status,
      "link" to link
    )
    val list = listOf(detail)

    whenever(negotiationService.getNegotiationsForClient()).thenReturn(list)

    mockMvc.perform(get("/api/v1/negotiation/client"))
      .andExpect(status().isOk)
      .andExpect(jsonPath("$[0].name").value(name))
      .andExpect(jsonPath("$[0].tag").value(tag))
  }

  @Test
  @WithMockUser
  fun `can get list of negotiations of a client`() {
    val clientId = 0L
    val stateId = "abcd"
    val tag = "tag"
    val name = "name"
    val status = "CREATED"
    val link = "https://www.pactum.com"
    val detail = mapOf(
      "id" to 0,
      "stateId" to stateId,
      "tag" to tag,
      "name" to name,
      "status" to status,
      "link" to link
    )
    val list = listOf(detail)

    whenever(negotiationService.getNegotiationsByClientId(clientId)).thenReturn(list)

    mockMvc.perform(get("/api/v1/negotiation/$clientId"))
      .andExpect(status().isOk)
      .andExpect(jsonPath("$[0].name").value(name))
      .andExpect(jsonPath("$[0].tag").value(tag))
  }

  @Test
  @WithMockUser
  fun `can delete a negotiation by id`() {
    val id = 0L

    whenever(negotiationService.deleteNegotiation(id)).thenReturn(GenericNoContentResponse())

    mockMvc.perform(
      delete("/api/v1/negotiation/$id")
        .with(SecurityMockMvcRequestPostProcessors.csrf())
    )
      .andExpect(status().isNoContent)
  }

  @Test
  @WithMockUser
  fun `can update new negotiations if admin`() {

    val negId = 1L
    val status = "ONGOING"
    val isVisibleSupplier = false
    val isVisibleClient = true
    val req = UpdateNegotiationReq(status = status, isVisibleSupplier = isVisibleSupplier, isVisibleClient = isVisibleClient)
    val updated = Negotiation.ApiEntity(negId, "stateId", "chatUrl")

    whenever(negotiationService.updateNegotiation(negId, req)).thenReturn(GenericOkResponse(updated))

    mockMvc.perform(
      put("/api/v1/negotiation/$negId")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jacksonObjectMapper().writeValueAsString(req))
        .characterEncoding("utf-8")
        .with(SecurityMockMvcRequestPostProcessors.csrf())
    )
      .andExpect(status().isOk)
      .andExpect(jsonPath("$.id").value(negId))
      .andExpect(jsonPath("$.stateId").value("stateId"))
      .andExpect(jsonPath("$.chatUrl").value("chatUrl"))
  }
}
