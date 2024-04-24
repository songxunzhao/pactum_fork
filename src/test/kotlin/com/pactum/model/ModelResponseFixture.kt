package com.pactum.model

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.json.simple.JSONObject

class ModelResponseFixture {
  companion object {

    fun modelsFixture(file: String): String {
      return this::class.java.getResource("/model/$file.json").readText()
    }

    @Suppress("UNCHECKED_CAST")
    fun modelsResponseFixture(modelKey: String): Map<String, Any> {
      val objectMapper = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      val jsonResponse = this::class.java.getResource("/model/1Name.json").readText()
      val json = objectMapper.readValue(jsonResponse, JSONObject::class.java)
      val models = json["models"] as HashMap<String, Any>
      return models[modelKey] as HashMap<String, Any>
    }
  }
}
