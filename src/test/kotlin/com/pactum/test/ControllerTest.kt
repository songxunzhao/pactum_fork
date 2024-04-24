package com.pactum.test

import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@SpringJUnitConfig(classes = [ResourceServerConfiguration::class])
@WebMvcTest
@AutoConfigureJson
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = ["oauth.stateless=false"])
annotation class ControllerTest
