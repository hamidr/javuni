package com.kaizo

import java.time.{Instant, OffsetDateTime, ZoneOffset}
import scala.concurrent.duration.{DurationInt, FiniteDuration}

object Utils:
  val POLLING_IN_MIN: Long = 60 / 10 // Rate limited: 10 per minute

  val TRIGGER_TIME: FiniteDuration = 1.second // No reason. 1 sec is fine too.

  def now: OffsetDateTime = Instant.now().atOffset(ZoneOffset.UTC)

  def beforeNow: OffsetDateTime = now.minusSeconds(POLLING_IN_MIN)

  def nextWindow: OffsetDateTime = now.plusSeconds(POLLING_IN_MIN)
  
  def oneMinuteAgo: OffsetDateTime = now.minusMinutes(1)

end Utils