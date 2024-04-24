package com.pactum.google

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.pactum.config.Loggable
import com.pactum.utils.trace
import io.opentracing.Span
import io.micrometer.core.annotation.Timed
import io.opentracing.util.GlobalTracer
import mu.KotlinLogging.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

private val logger = logger {}

@Service
class GoogleStorageService(
  private val storage: Storage,
  @Value("\${google.storage.assetsBucketName}") private var assetsBucketName: String,
  @Value("\${google.storage.clientFilesBucketName}") private var clientFilesBucketName: String
) {

  @Loggable(includeResults = true)
  fun upload(
    fileId: String,
    bucket: GoogleStorageBucket,
    fileContent: String,
    contentType: String = "application/json"
  ): Long {
    return upload(fileId, bucket, fileContent.toByteArray(), contentType)
  }

  @Timed("google_storage.upload")
  @Loggable(includeResults = true)
  fun upload(
    fileId: String,
    bucket: GoogleStorageBucket,
    fileContent: ByteArray,
    contentType: String = "application/json"
  ): Long {
    val tracer = GlobalTracer.get()
    return tracer.trace("Google_Storage_upload") { span: Span ->
      try {
        val blobId = BlobId.of(bucketName(bucket), fileId)
        val blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType).build()
        val blob = storage.create(blobInfo, fileContent)
        blob.generation
      } catch (e: Exception) {
        logger.error(e.localizedMessage, e)
        throw e
      }
    } as Long
  }

  @Timed("google_storage.download")
  @Loggable(includeArgs = true)
  fun download(fileId: String, bucket: GoogleStorageBucket): String {
    val tracer = GlobalTracer.get()
    return tracer.trace("Google_Storage_download") { span: Span ->
      try {
        val blobId = BlobId.of(bucketName(bucket), fileId)
        val content = storage.readAllBytes(blobId)
        String(content, Charsets.UTF_8)
      } catch (e: Exception) {
        logger.error(e.localizedMessage, e)
        throw e
      }
    } as String
  }

  private fun bucketName(bucket: GoogleStorageBucket): String {
    return when (bucket) {
      GoogleStorageBucket.ASSETS -> assetsBucketName
      GoogleStorageBucket.CLIENT_FILES -> clientFilesBucketName
    }
  }
}

enum class GoogleStorageBucket {
  ASSETS,
  CLIENT_FILES
}
