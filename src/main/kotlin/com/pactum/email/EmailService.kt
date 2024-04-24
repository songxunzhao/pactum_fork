package com.pactum.email

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.pactum.email.model.Email
import com.pactum.email.model.MailchimpRes
import com.pactum.api.GenericCreatedResponse
import org.json.simple.JSONObject
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException

@Service
class EmailService(
  private val restTemplateBuilder: RestTemplateBuilder,
  @Value("\${mailchimp.api.token}") private val token: String,
  @Value("\${mailchimp.api.url}") private val baseUrl: String
) {

  private val restTemplate = restTemplateBuilder.build()

  fun addEmail(body: String): GenericCreatedResponse {

    val formattedEmailAddress = body.replace("\"", "")

    val emailJson = JSONObject()
    emailJson["email_address"] = formattedEmailAddress
    emailJson["status"] = "subscribed"

    val headers = HttpHeaders()
    headers.set("Authorization", "auth $token")

    val entity = HttpEntity(emailJson.toString(), headers)

    return try {
      restTemplate.postForObject("$baseUrl/lists/f14d9d9b2c/members", entity, String::class.java)
      GenericCreatedResponse(Email(formattedEmailAddress, "subscribed"))
    } catch (exception: HttpClientErrorException) {
      val res = jacksonObjectMapper().readValue(exception.responseBodyAsString, MailchimpRes::class.java)
      if (res.title.contains("Member Exists"))
        throw EmailException(res.title)
      else
        throw EmailException(res.detail)
    }
  }
}
