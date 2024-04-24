package com.pactum.chat

import com.pactum.chat.model.ConditionalMultipleChoiceStep
import com.pactum.chat.model.ConditionalTextStep
import com.pactum.chat.model.ConditionalUserInputStep
import com.pactum.chat.model.JsonOption
import com.pactum.chat.model.MultipleChoiceStep.JsonChoice
import com.pactum.chat.model.MultipleChoiceStep.RegularChoice
import com.pactum.chat.model.OptionsStep
import com.pactum.chat.model.RegularOption
import com.pactum.chat.model.SimpleMultipleChoiceStep
import com.pactum.chat.model.TextStep
import com.pactum.chat.model.UserInputStep
import com.pactum.chat.model.ValueStep
import com.pactum.chat.model.chatVariableRegex
import com.pactum.chat.model.extractChatVariableName
import com.pactum.chat.model.extractMainVariableAndKeyPath
import com.pactum.chat.model.isChatVariable
import com.pactum.chat.model.isNestedChatVariable
import com.pactum.chat.model.modelVariableRegex
import com.pactum.test.UnitTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@UnitTest
class ChatTest {
  @Test
  fun `can assign end property to OptionsStep with no trigger in all options`() {
    val step = OptionsStep(
      id = "",
      options = listOf(
        RegularOption(value = "", label = ""),
        JsonOption(value = mapOf<String, Any>(), label = "")
      )
    )

    val expectedStep = step.copy(end = true)

    assertThat(step.assignEndProperty()).isEqualTo(expectedStep)
  }

  @Test
  fun `will not assign end property to OptionsStep with trigger in any of options`() {
    val step = OptionsStep(
      id = "",
      options = listOf(
        RegularOption(value = "", label = "", trigger = ""),
        JsonOption(value = mapOf<String, Any>(), label = "")
      )
    )

    val expectedStep = step.copy(end = false)

    assertThat(step.assignEndProperty()).isEqualTo(expectedStep)
  }

  @Test
  fun `extend step works for TextStep`() {
    val step = TextStep(
      id = "id",
      evalExpression = "",
      message = "",
      trigger = null
    )

    val expectedOutput = step.copy(
      evalExpression = "evalExpression",
      message = "message",
      trigger = "trigger"
    )

    val stepProperties = mapOf(
      "evalExpression" to expectedOutput.evalExpression,
      "message" to expectedOutput.message,
      "trigger" to expectedOutput.trigger
    )

    assertThat(step.extendStep(stepProperties)).isEqualTo(expectedOutput)
  }

  @Test
  fun `extend step works for ConditionalTextStep`() {
    val step = ConditionalTextStep(
      id = "id",
      evalExpression = "",
      message = "",
      trigger = emptyMap()
    )

    val expectedOutput = step.copy(
      evalExpression = "evalExpression",
      message = "message",
      trigger = mapOf("condition" to "trigger")
    )

    val stepProperties = mapOf(
      "evalExpression" to expectedOutput.evalExpression,
      "message" to expectedOutput.message,
      "trigger" to expectedOutput.trigger
    )

    assertThat(step.extendStep(stepProperties)).isEqualTo(expectedOutput)
  }

  @Test
  fun `extend step works for SimpleMultipleChoiceStep`() {
    val step = SimpleMultipleChoiceStep(
      id = "id"
    )

    val expectedOutput = step.copy(
      choices = listOf(RegularChoice("label", "value")),
      minChoices = 1,
      maxChoices = 2,
      evalExpression = "evalExpression"
    )

    val stepProperties = mapOf(
      "choices" to listOf(mapOf("label" to "label", "value" to "value")),
      "minChoices" to expectedOutput.minChoices,
      "maxChoices" to expectedOutput.maxChoices,
      "evalExpression" to expectedOutput.evalExpression
    )

    assertThat(step.extendStep(stepProperties)).isEqualTo(expectedOutput)
  }

