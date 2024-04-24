package com.pactum.docusign

import com.docusign.esign.model.ViewUrl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest
import org.springframework.http.HttpMethod.POST
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.client.ExpectedCount.once
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.content
import org.springframework.test.web.client.match.MockRestRequestMatchers.header
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess

@RestClientTest(DocuSignService::class)
class DocuSignServiceTest @Autowired constructor(
  private val docuSignService: DocuSignService,
  private val server: MockRestServiceServer,
  private val properties: DocuSignProperties,
  @Value("\${server.baseUrl}") private var baseUrl: String
) {

  @Test
  fun createAndSendEnvelope() {
    val envelopeSummary = envelopeSummaryFixture()

    server.expect(
      once(),
      requestTo("/envelopes")
    )
      .andExpect(method(POST))
      .andExpect(
        content().json(
          """
          {
            "documents": [
              {
                "documentBase64": "${getDocumentBase64()}",
                "documentId": "1",
                "fileExtension": "pdf",
                "name": "demo.pdf"
              }
            ],
            "emailSubject": "Please sign this document",
            "recipients": {
              "signers": [
                {
                  "email": "kristjan@pactum.com",
                  "name": "Joe Black",
                  "recipientId": "1",
                  "tabs": {
                    "signHereTabs": [
                      {
                        "xPosition": "150",
                        "yPosition": "500",
                        "documentId": "1",
                        "pageNumber": "4"
                      }
                    ]
                  }
                }
              ]
            },
            "status": "sent"
          }
        """
        )
      )
      .andExpect(
        header(
          "X-DocuSign-Authentication",
          """{
              "Username": "${properties.username}",
              "Password": "${properties.password}",
              "IntegratorKey": "${properties.clientId}"
          }"""
        )
      )
      .andExpect(header("Content-Type", "application/json"))
      .andRespond(
        withSuccess(
          """{
              "envelopeId": "${envelopeSummary.envelopeId}",
              "uri": "${envelopeSummary.uri}",
              "statusDateTime": "${envelopeSummary.statusDateTime}",
              "status": "${envelopeSummary.status}"
            }""",
          APPLICATION_JSON
        )
      )

    val returnedEnvelopeSummary = docuSignService.createAndSendEnvelope(getDocumentBase64())

    assertThat(returnedEnvelopeSummary).isEqualTo(envelopeSummary)
  }

  @Test
  fun getUrlToRecipientViewUI() {
    val viewUrl = ViewUrl()
    viewUrl.url = getExpectedUrl()

    server.expect(
      once(),
      requestTo("/envelopes/${getEnvelopeId()}/views/recipient")
    )
      .andExpect(method(POST))
      .andExpect(
        content().json(
          """{
              "returnUrl": "$baseUrl",
              "authenticationMethod": "None",
              "email": "demo@pactum.com",
              "userName": "Joe Black"
          }"""
        )
      )
      .andExpect(
        header(
          "X-DocuSign-Authentication",
          """{
              "Username": "${properties.username}",
              "Password": "${properties.password}",
              "IntegratorKey": "${properties.clientId}"
          }"""
        )
      )
      .andExpect(header("Content-Type", "application/json"))
      .andRespond(
        withSuccess(
          """{
              "url": "${viewUrl.url}"
            }""",
          APPLICATION_JSON
        )
      )

    val returnedViewUrl = docuSignService.getUrlToRecipientViewUI(getEnvelopeId())

    assertThat(returnedViewUrl).isEqualTo(viewUrl.url)
  }
}
