package com.pactum.api

import com.pactum.exception.ClientFaultException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

// used with PUT when updating a resource - returns 200
class GenericOkResponse(body: Any) : ResponseEntity<Any>(body, HttpStatus.OK)

// used with POST when creating a resource - returns 201
class GenericCreatedResponse(body: Any) : ResponseEntity<Any>(body, HttpStatus.CREATED)

// used with DELETE when deleting a resource - returns 204
class GenericNoContentResponse : ResponseEntity<Void>(HttpStatus.NO_CONTENT)

class GenericNotFoundException(override val message: String) : ClientFaultException(message, HttpStatus.NOT_FOUND)
