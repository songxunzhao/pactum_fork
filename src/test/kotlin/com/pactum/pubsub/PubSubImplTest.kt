package com.pactum.pubsub

import com.google.cloud.pubsub.v1.Publisher
import com.google.pubsub.v1.PubsubMessage
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.pactum.negotiation.model.NegotiationTermChangedPubSubMessage
import com.pactum.test.UnitTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.springframework.cloud.gcp.pubsub.PubSubAdmin
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate
import org.springframework.cloud.gcp.pubsub.support.PublisherFactory

@UnitTest
class PubSubImplTest {

  private lateinit var pubSubAdmin: PubSubAdmin
  private lateinit var pubSubTemplate: PubSubTemplate
  private lateinit var pubSubImpl: PubSubImplBase

  @BeforeEach
  fun `set up`() {
    pubSubTemplate = spy(PubSubTemplate(mock<PublisherFactory>(), mock()))
    pubSubAdmin = mock()
    pubSubImpl = spy(PubSubImpl("test", "http://example.com", pubSubTemplate, pubSubAdmin))
  }

  @Test
  fun `creates new topic if it does not exist`() {
    whenever(pubSubTemplate.publisherFactory.createPublisher(anyString())).thenReturn(mock())
    whenever(pubSubAdmin.getTopic("negotiation-event-staging")).thenReturn(null)

    pubSubImpl.initTopics()
    verify(pubSubAdmin, times(PubSubTopic.values().size)).createTopic(any())
  }

  @Test
  fun `creates no topics if they exist`() {
    whenever(pubSubTemplate.publisherFactory.createPublisher(anyString())).thenReturn(mock())
    whenever(pubSubAdmin.getTopic(anyString())).thenReturn(mock())

    pubSubImpl.initTopics()
    verify(pubSubAdmin, never()).createTopic(any())
  }

  @Test
  fun `should shut down all publishers correctly`() {
    val mockPublisher = mock<Publisher>()
    whenever(pubSubTemplate.publisherFactory.createPublisher(anyString())).thenReturn(mockPublisher)

    pubSubImpl.initTopics()
    pubSubImpl.destroy()
    verify(mockPublisher, times(PubSubTopic.values().size)).shutdown()
  }

  @Test
  fun `publishing works correctly`() {
    val mockPublisher = mock<Publisher>()
    whenever(pubSubTemplate.publisherFactory.createPublisher(anyString())).thenReturn(mockPublisher)
    pubSubImpl.initTopics()

    val messages = listOf(
      NegotiationTermChangedPubSubMessage("term1", "prev1", "new1", "state1", "client1"),
      NegotiationTermChangedPubSubMessage("term2", "prev2", "new2", "state2", "client2")
    )
    pubSubImpl.publishMany(PubSubTopic.NEGOTIATION_EVENT, messages, mapOf("foo" to "bar"))

    argumentCaptor<PubsubMessage>().apply {
      verify(mockPublisher, times(2)).publish(capture())

      assertThat(firstValue.attributesMap["foo"]).isEqualTo("bar")
      assertThat(firstValue.data.toStringUtf8()).isEqualTo(
        """
        {"term":"term1","previousValue":"prev1","value":"new1","stateId":"state1","clientTag":"client1","chatStartTime":null,"chatEndTime":null,"vendorId":null}
      """.trimIndent()
      )

      assertThat(secondValue.attributesMap["foo"]).isEqualTo("bar")
      assertThat(secondValue.data.toStringUtf8()).isEqualTo(
        """
        {"term":"term2","previousValue":"prev2","value":"new2","stateId":"state2","clientTag":"client2","chatStartTime":null,"chatEndTime":null,"vendorId":null}
      """.trimIndent()
      )

      assertThat(firstValue.attributesMap["appEnv"]).isEqualTo("test")
      assertThat(firstValue.attributesMap["baseUrl"]).isEqualTo("http://example.com")
    }

    verify(mockPublisher, times(1)).publishAllOutstanding()
  }
}
