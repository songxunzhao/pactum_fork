package com.pactum.auth

import com.pactum.exception.ClientFaultException
import org.springframework.http.HttpStatus

// auth exceptions
class InvalidTokenException : ClientFaultException("Invalid Token", HttpStatus.UNAUTHORIZED)
class AccessDeniedException : ClientFaultException("Access Denied", HttpStatus.FORBIDDEN)
