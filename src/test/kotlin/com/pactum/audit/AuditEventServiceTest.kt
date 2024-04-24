package com.pactum.audit

import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.pactum.audit.model.AuditEvent
import com.pactum.audit.model.EntityAuditEvent
import com.pactum.auth.model.Role
import com.pactum.test.TestClockHolder
import com.pactum.test.UnitTest
import com.pactum.auth.SessionHelper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@UnitTest
class AuditEventServiceTest {

  private val clock = TestClockHolder.CLOCK
  private var auditEventRepository: AuditEventRepository = spy()
  private var auditEventService = AuditEventService(auditEventRepository, clock)

  enum class TestAuditEvent : EntityAuditEvent {
    TEST_EVENT {
      override fun getDescription(): String = "Event description"
    };

    override fun getEntityType(): String = "TEST_ENTITY"
  }

  @BeforeEach
  fun `set up`() {
    SessionHelper.clearLoggedInUser()
  }

  @Test
  fun `add basic audit event without actor information`() {
    val type = "EVENT_TYPE"
    val description = "description"
    val extraData = mapOf("foo" to "bar")

    auditEventService.addAuditEvent("EVENT_TYPE", description)
    auditEventService.addAuditEvent("EVENT_TYPE", description, extraData)

    val expectedEvent1 = AuditEvent(
      time = clock.instant(),
      type = type,
      description = description,
      username = "-",
      extraDataJson = "{}"
    )
    val expectedEvent2 = AuditEvent(
      time = clock.instant(),
      type = type,
      description = description,
      username = "-",
      extraDataJson = "{\"foo\":\"bar\"}"
    )
    verify(auditEventRepository, times(1)).save(eq(expectedEvent1))
    verify(auditEventRepository, times(1)).save(eq(expectedEvent2))
  }

  @Test
  fun `add basic audit events with actor information`() {
    val type = "EVENT_TYPE"
    val description = "description"

    val username = "backend@pactum.com"
    SessionHelper.setLoggedInUser("avalidtoken", username, listOf(Role.Admin))
    print(SessionHelper.getLoggedInUserEmail())
    auditEventService.addAuditEvent("EVENT_TYPE", description)
    val expectedEvent3 = AuditEvent(
      time = clock.instant(),
      type = type,
      description = description,
      username = username,
      extraDataJson = "{}"
    )
    verify(auditEventRepository, times(1)).save(eq(expectedEvent3))
  }

  @Test
  fun `add entity specific audit event`() {
    val entity = mapOf("foo" to "bar")
    val entityNew = mapOf("foo" to "baz")
    val extra = mapOf("comment" to "foobar")

    auditEventService.addEntityAuditEvent(TestAuditEvent.TEST_EVENT, 42, entityNew, entity, extra)
    val expectedEvent = AuditEvent(
      time = clock.instant(),
      type = "TEST_EVENT",
      description = "Event description",
      targetEntityPk = "42",
      targetEntityType = "TEST_ENTITY",
      username = "-",
      extraDataJson = """{"previousEntity":{"foo":"bar"},"entity":{"foo":"baz"},"comment":"foobar"}"""
    )
    verify(auditEventRepository, times(1)).save(eq(expectedEvent))
  }
}
