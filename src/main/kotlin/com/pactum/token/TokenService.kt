package com.pactum.token

import com.auth0.jwt.JWT
import com.auth0.utils.tokens.IdTokenVerifier
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.pactum.auth.InvalidTokenException
import com.pactum.auth.model.JwtData
import com.pactum.auth.model.LoginReq
import com.pactum.auth.model.TokenObject
import com.pactum.utils.AESEncryption
import com.pactum.utils.trace
import io.micrometer.core.annotation.Timed
import io.opentracing.Span
import io.opentracing.util.GlobalTracer
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

private val logger = KotlinLogging.logger {}

@Service
class TokenService(
  @Value("\${auth.clientSecret}") private val clientSecret: String,
  private val idTokenVerifier: IdTokenVerifier,
  private val tokenRepository: TokenRepository
) {

  @Timed("token_service.verify_auth0_token")
  fun verifyAuth0Token(loginReq: LoginReq): JwtData {
    val tracer = GlobalTracer.get()
    return tracer.trace("Auth0_verifyToken") { span: Span ->
      try {
        idTokenVerifier.verify(loginReq.jwtToken)
        val jwt = JWT.decode(loginReq.jwtToken)
        val email = jwt.getClaim("email").asString()
        val jwtRoles =
          jwt.getClaim("http://schemas.microsoft.com/ws/2008/06/identity/claims/roles").asList(String::class.java)
        val roles = if (jwtRoles != null && jwtRoles.size > 0) jwtRoles else null
        val appMetadata = jwt.getClaim("https://login.pactum.com/app_metadata").asMap()
        val clientTag = if (appMetadata != null) appMetadata["client_tag"] as? String else null
        return@trace JwtData(email, roles, clientTag)
      } catch (e: Exception) {
        logger.error(e.localizedMessage, e)
      }
      throw InvalidTokenException()
    } as JwtData
  }

  internal fun generateToken(email: String, expire: Long, roles: List<String>?, clientTag: String?): String {
    val random = UUID.randomUUID().toString().substring(16, 24)
    val token = jacksonObjectMapper().writeValueAsString(
      TokenObject(random, email, expire, roles, clientTag)
    )
    return AESEncryption.encrypt(token, clientSecret)
  }

  internal fun generateExpire(): Long {
    return Instant.now().plus(1, ChronoUnit.DAYS).epochSecond
  }

  fun decryptToken(encryptedToken: String): TokenObject {
    try {
      val decrypted = AESEncryption.decrypt(encryptedToken, clientSecret)
      val token = jacksonObjectMapper().readValue(decrypted, TokenObject::class.java)
      if (!isExpired(token.expire)) {
        return token
      }
    } catch (e: Exception) {
      // e.printStackTrace()
    }
    throw InvalidTokenException()
  }

  private fun isExpired(expire: Long): Boolean {
    val now = Instant.now().epochSecond
    return expire < now
  }

  internal fun checkTokenIsValid(token: String, tokenObject: TokenObject) {
    val tokenDb = tokenRepository.findByToken(token) ?: throw InvalidTokenException()
    if (tokenDb.email != tokenObject.email) throw InvalidTokenException()
    if (isExpired(tokenDb.expire)) throw InvalidTokenException()
    if (isExpired(tokenObject.expire)) throw InvalidTokenException()
  }
}
