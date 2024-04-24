package com.pactum.exception

import org.springframework.http.HttpStatus

open class FaultException(
  override val message: String,
  open val statusCode: HttpStatus
) : RuntimeException(message)

// client exceptions which set 400 status code
open class ClientFaultException(
  override val message: String,
  override val statusCode: HttpStatus = HttpStatus.BAD_REQUEST
) : FaultException(message, statusCode)

// server exceptions which set 500 status code
open class ServerFaultException(
  override val message: String
) : FaultException(message, HttpStatus.INTERNAL_SERVER_ERROR)
