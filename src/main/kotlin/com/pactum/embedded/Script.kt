package com.pactum.embedded
import javax.script.ScriptContext
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

interface Script {
  fun evalInContext(context: Collection<ContextValue>, expression: String): Any?
}

enum class ScriptEngine {
  KTS,
  JS
}

class ScriptEngineProvider {

  companion object {
    private val kts = ScriptEngineManager().getEngineByExtension("kts")

    @JvmStatic
    fun getKts(): ScriptEngine {
      return kts
    }
  }
}

class KotlinScript : Script {

  override fun evalInContext(context: Collection<ContextValue>, expression: String): Any? {
    with(ScriptEngineProvider.getKts()) {
      val trimBindingExpression = StringBuilder()
      getBindings(ScriptContext.ENGINE_SCOPE).apply {
        for (value in context) {
          val valueName = value.name
          val valueType = value.asType
          val valueData = value.data

          trimBindingExpression.append("val $valueName = bindings[\"$valueName\"] as $valueType").append("\n")
          put(valueName, valueData)
        }
      }
      eval(trimBindingExpression.toString())
      return eval(expression)
    }
  }
}