  @Test
  fun `extend step works for ConditionalMultipleChoiceStep`() {
    val step = ConditionalMultipleChoiceStep(
      id = "id",
      variable = "{variable}",
      trigger = emptyMap()
    )

    val expectedOutput = step.copy(
      choices = listOf(RegularChoice("label", "value")),
      minChoices = 1,
      maxChoices = 2,
      evalExpression = "evalExpression"
    )

    val stepProperties = mapOf(
      "choices" to listOf(mapOf("label" to "label", "value" to "value")),
      "minChoices" to expectedOutput.minChoices,
      "maxChoices" to expectedOutput.maxChoices,
      "evalExpression" to expectedOutput.evalExpression
    )

    assertThat(step.extendStep(stepProperties)).isEqualTo(expectedOutput)
  }

  @Test
  fun `extend step works for OptionsStep`() {
    val step = OptionsStep(
      id = "id",
      evalExpression = "",
      options = emptyList()
    )

    val expectedOutput = step.copy(
      options = listOf(RegularOption("value", "label"))
    )

    val stepProperties = mapOf(
      "evalExpression" to expectedOutput.evalExpression, // should not be present in output
      "options" to expectedOutput.options
    )

    assertThat(step.extendStep(stepProperties)).isEqualTo(expectedOutput)
  }

  @Test
  fun `extend step works for UserInputStep`() {
    val step = UserInputStep(
      id = "id",
      evalExpression = null,
      trigger = null
    )

    val expectedOutput = step.copy(
      evalExpression = "evalExpression",
      trigger = "trigger"
    )

    val stepProperties = mapOf(
      "evalExpression" to expectedOutput.evalExpression,
      "trigger" to expectedOutput.trigger
    )

    assertThat(step.extendStep(stepProperties)).isEqualTo(expectedOutput)
  }

  @Test
  fun `extend step works for ConditionalUserInputStep`() {
    val step = ConditionalUserInputStep(
      id = "id",
      evalExpression = "",
      trigger = mapOf()
    )

    val expectedOutput = step.copy(
      evalExpression = "evalExpression",
      trigger = mapOf("condition" to "trigger"),
      format = "format",
      invalidFormatMessage = "invalidFormatMessage"
    )

    val stepProperties = mapOf(
      "evalExpression" to expectedOutput.evalExpression,
      "trigger" to expectedOutput.trigger,
      "format" to expectedOutput.format,
      "invalidFormatMessage" to expectedOutput.invalidFormatMessage
    )

    assertThat(step.extendStep(stepProperties)).isEqualTo(expectedOutput)
  }

  @Test
  fun `model variable pattern works for proper variable name only`() {
    val normalVariable = "variable"
    val textWithNormalVariable = createTextWithModelVariable(normalVariable)
    assertThat(modelVariableRegex.containsMatchIn(textWithNormalVariable)).isTrue()
    assertThat(modelVariableRegex.find(textWithNormalVariable)?.groups?.get(1)?.value).isEqualTo(normalVariable)

    val underscoredVariable = "_variable"
    val textWithUnderscoredVariable = createTextWithModelVariable(underscoredVariable)
    assertThat(modelVariableRegex.containsMatchIn(textWithUnderscoredVariable)).isTrue()
    assertThat(modelVariableRegex.find(textWithUnderscoredVariable)?.groups?.get(1)?.value).isEqualTo(
      underscoredVariable
    )

    val firstLetterCapitalizedVariable = "Variable"
    val textWithFirstLetterCapitalizedVariable = createTextWithModelVariable(firstLetterCapitalizedVariable)
    assertThat(modelVariableRegex.containsMatchIn(textWithFirstLetterCapitalizedVariable)).isTrue()
    assertThat(modelVariableRegex.find(textWithFirstLetterCapitalizedVariable)?.groups?.get(1)?.value).isEqualTo(
      firstLetterCapitalizedVariable
    )

    val camelCaseVariable = "camelCaseVariable"
    val textWithCamelCaseVariable = createTextWithModelVariable(camelCaseVariable)
    assertThat(modelVariableRegex.containsMatchIn(textWithCamelCaseVariable)).isTrue()
    assertThat(modelVariableRegex.find(textWithCamelCaseVariable)?.groups?.get(1)?.value).isEqualTo(camelCaseVariable)

    val allCapsVariable = "ALL_CAPS_VARIABLE"
    val textWithAllCapsVariable = createTextWithModelVariable(allCapsVariable)
    assertThat(modelVariableRegex.containsMatchIn(textWithAllCapsVariable)).isTrue()
    assertThat(modelVariableRegex.find(textWithAllCapsVariable)?.groups?.get(1)?.value).isEqualTo(allCapsVariable)

    val hyphenatedVariable = "variable-name"
    val textWithHyphenatedVariable = createTextWithModelVariable(hyphenatedVariable)
    assertThat(modelVariableRegex.containsMatchIn(textWithHyphenatedVariable)).isTrue()
    assertThat(modelVariableRegex.find(textWithHyphenatedVariable)?.groups?.get(1)?.value).isEqualTo(hyphenatedVariable)

    val nestedVariable = "variable.property"
    val textWithNestedVariable = createTextWithModelVariable(nestedVariable)
    assertThat(modelVariableRegex.containsMatchIn(textWithNestedVariable)).isTrue()
    assertThat(modelVariableRegex.find(textWithNestedVariable)?.groups?.get(1)?.value).isEqualTo(nestedVariable)
  }

