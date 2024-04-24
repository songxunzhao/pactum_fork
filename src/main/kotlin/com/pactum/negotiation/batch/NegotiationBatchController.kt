package com.pactum.negotiation.batch

import com.pactum.api.GenericOkResponse
import com.pactum.negotiation.batch.model.BatchActionReq
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader

@RestController
@Api(tags = ["admin"], description = "APIs consumed by the Admin panel")
class NegotiationBatchController(
  private val negotiationBatchService: NegotiationBatchService
) {

  @ApiOperation(value = "Batch action on negotiations")
  @PostMapping("/api/v1/negotiation/batch-action")
  fun batchAction(
    @RequestHeader headers: Map<String, String>,
    @RequestBody req: BatchActionReq
  ): GenericOkResponse {
    return negotiationBatchService.batchAction(req, headers)
  }
}
