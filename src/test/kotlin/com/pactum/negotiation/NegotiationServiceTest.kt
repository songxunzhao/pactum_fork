package com.pactum.negotiation

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.whenever
import com.pactum.audit.AuditEventService
import com.pactum.auth.AccessDeniedException
import com.pactum.auth.model.Role
import com.pactum.negotiationstate.NegotiationStateRepository
import com.pactum.client.ClientRepository
import com.pactum.client.ClientService
import com.pactum.client.model.Client
import com.pactum.client.model.NegotiationFieldConfig
import com.pactum.client.model.NegotiationFieldConfigType
import com.pactum.extract.ExtractService
import com.pactum.model.ModelService
import com.pactum.negotiation.model.CreateNegotiationReq
import com.pactum.negotiation.model.Negotiation
import com.pactum.negotiation.model.NegotiationPubSubEvent
import com.pactum.negotiation.model.NegotiationTermChangedPubSubMessage
import com.pactum.negotiation.model.ReloadNegotiationModelReq
import com.pactum.negotiation.summary.model.ExtraValueFormat
import com.pactum.test.TestClockHolder
import com.pactum.test.UnitTest
import com.pactum.auth.SessionHelper
import com.pactum.negotiation.model.NegotiationAuditEvent
import com.pactum.negotiation.model.NegotiationStatusField
import com.pactum.negotiation.model.UpdateNegotiationReq
import com.pactum.negotiation.summary.model.ExtraValue
import com.pactum.negotiationfield.NegotiationFieldService
import com.pactum.negotiationfield.model.NegotiationField
import com.pactum.negotiationfield.model.createMock
import com.pactum.pubsub.PubSubService
import com.pactum.pubsub.PubSubTopic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.relational.core.conversion.DbActionExecutionException
import org.springframework.http.HttpStatus
import java.time.Instant
import java.util.Optional

@UnitTest
class NegotiationServiceTest {

  private val negotiationRepository: NegotiationRepository = mock()
  private val clientRepository: ClientRepository = mock()
  private val extractService: ExtractService = mock()
  private val negotiationFieldService: NegotiationFieldService = mock()
  private val negotiationModelTermService: NegotiationModelTermService = mock()
  private val pubSubService: PubSubService = mock()
  private val clientService: ClientService = mock()
  private val modelService: ModelService = mock()
  private val negotiationStateRepository: NegotiationStateRepository = mock()
  private val baseUrl = "https://www.pactum.com"
  private lateinit var auditEventService: AuditEventService
  private lateinit var negotiationService: NegotiationService

  @BeforeEach
  fun `set up`() {
    auditEventService = spy(AuditEventService(mock(), TestClockHolder.CLOCK))
    negotiationService = NegotiationService(
      negotiationRepository,
      baseUrl,
      clientRepository,
      extractService,
      auditEventService,
      negotiationFieldService,
      negotiationModelTermService,
      pubSubService,
      clientService,
      modelService,
      negotiationStateRepository
    )
  }

  @Test
  fun `can create new a negotiation`() {
    val clientId = 0L
    val flowId = "123"
    val modelId = "qwr"
    val modelKey = "asd"
    val modelKeys = listOf(modelKey)
    val email = "backend@pactum.com"
    val token = "avalidtoken"

    SessionHelper.setLoggedInUser(token, email, listOf(Role.Admin))

    val req = CreateNegotiationReq(clientId, flowId, modelId, modelKeys)
    val negotiation = Negotiation.create(clientId, flowId, modelId, modelKey)
    val existingNegotiation = negotiation.copy(id = 42)

    whenever(negotiationRepository.save(any<Negotiation>())).thenReturn(existingNegotiation)

    val createdNegotiations = negotiationService.createNegotiations(req)
    assertThat(createdNegotiations).isNotNull

    verify(auditEventService, times(1)).addEntityAuditEvent(
      eq(NegotiationAuditEvent.NEGOTIATION_CREATED),
      eq(42L),
      eq(existingNegotiation),
      anyOrNull(),
      anyOrNull()
    )
  }

