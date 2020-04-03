import models._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import typeclasses.Byteable


class ModelsSpec extends AnyFlatSpec with Matchers {
  val word = Word.fromString("hello").get
  val wo = WordOccurrence(word, 100)
  val bytes = Vector('h', 'e', 'l','l', 'o').map(_.toByte)

  "Word" should "be able to convert from bytes and to itself" in {
    Byteable[Word].toBytes(word) shouldBe bytes
    Byteable[Word].toT(bytes).get.value shouldBe "hello"
  }

  it should "can't be created from unknown chars" in {
    Word.fromString("سلام").isDefined shouldBe true
    Word.fromString(" ").isEmpty shouldBe true
    Word.fromString("1").isEmpty shouldBe true
    Word.fromString("hey").isDefined shouldBe true
    Word.fromString("1 @$ 323 ۱").isEmpty shouldBe true
  }

  "WordOccurrence" should "be able to convert from bytes and to itself" in {
    val bytes2 = "hello,100".toVector.map(_.toByte)
    Byteable[WordOccurrence].toBytes(wo) shouldBe bytes2
    val newWO = Byteable[WordOccurrence].toT(bytes2).get
    newWO.occurrences shouldBe 100
    newWO.word.value shouldBe "hello"
  }
}
