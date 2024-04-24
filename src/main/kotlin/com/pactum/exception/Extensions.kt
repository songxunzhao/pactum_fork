package com.pactum.exception

import org.springframework.http.HttpStatus

fun Exception.toApiError(): ApiError {
  return ApiError(
    this.javaClass.simpleName,
    this.message ?: this.localizedMessage ?: ""
  )
}

fun HttpStatus.toException(): Exception {
  return ClientFaultException(this.reasonPhrase)
}
