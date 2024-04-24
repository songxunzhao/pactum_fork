package com.pactum.negotiationasset

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.whenever
import com.pactum.chat.ChatNotFoundException
import com.pactum.google.GoogleDriveFetchException
import com.pactum.google.GoogleDriveService
import com.pactum.google.GoogleDriveTimeoutException
import com.pactum.google.GoogleStorageBucket
import com.pactum.google.GoogleStorageDownloadException
import com.pactum.google.GoogleStorageService
import com.pactum.negotiationasset.model.NegotiationAsset
import com.pactum.negotiationasset.model.NegotiationAssetType
import com.pactum.test.UnitTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.cache.Cache
import org.springframework.cache.ehcache.EhCacheCacheManager

@UnitTest
class NegotiationAssetServiceTest {

  private val negotiationAssetRepository: NegotiationAssetRepository = mock()
  private val googleDriveService: GoogleDriveService = mock()
  private val googleStorageService: GoogleStorageService = mock()
  private val cacheManager: EhCacheCacheManager = mock()
  private val defaultflowId = "123"
  private val cache: Cache = mock()

  private val chatAssetService = NegotiationAssetService(
    negotiationAssetRepository,
    googleDriveService,
    googleStorageService,
    cacheManager
  )

  @BeforeEach
  fun `clean up`() {
    reset(googleDriveService)
    reset(googleStorageService)
    reset(cacheManager)
    reset(negotiationAssetRepository)
  }

  @Test
  fun `should return flow if cached`() {
    val json = """{"variable": 1}""".trimMargin()

    whenever(cacheManager.getCache(GoogleDriveService.FILE_CACHE)).thenReturn(cache)
    whenever(cache.get(defaultflowId, String::class.java)).thenReturn(json)

    val result = chatAssetService.getChatFlow(defaultflowId)
    assertThat(result).isEqualTo(json)
  }

  @Test
  fun `should return flow from google drive if not cached and file is not changed`() {
    val json = """{"variable": 1}""".trimMargin()
    val chatAsset = NegotiationAsset.newFlowAsset(defaultflowId, 1, "2")

    whenever(googleDriveService.getMD5Checksum(defaultflowId)).thenReturn("2")
    whenever(negotiationAssetRepository.findLatestByDriveId(defaultflowId, NegotiationAssetType.FLOW.name))
      .thenReturn(chatAsset)
    whenever(googleDriveService.getContent(defaultflowId)).thenReturn(json)

    val result = chatAssetService.getChatFlow(defaultflowId)
    assertThat(result).isEqualTo(json)
  }

  @Test
  fun `should return upload and add record if not cached and file is changed`() {
    val json = """{"variable": 1}""".trimMargin()
    val chatAsset = NegotiationAsset.newFlowAsset(defaultflowId, 1, "2")

    whenever(googleDriveService.getMD5Checksum(defaultflowId)).thenReturn("3")
    whenever(negotiationAssetRepository.findLatestByDriveId(defaultflowId, NegotiationAssetType.FLOW.name))
      .thenReturn(chatAsset)
    whenever(googleDriveService.getContent(defaultflowId)).thenReturn(json)
    whenever(googleStorageService.upload(defaultflowId, GoogleStorageBucket.ASSETS, json)).thenReturn(2)
    val newChatAsset = NegotiationAsset.newFlowAsset(defaultflowId, 2, "3")
    whenever(negotiationAssetRepository.save(newChatAsset)).thenReturn(newChatAsset)

    val result = chatAssetService.getChatFlow(defaultflowId)
    assertThat(result).isEqualTo(json)
  }

  @Test
  fun `should upload and add record if not cached and file is new`() {
    val json = """{"variable": 1}""".trimMargin()

    whenever(googleDriveService.getMD5Checksum(defaultflowId)).thenReturn("3")
    whenever(
      negotiationAssetRepository.findLatestByDriveId(defaultflowId, NegotiationAssetType.FLOW.name)
    ).thenReturn(null)
    whenever(googleDriveService.getContent(defaultflowId)).thenReturn(json)
    whenever(googleStorageService.upload(defaultflowId, GoogleStorageBucket.ASSETS, json)).thenReturn(2)
    val newChatAsset = NegotiationAsset.newFlowAsset(defaultflowId, 2, "3")
    whenever(negotiationAssetRepository.save(newChatAsset)).thenReturn(newChatAsset)

    val result = chatAssetService.getChatFlow(defaultflowId)
    assertThat(result).isEqualTo(json)
  }

