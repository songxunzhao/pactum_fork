package com.pactum.test

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

object TestClockHolder {
  // 15:05
  val NOW: Instant = Instant.EPOCH.plusSeconds(54300)
  val CLOCK: Clock = Clock.fixed(NOW, ZoneId.of("Z"))
}