  @Test
  fun `model variable pattern does not work for variable name starting with non-letters and non-hyphen`() {
    val variableStartingWithPeriod = ".variable"
    val textWithVariableStartingWithPeriod = createTextWithModelVariable(variableStartingWithPeriod)
    assertThat(modelVariableRegex.containsMatchIn(textWithVariableStartingWithPeriod)).isFalse()

    val variableStartingWithNumber = "1variable"
    val textWithVariableStartingWithNumber = createTextWithModelVariable(variableStartingWithNumber)
    assertThat(modelVariableRegex.containsMatchIn(textWithVariableStartingWithNumber)).isFalse()

    val variableStartingWithHyphen = "-variable"
    val textWithVariableStartingWithHyphen = createTextWithModelVariable(variableStartingWithHyphen)
    assertThat(modelVariableRegex.containsMatchIn(textWithVariableStartingWithHyphen)).isFalse()
  }

  private fun createTextWithModelVariable(variableName: String): String {
    return "Some [$variableName] needs to be replaced"
  }

  @Test
  fun `model variable pattern does not work with array numerical access or object string property access`() {
    assertThat(chatVariableRegex.containsMatchIn("array[0]")).isFalse()
    assertThat(chatVariableRegex.containsMatchIn("""array["hello"]""")).isFalse()
  }

  @Test
  fun `chat variable pattern works for proper variable name only`() {
    val normalVariable = "variable"
    val textWithNormalVariable = createTextWithChatVariable(normalVariable)
    assertThat(chatVariableRegex.containsMatchIn(textWithNormalVariable)).isTrue()
    assertThat(chatVariableRegex.find(textWithNormalVariable)?.groups?.get(1)?.value).isEqualTo(normalVariable)

    val underscoredVariable = "_variable"
    val textWithUnderscoredVariable = createTextWithChatVariable(underscoredVariable)
    assertThat(chatVariableRegex.containsMatchIn(textWithUnderscoredVariable)).isTrue()
    assertThat(chatVariableRegex.find(textWithUnderscoredVariable)?.groups?.get(1)?.value).isEqualTo(
      underscoredVariable
    )

    val firstLetterCapitalizedVariable = "Variable"
    val textWithFirstLetterCapitalizedVariable = createTextWithChatVariable(firstLetterCapitalizedVariable)
    assertThat(chatVariableRegex.containsMatchIn(textWithFirstLetterCapitalizedVariable)).isTrue()
    assertThat(chatVariableRegex.find(textWithFirstLetterCapitalizedVariable)?.groups?.get(1)?.value).isEqualTo(
      firstLetterCapitalizedVariable
    )

    val camelCaseVariable = "camelCaseVariable"
    val textWithCamelCaseVariable = createTextWithChatVariable(camelCaseVariable)
    assertThat(chatVariableRegex.containsMatchIn(textWithCamelCaseVariable)).isTrue()
    assertThat(chatVariableRegex.find(textWithCamelCaseVariable)?.groups?.get(1)?.value).isEqualTo(camelCaseVariable)

    val allCapsVariable = "ALL_CAPS_VARIABLE"
    val textWithAllCapsVariable = createTextWithChatVariable(allCapsVariable)
    assertThat(chatVariableRegex.containsMatchIn(textWithAllCapsVariable)).isTrue()
    assertThat(chatVariableRegex.find(textWithAllCapsVariable)?.groups?.get(1)?.value).isEqualTo(allCapsVariable)

    val hyphenatedVariable = "variable-name"
    val textWithHyphenatedVariable = createTextWithChatVariable(hyphenatedVariable)
    assertThat(chatVariableRegex.containsMatchIn(textWithHyphenatedVariable)).isTrue()
    assertThat(chatVariableRegex.find(textWithHyphenatedVariable)?.groups?.get(1)?.value).isEqualTo(hyphenatedVariable)

    val nestedVariable = "variable.property"
    val textWithNestedVariable = createTextWithChatVariable(nestedVariable)
    assertThat(chatVariableRegex.containsMatchIn(textWithNestedVariable)).isTrue()
    assertThat(chatVariableRegex.find(textWithNestedVariable)?.groups?.get(1)?.value).isEqualTo(nestedVariable)
  }

