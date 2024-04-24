package com.pactum.auth

import com.pactum.audit.AuditEventService
import com.pactum.auth.model.LoginReq
import com.pactum.auth.model.LoginRes
import com.pactum.api.GenericOkResponse
import com.pactum.token.TokenService
import com.pactum.token.TokenRepository
import com.pactum.token.model.Token
import org.springframework.stereotype.Service

@Service
class AuthService(
  private val userTokenRepository: TokenRepository,
  private val auditEventService: AuditEventService,
  private val tokenService: TokenService
) {

  fun login(loginReq: LoginReq): GenericOkResponse {

    try {
      val jwtData = tokenService.verifyAuth0Token(loginReq)
      val userToken = userTokenRepository.findByEmail(jwtData.email)
      val newUserToken = getNewUserToken(userToken, jwtData.email, jwtData.roles, jwtData.clientTag)
      userTokenRepository.save(newUserToken)
      SessionHelper.setLoggedInUser(newUserToken.token, jwtData.email, emptyList())
      auditEventService.addAuditEvent(AuthAuditEventType.AUTH_LOGIN_SUCCESSFUL.name, "User has authenticated")
      return GenericOkResponse(LoginRes(newUserToken.token))
    } catch (e: InvalidTokenException) {
      auditEventService.addAuditEvent(AuthAuditEventType.AUTH_LOGIN_FAILED.name, "Authentication failure")
      throw e
    }
  }

  private fun getNewUserToken(
    currentUserToken: Token?,
    email: String,
    roles: List<String>?,
    clientTag: String?
  ): Token {
    val expire = tokenService.generateExpire()
    val newToken = tokenService.generateToken(email, expire, roles, clientTag)
    return currentUserToken?.copy(expire = expire, token = newToken) ?: Token.create(email, newToken, expire)
  }

  fun logout(): LoginRes {
    val email = SessionHelper.getLoggedInUserEmail()
    val user = userTokenRepository.findByEmail(email) ?: throw InvalidTokenException()
    val isApi = email == "api"
    val updateToken = if (isApi) user.token else ""
    val updateExpire = if (isApi) user.expire else 0
    val updateUser = user.copy(token = updateToken, expire = updateExpire)
    userTokenRepository.save(updateUser)
    auditEventService.addAuditEvent(AuthAuditEventType.AUTH_LOGOUT.name, "User has logged out")
    return LoginRes("N/A")
  }

  enum class AuthAuditEventType {
    AUTH_LOGIN_SUCCESSFUL,
    AUTH_LOGIN_FAILED,
    AUTH_LOGOUT,
  }
}
