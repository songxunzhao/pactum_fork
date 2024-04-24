package com.pactum.clientfile.model

import com.pactum.test.TestClockHolder
import com.pactum.test.UnitTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@UnitTest
class ClientFileTest {

  @Test
  fun toApiEntity() {
    val clientFile = ClientFile(
      id = 123L,
      clientId = 2L,
      storageId = "storage",
      originalFileName = "test.txt",
      originalFileSize = 22,
      username = "client@gmail.com",
      uploadTime = TestClockHolder.CLOCK.instant()
    )
    assertThat(clientFile.toApiEntity()).isEqualTo(
      ClientFile.ApiEntity(
        "test.txt",
        22,
        "client@gmail.com",
        TestClockHolder.CLOCK.instant().toEpochMilli()
      )
    )
  }
}
