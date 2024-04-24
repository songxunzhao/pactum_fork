package com.pactum.audit

import com.pactum.audit.model.AuditEvent
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface AuditEventRepository : CrudRepository<AuditEvent, Long> {
  @Query("SELECT * FROM audit_event WHERE target_entity_type = :type AND target_entity_pk = :pk ORDER BY time ASC")
  fun findByTargetEntityTypeAndTargetEntityPkOrderByTimeAsc(type: String, pk: String): List<AuditEvent>
}
