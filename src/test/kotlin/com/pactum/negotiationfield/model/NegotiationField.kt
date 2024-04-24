package com.pactum.negotiationfield.model

import com.pactum.client.model.NegotiationFieldConfigType
import java.time.Instant
import java.util.Random

fun NegotiationField.Companion.createMock(
  negotiationId: Long,
  type: NegotiationFieldConfigType,
  attribute: String,
  value: String
): NegotiationField {
  return NegotiationField(Random().nextLong(), negotiationId, type.name, attribute, value, Instant.now())
}
