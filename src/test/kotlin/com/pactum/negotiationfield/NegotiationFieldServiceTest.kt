package com.pactum.negotiationfield

import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.pactum.audit.AuditEventService
import com.pactum.client.model.NegotiationFieldConfigType.MODEL
import com.pactum.client.model.NegotiationFieldConfigType.TERM
import com.pactum.client.model.NegotiationFieldConfigType.OTHER
import com.pactum.negotiation.model.NegotiationAuditEvent
import com.pactum.negotiationfield.model.NegotiationField
import com.pactum.test.TestClockHolder
import com.pactum.test.UnitTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@UnitTest
class NegotiationFieldServiceTest {

  private lateinit var negotiationFieldRepository: NegotiationFieldRepository
  private lateinit var auditEventService: AuditEventService
  private lateinit var negotiationFieldService: NegotiationFieldService

  private val mockNegotiationId = 43L
  private val mockFields = listOf(
    NegotiationField(1L, mockNegotiationId, MODEL.name, "attr1", "value1", TestClockHolder.NOW),
    NegotiationField(2L, mockNegotiationId, TERM.name, "attr2", "value2", TestClockHolder.NOW),
    NegotiationField(3L, mockNegotiationId, OTHER.name, "attr3", "value3", TestClockHolder.NOW)
  )

  @BeforeEach
  fun `set up`() {
    auditEventService = mock()
    negotiationFieldRepository = mock()
    negotiationFieldService = NegotiationFieldService(
      negotiationFieldRepository,
      auditEventService,
      TestClockHolder.CLOCK
    )
  }

  @Test
  fun `can get fields from service`() {
    whenever(negotiationFieldRepository.findAllByNegotiationId(mockNegotiationId)).thenReturn(mockFields)

    val expectedList = listOf(
      mockFields[0].toApiEntity(),
      mockFields[1].toApiEntity(),
      mockFields[2].toApiEntity()
    )

    val list = negotiationFieldService.getFieldsApiEntitiesForNegotiation(mockNegotiationId)

    assertThat(list).isEqualTo(expectedList)
  }

  @Test
  fun `can set fields to a negotiation`() {
    whenever(negotiationFieldRepository.findAllByNegotiationId(mockNegotiationId)).thenReturn(mockFields)
    val unchangedField = mockFields[2]
    val modifiedField = mockFields[0].copy(value = "value1 updated")
    val addedField = NegotiationField(
      null,
      mockNegotiationId,
      MODEL.name,
      "attr5",
      "new value added",
      TestClockHolder.NOW
    )

    val payload = listOf(
      modifiedField.toApiEntity(),
      addedField.toApiEntity(),
      unchangedField.toApiEntity()
    )

    whenever(negotiationFieldRepository.save(addedField)).thenReturn(addedField)
    whenever(negotiationFieldRepository.save(modifiedField)).thenReturn(modifiedField)

    val result = negotiationFieldService.setFieldsForNegotiation(mockNegotiationId, payload)
    @Suppress("UNCHECKED_CAST")
    assertThat(result.body as List<NegotiationField.ApiEntity>).containsExactlyInAnyOrder(payload[0], payload[1])

    // verify that item was deleted
    verify(negotiationFieldRepository, times(1)).delete(mockFields[1])
    verify(auditEventService, times(1)).addEntityAuditEvent(
      eq(NegotiationAuditEvent.NEGOTIATION_FIELD_DELETED),
      eq(mockNegotiationId),
      eq(mockFields[1]),
      anyOrNull(),
      anyOrNull()
    )

    // verify that an item was updated
    verify(negotiationFieldRepository, times(1)).save(modifiedField)
    verify(auditEventService, times(1)).addEntityAuditEvent(
      eq(NegotiationAuditEvent.NEGOTIATION_FIELD_UPDATED),
      eq(mockNegotiationId),
      eq(modifiedField),
      eq(mockFields[0]),
      anyOrNull()
    )

    // verify that an item was created
    verify(negotiationFieldRepository, times(1)).save(addedField)
    verify(auditEventService, times(1)).addEntityAuditEvent(
      eq(NegotiationAuditEvent.NEGOTIATION_FIELD_CREATED),
      eq(mockNegotiationId),
      eq(addedField),
      anyOrNull(),
      anyOrNull()
    )

    // verify that unchanged item was not modified
    verify(negotiationFieldRepository, times(0)).save(mockFields[2])
    verify(auditEventService, times(0)).addEntityAuditEvent(
      eq(NegotiationAuditEvent.NEGOTIATION_FIELD_UPDATED),
      eq(mockNegotiationId),
      eq(mockFields[2]),
      anyOrNull(),
      anyOrNull()
    )
  }

