package com.pactum.clientfile

import com.pactum.exception.ClientFaultException

class ClientFileEmptyException : ClientFaultException("Uploaded file is empty")
