package com.pactum.health

import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.pactum.audit.AuditEventService
import com.pactum.auth.model.Role
import com.pactum.health.model.Health
import com.pactum.health.model.HealthStatus
import com.pactum.test.TestClockHolder
import com.pactum.test.UnitTest
import com.pactum.auth.SessionHelper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString

@UnitTest
class HealthServiceTest {

  private lateinit var auditEventService: AuditEventService
  private lateinit var healthService: HealthService

  @BeforeEach
  fun `set up`() {
    auditEventService = spy(AuditEventService(mock(), TestClockHolder.CLOCK))
    healthService = HealthService(auditEventService)
  }

  @Test
  fun `can get and set system health as admin`() {

    val req = Health(HealthStatus.DOWN)

    val email = "backend@pactum.com"
    val token = "avalidtoken"

    SessionHelper.setLoggedInUser(token, email, listOf(Role.Admin))

    val resp = healthService.setHealth(req)
    assertThat(resp).isNotNull
    val body = resp.body as Health
    assertThat(body.status).isEqualTo(HealthStatus.DOWN)

    val auditMap = mapOf(
      "oldStatus" to HealthStatus.UP,
      "newStatus" to HealthStatus.DOWN
    )
    verify(auditEventService, times(1)).addAuditEvent(eq("SET_HEALTH_STATUS"), anyString(), eq(auditMap))

    val health = healthService.getHealth()
    assertThat(health.status).isEqualTo(HealthStatus.DOWN)
  }
}
