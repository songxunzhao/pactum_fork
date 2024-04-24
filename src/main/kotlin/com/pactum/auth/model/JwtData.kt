package com.pactum.auth.model

data class JwtData(
  val email: String,
  val roles: List<String>?,
  val clientTag: String?
)
