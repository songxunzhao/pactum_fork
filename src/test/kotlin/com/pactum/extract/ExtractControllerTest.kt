package com.pactum.extract

import com.nhaarman.mockitokotlin2.whenever
import com.pactum.token.TokenService
import com.pactum.chat.model.NegotiationHistoryItem
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
import java.time.Instant

@WebMvcTest(ExtractController::class, excludeFilters = [Filter(EnableWebSecurity::class)])
class ExtractControllerTest {

  @Autowired
  lateinit var mockMvc: MockMvc

  @MockBean
  lateinit var extractService: ExtractService

  @MockBean
  lateinit var tokenService: TokenService

  @Test
  @WithMockUser
  fun `can get terms of a chat by flowId`() {
    val flowIds = "123"
    val map1 = mutableMapOf<String, Any>(
      "chat_id" to "123",
      "duration_min" to 0,
      "{current_ads}" to "pop-under"
    )
    val map2 = mutableMapOf<String, Any>(
      "chat_id" to "123",
      "end" to false
    )
    val list = listOf<Map<String, Any>>(
      map1,
      map2
    )

    whenever(extractService.getChatsTermsByFlowIds(flowIds)).thenReturn(list)

    mockMvc.perform(get("/api/v1/extract/terms/flowIds/$flowIds"))
      .andExpect(status().isOk)
      .andExpect(jsonPath("$[0].chat_id").value("123"))
      .andExpect(jsonPath("$[0].duration_min").value(0))
      .andExpect(jsonPath("$[0].{current_ads}").value("pop-under"))
      .andExpect(jsonPath("$[1].chat_id").value("123"))
      .andExpect(jsonPath("$[1].end").value(false))
  }

  @Test
  @WithMockUser
  fun `can get terms of multiple chats by flowIds`() {
    val flowIds = "123,456"
    val map1 = mutableMapOf<String, Any>(
      "chat_id" to "123",
      "duration_min" to 0,
      "{current_ads}" to "pop-under"
    )
    val map2 = mutableMapOf<String, Any>(
      "chat_id" to "456",
      "end" to false
    )
    val list = listOf<Map<String, Any>>(
      map1,
      map2
    )

    whenever(extractService.getChatsTermsByFlowIds(flowIds)).thenReturn(list)

    mockMvc.perform(get("/api/v1/extract/terms/flowIds/$flowIds"))
      .andExpect(status().isOk)
      .andExpect(jsonPath("$[0].chat_id").value("123"))
      .andExpect(jsonPath("$[0].duration_min").value(0))
      .andExpect(jsonPath("$[0].{current_ads}").value("pop-under"))
      .andExpect(jsonPath("$[1].chat_id").value("456"))
      .andExpect(jsonPath("$[1].end").value(false))
  }

  @Test
  @WithMockUser
  fun `can get terms of a chat by stateId`() {
    val stateIds = "123"
    val map1 = mutableMapOf<String, Any>(
      "chat_id" to "123",
      "duration_min" to 0,
      "{current_ads}" to "pop-under"
    )
    val map2 = mutableMapOf<String, Any>(
      "chat_id" to "123",
      "end" to false
    )
    val list = listOf<Map<String, Any>>(
      map1,
      map2
    )

    whenever(extractService.getChatsTermsByStateIds(stateIds)).thenReturn(list)

    mockMvc.perform(get("/api/v1/extract/terms/stateIds/$stateIds"))
      .andExpect(status().isOk)
      .andExpect(jsonPath("$[0].chat_id").value("123"))
      .andExpect(jsonPath("$[0].duration_min").value(0))
      .andExpect(jsonPath("$[0].{current_ads}").value("pop-under"))
      .andExpect(jsonPath("$[1].chat_id").value("123"))
      .andExpect(jsonPath("$[1].end").value(false))
  }

  @Test
  @WithMockUser
  fun `can get terms of multiple chats by stateIds`() {
    val stateIds = "123,456"
    val map1 = mutableMapOf<String, Any>(
      "chat_id" to "123",
      "duration_min" to 0,
      "{current_ads}" to "pop-under"
    )
    val map2 = mutableMapOf<String, Any>(
      "chat_id" to "456",
      "end" to false
    )
    val list = listOf<Map<String, Any>>(
      map1,
      map2
    )

    whenever(extractService.getChatsTermsByStateIds(stateIds)).thenReturn(list)

    mockMvc.perform(get("/api/v1/extract/terms/stateIds/$stateIds"))
      .andExpect(status().isOk)
      .andExpect(jsonPath("$[0].chat_id").value("123"))
      .andExpect(jsonPath("$[0].duration_min").value(0))
      .andExpect(jsonPath("$[0].{current_ads}").value("pop-under"))
      .andExpect(jsonPath("$[1].chat_id").value("456"))
      .andExpect(jsonPath("$[1].end").value(false))
  }

