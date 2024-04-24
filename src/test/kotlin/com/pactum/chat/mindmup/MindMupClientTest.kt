package com.pactum.chat.mindmup

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.pactum.negotiationasset.NegotiationAssetService
import com.pactum.test.UnitTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.math.BigDecimal.ONE

@UnitTest
class MindMupClientTest {

  val objectMapper = jacksonObjectMapper()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

  val negotiationAssetSerivce: NegotiationAssetService = mock()

  val mindMupClient = MindMupClient(objectMapper, negotiationAssetSerivce)

  @Test
  fun `parse mindmup json`() {
    val fileId = "1Simple"
    val json = javaClass.getResource("/$fileId.mup").readText()
    whenever(negotiationAssetSerivce.getChatFlow(fileId)).thenReturn(json)

    val response = mindMupClient.getMup(fileId)

    assertThat(response.id).isEqualTo("root")
    assertThat(response.ideas.getValue(ONE).title).isEqualTo("Hi!")
    assertThat(response.ideas.getValue(ONE).attr!!.style!!["backgroundColor"]).isEqualTo("#FFFFFF")
    assertThat(response.ideas.getValue(ONE).ideas!!.getValue(ONE).title).isEqualTo("Choose!")
    assertThat(response.ideas.getValue(ONE).ideas!!.getValue(ONE).ideas!!.getValue(ONE).title)
      .isEqualTo("Option A")
    assertThat(response.ideas.getValue(ONE).ideas!!.getValue(ONE).ideas!!.getValue(BigDecimal(2)).title)
      .isEqualTo("Option B")
    assertThat(response.ideas.getValue(ONE).ideas!!.getValue(ONE).ideas!!.getValue(ONE).ideas!!.getValue(ONE).title)
      .isEqualTo("Thanks!")
    assertThat(
      response.ideas.getValue(ONE).ideas!!.getValue(ONE).ideas!!.getValue(BigDecimal(2)).ideas!!.getValue(ONE)
        .title
    )
      .isEqualTo("Choose again")
    assertThat(
      response.ideas.getValue(ONE).ideas!!.getValue(ONE).ideas!!.getValue(BigDecimal(2)).ideas!!
        .getValue(ONE).ideas!!.getValue(BigDecimal(2)).title
    ).isEqualTo("Option C")
    assertThat(response.links[0].ideaIdFrom).isEqualTo("10.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat(response.links[0].ideaIdTo).isEqualTo("5.f745.dc70c5aaf-4010.f36e69ad1")
  }

  @Test
  fun `handle missing links property without crashing`() {
    val fileId = "31NoLinksElement"
    val json = javaClass.getResource("/$fileId.mup").readText()
    whenever(negotiationAssetSerivce.getChatFlow(fileId)).thenReturn(json)

    val response = mindMupClient.getMup(fileId)
    assertThat(response.links).hasSize(0)
  }

  @Test
  fun `get evalExpression from note`() {
    val noteText = """
      <eval_expression>
        evalExpression1
      </eval_expression>
      <some_other_tag>
        someOtherExpression
      </some_other_tag>
      evalExpression2
    """.trimIndent()

    val idea = createIdeaWithNoteText(noteText)

    val expectedResponse = "evalExpression1\nevalExpression2"

    assertThat(idea.evalExpression).isEqualTo(expectedResponse)
  }

  @Test
  fun `can get evalExpression when no evalExpression is present in note`() {
    val noteText = "<eval_expression></eval_expression>"

    val idea = createIdeaWithNoteText(noteText)

    val expectedResponse = ""

    assertThat(idea.evalExpression).isEqualTo(expectedResponse)
  }

  @Test
  fun `get stepProperties from note`() {
    val noteText = """
      <step_properties>
      {
        "property": "value"
      }
      </step_properties>
    """.trimIndent()

    val idea = createIdeaWithNoteText(noteText)

    val expectedResponse = mapOf("property" to "value")

    assertThat(idea.stepProperties).isEqualTo(expectedResponse)
  }

  @Test
  fun `can get stepProperties when no stepProperties is present in note`() {
    val noteText = ""

    val idea = createIdeaWithNoteText(noteText)

    val expectedResponse = mapOf<String, Any>()

    assertThat(idea.stepProperties).isEqualTo(expectedResponse)
  }

  @Test
  fun `can get stepProperties when it is given as blank`() {
    val noteText = " \t\n"

    val idea = createIdeaWithNoteText(noteText)

    val expectedResponse = mapOf<String, Any>()

    assertThat(idea.stepProperties).isEqualTo(expectedResponse)
  }

  private fun createIdeaWithNoteText(noteText: String): MindMupResponse.Idea {
    return MindMupResponse.Idea(
      id = "",
      title = "",
      attr = MindMupResponse.Attributes(
        note = mapOf(
          "text" to noteText
        ),
        style = mapOf(),
        parentConnector = mapOf()
      ),
      ideas = sortedMapOf()
    )
  }
}
