package com.pactum.negotiation.batch

import com.pactum.api.GenericOkResponse
import com.pactum.negotiation.NegotiationService
import com.pactum.negotiation.batch.action.CreateUpdateBatchService
import com.pactum.negotiation.batch.action.UpdateClientVisibilityService
import com.pactum.negotiation.batch.action.UpdateFieldsService
import com.pactum.negotiation.batch.action.UpdateStatusService
import com.pactum.negotiation.batch.action.UpdateSupplierVisibilityService
import com.pactum.negotiation.batch.model.BatchActionReq
import com.pactum.negotiation.batch.model.BatchActionType
import com.pactum.negotiation.batch.model.CreateBatchOfNegotiationsReq
import com.pactum.negotiation.model.ReloadNegotiationModelReq
import com.pactum.negotiation.model.UpdateNegotiationReq
import com.pactum.utils.Utils
import io.opentracing.propagation.Format
import io.opentracing.propagation.TextMapAdapter
import io.opentracing.util.GlobalTracer
import org.springframework.stereotype.Service

@Service
class NegotiationBatchService(
  private val negotiationService: NegotiationService,
  private val createUpdateBatchService: CreateUpdateBatchService,
  private val updateStatusService: UpdateStatusService,
  private val updateClientVisibilityService: UpdateClientVisibilityService,
  private val updateSupplierVisibilityService: UpdateSupplierVisibilityService,
  private val updateFieldsService: UpdateFieldsService
) {

  fun batchAction(actionReq: BatchActionReq, spanInfo: Map<String, String>): GenericOkResponse {
    return extractSpanAndTrace(
      spanInfo,
      "batchAction",
      fun(): GenericOkResponse {
        return when (actionReq.action) {
          BatchActionType.CREATE_UPDATE_BATCH ->
            createUpdateBatchService.createUpdate(Utils.cast<CreateBatchOfNegotiationsReq>(actionReq.body))
          BatchActionType.RELOAD_MODEL ->
            negotiationService.reloadNegotiationModel(Utils.cast<ReloadNegotiationModelReq>(actionReq.body))
          BatchActionType.UPDATE_STATUS ->
            updateStatusService.updateStatus(Utils.cast<Map<String, UpdateNegotiationReq>>(actionReq.body))
          BatchActionType.UPDATE_CLIENT_VISIBILITY ->
            updateClientVisibilityService.updateClientVisibility(
              Utils.cast<Map<String, UpdateNegotiationReq>>(actionReq.body)
            )
          BatchActionType.UPDATE_SUPPLIER_VISIBILITY ->
            updateSupplierVisibilityService.updateSupplierVisibility(
              Utils.cast<Map<String, UpdateNegotiationReq>>(actionReq.body)
            )
          BatchActionType.UPDATE_FIELDS ->
            updateFieldsService.updateFields(Utils.cast<Map<String, UpdateNegotiationReq>>(actionReq.body))
        }
      }
    )
  }

  private fun extractSpanAndTrace(
    spanMap: Map<String, String>,
    operationName: String,
    handler: () -> GenericOkResponse
  ): GenericOkResponse {
    val tracer = GlobalTracer.get()
    val extractedContext =
      tracer.extract(Format.Builtin.HTTP_HEADERS, TextMapAdapter(spanMap))
    return if (extractedContext != null) {
      val span = tracer.buildSpan(operationName).asChildOf(extractedContext).start()
      val scope = tracer.activateSpan(span)
      val result = handler()
      scope.close()
      span.finish()
      result
    } else {
      handler()
    }
  }
}
