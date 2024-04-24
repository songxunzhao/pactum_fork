package com.pactum.clientfile

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.pactum.api.GenericOkResponse
import com.pactum.audit.AuditEventService
import com.pactum.auth.SessionHelper
import com.pactum.auth.model.Role
import com.pactum.client.ClientService
import com.pactum.client.model.Client
import com.pactum.clientfile.model.ClientFile
import com.pactum.google.GoogleStorageBucket
import com.pactum.google.GoogleStorageService
import com.pactum.test.TestClockHolder
import com.pactum.test.UnitTest
import com.pactum.utils.PbeFileEncryptor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.core.io.ClassPathResource
import org.springframework.mock.web.MockMultipartFile

@UnitTest
class ClientFileServiceTest {
  private val clock = TestClockHolder.CLOCK
  private val auditEventService: AuditEventService = mock()
  private val clientService: ClientService = mock()
  private val clientFileRepository: ClientFileRepository = mock()
  private val googleStorageService: GoogleStorageService = mock()
  private val pbeFileEncryptor: PbeFileEncryptor = mock()

  private val clientFileService = ClientFileService(
    auditEventService,
    clientService,
    clientFileRepository,
    googleStorageService,
    pbeFileEncryptor,
    clock
  )

  @Test
  fun processUploadedFile() {
    val testFile = ClassPathResource("clientfiles/test.txt").file
    val mockFile = MockMultipartFile("file", "test.txt", null, testFile.inputStream())
    val clientId = 123L
    val passphrase = "mypass"

    val mockEncryptedFile = byteArrayOf(1, 10, -34, 4, 2, 5, 31)
    whenever(pbeFileEncryptor.encryptFile(testFile.readBytes(), passphrase)).thenReturn(mockEncryptedFile)

    SessionHelper.setLoggedInUser("token", "client@gmail.com", listOf(Role.Client))
    val client: Client = mock {
      on { id }.thenReturn(clientId)
    }
    whenever(clientService.getActiveClient()).thenReturn(client)

    val expectedStorageId = "123/test.txt_30qklduj5iln5em4puzqpakps"
    val expectedClientFile = ClientFile(
      clientId = clientId,
      storageId = expectedStorageId,
      originalFileName = "test.txt",
      originalFileSize = testFile.length(),
      username = "client@gmail.com",
      uploadTime = clock.instant()
    )
    val savedClientFile = expectedClientFile.copy(id = 44L)
    whenever(clientFileRepository.save(expectedClientFile)).thenReturn(savedClientFile)

    val response = clientFileService.processUploadedFile(mockFile, passphrase)

    verify(googleStorageService).upload(
      expectedStorageId,
      GoogleStorageBucket.CLIENT_FILES,
      mockEncryptedFile,
      "application/octet-stream"
    )
    verify(clientFileRepository).save(expectedClientFile)
    verify(auditEventService).addEntityAuditEvent(ClientFileAuditEvent.FILE_UPLOADED, 44L, savedClientFile)

    assertThat(response.body).isEqualTo(expectedClientFile.toApiEntity())
  }

  @Test
  fun `processUploadedFile fails with empty file`() {
    val mockFile = MockMultipartFile("file", "spreadsheet.xls", null, byteArrayOf())

    assertThrows<ClientFileEmptyException> { clientFileService.processUploadedFile(mockFile, "mypass") }
  }

  @Test
  fun getFiles() {
    val clientId = 123L
    val client: Client = mock {
      on { id }.thenReturn(clientId)
    }
    val clientFile1: ClientFile = mock()
    whenever(clientFile1.toApiEntity()).thenReturn(mock())
    val clientFile2: ClientFile = mock()
    whenever(clientFile2.toApiEntity()).thenReturn(mock())

    whenever(clientService.getActiveClient()).thenReturn(client)
    whenever(clientFileRepository.findAllByClientId(clientId)).thenReturn(listOf(clientFile1, clientFile2))

    assertThat(clientFileService.getFiles()).isEqualTo(
      GenericOkResponse(listOf(clientFile1.toApiEntity(), clientFile2.toApiEntity()))
    )
  }
}
