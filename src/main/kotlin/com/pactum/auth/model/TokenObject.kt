package com.pactum.auth.model

data class TokenObject(
  val random: String,
  val email: String,
  val expire: Long,
  val roles: List<String>?,
  val clientTag: String?
)
