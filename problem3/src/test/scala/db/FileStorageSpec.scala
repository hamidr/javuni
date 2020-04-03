package db

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import cats.effect.IO
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import fs2._
import models.{Word, WordOccurrence}

import scala.concurrent.ExecutionContext

class FileStorageSpec extends AnyFlatSpec with Matchers  {
  val ec = ExecutionContext.global
  implicit val ctxShit = IO.contextShift(ec)
  val bytes: Seq[Byte] = Seq('h', 'e', 'l','l', 'o', ' ', 'h', 'i' ,'1', ' ')

  import FileStorage._

  "byteStreamToOutputStream" should "Write Stream[F, Byte] to OutputStream" in {
    val output = new ByteArrayOutputStream()
    Stream.emits(bytes).through(byteStreamToOutputStream[IO](IO.pure(output))).compile.drain.unsafeRunSync()
    output.toByteArray shouldBe bytes.toArray
  }

  "inputStreamToStreamBytes" should "Read Stream[F, Byte] from InputStream" in {
    val input = new ByteArrayInputStream(bytes.toArray)
    val readData = inputStreamToStreamBytes[IO](IO.pure(input), 100).compile.toList.unsafeRunSync().toArray
    readData shouldBe bytes
  }

  "toByte and toWordOccurrence" should "Be interchangeable and the produced data should be compatible" in {
    val wo = WordOccurrence(Word.fromString("hello").get, 100)
    val data = Stream.emit(wo).through(toByte[IO]).compile.toList.unsafeRunSync()
    val woCopy = Stream.emits(data).through(toWordOccurrence[IO]).compile.toList.unsafeRunSync().head
    wo shouldBe woCopy
  }
}
