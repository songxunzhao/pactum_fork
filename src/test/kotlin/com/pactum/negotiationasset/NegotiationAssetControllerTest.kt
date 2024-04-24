package com.pactum.negotiationasset

import com.nhaarman.mockitokotlin2.whenever
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest(NegotiationAssetController::class, excludeFilters = [Filter(EnableWebSecurity::class)])
class NegotiationAssetControllerTest {

  @Autowired
  lateinit var mockMvc: MockMvc

  @MockBean
  lateinit var negotiationAssetService: NegotiationAssetService

  @MockBean
  lateinit var tokenService: TokenService

  @Test
  @WithMockUser
  fun `can fetch a mindmup file`() {
    val flowId = "flowId"

    val mockValue = """
      {"foobar": 123}
    """
    whenever(negotiationAssetService.getChatFlow(flowId)).thenReturn(mockValue.trimIndent())

    mockMvc.perform(
      get("/api/v1/negotiationasset/flow/$flowId")
    )
      .andExpect(status().isOk)
      .andExpect(jsonPath(".foobar").value(123))
  }

  @Test
  @WithMockUser
  fun `can fetch a model file`() {
    val modelId = "modelId"

    val mockValue = """
      {"models": {
          "asd": {"foobar": 123}
        }
      }
    """
    whenever(negotiationAssetService.getChatModel(modelId)).thenReturn(mockValue.trimIndent())

    mockMvc.perform(
      get("/api/v1/negotiationasset/model/$modelId")
    )
      .andExpect(status().isOk)
      .andExpect(jsonPath(".models.asd.foobar").value(123))
  }
}
