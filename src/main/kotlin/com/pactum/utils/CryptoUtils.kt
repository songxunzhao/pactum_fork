package com.pactum.utils

import java.math.BigInteger

class CryptoUtils {
  companion object {
    fun encodeBytesBase36(bytes: ByteArray): String = BigInteger(1, bytes).toString(36)
  }
}
