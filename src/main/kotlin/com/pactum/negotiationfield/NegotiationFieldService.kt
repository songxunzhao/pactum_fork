package com.pactum.negotiationfield

import com.pactum.audit.AuditEventService
import com.pactum.audit.model.EntityAuditEvent
import com.pactum.client.model.NegotiationFieldConfigType
import com.pactum.api.GenericOkResponse
import com.pactum.negotiation.model.NegotiationAuditEvent
import com.pactum.negotiationfield.model.NegotiationField
import org.springframework.stereotype.Service
import java.time.Clock

@Service
class NegotiationFieldService(
  private val negotiationFieldRepository: NegotiationFieldRepository,
  private val auditEventService: AuditEventService,
  private val clock: Clock
) {

  fun setFieldsForNegotiation(
    negotiationId: Long,
    updatedFields: List<NegotiationField.ApiEntity>,
    comment: String? = null,
    behaviour: Behaviour = Behaviour.REPLACE
  ): GenericOkResponse {

    fun NegotiationField.auditLog(event: EntityAuditEvent, previousField: NegotiationField? = null, comment: String?) {
      auditEventService.addEntityAuditEvent(event, this.negotiationId, this, previousField, mapOf("comment" to comment))
    }

    val existingFields = negotiationFieldRepository.findAllByNegotiationId(negotiationId)
    val finalFields = mutableListOf<NegotiationField>()

    updatedFields.forEach { field ->
      val existingField = existingFields.find {
        it.attribute == field.attribute && NegotiationFieldConfigType.valueOf(it.type) == field.type
      }
      if (existingField == null) {
        // create a new field
        val entity = NegotiationField(null, negotiationId, field.type.name, field.attribute, field.value, clock.instant())
        entity.auditLog(NegotiationAuditEvent.NEGOTIATION_FIELD_CREATED, comment = comment)
        val saved = negotiationFieldRepository.save(entity)
        finalFields.add(saved)
      } else if (existingField.value != field.value) {
        // update existing
        val entity = existingField.copy(value = field.value)
        entity.auditLog(NegotiationAuditEvent.NEGOTIATION_FIELD_UPDATED, existingField, comment = comment)
        val saved = negotiationFieldRepository.save(entity)
        finalFields.add(saved)
      }
    }

    if (behaviour == Behaviour.REPLACE) {
      // delete stuff that was not set
      existingFields.forEach { field ->
        val updatedField = updatedFields.find {
          it.attribute == field.attribute && it.type == NegotiationFieldConfigType.valueOf(field.type)
        }
        if (updatedField == null) {
          field.auditLog(NegotiationAuditEvent.NEGOTIATION_FIELD_DELETED, comment = comment)
          negotiationFieldRepository.delete(field)
        }
      }
    }
    return GenericOkResponse(finalFields.map { it.toApiEntity() })
  }

  fun getFieldsForNegotiation(negotiationId: Long): List<NegotiationField> {
    return negotiationFieldRepository.findAllByNegotiationId(negotiationId)
  }

  fun getFieldsApiEntitiesForNegotiation(negotiationId: Long): List<NegotiationField.ApiEntity> {
    return getFieldsForNegotiation(negotiationId).map { it.toApiEntity() }
  }

  fun getModelFields(negotiationId: Long): Map<String, Any> {
    return getFieldsByType(negotiationId, null, NegotiationFieldConfigType.MODEL)
  }

  fun filterModelFields(fields: List<NegotiationField>?): Map<String, Any> {
    return getFieldsByType(null, fields, NegotiationFieldConfigType.MODEL)
  }

  fun getTermFields(negotiationId: Long): Map<String, Any> {
    return getFieldsByType(negotiationId, null, NegotiationFieldConfigType.TERM)
  }

  fun filterTermFields(fields: List<NegotiationField>?): Map<String, Any> {
    return getFieldsByType(null, fields, NegotiationFieldConfigType.TERM)
  }

  fun getOtherFields(negotiationId: Long): Map<String, Any> {
    return getFieldsByType(negotiationId, null, NegotiationFieldConfigType.OTHER)
  }

  fun filterOtherFields(fields: List<NegotiationField>?): Map<String, Any> {
    return getFieldsByType(null, fields, NegotiationFieldConfigType.OTHER)
  }

  private fun getFieldsByType(
    negotiationId: Long?,
    fields: List<NegotiationField>?,
    type: NegotiationFieldConfigType
  ): Map<String, Any> {
    val allFields = if (!fields.isNullOrEmpty()) {
      fields
    } else if (negotiationId != null) {
      getFieldsForNegotiation(negotiationId)
    } else {
      emptyList()
    }
    return allFields.filter {
      NegotiationFieldConfigType.valueOf(it.type) == type
    }.map {
      mapOf(it.attribute to it.value)
    }.flatMap {
      it.entries
    }.associate {
      it.key to it.value
    }
  }

  enum class Behaviour {
    ADD_UPDATE,
    REPLACE
  }
}
