package com.pactum.auth

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nhaarman.mockitokotlin2.whenever
import com.pactum.auth.model.LoginReq
import com.pactum.auth.model.LoginRes
import com.pactum.api.GenericOkResponse
import com.pactum.token.TokenService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.ComponentScan.Filter
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(AuthController::class, excludeFilters = [Filter(EnableWebSecurity::class)])
class AuthControllerTest {

  @Autowired
  lateinit var mockMvc: MockMvc

  @MockBean
  lateinit var authService: AuthService

  @MockBean
  lateinit var tokenService: TokenService

  @Test
  @WithMockUser
  fun `can login with valid jwt token`() {
    val tokenId = "avalidgoogletoken"
    val token = "avalidtoken"

    val googleReq = LoginReq(tokenId)
    val googleRes = LoginRes(token)

    whenever(authService.login(googleReq)).thenReturn(GenericOkResponse(googleRes))

    mockMvc.perform(
      post("/api/v1/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jacksonObjectMapper().writeValueAsString(googleReq))
        .characterEncoding("utf-8")
        .with(csrf())
    )
      .andExpect(status().isOk)
      .andExpect(jsonPath("accessToken").value(token))
  }

  @Test
  @WithMockUser
  fun `can logout`() {

    val logoutRes = LoginRes("N/A")
    whenever(authService.logout()).thenReturn(logoutRes)

    mockMvc.perform(get("/api/v1/auth/logout").header("Authorization", "asda"))
      .andExpect(status().isOk)
      .andExpect(jsonPath("accessToken").value("N/A"))
  }
}
