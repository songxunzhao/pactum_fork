package com.pactum.negotiationasset

import com.pactum.chat.ChatNotFoundException
import com.pactum.google.GoogleDriveService
import com.pactum.google.GoogleStorageBucket
import com.pactum.google.GoogleStorageService
import com.pactum.negotiationasset.model.NegotiationAssetType
import com.pactum.negotiationasset.model.NegotiationAsset
import com.pactum.utils.SentryHelper
import com.pactum.utils.Utils
import mu.KotlinLogging
import org.springframework.cache.ehcache.EhCacheCacheManager
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class NegotiationAssetService(
  private val negotiationAssetRepository: NegotiationAssetRepository,
  private val googleDriveService: GoogleDriveService,
  private val googleStorageService: GoogleStorageService,
  private val cacheManager: EhCacheCacheManager
) {

  fun getChatFlow(driveId: String): String? {
    return getAssetInternal(driveId, NegotiationAssetType.FLOW)
  }

  fun getChatModel(driveId: String): String? {
    return getAssetInternal(driveId, NegotiationAssetType.MODEL)
  }

  private fun getAssetInternal(driveId: String, type: NegotiationAssetType): String? {
    // first check cache
    val cached = checkCache(driveId)
    if (cached != null) {
      // file is cached. return it
      return cached
    }

    // try to get MD5 of file on Google Drive.
    val md5 = try {
      googleDriveService.getMD5Checksum(driveId)
    } catch (e: Exception) {
      // Google Drive failed to fetch metadata in time. Return latest Google Storage file
      try {
        return googleStorageService.download(driveId, GoogleStorageBucket.ASSETS)
      } catch (e: Exception) {
        // fileId is wrong. End chat
        throw ChatNotFoundException()
      }
    }

    val chatAsset = negotiationAssetRepository.findLatestByDriveId(driveId, type.name)
    return if (chatAsset != null) {
      // this is not the first time. we need to check if file is updated
      if (md5 != chatAsset.md5Checksum) {
        // file is changed. we need to store file content in Google Storage and create a record in db
        uploadAndAddRecord(driveId, md5, type)
      } else {
        // file is not changed. return this file content
        tryGetContent(driveId)
      }
    } else {
      // this is first time. we need to store file content in Google Storage and create a record in db
      uploadAndAddRecord(driveId, md5, type)
    }
  }

  private fun checkCache(driveId: String): String? {
    return cacheManager.getCache(GoogleDriveService.FILE_CACHE)?.get(driveId, String::class.java)
  }

  private fun tryGetContent(driveId: String): String {
    return try {
      // this is needed to be able to cache it
      googleDriveService.getContent(driveId)
    } catch (e: Exception) {
      // Google Drive failed to get file in time. Return latest Google Storage file
      try {
        googleStorageService.download(driveId, GoogleStorageBucket.ASSETS)
      } catch (e: Exception) {
        // fileId is wrong. End chat
        throw ChatNotFoundException()
      }
    }
  }

  private fun uploadAndAddRecord(driveId: String, md5: String, type: NegotiationAssetType): String {
    return try {
      val json = googleDriveService.getContent(driveId)
      val storageId = googleStorageService.upload(driveId, GoogleStorageBucket.ASSETS, json)
      val newChatAsset = when (type) {
        NegotiationAssetType.FLOW -> NegotiationAsset.newFlowAsset(driveId, storageId, md5)
        NegotiationAssetType.MODEL -> NegotiationAsset.newModelAsset(driveId, storageId, md5)
      }
      negotiationAssetRepository.save(newChatAsset)
      json
    } catch (e: Exception) {
      logger.error(e.localizedMessage, e)
      if (Utils.isDbInsertException(e)) {
        SentryHelper.report(e, mapOf("driveId" to driveId))
        negotiationAssetRepository.resolveInsertException()
      }
      // Google Drive failed to fetch file in time. Return latest Google Storage files
      try {
        googleStorageService.download(driveId, GoogleStorageBucket.ASSETS)
      } catch (e: Exception) {
        // fileId is wrong. End chat
        throw ChatNotFoundException()
      }
    }
  }
}
