package com.pactum.negotiation

import com.pactum.negotiation.model.Negotiation
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface NegotiationRepository : CrudRepository<Negotiation, Long> {

  fun findFirstByStateIdAndIsDeletedIsFalseOrderByCreateTimeDesc(stateId: String): Negotiation?

  @Query(
    """
      SELECT negotiation.* 
      FROM negotiation 
      WHERE state_id IN (:stateIds) 
      AND is_deleted = false 
      ORDER BY create_time DESC
    """
  )
  fun findByStateIds(@Param("stateIds")stateIds: List<String>): List<Negotiation>

  @Query(
    """
      SELECT negotiation.* 
      FROM negotiation 
      WHERE flow_id IN (:flowIds) 
      AND is_deleted = false 
      ORDER BY create_time DESC
    """
  )
  fun findByFlowIds(@Param("flowIds")flowIds: List<String>): List<Negotiation>

  fun findByClientIdAndFlowIdAndIsDeletedIsFalse(clientId: Long, flowId: String): List<Negotiation>

  fun findByClientIdAndFlowIdAndModelIdAndModelKeyAndIsDeletedIsFalse(
    clientId: Long,
    flowId: String,
    modelId: String,
    modelKey: String
  ): List<Negotiation>

  fun findByClientIdAndIsDeletedIsFalseOrderByCreateTimeDesc(clientId: Long): List<Negotiation>

  @Query("SELECT setval('client_chat_id_seq', (SELECT MAX(id) FROM negotiation))")
  fun resolveInsertException(): Int
}
