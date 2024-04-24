package com.pactum.config

import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.pactum.token.TokenService
import com.pactum.auth.InvalidTokenException
import com.pactum.auth.model.TokenObject
import com.pactum.test.UnitTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import javax.servlet.FilterChain
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse

@UnitTest
class AuthorizationFilterTest {

  lateinit var authrizationFilter: AuthorizationFilter
  private val tokenService: TokenService = mock()

  @BeforeAll
  fun setup() {
    authrizationFilter = AuthorizationFilter(tokenService)
  }

  @Test
  fun `check token if invalid authorization header is passed in`() {
    val mockRequest = MockHttpServletRequest("GET", "/api/v1/states/blacklist")
    mockRequest.addHeader("Authorization", "Bearer invalidtoken")
    val mockResponse = MockHttpServletResponse()
    val mockFilterChain = MockFilterChain()
    whenever(tokenService.decryptToken("invalidtoken")).thenThrow(InvalidTokenException::class.java)
    authrizationFilter.doFilterInternal(mockRequest, mockResponse, mockFilterChain)
    assertEquals(mockResponse.status, HttpServletResponse.SC_UNAUTHORIZED)
  }

  @Test
  fun `check token if valid authorization header is passed in`() {
    val mockRequest = MockHttpServletRequest("GET", "/api/v1/states/blacklist")
    mockRequest.addHeader("Authorization", "Bearer validtoken")
    val mockResponse = MockHttpServletResponse()
    val mockFilterChain = mock<FilterChain>()
    val tokenObject = TokenObject("", "backend@pactum.com", 12341212, null, null)
    whenever(tokenService.decryptToken("validtoken")).thenReturn(tokenObject)
    doNothing().`when`(tokenService).checkTokenIsValid("validtoken", tokenObject)
    authrizationFilter.doFilterInternal(mockRequest, mockResponse, mockFilterChain)
    verify(mockFilterChain).doFilter(mockRequest, mockResponse)
    assertEquals(mockResponse.status, HttpServletResponse.SC_OK)
  }

  @Test
  fun `check token if valid cookie is sent`() {
    val mockRequest = MockHttpServletRequest("GET", "/api/v1/states/blacklist")
    mockRequest.setCookies(Cookie("pactumAccessToken", "validtoken"))
    val mockResponse = MockHttpServletResponse()
    val mockFilterChain = mock<FilterChain>()
    val tokenObject = TokenObject("", "backend@pactum.com", 12341212, null, null)
    whenever(tokenService.decryptToken("validtoken")).thenReturn(tokenObject)
    doNothing().`when`(tokenService).checkTokenIsValid("validtoken", tokenObject)
    authrizationFilter.doFilterInternal(mockRequest, mockResponse, mockFilterChain)
    verify(mockFilterChain).doFilter(mockRequest, mockResponse)
    assertEquals(mockResponse.status, HttpServletResponse.SC_OK)
  }
}
