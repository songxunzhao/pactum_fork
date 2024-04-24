package com.pactum.negotiation.batch.action

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.pactum.auth.model.Role
import com.pactum.negotiation.NegotiationRepository
import com.pactum.negotiation.model.Negotiation
import com.pactum.test.UnitTest
import com.pactum.auth.SessionHelper
import com.pactum.negotiation.NegotiationService
import com.pactum.negotiation.model.UpdateNegotiationReq
import org.springframework.http.HttpStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.Optional

@UnitTest
class UpdateClientVisibilityServiceTest {

  private val negotiationRepository: NegotiationRepository = mock()
  private val negotiationService: NegotiationService = mock()
  private val baseUrl = "https://www.pactum.com"

  private val updateClientVisibilityService = UpdateClientVisibilityService(
    negotiationRepository,
    negotiationService,
    baseUrl
  )

  @Test
  fun `can update client visibility of a list of negotiations`() {
    val negId1 = 1L
    val negId2 = 2L
    val stateId1 = "stateId1"
    val stateId2 = "stateId2"
    val email = "backend@pactum.com"
    val token = "avalidtoken"
    val isVisibleClient = true
    val comment = "comment"

    val neg1 = Negotiation.empty(negId1).copy(stateId = stateId1)
    val neg2 = Negotiation.empty(negId2).copy(stateId = stateId2)
    val req = UpdateNegotiationReq(isVisibleClient = isVisibleClient, comment = comment)
    val batchReq = mapOf(
      "$negId1" to req,
      "$negId2" to req
    )

    whenever(negotiationRepository.findById(negId1)).thenReturn(Optional.of(neg1))
    whenever(negotiationRepository.findById(negId2)).thenReturn(Optional.of(neg2))
    whenever(negotiationService.updateNegotiationClientVisibility(neg1, isVisibleClient, comment)).thenReturn(neg1)
    whenever(negotiationService.updateNegotiationClientVisibility(neg2, isVisibleClient, comment)).thenReturn(neg2)

    SessionHelper.setLoggedInUser(token, email, listOf(Role.Admin))

    val resp = updateClientVisibilityService.updateClientVisibility(batchReq)
    assertThat(resp.statusCode).isEqualTo(HttpStatus.OK)
    @Suppress("UNCHECKED_CAST")
    val body = resp.body as List<Negotiation.ApiEntity>
    assertThat(body.size).isEqualTo(2)
    assertThat(body[0].id).isEqualTo(negId1)
    assertThat(body[0].stateId).isEqualTo(stateId1)
    assertThat(body[1].id).isEqualTo(negId2)
    assertThat(body[1].stateId).isEqualTo(stateId2)
  }
}
