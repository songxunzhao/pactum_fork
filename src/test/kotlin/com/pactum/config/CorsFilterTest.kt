package com.pactum.config

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.pactum.test.UnitTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletResponse

@UnitTest
class CorsFilterTest {

  lateinit var corsFilter: CorsFilter

  @BeforeAll
  fun setup() {
    corsFilter = CorsFilter(60)
  }

  @Test
  fun `check response headers if request is options and origin is allowed`() {
    val mockRequest = MockHttpServletRequest("OPTIONS", "/api/v1/auth/login")
    mockRequest.addHeader("Origin", corsFilter.allowedOrigins[0])
    val mockResponse = MockHttpServletResponse()
    val mockFilterChain = MockFilterChain()
    corsFilter.doFilter(mockRequest, mockResponse, mockFilterChain)
    val header1 = mockResponse.getHeaderValue("Access-Control-Allow-Origin")
    val header2 = mockResponse.getHeaderValue("Access-Control-Allow-Methods")
    assertEquals(mockResponse.status, HttpServletResponse.SC_NO_CONTENT)
    assertEquals(header1, mockRequest.getHeader("Origin"))
    assertEquals(header2, corsFilter.allowedMethods)
  }

  @Test
  fun `check response headers if request is options and origin is not allowed`() {
    val mockRequest = MockHttpServletRequest("OPTIONS", "/api/v1/auth/login")
    mockRequest.addHeader("Origin", "some unallowed origin")
    val mockResponse = MockHttpServletResponse()
    val mockFilterChain = MockFilterChain()
    corsFilter.doFilter(mockRequest, mockResponse, mockFilterChain)
    val header1 = mockResponse.getHeaderValue("Access-Control-Allow-Origin")
    val header2 = mockResponse.getHeaderValue("Access-Control-Allow-Methods")
    assertEquals(mockResponse.status, HttpServletResponse.SC_NO_CONTENT)
    assertNull(header1)
    assertEquals(header2, corsFilter.allowedMethods)
  }

  @Test
  fun `check response status if request is not options`() {
    val mockRequest = MockHttpServletRequest("POST", "/api/v1/auth/login")
    val mockResponse = MockHttpServletResponse()
    val mockFilterChain = mock<FilterChain>()
    corsFilter.doFilter(mockRequest, mockResponse, mockFilterChain)
    verify(mockFilterChain).doFilter(mockRequest, mockResponse)
    assertEquals(mockResponse.status, HttpServletResponse.SC_OK)
  }
}
