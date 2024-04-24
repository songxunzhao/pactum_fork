package com.pactum.negotiationstate

import com.pactum.exception.ClientFaultException

// chat state exceptions
class ChatStateNotFoundException(stateId: String) : ClientFaultException("Chat state $stateId not found")
class ChatStateNotAvailableException : ClientFaultException("This negotiation is no longer available")
class ChatStateException(message: String) : ClientFaultException(message)
class ChatStateNotOpenedException : ClientFaultException("Chat not opened yet")

// chat step exceptions
class ChatStepException(message: String) : ClientFaultException(message)
