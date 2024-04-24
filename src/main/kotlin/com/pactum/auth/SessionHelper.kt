package com.pactum.auth

import com.pactum.auth.model.PactumAuthenticationToken
import com.pactum.auth.model.Role
import org.springframework.security.core.context.SecurityContextHolder

class SessionHelper {

  companion object {

    fun setLoggedInUser(token: String, email: String, roles: List<Role>): PactumAuthenticationToken {
      val authentication =
        PactumAuthenticationToken(token, email, roles).apply {
          isAuthenticated = true
        }
      SecurityContextHolder.getContext().authentication = authentication
      return authentication
    }

    fun clearLoggedInUser() {
      SecurityContextHolder.getContext().authentication = null
    }

    private fun getLoggedInUser(): PactumAuthenticationToken {
      return try {
        SecurityContextHolder.getContext().authentication as PactumAuthenticationToken
      } catch (e: Exception) {
        throw InvalidTokenException()
      }
    }

    fun getLoggedInUserEmail(): String {
      return getLoggedInUser().credentials as String
    }

    fun getLoggedInUserRoles(): List<Role> {
      return getLoggedInUser().authorities.map { Role.valueOf(it.authority) }
    }

    fun getLoggedInUserClientTag(): String? {
      return getLoggedInUser().clientTag
    }
  }
}
