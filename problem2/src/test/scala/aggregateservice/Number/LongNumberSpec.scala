package aggregateservice.Number

import org.scalatest.{Matchers, WordSpec}

class LongNumberSpec extends WordSpec with Matchers {

  val sample0 = LongNumber.zero
  val sample1 = LongNumber.point(1)
  val sample2 = LongNumber.point(2)

  "LongNumber instance functions" should {
    "zero; Zero instance" in {
      LongNumber.zero.toString shouldBe "0"
    }

    "point; convert a Long value to an Instance Number" in {
      LongNumber.point(Long.MaxValue).toString shouldBe Long.MaxValue.toString
    }

    "plus; Add two Long instances" in {
      LongNumber.plus(sample1, sample2).toString shouldBe "3"
    }

    "gt: Bigger number between two instances" in {
      val n1 = LongNumber.gt(sample1, sample2)
      val n2 = LongNumber.gt(sample2, sample1)
      n1 shouldEqual n2
      n1.toString shouldEqual "2"
    }

    "div: Division of two instance numbers:" in {
      LongNumber.div(sample1, sample2).map(_.toString) shouldEqual Some("0")
      LongNumber.div(sample2, sample1).map(_.toString) shouldEqual Some("2")
      LongNumber.div(sample2, sample0) shouldEqual None
    }

    "fromInt: int to Instance number" in {
      LongNumber.fromInt(3).toString shouldBe "3"
    }

    "fromStr: String to Instance number:" in {
      LongNumber.fromStr("314").map(_.toString) shouldBe Some("314")
      LongNumber.fromStr("3.14").map(_.toString) shouldBe None
      LongNumber.fromStr("x3.14") shouldBe None
    }
  }
}
