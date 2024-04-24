package com.pactum.config

import com.pactum.embedded.ExpressionEval
import com.pactum.embedded.JavaScriptExpressionEval
import com.pactum.embedded.KotlinExpressionEval
import com.pactum.embedded.ScriptEngine
import com.pactum.embedded.TriggerEval
import com.pactum.embedded.KotlinTriggerEval
import com.pactum.embedded.JavaScriptTriggerEval
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ScriptConfiguration(
  @Value("\${chat.scriptEngine}") val scriptEngine: ScriptEngine
) {

  @Bean
  fun resolveExpressionEval(): ExpressionEval {
    return when (scriptEngine) {
      ScriptEngine.KTS -> KotlinExpressionEval()
      ScriptEngine.JS -> JavaScriptExpressionEval()
    }
  }

  @Bean
  fun resolveTriggerEval(): TriggerEval {
    return when (scriptEngine) {
      ScriptEngine.KTS -> KotlinTriggerEval()
      ScriptEngine.JS -> JavaScriptTriggerEval()
    }
  }
}
