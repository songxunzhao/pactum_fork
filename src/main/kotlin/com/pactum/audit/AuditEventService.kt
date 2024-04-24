package com.pactum.audit

import com.pactum.api.GenericNotFoundException
import com.pactum.audit.model.AuditActor
import com.pactum.audit.model.AuditEvent
import com.pactum.audit.model.AuditEventListItem
import com.pactum.audit.model.EntityAuditEvent
import com.pactum.audit.model.TargetEntity
import com.pactum.auth.SessionHelper
import org.springframework.stereotype.Service
import java.time.Clock
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.core.Context

@Service
class AuditEventService(
  private val auditEventRepository: AuditEventRepository,
  private val clock: Clock
) {
  @Context
  private val request: HttpServletRequest? = null

  fun getAuditEvents(): List<AuditEventListItem> {
    return auditEventRepository.findAll().map { AuditEventListItem.createFromAuditEvent(it) }
  }

  fun getAuditEvent(id: Long): AuditEvent {
    val event = auditEventRepository.findById(id)
    if (event.isPresent) {
      return event.get()
    }
    throw GenericNotFoundException("Audit event not found")
  }

  fun addAuditEvent(type: String, description: String, extraData: Map<String, Any?>? = mapOf()) {
    val instant = clock.instant()
    val event = AuditEvent.create(
      getCurrentActor(),
      type,
      description,
      instant,
      null,
      extraData
    )
    auditEventRepository.save(event)
  }

  fun addEntityAuditEvent(
    targetEntityType: EntityAuditEvent,
    targetEntityId: Any,
    entity: Any? = null,
    previousEntity: Any? = null,
    extraData: Map<String, Any?> = mapOf()
  ) {

    val extraWithEntity = mapOf("previousEntity" to previousEntity, "entity" to entity) + extraData
    val event = AuditEvent.create(
      getCurrentActor(),
      targetEntityType.toString(),
      targetEntityType.getDescription(),
      clock.instant(),
      TargetEntity(targetEntityType.getEntityType(), targetEntityId.toString()),
      extraWithEntity
    )
    auditEventRepository.save(event)
  }

  private fun getCurrentActor(): AuditActor {
    val username = try {
      SessionHelper.getLoggedInUserEmail()
    } catch (e: Exception) {
      "-"
    }
    return AuditActor(request?.remoteAddr, username)
  }

  fun getAuditEventsForEntity(targetEntity: TargetEntity): List<AuditEventListItem> {
    return auditEventRepository.findByTargetEntityTypeAndTargetEntityPkOrderByTimeAsc(targetEntity.type, targetEntity.pk)
      .map { AuditEventListItem.createFromAuditEvent(it) }
  }
}
