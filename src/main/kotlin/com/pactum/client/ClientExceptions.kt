package com.pactum.client

import com.pactum.exception.ClientFaultException

// client exceptions
class ClientExistsException(tag: String) : ClientFaultException("Client $tag already exists")
class ClientNotFoundException(tag: String) : ClientFaultException("Client $tag not found")
class ClientIdNotFoundException(id: Long) : ClientFaultException("Client $id not found")
