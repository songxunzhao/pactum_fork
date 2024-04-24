package com.pactum.model

import com.pactum.chat.model.ChatApiInput
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@Api(tags = ["chat"], description = "APIs consumed by the chat application")
class ModelController(
  private val modelService: ModelService
) {

  @ApiOperation(value = "Get chat parameters, i.e. model file content for a chat")
  @GetMapping("/api/v1/models/{modelId}/modelKey/{modelKey}/states/{stateId}/chatParams")
  fun getChatParams(
    @PathVariable modelId: String,
    @PathVariable modelKey: String,
    @PathVariable stateId: String,
    @RequestParam(required = false, name = "readOnly") isReadOnly: Boolean = false
  ): Map<String, Any> {
    return modelService.getChatParams(
      ChatApiInput(modelId = modelId, modelKey = modelKey, readOnly = isReadOnly, stateId = stateId)
    )
  }

  @ApiOperation(value = "Get chat parameters, i.e. model file content for a chat by state id")
  @GetMapping("/api/v1/models/states/{stateId}/chatParams")
  fun getChatParamsByStateId(
    @PathVariable stateId: String,
    @RequestParam(required = false, name = "readOnly") isReadOnly: Boolean = false
  ): Map<String, Any> {
    return modelService.getChatParamsByStateId(
      ChatApiInput(readOnly = isReadOnly, stateId = stateId, shouldCreateNegotiationIfNotFound = false)
    )
  }
}
