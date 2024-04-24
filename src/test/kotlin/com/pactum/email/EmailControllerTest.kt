package com.pactum.email

import com.nhaarman.mockitokotlin2.whenever
import com.pactum.token.TokenService
import com.pactum.email.model.Email
import com.pactum.api.GenericCreatedResponse
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(EmailController::class, excludeFilters = [Filter(EnableWebSecurity::class)])
class EmailControllerTest {

  @Autowired
  lateinit var mockMvc: MockMvc

  @MockBean
  lateinit var emailService: EmailService

  @MockBean
  lateinit var tokenService: TokenService

  @Test
  @WithMockUser
  fun `can POST an email`() {

    val emailAddress = "test@email.com"
    val email = Email(emailAddress, "subscribed")
    val expectedResult = "{\"email\":\"test@email.com\",\"status\":\"subscribed\"}"

    whenever(emailService.addEmail(emailAddress)).thenReturn(GenericCreatedResponse(email))

    mockMvc.perform(
      post("/api/v1/emails")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .characterEncoding("utf-8")
        .content(emailAddress)
    )
      .andExpect(status().isCreated)
      .andExpect(content().json(expectedResult))
  }
}