  @Test
  fun `chat variable pattern does not work for variable name starting with non-letters and non-hyphen`() {
    val variableStartingWithPeriod = ".variable"
    val textWithVariableStartingWithPeriod = createTextWithChatVariable(variableStartingWithPeriod)
    assertThat(chatVariableRegex.containsMatchIn(textWithVariableStartingWithPeriod)).isFalse()

    val variableStartingWithNumber = "1variable"
    val textWithVariableStartingWithNumber = createTextWithChatVariable(variableStartingWithNumber)
    assertThat(chatVariableRegex.containsMatchIn(textWithVariableStartingWithNumber)).isFalse()

    val variableStartingWithHyphen = "-variable"
    val textWithVariableStartingWithHyphen = createTextWithChatVariable(variableStartingWithHyphen)
    assertThat(chatVariableRegex.containsMatchIn(textWithVariableStartingWithHyphen)).isFalse()
  }

  private fun createTextWithChatVariable(variableName: String): String {
    return "Some {$variableName} needs to be replaced"
  }

  @Test
  fun `can replace model variables`() {
    val step = TextStep(id = "", message = "[modelVariable]")
    val model = mapOf(
      "modelVariable" to "value"
    )

    val stepWithModelVariablesReplaced = step.replaceModelVariables(model) as TextStep
    assertThat(stepWithModelVariablesReplaced.message).isEqualTo(model["modelVariable"])
  }

  @Test
  fun `can replace nested model variable`() {
    val step = TextStep(
      id = "",
      message = "[variable.innerVariable.innerInnerVariable]"
    )
    val model = mapOf(
      "variable" to mapOf(
        "innerVariable" to mapOf(
          "innerInnerVariable" to "value"
        )
      )
    )

    val stepWithModelVariablesReplaced = step.replaceModelVariables(model) as TextStep
    assertThat(stepWithModelVariablesReplaced.message).isEqualTo(
      model["variable"]?.get("innerVariable")?.get("innerInnerVariable")
    )
  }

  @Test
  fun `throws error when model variable not found while replacing`() {
    val step = TextStep(id = "", message = "[path.doesNotExist]")
    val model = mapOf<String, Any>()

    assertThrows<VariableException> { step.replaceModelVariables(model) }
  }

  @Test
  fun `can replace chat variables`() {
    val step = TextStep(
      id = "",
      message = "{chatVariable} {chatVariable2} {chatVariable3} {chatVariable4}"
    )
    val chatVariableValues = mapOf(
      "chatVariable" to "\nvalue",
      "chatVariable2" to true,
      "chatVariable3" to 1.5,
      "chatVariable4" to "normal"
    )

    val expectedTextMessage = "\nvalue true 1.5 normal"

    val stepWithChatVariablesReplaced = step.replaceChatVariables(chatVariableValues)
    assertThat(stepWithChatVariablesReplaced.message).isEqualTo(expectedTextMessage)
  }

