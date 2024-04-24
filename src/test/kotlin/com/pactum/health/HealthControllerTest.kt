package com.pactum.health

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nhaarman.mockitokotlin2.whenever
import com.pactum.api.GenericOkResponse
import com.pactum.token.TokenService
import com.pactum.health.model.Health
import com.pactum.health.model.HealthStatus
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(HealthController::class, excludeFilters = [Filter(EnableWebSecurity::class)])
class HealthControllerTest {

  @Autowired
  lateinit var mockMvc: MockMvc

  @MockBean
  lateinit var healthService: HealthService

  @MockBean
  lateinit var tokenService: TokenService

  @Test
  @WithMockUser
  fun `can get system health`() {

    val health = Health(HealthStatus.UP)

    whenever(healthService.getHealth()).thenReturn(health)

    mockMvc.perform(get("/api/v1/health/get"))
      .andExpect(status().isOk)
      .andExpect(jsonPath("$.status").value("UP"))
  }

  @Test
  @WithMockUser
  fun `can set health system`() {

    val req = Health(HealthStatus.DOWN)

    whenever(healthService.setHealth(req)).thenReturn(GenericOkResponse(req))

    mockMvc.perform(
      post("/api/v1/health/")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jacksonObjectMapper().writeValueAsString(req))
        .characterEncoding("utf-8")
        .with(SecurityMockMvcRequestPostProcessors.csrf())
    )
      .andExpect(status().isOk)
      .andExpect(jsonPath("$.status").value("DOWN"))
  }
}
