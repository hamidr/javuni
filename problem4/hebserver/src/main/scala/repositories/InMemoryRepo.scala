package repositories

import cats.effect.IO
import fs2.Stream
import models._

import java.time.Instant
import scala.collection.mutable
import scala.math.Ordering.Implicits.infixOrderingOps

class InMemoryRepo(dataSet: mutable.Set[IoTRecord]) extends IRecordRepo {
  def store: IoTRecord => IO[Unit] = record =>
    IO.delay { dataSet.add(record) }.flatMap(r => IO.raiseWhen(!r)(new Exception("Not added!!!")))

  def retrieveRange(from: Instant, to: Instant): Stream[IO, IoTRecord] = {
    val data = dataSet.filter { record =>
      record.time >= from && record.time < to }.toSeq.sortBy(_.id)
    Stream.emits(data)
  }
}

object InMemoryRepo {
  def createSet[T](): mutable.Set[T] = {
    import scala.jdk.CollectionConverters._
    java.util.Collections.newSetFromMap(
      new java.util.concurrent.ConcurrentHashMap[T, java.lang.Boolean]).asScala
  }

  def build: IRecordRepo = {
    new InMemoryRepo(createSet[IoTRecord]())
  }
}