  @Test
  fun `can replace nested chat variable`() {
    val step = TextStep(
      id = "",
      message = "{variable.innerVariable.innerInnerVariable}"
    )
    val chatVariableValues = mapOf(
      "variable" to mapOf(
        "innerVariable" to mapOf(
          "innerInnerVariable" to "value"
        )
      )
    )

    val stepWithChatVariablesReplaced = step.replaceChatVariables(chatVariableValues)
    assertThat(stepWithChatVariablesReplaced.message).isEqualTo(
      chatVariableValues["variable"]?.get("innerVariable")?.get("innerInnerVariable")
    )
  }

  @Test
  fun `can replace chat variables in ConditionalTextStep`() {
    val step = ConditionalTextStep(id = "", message = "{chatVariable}", trigger = mapOf())
    val chatVariableValues = mapOf(
      "chatVariable" to "value"
    )

    val stepWithChatVariablesReplaced = step.replaceChatVariables(chatVariableValues)
    assertThat(stepWithChatVariablesReplaced.message).isEqualTo(chatVariableValues["chatVariable"])
  }

  @Test
  fun `can replace chat variable in OptionsStep`() {
    val step = OptionsStep(
      id = "",
      options = listOf(
        RegularOption(label = "{optionLabel1}", value = ""),
        JsonOption(label = "{optionLabel2}", value = mapOf())
      )
    )

    val chatVariableValues = mapOf(
      "optionLabel1" to "Option 1",
      "optionLabel2" to "Option 2"
    )

    val stepWithChatVariableReplaced = step.replaceChatVariables(chatVariableValues)
    assertThat(stepWithChatVariableReplaced.options[0].label).isEqualTo(chatVariableValues["optionLabel1"])
    assertThat(stepWithChatVariableReplaced.options[1].label).isEqualTo(chatVariableValues["optionLabel2"])
  }

  @Test
  fun `can replace chat variable in SimpleMultipleChoiceStep`() {
    val step = SimpleMultipleChoiceStep(
      id = "",
      choices = listOf(
        RegularChoice("{choiceLabel1}", ""),
        JsonChoice("{choiceLabel2}", mapOf())
      )
    )

    val chatVariableValues = mapOf(
      "choiceLabel1" to "Choice 1",
      "choiceLabel2" to "Choice 2"
    )

    val stepWithChatVariableReplaced = step.replaceChatVariables(chatVariableValues)
    assertThat(stepWithChatVariableReplaced.choices[0].label).isEqualTo(chatVariableValues["choiceLabel1"])
    assertThat(stepWithChatVariableReplaced.choices[1].label).isEqualTo(chatVariableValues["choiceLabel2"])
  }

  @Test
  fun `can replace chat variable in ConditionalMultipleChoiceStep`() {
    val step = ConditionalMultipleChoiceStep(
      id = "",
      variable = null,
      choices = listOf(
        RegularChoice("{choiceLabel1}", ""),
        JsonChoice("{choiceLabel2}", mapOf())
      ),
      trigger = mapOf()
    )

    val chatVariableValues = mapOf(
      "choiceLabel1" to "Choice 1",
      "choiceLabel2" to "Choice 2"
    )

    val stepWithChatVariableReplaced = step.replaceChatVariables(chatVariableValues)
    assertThat(stepWithChatVariableReplaced.choices[0].label).isEqualTo(chatVariableValues["choiceLabel1"])
    assertThat(stepWithChatVariableReplaced.choices[1].label).isEqualTo(chatVariableValues["choiceLabel2"])
  }

  @Test
  fun `throws error when chat variable not found while replacing`() {
    val step = TextStep(id = "", message = "{path.doesNotExist}")
    val model = mapOf<String, Any>()

    assertThrows<VariableException> { step.replaceChatVariables(model) }
  }

