package com.pactum.chat

import com.pactum.exception.ClientFaultException
import com.pactum.exception.ServerFaultException

// chat exceptions
class ChatNotFoundException : ClientFaultException("Chat not found")

// chat flow design exceptions
open class FlowDesignException(message: String) : ServerFaultException(message)
class VariableException(message: String) : FlowDesignException(message)
class ChoicesNotFoundException : FlowDesignException("Choices not found in note under <step_properties>")
class TagNotFoundException : FlowDesignException("Tag Not Found")
class TagUnclosedException : FlowDesignException("Unclosed Tag Found")
class PatternIdInvalidException(message: String) : FlowDesignException(message)
class InvalidTriggerException(message: String) : FlowDesignException(message)
class InvalidEvalExpressionException(message: String) : FlowDesignException(message)

// chat dsl exceptions
class ChatDSLInvalidSyntaxException(message: String) : ServerFaultException(message)
class ChatDSLFlowNotFound : ServerFaultException("Chat DSL Flow Not Found")
