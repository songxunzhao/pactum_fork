package com.pactum.clientfile

import com.pactum.audit.model.EntityAuditEvent

enum class ClientFileAuditEvent : EntityAuditEvent {
  FILE_UPLOADED {
    override fun getDescription(): String = "File uploaded"
  };

  override fun getEntityType(): String = "CLIENT_FILE"
}
