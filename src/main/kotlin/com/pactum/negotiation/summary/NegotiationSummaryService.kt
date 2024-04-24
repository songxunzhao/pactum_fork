package com.pactum.negotiation.summary

import com.pactum.auth.AccessDeniedException
import com.pactum.auth.model.Role
import com.pactum.negotiationstate.NegotiationStateRepository
import com.pactum.client.ClientRepository
import com.pactum.client.model.ExtraSummaryOperation
import com.pactum.extract.ExtractService
import com.pactum.negotiation.NegotiationRepository
import com.pactum.negotiation.model.Negotiation
import com.pactum.negotiation.summary.model.ExtraValue
import com.pactum.negotiation.summary.model.ExtraValueFormat
import com.pactum.negotiation.summary.model.NegotiationsSummary
import com.pactum.auth.SessionHelper
import com.pactum.utils.Utils
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class NegotiationSummaryService(
  private val negotiationRepository: NegotiationRepository,
  private val negotiationStateRepository: NegotiationStateRepository,
  @Value("\${server.baseUrl}") val baseUrl: String,
  private val clientRepository: ClientRepository,
  private val extractService: ExtractService
) {

  fun getNegotiationsSummaryByClientId(clientId: Long): NegotiationsSummary {
    val negotiations = negotiationRepository.findByClientIdAndIsDeletedIsFalseOrderByCreateTimeDesc(clientId)
      .filter { it.isVisibleSupplier }
    return getNegotiationsSummary(negotiations, clientId, Role.Admin)
  }

  fun getNegotiationsSummaryForClient(): NegotiationsSummary {
    val clientTag = SessionHelper.getLoggedInUserClientTag() ?: throw AccessDeniedException()
    val client = clientRepository.findFirstByTag(clientTag) ?: throw AccessDeniedException()
    val negotiations = negotiationRepository.findByClientIdAndIsDeletedIsFalseOrderByCreateTimeDesc(client.id!!)
      .filter { it.isVisibleSupplier }
    return getNegotiationsSummary(negotiations, client.id, Role.Client)
  }

  private fun getNegotiationsSummary(
    negotiations: List<Negotiation>,
    clientId: Long,
    role: Role
  ): NegotiationsSummary {
    val totalCount = negotiations.size
    var openedCount = 0
    var finishedCount = 0
    val extraList = mutableListOf<ExtraValue>()
    val client = clientRepository.findById(clientId)
    if (client.isPresent) {
      val negotiationSummaryList = client.get().getConfig().filterSummaryByRole(role)
      val termsMapList = mutableMapOf<String, Map<String, Any?>>()
      negotiations.forEach { negotiation ->
        val map = extractService.getChatsTermsByStateIds(negotiation.stateId)[0]
        termsMapList[negotiation.stateId] = map
        if (negotiationStateRepository.getStatesByStateId(negotiation.stateId).isNotEmpty())
          openedCount++
        // todo find a better way to calculate finished count
        val value = map["status"] as? String ?: ""
        if (value == "Agreement reached") {
          finishedCount++
        }
      }
      negotiationSummaryList.summary.forEach { summary ->
        var total = 0.0
        var count = 0
        for (negotiation in negotiations) {
          val termsMap = termsMapList[negotiation.stateId]
          val value = termsMap?.get(summary.key) ?: continue
          if (summary.type == ExtraValueFormat.PERCENT || summary.type == ExtraValueFormat.CURRENCY) {
            val double = Utils.toDouble(value) ?: continue
            total += double
          }
          count++
        }
        val result: Number = when (summary.operation) {
          ExtraSummaryOperation.COUNT -> count
          ExtraSummaryOperation.SUM -> Utils.round(total)
          ExtraSummaryOperation.AVE -> Utils.round(total / count)
        }
        extraList.add(ExtraValue(summary.label, Utils.format(result, summary.type), summary.type))
      }
    }
    return NegotiationsSummary(totalCount, openedCount, finishedCount, extraList)
  }
}
