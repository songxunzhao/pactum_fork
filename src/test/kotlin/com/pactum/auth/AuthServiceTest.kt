package com.pactum.auth

import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.pactum.auth.model.JwtData
import com.pactum.auth.model.LoginReq
import com.pactum.auth.model.Role
import com.pactum.test.UnitTest
import com.pactum.token.TokenService
import com.pactum.token.TokenRepository
import com.pactum.token.model.Token
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.spy
import com.pactum.audit.AuditEventService
import com.pactum.auth.model.LoginRes
import com.pactum.test.TestClockHolder
import org.junit.jupiter.api.BeforeEach
import org.mockito.ArgumentMatchers.anyString

@UnitTest
class AuthServiceTest {
  private val tokenService: TokenService = mock()
  private val userTokenRepository: TokenRepository = mock()
  private lateinit var auditEventService: AuditEventService
  private lateinit var authService: AuthService

  @BeforeEach
  fun `set up`() {
    auditEventService = spy(AuditEventService(mock(), TestClockHolder.CLOCK))
    authService = AuthService(
      userTokenRepository,
      auditEventService,
      tokenService
    )
  }

  @Test
  fun `can login with valid token`() {
    val tokenId = "avalidauth0token"
    val email = "backend@pactum.com"
    val token = "avalidtoken"
    val newExpire = Instant.now().plusSeconds(3600).toEpochMilli() / 1000

    val loginReq = LoginReq(tokenId)

    val user = Token(email = email, token = token, expire = newExpire)
    val jwtData = JwtData(email, null, null)

    whenever(tokenService.verifyAuth0Token(loginReq)).thenReturn(jwtData)
    whenever(userTokenRepository.findByEmail(email)).thenReturn(null)
    whenever(tokenService.generateExpire()).thenReturn(newExpire)
    whenever(tokenService.generateToken(email, newExpire, null, null)).thenReturn(token)
    whenever(userTokenRepository.save(user)).thenReturn(user)

    val googleRes = authService.login(loginReq)

    verify(auditEventService, times(1)).addAuditEvent(eq("AUTH_LOGIN_SUCCESSFUL"), anyString(), anyOrNull())
    val body = googleRes.body as LoginRes
    assertThat(body.accessToken).isEqualTo(token)
  }

  @Test
  fun `can not login if token is invalid`() {
    val tokenId = "aninvalidauth0token"

    val loginReq = LoginReq(tokenId)

    whenever(tokenService.verifyAuth0Token(loginReq)).thenThrow(InvalidTokenException::class.java)

    assertThrows<InvalidTokenException> {
      authService.login(loginReq)
    }
    verify(auditEventService, times(1)).addAuditEvent(eq("AUTH_LOGIN_FAILED"), anyString(), anyOrNull())
  }

  @Test
  fun `can logout`() {
    val email = "backend@pactum.com"
    val token = "avalidtoken"
    val newExpire = Instant.now().plusSeconds(3600).toEpochMilli() / 1000

    val user = Token(email = email, token = token, expire = newExpire)

    SessionHelper.setLoggedInUser(token, email, listOf(Role.Admin))

    whenever(userTokenRepository.findByEmail(email)).thenReturn(user)
    whenever(userTokenRepository.save(user)).thenReturn(user)

    val googleRes = authService.logout()

    assertThat(googleRes.accessToken).isEqualTo("N/A")
    verify(auditEventService, times(1)).addAuditEvent(eq("AUTH_LOGOUT"), anyString(), anyOrNull())
  }
}
