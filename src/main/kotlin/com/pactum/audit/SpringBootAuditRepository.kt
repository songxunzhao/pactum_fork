package com.pactum.audit

import com.pactum.audit.model.AuditActor
import org.springframework.boot.actuate.audit.AuditEvent
import org.springframework.boot.actuate.audit.AuditEventRepository
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class SpringBootAuditRepository(
  private val auditEventRepository: com.pactum.audit.AuditEventRepository
) : AuditEventRepository {

  override fun add(auditEvent: AuditEvent) {
    val event = com.pactum.audit.model.AuditEvent.create(
      AuditActor(null, auditEvent.principal),
      auditEvent.type,
      "System event: ${auditEvent.type}",
      auditEvent.timestamp,
      null,
      auditEvent.data
    )
    auditEventRepository.save(event)
  }

  override fun find(principal: String, after: Instant, type: String): List<AuditEvent> {
    return emptyList()
  }
}
