package com.pactum.token

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.utils.tokens.IdTokenVerifier
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.pactum.auth.InvalidTokenException
import com.pactum.auth.model.JwtData
import com.pactum.auth.model.LoginReq
import com.pactum.auth.model.TokenObject
import com.pactum.test.UnitTest
import com.pactum.token.model.Token
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant

@UnitTest
class TokenServiceTest {

  private val clientSecret = "K-5WXzip_SDyEjx09bY8sZIE1vB56nhG"
  private val verifier: IdTokenVerifier = mock()
  private val tokenRepository: TokenRepository = mock()

  private val authTokenService =
    TokenService(
      clientSecret,
      verifier,
      tokenRepository
    )

  @Test
  fun `can not verify invalid jwt token id`() {
    val tokenId = "aninvalidjwttoken"

    val googleReq = LoginReq(tokenId)

    whenever(verifier.verify(tokenId)).thenThrow(InvalidTokenException::class.java)

    assertThrows<InvalidTokenException> {
      authTokenService.verifyAuth0Token(googleReq)
    }
  }

  @Test
  fun `can verify valid jwt token id`() {
    val token = JWT.create().withClaim("email", "test@mail.com")
      .withClaim("https://login.pactum.com/app_metadata", mapOf("client_tag" to "test"))
      .withArrayClaim("http://schemas.microsoft.com/ws/2008/06/identity/claims/roles", arrayOf("test"))
      .sign(Algorithm.HMAC256("secret"))

    val googleReq = LoginReq(token)

    doNothing().whenever(verifier).verify(token)

    val jwtData = authTokenService.verifyAuth0Token(googleReq)
    assertThat(jwtData).isEqualTo(
      JwtData(
        email = "test@mail.com",
        roles = listOf("test"),
        clientTag = "test"
      )
    )
  }

  @Test
  fun `can decrypt valid token`() {
    val email = "backend@pactum.com"
    val expire = authTokenService.generateExpire()
    val token = authTokenService.generateToken(email, expire, null, null)

    val user = authTokenService.decryptToken(token)
    assertThat(user.email).isEqualTo(email)
  }

  @Test
  fun `can not decrypt invalid token`() {

    assertThrows<InvalidTokenException> {
      authTokenService.decryptToken("invalidToken")
    }
  }

  @Test
  fun `can not get user from token if expired`() {
    val email = "backend@pactum.com"
    val expire = Instant.now().minusSeconds(1000).epochSecond
    val token = authTokenService.generateToken(email, expire, null, null)

    assertThrows<InvalidTokenException> {
      authTokenService.decryptToken(token)
    }
  }

  @Test
  fun `can not get user from token if token is not found`() {
    val email = "backend@pactum.com"
    val expire = Instant.now().plusSeconds(1000).epochSecond
    val token = authTokenService.generateToken(email, expire, null, null)
    val tokenObject = TokenObject("", email, expire, null, null)

    whenever(tokenRepository.findByToken(token)).thenReturn(null)

    assertThrows<InvalidTokenException> {
      authTokenService.checkTokenIsValid(token, tokenObject)
    }
  }

  @Test
  fun `can not get user from token if email is not the same as db token`() {
    val email = "backend@pactum.com"
    val expire = Instant.now().plusSeconds(1000).epochSecond
    val token = authTokenService.generateToken(email, expire, null, null)
    val tokenObject = TokenObject("", email, expire, null, null)
    val tokenDb = Token(0, "invalidEmail", token, expire)

    whenever(tokenRepository.findByToken(token)).thenReturn(tokenDb)

    assertThrows<InvalidTokenException> {
      authTokenService.checkTokenIsValid(token, tokenObject)
    }
  }

  @Test
  fun `can not get user from token if token in database is expired`() {
    val email = "backend@pactum.com"
    val expire = Instant.now().minusSeconds(1000).epochSecond
    val token = authTokenService.generateToken(email, expire, null, null)
    val expire2 = Instant.now().plusSeconds(1000).epochSecond
    val tokenObject = TokenObject("", email, expire2, null, null)
    val tokenDb = Token(0, email, token, expire)

    whenever(tokenRepository.findByToken(token)).thenReturn(tokenDb)

    assertThrows<InvalidTokenException> {
      authTokenService.checkTokenIsValid(token, tokenObject)
    }
  }

  @Test
  fun `can not get user from token if token is expired`() {
    val email = "backend@pactum.com"
    val expire = Instant.now().minusSeconds(1000).epochSecond
    val expire2 = Instant.now().plusSeconds(1000).epochSecond
    val token = authTokenService.generateToken(email, expire2, null, null)
    val tokenObject = TokenObject("", email, expire, null, null)
    val tokenDb = Token(0, email, token, expire)

    whenever(tokenRepository.findByToken(token)).thenReturn(tokenDb)

    assertThrows<InvalidTokenException> {
      authTokenService.checkTokenIsValid(token, tokenObject)
    }
  }
}
