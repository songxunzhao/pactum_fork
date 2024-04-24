package com.pactum.negotiationasset

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
@Api(tags = ["tools"], description = "APIs consumed by humans and scripts")
class NegotiationAssetController(private val negotiationAssetService: NegotiationAssetService) {

  @ApiOperation(value = "Fetch a MindMup file by Google Drive ID")
  @GetMapping("/api/v1/negotiationasset/flow/{flowId}")
  fun getMindMupContent(@PathVariable flowId: String): String? {
    return negotiationAssetService.getChatFlow(flowId)
  }

  @ApiOperation(value = "Fetch a Model file by Google Drive ID")
  @GetMapping("/api/v1/negotiationasset/model/{modelId}")
  fun getModelContent(@PathVariable modelId: String): String? {
    return negotiationAssetService.getChatModel(modelId)
  }
}
