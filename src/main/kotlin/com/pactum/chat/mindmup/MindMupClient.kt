package com.pactum.chat.mindmup

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.pactum.chat.ChoicesNotFoundException
import com.pactum.chat.TagNotFoundException
import com.pactum.chat.TagUnclosedException
import com.pactum.chat.model.Chat
import com.pactum.chat.model.ConditionalTextStep
import com.pactum.chat.model.DynamicMultipleChoiceStep
import com.pactum.chat.model.ConditionalUserInputStep
import com.pactum.chat.model.SimpleMultipleChoiceStep
import com.pactum.chat.model.OptionsStep
import com.pactum.chat.model.JsonOption
import com.pactum.chat.model.Option
import com.pactum.chat.model.getOptionsFromList
import com.pactum.chat.model.RegularOption
import com.pactum.chat.model.ConditionalMultipleChoiceStep
import com.pactum.chat.model.Step
import com.pactum.chat.model.TextStep
import com.pactum.chat.model.UserInputStep
import com.pactum.negotiationasset.NegotiationAssetService
import com.pactum.utils.JsonHelper
import mu.KotlinLogging.logger
import org.springframework.boot.json.JsonParserFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.SortedMap
import java.util.UUID
import kotlin.collections.ArrayList
import kotlin.collections.component1
import kotlin.collections.component2

private val logger = logger {}

@Service
class MindMupClient(
  private val objectMapper: ObjectMapper,
  private val negotiationAssetService: NegotiationAssetService
) {

  fun getMup(driveId: String): MindMupResponse {
    val json = negotiationAssetService.getChatFlow(driveId)
    return objectMapper.readValue(json, MindMupResponse::class.java)
  }
}

