package com.pactum.chat.mindmup
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo.As
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id
import com.pactum.chat.model.BaseStep
import com.pactum.chat.model.Step
import com.pactum.chat.model.ValueHolder
import com.pactum.chat.model.ValueStep
import com.pactum.chat.model.VariableHolder
import com.pactum.chat.model.extractChatVariableName

data class State(
  @JsonTypeInfo(use = Id.MINIMAL_CLASS, include = As.PROPERTY, property = "@class")
  val previousStep: Step?,
  @JsonTypeInfo(use = Id.MINIMAL_CLASS, include = As.PROPERTY, property = "@class")
  val currentStep: Step,
  @JsonTypeInfo(use = Id.MINIMAL_CLASS, include = As.PROPERTY, property = "@class")
  val renderedSteps: List<BaseStep>
) {
  fun generateStepsById(): Map<String, BaseStep> {
    val steps = HashMap<String, BaseStep>()
    for (step in renderedSteps) {
      if (step is VariableHolder && step.variable != null) {
        steps[step.variable!!] = step
      } else {
        steps[step.id] = step
      }
    }

    steps[currentStep.id] = currentStep
    return steps
  }

  fun generateStepValuesById(): Map<String, Any> {
    val values = HashMap<String, Any>()

    for (step in renderedSteps) {
      if (step is ValueHolder && step.value != null) {
        if (step.variable != null) {
          values[step.variable!!] = step.value!!
        } else {
          values[step.id] = step.value!!
        }
      }
    }
    return values
  }

  @JsonIgnore
  fun getChatVariableValues(): Map<String, Any> {
    val chatVariables = mutableMapOf<String, Any>()
    for ((variable, value) in generateStepValuesById()) {
      chatVariables[extractChatVariableName(variable)] = value
    }
    return chatVariables
  }

  companion object {
    fun new(previousStep: Step?, currentStep: Step, renderedSteps: List<BaseStep>): State =
      State(previousStep = previousStep, currentStep = currentStep, renderedSteps = renderedSteps)
  }

  fun replaceCurrentStep(step: Step): State {
    val newRenderedSteps = renderedSteps.dropLast(1) + listOf(step)
    return this.copy(currentStep = step, renderedSteps = newRenderedSteps)
  }

  fun pushNewStep(steps: List<BaseStep>): State {
    val newRenderedSteps = renderedSteps + steps
    val newCurrentStep = steps.last() as Step
    return State(previousStep = currentStep, currentStep = newCurrentStep, renderedSteps = newRenderedSteps)
  }

  fun addValueStep(step: ValueStep): State {
    val newRenderedSteps = renderedSteps + listOf(step)
    return this.copy(renderedSteps = newRenderedSteps)
  }
}
