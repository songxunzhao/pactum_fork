package com.pactum.negotiation.summary

import com.nhaarman.mockitokotlin2.whenever
import com.pactum.negotiation.summary.model.NegotiationsSummary
import com.pactum.negotiation.summary.model.ExtraValue
import com.pactum.negotiation.summary.model.ExtraValueFormat
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

@WebMvcTest(NegotiationSummaryController::class, excludeFilters = [Filter(EnableWebSecurity::class)])
class NegotiationSummaryControllerTest {

  @Autowired
  lateinit var mockMvc: MockMvc

  @MockBean
  lateinit var negotiationSummaryService: NegotiationSummaryService

  @MockBean
  lateinit var tokenService: TokenService

  @Test
  @WithMockUser
  fun `can get negotiations summary for client`() {
    val total = 10
    val opened = 5
    val finished = 3
    val extra = ExtraValue("amount", "24,340.00", ExtraValueFormat.CURRENCY)
    val summary = NegotiationsSummary(total, opened, finished, listOf(extra))

    whenever(negotiationSummaryService.getNegotiationsSummaryForClient()).thenReturn(summary)

    mockMvc.perform(get("/api/v1/negotiation/summary/client"))
      .andExpect(status().isOk)
      .andExpect(jsonPath("$.totalCount").value(total))
      .andExpect(jsonPath("$.extra[0].label").value("amount"))
  }

  @Test
  @WithMockUser
  fun `can get negotiations summary of a client`() {
    val clientId = 1L
    val total = 10
    val opened = 5
    val finished = 3
    val extra = ExtraValue("amount", "24,340.00", ExtraValueFormat.CURRENCY)
    val summary = NegotiationsSummary(total, opened, finished, listOf(extra))

    whenever(negotiationSummaryService.getNegotiationsSummaryByClientId(clientId)).thenReturn(summary)

    mockMvc.perform(get("/api/v1/negotiation/summary/$clientId"))
      .andExpect(status().isOk)
      .andExpect(jsonPath("$.totalCount").value(total))
      .andExpect(jsonPath("$.extra[0].label").value("amount"))
  }
}
