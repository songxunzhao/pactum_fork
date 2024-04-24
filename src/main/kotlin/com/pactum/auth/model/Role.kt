package com.pactum.auth.model

enum class Role(val property: Byte) {
  Admin(1),
  Client(2),
  UNDEFINED(0)
}
