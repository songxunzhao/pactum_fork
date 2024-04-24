package com.pactum.chat.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_DEFAULT
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id.MINIMAL_CLASS
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.pactum.chat.ChoicesNotFoundException
import com.pactum.chat.VariableException
import com.pactum.chat.getValueUsingKeyPath
import com.pactum.chat.mindmup.State
import com.pactum.embedded.TriggerEval
import java.util.regex.Pattern
import java.util.UUID

data class Chat(
  @JsonTypeInfo(use = MINIMAL_CLASS, include = PROPERTY, property = "@class")
  val steps: List<Step>,
  val params: Map<String, Any>? = null
) {
  fun replaceModelVariables(model: Map<String, *>): Chat {
    return copy(steps = steps.map { step -> step.replaceModelVariables(model) })
  }

  fun getFirstStep(): Step? {
    return steps[0]
  }

  fun findStepById(id: String): Step? {
    return steps.find { step -> step.id == id }
  }

  fun findStepByVariable(variable: String): Step? {
    return steps.find {
      (if (it is VariableHolder) it.variable == variable else false)
    }
  }
}

class StepIDGenerator {
  companion object {
    fun idFrom(str: String): String {
      return UUID.nameUUIDFromBytes(str.toByteArray()).toString()
    }
    fun randomId(): String {
      return UUID.randomUUID().toString()
    }
  }
}

@JsonIgnoreProperties(ignoreUnknown = true)
interface BaseStep {
  val id: String

  fun extendStep(stepProperties: Map<String, *>): BaseStep
}

interface Step : BaseStep {
  val evalExpression: String?

  @get:JsonInclude(NON_DEFAULT)
  val end: Boolean

  fun replaceModelVariables(model: Map<String, *>): Step
}

interface TriggerStep : Step {
  val trigger: Any?
}

interface SimpleTriggerStep : TriggerStep {
  override val trigger: String?
}

interface SimpleTriggerConvertible {
  fun toSimpleTriggerStep(trigger: String?, props: Map<String, Any> = emptyMap()): SimpleTriggerStep
}

interface ConditionTriggerStep : TriggerStep, SimpleTriggerConvertible, Trimmable {
  override val trigger: Map<String, String>?

  fun nextStepId(state: State, triggerEval: TriggerEval): String? {
    trigger?.let { trigger ->
      for ((condition, triggerId) in trigger) {
        if (triggerEval.eval(state, condition)) return triggerId
      }
    }

    return null
  }
}

interface VariableHolder {
  val variable: String?
}

interface ValueHolder : VariableHolder {
  val value: Any?

  fun updateValue(value: Any?): ValueHolder
}

interface ValueInputable : VariableHolder {
  fun inputValueWithJson(json: String): Step
}

interface ChatVariableReplaceable {
  fun replaceChatVariables(variables: Map<String, *>): Step
}

