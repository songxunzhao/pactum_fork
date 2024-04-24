package com.pactum.pubsub

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.cloud.pubsub.v1.Publisher
import com.google.protobuf.ByteString
import com.google.pubsub.v1.PubsubMessage
import io.opentracing.propagation.Format
import io.opentracing.propagation.TextMapInjectAdapter
import io.opentracing.util.GlobalTracer
import mu.KotlinLogging
import org.springframework.cloud.gcp.pubsub.PubSubAdmin
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate
import java.util.concurrent.TimeUnit

class PubSubImpl(
  val appEnv: String,
  val baseUrl: String,
  val pubSubTemplate: PubSubTemplate,
  val pubSubAdmin: PubSubAdmin
) : PubSubImplBase {

  private val logger = KotlinLogging.logger {}

  val publishers: MutableMap<PubSubTopic, Publisher> = mutableMapOf()

  private fun getMqEnv(): String {
    if (appEnv == "production") {
      return appEnv
    }
    return "staging"
  }

  override fun initTopics() {
    PubSubTopic.values().forEach {
      val topicName = "${it.topicName}-${getMqEnv()}"
      if (pubSubAdmin.getTopic(topicName) == null) {
        logger.info { "Pub/Sub topic '$topicName' does not exist, creating now." }
        pubSubAdmin.createTopic(topicName)
      } else {
        logger.info { "Pub/Sub topic '$topicName' already exists, creating publisher instance." }
      }
      publishers[it] = pubSubTemplate.publisherFactory.createPublisher(topicName)
    }
  }

  override fun destroy() {
    logger.info { "Shutting down ${publishers.size} Pub/Sub publishers" }
    publishers.forEach {
      logger.info { "shutting down '${it.key}' " }

      it.value.shutdown()
      it.value.awaitTermination(100, TimeUnit.MILLISECONDS)
    }
    logger.info { "Pub/Sub terminated" }
  }

  override fun publishMany(pubSubTopic: PubSubTopic, messages: List<Any>, attributes: Map<String, String>) {
    logger.info { "Publishing ${messages.size} messages to ${pubSubTopic.topicName}" }
    val publisher = this.publishers[pubSubTopic]!!
    messages.forEach {
      publish(publisher, it, attributes)
    }
    publisher.publishAllOutstanding()
  }

  private fun publish(publisher: Publisher, payload: Any, attributes: Map<String, String> = emptyMap()) {
    val json = jacksonObjectMapper().writeValueAsString(payload)
    val baseAttributes = mutableMapOf(
      "appEnv" to appEnv,
      "baseUrl" to baseUrl
    )
    baseAttributes.injectTracing()

    val bytes = ByteString.copyFromUtf8(json)
    val msg = PubsubMessage
      .newBuilder()
      .setData(bytes)
      .putAllAttributes(baseAttributes + attributes)
      .build()

    logger.info { "Publishing message to '${publisher.topicNameString}': \n$json\n${msg.attributesMap}" }
    publisher.publish(msg)
  }

  private fun MutableMap<String, String>.injectTracing() {
    val tracer = GlobalTracer.get()
    val span = tracer.activeSpan()
    if (span != null) {
      tracer.inject(span.context(), Format.Builtin.TEXT_MAP_INJECT, TextMapInjectAdapter(this))
    }
  }
}

class PubSubTestImpl : PubSubImplBase {
  override fun initTopics() {
  }
  override fun destroy() {
  }
  override fun publishMany(pubSubTopic: PubSubTopic, messages: List<Any>, attributes: Map<String, String>) {
  }
}
