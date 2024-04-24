package com.pactum.docusign

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("docusign")
class DocuSignProperties {
  lateinit var baseUrl: String
  lateinit var username: String
  lateinit var password: String
  lateinit var clientId: String
}
