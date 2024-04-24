package com.pactum.email

import com.pactum.exception.ClientFaultException

class EmailException(msg: String) : ClientFaultException(msg)
