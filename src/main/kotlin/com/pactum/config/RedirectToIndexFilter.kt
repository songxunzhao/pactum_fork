package com.pactum.config

import mu.KLogging
import org.springframework.stereotype.Component
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class RedirectToIndexFilter : Filter {

  companion object : KLogging() {
    val ignoredPaths =
      listOf(
        "/api",
        "/static",
        "/actuator",
        "/favicon.png",
        "/robots.txt",
        "/manifest.json",
        "/Pactum_press_release.pdf",
        "/Pactum_press_release_2.pdf",
        "/Pactum_press_release_3.pdf",
        "/swagger-ui.html",
        "/webjars",
        "/swagger-resources"
      )

    val indexPageHeaders =
      mapOf(
        "Content-Security-Policy" to "frame-ancestors digitalexpo.e-estonia.com"
      )
  }

  override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {

    val req = request as HttpServletRequest
    val res = response as HttpServletResponse
    val requestURI = req.requestURI

    if (ignoredPaths.any { requestURI.startsWith(it) }) {
      chain.doFilter(request, response)
      return
    }
    if (req.method != "GET") {
      chain.doFilter(request, response)
      return
    }

    indexPageHeaders.forEach { res.setHeader(it.key, it.value) }

    // all requests not api or static will be forwarded to index page.
    request.getRequestDispatcher("/index.html").forward(request, response)
  }
}
