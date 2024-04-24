package com.pactum.negotiation.summary

import com.pactum.negotiation.summary.model.NegotiationsSummary
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@RestController
@Api(tags = ["admin"], description = "APIs consumed by the Admin panel")
class NegotiationSummaryController(
  private val negotiationSummaryService: NegotiationSummaryService
) {

  @ApiOperation(value = "Get negotiations summary for this client by admin")
  @GetMapping("/api/v1/negotiation/summary/{clientId}")
  fun getNegotiationsSummary(@PathVariable clientId: Long): NegotiationsSummary {
    return negotiationSummaryService.getNegotiationsSummaryByClientId(clientId)
  }

  @ApiOperation(value = "Get negotiations summary by client")
  @GetMapping("/api/v1/negotiation/summary/client")
  fun getNegotiationsSummary(): NegotiationsSummary {
    return negotiationSummaryService.getNegotiationsSummaryForClient()
  }
}
