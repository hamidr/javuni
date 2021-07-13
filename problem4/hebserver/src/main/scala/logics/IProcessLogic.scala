package logics

import cats.effect.IO
import models.{IoTRecord, ProcessedData, ReadingsQuery}

trait IProcessLogic {
  def create(request: IoTRecord): IO[Unit]
  def retrieve(request: ReadingsQuery): IO[List[ProcessedData]]
}