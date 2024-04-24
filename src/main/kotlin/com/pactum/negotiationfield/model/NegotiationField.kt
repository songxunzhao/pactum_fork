package com.pactum.negotiationfield.model

import com.pactum.client.model.NegotiationFieldConfigType
import org.springframework.data.annotation.Id
import java.time.Instant

data class NegotiationField(
  @Id
  val id: Long? = null,
  val negotiationId: Long,
  val type: String,
  val attribute: String,
  val value: String,
  val createTime: Instant
) {

  companion object;

  data class ApiEntity(
    val type: NegotiationFieldConfigType,
    val attribute: String,
    val value: String
  ) {

    override fun equals(other: Any?): Boolean {
      return type == (other as? ApiEntity)?.type
    }

    override fun hashCode(): Int {
      return 31 * type.ordinal
    }
  }

  fun toApiEntity(): ApiEntity {
    return ApiEntity(
      type = NegotiationFieldConfigType.valueOf(type),
      attribute = attribute,
      value = value
    )
  }
}
