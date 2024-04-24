package com.pactum.extract

import com.pactum.chat.model.NegotiationHistoryItem
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
@Api(tags = ["tools"], description = "APIs consumed by humans and scripts")
class ExtractController(
  private val extractService: ExtractService
) {

  @ApiOperation(value = "Get the list of chat terms by one or more chat IDs (comma separated)")
  @GetMapping("/api/v1/extract/terms/flowIds/{flowIds}")
  fun getChatsTermsByFlowIds(
    @ApiParam(
      name = "flowIds",
      value = "One or many chat IDs (comma separated)",
      example = "1pKp6Xd8rq_oZJtaKcZ_IUVFARVwE9XTj"
    )
    @PathVariable flowIds: String
  ): List<Map<String, Any?>> {
    return extractService.getChatsTermsByFlowIds(flowIds)
  }

  @ApiOperation(value = "Get the list of chat terms by one or more state IDs (comma separated)")
  @GetMapping("/api/v1/extract/terms/stateIds/{stateIds}")
  fun getChatsTermsByStateIds(
    @ApiParam(
      name = "stateIds",
      value = "One or many state IDs (comma separated)",
      example = "1pK064NJWTj,1pK092QVMTj"
    )
    @PathVariable stateIds: String
  ): List<Map<String, Any?>> {
    return extractService.getChatsTermsByStateIds(stateIds)
  }

  @ApiOperation(value = "Get all chat steps in chronological order, by one or more chat ID (comma separated)")
  @GetMapping("/api/v1/extract/history/flowIds/{flowIds}")
  fun getChatsHistoryByFlowIds(
    @ApiParam(
      name = "flowIds",
      value = "One or many chat IDs (comma separated)",
      example = "1pKp6Xd8rq_oZJtaKcZ_IUVFARVwE9XTj"
    )
    @PathVariable flowIds: String
  ): List<NegotiationHistoryItem> {
    return extractService.getChatsHistoryByFlowIds(flowIds)
  }

  @ApiOperation(value = "Get all chat steps in chronological order, by one or more state ID (comma separated)")
  @GetMapping("/api/v1/extract/history/stateIds/{stateIds}")
  @ApiParam(
    name = "stateIds",
    value = "One or many state IDs (comma separated)",
    example = "1pK064NJWTj,1pK092QVMTj"
  )
  fun getChatsHistoryByStateIds(
    @PathVariable stateIds: String
  ): List<NegotiationHistoryItem> {
    return extractService.getChatsHistoryByStateIds(stateIds)
  }
}
