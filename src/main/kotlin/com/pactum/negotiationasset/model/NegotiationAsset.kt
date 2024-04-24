package com.pactum.negotiationasset.model

import org.springframework.data.annotation.Id
import java.time.Instant

data class NegotiationAsset(
  @Id
  val id: Long? = null,
  val driveId: String,
  val type: String,
  val generationId: Long,
  val md5Checksum: String,
  val time: Instant
) {
  companion object {
    fun newFlowAsset(driveId: String, generationId: Long, md5Checksum: String): NegotiationAsset {
      return NegotiationAsset(
        null,
        driveId,
        NegotiationAssetType.FLOW.name,
        generationId,
        md5Checksum,
        Instant.now()
      )
    }
    fun newModelAsset(driveId: String, generationId: Long, md5Checksum: String): NegotiationAsset {
      return NegotiationAsset(
        null,
        driveId,
        NegotiationAssetType.MODEL.name,
        generationId,
        md5Checksum,
        Instant.now()
      )
    }
  }
}

enum class NegotiationAssetType {
  FLOW,
  MODEL
}