  @Test
  @WithMockUser
  fun `can get history by flowId`() {
    val flowId = "123"
    val historyItem = NegotiationHistoryItem(
      id = "10",
      flowId = "123",
      stateId = "1",
      time = Instant.parse("2020-02-18T11:17:49.763501Z"),
      timeSincePreviousSec = 13,
      message = "test message",
      user = true
    )

    whenever(extractService.getChatsHistoryByFlowIds(flowId)).thenReturn(listOf(historyItem))

    mockMvc.perform(get("/api/v1/extract/history/flowIds/$flowId"))
      .andExpect(status().isOk)
      .andExpect(jsonPath("$[0].id").value("10"))
      .andExpect(jsonPath("$[0].flowId").value(flowId))
      .andExpect(jsonPath("$[0].stateId").value("1"))
      .andExpect(jsonPath("$[0].time").value("2020-02-18T11:17:49.763501Z"))
      .andExpect(jsonPath("$[0].timeSincePreviousSec").value(13))
      .andExpect(jsonPath("$[0].message").value("test message"))
  }

  @Test
  @WithMockUser
  fun `can get history by multiple flowIds`() {
    val flowIds = "123,456"
    val historyItem1 = NegotiationHistoryItem(
      id = "10",
      flowId = "123",
      stateId = "1",
      time = Instant.parse("2020-02-18T11:17:49.763501Z"),
      timeSincePreviousSec = 13,
      message = "test message",
      user = true
    )
    val historyItem2 = NegotiationHistoryItem(
      id = "11",
      flowId = "456",
      stateId = "2",
      time = Instant.parse("2020-02-18T11:17:49.763501Z"),
      timeSincePreviousSec = 10,
      message = "test message",
      user = true
    )

    whenever(extractService.getChatsHistoryByFlowIds(flowIds)).thenReturn(
      listOf(
        historyItem1,
        historyItem2
      )
    )

    mockMvc.perform(get("/api/v1/extract/history/flowIds/$flowIds"))
      .andExpect(status().isOk)
      .andExpect(jsonPath("$[0].id").value("10"))
      .andExpect(jsonPath("$[0].flowId").value("123"))
      .andExpect(jsonPath("$[0].stateId").value("1"))
      .andExpect(jsonPath("$[0].time").value("2020-02-18T11:17:49.763501Z"))
      .andExpect(jsonPath("$[0].timeSincePreviousSec").value(13))
      .andExpect(jsonPath("$[0].message").value("test message"))
      .andExpect(jsonPath("$[1].id").value("11"))
      .andExpect(jsonPath("$[1].flowId").value("456"))
  }

  @Test
  @WithMockUser
  fun `can get history by multiple stateIds`() {
    val stateIds = "123,456"
    val historyItem1 = NegotiationHistoryItem(
      id = "10",
      flowId = "111",
      stateId = "123",
      time = Instant.parse("2020-02-18T11:17:49.763501Z"),
      timeSincePreviousSec = 13,
      message = "test message",
      user = true
    )
    val historyItem2 = NegotiationHistoryItem(
      id = "11",
      flowId = "222",
      stateId = "456",
      time = Instant.parse("2020-02-18T11:17:49.763501Z"),
      timeSincePreviousSec = 10,
      message = "test message",
      user = true
    )

    whenever(extractService.getChatsHistoryByStateIds(stateIds)).thenReturn(
      listOf(
        historyItem1,
        historyItem2
      )
    )

    mockMvc.perform(get("/api/v1/extract/history/stateIds/$stateIds"))
      .andExpect(status().isOk)
      .andExpect(jsonPath("$[0].id").value("10"))
      .andExpect(jsonPath("$[0].flowId").value("111"))
      .andExpect(jsonPath("$[0].stateId").value("123"))
      .andExpect(jsonPath("$[0].time").value("2020-02-18T11:17:49.763501Z"))
      .andExpect(jsonPath("$[0].timeSincePreviousSec").value(13))
      .andExpect(jsonPath("$[0].message").value("test message"))
      .andExpect(jsonPath("$[1].id").value("11"))
      .andExpect(jsonPath("$[1].flowId").value("222"))
      .andExpect(jsonPath("$[1].stateId").value("456"))
  }
}
