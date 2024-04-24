package com.pactum.audit.model

import com.pactum.test.TestClockHolder
import com.pactum.test.UnitTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@UnitTest
class AuditEventTest {

  @Test
  fun `can create audit event model`() {
    val actor = AuditActor("ip", "username")
    val type = "type"
    val description = "description"
    val entity = TargetEntity("entityType", "id")
    val data = mapOf("foo" to "bar")
    val now = TestClockHolder.CLOCK.instant()

    val event = AuditEvent.create(actor, type, description, now, entity, data)

    assertThat(event.id).isNull()
    assertThat(event.type).isEqualTo(type)
    assertThat(event.time).isEqualTo(now)
    assertThat(event.description).isEqualTo(description)
    assertThat(event.targetEntityType).isEqualTo("entityType")
    assertThat(event.targetEntityPk).isEqualTo("id")
    assertThat(event.username).isEqualTo("username")
    assertThat(event.remoteIp).isEqualTo("ip")
    assertThat(event.extraDataJson).isEqualTo("{\"foo\":\"bar\"}")
    assertThat(event.extraData).isEqualTo(data)
  }
}
