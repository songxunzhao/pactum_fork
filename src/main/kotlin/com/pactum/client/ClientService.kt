package com.pactum.client

import com.pactum.api.GenericCreatedResponse
import com.pactum.api.GenericNoContentResponse
import com.pactum.audit.AuditEventService
import com.pactum.audit.model.EntityAuditEvent
import com.pactum.auth.AccessDeniedException
import com.pactum.auth.SessionHelper
import com.pactum.client.model.Client
import com.pactum.client.model.ClientConfig
import com.pactum.client.model.CreateClientReq
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class ClientService(
  private val clientRepository: ClientRepository,
  private val auditEventService: AuditEventService,
  @Value("\${chat.pactumClientTag}") private val pactumClientTag: String
) {

  private val clientTagsById: MutableMap<Long, String> = mutableMapOf()

  fun createClient(req: CreateClientReq): GenericCreatedResponse {

    if (clientRepository.findFirstByTag(req.tag) != null) {
      throw ClientExistsException(req.tag)
    }

    val savedClient = clientRepository.save(Client.create(req.tag, req.name))
    auditEventService.addEntityAuditEvent(
      ClientAuditEventType.CLIENT_CREATED,
      savedClient.id!!,
      savedClient
    )

    return GenericCreatedResponse(savedClient.toApiEntity())
  }

  fun getClients(): List<Client.ApiEntity> {
    return clientRepository.findByTagNot(pactumClientTag).map { it.toApiEntity() }
  }

  fun getActiveClient(): Client {
    val clientTag = SessionHelper.getLoggedInUserClientTag() ?: throw AccessDeniedException()
    return clientRepository.findFirstByTag(clientTag) ?: throw AccessDeniedException()
  }

  fun getClientConfig(id: Long): ClientConfig {
    val client = clientRepository.findById(id)
    if (client.isPresent) {
      return client.get().getConfig()
    }
    throw ClientIdNotFoundException(id)
  }

  fun deleteClient(id: Long): GenericNoContentResponse {
    val client = clientRepository.findById(id)
    if (client.isPresent) {
      clientRepository.delete(client.get())
      auditEventService.addEntityAuditEvent(ClientAuditEventType.CLIENT_DELETED, id, client.get())
    }
    return GenericNoContentResponse()
  }

  fun getTagById(clientId: Long): String? {
    if (!clientTagsById.containsKey(clientId)) {
      val client = clientRepository.findById(clientId)
      if (client.isPresent) {
        clientTagsById[clientId] = client.get().tag
      }
    }
    return clientTagsById.getOrDefault(clientId, null)
  }

  enum class ClientAuditEventType : EntityAuditEvent {
    CLIENT_CREATED {
      override fun getDescription(): String = "Created a new client"
    },
    CLIENT_DELETED {
      override fun getDescription(): String = "Deleted a client"
    };

    override fun getEntityType(): String = "CLIENT"
  }
}
