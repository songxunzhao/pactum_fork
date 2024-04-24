package com.pactum.health

import com.pactum.api.GenericOkResponse
import com.pactum.health.model.Health
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.GetMapping

@RestController
@Api(tags = ["tools"], description = "APIs consumed by humans and scripts")
class HealthController(
  private val healthService: HealthService
) {

  @ApiOperation(value = "Set system health")
  @PostMapping("/api/v1/health")
  fun setHealth(@RequestBody req: Health): GenericOkResponse {
    return healthService.setHealth(req)
  }

  @ApiOperation(value = "Get system health")
  @GetMapping("/api/v1/health/get")
  fun getHealth(): Health {
    return healthService.getHealth()
  }
}
