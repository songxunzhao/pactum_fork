package com.pactum.negotiationfield

import com.pactum.api.GenericOkResponse
import com.pactum.negotiationfield.model.NegotiationField
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@RestController
@Api(tags = ["admin"], description = "APIs consumed by the Admin panel")
class NegotiationFieldController(
  private val negotiationFieldService: NegotiationFieldService
) {
  @ApiOperation(value = "Get negotiation field values")
  @GetMapping("/api/v1/negotiation/{id}/fields")
  fun getNegotiationFields(@PathVariable id: Long): List<NegotiationField.ApiEntity> {
    return negotiationFieldService.getFieldsApiEntitiesForNegotiation(id)
  }

  @ApiOperation(value = "Update negotiation field values")
  @PostMapping("/api/v1/negotiation/{id}/set-fields")
  fun setNegotiationFields(
    @RequestBody fields: List<NegotiationField.ApiEntity>,
    @PathVariable id: Long
  ): GenericOkResponse {
    return negotiationFieldService.setFieldsForNegotiation(id, fields)
  }
}