  @Test
  fun `can delete a negotiation by id`() {
    val email = "backend@pactum.com"
    val token = "avalidtoken"
    val id = 0L

    SessionHelper.setLoggedInUser(token, email, listOf(Role.Admin))
    val negotiation = Negotiation.empty(id)
    whenever(negotiationRepository.findById(id)).thenReturn(Optional.of(negotiation))
    whenever(negotiationRepository.save(negotiation)).thenReturn(negotiation)
    val resp = negotiationService.deleteNegotiation(id)

    verify(auditEventService, times(1)).addEntityAuditEvent(
      eq(NegotiationAuditEvent.NEGOTIATION_DELETED),
      eq(id),
      anyOrNull(),
      anyOrNull(),
      anyOrNull()
    )
    assertThat(resp.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
  }

  @Test
  fun `can not delete a negotiation by id if not found`() {
    val email = "backend@pactum.com"
    val token = "avalidtoken"
    val id = 0L

    SessionHelper.setLoggedInUser(token, email, listOf(Role.Admin))
    whenever(negotiationRepository.findById(id)).thenReturn(Optional.empty())
    assertThrows<NegotiationIdNotFoundException> {
      negotiationService.deleteNegotiation(id)
    }
  }

  @Test
  fun `can get list of negotiations if is admin`() {
    val email = "backend@pactum.com"
    val token = "avalidtoken"
    val clientId = 1L
    val id = 0L
    val stateId = "abc"
    val status = "CREATED"
    val flowId = "qwe"
    val modelId = "123"
    val modelKey = "123"
    val flowVersionId = null
    val modelVersionId = null
    val clientTag = "tag"
    val clientName = "name"
    val fields = listOf(
      NegotiationFieldConfig(
        NegotiationFieldConfigType.MODEL,
        "Status",
        ExtraValueFormat.TEXT,
        "status",
        3
      ),
      NegotiationFieldConfig(
        NegotiationFieldConfigType.TERM,
        "Term 1",
        ExtraValueFormat.TEXT,
        "term1",
        3
      )
    )
    SessionHelper.setLoggedInUser(token, email, listOf(Role.Admin))

    val client = Client(
      tag = clientTag,
      name = clientName,
      negotiationFieldsConfig = jacksonObjectMapper().writeValueAsString(fields)
    )
    val negotiation = Negotiation(id = id, clientId = clientId, stateId = stateId, status = status,
      flowId = flowId, modelId = modelId, modelKey = modelKey, flowVersionId = flowVersionId, modelVersionId = modelVersionId,
      createTime = Instant.now(), chatStartTime = Instant.now(), chatUpdateTime = Instant.now(),
      isVisibleSupplier = true, modelAttributes = """{"status": "On going"}""")

    val negotiationField1 = NegotiationField.createMock(id, NegotiationFieldConfigType.MODEL, "attr1", "val1")
    val negotiationField2 = NegotiationField.createMock(id, NegotiationFieldConfigType.TERM, "term1", "value2")
    val negotiationFields = listOf(negotiationField1, negotiationField2)

    whenever(negotiationRepository.findByClientIdAndIsDeletedIsFalseOrderByCreateTimeDesc(clientId))
      .thenReturn(listOf(negotiation))
    whenever(clientRepository.findById(clientId)).thenReturn(Optional.of(client))
    whenever(negotiationFieldService.getFieldsForNegotiation(negotiation.id!!)).thenReturn(negotiationFields)
    whenever(negotiationFieldService.filterModelFields(negotiationFields)).thenReturn(mapOf("attr1" to "val1"))
    whenever(negotiationFieldService.filterTermFields(negotiationFields)).thenReturn(mapOf("term1" to "value2"))
    whenever(negotiationFieldService.filterOtherFields(negotiationFields)).thenReturn(emptyMap())
    whenever(extractService.getTerms(negotiation)).thenReturn(emptyMap())

    val result = negotiationService.getNegotiationsByClientId(clientId)

    assertThat(result.size).isEqualTo(1)
    assertThat(result[0]["clientName"]).isEqualTo(clientName)
    @Suppress("UNCHECKED_CAST")
    val extra = result[0]["extra"] as List<ExtraValue>
    assertThat(extra[0].label).isEqualTo("Status")
    assertThat(extra[0].value).isEqualTo("On going")
    assertThat(extra[1].label).isEqualTo("Term 1")
    assertThat(extra[1].value).isEqualTo("value2")
  }

  @Test
  fun `can get list of negotiations of this client if not admin and client tag is set`() {
    val email = "backend@pactum.com"
    val token = "avalidtoken"

    val thisClientId = 1L
    val id = 0L
    val stateId = "abc"
    val status = "CREATED"
    val flowId = "qwe"
    val modelId = "123"
    val modelKey = "123"
    val flowVersionId = null
    val modelVersionId = null
    val thisClientTag = "tag"
    val clientName = "name"

    val fields = listOf(
      NegotiationFieldConfig(
        NegotiationFieldConfigType.MODEL,
        "Status",
        ExtraValueFormat.TEXT,
        "status",
        3
      ),
      NegotiationFieldConfig(
        NegotiationFieldConfigType.TERM,
        "Term 1",
        ExtraValueFormat.TEXT,
        "term1",
        3
      )
    )

    val client = Client(
      id = thisClientId,
      tag = thisClientTag,
      name = clientName,
      negotiationFieldsConfig = jacksonObjectMapper().writeValueAsString(fields)
    )

    SessionHelper.setLoggedInUser(token, email, listOf(Role.Client)).apply {
      clientTag = thisClientTag
    }

    val negotiation = Negotiation(
      id = id, clientId = thisClientId, stateId = stateId, status = status,
      flowId = flowId, modelId = modelId, modelKey = modelKey, flowVersionId = flowVersionId, modelVersionId = modelVersionId,
      createTime = Instant.now(), chatStartTime = Instant.now(), chatUpdateTime = Instant.now(),
      isVisibleSupplier = true, modelAttributes = """{"status": "On going"}"""
    )

    val negotiationField1 = NegotiationField.createMock(id, NegotiationFieldConfigType.MODEL, "attr1", "val1")
    val negotiationField2 = NegotiationField.createMock(id, NegotiationFieldConfigType.TERM, "term1", "value2")
    val negotiationFields = listOf(negotiationField1, negotiationField2)

    whenever(clientRepository.findFirstByTag(thisClientTag)).thenReturn(client)
    whenever(negotiationRepository.findByClientIdAndIsDeletedIsFalseOrderByCreateTimeDesc(thisClientId))
      .thenReturn(listOf(negotiation))
    whenever(negotiationFieldService.getFieldsForNegotiation(negotiation.id!!)).thenReturn(negotiationFields)
    whenever(negotiationFieldService.filterModelFields(negotiationFields)).thenReturn(mapOf("attr1" to "val1"))
    whenever(negotiationFieldService.filterTermFields(negotiationFields)).thenReturn(mapOf("term1" to "value2"))
    whenever(negotiationFieldService.filterOtherFields(negotiationFields)).thenReturn(emptyMap())
    whenever(extractService.getTerms(negotiation)).thenReturn(emptyMap())

    val result = negotiationService.getNegotiationsForClient()

    assertThat(result.size).isEqualTo(1)
    assertThat(result[0]["clientName"]).isEqualTo(clientName)
    @Suppress("UNCHECKED_CAST")
    val extra = result[0]["extra"] as List<ExtraValue>
    assertThat(extra[0].label).isEqualTo("Status")
    assertThat(extra[0].value).isEqualTo("On going")
    assertThat(extra[1].label).isEqualTo("Term 1")
    assertThat(extra[1].value).isEqualTo("value2")
  }

  @Test
  fun `can not get list of negotiations if not admin and clientTag is not set`() {
    val email = "backend@pactum.com"
    val token = "avalidtoken"

    SessionHelper.setLoggedInUser(token, email, listOf(Role.Client)).apply {
      clientTag = null
    }

    assertThrows<AccessDeniedException> {
      negotiationService.getNegotiationsForClient()
    }
  }

  @Test
  fun `can update negotiation model and publish changed terms`() {

    val oldTerms = mapOf(
      "a" to "b",
      "c" to mapOf(
        "d" to 1,
        "e" to 2
      )
    )
    val oldModel = Negotiation(
      id = 42,
      clientId = 1,
      stateId = "1",
      status = "OPENED",
      flowId = "a",
      modelId = "b",
      modelKey = "c",
      createTime = Instant.now(),
      terms = jacksonObjectMapper().writeValueAsString(oldTerms)
    )

    val newTerms = mapOf(
      "a" to "b",
      "c" to mapOf(
        "d" to 1,
        "e" to 4
      )
    )
    val newModel = Negotiation(
      id = 42,
      clientId = 1,
      stateId = "1",
      status = "OPENED",
      flowId = "a",
      modelId = "b",
      modelKey = "c",
      createTime = Instant.now(),
      terms = jacksonObjectMapper().writeValueAsString(oldTerms + newTerms)
    )

    whenever(negotiationModelTermService.getTerms(oldModel)).thenReturn(oldTerms)
    whenever(clientService.getTagById(any())).thenReturn("testclient")
    whenever(negotiationRepository.save(any<Negotiation>())).thenReturn(newModel)

    negotiationService.updateWithPublish(oldModel, newModel)

    verify(negotiationRepository, times(1)).save(newModel)
    val (topic, messages, attributes) =
      argumentCaptor<PubSubTopic, List<NegotiationTermChangedPubSubMessage>, Map<String, String>>()
    verify(pubSubService, times(1)).publishMany(topic.capture(), messages.capture(), attributes.capture())

    assertThat(topic.firstValue).isEqualTo(PubSubTopic.NEGOTIATION_EVENT)
    assertThat(messages.firstValue[0]).isEqualTo(
      NegotiationTermChangedPubSubMessage(
        term = "c",
        previousValue = jacksonObjectMapper().readValue("""{"d":1,"e":2}"""),
        value = jacksonObjectMapper().readValue("""{"d":1,"e":4}"""),
        stateId = "1",
        clientTag = "testclient",
        chatEndTime = null,
        chatStartTime = null,
        vendorId = "c"
      )
    )
    assertThat(attributes.firstValue).containsEntry("clientTag", "testclient")
    assertThat(attributes.firstValue).containsEntry("eventType", NegotiationPubSubEvent.TERM_CHANGED.name)

    verify(auditEventService, times(1)).addEntityAuditEvent(
      eq(NegotiationAuditEvent.NEGOTIATION_UPDATED),
      eq(42L),
      eq(newModel),
      eq(oldModel),
      anyOrNull()
    )
  }

  @Test
  fun `can reload negotiation model if admin`() {
    val email = "backend@pactum.com"
    val token = "avalidtoken"
    val clientId = 1L
    val id = 0L
    val stateId = "abc"
    val status = "CREATED"
    val flowId = "qwe"
    val modelId = "123"
    val modelKey = "123"
    val flowVersionId = null
    val modelVersionId = null

    val attributes = mapOf(
      "vendorId" to "1",
      "attr1" to true,
      "attr2" to 2.5
    )

    SessionHelper.setLoggedInUser(token, email, listOf(Role.Admin))

    val negotiation = Negotiation(id, clientId, stateId, status,
      flowId, modelId, modelKey, flowVersionId, modelVersionId, Instant.now())
    val updatedNegotiation = negotiation.copy(modelAttributes = jacksonObjectMapper().writeValueAsString(attributes))

    val listOfIds = listOf(id)
    val req = ReloadNegotiationModelReq(listOfIds)
    whenever(negotiationRepository.findAllById(listOfIds)).thenReturn(listOf(negotiation))
    whenever(modelService.getModelFromFile(modelId, modelKey)).thenReturn(attributes)
    whenever(negotiationRepository.save<Negotiation>(any())).thenReturn(updatedNegotiation)

    val result = negotiationService.reloadNegotiationModel(req)
    assertThat(result).isNotNull
    @Suppress("UNCHECKED_CAST")
    val body = result.body as List<Negotiation.ApiEntity>
    assertThat(body.size).isEqualTo(1)
    assertThat(body[0].id).isEqualTo(id)
    assertThat(body[0].stateId).isEqualTo(stateId)

    verify(auditEventService, times(1)).addEntityAuditEvent(
      eq(NegotiationAuditEvent.NEGOTIATION_RELOAD_MODEL),
      eq(id),
      eq(updatedNegotiation),
      eq(negotiation),
      anyOrNull()
    )
  }

  @Test
  fun `can save a negotiation if db exception happens`() {

    val negotiation = Negotiation.empty(1)
    whenever(negotiationRepository.save(negotiation)).thenThrow(DbActionExecutionException::class.java)
    whenever(negotiationRepository.resolveInsertException()).thenReturn(1)

    assertThrows<DbActionExecutionException> {
      negotiationService.trySaveNegotiation(negotiation)
    }
  }

  @Test
  fun `can update a negotiation`() {
    val email = "backend@pactum.com"
    val token = "avalidtoken"
    val negId = 1L
    val clientId = 1L
    val statuses = mapOf("ONGOING" to NegotiationStatusField("On going"))
    val client = Client(
      id = clientId,
      tag = "tag",
      name = "name",
      negotiationStatusFields = jacksonObjectMapper().writeValueAsString(statuses)
    )

    val status = "ONGOING"
    val isVisibleSupplier = false
    val isVisibleClient = true
    val list = listOf(
      NegotiationField.ApiEntity(NegotiationFieldConfigType.MODEL, "attr1", "value1"),
      NegotiationField.ApiEntity(NegotiationFieldConfigType.OTHER, "attr2", "value2")
    )

    val negotiation = Negotiation.empty(negId).copy(clientId = clientId)
    val req = UpdateNegotiationReq(
      status = status,
      isVisibleSupplier = isVisibleSupplier,
      isVisibleClient = isVisibleClient,
      fields = list,
      comment = "comment"
    )
    val updatedNegotiation = negotiation.copy(
      status = status,
      isVisibleClient = isVisibleClient,
      isVisibleSupplier = isVisibleSupplier
    )

    SessionHelper.setLoggedInUser(token, email, listOf(Role.Admin))

    whenever(negotiationRepository.findById(negId)).thenReturn(Optional.of(negotiation))
    whenever(clientRepository.findById(clientId)).thenReturn(Optional.of(client))
    whenever(negotiationRepository.save(negotiation)).thenReturn(updatedNegotiation)

    val resp = negotiationService.updateNegotiation(negId, req)
    assertThat(resp.statusCode).isEqualTo(HttpStatus.OK)
    val body = resp.body as Negotiation.ApiEntity
    assertThat(body.id).isEqualTo(negId)
    assertThat(body.stateId).isEqualTo(negotiation.stateId)
  }

  @Test
  fun `can not update a negotiation if not found`() {
    val email = "backend@pactum.com"
    val token = "avalidtoken"
    val negId = 1L

    val status = "ONGOING"
    val isVisibleSupplier = false
    val isVisibleClient = true

    val req = UpdateNegotiationReq(status = status, isVisibleSupplier = isVisibleSupplier, isVisibleClient = isVisibleClient)

    SessionHelper.setLoggedInUser(token, email, listOf(Role.Admin))

    whenever(negotiationRepository.findById(negId)).thenReturn(Optional.empty())

    assertThrows<NegotiationIdNotFoundException> {
      negotiationService.updateNegotiation(negId, req)
    }
  }

  @Test
  fun `can update status of a negotiation`() {
    val email = "backend@pactum.com"
    val token = "avalidtoken"
    val negId = 1L
    val clientId = 1L
    val statuses = mapOf("ONGOING" to NegotiationStatusField("On going"))
    val client = Client(
      id = clientId,
      tag = "tag",
      name = "name",
      negotiationStatusFields = jacksonObjectMapper().writeValueAsString(statuses)
    )

    val status = "ONGOING"
    val comment = "comment"

    val negotiation = Negotiation.empty(negId).copy(clientId = clientId)
    val updatedNegotiation = negotiation.copy(status = status)

    SessionHelper.setLoggedInUser(token, email, listOf(Role.Admin))

    whenever(clientRepository.findById(clientId)).thenReturn(Optional.of(client))
    whenever(negotiationRepository.save(negotiation)).thenReturn(updatedNegotiation)

    val resp = negotiationService.updateNegotiationStatus(negotiation, status, comment)
    assertThat(resp.status).isEqualTo(status)
  }

  @Test
  fun `can update client visibility of a negotiation`() {
    val email = "backend@pactum.com"
    val token = "avalidtoken"
    val negId = 1L
    val isClientVisible = true
    val comment = "comment"

    val negotiation = Negotiation.empty(negId).copy()
    val updatedNegotiation = negotiation.copy(isVisibleClient = isClientVisible)

    SessionHelper.setLoggedInUser(token, email, listOf(Role.Admin))

    whenever(negotiationRepository.save(negotiation)).thenReturn(updatedNegotiation)

    val resp = negotiationService.updateNegotiationClientVisibility(negotiation, isClientVisible, comment)
    assertThat(resp.isVisibleClient).isEqualTo(isClientVisible)
  }

  @Test
  fun `can update supplier visibility of a negotiation`() {
    val email = "backend@pactum.com"
    val token = "avalidtoken"
    val negId = 1L
    val isSupplierVisible = true
    val comment = "comment"

    val negotiation = Negotiation.empty(negId).copy()
    val updatedNegotiation = negotiation.copy(isVisibleSupplier = isSupplierVisible)

    SessionHelper.setLoggedInUser(token, email, listOf(Role.Admin))

    whenever(negotiationRepository.save(negotiation)).thenReturn(updatedNegotiation)

    val resp = negotiationService.updateNegotiationSupplierVisibility(negotiation, isSupplierVisible, comment)
    assertThat(resp.isVisibleSupplier).isEqualTo(isSupplierVisible)
  }
}