  @Test
  fun `can trim steps`() {
    val textStep = TextStep(id = "", message = "", evalExpression = "evalExpression", value = "value")
    val trimmedTextStep = textStep.trim()
    assertThat(trimmedTextStep.evalExpression).isNull()
    assertThat(trimmedTextStep.value).isNull()

    val conditionalTextStep =
      ConditionalTextStep(id = "", message = "", evalExpression = "evalExpression", value = "value", trigger = mapOf())
    val trimmedConditionalTextStep = conditionalTextStep.trim()
    assertThat(trimmedConditionalTextStep.evalExpression).isNull()
    assertThat(trimmedConditionalTextStep.value).isNull()
    assertThat(trimmedConditionalTextStep.trigger).isNull()

    val optionsStep = OptionsStep(
      id = "",
      options = listOf(RegularOption(trigger = "trigger", label = "", value = "")),
      evalExpression = "evalExpression"
    )
    val trimmedOptionsStep = optionsStep.trim()
    assertThat(trimmedOptionsStep.evalExpression).isNull()
    assertThat(trimmedOptionsStep.options[0].trigger).isNull()

    val userInputStep = UserInputStep(
      id = "",
      value = "value",
      evalExpression = "evalExpression",
      trigger = "trigger"
    )
    val trimmedUserInputStep = userInputStep.trim()
    assertThat(trimmedUserInputStep.value).isNull()
    assertThat(trimmedUserInputStep.evalExpression).isNull()
    assertThat(trimmedUserInputStep.trigger).isNull()

    val conditionalUserInputStep = ConditionalUserInputStep(
      id = "",
      value = "value",
      evalExpression = "evalExpression",
      trigger = mapOf()
    )
    val trimmedConditionalUserInputStep = conditionalUserInputStep.trim()
    assertThat(trimmedConditionalUserInputStep.value).isNull()
    assertThat(trimmedConditionalUserInputStep.evalExpression).isNull()
    assertThat(trimmedConditionalUserInputStep.trigger).isNull()

    val simpleMultipleChoiceStep = SimpleMultipleChoiceStep(
      id = "",
      choices = listOf(),
      evalExpression = "evalExpression",
      trigger = "trigger"
    )
    val trimmedSimpleMultipleChoiceStep = simpleMultipleChoiceStep.trim()
    assertThat(trimmedSimpleMultipleChoiceStep.evalExpression).isNull()
    assertThat(trimmedSimpleMultipleChoiceStep.trigger).isNull()

    val conditionalMulpleChoiceStep = ConditionalMultipleChoiceStep(
      id = "",
      variable = null,
      choices = listOf(),
      evalExpression = "evalExpression",
      trigger = mapOf()
    )
    val trimmedConditionalMultipleChoiceStep = conditionalMulpleChoiceStep.trim()
    assertThat(trimmedConditionalMultipleChoiceStep.evalExpression).isNull()
    assertThat(trimmedConditionalMultipleChoiceStep.trigger).isNull()
  }

