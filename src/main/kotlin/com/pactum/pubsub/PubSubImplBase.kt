package com.pactum.pubsub

enum class PubSubTopic(val topicName: String) {
  NEGOTIATION_EVENT("negotiation-event")
}

interface PubSubImplBase {
  fun initTopics()
  fun destroy()
  fun publishMany(pubSubTopic: PubSubTopic, messages: List<Any>, attributes: Map<String, String>)
}
