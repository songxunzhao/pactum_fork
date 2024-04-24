package com.pactum.utils

import com.pactum.test.UnitTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
@UnitTest
class CryptoUtilsTest {

  @Test
  fun encodeBytesBase36() {
    assertThat(CryptoUtils.encodeBytesBase36(byteArrayOf())).isEqualTo("0")
    assertThat(CryptoUtils.encodeBytesBase36(byteArrayOf(10))).isEqualTo("a")
    assertThat(CryptoUtils.encodeBytesBase36(byteArrayOf(1, 2, 3, 4))).isEqualTo("a2f44")
  }
}
