package com.pactum.chat

import com.pactum.chat.model.BaseStep
import com.pactum.chat.model.ChatApiInput
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView

private val logger = KotlinLogging.logger {}

@RestController
@Api(tags = ["chat"], description = "APIs consumed by the chat application")
class ChatController(
  private val chatService: ChatService,
  @Value("\${chat.secretStateId}") private val secretStateId: String
) {

  @ApiOperation(value = "Get the next chat step")
  @GetMapping("/api/v1/chats/{flowId}/models/{modelId}/modelKey/{modelKey}/states/{stateId}/nextStep")
  fun getChatStep(
    @PathVariable flowId: String,
    @PathVariable modelId: String,
    @PathVariable modelKey: String,
    @PathVariable stateId: String,
    @RequestParam stepId: String?,
    @RequestParam(required = false) value: String?,
    @RequestParam(name = "readOnly") isReadOnly: Boolean,
    @RequestHeader sessionId: String
  ): List<BaseStep> {
    return chatService.getChatStep(
      ChatApiInput(
        flowId = flowId,
        modelId = modelId,
        modelKey = modelKey,
        stateId = ChatApiInput.createNewStateId(stateId, sessionId, secretStateId),
        stepId = stepId,
        value = value,
        readOnly = isReadOnly
      )
    )
  }

  @ApiOperation(value = "Get the next chat step by stateId")
  @GetMapping("/api/v1/chats/states/{stateId}/nextStep")
  fun getChatStepByStateId(
    @PathVariable stateId: String,
    @RequestParam stepId: String?,
    @RequestParam(required = false) value: String?,
    @RequestParam(name = "readOnly") isReadOnly: Boolean
  ): List<BaseStep> {
    return chatService.getChatStepByStateId(
      ChatApiInput(
        stateId = stateId,
        stepId = stepId,
        value = value,
        readOnly = isReadOnly,
        shouldCreateNegotiationIfNotFound = false
      )
    )
  }

  @ApiOperation(value = "Redirect to a DocuSign URL")
  @GetMapping("/api/v1/chats/states/{stateId}/contract-signing-url")
  fun getContractSigningURL(
    @PathVariable stateId: String
  ): RedirectView {
    return RedirectView(chatService.getContractSigningUrl(stateId))
  }

  @ApiOperation(value = "Download the contract PDF")
  @GetMapping("/api/v1/chats/states/{stateId}/contract-download")
  fun downloadContract(
    @PathVariable stateId: String
  ): String {
    return chatService.getDemoContractHTML(stateId)
  }
}
