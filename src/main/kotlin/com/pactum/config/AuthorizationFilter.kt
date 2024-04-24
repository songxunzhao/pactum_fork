package com.pactum.config

import com.pactum.auth.InvalidTokenException
import com.pactum.auth.model.Role
import com.pactum.auth.model.TokenObject
import com.pactum.token.TokenService
import com.pactum.auth.SessionHelper
import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.WebUtils
import java.net.URLDecoder
import java.nio.charset.Charset
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

private val klogger = KotlinLogging.logger {}

@Component
class AuthorizationFilter(
  private val tokenService: TokenService
) : OncePerRequestFilter() {

  companion object {
    const val HEADER = "Authorization"
    const val PREFIX = "Bearer "
    const val PACTUM_COOKIE = "pactumAccessToken"
  }

  override fun shouldNotFilter(request: HttpServletRequest): Boolean {
    if (!request.requestURI.startsWith("/api"))
      return true
    WebSecurityConfiguration.PERMITTED_APIS.forEach { url ->
      if (request.requestURI.startsWith(url.replace("**", "")))
        return true
    }
    return false
  }

  public override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
    val token = getToken(request)
    if (token != null) {
      try {
        val correctToken = token.replace(PREFIX, "")
        val tokenObject = tokenService.decryptToken(correctToken)
        tokenService.checkTokenIsValid(correctToken, tokenObject)
        setAuthentication(correctToken, tokenObject)
      } catch (e: InvalidTokenException) {
        klogger.error(e.localizedMessage, e)
        SessionHelper.clearLoggedInUser()
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.message)
        return
      }
    }
    chain.doFilter(request, response)
  }

  private fun getToken(request: HttpServletRequest): String? {
    val authHeader = request.getHeader(HEADER)
    if (authHeader != null && authHeader.startsWith(PREFIX))
      return authHeader
    WebUtils.getCookie(request, PACTUM_COOKIE)?.let {
      return URLDecoder.decode(it.value, Charset.defaultCharset())
    }
    return null
  }

  private fun setAuthentication(token: String, tokenObject: TokenObject) {
    val roles = when {
        tokenObject.email == "api" -> listOf(Role.Admin)
        tokenObject.roles != null -> tokenObject.roles.map { Role.valueOf(it) }
        else -> listOf(Role.UNDEFINED)
    }
    SessionHelper.setLoggedInUser(token, tokenObject.email, roles).apply {
      clientTag = tokenObject.clientTag
    }
  }
}
