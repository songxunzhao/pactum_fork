package com.pactum.negotiationstate

import com.pactum.chat.model.NegotiationState
import com.pactum.chat.model.KeyValueWithStrings
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface NegotiationStateRepository : CrudRepository<NegotiationState, Long> {

  @Query("SELECT negotiation_state.* FROM negotiation_state WHERE state_id = :stateId ORDER BY time DESC LIMIT 1")
  fun findByStateId(stateId: String): NegotiationState?

  @Query("SELECT time AT TIME ZONE 'UTC' FROM negotiation_state WHERE state_id = :stateId ORDER BY time ASC LIMIT 1")
  fun findChatOpenTimeByStateId(stateId: String): Instant?

  @Query(
    """
    SELECT time AT TIME ZONE 'UTC' FROM negotiation_state
      WHERE negotiation_id IN (SELECT id FROM negotiation WHERE state_id = :stateId)
        ORDER BY time DESC LIMIT 1
    """
  )
  fun findChatLastUpdateTimeByStateId(stateId: String): Instant?

  @Query(
    """
    SELECT DISTINCT state_id FROM negotiation_state
      WHERE negotiation_id IN (SELECT id FROM negotiation WHERE flow_id = :flowId)
        AND (:excludeStateIdPrefix::varchar IS NULL OR state_id NOT LIKE :excludeStateIdPrefix||'%')
    """
  )
  fun getAllStateIdsByFlowId(flowId: String, excludeStateIdPrefix: String? = null): List<String>?

  @Query(
    """
    SELECT negotiation_state.* FROM negotiation_state 
      WHERE negotiation_id IN (SELECT id FROM negotiation WHERE flow_id = :flowId)
        AND (:excludeStateIdPrefix::varchar IS NULL OR state_id NOT LIKE :excludeStateIdPrefix||'%')
          ORDER BY time ASC
    """
  )
  fun getStatesByFlowId(flowId: String, excludeStateIdPrefix: String? = null): List<NegotiationState>

  @Query(
    """
    SELECT negotiation_state.* FROM negotiation_state WHERE state_id = :stateId 
        AND (:excludeStateIdPrefix::varchar IS NULL OR state_id NOT LIKE :excludeStateIdPrefix||'%') ORDER BY time ASC
    """
  )
  fun getStatesByStateId(stateId: String, excludeStateIdPrefix: String? = null): List<NegotiationState>

  @Query(
    "WITH\n" +
      "single_chat_state AS (\n" +
      "    SELECT state\n" +
      "    FROM negotiation_state WHERE state_id = :stateId ORDER BY time DESC LIMIT 1\n" +
      "    ),\n" +
      "unwrapped_previous_steps AS (\n" +
      "    SELECT *\n" +
      "    FROM single_chat_state s, json_array_elements((s.state->'renderedSteps')::json) obj\n" +
      "     )\n" +
      "SELECT SUBSTRING(value->>'variable', 2, LENGTH(value->>'variable')-2) as key, value->>'value' as value\n" +
      "FROM unwrapped_previous_steps\n" +
      "WHERE value->>'variable' is not NULL;"
  )
  fun getAllVariablesByStateId(stateId: String): List<KeyValueWithStrings>?
}
