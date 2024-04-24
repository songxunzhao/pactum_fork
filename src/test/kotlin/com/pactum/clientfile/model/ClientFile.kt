package com.pactum.clientfile.model

import com.pactum.test.TestClockHolder

fun ClientFile.Companion.createMock(clientId: Long, originalFileName: String): ClientFile {
  return ClientFile(
    null,
    clientId,
    "asdfg123",
    originalFileName,
    14L,
    "user@gmail.com",
    TestClockHolder.NOW
  )
}
