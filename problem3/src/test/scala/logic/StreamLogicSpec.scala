package logic

import cats.effect.IO
import db.FileStorage
import fs2.{Pipe, Stream}
import models.{Word, WordOccurrence}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext

class StreamLogicSpec extends AnyFlatSpec with Matchers {
  import StreamLogic._

  val bytes: Seq[Byte] = Seq('h', 'e', 'l','l', 'o', ' ', 'h', 'i' ,'1', ' ')
  val wordStream: Stream[IO, Word] = Stream.emits(bytes).through(toWord[IO]).repeat

  val ec = ExecutionContext.global
  implicit val ctxShit = IO.contextShift(ec)

  ".toWord" should "transform bytes to separated cleaned words" in {
    wordStream.take(2).compile.toList.unsafeRunSync().map(_.value) shouldBe List("hello", "hi")
  }

  ".wordsToWordOccurrences" should "transform words to counted occurrences" in {
    wordStream.take(100) // 100 = 2 words * 50
      .through(wordsToWordOccurrences[IO](1000))
      .compile.toList.unsafeRunSync() shouldBe
      List(WordOccurrence(Word.fromString("hello").get, 50), WordOccurrence(Word.fromString("hi").get, 50))
  }

  it should "partition words and count them" in {
    wordStream.take(100) // 100 = 2 words * 50
      .through(wordsToWordOccurrences[IO](50))
      .compile.toList.unsafeRunSync() shouldBe
      List(
        WordOccurrence(Word.fromString("hello").get, 25),
        WordOccurrence(Word.fromString("hi").get, 25),
        WordOccurrence(Word.fromString("hello").get, 25),
        WordOccurrence(Word.fromString("hi").get, 25),
      )
  }

  ".wordOccurrencesToDigestedText" should "summarize the processed WordOccurrences" in {
    val digestedText = wordStream.take(100) // 100 = 2 words * 50
      .through(wordsToWordOccurrences[IO](1000))
      .through(wordOccurrencesToDigestedText[IO])
      .compile.toList.unsafeRunSync().head

    digestedText.textSummary.map(_.value) shouldBe List("hello", "hi")
    digestedText.uniqueWordCount shouldBe 2
    digestedText.wordOccurrences shouldBe Map(Word.fromString("hello").get -> 50, Word.fromString("hi").get -> 50)
  }

  val mockStorage = new FileStorage[IO] {
    var data = List.empty[WordOccurrence]

    def store(fileName: String): Pipe[IO, WordOccurrence, Unit] = _.flatMap { e =>
      Stream.eval(IO {
        data = e :: data
      })
    }

    def retrieve(fileName: String): Stream[IO, WordOccurrence] =
      Stream.emits(data)
  }

  val logic = StreamLogic[IO](10, mockStorage, _.map(identity))

  "StreamLogic" should "be able to store and retrieve the same data" in {
    logic.saveStream("", Stream.emits(bytes)).unsafeRunSync()
    val dt = logic.getStream("").compile.toList.unsafeRunSync().head
    dt.textSummary.map(_.value).sorted shouldBe List("hello", "hi")
  }
}
