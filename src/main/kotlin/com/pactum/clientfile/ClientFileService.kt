package com.pactum.clientfile

import com.pactum.api.GenericCreatedResponse
import com.pactum.api.GenericOkResponse
import com.pactum.audit.AuditEventService
import com.pactum.auth.SessionHelper
import com.pactum.client.ClientService
import com.pactum.clientfile.model.ClientFile
import com.pactum.google.GoogleStorageBucket
import com.pactum.google.GoogleStorageService
import com.pactum.utils.CryptoUtils
import com.pactum.utils.PbeFileEncryptor
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.security.MessageDigest
import java.time.Clock

@Service
class ClientFileService(
  private val auditEventService: AuditEventService,
  private val clientService: ClientService,
  private val clientFileRepository: ClientFileRepository,
  private val googleStorageService: GoogleStorageService,
  private val fileEncryptor: PbeFileEncryptor,
  private val clock: Clock
) {
  fun processUploadedFile(file: MultipartFile, passphrase: String): GenericCreatedResponse {
    if (file.isEmpty || file.resource.filename == null) throw ClientFileEmptyException()
    val client = clientService.getActiveClient()

    val filename = file.resource.filename!!
    val encryptedFile = fileEncryptor.encryptFile(file.bytes, passphrase)
    val storageId = uploadFileToStorage(filename, encryptedFile, client.id!!)

    val newClientFile = ClientFile(
      clientId = client.id,
      storageId = storageId,
      originalFileName = filename,
      originalFileSize = file.size,
      username = SessionHelper.getLoggedInUserEmail(),
      uploadTime = clock.instant()
    )
    val savedClientFile = clientFileRepository.save(newClientFile)

    auditEventService.addEntityAuditEvent(ClientFileAuditEvent.FILE_UPLOADED, savedClientFile.id!!, savedClientFile)

    return GenericCreatedResponse(newClientFile.toApiEntity())
  }

  private fun uploadFileToStorage(filename: String, encryptedFile: ByteArray, clientId: Long): String {
    val fileDigest = MessageDigest.getInstance("MD5").digest(encryptedFile)
    // base36 encoding to avoid invalid bucket item name characters
    val storageId = clientId.toString() + "/" + filename + "_" + CryptoUtils.encodeBytesBase36(fileDigest)
    googleStorageService.upload(storageId, GoogleStorageBucket.CLIENT_FILES, encryptedFile, "application/octet-stream")

    return storageId
  }

  fun getFiles(): GenericOkResponse {
    val client = clientService.getActiveClient()
    val clientFiles = clientFileRepository.findAllByClientId(client.id!!)

    return GenericOkResponse(clientFiles.map { it.toApiEntity() })
  }
}
