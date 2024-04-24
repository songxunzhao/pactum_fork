package com.pactum.audit

import com.pactum.audit.model.AuditEvent
import com.pactum.test.RepositoryTest
import com.pactum.test.TestClockHolder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@RepositoryTest
class AuditEventRepositoryTest @Autowired constructor(
  private val repository: AuditEventRepository
) {

  private val clock = TestClockHolder.CLOCK

  @BeforeEach
  fun `clean up`() {
    repository.deleteAll()
  }

  @Test
  fun `finds events by entity`() {
    val event1 = AuditEvent(
      time = clock.instant(),
      type = "TEST_EVENT",
      description = "Event description",
      targetEntityPk = "42",
      targetEntityType = "NEGOTIATION",
      username = "-",
      extraDataJson = "{}"
    )
    val event2 = AuditEvent(
      time = clock.instant().plusSeconds(60),
      type = "TEST_EVENT2",
      description = "Event description 2",
      targetEntityPk = "43",
      targetEntityType = "NEGOTIATION",
      username = "-",
      extraDataJson = "{}"
    )
    val event3 = AuditEvent(
      time = clock.instant().minusSeconds(120),
      type = "TEST_EVENT3",
      description = "Event description 3",
      targetEntityPk = "43",
      targetEntityType = "NEGOTIATION",
      username = "-",
      extraDataJson = "{}"
    )
    repository.save(event1)
    val savedEvent2 = repository.save(event2)
    val savedEvent3 = repository.save(event3)

    val foundEvents = repository.findByTargetEntityTypeAndTargetEntityPkOrderByTimeAsc("NEGOTIATION", "43")

    assertThat(foundEvents.size).isEqualTo(2)
    assertThat(foundEvents[0]).isEqualTo(savedEvent3)
    assertThat(foundEvents[1]).isEqualTo(savedEvent2)
  }
}
