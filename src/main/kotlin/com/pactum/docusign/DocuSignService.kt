package com.pactum.docusign

import com.docusign.esign.model.Document
import com.docusign.esign.model.EnvelopeDefinition
import com.docusign.esign.model.EnvelopeSummary
import com.docusign.esign.model.RecipientViewRequest
import com.docusign.esign.model.Recipients
import com.docusign.esign.model.SignHere
import com.docusign.esign.model.Signer
import com.docusign.esign.model.Tabs
import com.docusign.esign.model.ViewUrl
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
@EnableConfigurationProperties(DocuSignProperties::class)
class DocuSignService(
  restTemplateBuilder: RestTemplateBuilder,
  @Value("\${server.baseUrl}") private var baseUrl: String,
  private val properties: DocuSignProperties
) {

  private val restTemplate: RestTemplate = restTemplateBuilder
    .rootUri(properties.baseUrl)
    .additionalInterceptors(
      ClientHttpRequestInterceptor { request, body, execution ->
        request.headers.set(
          "X-DocuSign-Authentication",
          """{
              "Username": "${properties.username}",
              "Password": "${properties.password}",
              "IntegratorKey": "${properties.clientId}"
          }"""
        )
        request.headers.contentType = APPLICATION_JSON
        execution.execute(request, body)
      }
    )
    // .additionalInterceptors(LoggingHttpRequestInterceptor())
    .build()

  fun createAndSendEnvelope(documentBase64: String): EnvelopeSummary {
    var tries = 0
    do {
      try {
        return postToEnvelops(documentBase64)
      } catch (exception: Exception) {
        tries++
      }
    } while (tries < 3)
    throw DocusignPostException()
  }

  private fun postToEnvelops(documentBase64: String): EnvelopeSummary {
    return restTemplate.postForObject("/envelopes", newEnvelope(documentBase64), EnvelopeSummary::class.java)!!
  }

  private fun newEnvelope(base64: String): EnvelopeDefinition {

    val document = Document().apply {
      documentBase64 = base64
      name = "demo.pdf"
      fileExtension = "pdf"
      documentId = "1"
    }

    val signHere = SignHere().apply {
      xPosition = "150"
      yPosition = "500"
      documentId = "1"
      pageNumber = "4"
    }

    val tab = Tabs().apply {
      signHereTabs = listOf(signHere)
    }

    val signer = Signer().apply {
      email = "kristjan@pactum.com"
      name = "Joe Black"
      recipientId("1")
      tabs = tab
    }

    val recits = Recipients().apply {
      signers = listOf(signer)
    }

    return EnvelopeDefinition().apply {
      emailSubject = "Please sign this document"
      status = "sent"
      documents = listOf(document)
      recipients = recits
    }
  }

  fun getUrlToRecipientViewUI(envelopeId: String): String {
    return restTemplate.postForObject(
      "/envelopes/$envelopeId/views/recipient",
      newRecipientViewRequest(),
      ViewUrl::class.java
    )!!.url
  }

  private fun newRecipientViewRequest(): RecipientViewRequest {
    return RecipientViewRequest().apply {
      returnUrl = baseUrl
      authenticationMethod = "None"
      email = "demo@pactum.com"
      userName = "Joe Black"
    }
  }
}
