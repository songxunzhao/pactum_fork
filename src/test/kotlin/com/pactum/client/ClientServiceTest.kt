package com.pactum.client

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.pactum.audit.AuditEventService
import com.pactum.auth.AccessDeniedException
import com.pactum.auth.SessionHelper
import com.pactum.auth.model.Role
import com.pactum.client.model.Client
import com.pactum.client.model.CreateClientReq
import com.pactum.test.TestClockHolder
import com.pactum.test.UnitTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.util.Optional

@UnitTest
class ClientServiceTest {

  private val clientRepository: ClientRepository = mock()
  private lateinit var auditEventService: AuditEventService
  private lateinit var clientService: ClientService

  @BeforeEach
  fun `set up`() {
    auditEventService = spy(AuditEventService(mock(), TestClockHolder.CLOCK))
    clientService = ClientService(
      clientRepository,
      auditEventService,
      "pactum"
    )
  }

  @Test
  fun `create new client`() {
    val tag = "tag"
    val name = "name"

    val req = CreateClientReq(tag, name)
    val client = Client.create(tag, name)
    val existingClient = Client(42, tag, name, null)
    val email = "backend@pactum.com"
    val token = "avalidtoken"

    SessionHelper.setLoggedInUser(token, email, listOf(Role.Admin))

    whenever(clientRepository.findFirstByTag(tag)).thenReturn(null)
    whenever(clientRepository.save(client)).thenReturn(existingClient)

    val foundClient = clientService.createClient(req)
    assertThat(foundClient).isNotNull

    verify(auditEventService, times(1)).addEntityAuditEvent(
      eq(ClientService.ClientAuditEventType.CLIENT_CREATED),
      eq(42L),
      eq(existingClient),
      anyOrNull(),
      anyOrNull()
    )
  }

  @Test
  fun `can not create duplicate client`() {
    val tag = "tag"
    val name = "name"

    val req = CreateClientReq(tag, name)
    val client = Client.create(tag, name)

    whenever(clientRepository.findFirstByTag(tag)).thenReturn(client)

    assertThrows<ClientExistsException> {
      clientService.createClient(req)
    }
  }

  @Test
  fun `get list of clients except pactum`() {
    val tag1 = "tag1"
    val name1 = "name1"
    val tag2 = "tag2"
    val name2 = "name2"
    val tag3 = "pactum"
    val name3 = "pactum"

    val client1 = Client.create(tag1, name1)
    val client2 = Client.create(tag2, name2)
    val client3 = Client.create(tag3, name3)
    val list = listOf(
      client1,
      client2
    )

    whenever(clientRepository.findByTagNot(tag3)).thenReturn(list)
    clientRepository.save(client1)
    clientRepository.save(client2)
    clientRepository.save(client3)

    val clients = clientService.getClients()
    assertThat(clients.size).isEqualTo(2)
    assertThat(clients[0].tag).isEqualTo(tag1)
    assertThat(clients[1].name).isEqualTo(name2)
  }

  @Test
  fun `can delete client by id`() {
    val email = "backend@pactum.com"
    val token = "avalidtoken"

    SessionHelper.setLoggedInUser(token, email, listOf(Role.Admin))

    val tag1 = "tag1"
    val name1 = "name1"
    val client1 = Client.create(tag1, name1)
    whenever(clientRepository.findById(any())).thenReturn(Optional.of(client1))

    Mockito.doNothing().`when`(clientRepository).delete(client1)
    val deletedClient = clientService.deleteClient(0)
    assertThat(deletedClient).isNotNull

    verify(auditEventService, times(1)).addEntityAuditEvent(
      eq(ClientService.ClientAuditEventType.CLIENT_DELETED),
      eq(0L),
      eq(client1),
      anyOrNull(),
      anyOrNull()
    )
  }

  @Test
  fun `can get client tag`() {
    val tag1 = "tag1"
    val name1 = "name1"

    val client1 = Client.create(tag1, name1)

    whenever(clientRepository.findById(any())).thenReturn(Optional.of(client1))
    val tag = clientService.getTagById(1)
    assertThat(tag).isEqualTo(tag1)
  }

  @Test
  fun `can get active client`() {
    SessionHelper.setLoggedInUser("validtoken", "email", listOf(Role.Client)).apply {
      clientTag = "client-tag"
    }

    val client: Client = mock()
    whenever(clientRepository.findFirstByTag("client-tag")).thenReturn(client)

    assertThat(clientService.getActiveClient()).isEqualTo(client)
  }

  @Test
  fun `can not get active client when logged in user clientTag not set`() {
    SessionHelper.setLoggedInUser("validtoken", "email", listOf(Role.Client)).apply {
      clientTag = null
    }

    assertThrows<AccessDeniedException> {
      clientService.getActiveClient()
    }
  }

  @Test
  fun `can get client's config`() {
    val id = 123L

    val client: Client = mock {
      on { getConfig() }.thenReturn(mock())
    }

    whenever(clientRepository.findById(id)).thenReturn(Optional.of(client))

    val result = clientService.getClientConfig(id)
    assertThat(result).isEqualTo(client.getConfig())
  }

  @Test
  fun `can not get client's config if client not found`() {

    val id = 123L

    whenever(clientRepository.findById(id)).thenReturn(Optional.empty())

    assertThrows<ClientIdNotFoundException> {
      clientService.getClientConfig(id)
    }
  }
}