  @Test
  fun `should return flow from google storage if not cached and google drive timedout`() {
    val json = """{"variable": 1}""".trimMargin()

    whenever(googleDriveService.getMD5Checksum(defaultflowId)).thenThrow(GoogleDriveTimeoutException::class.java)
    whenever(googleStorageService.download(defaultflowId, GoogleStorageBucket.ASSETS)).thenReturn(json)

    val result = chatAssetService.getChatFlow(defaultflowId)
    assertThat(result).isEqualTo(json)
  }

  @Test
  fun `should throw exception from google storage if not cached and google drive timedout and file is not found`() {
    whenever(googleDriveService.getMD5Checksum(defaultflowId)).thenThrow(GoogleDriveTimeoutException::class.java)
    whenever(googleStorageService.download(defaultflowId, GoogleStorageBucket.ASSETS))
      .thenThrow(GoogleStorageDownloadException::class.java)

    assertThrows<ChatNotFoundException> {
      chatAssetService.getChatFlow(defaultflowId)
    }
  }

  @Test
  fun `should return flow from google storage if not cached and file is not changed and google drive timedout`() {
    val json = """{"variable": 1}""".trimMargin()
    val chatAsset = NegotiationAsset.newFlowAsset(defaultflowId, 1, "2")

    whenever(googleDriveService.getMD5Checksum(defaultflowId)).thenReturn("2")
    whenever(negotiationAssetRepository.findLatestByDriveId(defaultflowId, NegotiationAssetType.FLOW.name))
      .thenReturn(chatAsset)
    whenever(googleDriveService.getContent(defaultflowId)).thenThrow(GoogleDriveTimeoutException::class.java)
    whenever(googleStorageService.download(defaultflowId, GoogleStorageBucket.ASSETS)).thenReturn(json)

    val result = chatAssetService.getChatFlow(defaultflowId)
    assertThat(result).isEqualTo(json)
  }

  @Test
  fun `should throw exception if not cached and file is not changed and google drive timedout and file not found`() {
    val chatAsset = NegotiationAsset.newFlowAsset(defaultflowId, 1, "2")

    whenever(googleDriveService.getMD5Checksum(defaultflowId)).thenReturn("2")
    whenever(negotiationAssetRepository.findLatestByDriveId(defaultflowId, NegotiationAssetType.FLOW.name))
      .thenReturn(chatAsset)
    whenever(googleDriveService.getContent(defaultflowId)).thenThrow(GoogleDriveTimeoutException::class.java)
    whenever(googleStorageService.download(defaultflowId, GoogleStorageBucket.ASSETS))
      .thenThrow(GoogleDriveFetchException::class.java)

    assertThrows<ChatNotFoundException> {
      chatAssetService.getChatFlow(defaultflowId)
    }
  }

  @Test
  fun `should return flow from google storage if should upload and add record and google drive timedout`() {
    val json = """{"variable": 1}""".trimMargin()
    val chatAsset = NegotiationAsset.newFlowAsset(defaultflowId, 1, "2")

    whenever(googleDriveService.getMD5Checksum(defaultflowId)).thenReturn("3")
    whenever(negotiationAssetRepository.findLatestByDriveId(defaultflowId, NegotiationAssetType.FLOW.name))
      .thenReturn(chatAsset)
    whenever(googleDriveService.getContent(defaultflowId)).thenThrow(GoogleDriveTimeoutException::class.java)
    whenever(googleStorageService.download(defaultflowId, GoogleStorageBucket.ASSETS)).thenReturn(json)

    val result = chatAssetService.getChatFlow(defaultflowId)
    assertThat(result).isEqualTo(json)
  }
}
