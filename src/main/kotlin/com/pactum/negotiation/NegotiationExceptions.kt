package com.pactum.negotiation

import com.pactum.exception.ClientFaultException

// negotiation exceptions
class NegotiationNotFoundException(stateId: String) : ClientFaultException("Negotiation $stateId not found")
class NegotiationIdNotFoundException(id: Long) : ClientFaultException("Negotiation $id not found")
class NegotiationStatusNotFoundException(status: String) :
  ClientFaultException("Negotiation status $status not found in client config")
