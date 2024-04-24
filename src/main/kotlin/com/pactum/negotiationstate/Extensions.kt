package com.pactum.negotiationstate

import com.pactum.chat.VariableException
import com.pactum.chat.model.extractMainVariableAndKeyPath

fun Map<String, Any>.insertIntoKeyPathAndCoalesceIfPossible(keyPath: String, value: Any): Map<String, Any> {
  val mutableThis = this.toMutableMap()
  var current = mutableThis
  var currentKeyPath = keyPath

  do {
    val (mainVariable, relativeKeyPath) = extractMainVariableAndKeyPath(currentKeyPath)

    if (relativeKeyPath.isBlank()) {
      @Suppress("UNCHECKED_CAST")
      current[mainVariable] = if (current[mainVariable] is Map<*, *> && value is Map<*, *>)
        current[mainVariable] as Map<String, Any> + value as Map<String, Any>
      else
        value
    } else {
      if (!current.containsKey(mainVariable))
        current[mainVariable] = mapOf<String, Any>()

      @Suppress("UNCHECKED_CAST")
      current[mainVariable] = (current[mainVariable] as? Map<String, Any>)?.toMutableMap()
        ?: throw VariableException("Could not insert into keyPath: $keyPath")

      @Suppress("UNCHECKED_CAST")
      current = current[mainVariable] as MutableMap<String, Any>
    }

    currentKeyPath = relativeKeyPath
  } while (!currentKeyPath.isBlank())

  return mutableThis
}
