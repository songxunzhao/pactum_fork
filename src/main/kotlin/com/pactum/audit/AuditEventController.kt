package com.pactum.audit

import com.pactum.audit.model.AuditEvent
import com.pactum.audit.model.AuditEventListItem
import com.pactum.audit.model.TargetEntity
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@RestController
@Api(tags = ["admin"], description = "APIs consumed by the Admin panel")
class AuditEventController(
  private val auditEventService: AuditEventService
) {

  @ApiOperation(value = "Get audit events for target entity")
  @GetMapping("/api/v1/audit/event/{entityType}/{entityPk}")
  fun getEventsByEntity(@PathVariable entityType: String, @PathVariable entityPk: String): List<AuditEventListItem> {
    return auditEventService.getAuditEventsForEntity(TargetEntity(entityType, entityPk))
  }

  @ApiOperation(value = "Get list of audit events")
  @GetMapping("/api/v1/audit/event")
  fun getAuditEvents(): List<AuditEventListItem> {
    return auditEventService.getAuditEvents()
  }

  @ApiOperation(value = "Get audit event details")
  @GetMapping("/api/v1/audit/event/{id}")
  fun getAuditEvent(@PathVariable id: Long): AuditEvent {
    return auditEventService.getAuditEvent(id)
  }
}
