package com.pactum.negotiationasset

import com.pactum.negotiationasset.model.NegotiationAsset
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface NegotiationAssetRepository : CrudRepository<NegotiationAsset, Long> {

  @Query(
    """
      SELECT negotiation_asset.*
      FROM negotiation_asset 
      WHERE drive_id = :driveId AND type = :assetType 
      ORDER BY time DESC LIMIT 1
    """
  )
  fun findLatestByDriveId(driveId: String, assetType: String): NegotiationAsset?

  @Query("SELECT setval('chat_asset_id_seq', (SELECT MAX(id) FROM negotiation_asset))")
  fun resolveInsertException(): Int
}
