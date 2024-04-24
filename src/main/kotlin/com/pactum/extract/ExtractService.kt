package com.pactum.extract

import com.pactum.negotiationstate.NegotiationStateRepository
import com.pactum.chat.model.NegotiationHistoryItem
import com.pactum.chat.model.ChatApiInput
import com.pactum.chat.model.NegotiationState
import com.pactum.negotiation.NegotiationModelTermService
import com.pactum.negotiation.NegotiationRepository
import com.pactum.negotiation.model.Negotiation
import com.pactum.utils.Utils
import org.jetbrains.kotlin.backend.common.push
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

@Service
class ExtractService(
  private val negotiationStateRepository: NegotiationStateRepository,
  private val negotiationRepository: NegotiationRepository,
  private val negotiationModelTermService: NegotiationModelTermService,
  @Value("\${chat.secretStateId}") private val secretStateId: String,
  @Value("\${server.baseUrl}") val baseUrl: String
) {

  fun getChatsTermsByFlowIds(flowIds: String): List<Map<String, Any?>> {

    val list = flowIds.split(",").toList()
    return negotiationRepository.findByFlowIds(list).map { getTerms(it) }
  }

  fun getChatsTermsByStateIds(stateIds: String): List<Map<String, Any?>> {

    val list = stateIds.split(",").toList()
    return negotiationRepository.findByStateIds(list).map { getTerms(it) }
  }

  fun getTerms(negotiation: Negotiation): Map<String, Any?> {

    val map = mutableMapOf<String, Any?>()
    map["stateId"] = negotiation.stateId
    map["flowId"] = negotiation.flowId
    map["modelId"] = negotiation.modelId
    map["modelKey"] = negotiation.modelKey
    map["chatStartTime"] = negotiation.chatStartTime
    map["chatLastUpdateTime"] = negotiation.chatUpdateTime
    val chatHolder = ChatApiInput(
      flowId = negotiation.flowId,
      modelId = negotiation.modelId,
      modelKey = negotiation.modelKey,
      stateId = negotiation.stateId,
      readOnly = true
    )
    map["chatLink"] = Utils.generateChatLink(baseUrl, chatHolder)
    val duration = if (negotiation.chatStartTime != null && negotiation.chatUpdateTime != null) {
      val durationMin = negotiation.chatStartTime.until(negotiation.chatUpdateTime, ChronoUnit.SECONDS) / 60.0
      (durationMin * 100.0).roundToInt() / 100.0
    } else {
      null
    }
    map["duration"] = duration
    map += negotiationModelTermService.getTerms(negotiation)

    return map
  }

  fun getChatsHistoryByFlowIds(flowIds: String): List<NegotiationHistoryItem> {

    val list = mutableListOf<NegotiationHistoryItem>()
    flowIds
      .split(",")
      .map { flowId ->
        val chatStates = negotiationStateRepository.getStatesByFlowId(flowId, secretStateId)
          .groupBy { it.stateId }
        list.addAll(getHistory(chatStates, flowId))
      }
    return list
  }

  fun getChatsHistoryByStateIds(stateIds: String): List<NegotiationHistoryItem> {

    val list = mutableListOf<NegotiationHistoryItem>()
    stateIds
      .split(",")
      .map { stateId ->
        val chatStates = negotiationStateRepository.getStatesByStateId(stateId, secretStateId)
          .groupBy { it.stateId }
        list.addAll(getHistory(chatStates, null))
      }
    return list
  }

  private fun getHistory(negotiationStates: Map<String, List<NegotiationState>>, flowId: String?): List<NegotiationHistoryItem> {

    val list = mutableListOf<NegotiationHistoryItem>()
    negotiationStates.forEach {
      var prevTime: Instant? = null
      for (state in it.value) {
        val newFlowId = flowId ?: negotiationRepository.findFirstByStateIdAndIsDeletedIsFalseOrderByCreateTimeDesc(
          state.stateId
        )?.flowId ?: ""
        val entry = NegotiationHistoryItem.createFromNegotiationState(state, newFlowId)
        prevTime?.let {
          entry.timeSincePreviousSec = Duration.between(prevTime, entry.time).toSeconds()
        }
        list.push(entry)
        prevTime = entry.time
      }
    }
    return list
  }
}