data class MindMupResponse(
  val id: String,
  val ideas: SortedMap<BigDecimal, Idea>,
  val links: List<Link> = mutableListOf()
) {

  fun toChat(): Chat {
    return Chat(
      toSteps(ideas)
    )
  }

  private fun toSteps(ideas: Map<BigDecimal, Idea>): List<Step> {
    return ideas.flatMap { (_, idea) ->
      val steps = ArrayList<Step>()
      val optionsStepId = idea.uuid()
      val trigger = trigger(idea, optionsStepId)
      val stepProperties = idea.stepProperties

      if (idea.isMultipleChoice()) {
        steps += multipleChoicesToStep(idea, trigger, stepProperties)
      }

      if (idea.isConditionalText()) {
        val step = ConditionalTextStep(
          id = idea.id,
          message = idea.title,
          trigger = triggerConditions(idea),
          evalExpression = idea.evalExpression,
          end = trigger.isNullOrEmpty()
        )
        steps += step.extendStep(stepProperties)
      } else {
        if (idea.isText() || idea.isPatternEnd()) {
          val step = TextStep(
            id = idea.id,
            message = idea.title,
            trigger = trigger,
            evalExpression = idea.evalExpression,
            end = trigger.isNullOrEmpty()
          )
          steps += step.extendStep(stepProperties)
        }
      }

      if (idea.isOptions()) {
        steps += optionsToStep(idea, optionsStepId, stepProperties)
      }

      if (idea.isConditionalUserInput()) {
        val step = ConditionalUserInputStep(
          id = idea.id,
          variable = idea.title,
          trigger = triggerConditions(idea),
          evalExpression = idea.evalExpression
        )
        steps += step.extendStep(stepProperties)
      } else {
        if (idea.isUserInput()) {
          val step = UserInputStep(
            id = idea.id,
            variable = idea.title,
            trigger = trigger,
            evalExpression = idea.evalExpression,
            end = trigger.isNullOrEmpty()
          )
          steps += step.extendStep(stepProperties)
        }
      }

      if (!idea.isText() || !idea.isOptions()) {
        // ignore root idea and individual option ideas
        logger.debug { "Ignoring idea: ${idea.id}" }
      }
      steps + toSteps(idea.ideas ?: emptyMap())
    }
  }

  private fun multipleChoicesToStep(idea: Idea, trigger: String?, stepProperties: Map<String, Any>): Step {
    when {
      idea.isDynamicMultipleChoice() -> {
        val step = DynamicMultipleChoiceStep(
          id = idea.id,
          variable = idea.title,
          trigger = trigger,
          evalExpression = idea.evalExpression,
          end = trigger.isNullOrEmpty()
        )
        return step.extendStep(stepProperties)
      }
      idea.is1OutOfN() -> {
        val step = OptionsStep(
          id = idea.id,
          variable = idea.title,
          options = emptyList()
        )
        val extendedStep = step.extendStep(stepProperties) as OptionsStep
        @Suppress("UNCHECKED_CAST")
        val options = getOptionsFromList(stepProperties["choices"] as List<Map<String, Any>>, trigger(idea))
          ?: throw ChoicesNotFoundException()
        val optionsStep = extendedStep.copy(options = options)
        return optionsStep.assignEndProperty()
      }
      idea.hasConditionals() -> {
        val step = ConditionalMultipleChoiceStep(
          id = idea.id,
          variable = idea.title,
          trigger = triggerConditions(idea),
          evalExpression = idea.evalExpression
        )
        return step.extendStep(stepProperties)
      }
      else -> {
        val step = SimpleMultipleChoiceStep(
          id = idea.id,
          variable = idea.title,
          trigger = trigger,
          evalExpression = idea.evalExpression,
          end = trigger.isNullOrEmpty()
        )
        return step.extendStep(stepProperties)
      }
    }
  }

  private fun optionsToStep(idea: Idea, optionsStepId: String, stepProperties: Map<String, Any>): Step {
    val existingIdea = findExistingByVariableLabel(idea)
    val evalExpression = existingIdea?.evalExpression

    val step = OptionsStep(
      id = optionsStepId,
      evalExpression = evalExpression,
      variable = idea.getLabel(),
      options = toOptions(idea.ideas!!)
    )
    val extendedStep = step.extendStep(stepProperties) as OptionsStep
    return extendedStep.assignEndProperty()
  }

  private fun triggerConditions(idea: Idea): Map<String, String> {
    val ideaTriggerConditions = idea.ideas!!.map { (_, idea) ->
      val condition = idea.getLabel() ?: error("Idea condition not found: ${idea.id}")
      condition to idea.id
    }.toMap()

    val linkTriggerConditions = links
      .filter { link -> link.ideaIdFrom == idea.id }
      .map { link ->
        val condition = link.getLabel() ?: error("Link condition not found: ${link.ideaIdFrom} to ${link.ideaIdTo}")
        condition to link.ideaIdTo
      }.toMap()

    return ideaTriggerConditions + linkTriggerConditions
  }

  private fun trigger(idea: Idea, optionsStepId: String? = null): String? {
    return when {
      idea.isOptions() -> optionsStepId
      else -> nextStepId(idea)
    }
  }

  private fun findExistingByVariableLabel(idea: Idea?): Idea? {
    if (idea == null || !idea.isVariable()) {
      return null
    }
    val firstIdeaByLabel = findFirstIdeaByLabel(idea.getLabel()!!, this.ideas)
    val firstIdeaByTitle = findFirstIdeaByTitle(idea.getLabel()!!, this.ideas)

    return when {
      (firstIdeaByLabel != null && firstIdeaByLabel != idea) -> firstIdeaByLabel
      (firstIdeaByTitle != null && firstIdeaByTitle != idea) -> firstIdeaByTitle
      else -> null
    }
  }

  private fun toOptions(ideas: Map<BigDecimal, Idea>): List<Option> {
    return ideas.map { (_, idea) ->
      if (JsonHelper.isJsonObject(idea.getLabel())) {
        JsonOption(
          value = jacksonObjectMapper().readValue(idea.getLabel()!!),
          label = idea.title,
          trigger = trigger(idea)
        )
      } else {
        RegularOption(
          value = idea.getLabel() ?: idea.id,
          label = idea.title,
          trigger = trigger(idea)
        )
      }
    }
  }

  private fun nextStepId(idea: Idea) = idea.nextIdea()?.id ?: getLinkFrom(idea.id)

  private fun findFirstIdeaByTitle(title: String, ideas: Map<BigDecimal, Idea>): Idea? {
    var result: Idea? = null
    ideas.forEach { (_, idea) ->
      if (idea.title == title) {
        result = idea
        return@forEach
      }
      val nextIdea = findFirstIdeaByTitle(title, idea.ideas ?: emptyMap())
      if (nextIdea != null) {
        result = nextIdea
        return@forEach
      }
    }
    return result
  }

  private fun findFirstIdeaByLabel(label: String, ideas: Map<BigDecimal, Idea>): Idea? {
    var result: Idea? = null
    ideas.forEach { (_, idea) ->
      if (idea.getLabel() == label) {
        result = idea
        return@forEach
      }
      val nextIdea = findFirstIdeaByLabel(label, idea.ideas ?: emptyMap())
      if (nextIdea != null) {
        result = nextIdea
        return@forEach
      }
    }
    return result
  }

  private fun getLinkFrom(ideaId: String): String? {
    val link = links.find { link -> link.ideaIdFrom == ideaId }
    return link?.ideaIdTo
  }

  data class Idea(
    val id: String,
    val title: String,
    val attr: Attributes?,
    val ideas: SortedMap<BigDecimal, Idea>?
  ) {

    val evalExpression: String?
      get() {
        val tagName = "eval_expression"
        val evalExpressionFromTagElement = getContentByTagNameFromNote(tagName)?.trim()
        val untaggedEvalExpression = getUntaggedContentFromNote()?.trim()
        return if (untaggedEvalExpression != null && untaggedEvalExpression != "") {
          evalExpressionFromTagElement + "\n" + untaggedEvalExpression
        } else {
          evalExpressionFromTagElement
        }
      }

    val stepProperties: Map<String, Any>
      get() {
        val jsonParser = JsonParserFactory.getJsonParser()
        val tagName = "step_properties"
        val elementContent = getContentByTagNameFromNote(tagName)

        if (elementContent == null || elementContent.isBlank())
          return mapOf()

        return jsonParser.parseMap(elementContent)
      }

    companion object {
      const val PATTERN_PREFIX_LENGTH = 5
    }

    private fun getContentByTagNameFromNote(tagName: String): String? {
      val openingTag = "<$tagName>"
      val closingTag = "</$tagName>"

      val text = this.getNote()

      if (text != null && openingTag in text && closingTag in text) {
        val startIndex = text.indexOf(openingTag) + openingTag.length
        val endIndex = text.indexOf(closingTag)

        return text.substring(startIndex, endIndex).trim { it <= ' ' }
      }

      return null
    }

    private fun getUntaggedContentFromNote(): String? {
      val note = getNote()
      return if (note == null)
        null
      else
        removeAllTagElement(note)
    }

    private fun getNote() = this.attr?.note?.get("text")

    private fun removeAllTagElement(text: String): String {
      var cleanText = text

      val tagRegex = """<(\w+?)>""".toRegex()
      while (tagRegex.containsMatchIn(cleanText)) {
        val matchResult = tagRegex.find(cleanText)!!
        val (tagName) = matchResult.destructured
        cleanText = removeTagElement(tagName, cleanText)
        matchResult.next()
      }

      return cleanText
    }

    private fun removeTagElement(tagName: String, text: String): String {
      val openingTagRegex = """<$tagName>""".toRegex()
      val closingTagRegex = """</$tagName>""".toRegex()

      val openingTagMatchResult = openingTagRegex.find(text) ?: throw TagNotFoundException()
      val closingTagMatchResult = closingTagRegex.find(text) ?: throw TagUnclosedException()

      val openingTagStartingPosition = openingTagMatchResult.range.first
      val closingTagEndingPosition = closingTagMatchResult.range.last

      return text.removeRange(openingTagStartingPosition, closingTagEndingPosition + 1)
    }

    fun isText() = attr?.style?.get("backgroundColor") == "#FFFFFF"

    fun isMultipleChoice() = attr?.style?.get("backgroundColor") == "#FFFF99"

    fun isOption() = attr?.style?.get("backgroundColor") == null

    fun isUserInput() = attr?.style?.get("backgroundColor") == "#CCFFFF"

    fun isPattern() = attr?.style?.get("backgroundColor") == "#4ef542"

    fun isPatternEnd() = attr?.style?.get("backgroundColor") == "#dbfc03"

    fun is1OutOfN() = stepProperties["maxChoices"] as? Int == 1

    fun isDynamicMultipleChoice() =
      stepProperties["choices"] is String ||
        stepProperties["minChoices"] is String ||
        stepProperties["maxChoices"] is String

    fun isOptions() = !ideas.isNullOrEmpty() && ideas.all { (_, idea) -> idea.isOption() }

    fun isConditionalUserInput() = isUserInput() && hasConditionals()

    fun hasConditionals(): Boolean {
      return !ideas.isNullOrEmpty() && ideas.all { (_, idea) ->
        idea.getLabel() != null && !idea.isVariable()
      }
    }

    fun getLabel() = attr?.parentConnector?.get("label")

    fun isVariable() = getLabel()?.startsWith('{') == true && getLabel()?.endsWith('}') == true

    fun nextIdea() = ideas?.values?.firstOrNull()

    fun uuid() = UUID.nameUUIDFromBytes(id.toByteArray()).toString()

    fun isConditionalText() = isText() && hasConditionals() && !isVariable()

    fun getPatternId() = attr?.note?.get("text")

    fun hasChildren() = ideas.isNullOrEmpty()

    fun getPatternPrefix(): String {
      return if (id.length > PATTERN_PREFIX_LENGTH)
        id.substring(0, PATTERN_PREFIX_LENGTH)
      else
        id
    }
  }

  data class Attributes(
    val style: Map<String, String>?,
    val parentConnector: Map<String, String>?,
    val note: Map<String, String>?
  )

  data class Link(
    val ideaIdFrom: String,
    val ideaIdTo: String,
    val attr: Attributes?
  ) {
    fun getLabel() = attr?.style?.get("label")
  }

  override fun toString(): String {
    return jacksonObjectMapper().writeValueAsString(this)
  }
}
