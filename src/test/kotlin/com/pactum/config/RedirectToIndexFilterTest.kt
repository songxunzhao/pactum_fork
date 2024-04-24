package com.pactum.config

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.pactum.test.UnitTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import javax.servlet.FilterChain

@UnitTest
class RedirectToIndexFilterTest {

  lateinit var redirectToIndexFilter: RedirectToIndexFilter

  @BeforeAll
  fun setup() {
    redirectToIndexFilter = RedirectToIndexFilter()
  }

  @Test
  fun `redirects when request is valid GET request`() {
    val mockRequest = MockHttpServletRequest("GET", "/login")
    val mockResponse = MockHttpServletResponse()
    val mockFilterChain = mock<FilterChain>()
    redirectToIndexFilter.doFilter(mockRequest, mockResponse, mockFilterChain)
    verifyZeroInteractions(mockFilterChain)
    assertEquals("/index.html", mockResponse.forwardedUrl)
  }

  @Test
  fun `does not redirect when request is not GET`() {
    val mockRequest = MockHttpServletRequest("POST", "/login")
    val mockResponse = MockHttpServletResponse()
    val mockFilterChain = mock<FilterChain>()
    redirectToIndexFilter.doFilter(mockRequest, mockResponse, mockFilterChain)
    verify(mockFilterChain).doFilter(mockRequest, mockResponse)
    assertNull(mockResponse.forwardedUrl)
  }

  @ParameterizedTest
  @MethodSource("ignoredPaths")
  fun `does not redirect when path is ignored`(ignoredPath: String) {
    val mockRequest = MockHttpServletRequest("GET", ignoredPath)
    val mockResponse = MockHttpServletResponse()
    val mockFilterChain = mock<FilterChain>()
    redirectToIndexFilter.doFilter(mockRequest, mockResponse, mockFilterChain)
    verify(mockFilterChain).doFilter(mockRequest, mockResponse)
    assertNull(mockResponse.forwardedUrl)
  }

  private fun ignoredPaths() = RedirectToIndexFilter.ignoredPaths.toTypedArray()
}