  @Test
  fun `can add-update fields of a negotiation`() {
    whenever(negotiationFieldRepository.findAllByNegotiationId(mockNegotiationId)).thenReturn(mockFields)
    val modifiedField = mockFields[0].copy(value = "value1 updated")
    val addedField = NegotiationField(
      null,
      mockNegotiationId,
      MODEL.name,
      "attr5",
      "new value added",
      TestClockHolder.NOW
    )

    val payload = listOf(
      modifiedField.toApiEntity(),
      addedField.toApiEntity()
    )

    whenever(negotiationFieldRepository.save(addedField)).thenReturn(addedField)
    whenever(negotiationFieldRepository.save(modifiedField)).thenReturn(modifiedField)

    val result = negotiationFieldService.setFieldsForNegotiation(
      mockNegotiationId,
      payload,
      null,
      NegotiationFieldService.Behaviour.ADD_UPDATE
    )
    @Suppress("UNCHECKED_CAST")
    assertThat(result.body as List<NegotiationField.ApiEntity>).containsExactlyInAnyOrder(payload[0], payload[1])

    // verify that item was NOT deleted
    verify(negotiationFieldRepository, times(0)).delete(mockFields[1])

    // verify that an item was updated
    verify(negotiationFieldRepository, times(1)).save(modifiedField)

    // verify that an item was created
    verify(negotiationFieldRepository, times(1)).save(addedField)
  }

  @Test
  fun `can get fields`() {

    whenever(negotiationFieldRepository.findAllByNegotiationId(mockNegotiationId)).thenReturn(mockFields)
    val expectedList = listOf(
      mockFields[0],
      mockFields[1],
      mockFields[2]
    )

    val list = negotiationFieldService.getFieldsForNegotiation(mockNegotiationId)
    assertThat(list).isEqualTo(expectedList)
  }

  @Test
  fun `can get model fields with negotiation id`() {

    whenever(negotiationFieldRepository.findAllByNegotiationId(mockNegotiationId)).thenReturn(mockFields)
    val expectedMap = mapOf(
      "attr1" to "value1"
    )

    val list = negotiationFieldService.getModelFields(mockNegotiationId)
    assertThat(list).isEqualTo(expectedMap)
  }

  @Test
  fun `can get term fields with negotiation id`() {

    whenever(negotiationFieldRepository.findAllByNegotiationId(mockNegotiationId)).thenReturn(mockFields)
    val expectedMap = mapOf(
      "attr2" to "value2"
    )
    val list = negotiationFieldService.getTermFields(mockNegotiationId)
    assertThat(list).isEqualTo(expectedMap)
  }

  @Test
  fun `can get other fields with negotiation id`() {

    whenever(negotiationFieldRepository.findAllByNegotiationId(mockNegotiationId)).thenReturn(mockFields)
    val expectedMap = mapOf(
      "attr3" to "value3"
    )

    val list = negotiationFieldService.getOtherFields(mockNegotiationId)
    assertThat(list).isEqualTo(expectedMap)
  }

  @Test
  fun `can filter model fields with negotiation fields`() {

    val multipleModelFields = mockFields + listOf(
      NegotiationField(4L, mockNegotiationId, MODEL.name, "attr4", "value4", TestClockHolder.NOW)
    )
    val expectedMap = mapOf(
      "attr1" to "value1",
      "attr4" to "value4"
    )

    val result = negotiationFieldService.filterModelFields(multipleModelFields)
    assertThat(result).isEqualTo(expectedMap)
  }

  @Test
  fun `can filter term fields with negotiation fields`() {

    val multipleTermFields = mockFields + listOf(
      NegotiationField(4L, mockNegotiationId, TERM.name, "attr4", "value4", TestClockHolder.NOW)
    )
    val expectedMap = mapOf(
      "attr2" to "value2",
      "attr4" to "value4"
    )

    val result = negotiationFieldService.filterTermFields(multipleTermFields)
    assertThat(result).isEqualTo(expectedMap)
  }

  @Test
  fun `can filter other fields with negotiation fields`() {

    val multipleOtherFields = mockFields + listOf(
      NegotiationField(4L, mockNegotiationId, OTHER.name, "attr4", "value4", TestClockHolder.NOW)
    )
    val expectedMap = mapOf(
      "attr3" to "value3",
      "attr4" to "value4"
    )

    val result = negotiationFieldService.filterOtherFields(multipleOtherFields)
    assertThat(result).isEqualTo(expectedMap)
  }
}
