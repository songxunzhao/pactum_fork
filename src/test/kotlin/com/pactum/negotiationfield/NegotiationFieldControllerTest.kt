package com.pactum.negotiationfield

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nhaarman.mockitokotlin2.whenever
import com.pactum.audit.AuditEventService
import com.pactum.client.model.NegotiationFieldConfigType
import com.pactum.api.GenericOkResponse
import com.pactum.negotiationfield.model.NegotiationField
import com.pactum.test.TestConfiguration
import com.pactum.token.TokenService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.ComponentScan.Filter
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(NegotiationFieldController::class, excludeFilters = [Filter(EnableWebSecurity::class)])
@Import(TestConfiguration::class)
class NegotiationFieldControllerTest {

  @Autowired
  lateinit var mockMvc: MockMvc
  @MockBean
  lateinit var negotiationFieldService: NegotiationFieldService
  @MockBean
  lateinit var auditEventService: AuditEventService
  @MockBean
  lateinit var negotiationFieldRepository: NegotiationFieldRepository
  @MockBean
  lateinit var tokenService: TokenService

  @Test
  @WithMockUser
  fun `can get fetch negotiation fields`() {
    val negotiationId = 42L

    val list = listOf(
      NegotiationField.ApiEntity(NegotiationFieldConfigType.MODEL, "attr1", "value1"),
      NegotiationField.ApiEntity(NegotiationFieldConfigType.OTHER, "attr2", "value2")
    )

    whenever(negotiationFieldService.getFieldsApiEntitiesForNegotiation(negotiationId)).thenReturn(list)

    mockMvc.perform(get("/api/v1/negotiation/$negotiationId/fields"))
      .andExpect(status().isOk)
      .andExpect(jsonPath("$[0].type").value(NegotiationFieldConfigType.MODEL.name))
      .andExpect(jsonPath("$[0].attribute").value("attr1"))
      .andExpect(jsonPath("$[0].value").value("value1"))
      .andExpect(jsonPath("$[1].type").value(NegotiationFieldConfigType.OTHER.name))
      .andExpect(jsonPath("$[1].attribute").value("attr2"))
      .andExpect(jsonPath("$[1].value").value("value2"))
  }

  @Test
  @WithMockUser
  fun `can update new negotiations if admin`() {

    val negotiationId = 42L

    val list = listOf(
      NegotiationField.ApiEntity(NegotiationFieldConfigType.MODEL, "attr1", "value1"),
      NegotiationField.ApiEntity(NegotiationFieldConfigType.OTHER, "attr2", "value2")
    )
    val result = listOf(
      NegotiationField.ApiEntity(NegotiationFieldConfigType.MODEL, "attr1", "value1"),
      NegotiationField.ApiEntity(NegotiationFieldConfigType.OTHER, "attr2", "value2")
    )

    whenever(negotiationFieldService.setFieldsForNegotiation(negotiationId, list)).thenReturn(GenericOkResponse(result))

    mockMvc.perform(
      post("/api/v1/negotiation/$negotiationId/set-fields")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jacksonObjectMapper().writeValueAsString(list))
        .characterEncoding("utf-8")
        .with(SecurityMockMvcRequestPostProcessors.csrf())
    )
      .andExpect(status().isOk)
      .andExpect(jsonPath("$[0].type").value(NegotiationFieldConfigType.MODEL.name))
      .andExpect(jsonPath("$[0].attribute").value("attr1"))
      .andExpect(jsonPath("$[0].value").value("value1"))
      .andExpect(jsonPath("$[1].type").value(NegotiationFieldConfigType.OTHER.name))
      .andExpect(jsonPath("$[1].attribute").value("attr2"))
      .andExpect(jsonPath("$[1].value").value("value2"))
  }
}
