package com.pactum

import com.pactum.utils.SentryHelper
import io.opentracing.contrib.spring.web.starter.WebTracingProperties
import mu.KotlinLogging
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.info.GitProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties(WebTracingProperties::class)
class Application {
  lateinit var environment: String
}

fun main(args: Array<String>) {
  // CloudFlare is not a fan of Java user agents
  System.setProperty("http.agent", "HTTPie/1.0.2")
  System.setProperty("user.timezone", "GMT")
  val logger = KotlinLogging.logger {}
  val context = runApplication<Application>(*args)
  val env = context.environment.getProperty("server.env")
  val release: String? = try {
    val gitProperties = context.getBean(GitProperties::class.java)
    gitProperties.shortCommitId
  } catch (e: NoSuchBeanDefinitionException) {
    logger.info { "No git properties found, continuing without release info" }
    null
  }

  SentryHelper.init(env, release)
  logger.info { "Started application in \"$env\" mode" }
}
