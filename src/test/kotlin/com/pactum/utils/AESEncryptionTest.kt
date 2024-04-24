package com.pactum.utils

import com.pactum.test.UnitTest
import org.junit.Test

@UnitTest
class AESEncryptionTest {

  @Test
  fun `can encrypt`() {

    val secretKey = "K-5WXzip_SDyEjx09bY8sZIE1vB56nhG"
    val str = "Hello"
    val encrypted = "8E0FZCQVvUpmomEDuUEB2Q=="

    val enc = AESEncryption.encrypt(str, secretKey)
    assert(encrypted == enc)
  }

  @Test
  fun `can decrypt`() {

    val secretKey = "K-5WXzip_SDyEjx09bY8sZIE1vB56nhG"
    val encrypted = "8E0FZCQVvUpmomEDuUEB2Q=="
    val str = "Hello"

    val decrypted = AESEncryption.decrypt(encrypted, secretKey)
    assert(decrypted == str)
  }
}
