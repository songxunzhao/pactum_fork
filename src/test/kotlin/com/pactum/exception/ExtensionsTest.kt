package com.pactum.exception

import com.pactum.chat.ChatNotFoundException
import com.pactum.test.UnitTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

@UnitTest
class ExtensionsTest {

  @Test
  fun `exception to api error`() {
    val exception = ChatNotFoundException()
    val apiError = exception.toApiError()

    Assertions.assertThat(apiError.message).isEqualTo(exception.message)
    Assertions.assertThat(apiError.name).isEqualTo(exception.javaClass.simpleName)
  }

  @Test
  fun `https status to exception`() {
    val exception = ClientFaultException("Unauthorized")
    val httpStatus = HttpStatus.valueOf(401)

    Assertions.assertThat(httpStatus.toException().message == exception.message)
    Assertions.assertThat(httpStatus.toException().javaClass.simpleName == exception.javaClass.simpleName)
  }
}
