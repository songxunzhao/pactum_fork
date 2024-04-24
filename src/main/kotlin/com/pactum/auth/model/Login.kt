package com.pactum.auth.model

data class LoginReq(
  val jwtToken: String
)

data class LoginRes(
  val accessToken: String
)
