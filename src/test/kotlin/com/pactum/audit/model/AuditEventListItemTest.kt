package com.pactum.audit.model

import com.pactum.test.TestClockHolder
import com.pactum.test.UnitTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@UnitTest
class AuditEventListItemTest {

  @Test
  fun `can create audit event list item`() {
    val type = "type"
    val description = "description"
    val entity = TargetEntity("entityType", "id")
    val now = TestClockHolder.CLOCK.instant()
    val username = "username"

    val event = AuditEvent(42, now, type, description, entity.type, entity.pk, username)
    val listItem = AuditEventListItem.createFromAuditEvent(event)

    assertThat(listItem.id).isEqualTo(42)
    assertThat(listItem.type).isEqualTo(type)
    assertThat(listItem.time).isEqualTo(now)
    assertThat(listItem.description).isEqualTo(description)
    assertThat(listItem.targetEntity).isEqualTo(entity)
    assertThat(listItem.username).isEqualTo("username")
  }
}
