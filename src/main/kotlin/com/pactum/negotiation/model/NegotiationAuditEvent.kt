package com.pactum.negotiation.model

import com.pactum.audit.model.EntityAuditEvent

enum class NegotiationAuditEvent : EntityAuditEvent {
  NEGOTIATION_CREATED {
    override fun getDescription(): String = "Negotiation was created"
  },
  NEGOTIATION_UPDATED {
    override fun getDescription(): String = "Negotiation was updated"
  },
  NEGOTIATION_RELOAD_MODEL {
    override fun getDescription(): String = "Negotiation model data was reloaded"
  },
  NEGOTIATION_DELETED {
    override fun getDescription(): String = "Negotiation was deleted"
  },

  NEGOTIATION_FIELD_CREATED {
    override fun getDescription(): String = "Negotiation field created"
  },
  NEGOTIATION_FIELD_UPDATED {
    override fun getDescription(): String = "Negotiation field updated"
  },
  NEGOTIATION_FIELD_DELETED {
    override fun getDescription(): String = "Negotiation field deleted"
  };

  override fun getEntityType(): String = "NEGOTIATION"
}
