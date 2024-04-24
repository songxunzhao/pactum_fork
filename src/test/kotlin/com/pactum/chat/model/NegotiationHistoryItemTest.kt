package com.pactum.chat.model

import com.pactum.test.UnitTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant

@UnitTest
class NegotiationHistoryItemTest {

  @Test
  fun `can create history entry from chat state`() {

    val time = Instant.parse("2019-08-12T21:03:33.123Z")
    val flowId = "123"

    val state = NegotiationState(
      stateId = "abc",
      time = time,
      state = """{
        "currentStep": { "id": "1", "user": false, "message": "Hello"}
        }"""
    )

    val historyItem = NegotiationHistoryItem.createFromNegotiationState(state, flowId)
    val expectedValue = NegotiationHistoryItem(
      id = "1",
      stateId = "abc",
      user = false,
      message = "Hello",
      time = time,
      flowId = flowId,
      timeSincePreviousSec = null
    )

    assertThat(historyItem).isEqualTo(expectedValue)
  }

  @Test
  fun `can handle empty state values`() {
    val time = Instant.parse("2019-08-12T21:03:33.123Z")
    val flowId = "123"

    val historyItem = NegotiationHistoryItem.createFromNegotiationState(
      NegotiationState(
        stateId = "abc",
        time = time,
        state = """{
        "currentStep": { "id": "1"}
        }"""

      ),
      flowId
    )
    val expectedValue = NegotiationHistoryItem(
      id = "1",
      flowId = flowId,
      stateId = "abc",
      user = false,
      message = "",
      time = time
    )

    assertThat(historyItem).isEqualToComparingOnlyGivenFields(expectedValue)
  }
}