  @Test
  fun `steps can use json string as value to update itself`() {
    val optionsStepWithRegularOption = OptionsStep(
      id = "",
      variable = "{variable}",
      options = listOf(
        RegularOption(label = "Label 1", value = "value1"),
        RegularOption(label = "Label 2", value = "value2")
      )
    )
    val valueUpdatedOptionsStepWithRegularOption = optionsStepWithRegularOption.inputValueWithJson("\"value1\"")
    assertThat(valueUpdatedOptionsStepWithRegularOption.message).isEqualTo("Label 1")
    assertThat(valueUpdatedOptionsStepWithRegularOption.value).isEqualTo("value1")
    assertThat(valueUpdatedOptionsStepWithRegularOption.user).isEqualTo(true)
    assertThat(valueUpdatedOptionsStepWithRegularOption.variable).isEqualTo("{variable}")

    val optionsStepWithJsonOption = OptionsStep(
      id = "",
      variable = "{variable}",
      options = listOf(
        JsonOption(label = "Label 1", value = mapOf("property" to "value1")),
        JsonOption(label = "Label 2", value = mapOf("property" to "value2"))
      )
    )
    val valueUpdatedOptionsStepWithJsonOption =
      optionsStepWithJsonOption.inputValueWithJson("""{ "property":"value1" }""")
    assertThat(valueUpdatedOptionsStepWithJsonOption.message).isEqualTo("Label 1")
    assertThat(valueUpdatedOptionsStepWithJsonOption.value).isEqualTo(mapOf("property" to "value1"))
    assertThat(valueUpdatedOptionsStepWithJsonOption.variable).isEqualTo("{variable}")
    assertThat(valueUpdatedOptionsStepWithJsonOption.user).isEqualTo(true)

    val userInputStep = UserInputStep(id = "", variable = "{variable}")
    val valueUpdatedUserInputStep = userInputStep.inputValueWithJson("\"Some Input\"")
    assertThat(valueUpdatedUserInputStep.message).isEqualTo("Some Input")
    assertThat(valueUpdatedUserInputStep.variable).isEqualTo("{variable}")
    assertThat(valueUpdatedUserInputStep.value).isEqualTo("Some Input")

    val conditionalUserInputStep = ConditionalUserInputStep(id = "", variable = "{variable}", trigger = mapOf())
    val valueUpdatedConditionalUserInputStep = conditionalUserInputStep.inputValueWithJson("\"Some Input\"")
    assertThat(valueUpdatedConditionalUserInputStep.message).isEqualTo("Some Input")
    assertThat(valueUpdatedConditionalUserInputStep.variable).isEqualTo("{variable}")
    assertThat(valueUpdatedConditionalUserInputStep.value).isEqualTo("Some Input")

    val simpleMultipleChoiceStep = SimpleMultipleChoiceStep(
      id = "",
      variable = "{variable}",
      choices = listOf(
        RegularChoice(label = "Label 1", value = "value1"),
        RegularChoice(label = "Label 2", value = "value2"),
        RegularChoice(label = "Label 3", value = "value3")
      )
    )
    val valueUpdatedSimpleMultipleChoiceStep = simpleMultipleChoiceStep.inputValueWithJson(
      """[ "value1", "value3" ]"""
    )
    assertThat(valueUpdatedSimpleMultipleChoiceStep.message).isEqualTo("Label 1, Label 3")
    assertThat(valueUpdatedSimpleMultipleChoiceStep.variable).isEqualTo("{variable}")
    assertThat(valueUpdatedSimpleMultipleChoiceStep.value).isEqualTo(listOf("value1", "value3"))
    assertThat(valueUpdatedSimpleMultipleChoiceStep.user).isEqualTo(true)

    val conditionalMultipleChoiceStep = ConditionalMultipleChoiceStep(
      id = "",
      variable = "{variable}",
      choices = listOf(
        JsonChoice(label = "Label 1", value = mapOf("property" to "value1")),
        JsonChoice(label = "Label 2", value = mapOf("property" to "value2")),
        JsonChoice(label = "Label 3", value = mapOf("property" to "value3"))
      ),
      trigger = mapOf()
    )
    val valueUpdatedConditionalMultipleChoiceStep = conditionalMultipleChoiceStep.inputValueWithJson(
      """[ { "property":"value2" }, { "property":"value3" } ]"""
    )
    assertThat(valueUpdatedConditionalMultipleChoiceStep.message).isEqualTo("Label 2, Label 3")
    assertThat(valueUpdatedConditionalMultipleChoiceStep.variable).isEqualTo("{variable}")
    assertThat(valueUpdatedConditionalMultipleChoiceStep.value).isEqualTo(
      listOf(
        mapOf("property" to "value2"),
        mapOf("property" to "value3")
      )
    )
    assertThat(valueUpdatedConditionalMultipleChoiceStep.user).isEqualTo(true)
  }

  @Test
  fun `steps can use object as value to update itself`() {
    val userInputStep = UserInputStep(id = "")
    val valueUpdatedUserInputStep = userInputStep.updateValue("Some Input")
    assertThat(valueUpdatedUserInputStep.value).isEqualTo("Some Input")

    val conditionalUserInputStep = ConditionalUserInputStep(id = "", trigger = mapOf())
    val valueUpdatedConditionalUserInputStep = conditionalUserInputStep.updateValue("Some Input")
    assertThat(valueUpdatedConditionalUserInputStep.value).isEqualTo("Some Input")

    val textStep = TextStep(id = "", message = "Some Input")
    val valueUpdatedTextStep = textStep.updateValue("Some Input")
    assertThat(valueUpdatedTextStep.value).isEqualTo("Some Input")

    val valueStep = ValueStep(id = "")
    val valueUpdatedValueStep = valueStep.updateValue("Some Input")
    assertThat(valueUpdatedValueStep.value).isEqualTo("Some Input")

    val conditionalTextStep = ConditionalTextStep(id = "", message = "Some Input", trigger = mapOf<String, String>())
    val valueUpdatedConditionalTextStep = conditionalTextStep.updateValue("Some Input")
    assertThat(valueUpdatedConditionalTextStep.value).isEqualTo("Some Input")
  }

