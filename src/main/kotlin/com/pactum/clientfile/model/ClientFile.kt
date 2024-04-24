package com.pactum.clientfile.model

import org.springframework.data.annotation.Id
import java.time.Instant

data class ClientFile(
  @Id
  val id: Long? = null,
  val clientId: Long,
  val storageId: String,
  val originalFileName: String,
  val originalFileSize: Long,
  val username: String,
  val uploadTime: Instant
) {
  companion object;

  data class ApiEntity(
    val originalFileName: String,
    val originalFileSize: Long,
    val username: String,
    val uploadTime: Long
  )

  fun toApiEntity() = ApiEntity(originalFileName, originalFileSize, username, uploadTime.toEpochMilli())
}
