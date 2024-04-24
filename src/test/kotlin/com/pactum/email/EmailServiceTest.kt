package com.pactum.email

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.pactum.email.model.Email
import com.pactum.email.model.MailchimpRes
import com.pactum.api.GenericCreatedResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest

@RestClientTest(EmailService::class)
class EmailServiceTest {

  @Autowired
  lateinit var emailService: EmailService

  @Autowired
  lateinit var mockRestServiceServer: MockRestServiceServer

  @Test
  fun `can do POST api call`() {

    val emailAddress = "test@email.com"
    val expectedResult = GenericCreatedResponse(Email(emailAddress, "subscribed"))

    mockRestServiceServer.expect(requestTo("https://us3.api.mailchimp.com/3.0/lists/f14d9d9b2c/members"))
      .andRespond(withSuccess(expectedResult.toString(), MediaType.APPLICATION_JSON))

    val result = emailService.addEmail(emailAddress)

    assertThat(expectedResult).isEqualTo(result)
  }

  @Test
  fun `throws Member Exists exception if email was already sent before`() {

    val emailAddress = "test@email.com"
    val expectedResult = MailchimpRes("", "Member Exists", 0, "email already sent", "")

    mockRestServiceServer.expect(requestTo("https://us3.api.mailchimp.com/3.0/lists/f14d9d9b2c/members"))
      .andRespond(withBadRequest().body(jacksonObjectMapper().writeValueAsString(expectedResult)))

    assertThrows<EmailException> {
      emailService.addEmail(emailAddress)
    }
  }

  @Test
  fun `throws exception with other errors`() {

    val emailAddress = "test@email.com"
    val expectedResult = MailchimpRes("", "General Error", 0, "other errors happened", "")

    mockRestServiceServer.expect(requestTo("https://us3.api.mailchimp.com/3.0/lists/f14d9d9b2c/members"))
      .andRespond(withBadRequest().body(jacksonObjectMapper().writeValueAsString(expectedResult)))

    assertThrows<EmailException> {
      emailService.addEmail(emailAddress)
    }
  }
}
