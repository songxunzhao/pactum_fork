package com.pactum.token.model

import org.springframework.data.annotation.Id

data class Token(
  @Id
  val id: Long? = null,
  val email: String,
  val token: String,
  val expire: Long
) {
  companion object {
    fun create(email: String, token: String, expire: Long): Token {
      return Token(null, email, token, expire)
    }
  }
}
