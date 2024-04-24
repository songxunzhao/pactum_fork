package com.pactum.negotiation.batch.action

import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.pactum.auth.model.Role
import com.pactum.negotiation.NegotiationRepository
import com.pactum.negotiation.model.Negotiation
import com.pactum.test.UnitTest
import com.pactum.auth.SessionHelper
import com.pactum.client.model.NegotiationFieldConfigType
import com.pactum.negotiation.model.UpdateNegotiationReq
import com.pactum.negotiationfield.NegotiationFieldService
import com.pactum.negotiationfield.model.NegotiationField
import org.springframework.http.HttpStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Optional

@UnitTest
class UpdateFieldsServiceTest {

  private val baseUrl = "https://www.pactum.com"
  private lateinit var negotiationRepository: NegotiationRepository
  private lateinit var negotiationFieldService: NegotiationFieldService
  private lateinit var updateFieldsService: UpdateFieldsService

  @BeforeEach
  fun `set up`() {
    negotiationRepository = mock()
    negotiationFieldService = mock()
    updateFieldsService = UpdateFieldsService(
      negotiationRepository,
      negotiationFieldService,
      baseUrl
    )
  }

  @Test
  fun `can update status of a list of negotiations`() {
    val negId1 = 1L
    val negId2 = 2L
    val stateId1 = "stateId1"
    val stateId2 = "stateId2"
    val email = "backend@pactum.com"
    val token = "avalidtoken"
    val comment = "comment"

    val list = listOf(
      NegotiationField.ApiEntity(NegotiationFieldConfigType.MODEL, "attr1", "value1"),
      NegotiationField.ApiEntity(NegotiationFieldConfigType.OTHER, "attr2", "value2")
    )
    val neg1 = Negotiation.empty(negId1).copy(stateId = stateId1)
    val neg2 = Negotiation.empty(negId2).copy(stateId = stateId2)
    val req = UpdateNegotiationReq(fields = list, comment = comment)
    val batchReq = mapOf(
      "$negId1" to req,
      "$negId2" to req
    )

    whenever(negotiationRepository.findById(negId1)).thenReturn(Optional.of(neg1))
    whenever(negotiationRepository.findById(negId2)).thenReturn(Optional.of(neg2))

    SessionHelper.setLoggedInUser(token, email, listOf(Role.Admin))

    val resp = updateFieldsService.updateFields(batchReq)
    assertThat(resp.statusCode).isEqualTo(HttpStatus.OK)
    @Suppress("UNCHECKED_CAST")
    val body = resp.body as List<Negotiation.ApiEntity>
    assertThat(body.size).isEqualTo(2)
    assertThat(body[0].id).isEqualTo(negId1)
    assertThat(body[0].stateId).isEqualTo(stateId1)
    assertThat(body[1].id).isEqualTo(negId2)
    assertThat(body[1].stateId).isEqualTo(stateId2)

    verify(negotiationFieldService, times(1))
      .setFieldsForNegotiation(eq(negId1), eq(list), eq(comment), eq(NegotiationFieldService.Behaviour.ADD_UPDATE))
    verify(negotiationFieldService, times(1))
      .setFieldsForNegotiation(eq(negId2), eq(list), eq(comment), eq(NegotiationFieldService.Behaviour.ADD_UPDATE))
  }
}
