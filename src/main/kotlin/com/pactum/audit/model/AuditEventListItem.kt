package com.pactum.audit.model

import java.time.Instant

data class AuditEventListItem(
  val id: Long? = null,
  val time: Instant,
  val type: String,
  val description: String? = null,
  val targetEntity: TargetEntity? = null,
  val username: String? = null
) {

  companion object {
    fun createFromAuditEvent(event: AuditEvent): AuditEventListItem {
      return AuditEventListItem(
        event.id,
        event.time,
        event.type,
        event.description,
        if (event.targetEntityPk != null && event.targetEntityType != null)
          TargetEntity(event.targetEntityType, event.targetEntityPk)
        else
          null,
        event.username
      )
    }
  }
}
