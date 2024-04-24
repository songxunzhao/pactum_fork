package com.pactum.token

import com.pactum.token.model.Token
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TokenRepository : CrudRepository<Token, Long> {

  fun findByEmail(email: String): Token?

  fun findByToken(token: String): Token?
}
