package com.pactum.auth.model

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken

class PactumAuthenticationToken(token: String, email: String, roles: List<Role>) :
  PreAuthenticatedAuthenticationToken(token, email, roles.map { SimpleGrantedAuthority(it.name) }) {
  var clientTag: String? = null
}