interface Trimmable {
  fun trim(): Step
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class TextStep(
  override val id: String,
  override val variable: String? = null,
  override val evalExpression: String? = null,
  override val value: Any? = null,
  @get:JsonInclude(NON_DEFAULT)
  override val trigger: String? = null,
  override val end: Boolean = false,
  val message: String,
  val user: Boolean = false
) : Step, SimpleTriggerStep, ValueHolder, ChatVariableReplaceable, Trimmable {

  override fun updateValue(value: Any?): TextStep {
    return copy(value = value)
  }

  override fun replaceModelVariables(model: Map<String, *>): Step {
    return copy(
      message = replaceModelVariablesInText(message, model),
      evalExpression = replaceModelVariablesInText(evalExpression ?: "", model)
    )
  }

  override fun replaceChatVariables(variables: Map<String, *>): TextStep {
    return copy(
      message = replaceChatVariablesInText(message, variables)
    )
  }

  override fun extendStep(stepProperties: Map<String, *>): Step {
    return copy(
      id = stepProperties["id"] as? String ?: id,
      variable = stepProperties["variable"] as? String ?: variable,
      evalExpression = stepProperties["evalExpression"] as? String ?: evalExpression,
      message = stepProperties["message"] as? String ?: message,
      trigger = stepProperties["trigger"] as? String ?: trigger,
      user = stepProperties["user"] as? Boolean ?: user
    )
  }

  override fun trim(): TextStep {
    return copy(
      evalExpression = null,
      value = null
    )
  }
}

data class ValueStep(
  override val id: String,
  override val variable: String? = null,
  override val value: Any? = null
) : BaseStep, ValueHolder {
  override fun updateValue(value: Any?): ValueStep {
    return copy(value = value)
  }

  override fun extendStep(stepProperties: Map<String, *>): BaseStep {
    return copy(
      id = stepProperties["id"] as? String ?: id,
      variable = stepProperties["variable"] as? String ?: variable
    )
  }
}

data class ConditionalTextStep(
  override val id: String,
  override val variable: String? = null,
  override val evalExpression: String? = null,
  val message: String,
  override val value: Any? = null,
  override val trigger: Map<String, String>?,
  override val end: Boolean = false,
  val user: Boolean = false
) : Step, ConditionTriggerStep, ValueHolder, ChatVariableReplaceable {

  override fun updateValue(value: Any?): ConditionalTextStep {
    return copy(value = value)
  }

  override fun replaceModelVariables(model: Map<String, *>): Step {
    return copy(
      message = replaceModelVariablesInText(message, model),
      trigger = if (trigger != null) replaceVariablesInKeys(trigger, model) else trigger,
      evalExpression = replaceModelVariablesInText(evalExpression ?: "", model)
    )
  }

  override fun replaceChatVariables(variables: Map<String, *>): ConditionalTextStep {
    return copy(
      message = replaceChatVariablesInText(message, variables)
    )
  }

  override fun extendStep(stepProperties: Map<String, *>): Step {
    @Suppress("UNCHECKED_CAST")
    return copy(
      evalExpression = stepProperties["evalExpression"] as? String ?: evalExpression,
      message = stepProperties["message"] as? String ?: message,
      trigger =
      if (stepProperties.containsKey("trigger"))
        stepProperties["trigger"] as Map<String, String>?
      else trigger,
      user = stepProperties["user"] as? Boolean ?: user
    )
  }

  override fun trim(): ConditionalTextStep {
    return copy(
      evalExpression = null,
      trigger = null,
      value = null
    )
  }

  override fun toSimpleTriggerStep(trigger: String?, props: Map<String, Any>): SimpleTriggerStep {
    return TextStep(
      id = this.id,
      variable = variable,
      evalExpression = this.evalExpression,
      message = this.message,
      trigger = trigger,
      end = this.end,
      user = this.user
    )
  }
}

interface MultipleChoiceStep : Step, TriggerStep, ValueInputable, ChatVariableReplaceable {
  val choices: Any?
  override val trigger: Any?
  val minChoices: Any?
  val maxChoices: Any?

  interface Choice {
    val value: Any
    val label: String
    fun replaceModelVariables(model: Map<String, *>): Choice
    fun compareValueWithString(other: String): Boolean
    fun replaceChatVariables(variables: Map<String, *>): Choice
  }

  data class RegularChoice(
    override val label: String,
    override val value: String
  ) : Choice {
    override fun replaceModelVariables(model: Map<String, *>): Choice {
      return copy(
        value = replaceModelVariablesInText(value, model),
        label = replaceModelVariablesInText(label, model)
      )
    }

    override fun replaceChatVariables(variables: Map<String, *>): RegularChoice {
      return copy(
        label = replaceChatVariablesInText(label, variables)
      )
    }

    override fun compareValueWithString(other: String): Boolean {
      if (this.value == other)
        return true
      val otherObj = jacksonObjectMapper().readValue<String>(other)
      return this.value == otherObj
    }
  }

