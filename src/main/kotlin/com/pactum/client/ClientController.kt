package com.pactum.client

import com.pactum.api.GenericCreatedResponse
import com.pactum.api.GenericNoContentResponse
import com.pactum.client.model.Client
import com.pactum.client.model.ClientConfig
import com.pactum.client.model.CreateClientReq
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@Api(tags = ["admin"], description = "APIs consumed by the Admin panel")
class ClientController(
  private val clientService: ClientService
) {

  @ApiOperation(value = "Create a new client")
  @PostMapping("/api/v1/client")
  fun createClient(@RequestBody req: CreateClientReq): GenericCreatedResponse {
    return clientService.createClient(req)
  }

  @ApiOperation(value = "Get list of clients")
  @GetMapping("/api/v1/client")
  fun getClients(): List<Client.ApiEntity> {
    return clientService.getClients()
  }

  @ApiOperation(value = "Get client's config")
  @GetMapping("/api/v1/client/{id}/config")
  fun getClientConfig(@PathVariable id: Long): ClientConfig {
    return clientService.getClientConfig(id)
  }

  @ApiOperation(value = "Get active client's config")
  @GetMapping("/api/v1/client/config")
  fun getClientConfig(): ClientConfig {
    return clientService.getActiveClient().getConfig()
  }

  @ApiOperation(value = "Delete client by id")
  @DeleteMapping("/api/v1/client/{id}")
  fun deleteClient(@PathVariable id: Long): GenericNoContentResponse {
    return clientService.deleteClient(id)
  }
}
