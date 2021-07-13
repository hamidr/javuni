package models

import java.time.Instant

final case class ReadingsQuery(
  from: Instant,
  to: Instant,
  sensors: Set[SensorType],
  operations: Set[OpFunc]
)