  @Test
  fun `can identify chat variables`() {
    assertThat(isChatVariable("{chatVariable}")).isTrue()
    assertThat(isChatVariable("Some text {chatVariable} some text")).isFalse()
    assertThat(isChatVariable("chatVariable")).isFalse()
    assertThat(isChatVariable("{chatVariable")).isFalse()
    assertThat(isChatVariable("chatVariable}")).isFalse()
    assertThat(isChatVariable("{{chatVariable}}")).isFalse()
  }

  @Test
  fun `can identify nested chat variables`() {
    assertThat(isNestedChatVariable("{mainVariable.property1}")).isTrue()
    assertThat(isNestedChatVariable("{mainVariable.innerProperty.innerInnerProperty}")).isTrue()
    assertThat(isNestedChatVariable("some text {mainVariable.property1} around variable")).isFalse()
    assertThat(isNestedChatVariable("{mainVariableOnly}")).isFalse()
  }

  @Test
  fun `can extract chat variable names`() {
    val variableNameOnly = "chatVariableName"

    val properVariable = "{$variableNameOnly}"
    assertThat(extractChatVariableName(properVariable)).isEqualTo(variableNameOnly)

    val improperVariableName = variableNameOnly
    assertThat(extractChatVariableName(improperVariableName)).isEqualTo(improperVariableName)

    val nestedVariableName = "mainVariable.property1.property2"
    val properNestedVariable = "{$nestedVariableName}"
    assertThat(extractChatVariableName(properNestedVariable)).isEqualTo(nestedVariableName)
  }

  @Test
  fun `can extract main variable and key path from nested chat variable`() {
    val mainVariable = "mainVariable"
    val keyPath = "property1.property2.property3"
    val nestedVariable = "$mainVariable.$keyPath"
    assertThat(extractMainVariableAndKeyPath(nestedVariable)).isEqualTo(Pair(mainVariable, keyPath))
  }

  @Test
  fun `keyPath is blank when extracting main variable from non-nested chat variable`() {
    val variable = "mainVariableOnly"
    assertThat(extractMainVariableAndKeyPath(variable)).isEqualTo(Pair(variable, ""))
  }

  @Test
  fun `can convert into simple trigger steps`() {
    val conditionalTextStep = ConditionalTextStep(
      id = "123",
      variable = "{variable}",
      evalExpression = "",
      message = "",
      trigger = mapOf(),
      end = false,
      user = false
    )
    val textStep = conditionalTextStep.toSimpleTriggerStep(trigger = "234")
    assertThat(textStep.trigger).isEqualTo("234")
    assertThat(textStep).isEqualToIgnoringGivenFields(conditionalTextStep, "trigger")

    val conditionalUserInputStep = ConditionalUserInputStep(
      id = "123",
      variable = "{variable}",
      evalExpression = "",
      trigger = mapOf()
    )
    val userInputStep = conditionalUserInputStep.toSimpleTriggerStep(trigger = "234")
    assertThat(userInputStep.trigger).isEqualTo("234")
    assertThat(userInputStep).isEqualToIgnoringGivenFields(conditionalUserInputStep, "trigger")

    val conditionalMultipleChoiceStep = ConditionalMultipleChoiceStep(
      id = "123",
      variable = "{variable}",
      trigger = mapOf()
    )
    val multipleChoiceStep = conditionalMultipleChoiceStep.toSimpleTriggerStep(trigger = "234")
    assertThat(multipleChoiceStep.trigger).isEqualTo("234")
    assertThat(multipleChoiceStep).isEqualToIgnoringGivenFields(conditionalMultipleChoiceStep, "trigger")
  }
}