  data class JsonChoice(
    override val label: String,
    override val value: Map<String, Any>
  ) : Choice {
    override fun replaceModelVariables(model: Map<String, *>): Choice {
      @Suppress("UNCHECKED_CAST")
      return copy(
        label = replaceModelVariablesInText(label, model),
        value = replaceVariablesInMap(value, model) as Map<String, Any>
      )
    }

    override fun replaceChatVariables(variables: Map<String, *>): JsonChoice {
      return copy(
        label = replaceChatVariablesInText(label, variables)
      )
    }

    override fun compareValueWithString(other: String): Boolean {
      val otherObj = jacksonObjectMapper().readTree(other)
      val thisObj = jacksonObjectMapper().readTree(jacksonObjectMapper().writeValueAsString(value))
      return otherObj == thisObj
    }
  }
}

data class SimpleMultipleChoiceStep(
  override val id: String,
  override val variable: String? = null,
  @JsonTypeInfo(use = MINIMAL_CLASS, include = PROPERTY, property = "@class")
  override val choices: List<MultipleChoiceStep.Choice> = listOf(),
  override val minChoices: Int = 0,
  override val maxChoices: Int = Int.MAX_VALUE,
  override val trigger: String? = null,
  override val evalExpression: String? = null,
  override val end: Boolean = false
) : MultipleChoiceStep, SimpleTriggerStep, ValueInputable, ChatVariableReplaceable, Trimmable {
  override fun replaceModelVariables(model: Map<String, *>): Step {
    return copy(
      choices = choices.map { choice -> choice.replaceModelVariables(model) },
      evalExpression = replaceModelVariablesInText(evalExpression ?: "", model)
    )
  }

  override fun replaceChatVariables(variables: Map<String, *>): SimpleMultipleChoiceStep {
    return copy(
      choices = choices.map { it.replaceChatVariables(variables) }
    )
  }

  @Suppress("UNCHECKED_CAST")
  override fun extendStep(stepProperties: Map<String, *>): Step {
    return copy(
      minChoices = stepProperties["minChoices"] as? Int ?: 0,
      maxChoices = stepProperties["maxChoices"] as? Int ?: Int.MAX_VALUE,
      choices = getChoicesFromList(stepProperties["choices"] as? List<Map<String, Any>>)
        ?: throw ChoicesNotFoundException(),
      trigger = stepProperties["trigger"] as? String ?: trigger,
      evalExpression = stepProperties["evalExpression"] as? String ?: evalExpression
    )
  }

  override fun trim(): SimpleMultipleChoiceStep {
    return copy(evalExpression = null, trigger = null)
  }

  override fun inputValueWithJson(json: String): TextStep {
    val valueList = jacksonObjectMapper().readValue<List<Any>>(json)

    val selectedChoices = valueList.map { value -> choices.find { choice -> choice.value == value } }
    val message = selectedChoices.map { choice -> choice?.label }.joinToString(", ")
    return TextStep(
      id = id,
      variable = variable,
      evalExpression = evalExpression,
      end = end,
      message = message,
      value = valueList,
      trigger = trigger,
      user = true
    )
  }
}

data class ConditionalMultipleChoiceStep(
  override val id: String,
  override val variable: String?,
  @JsonTypeInfo(use = MINIMAL_CLASS, include = PROPERTY, property = "@class")
  override val choices: List<MultipleChoiceStep.Choice> = listOf(),
  override val minChoices: Int = 0,
  override val maxChoices: Int = Int.MAX_VALUE,
  override val trigger: Map<String, String>?,
  override val evalExpression: String? = null,
  override val end: Boolean = false
) : MultipleChoiceStep, ConditionTriggerStep, ValueInputable {
  override fun replaceModelVariables(model: Map<String, *>): Step {
    return copy(
      trigger = if (trigger != null) replaceVariablesInKeys(trigger, model) else trigger,
      choices = choices.map { choice -> choice.replaceModelVariables(model) },
      evalExpression = replaceModelVariablesInText(evalExpression ?: "", model)
    )
  }

  override fun replaceChatVariables(variables: Map<String, *>): ConditionalMultipleChoiceStep {
    return copy(
      choices = choices.map { it.replaceChatVariables(variables) }
    )
  }

  override fun extendStep(stepProperties: Map<String, *>): Step {
    @Suppress("UNCHECKED_CAST")
    return copy(
      minChoices = stepProperties["minChoices"] as? Int ?: 0,
      maxChoices = stepProperties["maxChoices"] as? Int ?: Int.MAX_VALUE,
      choices = getChoicesFromList(stepProperties["choices"] as? List<Map<String, Any>>)
        ?: throw ChoicesNotFoundException(),
      trigger = (
        if (stepProperties.containsKey("trigger")) stepProperties["trigger"] as Map<String, String>? else trigger
        ),
      evalExpression = stepProperties["evalExpression"] as? String ?: evalExpression
    )
  }

  override fun trim(): ConditionalMultipleChoiceStep {
    return copy(evalExpression = null, trigger = null)
  }

  override fun toSimpleTriggerStep(trigger: String?, props: Map<String, Any>): SimpleTriggerStep {
    return SimpleMultipleChoiceStep(
      id = id,
      variable = variable,
      choices = choices,
      minChoices = minChoices,
      maxChoices = maxChoices,
      trigger = trigger,
      evalExpression = evalExpression,
      end = end
    )
  }

  override fun inputValueWithJson(json: String): ConditionalTextStep {
    val valueList = jacksonObjectMapper().readValue<List<Any>>(json)

    val selectedChoices = valueList.map { value -> choices.find { choice -> choice.value == value } }
    val message = selectedChoices.map { choice -> choice?.label }.joinToString(", ")
    return ConditionalTextStep(
      id = id,
      variable = variable,
      evalExpression = evalExpression,
      end = end,
      message = message,
      value = valueList,
      trigger = trigger,
      user = true
    )
  }
}

data class DynamicMultipleChoiceStep(
  override val id: String,
  override val variable: String? = null,
  // @JsonTypeInfo(use = MINIMAL_CLASS, include = PROPERTY, property = "@class")
  override val choices: Any? = null,
  override val minChoices: Any? = 0,
  override val maxChoices: Any? = Int.MAX_VALUE,
  override val trigger: String? = null,
  override val evalExpression: String? = null,
  override val end: Boolean = false
) : MultipleChoiceStep, SimpleTriggerStep, ValueInputable, ChatVariableReplaceable, Trimmable {

  override fun replaceModelVariables(model: Map<String, *>): Step {
    return copy(
      evalExpression = replaceModelVariablesInText(evalExpression ?: "", model)
    )
  }

  @Suppress("UNCHECKED_CAST")
  override fun replaceChatVariables(variables: Map<String, *>): Step {
    val minChoicesInt = if (minChoices is String) {
      variables[extractChatVariableName(minChoices)] as Int
    } else minChoices as Int
    val maxChoicesInt = if (maxChoices is String) {
      variables[extractChatVariableName(maxChoices)] as Int
    } else maxChoices as Int
    val choicesList = if (choices is String) {
      variables[extractChatVariableName(choices)] as List<Map<String, Any>>
    } else choices as List<Map<String, Any>>
    val choices = getChoicesFromList(choicesList)!!

    if (maxChoicesInt == 1) {
      return OptionsStep(
        id = id,
        end = end,
        evalExpression = evalExpression,
        variable = variable,
        options = choices.map {
          convertChoiceToOption(it.replaceChatVariables(variables), trigger)
        }.toList()
      )
    }

    return copy(
      minChoices = minChoicesInt,
      maxChoices = maxChoicesInt,
      choices = choices.map { it.replaceChatVariables(variables) }
    )
  }

  override fun extendStep(stepProperties: Map<String, *>): Step {
    return copy(
      minChoices = stepProperties["minChoices"],
      maxChoices = stepProperties["maxChoices"],
      choices = stepProperties["choices"],
      trigger = stepProperties["trigger"] as? String ?: trigger,
      evalExpression = stepProperties["evalExpression"] as? String ?: evalExpression
    )
  }

  override fun trim(): DynamicMultipleChoiceStep {
    return copy(evalExpression = null, trigger = null)
  }

  @Suppress("UNCHECKED_CAST")
  override fun inputValueWithJson(json: String): TextStep {
    val valueList = jacksonObjectMapper().readValue<List<Any>>(json)
    val choicesList = choices as List<Map<String, Any>>
    val choices = getChoicesFromList(choicesList)!!
    val selectedChoices = valueList.map { value -> choices.find { choice -> choice.value == value } }
    val message = selectedChoices.map { choice -> choice?.label }.joinToString(", ")
    return TextStep(
      id = id,
      variable = variable,
      evalExpression = evalExpression,
      end = end,
      message = message,
      value = valueList,
      trigger = trigger,
      user = true
    )
  }
}

data class OptionsStep(
  override val id: String,
  override val variable: String? = null,
  override val evalExpression: String? = null,
  @JsonTypeInfo(use = MINIMAL_CLASS, include = PROPERTY, property = "@class")
  val options: List<Option>,
  override val end: Boolean = false
) : Step, ValueInputable, ChatVariableReplaceable, Trimmable {

  override fun replaceModelVariables(model: Map<String, *>): Step {
    return copy(
      options = options.map { option -> option.replaceModelVariables(model) },
      evalExpression = replaceModelVariablesInText(evalExpression ?: "", model)
    )
  }

  override fun replaceChatVariables(variables: Map<String, *>): OptionsStep {
    return copy(
      options = options.map { it.replaceChatVariables(variables) }
    )
  }

  override fun extendStep(stepProperties: Map<String, *>): Step {
    @Suppress("UNCHECKED_CAST")
    return copy(
      options = stepProperties["options"] as? List<Option> ?: options
    )
  }

  override fun trim(): OptionsStep {
    return copy(
      evalExpression = null,
      options = options.map { option -> option.trim() }
    )
  }

  override fun inputValueWithJson(json: String): TextStep {
    val option = options.find { it.compareValueWithString(json) }
    return TextStep(
      id = id,
      variable = variable,
      evalExpression = evalExpression,
      end = end,
      trigger = option?.trigger,
      value = option?.value,
      message = option?.label ?: "",
      user = true
    )
  }

  fun assignEndProperty(): OptionsStep {
    if (isEveryOptionTriggerEmpty()) {
      return copy(end = true)
    }
    return this
  }

  private fun isEveryOptionTriggerEmpty(): Boolean {
    return this.options.all { it.trigger == null }
  }
}

interface Option {
  val value: Any
  val label: String
  var trigger: String?
  fun replaceModelVariables(model: Map<String, *>): Option
  fun replaceChatVariables(variables: Map<String, *>): Option
  fun compareValueWithString(other: String): Boolean
  fun trim(): Option
}

data class RegularOption(
  override val value: String,
  override val label: String,
  @get:JsonInclude(NON_DEFAULT)
  override var trigger: String? = null
) : Option {
  override fun replaceModelVariables(model: Map<String, *>): Option {
    return copy(
      value = replaceModelVariablesInText(value, model),
      label = replaceModelVariablesInText(label, model)
    )
  }

  override fun replaceChatVariables(variables: Map<String, *>): RegularOption {
    return copy(
      label = replaceChatVariablesInText(label, variables)
    )
  }

  override fun compareValueWithString(other: String): Boolean {
    if (this.value == other)
      return true
    val otherObj = jacksonObjectMapper().readValue<String>(other)
    return this.value == otherObj
  }

  override fun trim(): RegularOption {
    return copy(
      trigger = null
    )
  }
}

data class JsonOption(
  override val value: Map<String, Any>,
  override val label: String,
  @get:JsonInclude(NON_DEFAULT)
  override var trigger: String? = null
) : Option {
  override fun replaceModelVariables(model: Map<String, *>): Option {
    return copy(label = replaceModelVariablesInText(label, model))
  }

  override fun replaceChatVariables(variables: Map<String, *>): JsonOption {
    return copy(
      label = replaceChatVariablesInText(label, variables)
    )
  }

  override fun compareValueWithString(other: String): Boolean {
    val otherObj = jacksonObjectMapper().readTree(other)
    val thisObj = jacksonObjectMapper().readTree(jacksonObjectMapper().writeValueAsString(value))
    return otherObj == thisObj
  }

  override fun trim(): JsonOption {
    return copy(
      trigger = null
    )
  }
}

data class UserInputStep(
  override val id: String,
  override val variable: String? = null,
  override val evalExpression: String? = null,
  val user: Boolean = true,
  val format: String? = null,
  val invalidFormatMessage: String? = null,
  @get:JsonInclude(NON_DEFAULT)
  override val trigger: String? = null,
  override val value: Any? = null,
  val message: String? = null,
  override val end: Boolean = false
) : Step, ValueHolder, ValueInputable, SimpleTriggerStep, Trimmable {

  override fun updateValue(value: Any?): UserInputStep {
    return copy(value = value)
  }

  override fun replaceModelVariables(model: Map<String, *>): Step {
    return copy(
      evalExpression = replaceModelVariablesInText(evalExpression ?: "", model)
    )
  }

  override fun extendStep(stepProperties: Map<String, *>): UserInputStep {
    return copy(
      id = stepProperties["id"] as? String ?: id,
      variable = stepProperties["variable"] as? String ?: variable,
      evalExpression = stepProperties["evalExpression"] as? String ?: evalExpression,
      format = stepProperties["format"] as? String,
      invalidFormatMessage = stepProperties["invalidFormatMessage"] as? String,
      trigger = if (stepProperties.containsKey("trigger")) stepProperties["trigger"] as String? else trigger,
      value = stepProperties["value"] ?: value,
      message = stepProperties["message"] as? String ?: message,
      end = stepProperties["end"] as Boolean? ?: end,
      user = stepProperties["user"] as Boolean? ?: user
    )
  }

  override fun inputValueWithJson(json: String): UserInputStep {
    val inputValue = jacksonObjectMapper().readValue<String>(json)
    return this.extendStep(
      mapOf(
        "value" to (inputValue.toFloatOrNull() ?: inputValue),
        "message" to inputValue,
        "user" to true
      )
    )
  }

  override fun trim(): UserInputStep {
    return copy(
      evalExpression = null,
      trigger = null,
      value = null
    )
  }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class ConditionalUserInputStep(
  override val id: String,
  override val variable: String? = null,
  override val evalExpression: String? = null,
  val user: Boolean = true,
  val format: String? = null,
  val invalidFormatMessage: String? = null,
  override val trigger: Map<String, String>?,
  override val value: Any? = null,
  val message: String? = null,
  override val end: Boolean = false
) : Step, ValueHolder, ValueInputable, ConditionTriggerStep {

  override fun updateValue(value: Any?): ConditionalUserInputStep {
    return copy(value = value)
  }

  override fun replaceModelVariables(model: Map<String, *>): Step {
    return copy(
      trigger = if (trigger != null) replaceVariablesInKeys(trigger, model) else trigger,
      evalExpression = replaceModelVariablesInText(evalExpression ?: "", model)
    )
  }

  override fun extendStep(stepProperties: Map<String, *>): ConditionalUserInputStep {
    @Suppress("UNCHECKED_CAST")
    return copy(
      evalExpression = stepProperties["evalExpression"] as? String ?: evalExpression,
      format = stepProperties["format"] as? String,
      invalidFormatMessage = stepProperties["invalidFormatMessage"] as? String,
      trigger = if (stepProperties.containsKey("trigger"))
        stepProperties["trigger"] as Map<String, String>?
      else
        trigger,
      value = stepProperties["value"] ?: value,
      message = stepProperties["message"] as? String ?: message,
      end = stepProperties["end"] as Boolean? ?: end,
      user = stepProperties["user"] as Boolean? ?: user
    )
  }

  override fun trim(): ConditionalUserInputStep {
    return copy(
      evalExpression = null,
      trigger = null,
      value = null
    )
  }

  override fun inputValueWithJson(json: String): ConditionalUserInputStep {
    val inputValue = jacksonObjectMapper().readValue<String>(json)
    return this.extendStep(
      mapOf(
        "value" to inputValue,
        "message" to inputValue,
        "user" to true
      )
    )
  }

  override fun toSimpleTriggerStep(trigger: String?, props: Map<String, Any>): SimpleTriggerStep {
    return UserInputStep(
      id = id,
      variable = variable,
      evalExpression = evalExpression,
      user = user,
      format = format,
      invalidFormatMessage = invalidFormatMessage,
      trigger = trigger,
      value = value,
      message = message,
      end = end
    )
  }
}

fun getOptionsFromList(list: List<Map<String, Any>>?, trigger: String?): List<Option>? {
  @Suppress("UNCHECKED_CAST")
  return list
    ?.map { choice ->
      val label = choice["label"] as String
      val value = choice["value"]
      if (value is Map<*, *>)
        JsonOption(value as Map<String, Any>, label, trigger)
      else
        RegularOption(value as String, label, trigger)
  }
}

fun getChoicesFromList(list: List<Map<String, Any>>?): List<MultipleChoiceStep.Choice>? {
  @Suppress("UNCHECKED_CAST")
  return list
    ?.map { choice ->
      val label = choice["label"] as String
      val value = choice["value"]
      if (value is Map<*, *>)
        MultipleChoiceStep.JsonChoice(label, value as Map<String, Any>)
      else
        MultipleChoiceStep.RegularChoice(label, value as String)
    }
}

private fun convertChoiceToOption(choice: MultipleChoiceStep.Choice, trigger: String?): Option {
  return if (choice is MultipleChoiceStep.JsonChoice) {
    JsonOption(choice.value, choice.label, trigger)
  } else {
    RegularOption(choice.value as String, choice.label, trigger)
  }
}

private fun replaceVariablesInKeys(map: Map<String, String>, model: Map<String, *>): Map<String, String> {
  return map.map { (key, value) ->
    val newKey = replaceModelVariablesInText(key, model)
    newKey to value
  }.toMap()
}

private fun replaceVariablesInMap(map: Map<*, *>, model: Map<String, *>): Map<*, *> {
  return map.map { (key, value) ->
    val newKey = if (key is String) replaceModelVariablesInText(key, model) else key
    // val newValue = if (value is String) replaceVariablesInText(value, model) else value
    val newValue = when (value) {
      is Map<*, *> -> replaceVariablesInMap(value, model)
      is String -> replaceModelVariablesInText(value, model)
      else -> value
    }
    newKey to newValue
  }.toMap()
}

val modelVariableRegex = """\[([a-zA-Z_][a-zA-Z0-9_.-]*?)]""".toRegex()

private fun replaceModelVariablesInText(text: String, model: Map<String, *>): String {
  return replaceVariablesInText(modelVariableRegex.toPattern(), text, model)
}

val chatVariableRegex = """\{([a-zA-Z_][a-zA-Z0-9_.-]*?)}""".toRegex()

private fun replaceChatVariablesInText(text: String, chatVariableValues: Map<String, *>): String {
  return replaceVariablesInText(chatVariableRegex.toPattern(), text, chatVariableValues)
}

private fun replaceVariablesInText(
  variablePattern: Pattern,
  text: String,
  variableValues: Map<String, *>
): String {
  val matcher = variablePattern.matcher(text)
  val builder = StringBuilder()
  var i = 0
  while (matcher.find()) {
    val variable = matcher.group(1)
    builder.append(text.substring(i, matcher.start()))
    val replacement = variableValues.getValueUsingKeyPath(variable)
      ?: throw VariableException("No variable value could be found for the variable: $variable")
    builder.append(serializeReplacement(replacement))
    i = matcher.end()
  }
  builder.append(text.substring(i, text.length))
  return builder.toString()
}

private fun serializeReplacement(replacement: Any?): String {
  return if (replacement is String) {
    replacement
  } else {
    jacksonObjectMapper().writeValueAsString(replacement)
  }
}

fun isChatVariable(text: String): Boolean {
  return chatVariableRegex.matches(text)
}

fun isNestedChatVariable(text: String): Boolean {
  return isChatVariable(text) && text.contains(".")
}

fun extractMainVariableAndKeyPath(nestedVariable: String): Pair<String, String> {
  val firstPeriodIndex = nestedVariable.indexOf('.')

  if (firstPeriodIndex == -1) return Pair(nestedVariable, "")

  val mainVariable = nestedVariable.substring(0, firstPeriodIndex)
  val keyPath = nestedVariable.substring(firstPeriodIndex + 1)
  return Pair(mainVariable, keyPath)
}

fun extractChatVariableName(text: String): String {
  return if (isChatVariable(text) || isNestedChatVariable(text))
    text.replace("{", "").replace("}", "")
  else text
}
