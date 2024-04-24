package com.pactum.audit.model

interface EntityAuditEvent {
  fun getEntityType(): String
  fun getDescription(): String
}
