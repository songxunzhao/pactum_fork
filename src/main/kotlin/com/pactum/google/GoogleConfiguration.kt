package com.pactum.google

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.auth.Credentials
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.ByteArrayInputStream
import java.util.Base64

@Configuration
class GoogleConfiguration(
  @Value("\${google.credentials.serviceAccount}") private var encodedAccount: String
) {

  @Bean
  fun driveCredential(): GoogleCredential {
    val inputStream = decodeBase64(encodedAccount)
    return GoogleCredential.fromStream(inputStream)
      .createScoped(listOf(DriveScopes.DRIVE_READONLY))
  }

  @Bean
  fun storageCredential(): Credentials {
    val inputStream = decodeBase64(encodedAccount)
    return ServiceAccountCredentials.fromStream(inputStream)
  }

  private fun decodeBase64(encoded: String): ByteArrayInputStream {
    // use this command to encode json file
    // base64 service_account.json | tr -d \\n
    val decoded = Base64.getDecoder().decode(encoded)
    return ByteArrayInputStream(decoded)
  }

  @Bean
  fun googleDrive(driveCredential: GoogleCredential): Drive {
    return Drive(
      GoogleNetHttpTransport.newTrustedTransport(),
      JacksonFactory.getDefaultInstance(),
      driveCredential
    )
  }

  @Bean
  fun googleStorage(storageCredential: Credentials): Storage {
    return StorageOptions.newBuilder()
      .setCredentials(ServiceAccountCredentials.fromStream(decodeBase64(encodedAccount)))
      .build()
      .service
  }
}
