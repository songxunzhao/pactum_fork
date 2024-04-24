package com.pactum.google

import com.google.api.services.drive.Drive
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.pactum.test.UnitTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import java.util.concurrent.Executor

@UnitTest
class GoogleDriveServiceTest {

  private val defaultFlowId = "defaultFlowId"
  private val googleDrive: Drive = mock(defaultAnswer = Mockito.RETURNS_DEEP_STUBS)
  private val executor: CurrentThreadExecutor = mock()
  private val googleDriveService = GoogleDriveService(
    googleDrive,
    executor,
    10L,
    10L
  )

  @Test
  fun `can get default chat flow in time`() {
    val content = "hello"
    val im = content.byteInputStream()
    whenever(googleDrive.files().get(anyString()).executeMediaAsInputStream()).thenReturn(im)
    whenever(executor.execute(any())).thenCallRealMethod()

    val fileContents = googleDriveService.getContent(defaultFlowId)
    assertThat(fileContents).isNotNull
    assertThat(fileContents).isEqualTo(content)
  }

  @Test
  fun `can not get invalid file id`() {
    whenever(googleDrive.files().get(anyString()).executeMediaAsInputStream()).thenReturn(null)
    assertThrows<Exception> {
      googleDriveService.getContent("invalid file id")
    }
  }

  @Test
  fun `can get default chat flow md5`() {
    val md5 = "someMd5Hash"
    whenever(googleDrive.files().get(anyString()).setFields("md5Checksum").execute().md5Checksum).thenReturn(md5)
    whenever(executor.execute(any())).thenCallRealMethod()

    val fileContents = googleDriveService.getMD5Checksum(defaultFlowId)
    assertThat(fileContents).isNotNull
    assertThat(fileContents).isEqualTo(md5)
  }

  @Test
  fun `can not get invalid file md5`() {
    whenever(googleDrive.files().get(anyString()).setFields("md5Checksum").execute().md5Checksum).thenReturn(null)
    assertThrows<Exception> {
      googleDriveService.getMD5Checksum("invalid file id")
    }
  }

  @Test
  fun `can not get content if timedout`() {
    val content = "hello"
    val im = content.byteInputStream()
    whenever(googleDrive.files().get(anyString()).executeMediaAsInputStream()).thenReturn(im)
    whenever(executor.execute(any())).thenThrow(RuntimeException())
    assertThrows<Exception> {
      googleDriveService.getContent(defaultFlowId)
    }
  }

  @Test
  fun `can not get md5 if timedout`() {
    val md5 = "someMd5Hash"
    whenever(googleDrive.files().get(anyString()).setFields("md5Checksum").execute().md5Checksum).thenReturn(md5)
    whenever(executor.execute(any())).thenThrow(RuntimeException())
    assertThrows<Exception> {
      googleDriveService.getMD5Checksum(defaultFlowId)
    }
  }
}

class CurrentThreadExecutor : Executor {
  override fun execute(r: Runnable) {
    r.run()
  }
}
