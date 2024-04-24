package com.pactum.utils

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.pactum.test.UnitTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource
import java.security.SecureRandom
import java.util.Base64

@UnitTest
class PbeFileEncryptorTest {
  private val secureRandom: SecureRandom = mock()

  private val pbeFileEncryptor = PbeFileEncryptor(secureRandom)

  @Test
  fun encryptFile() {
    val testFile = ClassPathResource("clientfiles/test.txt").file
    val passphrase = "mypass"

    val expectedEncryptedFileBase64 = "AAAAAAAAAAAAAAAAAAAAAFNhbHRlZF9fAAAAAAAAAAAKqkVloeZhiraR9yqO1scvswzh3UBy+/" +
      "AwTkyW9u7xHWmFdgTjW8jfbMPnFSBGa5w8XlA2g4ESS3+m5qeUD/aY"

    val expectedEncryptedFileBytes = Base64.getDecoder().decode(expectedEncryptedFileBase64)

    assertThat(pbeFileEncryptor.encryptFile(testFile.readBytes(), passphrase)).isEqualTo(expectedEncryptedFileBytes)
    verify(secureRandom, times(2)).nextBytes(any())
  }
}
