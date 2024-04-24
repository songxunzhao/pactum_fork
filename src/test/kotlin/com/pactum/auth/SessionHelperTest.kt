package com.pactum.auth

import com.pactum.auth.model.Role
import com.pactum.test.UnitTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.core.context.SecurityContextHolder

@UnitTest
class SessionHelperTest {
  @Test
  fun `can get logged-in user email`() {

    val token = "token"
    val email = "email"
    SessionHelper.setLoggedInUser(token, email, listOf(Role.Admin))

    val user = SessionHelper.getLoggedInUserEmail()
    Assertions.assertThat(user).isEqualTo(email)

    SecurityContextHolder.getContext().authentication = null
  }

  @Test
  fun `can get logged-in user role`() {

    val token = "token"
    val email = "email"
    SessionHelper.setLoggedInUser(token, email, listOf(Role.Admin))

    val roles = SessionHelper.getLoggedInUserRoles()
    Assertions.assertThat(roles.size).isEqualTo(1)
    Assertions.assertThat(roles[0].name).isEqualTo(Role.Admin.name)

    SecurityContextHolder.getContext().authentication = null
  }

  @Test
  fun `can get logged-in user clientTag`() {

    val token = "token"
    val email = "email"
    val tag = "tag"
    SessionHelper.setLoggedInUser(token, email, listOf(Role.Admin)).apply {
      clientTag = tag
    }

    val clientId = SessionHelper.getLoggedInUserClientTag()
    Assertions.assertThat(clientId).isEqualTo(tag)

    SecurityContextHolder.getContext().authentication = null
  }

  @Test
  fun `throws exception if user is not logged in`() {
    assertThrows<InvalidTokenException> {
      SessionHelper.getLoggedInUserEmail()
    }
  }

  @Test
  fun `can clear logged in user`() {
    val token = "token"
    val email = "email"
    val tag = "tag"
    SessionHelper.setLoggedInUser(token, email, listOf(Role.Admin)).apply {
      clientTag = tag
    }
    SessionHelper.clearLoggedInUser()
    Assertions.assertThat(SecurityContextHolder.getContext().authentication).isNull()
  }
}
