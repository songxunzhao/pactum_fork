package com.pactum.model

import com.pactum.exception.ClientFaultException

// model exceptions
class ModelException(message: String) : ClientFaultException(message)
