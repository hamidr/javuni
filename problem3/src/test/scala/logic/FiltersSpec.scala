package logic

import cats.effect.IO
import models.Word
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import fs2._

import scala.concurrent.ExecutionContext

class FiltersSpec extends AnyFlatSpec with Matchers {
  val ec = ExecutionContext.global
  implicit val ctxShit = IO.contextShift(ec)

  val id: Filters.Process[IO] = _.map(identity)
  val process: Filters.Process[IO] = Filters[IO](Set("aggressive_word", "unknown_word")).getOrElse(id)

  val words = List("hello", "fuck", "unknown").map(Word.fromString).map(_.get)

  "aggressive_word" should "remove aggressive words from stream of words" in {
    Stream.emits(words).through(process).compile.toList.unsafeRunSync().map(_.value) shouldBe List("hello")
  }

}
