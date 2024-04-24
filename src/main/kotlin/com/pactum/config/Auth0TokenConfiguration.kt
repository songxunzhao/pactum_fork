package com.pactum.config

import com.auth0.jwk.JwkProviderBuilder
import com.auth0.utils.tokens.IdTokenVerifier
import com.auth0.utils.tokens.PublicKeyProvider
import com.auth0.utils.tokens.SignatureVerifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.security.interfaces.RSAPublicKey

@Configuration
class Auth0TokenConfiguration(
  @Value("\${auth.clientId}") private val clientId: String,
  @Value("\${auth.issuer}") private val issuer: String
) {

  @Bean
  fun resolveVerifier(): IdTokenVerifier {
    val provider = JwkProviderBuilder(issuer).build()
    val sigVerifier = SignatureVerifier.forRS256(
      PublicKeyProvider { keyId ->
        return@PublicKeyProvider provider[keyId].publicKey as RSAPublicKey
      }
    )
    return IdTokenVerifier.init(
      issuer,
      clientId,
      sigVerifier
    ).build()
  }
}
