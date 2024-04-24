package com.pactum.audit

import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.pactum.audit.model.AuditEvent
import com.pactum.test.TestClockHolder
import com.pactum.test.UnitTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@UnitTest
class SpringBootAuditRepositoryTest {

  private val clock = TestClockHolder.CLOCK
  private lateinit var springBootAuditRepository: SpringBootAuditRepository
  private lateinit var auditEventRepository: AuditEventRepository

  @BeforeEach
  fun `set up`() {
    auditEventRepository = spy()
    springBootAuditRepository = SpringBootAuditRepository(auditEventRepository)
  }

  @Test
  fun `add basic audit event without actor information`() {
    val type = "EVENT_TYPE"
    val extraData = mapOf("foo" to "bar")
    val username = "anonymous"

    springBootAuditRepository.add(org.springframework.boot.actuate.audit.AuditEvent(clock.instant(), username, type, extraData))

    val expectedEvent = AuditEvent(
      time = clock.instant(),
      type = type,
      description = "System event: EVENT_TYPE",
      username = username,
      extraDataJson = "{\"foo\":\"bar\"}"
    )
    verify(auditEventRepository, times(1)).save(eq(expectedEvent))
  }
}
