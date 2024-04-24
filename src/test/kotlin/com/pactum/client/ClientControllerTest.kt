package com.pactum.client

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.pactum.api.GenericCreatedResponse
import com.pactum.api.GenericNoContentResponse
import com.pactum.client.model.Client
import com.pactum.client.model.ClientConfig
import com.pactum.client.model.CreateClientReq
import com.pactum.token.TokenService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.ComponentScan.Filter
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(ClientController::class, excludeFilters = [Filter(EnableWebSecurity::class)])
class ClientControllerTest {

  @Autowired
  lateinit var mockMvc: MockMvc

  @MockBean
  lateinit var clientService: ClientService

  @MockBean
  lateinit var tokenService: TokenService

  @Test
  @WithMockUser
  fun `can create new client`() {

    val tag = "tag"
    val name = "name"
    val req = CreateClientReq(tag, name)
    val client = Client(tag = tag, name = name).toApiEntity()

    whenever(clientService.createClient(req)).thenReturn(GenericCreatedResponse(client))

    mockMvc.perform(
      post("/api/v1/client")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jacksonObjectMapper().writeValueAsString(req))
        .characterEncoding("utf-8")
        .with(SecurityMockMvcRequestPostProcessors.csrf())
    )
      .andExpect(status().isCreated)
      .andExpect(jsonPath("$.tag").value(tag))
      .andExpect(jsonPath("$.name").value(name))
  }

  @Test
  @WithMockUser
  fun `can get list of clients`() {

    val tag = "tag"
    val name = "name"
    val client = Client.ApiEntity(1, tag, name)
    val list = listOf(client)

    whenever(clientService.getClients()).thenReturn(list)

    mockMvc.perform(get("/api/v1/client"))
      .andExpect(status().isOk)
      .andExpect(jsonPath("$[0].id").value(1))
      .andExpect(jsonPath("$[0].name").value(name))
      .andExpect(jsonPath("$[0].tag").value(tag))
  }

  @Test
  @WithMockUser
  fun `can get client's config`() {

    val id = 123L

    val clientConfig = mock<ClientConfig>()
    whenever(clientService.getClientConfig(id)).thenReturn(clientConfig)

    mockMvc.perform(get("/api/v1/client/$id/config"))
      .andExpect(status().isOk)
      .andExpect(content().string(jacksonObjectMapper().writeValueAsString(clientConfig)))

    verify(clientService).getClientConfig(id)
  }

  @Test
  @WithMockUser
  fun `can get active client's config`() {
    val client: Client = mock {
      on { getConfig() }.thenReturn(mock())
    }
    whenever(clientService.getActiveClient()).thenReturn(client)

    mockMvc.perform(get("/api/v1/client/config"))
      .andExpect(status().isOk)
      .andExpect(content().string(jacksonObjectMapper().writeValueAsString(client.getConfig())))

    verify(clientService).getActiveClient()
  }

  @Test
  @WithMockUser
  fun `can delete client by id`() {
    val id = 0L

    whenever(clientService.deleteClient(id)).thenReturn(GenericNoContentResponse())

    mockMvc.perform(
      delete("/api/v1/client/$id")
        .with(SecurityMockMvcRequestPostProcessors.csrf())
    )
      .andExpect(status().isNoContent)
  }
}
