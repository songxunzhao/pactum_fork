package com.pactum.google

import com.google.cloud.storage.Blob
import com.google.cloud.storage.Storage
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.pactum.test.UnitTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@UnitTest
class GoogleStorageServiceTest {

  private val storage: Storage = mock()
  private val assetsBucketName = "assetsBucket"
  private val clientFilesBucketName = "clientFilesBucket"

  private val googleStorageService = GoogleStorageService(
    storage,
    assetsBucketName,
    clientFilesBucketName
  )

  @Test
  fun `can upload file content`() {
    val json = """{"variable": 1}""".trimMargin()
    val blob: Blob = mock()
    whenever(storage.create(any(), eq(json.toByteArray()))).thenReturn(blob)

    val generation = googleStorageService.upload("test", GoogleStorageBucket.ASSETS, json)
    assertThat(generation).isNotNull
  }

  @Test
  fun `can download file content`() {
    val json = """{"variable": 1}""".trimMargin()

    whenever(storage.readAllBytes(any())).thenReturn(json.toByteArray())

    val content = googleStorageService.download("test", GoogleStorageBucket.ASSETS)
    assertThat(content).isNotNull
    assertThat(content).isEqualTo(json)
  }

  @Test
  fun `can not get upload if bucketname is invalid`() {
    val json = """{"variable": 1}""".trimMargin()
    whenever(storage.create(any(), eq(json.toByteArray()))).thenReturn(null)

    assertThrows<Exception> {
      googleStorageService.upload("fileId", GoogleStorageBucket.ASSETS, json)
    }
  }

  @Test
  fun `can not get download if bucketname is invalid`() {
    whenever(storage.readAllBytes(any())).thenReturn(null)

    assertThrows<Exception> {
      googleStorageService.download("fileId", GoogleStorageBucket.ASSETS)
    }
  }

  @Test
  fun `can not get download if fileId is invalid`() {
    whenever(storage.readAllBytes(any())).thenReturn(null)

    assertThrows<Exception> {
      googleStorageService.download("invalidfileId", GoogleStorageBucket.ASSETS)
    }
  }
}
