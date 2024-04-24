package com.pactum.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import javax.servlet.Filter
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
class CorsFilter(
  @Value("\${server.cookieMaxAge}") val cookieMaxAge: Long
) : Filter {

  val allowedOrigins = listOf(
    "https://localhost:3000",
    "https://admin.pactum.com",
    "https://admin-staging.pactum.com",
    "https://admin-sandbox.pactum.com",
    "https://chat.pactum.com"
  )
  val allowedMethods = "POST, GET, OPTIONS, DELETE, PUT"

  override fun doFilter(req: ServletRequest, res: ServletResponse, chain: FilterChain) {
    val response = res as HttpServletResponse
    val request = req as HttpServletRequest

    response.setHeader("Access-Control-Allow-Methods", allowedMethods)
    response.setHeader("Access-Control-Max-Age", cookieMaxAge.toString())
    response.setHeader("Access-Control-Allow-Credentials", "true")
    response.setHeader(
      "Access-Control-Allow-Headers",
      "Origin,Accept,X-Requested-With,Content-Type,Set-Cookie," +
        "Access-Control-Request-Method,Access-Control-Request-Headers,Authorization,sessionid,sentry-trace"
    )

    val originHeader = request.getHeader("Origin")
    if (allowedOrigins.contains(originHeader)) {
      response.setHeader("Access-Control-Allow-Origin", originHeader)
    }
    if (request.method == HttpMethod.OPTIONS.name) {
      response.status = HttpStatus.NO_CONTENT.value()
    } else {
      chain.doFilter(req, res)
    }
  }
}
