package logic

import cats.effect.{Concurrent, ConcurrentEffect}
import db.FileStorage
import fs2.{Pipe, Stream}
import models.{DigestedText, Word, WordOccurrence}

trait StreamLogic[F[_]] {
  def saveStream(fileName: String, data: Stream[F, Byte]): F[Unit]
  def getStream(fileName: String): Stream[F, DigestedText]
}

class StreamLogicImpl[F[_]: ConcurrentEffect](processChunkSize: Int, storage: FileStorage[F], filter: Filters.Process[F])
  extends StreamLogic[F] {

  import StreamLogic._

  def saveStream(fileName: String, data: Stream[F, Byte]): F[Unit] = {
    data.through(toWord[F])
      .through(filter)
      .through(wordsToWordOccurrences(processChunkSize))
      .through(storage.store(fileName))
      .compile.drain
  }

  def getStream(fileName: String): Stream[F, DigestedText] = {
    storage.retrieve(fileName)
      .through(wordOccurrencesToDigestedText)
  }
}

object StreamLogic {
  def apply[F[_]: ConcurrentEffect](processChunkSize: Int, storage: FileStorage[F], filter: Filters.Process[F]): StreamLogic[F] =
    new StreamLogicImpl[F](processChunkSize, storage, filter)

  def toWord[F[_]]: Pipe[F, Byte, Word] =
    _.split(_ == Word.separator).map(Word.fromChunkByte).unNone

  def wordsToWordOccurrences[F[_]: ConcurrentEffect](chunkSize: Int): Pipe[F, Word, WordOccurrence] = { stream =>
    //groupBy and count the elements
    val counter: Pipe[F, Word, WordOccurrence] = _.fold(Map[Word, Int]()) { case (map, key) =>
      map.updatedWith(key) {
        case Some(value) => Some(value + 1)
        case None => Some(1)
      }
    }.flatMap { map =>
      val list = map.toSeq.map { case (w, n) => WordOccurrence(w, n)}
      Stream.emits(list)
    }

    stream.chunkN(chunkSize)
      .map(c => Stream.chunk[F, Word](c).through(counter))
      .parJoinUnbounded // process the chunks in parallel
  }

  def wordOccurrencesToDigestedText[F[_]: Concurrent]: Pipe[F, WordOccurrence, DigestedText] = { stream =>
    val segmentProcess: Pipe[F, WordOccurrence, DigestedText] = _.fold(Map[Word, Long]()) { case (acc, wo) =>
      acc.updatedWith(wo.word) {
        case Some(n) => Some(n + wo.occurrences)
        case None => Some(wo.occurrences.toLong)
      }
    }.map(DigestedText.fromMap)

    stream.through(segmentProcess) //no parallel processing because im not getting paid for this
  }
}