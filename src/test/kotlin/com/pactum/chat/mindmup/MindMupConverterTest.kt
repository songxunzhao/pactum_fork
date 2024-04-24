package com.pactum.chat.mindmup

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.pactum.chat.model.ConditionalMultipleChoiceStep
import com.pactum.chat.model.ConditionalTextStep
import com.pactum.chat.model.ConditionalUserInputStep
import com.pactum.chat.model.OptionsStep
import com.pactum.chat.model.SimpleMultipleChoiceStep
import com.pactum.chat.model.TextStep
import com.pactum.chat.model.UserInputStep
import com.pactum.google.GoogleDriveService
import com.pactum.chat.model.MultipleChoiceStep
import com.pactum.chat.model.StepIDGenerator
import com.pactum.chat.model.VariableHolder
import com.pactum.negotiationasset.NegotiationAssetService
import com.pactum.test.UnitTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@UnitTest
class MindMupConverterTest {

  private val objectMapper = jacksonObjectMapper()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

  private val googleDriveService: GoogleDriveService = mock()
  private val negotiationAssetService: NegotiationAssetService = mock()

  private val mindMupClient = MindMupClient(objectMapper, negotiationAssetService)

  private val mindMupService = MindMupService(mindMupClient)

  @Test
  fun `converts a simple mindmup response to chat`() {
    val fileId = "1Simple"
    val json = javaClass.getResource("/$fileId.mup").readText()
    whenever(negotiationAssetService.getChatFlow(fileId)).thenReturn(json)

    val chat = mindMupService.getMup(fileId).toChat()

    assertThat(chat.steps.size).isEqualTo(6)

    assertThat(chat.steps[0].id).isEqualTo("1")
    assertThat((chat.steps[0] as TextStep).trigger).isEqualTo("2.f745.dc70c5aaf-4010.f36e69ad1")

    assertThat(chat.steps[1].id).isEqualTo("2.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[1] as TextStep).trigger).isEqualTo("bd2a3223-1f24-3704-821e-94c7dab164f5")

    assertThat(chat.steps[2].id).isEqualTo("bd2a3223-1f24-3704-821e-94c7dab164f5")
    assertThat((chat.steps[2] as OptionsStep).options[0].value).isEqualTo("3.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[2] as OptionsStep).options[0].trigger).isEqualTo("5.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[2] as OptionsStep).options[1].value).isEqualTo("4.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[2] as OptionsStep).options[1].trigger).isEqualTo("6.f745.dc70c5aaf-4010.f36e69ad1")

    assertThat(chat.steps[3].id).isEqualTo("5.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[3] as TextStep).trigger).isNull()

    assertThat(chat.steps[4].id).isEqualTo("6.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[4] as TextStep).trigger).isEqualTo("9760dbb1-4058-3d9e-ae29-8dc72fe08624")

    assertThat(chat.steps[5].id).isEqualTo("9760dbb1-4058-3d9e-ae29-8dc72fe08624")
    assertThat((chat.steps[5] as OptionsStep).options[0].value).isEqualTo("10.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[5] as OptionsStep).options[0].trigger).isEqualTo("5.f745.dc70c5aaf-4010.f36e69ad1")
  }

  @Test
  fun `converts a simple mindmup response with user text input to chat`() {
    val fileId = "2UserInput"
    val json = javaClass.getResource("/$fileId.mup").readText()
    whenever(negotiationAssetService.getChatFlow(fileId)).thenReturn(json)

    val chat = mindMupService.getMup(fileId).toChat()

    assertThat(chat.steps.size).isEqualTo(9)

    assertThat(chat.steps[0].id).isEqualTo("1")
    assertThat((chat.steps[0] as TextStep).end).isEqualTo(false)
    assertThat((chat.steps[0] as TextStep).trigger).isEqualTo("2.f745.dc70c5aaf-4010.f36e69ad1")

    assertThat(chat.steps[1].id).isEqualTo("2.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[1] as TextStep).trigger).isEqualTo("bd2a3223-1f24-3704-821e-94c7dab164f5")
    assertThat((chat.steps[1] as TextStep).end).isEqualTo(false)

    assertThat(chat.steps[2].id).isEqualTo("bd2a3223-1f24-3704-821e-94c7dab164f5")
    assertThat((chat.steps[2] as OptionsStep).options[0].value).isEqualTo("3.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[2] as OptionsStep).options[0].trigger).isEqualTo("5.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[2] as OptionsStep).options[1].value).isEqualTo("4.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[2] as OptionsStep).options[1].trigger).isEqualTo("6.f745.dc70c5aaf-4010.f36e69ad1")

    assertThat(chat.steps[3].id).isEqualTo("5.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[3] as TextStep).trigger).isNull()
    assertThat((chat.steps[3] as TextStep).end).isEqualTo(true)

    assertThat(chat.steps[4].id).isEqualTo("6.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[4] as TextStep).trigger).isEqualTo("9760dbb1-4058-3d9e-ae29-8dc72fe08624")
    assertThat((chat.steps[4] as TextStep).end).isEqualTo(false)

    assertThat(chat.steps[5].id).isEqualTo("9760dbb1-4058-3d9e-ae29-8dc72fe08624")
    assertThat((chat.steps[5] as OptionsStep).options[0].value).isEqualTo("10.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[5] as OptionsStep).options[0].trigger).isEqualTo("11.306b.5891d1449-d168.a8ecf6009")

    assertThat(chat.steps[6].id).isEqualTo("11.306b.5891d1449-d168.a8ecf6009")
    assertThat((chat.steps[6] as TextStep).trigger).isEqualTo("14.306b.5891d1449-d168.a8ecf6009")
    assertThat((chat.steps[6] as TextStep).end).isEqualTo(false)

    assertThat((chat.steps[7] as UserInputStep).variable).isEqualTo("{number}")
    assertThat((chat.steps[7] as UserInputStep).trigger).isEqualTo("15.306b.5891d1449-d168.a8ecf6009")
    assertThat((chat.steps[7] as UserInputStep).end).isEqualTo(false)

    assertThat(chat.steps[8].id).isEqualTo("15.306b.5891d1449-d168.a8ecf6009")
    assertThat((chat.steps[8] as TextStep).trigger).isEqualTo("5.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[8] as TextStep).end).isEqualTo(false)
  }

  @Test
  fun `converts a simple mindmup response with user text input and boolean logic to chat`() {
    val fileId = "3UserInputBooleanLogic"
    val json = javaClass.getResource("/$fileId.mup").readText()
    whenever(negotiationAssetService.getChatFlow(fileId)).thenReturn(json)

    val chat = mindMupService.getMup(fileId).toChat()

    assertThat(chat.steps.size).isEqualTo(10)

    assertThat(chat.steps[0].id).isEqualTo("1")
    assertThat((chat.steps[0] as TextStep).end).isEqualTo(false)
    assertThat((chat.steps[0] as TextStep).trigger).isEqualTo("2.f745.dc70c5aaf-4010.f36e69ad1")

    assertThat(chat.steps[1].id).isEqualTo("2.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[1] as TextStep).trigger).isEqualTo("bd2a3223-1f24-3704-821e-94c7dab164f5")
    assertThat((chat.steps[1] as TextStep).end).isEqualTo(false)

    assertThat(chat.steps[2].id).isEqualTo("bd2a3223-1f24-3704-821e-94c7dab164f5")
    assertThat((chat.steps[2] as OptionsStep).options[0].value).isEqualTo("3.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[2] as OptionsStep).options[0].trigger).isEqualTo("5.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[2] as OptionsStep).options[1].value).isEqualTo("4.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[2] as OptionsStep).options[1].trigger).isEqualTo("6.f745.dc70c5aaf-4010.f36e69ad1")

    assertThat(chat.steps[3].id).isEqualTo("5.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[3] as TextStep).trigger).isNull()
    assertThat((chat.steps[3] as TextStep).end).isEqualTo(true)

    assertThat(chat.steps[4].id).isEqualTo("6.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[4] as TextStep).trigger).isEqualTo("9760dbb1-4058-3d9e-ae29-8dc72fe08624")
    assertThat((chat.steps[4] as TextStep).end).isEqualTo(false)

    assertThat(chat.steps[5].id).isEqualTo("9760dbb1-4058-3d9e-ae29-8dc72fe08624")
    assertThat((chat.steps[5] as OptionsStep).options[0].value).isEqualTo("10.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[5] as OptionsStep).options[0].trigger).isEqualTo("11.306b.5891d1449-d168.a8ecf6009")

    assertThat(chat.steps[6].id).isEqualTo("11.306b.5891d1449-d168.a8ecf6009")
    assertThat((chat.steps[6] as TextStep).trigger).isEqualTo("14.306b.5891d1449-d168.a8ecf6009")
    assertThat((chat.steps[6] as TextStep).end).isEqualTo(false)

    assertThat((chat.steps[7] as ConditionalUserInputStep).variable).isEqualTo("{number}")
    assertThat((chat.steps[7] as ConditionalUserInputStep).trigger).isEqualTo(
      mapOf(
        "value >= 100" to "15.306b.5891d1449-d168.a8ecf6009",
        "value < 100" to "17.6fe7.3162047-efd2.e2d2e9328"
      )
    )
    assertThat((chat.steps[7] as ConditionalUserInputStep).end).isEqualTo(false)

    assertThat(chat.steps[8].id).isEqualTo("15.306b.5891d1449-d168.a8ecf6009")
    assertThat((chat.steps[8] as TextStep).trigger).isEqualTo("5.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[8] as TextStep).end).isEqualTo(false)

    assertThat(chat.steps[9].id).isEqualTo("17.6fe7.3162047-efd2.e2d2e9328")
    assertThat((chat.steps[9] as TextStep).trigger).isEqualTo("5.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[9] as TextStep).end).isEqualTo(false)
  }

  @Test
  fun `works with backwards text links to user input steps`() {
    val fileId = "4BackwardsTextLinkToUserInputStep"
    val json = javaClass.getResource("/$fileId.mup").readText()
    whenever(negotiationAssetService.getChatFlow(fileId)).thenReturn(json)

    val chat = mindMupService.getMup(fileId).toChat()

    assertThat(chat.steps.size).isEqualTo(11)

    assertThat(chat.steps[0].id).isEqualTo("1")
    assertThat((chat.steps[0] as TextStep).end).isEqualTo(false)
    assertThat((chat.steps[0] as TextStep).trigger).isEqualTo("2.f745.dc70c5aaf-4010.f36e69ad1")

    assertThat(chat.steps[1].id).isEqualTo("2.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[1] as TextStep).trigger).isEqualTo("bd2a3223-1f24-3704-821e-94c7dab164f5")
    assertThat((chat.steps[1] as TextStep).end).isEqualTo(false)

    assertThat(chat.steps[2].id).isEqualTo("bd2a3223-1f24-3704-821e-94c7dab164f5")
    assertThat((chat.steps[2] as OptionsStep).options[0].value).isEqualTo("3.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[2] as OptionsStep).options[0].trigger).isEqualTo("5.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[2] as OptionsStep).options[1].value).isEqualTo("4.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[2] as OptionsStep).options[1].trigger).isEqualTo("6.f745.dc70c5aaf-4010.f36e69ad1")

    assertThat(chat.steps[3].id).isEqualTo("5.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[3] as TextStep).trigger).isNull()
    assertThat((chat.steps[3] as TextStep).end).isEqualTo(true)

    assertThat(chat.steps[4].id).isEqualTo("6.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[4] as TextStep).trigger).isEqualTo("9760dbb1-4058-3d9e-ae29-8dc72fe08624")
    assertThat((chat.steps[4] as TextStep).end).isEqualTo(false)

    assertThat(chat.steps[5].id).isEqualTo("9760dbb1-4058-3d9e-ae29-8dc72fe08624")
    assertThat((chat.steps[5] as OptionsStep).options[0].value).isEqualTo("10.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[5] as OptionsStep).options[0].trigger).isEqualTo("11.306b.5891d1449-d168.a8ecf6009")

    assertThat(chat.steps[6].id).isEqualTo("11.306b.5891d1449-d168.a8ecf6009")
    assertThat((chat.steps[6] as TextStep).trigger).isEqualTo("14.306b.5891d1449-d168.a8ecf6009")
    assertThat((chat.steps[6] as TextStep).end).isEqualTo(false)

    assertThat((chat.steps[7] as ConditionalUserInputStep).variable).isEqualTo("{number}")
    assertThat((chat.steps[7] as ConditionalUserInputStep).trigger).isEqualTo(
      mapOf(
        "value >= 100" to "15.306b.5891d1449-d168.a8ecf6009",
        "value < 100" to "17.6fe7.3162047-efd2.e2d2e9328"
      )
    )
    assertThat((chat.steps[7] as ConditionalUserInputStep).end).isEqualTo(false)

    assertThat(chat.steps[8].id).isEqualTo("15.306b.5891d1449-d168.a8ecf6009")
    assertThat((chat.steps[8] as TextStep).trigger).isEqualTo("5.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[8] as TextStep).end).isEqualTo(false)

    assertThat(chat.steps[9].id).isEqualTo("17.6fe7.3162047-efd2.e2d2e9328")
    assertThat((chat.steps[9] as TextStep).trigger).isEqualTo("20.f17a.1243f6ef6-6008.c8ac7e478")
    assertThat((chat.steps[9] as TextStep).end).isEqualTo(false)

    assertThat(chat.steps[10].id).isEqualTo("20.f17a.1243f6ef6-6008.c8ac7e478")
    assertThat((chat.steps[10] as TextStep).trigger).isEqualTo("14.306b.5891d1449-d168.a8ecf6009")
    assertThat((chat.steps[10] as TextStep).end).isEqualTo(false)
  }

  @Test
  fun `works with backwards option links to user input steps`() {
    val fileId = "5BackwardsOptionLinkToUserInputStep"
    val json = javaClass.getResource("/$fileId.mup").readText()
    whenever(negotiationAssetService.getChatFlow(fileId)).thenReturn(json)

    val chat = mindMupService.getMup(fileId).toChat()

    assertThat(chat.steps.size).isEqualTo(11)

    assertThat(chat.steps[0].id).isEqualTo("1")
    assertThat((chat.steps[0] as TextStep).end).isEqualTo(false)
    assertThat((chat.steps[0] as TextStep).trigger).isEqualTo("2.f745.dc70c5aaf-4010.f36e69ad1")

    assertThat(chat.steps[1].id).isEqualTo("2.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[1] as TextStep).trigger).isEqualTo("bd2a3223-1f24-3704-821e-94c7dab164f5")
    assertThat((chat.steps[1] as TextStep).end).isEqualTo(false)

    assertThat(chat.steps[2].id).isEqualTo("bd2a3223-1f24-3704-821e-94c7dab164f5")
    assertThat((chat.steps[2] as OptionsStep).options[0].value).isEqualTo("3.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[2] as OptionsStep).options[0].trigger).isEqualTo("5.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[2] as OptionsStep).options[1].value).isEqualTo("4.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[2] as OptionsStep).options[1].trigger).isEqualTo("6.f745.dc70c5aaf-4010.f36e69ad1")

    assertThat(chat.steps[3].id).isEqualTo("5.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[3] as TextStep).trigger).isNull()
    assertThat((chat.steps[3] as TextStep).end).isEqualTo(true)

    assertThat(chat.steps[4].id).isEqualTo("6.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[4] as TextStep).trigger).isEqualTo("9760dbb1-4058-3d9e-ae29-8dc72fe08624")
    assertThat((chat.steps[4] as TextStep).end).isEqualTo(false)

    assertThat(chat.steps[5].id).isEqualTo("9760dbb1-4058-3d9e-ae29-8dc72fe08624")
    assertThat((chat.steps[5] as OptionsStep).options[0].value).isEqualTo("10.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[5] as OptionsStep).options[0].trigger).isEqualTo("11.306b.5891d1449-d168.a8ecf6009")

    assertThat(chat.steps[6].id).isEqualTo("11.306b.5891d1449-d168.a8ecf6009")
    assertThat((chat.steps[6] as TextStep).trigger).isEqualTo("14.306b.5891d1449-d168.a8ecf6009")
    assertThat((chat.steps[6] as TextStep).end).isEqualTo(false)

    assertThat((chat.steps[7] as ConditionalUserInputStep).variable).isEqualTo("{number}")
    assertThat((chat.steps[7] as ConditionalUserInputStep).trigger).isEqualTo(
      mapOf(
        "value >= 100" to "15.306b.5891d1449-d168.a8ecf6009",
        "value < 100" to "17.6fe7.3162047-efd2.e2d2e9328"
      )
    )
    assertThat((chat.steps[7] as ConditionalUserInputStep).end).isEqualTo(false)

    assertThat(chat.steps[8].id).isEqualTo("15.306b.5891d1449-d168.a8ecf6009")
    assertThat((chat.steps[8] as TextStep).trigger).isEqualTo("5.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[8] as TextStep).end).isEqualTo(false)

    assertThat(chat.steps[9].id).isEqualTo("17.6fe7.3162047-efd2.e2d2e9328")
    assertThat((chat.steps[9] as TextStep).trigger).isEqualTo("833c1d31-dc67-3bf4-9d62-528c36af7236")
    assertThat((chat.steps[9] as TextStep).end).isEqualTo(false)

    assertThat(chat.steps[10].id).isEqualTo("833c1d31-dc67-3bf4-9d62-528c36af7236")
    assertThat((chat.steps[10] as OptionsStep).options[0].value).isEqualTo("20.f17a.1243f6ef6-6008.c8ac7e478")
    assertThat((chat.steps[10] as OptionsStep).options[0].trigger).isEqualTo("14.306b.5891d1449-d168.a8ecf6009")
    assertThat((chat.steps[10] as OptionsStep).end).isEqualTo(false)
  }

  @Test
  fun `works with multiple choice variables and values`() {
    val fileId = "6MultipleChoiceVariablesAndValues"
    val json = javaClass.getResource("/$fileId.mup").readText()
    whenever(negotiationAssetService.getChatFlow(fileId)).thenReturn(json)

    val chat = mindMupService.getMup(fileId).toChat()

    assertThat(chat.steps.size).isEqualTo(11)

    assertThat(chat.steps[0].id).isEqualTo("1")
    assertThat((chat.steps[0] as TextStep).end).isEqualTo(false)
    assertThat((chat.steps[0] as TextStep).trigger).isEqualTo("2.f745.dc70c5aaf-4010.f36e69ad1")

    assertThat(chat.steps[1].id).isEqualTo("2.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[1] as TextStep).trigger).isEqualTo(
      StepIDGenerator.idFrom("2.f745.dc70c5aaf-4010.f36e69ad1")
    )
    assertThat((chat.steps[1] as TextStep).end).isEqualTo(false)

    assertThat((chat.steps[2] as OptionsStep).variable).isEqualTo("{firstOption}")
    assertThat((chat.steps[2] as OptionsStep).options[0].value).isEqualTo("15")
    assertThat((chat.steps[2] as OptionsStep).options[0].trigger).isEqualTo("5.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[2] as OptionsStep).options[1].value).isEqualTo("30")
    assertThat((chat.steps[2] as OptionsStep).options[1].trigger).isEqualTo("6.f745.dc70c5aaf-4010.f36e69ad1")

    assertThat(chat.steps[3].id).isEqualTo("5.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[3] as TextStep).trigger).isNull()
    assertThat((chat.steps[3] as TextStep).end).isEqualTo(true)

    assertThat(chat.steps[4].id).isEqualTo("6.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[4] as TextStep).trigger).isEqualTo("9760dbb1-4058-3d9e-ae29-8dc72fe08624")
    assertThat((chat.steps[4] as TextStep).end).isEqualTo(false)

    assertThat(chat.steps[5].id).isEqualTo("9760dbb1-4058-3d9e-ae29-8dc72fe08624")
    assertThat((chat.steps[5] as OptionsStep).options[0].value).isEqualTo("10.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[5] as OptionsStep).options[0].trigger).isEqualTo("11.306b.5891d1449-d168.a8ecf6009")

    assertThat(chat.steps[6].id).isEqualTo("11.306b.5891d1449-d168.a8ecf6009")
    assertThat((chat.steps[6] as TextStep).trigger).isEqualTo("14.306b.5891d1449-d168.a8ecf6009")
    assertThat((chat.steps[6] as TextStep).end).isEqualTo(false)

    assertThat((chat.steps[7] as ConditionalUserInputStep).variable).isEqualTo("{number}")
    assertThat((chat.steps[7] as ConditionalUserInputStep).trigger).isEqualTo(
      mapOf(
        "value >= 100" to "15.306b.5891d1449-d168.a8ecf6009",
        "value < 100" to "17.6fe7.3162047-efd2.e2d2e9328"
      )
    )
    assertThat((chat.steps[7] as ConditionalUserInputStep).end).isEqualTo(false)

    assertThat(chat.steps[8].id).isEqualTo("15.306b.5891d1449-d168.a8ecf6009")
    assertThat((chat.steps[8] as TextStep).trigger).isEqualTo("5.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[8] as TextStep).end).isEqualTo(false)

    assertThat(chat.steps[9].id).isEqualTo("17.6fe7.3162047-efd2.e2d2e9328")
    assertThat((chat.steps[9] as TextStep).trigger).isEqualTo("833c1d31-dc67-3bf4-9d62-528c36af7236")
    assertThat((chat.steps[9] as TextStep).end).isEqualTo(false)

    assertThat(chat.steps[10].id).isEqualTo("833c1d31-dc67-3bf4-9d62-528c36af7236")
    assertThat((chat.steps[10] as OptionsStep).options[0].value).isEqualTo("20.f17a.1243f6ef6-6008.c8ac7e478")
    assertThat((chat.steps[10] as OptionsStep).options[0].trigger).isEqualTo("14.306b.5891d1449-d168.a8ecf6009")
    assertThat((chat.steps[10] as OptionsStep).end).isEqualTo(false)
  }

  @Test
  fun `works with backwards pointing conditionals`() {
    val fileId = "7BackwardLinksWithConditionals"
    val json = javaClass.getResource("/$fileId.mup").readText()
    whenever(negotiationAssetService.getChatFlow(fileId)).thenReturn(json)

    val chat = mindMupService.getMup(fileId).toChat()

    assertThat(chat.steps.size).isEqualTo(9)

    assertThat(chat.steps[0].id).isEqualTo("1")
    assertThat((chat.steps[0] as TextStep).end).isEqualTo(false)
    assertThat((chat.steps[0] as TextStep).trigger).isEqualTo("2.f745.dc70c5aaf-4010.f36e69ad1")

    assertThat(chat.steps[1].id).isEqualTo("2.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[1] as TextStep).trigger).isEqualTo("bd2a3223-1f24-3704-821e-94c7dab164f5")
    assertThat((chat.steps[1] as TextStep).end).isEqualTo(false)

    assertThat(chat.steps[2].id).isEqualTo("bd2a3223-1f24-3704-821e-94c7dab164f5")
    assertThat((chat.steps[2] as OptionsStep).options[0].value).isEqualTo("3.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[2] as OptionsStep).options[0].trigger).isEqualTo("5.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[2] as OptionsStep).options[1].value).isEqualTo("4.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[2] as OptionsStep).options[1].trigger).isEqualTo("6.f745.dc70c5aaf-4010.f36e69ad1")

    assertThat(chat.steps[3].id).isEqualTo("5.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[3] as TextStep).trigger).isNull()
    assertThat((chat.steps[3] as TextStep).end).isEqualTo(true)

    assertThat(chat.steps[4].id).isEqualTo("6.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[4] as TextStep).trigger).isEqualTo("9760dbb1-4058-3d9e-ae29-8dc72fe08624")
    assertThat((chat.steps[4] as TextStep).end).isEqualTo(false)

    assertThat(chat.steps[5].id).isEqualTo("9760dbb1-4058-3d9e-ae29-8dc72fe08624")
    assertThat((chat.steps[5] as OptionsStep).options[0].value).isEqualTo("10.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[5] as OptionsStep).options[0].trigger).isEqualTo("11.306b.5891d1449-d168.a8ecf6009")

    assertThat(chat.steps[6].id).isEqualTo("11.306b.5891d1449-d168.a8ecf6009")
    assertThat((chat.steps[6] as TextStep).trigger).isEqualTo("14.306b.5891d1449-d168.a8ecf6009")
    assertThat((chat.steps[6] as TextStep).end).isEqualTo(false)

    assertThat((chat.steps[7] as ConditionalUserInputStep).variable).isEqualTo("{number}")
    assertThat((chat.steps[7] as ConditionalUserInputStep).trigger).isEqualTo(
      mapOf(
        "value >= 100" to "15.306b.5891d1449-d168.a8ecf6009",
        "value < 100" to "6.f745.dc70c5aaf-4010.f36e69ad1"
      )
    )
    assertThat((chat.steps[7] as ConditionalUserInputStep).end).isEqualTo(false)

    assertThat(chat.steps[8].id).isEqualTo("15.306b.5891d1449-d168.a8ecf6009")
    assertThat((chat.steps[8] as TextStep).trigger).isEqualTo("5.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[8] as TextStep).end).isEqualTo(false)
  }

  @Test
  fun `works with updating existing variables`() {
    val fileId = "8UpdatingExistingVariables"
    val json = javaClass.getResource("/$fileId.mup").readText()
    whenever(negotiationAssetService.getChatFlow(fileId)).thenReturn(json)

    val chat = mindMupService.getMup(fileId).toChat()

    assertThat(chat.steps.size).isEqualTo(14)

    assertThat(chat.steps[0].id).isEqualTo("1")
    assertThat((chat.steps[0] as TextStep).end).isEqualTo(false)
    assertThat((chat.steps[0] as TextStep).trigger).isEqualTo("2.f745.dc70c5aaf-4010.f36e69ad1")

    assertThat(chat.steps[1].id).isEqualTo("2.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[1] as TextStep).trigger).isEqualTo("bd2a3223-1f24-3704-821e-94c7dab164f5")
    assertThat((chat.steps[1] as TextStep).end).isEqualTo(false)

    assertThat(chat.steps[2].id).isEqualTo("bd2a3223-1f24-3704-821e-94c7dab164f5")
    assertThat((chat.steps[2] as OptionsStep).options[0].value).isEqualTo("3.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[2] as OptionsStep).options[0].trigger).isEqualTo("5.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[2] as OptionsStep).options[1].value).isEqualTo("4.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[2] as OptionsStep).options[1].trigger).isEqualTo("6.f745.dc70c5aaf-4010.f36e69ad1")

    assertThat(chat.steps[3].id).isEqualTo("5.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[3] as TextStep).trigger).isNull()
    assertThat((chat.steps[3] as TextStep).end).isEqualTo(true)

    assertThat(chat.steps[4].id).isEqualTo("6.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[4] as TextStep).trigger).isEqualTo("9760dbb1-4058-3d9e-ae29-8dc72fe08624")
    assertThat((chat.steps[4] as TextStep).end).isEqualTo(false)

    assertThat(chat.steps[5].id).isEqualTo("9760dbb1-4058-3d9e-ae29-8dc72fe08624")
    assertThat((chat.steps[5] as OptionsStep).options[0].value).isEqualTo("10.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[5] as OptionsStep).options[0].trigger).isEqualTo("11.306b.5891d1449-d168.a8ecf6009")

    assertThat(chat.steps[6].id).isEqualTo("11.306b.5891d1449-d168.a8ecf6009")
    assertThat((chat.steps[6] as TextStep).trigger).isEqualTo("14.306b.5891d1449-d168.a8ecf6009")
    assertThat((chat.steps[6] as TextStep).end).isEqualTo(false)

    assertThat((chat.steps[7] as ConditionalUserInputStep).variable).isEqualTo("{number}")
    assertThat((chat.steps[7] as ConditionalUserInputStep).trigger).isEqualTo(
      mapOf(
        "value >= 100" to "15.306b.5891d1449-d168.a8ecf6009",
        "value < 100" to "17.6fe7.3162047-efd2.e2d2e9328"
      )
    )
    assertThat((chat.steps[7] as ConditionalUserInputStep).end).isEqualTo(false)

    assertThat(chat.steps[8].id).isEqualTo("15.306b.5891d1449-d168.a8ecf6009")
    assertThat((chat.steps[8] as TextStep).trigger).isEqualTo("5.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[8] as TextStep).end).isEqualTo(false)

    assertThat(chat.steps[9].id).isEqualTo("17.6fe7.3162047-efd2.e2d2e9328")
    assertThat((chat.steps[9] as TextStep).trigger).isEqualTo("20.f17a.1243f6ef6-6008.c8ac7e478")
    assertThat((chat.steps[9] as TextStep).end).isEqualTo(false)

    assertThat(chat.steps[10].id).isEqualTo("20.f17a.1243f6ef6-6008.c8ac7e478")
    assertThat((chat.steps[10] as TextStep).trigger).isEqualTo("21.4ac3.8ac44e2a-54f3.eefbc9f02")
    assertThat((chat.steps[10] as TextStep).end).isEqualTo(false)

    assertThat(chat.steps[11].id).isEqualTo("21.4ac3.8ac44e2a-54f3.eefbc9f02")
    assertThat((chat.steps[11] as ConditionalUserInputStep).variable).isEqualTo("{number}")
    assertThat((chat.steps[11] as ConditionalUserInputStep).trigger).isEqualTo(
      mapOf(
        "value >= 100" to "22.4ac3.8ac44e2a-54f3.eefbc9f02",
        "value < 100" to "25.4ac3.8ac44e2a-54f3.eefbc9f02"
      )
    )
    assertThat((chat.steps[11] as ConditionalUserInputStep).end).isEqualTo(false)

    assertThat(chat.steps[12].id).isEqualTo("22.4ac3.8ac44e2a-54f3.eefbc9f02")
    assertThat((chat.steps[12] as TextStep).trigger).isEqualTo("5.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[12] as TextStep).end).isEqualTo(false)

    assertThat(chat.steps[13].id).isEqualTo("25.4ac3.8ac44e2a-54f3.eefbc9f02")
    assertThat((chat.steps[13] as TextStep).trigger).isNull()
    assertThat((chat.steps[13] as TextStep).end).isEqualTo(true)
  }

  @Test
  fun `correctly orders ideas`() {
    val fileId = "9IdeaOrdering"
    val json = javaClass.getResource("/$fileId.mup").readText()
    whenever(negotiationAssetService.getChatFlow(fileId)).thenReturn(json)

    val chat = mindMupService.getMup(fileId).toChat()

    assertThat(chat.steps.size).isEqualTo(3)

    assertThat(chat.steps[0].id).isEqualTo("1")
    assertThat((chat.steps[0] as TextStep).end).isEqualTo(false)
    assertThat((chat.steps[0] as TextStep).trigger).isEqualTo("2.f745.dc70c5aaf-4010.f36e69ad1")

    assertThat(chat.steps[1].id).isEqualTo("2.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[1] as TextStep).trigger).isEqualTo("bd2a3223-1f24-3704-821e-94c7dab164f5")
    assertThat((chat.steps[1] as TextStep).end).isEqualTo(false)

    assertThat(chat.steps[2].id).isEqualTo("bd2a3223-1f24-3704-821e-94c7dab164f5")
    assertThat((chat.steps[2] as OptionsStep).options[0].value).isEqualTo("4.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[2] as OptionsStep).options[1].value).isEqualTo("3.f745.dc70c5aaf-4010.f36e69ad1")
  }

  @Test
  fun `can branch based on existing variables`() {
    val fileId = "10BranchBasedOnPreviousVariables"
    val json = javaClass.getResource("/$fileId.mup").readText()
    whenever(negotiationAssetService.getChatFlow(fileId)).thenReturn(json)

    val chat = mindMupService.getMup(fileId).toChat()

    assertThat(chat.steps.size).isEqualTo(9)

    assertThat(chat.steps[0].id).isEqualTo("1")
    assertThat((chat.steps[0] as TextStep).end).isEqualTo(false)
    assertThat((chat.steps[0] as TextStep).trigger).isEqualTo("2.f745.dc70c5aaf-4010.f36e69ad1")

    assertThat(chat.steps[1].id).isEqualTo("2.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[1] as TextStep).trigger).isEqualTo(
      StepIDGenerator.idFrom("2.f745.dc70c5aaf-4010.f36e69ad1")
    )
    assertThat((chat.steps[1] as TextStep).end).isEqualTo(false)

    assertThat((chat.steps[2] as OptionsStep).variable).isEqualTo("{firstOption}")
    assertThat((chat.steps[2] as OptionsStep).options[0].value).isEqualTo("200")
    assertThat((chat.steps[2] as OptionsStep).options[0].trigger).isEqualTo("11.306b.5891d1449-d168.a8ecf6009")
    assertThat((chat.steps[2] as OptionsStep).options[1].value).isEqualTo("30")
    assertThat((chat.steps[2] as OptionsStep).options[1].trigger).isEqualTo("6.f745.dc70c5aaf-4010.f36e69ad1")

    assertThat(chat.steps[3].id).isEqualTo("6.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[3] as TextStep).trigger).isEqualTo("9760dbb1-4058-3d9e-ae29-8dc72fe08624")
    assertThat((chat.steps[3] as TextStep).end).isEqualTo(false)

    assertThat(chat.steps[4].id).isEqualTo("9760dbb1-4058-3d9e-ae29-8dc72fe08624")
    assertThat((chat.steps[4] as OptionsStep).options[0].value).isEqualTo("10.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[4] as OptionsStep).options[0].trigger).isEqualTo("11.306b.5891d1449-d168.a8ecf6009")

    assertThat(chat.steps[5].id).isEqualTo("11.306b.5891d1449-d168.a8ecf6009")
    assertThat((chat.steps[5] as ConditionalTextStep).trigger).isEqualTo(
      mapOf(
        """steps["{firstOption}"].value >= 100""" to "15.306b.5891d1449-d168.a8ecf6009",
        """steps["{firstOption}"].value < 100""" to "17.6fe7.3162047-efd2.e2d2e9328"
      )
    )
    assertThat((chat.steps[5] as ConditionalTextStep).end).isEqualTo(false)

    assertThat(chat.steps[6].id).isEqualTo("15.306b.5891d1449-d168.a8ecf6009")
    assertThat((chat.steps[6] as TextStep).trigger).isEqualTo("5.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[6] as TextStep).end).isEqualTo(false)

    assertThat(chat.steps[7].id).isEqualTo("17.6fe7.3162047-efd2.e2d2e9328")
    assertThat((chat.steps[7] as TextStep).trigger).isEqualTo("5.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[7] as TextStep).end).isEqualTo(false)

    assertThat(chat.steps[8].id).isEqualTo("5.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[8] as TextStep).trigger).isNull()
    assertThat((chat.steps[8] as TextStep).end).isEqualTo(true)
  }

  @Test
  fun `works with updating existing variables with multiple choice questions`() {
    val fileId = "11UpdatingExistingVariablesWithMultipleChoice"
    val json = javaClass.getResource("/$fileId.mup").readText()
    whenever(negotiationAssetService.getChatFlow(fileId)).thenReturn(json)

    val chat = mindMupService.getMup(fileId).toChat()

    assertThat(chat.steps.size).isEqualTo(6)

    assertThat(chat.steps[0].id).isEqualTo("1")
    assertThat((chat.steps[0] as TextStep).end).isEqualTo(false)
    assertThat((chat.steps[0] as TextStep).trigger).isEqualTo("2.f745.dc70c5aaf-4010.f36e69ad1")

    assertThat(chat.steps[1].id).isEqualTo("2.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[1] as TextStep).trigger).isEqualTo(
      StepIDGenerator.idFrom("2.f745.dc70c5aaf-4010.f36e69ad1")
    )
    assertThat((chat.steps[1] as TextStep).end).isEqualTo(false)

    assertThat((chat.steps[2] as OptionsStep).variable).isEqualTo("{firstOption}")
    assertThat((chat.steps[2] as OptionsStep).options[0].value).isEqualTo("15")
    assertThat((chat.steps[2] as OptionsStep).options[0].trigger).isEqualTo("5.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[2] as OptionsStep).options[1].value).isEqualTo("30")
    assertThat((chat.steps[2] as OptionsStep).options[1].trigger).isEqualTo("41.0372.728fb8bd3-2d8c.f333f6a69")

    assertThat(chat.steps[3].id).isEqualTo("5.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[3] as TextStep).trigger).isNull()
    assertThat((chat.steps[3] as TextStep).end).isEqualTo(true)

    assertThat(chat.steps[4].id).isEqualTo("41.0372.728fb8bd3-2d8c.f333f6a69")
    assertThat((chat.steps[4] as TextStep).trigger).isEqualTo("2243f30e-70bd-396b-a090-13ea216d915e")
    assertThat((chat.steps[4] as TextStep).end).isEqualTo(false)

    assertThat(chat.steps[5].id).isEqualTo("2243f30e-70bd-396b-a090-13ea216d915e")
    assertThat((chat.steps[5] as OptionsStep).variable).isEqualTo("{firstOption}")
    assertThat((chat.steps[5] as OptionsStep).options[0].value).isEqualTo("45")
    assertThat((chat.steps[5] as OptionsStep).options[0].trigger).isEqualTo(
      "5.f745.dc70c5aaf-4010.f36e69ad1"
    )
    assertThat((chat.steps[5] as OptionsStep).options[1].value).isEqualTo("60")
    assertThat((chat.steps[5] as OptionsStep).options[1].trigger).isEqualTo(
      "5.f745.dc70c5aaf-4010.f36e69ad1"
    )
    assertThat((chat.steps[5] as OptionsStep).end).isEqualTo(false)
  }

  @Test
  fun `can save multiple variables at once`() {
    val fileId = "12SaveMultipleVariablesAtOnce"
    val json = javaClass.getResource("/$fileId.mup").readText()
    whenever(negotiationAssetService.getChatFlow(fileId)).thenReturn(json)

    val chat = mindMupService.getMup(fileId).toChat()

    assertThat(chat.steps.size).isEqualTo(4)

    assertThat(chat.steps[0].id).isEqualTo("1")
    assertThat((chat.steps[0] as TextStep).end).isEqualTo(false)
    assertThat((chat.steps[0] as TextStep).trigger).isEqualTo("2.f745.dc70c5aaf-4010.f36e69ad1")

    assertThat(chat.steps[1].id).isEqualTo("2.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[1] as TextStep).trigger).isEqualTo(StepIDGenerator.idFrom("2.f745.dc70c5aaf-4010.f36e69ad1"))
    assertThat((chat.steps[1] as TextStep).end).isEqualTo(false)

    assertThat((chat.steps[2] as OptionsStep).variable).isEqualTo("{variables}")
    assertThat((chat.steps[2] as OptionsStep).options[0].value).isEqualTo(
      mapOf(
        "fee" to 15,
        "days" to 3
      )
    )
    assertThat((chat.steps[2] as OptionsStep).options[0].trigger).isEqualTo("5.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[2] as OptionsStep).options[1].value).isEqualTo(
      mapOf(
        "fee" to 30,
        "days" to 1
      )
    )
    assertThat((chat.steps[2] as OptionsStep).options[1].trigger).isEqualTo("5.f745.dc70c5aaf-4010.f36e69ad1")

    assertThat(chat.steps[3].id).isEqualTo("5.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[3] as TextStep).trigger).isNull()
    assertThat((chat.steps[3] as TextStep).end).isEqualTo(true)
  }

  @Test
  fun `can update multiple variables at once`() {
    val fileId = "13UpdateMultipleVariablesAtOnce"
    val json = javaClass.getResource("/$fileId.mup").readText()
    whenever(negotiationAssetService.getChatFlow(fileId)).thenReturn(json)

    val chat = mindMupService.getMup(fileId).toChat()

    assertThat(chat.steps.size).isEqualTo(7)

    assertThat(chat.steps[0].id).isEqualTo("1")
    assertThat((chat.steps[0] as TextStep).end).isEqualTo(false)
    assertThat((chat.steps[0] as TextStep).trigger).isEqualTo("2.f745.dc70c5aaf-4010.f36e69ad1")

    assertThat(chat.steps[1].id).isEqualTo("2.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[1] as TextStep).trigger).isEqualTo(StepIDGenerator.idFrom("2.f745.dc70c5aaf-4010.f36e69ad1"))
    assertThat((chat.steps[1] as TextStep).end).isEqualTo(false)

    assertThat((chat.steps[2] as OptionsStep).variable).isEqualTo("{variables}")
    assertThat((chat.steps[2] as OptionsStep).options[0].value).isEqualTo(
      mapOf(
        "fee" to 15,
        "days" to 3
      )
    )
    assertThat((chat.steps[2] as OptionsStep).options[0].trigger).isEqualTo("5.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[2] as OptionsStep).options[1].value).isEqualTo(
      mapOf(
        "fee" to 30,
        "days" to 1
      )
    )
    assertThat((chat.steps[2] as OptionsStep).options[1].trigger).isEqualTo("5.f745.dc70c5aaf-4010.f36e69ad1")

    assertThat(chat.steps[3].id).isEqualTo("5.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[3] as TextStep).trigger).isEqualTo("6.0d00.f4f7fc513-6505.865edf1f5")
    assertThat((chat.steps[3] as TextStep).end).isEqualTo(false)

    assertThat(chat.steps[4].id).isEqualTo("6.0d00.f4f7fc513-6505.865edf1f5")
    assertThat((chat.steps[4] as TextStep).trigger).isEqualTo("2bcc4b03-f23e-337c-9830-fe430d69901b")
    assertThat((chat.steps[4] as TextStep).end).isEqualTo(false)

    assertThat(chat.steps[5].id).isEqualTo("2bcc4b03-f23e-337c-9830-fe430d69901b")
    assertThat((chat.steps[5] as OptionsStep).options[0].value).isEqualTo(
      mapOf(
        "fee" to 16,
        "days" to 4
      )
    )
    assertThat((chat.steps[5] as OptionsStep).options[0].trigger)
      .isEqualTo("8.0d00.f4f7fc513-6505.865edf1f5")
    assertThat((chat.steps[5] as OptionsStep).options[1].value).isEqualTo(
      mapOf(
        "fee" to 31,
        "days" to 2
      )
    )
    assertThat((chat.steps[5] as OptionsStep).options[1].trigger)
      .isEqualTo("8.0d00.f4f7fc513-6505.865edf1f5")

    assertThat(chat.steps[6].id).isEqualTo("8.0d00.f4f7fc513-6505.865edf1f5")
    assertThat((chat.steps[6] as TextStep).trigger).isNull()
    assertThat((chat.steps[6] as TextStep).end).isEqualTo(true)
  }

  @Test
  fun `works with empty multiple choice values`() {
    val fileId = "15MissingValuesFromMultipleChoice"
    val json = javaClass.getResource("/$fileId.mup").readText()
    whenever(negotiationAssetService.getChatFlow(fileId)).thenReturn(json)

    val chat = mindMupService.getMup(fileId).toChat()

    assertThat(chat.steps.size).isEqualTo(4)

    assertThat(chat.steps[0].id).isEqualTo("1")
    assertThat((chat.steps[0] as TextStep).end).isEqualTo(false)
    assertThat((chat.steps[0] as TextStep).trigger).isEqualTo("2.f745.dc70c5aaf-4010.f36e69ad1")

    assertThat(chat.steps[1].id).isEqualTo("2.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[1] as TextStep).trigger).isEqualTo(
      StepIDGenerator.idFrom("2.f745.dc70c5aaf-4010.f36e69ad1")
    )
    assertThat((chat.steps[1] as TextStep).end).isEqualTo(false)

    assertThat((chat.steps[2] as OptionsStep).variable).isEqualTo("{firstOption}")
    assertThat((chat.steps[2] as OptionsStep).options[0].value).isEqualTo("3.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[2] as OptionsStep).options[0].trigger).isEqualTo("5.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[2] as OptionsStep).options[1].value).isEqualTo("30")
    assertThat((chat.steps[2] as OptionsStep).options[1].trigger).isEqualTo("5.f745.dc70c5aaf-4010.f36e69ad1")

    assertThat(chat.steps[3].id).isEqualTo("5.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[3] as TextStep).trigger).isNull()
    assertThat((chat.steps[3] as TextStep).end).isEqualTo(true)
  }

  @Test
  fun `works with updating existing user input variables with multiple choice`() {
    val fileId = "16UpdatingExistingUserVariablesWithMultipleChoice"
    val json = javaClass.getResource("/$fileId.mup").readText()
    whenever(negotiationAssetService.getChatFlow(fileId)).thenReturn(json)

    val chat = mindMupService.getMup(fileId).toChat()

    assertThat(chat.steps.size).isEqualTo(7)

    assertThat(chat.steps[0].id).isEqualTo("1")
    assertThat((chat.steps[0] as TextStep).end).isEqualTo(false)
    assertThat((chat.steps[0] as TextStep).trigger).isEqualTo("2.f745.dc70c5aaf-4010.f36e69ad1")

    assertThat(chat.steps[1].id).isEqualTo("2.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[1] as TextStep).trigger).isEqualTo("45.1bc3.bfbfa4b98-cedb.350171cef")
    assertThat((chat.steps[1] as TextStep).end).isEqualTo(false)

    assertThat((chat.steps[2] as ConditionalUserInputStep).evalExpression).isEqualTo("1==1")
    assertThat((chat.steps[2] as ConditionalUserInputStep).variable).isEqualTo("{number}")
    assertThat((chat.steps[2] as ConditionalUserInputStep).trigger).isEqualTo(
      mapOf(
        "value >= 100" to "46.1bc3.bfbfa4b98-cedb.350171cef",
        "value < 100" to "47.1bc3.bfbfa4b98-cedb.350171cef"
      )
    )
    assertThat((chat.steps[2] as ConditionalUserInputStep).end).isEqualTo(false)

    assertThat(chat.steps[3].id).isEqualTo("46.1bc3.bfbfa4b98-cedb.350171cef")
    assertThat((chat.steps[3] as TextStep).trigger).isNull()
    assertThat((chat.steps[3] as TextStep).end).isEqualTo(true)

    assertThat(chat.steps[4].id).isEqualTo("47.1bc3.bfbfa4b98-cedb.350171cef")
    assertThat((chat.steps[4] as TextStep).trigger).isEqualTo("52.1bc3.bfbfa4b98-cedb.350171cef")
    assertThat((chat.steps[4] as TextStep).end).isEqualTo(false)

    assertThat(chat.steps[5].id).isEqualTo("52.1bc3.bfbfa4b98-cedb.350171cef")
    assertThat((chat.steps[5] as TextStep).trigger).isEqualTo("5abc3af8-ab2c-3f3b-9a8e-ff0b17b55fbd")
    assertThat((chat.steps[5] as TextStep).end).isEqualTo(false)

    assertThat((chat.steps[6] as OptionsStep).id).isEqualTo("5abc3af8-ab2c-3f3b-9a8e-ff0b17b55fbd")
    assertThat((chat.steps[6] as OptionsStep).evalExpression).isEqualTo("1==1")
    assertThat((chat.steps[6] as OptionsStep).variable).isEqualTo("{number}")
    assertThat((chat.steps[6] as OptionsStep).options[0].value).isEqualTo("45")
    assertThat((chat.steps[6] as OptionsStep).options[0].trigger).isEqualTo(
      "46.1bc3.bfbfa4b98-cedb.350171cef"
    )
    assertThat((chat.steps[6] as OptionsStep).options[1].value).isEqualTo("60")
    assertThat((chat.steps[6] as OptionsStep).options[1].trigger).isEqualTo(
      "46.1bc3.bfbfa4b98-cedb.350171cef"
    )
    assertThat((chat.steps[6] as OptionsStep).end).isEqualTo(false)
  }

  @Test
  fun `works with updating existing multiple choice variables with user input`() {
    val fileId = "17UpdatingExistingMultipleChoiceVariablesWithUserInput"
    val json = javaClass.getResource("/$fileId.mup").readText()
    whenever(negotiationAssetService.getChatFlow(fileId)).thenReturn(json)

    val chat = mindMupService.getMup(fileId).toChat()

    assertThat(chat.steps.size).isEqualTo(7)

    assertThat(chat.steps[0].id).isEqualTo("1")
    assertThat((chat.steps[0] as TextStep).end).isEqualTo(false)
    assertThat((chat.steps[0] as TextStep).trigger).isEqualTo("62.4ef8.59253373a-1e26.eab76fe05")

    assertThat(chat.steps[1].id).isEqualTo("62.4ef8.59253373a-1e26.eab76fe05")
    assertThat((chat.steps[1] as TextStep).trigger).isEqualTo("4a7acde4-c75a-3d1c-821f-083455e1bcca")
    assertThat((chat.steps[1] as TextStep).end).isEqualTo(false)

    assertThat(chat.steps[2].id).isEqualTo("4a7acde4-c75a-3d1c-821f-083455e1bcca")
    assertThat((chat.steps[2] as OptionsStep).variable).isEqualTo("{number}")
    assertThat((chat.steps[2] as OptionsStep).options[0].value).isEqualTo("45")
    assertThat((chat.steps[2] as OptionsStep).options[0].trigger).isEqualTo(
      "65.4ef8.59253373a-1e26.eab76fe05"
    )
    assertThat((chat.steps[2] as OptionsStep).options[1].value).isEqualTo("60")
    assertThat((chat.steps[2] as OptionsStep).options[1].trigger).isEqualTo(
      "65.4ef8.59253373a-1e26.eab76fe05"
    )
    assertThat((chat.steps[2] as OptionsStep).end).isEqualTo(false)

    assertThat(chat.steps[3].id).isEqualTo("65.4ef8.59253373a-1e26.eab76fe05")
    assertThat((chat.steps[3] as TextStep).trigger).isEqualTo("66.4ef8.59253373a-1e26.eab76fe05")
    assertThat((chat.steps[3] as TextStep).end).isEqualTo(false)

    assertThat((chat.steps[4] as ConditionalUserInputStep).variable).isEqualTo("{number}")
    assertThat((chat.steps[4] as ConditionalUserInputStep).trigger).isEqualTo(
      mapOf(
        "value >= 100" to "67.4ef8.59253373a-1e26.eab76fe05",
        "value < 100" to "68.4ef8.59253373a-1e26.eab76fe05"
      )
    )
    assertThat((chat.steps[4] as ConditionalUserInputStep).end).isEqualTo(false)

    assertThat(chat.steps[5].id).isEqualTo("67.4ef8.59253373a-1e26.eab76fe05")
    assertThat((chat.steps[5] as TextStep).trigger).isNull()
    assertThat((chat.steps[5] as TextStep).end).isEqualTo(true)

    assertThat(chat.steps[6].id).isEqualTo("68.4ef8.59253373a-1e26.eab76fe05")
    assertThat((chat.steps[6] as TextStep).trigger).isNull()
    assertThat((chat.steps[6] as TextStep).end).isEqualTo(true)
  }

  @Test
  fun `works with template`() {
    val templateFileId = "1SimpleTemplate"
    val patternFileId = "1SimplePattern"
    val templateFileJson = javaClass.getResource("/template/$templateFileId.mup").readText()
    val patternFileJson = javaClass.getResource("/pattern/$patternFileId.mup").readText()

    whenever(negotiationAssetService.getChatFlow(templateFileId)).thenReturn(templateFileJson)
    whenever(negotiationAssetService.getChatFlow(patternFileId)).thenReturn(patternFileJson)

    val chat = mindMupService.getMup(templateFileId).toChat()

    assertThat(chat.steps.size).isEqualTo(8)

    assertThat(chat.steps[0].id).isEqualTo("1")
    assertThat((chat.steps[0] as TextStep).trigger).isEqualTo("2.7471.fb90c5909-a082.f1214b64d")

    assertThat(chat.steps[1].id).isEqualTo("2.7471.fb90c5909-a082.f1214b64d")
    assertThat((chat.steps[1] as TextStep).trigger).isEqualTo("2.7472.f745.dc70c5aaf-4010.f36e69ad1")

    assertThat(chat.steps[2].id).isEqualTo("2.7472.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[2] as TextStep).trigger).isEqualTo("6dad502b-593a-304f-9eef-b189eb34d68b")

    assertThat(chat.steps[3].id).isEqualTo("6dad502b-593a-304f-9eef-b189eb34d68b")
    assertThat((chat.steps[3] as OptionsStep).options[0].value).isEqualTo("2.7473.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[3] as OptionsStep).options[0].trigger).isEqualTo("2.7475.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[3] as OptionsStep).options[1].value).isEqualTo("2.7474.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[3] as OptionsStep).options[1].trigger).isEqualTo("2.7476.f745.dc70c5aaf-4010.f36e69ad1")

    assertThat(chat.steps[4].id).isEqualTo("2.7475.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[4] as TextStep).trigger).isEqualTo("3.8909.244021be6-c712.4f8d3e3a3")

    assertThat(chat.steps[5].id).isEqualTo("3.8909.244021be6-c712.4f8d3e3a3")
    assertThat((chat.steps[5] as TextStep).trigger).isNull()

    assertThat(chat.steps[6].id).isEqualTo("2.7476.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[6] as TextStep).trigger).isEqualTo("6a592db2-440b-3634-8b32-892100ab07bb")

    assertThat(chat.steps[7].id).isEqualTo("6a592db2-440b-3634-8b32-892100ab07bb")
    assertThat((chat.steps[7] as OptionsStep).options[0].value).isEqualTo("2.74710.f745.dc70c5aaf-4010.f36e69ad1")
    assertThat((chat.steps[7] as OptionsStep).options[0].trigger).isEqualTo("2.7475.f745.dc70c5aaf-4010.f36e69ad1")
  }

  @Test
  fun `works with evaluating notes as Javascript`() {
    val fileId = "19EvaluateNotesAsJavaScript"
    val json = javaClass.getResource("/$fileId.mup").readText()
    whenever(negotiationAssetService.getChatFlow(fileId)).thenReturn(json)

    val chat = mindMupService.getMup(fileId).toChat()

    val textStepThatAddsOne = chat.steps.find { it.id == "5.f745.dc70c5aaf-4010.f36e69ad1" }
    assertThat(textStepThatAddsOne).isNotNull
    assertThat(textStepThatAddsOne?.evalExpression)
      .isEqualTo("values[\"{terms}\"] = {\n  term1: previousValues[\"{terms}\"].term1 + 1\n}")

    val textStepThatAddsTwo = chat.steps.find { it.id == "6.f745.dc70c5aaf-4010.f36e69ad1" }
    assertThat(textStepThatAddsTwo).isNotNull
    assertThat(textStepThatAddsTwo?.evalExpression)
      .isEqualTo("values[\"{terms}\"] = {\n  term1: previousValues[\"{terms}\"].term1 + 2\n}")

    for (step in chat.steps.filter { it != textStepThatAddsOne && it != textStepThatAddsTwo }) {
      assertThat(step.evalExpression).isNull()
    }
  }

  @Test
  fun `works with evaluating notes as Javascript for a bigger mindmup`() {
    val fileId = "20WithMoreStepsEvaluateNotesAsJavaScript"
    val json = javaClass.getResource("/$fileId.mup").readText()
    whenever(negotiationAssetService.getChatFlow(fileId)).thenReturn(json)

    val chat = mindMupService.getMup(fileId).toChat()

    val firstSalaryUserInputStep =
      chat.steps.find {
        it is VariableHolder &&
          it.variable == "{salary}" &&
          it.evalExpression == "values[\"{enteringSalary}\"] = true;"
      }
    assertThat(firstSalaryUserInputStep).isNotNull
    assertThat(firstSalaryUserInputStep is UserInputStep).isTrue()

    val secondSalaryUserInputStep =
      chat.steps.find {
        it is VariableHolder &&
        it.variable == "{salary}" &&
        it.evalExpression == "values[\"{reEnteringSalary}\"] = true;"
      }
    assertThat(secondSalaryUserInputStep).isNotNull
    assertThat(secondSalaryUserInputStep is UserInputStep).isTrue()

    val ageConditionalUserInputStep =
      chat.steps.find {
        it is VariableHolder &&
        it.variable == "{age}" &&
        it.evalExpression == "values[\"{enteringAge}\"] = true;"
      }
    assertThat(ageConditionalUserInputStep).isNotNull
    assertThat(ageConditionalUserInputStep is ConditionalUserInputStep).isTrue()

    val interestRateConditionalTextStep = chat.steps.find { it.id == "115.7a2c.863782733-9f5f.0addd95c1" }
    assertThat(interestRateConditionalTextStep).isNotNull
    assertThat(interestRateConditionalTextStep is ConditionalTextStep).isTrue()
    assertThat(interestRateConditionalTextStep?.evalExpression)
      .isEqualTo("values[\"{offeringInterestRate\"] = true;")

    for (step in chat.steps.filter {
      it != firstSalaryUserInputStep &&
        it != secondSalaryUserInputStep &&
        it != ageConditionalUserInputStep &&
        it != interestRateConditionalTextStep
    }) {
      assertThat(step.evalExpression).isNull()
    }
  }

  @Test
  fun `works with Metadata in Note`() {
    val fileId = "21MetadataInNote"
    val json = javaClass.getResource("/$fileId.mup").readText()
    whenever(negotiationAssetService.getChatFlow(fileId)).thenReturn(json)

    val chat = mindMupService.getMup(fileId).toChat()

    val firstTextStep = chat.findStepById("120.7e96.caf729688-8c73.187d5840d") as? TextStep
    assertThat(firstTextStep).isNotNull
    assertThat(firstTextStep?.message).isEqualTo(
      "Hi! This is the proper response after being overwritten by Metadata. Please enter your salary!"
    )

    val salaryUserStep = chat.findStepByVariable("{salary}") as? ConditionalUserInputStep
    assertThat(salaryUserStep).isNotNull
    assertThat(salaryUserStep?.format).isEqualTo("currency")
  }

  @Test
  fun `converts mindmup with Multiple Choice steps into chat`() {
    val fileId = "22MultipleChoiceComponent"
    val json = javaClass.getResource("/$fileId.mup").readText()
    whenever(negotiationAssetService.getChatFlow(fileId)).thenReturn(json)

    val chat = mindMupService.getMup(fileId).toChat()

    val firstPromptTextStep = chat.findStepById("120.7e96.caf729688-8c73.187d5840d") as? TextStep
    assertThat(firstPromptTextStep).isNotNull
    assertThat(firstPromptTextStep?.message).isEqualTo("What would you like in your breakfast?")

    val conditionalMultipleChoiceStep = chat.findStepByVariable("{breakfast}") as? ConditionalMultipleChoiceStep
    assertThat(conditionalMultipleChoiceStep).isNotNull
    assertThat(conditionalMultipleChoiceStep?.minChoices).isEqualTo(1)
    assertThat(conditionalMultipleChoiceStep?.maxChoices).isEqualTo(5)
    assertThat(conditionalMultipleChoiceStep?.choices?.get(0)?.label).isEqualTo("Egg")
    assertThat(conditionalMultipleChoiceStep?.choices?.get(0)?.value).isEqualTo("egg")
    assertThat(conditionalMultipleChoiceStep?.choices?.get(1)?.label).isEqualTo("Toast")
    assertThat(conditionalMultipleChoiceStep?.choices?.get(1)?.value).isEqualTo("toast")
    assertThat(conditionalMultipleChoiceStep?.choices?.get(2)?.label).isEqualTo("Coffee")
    assertThat(conditionalMultipleChoiceStep?.choices?.get(2)?.value).isEqualTo("coffee")
    assertThat(conditionalMultipleChoiceStep?.choices?.get(3)?.label).isEqualTo("Sandwich")
    assertThat(conditionalMultipleChoiceStep?.choices?.get(3)?.value).isEqualTo("sandwich")
    assertThat(conditionalMultipleChoiceStep?.choices?.get(4)?.label).isEqualTo("Fruits")
    assertThat(conditionalMultipleChoiceStep?.choices?.get(4)?.value).isEqualTo("fruits")
    assertThat(conditionalMultipleChoiceStep?.choices?.get(5)?.label).isEqualTo("Beer")
    assertThat(conditionalMultipleChoiceStep?.choices?.get(5)?.value).isEqualTo("beer")

    val unhealthyEatingMessage = chat.steps.find { it.id == "125.319f.0cecdfb52-f896.17c337d6" } as? TextStep
    assertThat(unhealthyEatingMessage).isNotNull
    assertThat(unhealthyEatingMessage?.message).isEqualTo("You eat unhealthy")

    val improveEatingMessage = chat.steps.find { it.id == "126.319f.0cecdfb52-f896.17c337d6" } as? TextStep
    assertThat(improveEatingMessage).isNotNull
    assertThat(improveEatingMessage?.message).isEqualTo("Your eating habit can improve")

    val healthyEatingMessage = chat.steps.find { it.id == "127.319f.0cecdfb52-f896.17c337d6" } as? TextStep
    assertThat(healthyEatingMessage).isNotNull
    assertThat(healthyEatingMessage?.message).isEqualTo("You eat healthy")

    val secondPromptTextStep = chat.steps.find { it.id == "128.e1e5.e10012828-4b2b.d0ba381c1" } as? TextStep
    assertThat(secondPromptTextStep).isNotNull
    assertThat(secondPromptTextStep?.message).isEqualTo("Which continents have you been to?")

    val simpleMultipleChoiceStep = chat.steps.find {
      it.id == "129.e1e5.e10012828-4b2b.d0ba381c1"
    } as? SimpleMultipleChoiceStep

    assertThat(simpleMultipleChoiceStep).isNotNull
    assertThat(simpleMultipleChoiceStep?.minChoices).isEqualTo(1)
    assertThat(simpleMultipleChoiceStep?.maxChoices).isEqualTo(Int.MAX_VALUE)
    assertThat(simpleMultipleChoiceStep?.choices).containsExactlyInAnyOrder(
      MultipleChoiceStep.RegularChoice("Asia", "Asia"),
      MultipleChoiceStep.RegularChoice("North America", "North America"),
      MultipleChoiceStep.RegularChoice("South America", "South America"),
      MultipleChoiceStep.RegularChoice("Africa", "Africa"),
      MultipleChoiceStep.RegularChoice("Antarctica", "Antarctica"),
      MultipleChoiceStep.RegularChoice("Australia", "Australia"),
      MultipleChoiceStep.RegularChoice("Europe", "Europe")
    )

    val endTextStep = chat.steps.find { it.id == "130.e1e5.e10012828-4b2b.d0ba381c1" } as? TextStep
    assertThat(endTextStep).isNotNull
    assertThat(endTextStep?.message).isEqualTo(
      "You've visited places like {visitedPlace0} among many others"
    )
    assertThat(endTextStep?.end).isTrue()

    // check triggers
    assertThat(firstPromptTextStep?.trigger).isEqualTo(conditionalMultipleChoiceStep?.id)

    val trigger = conditionalMultipleChoiceStep?.trigger
    assertThat(trigger).containsExactlyEntriesOf(
      mapOf(
        "value.includes(\"beer\") || value.length >= 5 || value.length >= 4 && !value.includes(\"fruits\")"
to unhealthyEatingMessage?.id,
        "value.length >= 2 && value.length < 5 && !value.includes(\"fruits\")" to improveEatingMessage?.id,
        "value.includes(\"fruits\") || value.length < 2" to healthyEatingMessage?.id
      )
    )

    assertThat(unhealthyEatingMessage?.trigger).isEqualTo(secondPromptTextStep?.id)
    assertThat(improveEatingMessage?.trigger).isEqualTo(secondPromptTextStep?.id)
    assertThat(healthyEatingMessage?.trigger).isEqualTo(secondPromptTextStep?.id)

    assertThat(secondPromptTextStep?.trigger).isEqualTo(simpleMultipleChoiceStep?.id)

    assertThat(simpleMultipleChoiceStep?.trigger).isEqualTo(endTextStep?.id)
  }

  @Test
  fun `converts MindMup with Multiple choice steps with JSON values into chat`() {
    val fileId = "23JSONValuesInMultipleChoiceComponent"
    val json = javaClass.getResource("/$fileId.mup").readText()
    whenever(negotiationAssetService.getChatFlow(fileId)).thenReturn(json)

    val chat = mindMupService.getMup(fileId).toChat()

    val simpleMultipleChoiceStep = chat.findStepByVariable("{cart}") as? SimpleMultipleChoiceStep
    assertThat(simpleMultipleChoiceStep).isNotNull
    assertThat(simpleMultipleChoiceStep?.maxChoices).isEqualTo(Int.MAX_VALUE)
    assertThat(simpleMultipleChoiceStep?.minChoices).isEqualTo(0)
    assertThat(simpleMultipleChoiceStep?.choices).containsExactlyInAnyOrder(
      MultipleChoiceStep.JsonChoice(
        "Guitar",
        mapOf(
          "name" to "Guitar",
          "price" to 100
        )
      ),
      MultipleChoiceStep.JsonChoice(
        "Drum",
        mapOf(
          "name" to "Drum",
          "price" to 150
        )
      ),
      MultipleChoiceStep.JsonChoice(
        "Piano",
        mapOf(
          "name" to "Piano",
          "price" to 300
        )
      )
    )
  }

  @Test
  fun `assigns end property to the last OptionsStep`() {
    val fileId = "26ChatThatEndsInStepThatUpdatesPreviousOptionsStep"
    val json = javaClass.getResource("/$fileId.mup").readText()
    whenever(negotiationAssetService.getChatFlow(fileId)).thenReturn(json)

    val chat = mindMupService.getMup(fileId).toChat()

    val lastUpdateOptionsStep = chat.steps.last()

    assertThat(lastUpdateOptionsStep is OptionsStep).isTrue()
    assertThat((lastUpdateOptionsStep as OptionsStep).end).isTrue()
  }

  @Test
  fun `change multiple choice into options if maxChoices is 1`() {
    val fileId = "27MultipleChoice1OutOfN"
    val json = javaClass.getResource("/$fileId.mup").readText()
    whenever(negotiationAssetService.getChatFlow(fileId)).thenReturn(json)

    val chat = mindMupService.getMup(fileId).toChat()

    assertThat(chat.steps.size).isEqualTo(3)

    assertThat(chat.steps[0].id).isEqualTo("1")
    assertThat((chat.steps[0] as TextStep).trigger).isEqualTo("3.d21a.e1d019a93-d46d.e1f3baca4")

    assertThat(chat.steps[1].id).isEqualTo("3.d21a.e1d019a93-d46d.e1f3baca4")
    assertThat((chat.steps[1] as OptionsStep).variable).isEqualTo("{preference_1}")
    assertThat((chat.steps[1] as OptionsStep).options[0].label).isEqualTo("To have the apartment for myself")
    assertThat((chat.steps[1] as OptionsStep).options[0].value).isEqualTo("To have the apartment for myself")
    assertThat((chat.steps[1] as OptionsStep).options[0].trigger).isEqualTo("4.d21a.e1d019a93-d46d.e1f3baca4")

    assertThat(chat.steps[2].id).isEqualTo("4.d21a.e1d019a93-d46d.e1f3baca4")
    assertThat((chat.steps[2] as TextStep).end).isTrue()
  }
}
