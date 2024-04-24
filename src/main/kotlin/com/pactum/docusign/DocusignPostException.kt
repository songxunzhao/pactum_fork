package com.pactum.docusign

import com.pactum.exception.ServerFaultException

// docusign drive exceptions
class DocusignPostException : ServerFaultException("Failed to post envelope")
