package com.pactum.utils

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.pactum.chat.model.ChatHolder
import com.pactum.negotiation.summary.model.ExtraValue
import com.pactum.negotiation.summary.model.ExtraValueFormat
import org.springframework.data.relational.core.conversion.DbActionExecutionException
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.Instant
import java.util.Locale
import kotlin.math.roundToLong

class Utils {

  companion object {

    private const val STATE_ID_LENGTH_PREFIX = 3
    private const val STATE_ID_LENGTH_SUFFIX = 2

    fun isValidStateId(stateId: String, flowId: String): Boolean {
      val flowIdLength = flowId.length
      val stateIdPrefix = flowId.substring(0, STATE_ID_LENGTH_PREFIX)
      val stateIdSuffix = flowId.substring(flowIdLength - STATE_ID_LENGTH_SUFFIX, flowIdLength)

      val oldStateIdRegex = """$stateIdPrefix\d[A-Z]$stateIdSuffix""".toRegex()
      val newStateIdRegex = """$stateIdPrefix\d{3}[A-Z]{3}$stateIdSuffix""".toRegex()
      val oldStateIdLength = STATE_ID_LENGTH_PREFIX + 2 + STATE_ID_LENGTH_SUFFIX

      val stateIdRegex = if (stateId.length == oldStateIdLength) oldStateIdRegex else newStateIdRegex

      return stateId.matches(stateIdRegex)
    }

    fun generateStateId(flowId: String): String {
      val flowIdLength = flowId.length
      val stateIdPrefix = flowId.substring(0, STATE_ID_LENGTH_PREFIX)
      val stateIdSuffix = flowId.substring(flowIdLength - STATE_ID_LENGTH_SUFFIX, flowIdLength)
      val digits = (1..3).map { ('0'..'9').random() }.joinToString("")
      val chars = (1..3).map { ('A'..'Z').random() }.joinToString("")
      return "$stateIdPrefix$digits$chars$stateIdSuffix"
    }

    fun generateChatLink(baseUrl: String, chatHolder: ChatHolder): String {
      var link = baseUrl
      chatHolder.modelId?.let {
        link += "/models/$it"
      }
      link += "/chat/${chatHolder.flowId}"
      chatHolder.modelKey?.let {
        link += "/$it"
      }
      link += "/${chatHolder.stateId}"
      link += if (chatHolder.readOnly) "/read-only" else ""
      return link
    }

    fun getExtraValue(label: String, format: ExtraValueFormat, value: Any?): ExtraValue {
      if (value == null) {
        return ExtraValue(label, null, ExtraValueFormat.EMPTY)
      }
      if (value is String && value.isBlank()) {
        return ExtraValue(label, null, ExtraValueFormat.EMPTY)
      }
      return when (format) {
        ExtraValueFormat.LINK -> ExtraValue(label, value as String, format)
        ExtraValueFormat.TIME -> ExtraValue(label, (value as Instant).toEpochMilli(), format)
        ExtraValueFormat.PERCENT -> ExtraValue(label, format(convertToNumber(value), ExtraValueFormat.PERCENT), format)
        ExtraValueFormat.CURRENCY -> ExtraValue(label, format(convertToNumber(value), ExtraValueFormat.CURRENCY), format)
        ExtraValueFormat.NUMBER -> ExtraValue(label, convertToNumber(value), format)
        ExtraValueFormat.BOOLEAN -> ExtraValue(label, value as Boolean, format)
        ExtraValueFormat.TEXT -> ExtraValue(label, "$value", format)
        else -> ExtraValue(label, null, ExtraValueFormat.EMPTY)
      }
    }

    fun round(value: Double): Double {
      return if (value.isNaN()) 0.0 else (value * 100.0).roundToLong() / 100.0
    }

    fun format(value: Number?, format: ExtraValueFormat): String {
      if (value == null) return ""
      return when (format) {
        ExtraValueFormat.PERCENT -> "${convertToNumber(value)}%"
        ExtraValueFormat.CURRENCY -> NumberFormat.getCurrencyInstance(Locale.US).format(value.toDouble())
        else -> value.toString()
      }
    }

    fun convertToNumber(any: Any?): Number? {
      if (any == null) return null
      if (any is Number) return any
      return when (any) {
        is String -> BigDecimal(any.replace("[^0-9.]".toRegex(), ""))
        else -> null
      }
    }

    fun toDouble(any: Any?): Double? {
      return try {
        convertToNumber(any)?.toDouble()
      } catch (e: NumberFormatException) {
        null
      }
    }

    fun isDefaultFlowId(flowId: String, defaultFlowId: String): Boolean {
      return flowId == defaultFlowId
    }

    fun isSecretStateId(stateId: String, secretStateId: String): Boolean {
      return stateId.contains(secretStateId)
    }

    fun isDbInsertException(e: Exception): Boolean {
      return e is DbActionExecutionException
    }

    inline fun <reified T : Any> cast(any: Any): T {
      return jacksonObjectMapper().convertValue(any, object : TypeReference<T>() {})
    }
  }
}
