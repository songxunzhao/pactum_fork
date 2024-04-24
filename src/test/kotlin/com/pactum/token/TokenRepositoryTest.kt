package com.pactum.token

import com.pactum.test.RepositoryTest
import com.pactum.token.model.Token
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@RepositoryTest
class TokenRepositoryTest @Autowired constructor(
  private val repository: TokenRepository
) {

  @Test
  fun `finds user by email`() {
    val email = "backend@pactum.com"

    val user = Token(email = email, token = "sometoken", expire = 1000)
    val savedUser = repository.save(user)

    val foundUser = repository.findByEmail(email)

    assertThat(foundUser).isEqualToComparingOnlyGivenFields(savedUser)
    assertThat(foundUser!!.email).isEqualTo(email)
  }

  @Test
  fun `finds user by token`() {
    val email = "backend@pactum.com"
    val token = "sometoken"

    val user = Token(email = email, token = token, expire = 1000)
    val savedUser = repository.save(user)

    val foundUser = repository.findByToken(token)

    assertThat(foundUser).isEqualToComparingOnlyGivenFields(savedUser)
    assertThat(foundUser!!.email).isEqualTo(email)
    assertThat(foundUser.token).isEqualTo(token)
  }
}
