package repositories

import cats.effect.IO
import models.IoTRecord
import fs2.Stream

import java.time.Instant

trait IRecordRepo {
  def store: IoTRecord => IO[Unit]
  def retrieveRange(from: Instant, to: Instant): Stream[IO, IoTRecord]
}