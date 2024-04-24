package com.pactum.negotiationfield.model

import com.pactum.client.model.NegotiationFieldConfigType
import com.pactum.test.UnitTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@UnitTest
class NegotiationFieldApiEntityTest {

  @Test
  fun `can create entity from model`() {
    val field = NegotiationField.createMock(2, NegotiationFieldConfigType.MODEL, "b", "c")
    val entity = field.toApiEntity()

    assertThat(entity.type.name).isEqualTo(field.type)
    assertThat(entity.attribute).isEqualTo(field.attribute)
    assertThat(entity.value).isEqualTo(field.value)
  }
}
