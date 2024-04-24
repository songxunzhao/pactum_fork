package com.pactum.negotiation

import com.pactum.api.GenericCreatedResponse
import com.pactum.api.GenericNoContentResponse
import com.pactum.api.GenericOkResponse
import com.pactum.negotiation.model.CreateNegotiationReq
import com.pactum.negotiation.model.UpdateNegotiationReq
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PutMapping

@RestController
@Api(tags = ["admin"], description = "APIs consumed by the Admin panel")
class NegotiationController(
  private val negotiationService: NegotiationService
) {

  @ApiOperation(value = "Create a new negotiation")
  @PostMapping("/api/v1/negotiation")
  fun createNegotiations(@RequestBody req: CreateNegotiationReq): GenericCreatedResponse {
    return negotiationService.createNegotiations(req)
  }

  @ApiOperation(value = "Get list of negotiations for this client by admin")
  @GetMapping("/api/v1/negotiation/{clientId}")
  fun getNegotiations(@PathVariable clientId: Long): List<Map<String, Any>> {
    return negotiationService.getNegotiationsByClientId(clientId)
  }

  @ApiOperation(value = "Get list of negotiations by client")
  @GetMapping("/api/v1/negotiation/client")
  fun getNegotiations(): List<Map<String, Any>> {
    return negotiationService.getNegotiationsForClient()
  }

  @ApiOperation(value = "Delete a negotiation by id")
  @DeleteMapping("/api/v1/negotiation/{id}")
  fun deleteNegotiation(@PathVariable id: Long): GenericNoContentResponse {
    return negotiationService.deleteNegotiation(id)
  }

  @ApiOperation(value = "Update a negotiation by id")
  @PutMapping("/api/v1/negotiation/{id}")
  fun updateNegotiation(@RequestBody req: UpdateNegotiationReq, @PathVariable id: Long): GenericOkResponse {
    return negotiationService.updateNegotiation(id, req)
  }
}
