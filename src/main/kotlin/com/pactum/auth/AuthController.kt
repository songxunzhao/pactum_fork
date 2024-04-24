package com.pactum.auth

import com.pactum.auth.model.LoginReq
import com.pactum.auth.model.LoginRes
import com.pactum.api.GenericOkResponse
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.GetMapping

@RestController
@Api(tags = ["admin"], description = "APIs consumed by the Admin panel")
class AuthController(
  private val authService: AuthService
) {

  @ApiOperation(value = "Obtain an access token")
  @PostMapping("/api/v1/auth/login")
  fun login(@RequestBody loginReq: LoginReq): GenericOkResponse {
    return authService.login(loginReq)
  }

  @ApiOperation(value = "Log out user")
  @GetMapping("/api/v1/auth/logout")
  fun logout(): LoginRes {
    return authService.logout()
  }
}
