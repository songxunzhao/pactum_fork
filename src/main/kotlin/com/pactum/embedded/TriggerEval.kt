package com.pactum.embedded
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.pactum.chat.mindmup.State
import com.pactum.chat.model.ValueHolder
import com.pactum.chat.InvalidTriggerException
import javax.script.ScriptException
import org.graalvm.polyglot.Context

interface TriggerEval {
  fun eval(state: State, trigger: String): Boolean
}

class KotlinTriggerEval : TriggerEval {

  val script = KotlinScript()

  override fun eval(state: State, trigger: String): Boolean {
    val context = ArrayList<ContextValue>()
    val currentStep = state.currentStep
    val steps = state.generateStepsById()

    if (currentStep is ValueHolder && currentStep.value != null) {
      context.add(ContextValue("value", "Any", currentStep.value!!))
    }
    context.add(ContextValue("steps", "Map<String, com.pactum.chat.model.ValueHolder>", steps))
    return try {
      script.evalInContext(context, trigger) as Boolean
    } catch (e: ScriptException) {
      throw InvalidTriggerException("Invalid trigger: $trigger.\n Failed with error: ${e.message} ")
    }
  }
}

class JavaScriptTriggerEval : TriggerEval {

  override fun eval(state: State, trigger: String): Boolean {
    val stateJson = jacksonObjectMapper().writeValueAsString(state)
    val configExpression = """
        var state = JSON.parse(stateJson);
        var currentStep = state.currentStep;
        var value = currentStep.value;
        var steps = {};
        for(let step of state.renderedSteps) {
          if(step.variable != null) {
            steps[step.variable] = step;
          } else {
            steps[step.id] = step;
          }
          
        }
      """.trimIndent()

    return try {
      with(
        Context.newBuilder().build(),
        {
          this.getBindings("js").putMember("stateJson", stateJson)
          this.eval("js", configExpression + trigger).asBoolean()
        }
      )
    } catch (e: ScriptException) {
      throw InvalidTriggerException("Invalid trigger: $trigger.\n Failed with error: ${e.message} ")
    }
  }
}
