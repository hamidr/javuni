package http

import cats.effect.IO
import logic.StreamLogic
import models.{DigestedText, Word}
import org.http4s.{Method, Request, Status, Uri}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import fs2.Stream
import org.http4s.multipart.{Multipart, Part}

class EndPointsSpec extends AnyFlatSpec with Matchers  {
  val data = Stream.emits[IO, Byte](Seq(
    'h', 'e', 'y', ' ',
    't', 'o', 'o', ' ',
    'm', 'u', 'c', 'h', ' ',
    'c', 'o', 'd','i', 'n', 'g', ' ',
    'f', 'o', 'r', ' ',
    'a', 'n', ' ',
    'a','s','s','i', 'g', 'n', 'm', 'e', 'n', 't')
  )

  var db = Map.empty[String, Seq[Byte]]

  val logic: StreamLogic[IO] = new StreamLogic[IO] {
    def saveStream(fileName: String, data: fs2.Stream[IO, Byte]): IO[Unit] = IO.delay {
      val bytes = data.compile.toList.unsafeRunSync()
      db = db.updatedWith(fileName)(_ => Some(bytes))
    }

    def getStream(fileName: String): fs2.Stream[IO, DigestedText] =
      Stream.emit(DigestedText.fromMap(Map(Word.fromString("hello").get -> 1, Word.fromString("hi").get -> 2)))
  }

  val httpLayer = EndPoints.build[IO](logic)

  val parts = Multipart(Vector(Part.fileData("stream-file", "file1", data)))

  it should "Serve an endpoint for processing a stream and saving it" in {
    val streamRequest = Request[IO](
      method = Method.POST,
      uri = Uri.unsafeFromString("/stream"),
      headers = parts.headers
    ).withEntity(parts)

    val resp = httpLayer.run(streamRequest).unsafeRunSync()

    db("file1") shouldBe data.compile.toList.unsafeRunSync()
    db.get("file2") shouldBe None
    resp.status shouldBe Status.Created

    val resp2 = httpLayer.run(
      Request[IO](
      method = Method.GET,
      uri = Uri.unsafeFromString("/stream/whatever-file")
    )).unsafeRunSync()

    resp2.as[String].unsafeRunSync() shouldBe """{"wordOccurrences":{"hello":1,"hi":2},"uniqueWordCount":2,"textSummary":["hello","hi"]}"""
  }
}
