package com.pactum.utils

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.wnameless.json.flattener.JsonFlattener
import org.json.simple.JSONObject
import java.io.IOException

class JsonHelper {

  companion object {

    private fun getJson(json: String?): JsonNode? {
      if (json == null) {
        return null
      }
      return try {
        jacksonObjectMapper().readTree(json)
      } catch (e: IOException) {
        null
      }
    }

    fun isJsonObject(json: String?): Boolean {
      val obj = getJson(json) ?: return false
      return obj is ObjectNode
    }

    fun toJsonObject(json: String): JSONObject {
      return jacksonObjectMapper().readValue(json, JSONObject::class.java)
    }

    fun getObjectOrJson(item: Any?): Any? {
      return if (canSerializeToJson(item)) item else jsonOrNull(item)
    }

    fun canSerializeToJson(item: Any?): Boolean {
      return try {
        jacksonObjectMapper().canSerialize(item?.javaClass)
      } catch (e: Exception) {
        false
      }
    }

    private fun jsonOrNull(item: Any?): String? {
      val json = try {
        jacksonObjectMapper().writeValueAsString(item)
      } catch (e: Exception) {
        null
      }
      return if (json == null || json == "null") null else json
    }

    fun convertToMapOrEmpty(attr: String?): Map<String, Any> {
      return if (attr == null || attr == "null")
        emptyMap()
      else try {
        jacksonObjectMapper().readValue<Map<String, Any>>(attr)
      } catch (e: Exception) {
        emptyMap<String, Any>()
      }
    }

    fun flattenAsMap(items: Any?): Map<String, Any> {
      return when (items) {
        is String -> JsonFlattener.flattenAsMap(items)
        is Map<*, *> -> JsonFlattener.flattenAsMap(jacksonObjectMapper().writeValueAsString(items))
        else -> emptyMap()
      }
    }
  }
}
