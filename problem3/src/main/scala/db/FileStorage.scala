package db

import java.io.{File, FileInputStream, FileOutputStream, InputStream, OutputStream}
import java.nio.file.{Path => NioPath}

import cats.effect.{Blocker, ContextShift, Effect, Sync}
import fs2.io._
import fs2.{Pipe, Stream}
import models.{Word, WordOccurrence}
import os.{Path => OsPath}
import typeclasses.Byteable

import scala.util.Try

trait FileStorage[F[_]] {
  def store(fileName: String): Pipe[F, WordOccurrence, Unit]
  def retrieve(fileName: String): Stream[F, WordOccurrence]
}

object FileStorage {
  def apply[F[_]: ContextShift: Effect](basePath: NioPath, readSize: Int): FileStorage[F] =
    new FileStorageImpl[F](OsPath(basePath), readSize)

  def outputWriter(file: File): Try[OutputStream] =
    Try { new FileOutputStream(file) }

  def inputReader(file: File): Try[InputStream] =
    Try { new FileInputStream(file) }

  def byteStreamToOutputStream[F[_]: Sync: ContextShift](fOS: F[OutputStream]): Pipe[F, Byte, Unit] =
    byteStream => Stream.resource(Blocker[F]).flatMap { blocker =>
      byteStream.through(writeOutputStream(fOS, blocker))
    }

  def inputStreamToStreamBytes[F[_]: Sync: ContextShift](fIS: F[InputStream], chunkSize: Int): Stream[F, Byte] =
    Stream.resource(Blocker[F]).flatMap { blocker =>
      readInputStream(fIS, chunkSize, blocker, closeAfterUse = true)
    }

  def toWordOccurrence[F[_]: Effect]: Pipe[F, Byte, WordOccurrence] =
    _.split(_ == Word.separator).evalMap { str =>
      Byteable[WordOccurrence].toT(str.toVector) match {
        case Some(wo) => Effect[F].pure(wo)
        case _ =>
          val exception = new IllegalStateException("Read data should not contain a space separated string")
          Effect[F].raiseError[WordOccurrence](exception)
      }
    }

  def toByte[F[_]]: Pipe[F, WordOccurrence, Byte] = _.flatMap { wo =>
    val value = Byteable[WordOccurrence].toBytes(wo).appended(Word.separator)
    Stream.emits(value)
  }
}

class FileStorageImpl[F[_]: ContextShift: Effect](basePath: OsPath, readChunkSize: Int) extends FileStorage[F] {
  import FileStorage._

  def store(fileName: String): Pipe[F, WordOccurrence, Unit] = { stream =>
    val file = (basePath / fileName).toIO
    val fOS = Effect[F].fromTry(outputWriter(file))

    stream.through(toByte[F])
      .through(byteStreamToOutputStream(fOS))
  }

  def retrieve(fileName: String): Stream[F, WordOccurrence] = {
    val file = (basePath / fileName).toIO
    val fIS = Effect[F].fromTry(inputReader(file))

    inputStreamToStreamBytes(fIS, readChunkSize)
      .through(toWordOccurrence[F])
  }
}


