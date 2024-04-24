package com.pactum.model

import com.nhaarman.mockitokotlin2.whenever
import com.pactum.chat.model.ChatApiInput
import com.pactum.negotiation.NegotiationRepository
import com.pactum.negotiation.model.Negotiation
import com.pactum.negotiationstate.ChatStateNotAvailableException
import com.pactum.token.TokenService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.ComponentScan.Filter
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(ModelController::class, excludeFilters = [Filter(EnableWebSecurity::class)])
class ModelControllerTest {

  @Autowired
  lateinit var mockMvc: MockMvc

  @MockBean
  lateinit var modelService: ModelService

  @MockBean
  lateinit var negotiationRepository: NegotiationRepository

  @MockBean
  lateinit var tokenService: TokenService

  @Test
  @WithMockUser
  fun `can get chat params`() {
    val modelId = "456"
    val modelKey = "789"
    val stateId = "999"
    val map = HashMap<String, Any>().apply {
      put("name", "customer name")
      put("botAvatar", "somebase64image")
    }

    whenever(
      modelService.getChatParams(
        ChatApiInput(modelId = modelId, modelKey = modelKey, stateId = stateId)
      )
    ).thenReturn(map)

    mockMvc.perform(get("/api/v1/models/$modelId/modelKey/$modelKey/states/$stateId/chatParams"))
      .andExpect(status().isOk)
      .andExpect(jsonPath("$.name").value("customer name"))
      .andExpect(jsonPath("$.botAvatar").value("somebase64image"))
  }

  @Test
  @WithMockUser
  fun `can not get chat params if blacklisted`() {
    val modelId = "456"
    val modelKey = "789"
    val stateId = "999"

    whenever(negotiationRepository.findFirstByStateIdAndIsDeletedIsFalseOrderByCreateTimeDesc(stateId)).thenReturn(
      Negotiation.empty(1).copy(
        isVisibleSupplier = false
      )
    )
    whenever(
      modelService.getChatParams(
        ChatApiInput(modelId = modelId, modelKey = modelKey, stateId = stateId)
      )
    ).thenThrow(ChatStateNotAvailableException())

    mockMvc.perform(get("/api/v1/models/$modelId/modelKey/$modelKey/states/$stateId/chatParams"))
      .andExpect(status().isBadRequest)
  }

  @Test
  @WithMockUser
  fun `can get chat params if blacklisted and is read-only`() {
    val modelId = "456"
    val modelKey = "789"
    val stateId = "999"
    val map = HashMap<String, Any>().apply {
      put("name", "customer name")
      put("botAvatar", "somebase64image")
    }

    whenever(negotiationRepository.findFirstByStateIdAndIsDeletedIsFalseOrderByCreateTimeDesc(stateId)).thenReturn(
      Negotiation.empty(1).copy(
        isVisibleSupplier = false
      )
    )
    whenever(
      modelService.getChatParams(
        ChatApiInput(modelId = modelId, modelKey = modelKey, stateId = stateId, readOnly = true)
      )
    ).thenReturn(map)

    mockMvc.perform(get("/api/v1/models/$modelId/modelKey/$modelKey/states/$stateId/chatParams?readOnly=true"))
      .andExpect(status().isOk)
      .andExpect(jsonPath("$.name").value("customer name"))
      .andExpect(jsonPath("$.botAvatar").value("somebase64image"))
  }

  @Test
  @WithMockUser
  fun `can get chat params by state id`() {
    val stateId = "999"
    val map = HashMap<String, Any>().apply {
      put("name", "customer name")
      put("botAvatar", "somebase64image")
    }

    whenever(
      modelService.getChatParamsByStateId(
        ChatApiInput(stateId = stateId, shouldCreateNegotiationIfNotFound = false)
      )
    ).thenReturn(map)

    mockMvc.perform(get("/api/v1/models/states/$stateId/chatParams"))
      .andExpect(status().isOk)
      .andExpect(jsonPath("$.name").value("customer name"))
      .andExpect(jsonPath("$.botAvatar").value("somebase64image"))
  }
}
