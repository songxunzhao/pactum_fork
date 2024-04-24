package com.pactum.chat.mindmup

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class MindMupResponseFixture {
  companion object {
    private val objectMapper = jacksonObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    fun mindMupResponseFixture(): MindMupResponse {
      val jsonResponse = this::class.java.getResource("/1Simple.mup").readText()
      return objectMapper.readValue(jsonResponse, MindMupResponse::class.java)
    }

    fun mindMupUpdateExistingVariablesFixture(): MindMupResponse {
      val jsonResponse = this::class.java.getResource("/8UpdatingExistingVariables.mup").readText()
      return objectMapper.readValue(jsonResponse, MindMupResponse::class.java)
    }

    fun mindMupConditionalsResponseFixture(): MindMupResponse {
      val jsonResponse = this::class.java.getResource("/7BackwardLinksWithConditionals.mup").readText()
      return objectMapper.readValue(jsonResponse, MindMupResponse::class.java)
    }

    fun mindMupKotlinEvalExpressionFixture(): MindMupResponse {
      val jsonResponse = this::class.java.getResource("/28EvaluateNotesAsKotlin.mup").readText()
      return objectMapper.readValue(jsonResponse, MindMupResponse::class.java)
    }

    fun mindMupMultipleModelValuesResponseFixture(): MindMupResponse {
      val jsonResponse = this::class.java.getResource("/model/mindmup/1MultipleModelValues.mup").readText()
      return objectMapper.readValue(jsonResponse, MindMupResponse::class.java)
    }

    fun mindMupModelInFormulaResponseFixture(): MindMupResponse {
      val jsonResponse = this::class.java.getResource("/model/mindmup/3ModelInFormula.mup").readText()
      return objectMapper.readValue(jsonResponse, MindMupResponse::class.java)
    }

    fun mindMupInvalidModelValueResponseFixture(): MindMupResponse {
      val jsonResponse = this::class.java.getResource("/model/mindmup/2InvalidModelValue.mup").readText()
      return objectMapper.readValue(jsonResponse, MindMupResponse::class.java)
    }

    fun mindMupConditionalTextStepFixture(): MindMupResponse {
      val jsonResponse = this::class.java.getResource("/29ConditionalTextStep.mup").readText()
      return objectMapper.readValue(jsonResponse, MindMupResponse::class.java)
    }

    fun mindMupAssignLocalVariableFixture(): MindMupResponse {
      val jsonResponse = this::class.java.getResource("/30NestedPreviousVariables.mup").readText()
      return objectMapper.readValue(jsonResponse, MindMupResponse::class.java)
    }

    fun mindMupJSONValuesInMultipleChoiceFixture(): MindMupResponse {
      val jsonResponse = this::class.java.getResource("/23JSONValuesInMultipleChoiceComponent.mup").readText()
      return objectMapper.readValue(jsonResponse, MindMupResponse::class.java)
    }
  }
}
