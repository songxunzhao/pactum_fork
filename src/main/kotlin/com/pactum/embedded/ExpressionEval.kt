package com.pactum.embedded

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.pactum.chat.mindmup.State
import com.pactum.chat.model.BaseStep
import com.pactum.chat.model.StepIDGenerator
import com.pactum.chat.model.ValueStep
import com.pactum.chat.InvalidEvalExpressionException
import mu.KotlinLogging
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.Value
import java.io.ByteArrayOutputStream
import javax.script.ScriptException
import kotlin.collections.HashMap
import kotlin.collections.arrayListOf
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator

private val logger = KotlinLogging.logger {}

interface ExpressionEval {
  var state: State?
  fun eval(expression: String): ArrayList<BaseStep>
}

class KotlinExpressionEval : ExpressionEval {

  override var state: State? = null
  val script = KotlinScript()

  override fun eval(expression: String): ArrayList<BaseStep> {
    val values = HashMap<String, Any>()
    val context = arrayListOf(ContextValue("values", "HashMap<String, Any>", values))

    if (state != null) {
      val previousValues = state!!.generateStepValuesById()
      context.add(ContextValue("previousValues", "HashMap<String, Any>", previousValues))
    }

    return try {
      script.evalInContext(context, expression)
      val newSteps = ArrayList<BaseStep>()
      for ((variable, value) in values) {
        newSteps.add(
          ValueStep(
            id = StepIDGenerator.idFrom(variable),
            variable = variable,
            value = value
          )
        )
      }
      newSteps
    } catch (e: ScriptException) {
      throw InvalidEvalExpressionException("Invalid eval expression: $expression.\n Failed with error: ${e.message}")
    }
  }
}

class JavaScriptExpressionEval : ExpressionEval {

  override var state: State? = null

  override fun eval(expression: String): ArrayList<BaseStep> {
    val stateJson = jacksonObjectMapper().writeValueAsString(state)
    val configExpression = """
      var values = {};
      var previousValues = {};
      var state = JSON.parse(stateJson);
      if(state != null) {
        //console.log('Previous values', state, Object.getOwnPropertyNames(state));
        for(let step of state.renderedSteps) {
          if(step.value != null) {
            if(step.variable != null) {
              previousValues[step.variable] = step.value;
            } else {
              previousValues[step.id] = step.value;
            }
          }
        }
      }
    """.trimIndent()

    return try {
      val outputStream = ByteArrayOutputStream()
      val context = Context.newBuilder().out(outputStream).build()
      with(
        context,
        {
          this.getBindings("js").putMember("stateJson", stateJson)
          this.eval("js", configExpression + expression)

          val values = convert(this.eval("js", "values")) as? Map<*, *>
          val newSteps = ArrayList<BaseStep>()

          if (values != null) {
            for ((variable, value) in values) {
              newSteps.add(
                ValueStep(
                  id = StepIDGenerator.idFrom(variable as String),
                  variable = variable,
                  value = value
                )
              )
            }
          }
          logger.info(outputStream.toString())
          outputStream.flush()
          outputStream.close()
          newSteps
        }
      )
    } catch (e: ScriptException) {
      throw InvalidEvalExpressionException("Invalid eval expression: $expression.\n Failed with error: ${e.message}")
    }
  }

  private fun convert(original: Value): Any? {
    return if (original.hasMembers()) {
      when {
        original.hasArrayElements() -> {
          val listResult: MutableList<Any?> = ArrayList()
          val length = original.arraySize
          for (i in 0 until length) {
            listResult.add(convert(original.getArrayElement(i)))
          }
          listResult
        }
        original.canExecute() -> {
          null
        }
        else -> {
          val mapResult: MutableMap<String, Any?> = LinkedHashMap()
          for (key in original.memberKeys) {
            mapResult[key] = convert(original.getMember(key))
          }
          mapResult
        }
      }
    } else {
      original.`as`(Object::class.java)
    }
  }
}
