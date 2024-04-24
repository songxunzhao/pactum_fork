package com.pactum.exception

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import javax.servlet.RequestDispatcher
import javax.servlet.http.HttpServletRequest

@Controller
@Api(tags = ["website"], description = "APIs consumed by the website")
class ErrorPageController(
  @Value("\${server.adminUrl}") val adminUrl: String
) : ErrorController {

  @GetMapping(value = ["/error"])
  @ApiOperation(value = "Displays an error message")
  fun handleError(request: HttpServletRequest, model: Model): Any {
    model["adminUrl"] = adminUrl
    val statusCode = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE) as? Int
    val isJson = request.getHeader("Content-type") == "application/json"
    return if (isJson) respondJson(statusCode) else respondHtml(statusCode)
  }

  private fun respondJson(statusCode: Int?): ResponseEntity<ApiError> {
    if (statusCode != null) {
      val httpStatus = HttpStatus.valueOf(statusCode)
      return ResponseEntity(httpStatus.toException().toApiError(), httpStatus)
    }
    return ResponseEntity(RuntimeException().toApiError(), HttpStatus.INTERNAL_SERVER_ERROR)
  }

  private fun respondHtml(statusCode: Int?): String {

    if (statusCode != null) {
      return "error-$statusCode"
    }
    return "error-500"
  }

  override fun getErrorPath(): String {
    return "/error"
  }
}
