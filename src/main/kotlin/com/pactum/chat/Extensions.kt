package com.pactum.chat

fun Map<String, *>.getValueUsingKeyPath(keyPath: String): Any? {
  val keys = keyPath.split('.')
  var currentMap = this
  for (key in keys.dropLast(1)) {
    @Suppress("UNCHECKED_CAST")
    currentMap = currentMap[key] as? Map<String, *>
      ?: throw VariableException("No variable value could be found for variable: $keyPath")
  }
  return currentMap[keys.last()]
}
