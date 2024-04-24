package com.pactum.docusign

import com.docusign.esign.model.EnvelopeSummary

fun envelopeSummaryFixture(): EnvelopeSummary {
  return EnvelopeSummary().apply {
    envelopeId = "dad3be2c-011c-4f20-b06a-0f62f43c7a90"
    uri = "/envelopes/dad3be2c-011c-4f20-b06a-0f62f43c7a90"
    statusDateTime = "2019-10-01T16:21:16.5670000Z"
    status = "sent"
  }
}
