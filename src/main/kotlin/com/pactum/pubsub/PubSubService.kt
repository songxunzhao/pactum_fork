package com.pactum.pubsub

import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import javax.annotation.PreDestroy

@Service
class PubSubService(
  private val pubSubImpl: PubSubImplBase
) {

  @EventListener(ApplicationReadyEvent::class)
  fun initTopics() {
    pubSubImpl.initTopics()
  }

  @PreDestroy
  fun destroy() {
    pubSubImpl.destroy()
  }

  fun publishMany(pubSubTopic: PubSubTopic, messages: List<Any>, attributes: Map<String, String>) {
    pubSubImpl.publishMany(pubSubTopic, messages, attributes)
  }
}

@Service
class PubSubTestService(private val pubSubImpl: PubSubImplBase)
