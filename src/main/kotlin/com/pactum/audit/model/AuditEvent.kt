package com.pactum.audit.model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.pactum.utils.JsonHelper
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.relational.core.mapping.Column
import java.time.Instant

data class AuditEvent(
  @Id
  val id: Long? = null,
  val time: Instant,
  val type: String,
  val description: String? = null,
  val targetEntityType: String? = null,
  val targetEntityPk: String? = null,
  val username: String? = null,
  val remoteIp: String? = null,
  @Column("extra_data")
  val extraDataJson: String? = null
) {

  @Transient
  val extraData: Map<String, Any?> = JsonHelper.convertToMapOrEmpty(extraDataJson)

  companion object {
    fun create(
      actor: AuditActor,
      type: String,
      description: String,
      time: Instant,
      entity: TargetEntity?,
      extraData: Map<String, Any?>?
    ): AuditEvent {
      return AuditEvent(
        null,
        time,
        type,
        description,
        entity?.type,
        entity?.pk,
        actor.username,
        actor.remoteIp,
        jacksonObjectMapper().writeValueAsString(extraData)
      )
    }
  }
}
