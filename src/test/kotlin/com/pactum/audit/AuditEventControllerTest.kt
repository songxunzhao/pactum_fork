package com.pactum.audit

import com.nhaarman.mockitokotlin2.whenever
import com.pactum.api.GenericNotFoundException
import com.pactum.audit.model.AuditEvent
import com.pactum.audit.model.AuditEventListItem
import com.pactum.audit.model.TargetEntity
import com.pactum.test.TestClockHolder
import com.pactum.token.TokenService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.ComponentScan.Filter
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(AuditEventController::class, excludeFilters = [Filter(EnableWebSecurity::class)])
class AuditEventControllerTest {

  private val clock = TestClockHolder.CLOCK

  @Autowired
  lateinit var mockMvc: MockMvc

  @MockBean
  lateinit var auditEventService: AuditEventService

  @MockBean
  lateinit var tokenService: TokenService

  @Test
  @WithMockUser
  fun `can fetch an audit event list`() {
    val username = "username"
    val description = "description"
    val type = "type"
    val targetEntity = TargetEntity("entityType", "entityId")
    val event = AuditEventListItem(
      42,
      clock.instant(),
      type,
      description,
      targetEntity,
      username
    )
    val list = listOf(event)

    whenever(auditEventService.getAuditEvents()).thenReturn(list)

    mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/audit/event"))
      .andExpect(status().isOk)
      .andExpect(jsonPath("$[0].id").value(42))
      .andExpect(jsonPath("$[0].username").value(username))
      .andExpect(jsonPath("$[0].description").value(description))
      .andExpect(jsonPath("$[0].type").value(type))
      .andExpect(jsonPath("$[0].time").value(clock.instant().toString()))
      .andExpect(jsonPath("$[0].targetEntity.type").value(targetEntity.type))
      .andExpect(jsonPath("$[0].targetEntity.pk").value(targetEntity.pk))
  }

  @Test
  @WithMockUser
  fun `can fetch an audit event that has all possible fields null`() {
    val type = "type"
    val event = AuditEventListItem(time = clock.instant(), type = type)
    val list = listOf(event)

    whenever(auditEventService.getAuditEvents()).thenReturn(list)

    mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/audit/event"))
      .andExpect(status().isOk)
      .andExpect(jsonPath("$[0].id").isEmpty)
      .andExpect(jsonPath("$[0].username").isEmpty)
      .andExpect(jsonPath("$[0].description").isEmpty)
      .andExpect(jsonPath("$[0].type").value(type))
      .andExpect(jsonPath("$[0].time").value(clock.instant().toString()))
      .andExpect(jsonPath("$[0].targetEntity").isEmpty)
  }

  @Test
  @WithMockUser
  fun `can fetch a single audit event`() {
    val username = "username"
    val description = "description"
    val type = "type"
    val targetEntity = TargetEntity("entityType", "entityId")
    val remoteIp = "1.2.3.4"
    val event = AuditEvent(
      42,
      clock.instant(),
      type,
      description,
      targetEntity.type,
      targetEntity.pk,
      username,
      remoteIp,
      "{\"foo\":\"bar\"}"
    )

    whenever(auditEventService.getAuditEvent(42)).thenReturn(event)

    mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/audit/event/42"))
      .andExpect(status().isOk)
      .andExpect(jsonPath("$.id").value(42))
      .andExpect(jsonPath("$.username").value(username))
      .andExpect(jsonPath("$.description").value(description))
      .andExpect(jsonPath("$.type").value(type))
      .andExpect(jsonPath("$.time").value(clock.instant().toString()))
      .andExpect(jsonPath("$.targetEntityType").value(targetEntity.type))
      .andExpect(jsonPath("$.targetEntityPk").value(targetEntity.pk))
      .andExpect(jsonPath("$.extraData.foo").value("bar"))
  }

  @Test
  @WithMockUser
  fun `fetching works with non-existing events`() {
    whenever(auditEventService.getAuditEvent(42)).thenThrow(GenericNotFoundException("Audit event not found"))
    mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/audit/event/42"))
      .andExpect(status().isNotFound)
  }

  @Test
  @WithMockUser
  fun `can fetch an audit event by target entity`() {
    val targetEntity = TargetEntity("NEGOTIATION", "entityId123")
    val event = AuditEventListItem(
      42,
      clock.instant(),
      "type",
      "description",
      targetEntity,
      "username"
    )
    val list = listOf(event)

    whenever(auditEventService.getAuditEventsForEntity(targetEntity)).thenReturn(list)

    mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/audit/event/NEGOTIATION/entityId123"))
      .andExpect(status().isOk)
      .andExpect(jsonPath("$[0].targetEntity.type").value(targetEntity.type))
      .andExpect(jsonPath("$[0].targetEntity.pk").value(targetEntity.pk))
  }
}
