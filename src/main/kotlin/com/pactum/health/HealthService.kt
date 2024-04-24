package com.pactum.health

import com.pactum.audit.AuditEventService
import com.pactum.api.GenericOkResponse
import com.pactum.health.model.Health
import com.pactum.health.model.HealthStatus
import org.springframework.stereotype.Service

@Service
class HealthService(
  private val auditEventService: AuditEventService
) {

  object HealthStatusHolder {
    var globalHealth = Health(HealthStatus.UP)
  }

  fun setHealth(health: Health): GenericOkResponse {

    val currentValue = HealthStatusHolder.globalHealth.status
    HealthStatusHolder.globalHealth.status = health.status

    auditEventService.addAuditEvent(
      HealthAuditEventType.SET_HEALTH_STATUS.name,
      "Set application health status",
      mapOf("oldStatus" to currentValue, "newStatus" to getHealth().status)
    )

    return GenericOkResponse(HealthStatusHolder.globalHealth)
  }

  fun getHealth(): Health {
    return HealthStatusHolder.globalHealth
  }

  enum class HealthAuditEventType {
    SET_HEALTH_STATUS,
  }
}
