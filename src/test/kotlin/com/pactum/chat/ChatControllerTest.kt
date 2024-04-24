package com.pactum.chat

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nhaarman.mockitokotlin2.whenever
import com.pactum.token.TokenService
import com.pactum.chat.model.TextStep
import com.pactum.chat.model.ChatApiInput
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.ComponentScan.Filter
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.isEqualTo

@WebMvcTest(ChatController::class, excludeFilters = [Filter(EnableWebSecurity::class)])
class ChatControllerTest {

  @Autowired
  lateinit var mockMvc: MockMvc

  @MockBean
  lateinit var chatService: ChatService

  private val secretStateId = "secret"

  @MockBean
  lateinit var tokenService: TokenService

  @Test
  @WithMockUser
  fun `can GET a chat step`() {
    val flowId = "flowId"
    val modelId = "modelId"
    val modelKey = "modelKey"
    val stateId = "stateId"
    val stepId = "stepId"
    val sessionId = "1234"
    val isReadOnly = false

    val step = TextStep(id = stepId, message = "someMessage")

    whenever(
      chatService.getChatStep(
        ChatApiInput(
          flowId = flowId,
          modelId = modelId,
          modelKey = modelKey,
          stateId = ChatApiInput.createNewStateId(stateId, sessionId, secretStateId),
          stepId = stepId,
          readOnly = isReadOnly
        )
      )
    ).thenReturn(
      listOf(step)
    )

    mockMvc.perform(
      get(
        "/api/v1/chats/$flowId/models/$modelId/modelKey/$modelKey/states/$stateId/nextStep?stepId=$stepId" +
      "&readOnly=$isReadOnly"
      ).header("sessionId", sessionId)
    )
      .andExpect(status().isOk)
      .andExpect(jsonPath("[0].id").value(step.id))
      .andExpect(jsonPath("[0].message").value(step.message))
  }

  @Test
  @WithMockUser
  fun `can UPDATE a chat step with value`() {
    val flowId = "flowId"
    val modelId = "modelId"
    val modelKey = "modelKey"
    val stateId = "stateId"
    val stepId = "stepId"
    val value = "abc"
    val sessionId = "1234"
    val isReadOnly = false
    val step = TextStep(id = stepId, message = "someMessage")

    whenever(
      chatService.getChatStep(
        ChatApiInput(
          flowId = flowId,
          modelId = modelId,
          modelKey = modelKey,
          stateId = ChatApiInput.createNewStateId(stateId, sessionId, secretStateId),
          stepId = stepId,
          value = value,
          readOnly = isReadOnly
        )
      )
    ).thenReturn(
      listOf(step)
    )

    mockMvc.perform(
      get(
        "/api/v1/chats/$flowId/models/$modelId/modelKey/$modelKey/states/$stateId/nextStep" +
      "?stepId=$stepId&value=$value&readOnly=$isReadOnly"
      ).header("sessionId", sessionId)
    )
      .andExpect(status().isOk)
      .andExpect(jsonPath("[0].id").value(step.id))
      .andExpect(jsonPath("[0].message").value(step.message))
  }

  @Test
  @WithMockUser
  fun `can UPDATE a chat step with values list`() {
    val flowId = "flowId"
    val modelId = "modelId"
    val modelKey = "modelKey"
    val stateId = "stateId"
    val stepId = "stepId"
    val sessionId = "1234"
    val values = listOf("abc", "bcd")
    val step = TextStep(id = stepId, message = "someMessage")
    val valueJson = jacksonObjectMapper().writeValueAsString(values)
    val isReadOnly = false
    whenever(
      chatService.getChatStep(
        ChatApiInput(
          flowId = flowId,
          modelId = modelId,
          modelKey = modelKey,
          stateId = ChatApiInput.createNewStateId(stateId, sessionId, secretStateId),
          stepId = stepId,
          value = valueJson,
          readOnly = isReadOnly
        )
      )
    ).thenReturn(
      listOf(step)
    )

    mockMvc.perform(
      get(
        "/api/v1/chats/$flowId/models/$modelId/modelKey/$modelKey/states/$stateId/nextStep" +
      "?stepId=$stepId&value=$valueJson&readOnly=$isReadOnly"
      ).header("sessionId", sessionId)

    )
      .andExpect(status().isOk)
      .andExpect(jsonPath("[0].id").value(step.id))
      .andExpect(jsonPath("[0].message").value(step.message))
  }

  @Test
  @WithMockUser
  fun `can GET a URL for signing the contract`() {
    val stateId = "state1"

    whenever(chatService.getContractSigningUrl(stateId)).thenReturn("https://www.google.com")

    mockMvc.perform(get("/api/v1/chats/states/$stateId/contract-signing-url"))
      .andExpect(status().isEqualTo(302))
  }

  @Test
  @WithMockUser
  fun `can GET a contract HTML`() {
    val stateId = "state1"

    whenever(chatService.getDemoContractHTML(stateId)).thenReturn("<html></html>")

    mockMvc.perform(get("/api/v1/chats/states/$stateId/contract-download"))
      .andExpect(content().string("<html></html>"))
      .andExpect(status().isOk)
  }

  @Test
  @WithMockUser
  fun `can GET a chat step by state id`() {
    val stateId = "stateId"
    val stepId = "stepId"
    val sessionId = "1234"
    val isReadOnly = false

    val step = TextStep(id = stepId, message = "someMessage")

    whenever(
      chatService.getChatStepByStateId(
        ChatApiInput(
          stateId = ChatApiInput.createNewStateId(stateId, sessionId, secretStateId),
          stepId = stepId,
          readOnly = isReadOnly,
          shouldCreateNegotiationIfNotFound = false
        )
      )
    ).thenReturn(
      listOf(step)
    )

    mockMvc.perform(
      get("/api/v1/chats/states/$stateId/nextStep?stepId=$stepId&readOnly=$isReadOnly")
        .header("sessionId", sessionId)
    )
      .andExpect(status().isOk)
      .andExpect(jsonPath("[0].id").value(step.id))
      .andExpect(jsonPath("[0].message").value(step.message))
  }
}
