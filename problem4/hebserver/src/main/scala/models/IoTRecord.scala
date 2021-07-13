package models

import java.time.Instant

final case class IoTRecord(id: String, thermostat: Double, heartRate: Double, carFuel: Double, time: Instant)

object IoTRecord {
  import shapeless.Generic
  implicit val gen = Generic[IoTRecord]
}