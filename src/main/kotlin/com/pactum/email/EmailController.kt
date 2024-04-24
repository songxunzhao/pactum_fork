package com.pactum.email

import com.pactum.api.GenericCreatedResponse
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.PostMapping
import javax.validation.Valid

@RestController
@Api(tags = ["website"], description = "APIs consumed by the website")
class EmailController(
  private val service: EmailService
) {

  @ApiOperation(value = "Store a contact e-mail")
  @PostMapping("/api/v1/emails")
  fun addEmail(@Valid @RequestBody body: String): GenericCreatedResponse {
    return service.addEmail(body)
  }
}
