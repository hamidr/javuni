package aggregateservice.Number

import org.scalatest.{Matchers, WordSpec}

class BigDecimalNumberSpec extends WordSpec with Matchers {

  val sample0 = BigDecimalNumber.zero
  val sample1 = BigDecimalNumber.point(BigDecimal.valueOf(1))
  val sample2 = BigDecimalNumber.point(BigDecimal.valueOf(2))

  "BigDecimalNumber instance functions" should {
    "zero; Zero instance" in {
      BigDecimalNumber.zero.toString shouldBe "0"
    }

    "point; convert a BigDecimal value to an Instance Number" in {
      BigDecimalNumber.point(BigDecimal.valueOf(101)).toString shouldBe BigDecimal.valueOf(101).toString
    }

    "plus; Add two BigDecimal instances" in {
      BigDecimalNumber.plus(sample1, sample2).toString shouldBe "3"
    }

    "gt: Bigger number between two instances" in {
      val n1 = BigDecimalNumber.gt(sample1, sample2)
      val n2 = BigDecimalNumber.gt(sample2, sample1)
      n1 shouldEqual n2
      n1.toString shouldEqual "2"
    }

    "div: Division of two instance numbers:" in {
      BigDecimalNumber.div(sample1, sample2).map(_.toString) shouldEqual Some("0.5")
      BigDecimalNumber.div(sample2, sample1).map(_.toString) shouldEqual Some("2")
      BigDecimalNumber.div(sample2, sample0) shouldEqual None
    }

    "fromInt: int to Instance number:" in {
      BigDecimalNumber.fromInt(3).toString shouldBe "3"
    }

    "fromStr: String to Instance number:" in {
      BigDecimalNumber.fromStr("3.14").map(_.toString) shouldBe Some("3.14")
      BigDecimalNumber.fromStr("x3.14") shouldBe None
    }
  }
}
