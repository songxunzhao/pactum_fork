package com.pactum.health.model

class Health(var status: HealthStatus) {
  override fun equals(other: Any?): Boolean {
    return status == (other as? Health)?.status
  }
  override fun hashCode(): Int {
    return 31 * status.ordinal
  }
}

enum class HealthStatus {
  UP,
  DOWN
}
