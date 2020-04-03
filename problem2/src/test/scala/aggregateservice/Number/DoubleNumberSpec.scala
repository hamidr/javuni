package aggregateservice.Number

import org.scalatest.{Matchers, WordSpec}

class DoubleNumberSpec extends WordSpec with Matchers {

  val sample0 = DoubleNumber.zero
  val sample1 = DoubleNumber.point(1)
  val sample2 = DoubleNumber.point(2)

  "DoubleNumber instance functions" should {
    "zero; Zero instance" in {
      DoubleNumber.zero.toString shouldBe "0.0"
    }

    "point; convert a Double value to an Instance Number" in {
      DoubleNumber.point(Double.MaxValue).toString shouldBe Double.MaxValue.toString
    }

    "plus; Add two double instances" in {
      DoubleNumber.plus(sample1, sample2).toString shouldBe "3.0"
    }

    "gt: Bigger number between two instances" in {
      val n1 = DoubleNumber.gt(sample1, sample2)
      val n2 = DoubleNumber.gt(sample2, sample1)
      n1 shouldEqual n2
      n1.toString shouldEqual "2.0"
    }

    "div: Division of two instance numbers:" in {
      DoubleNumber.div(sample1, sample2).map(_.toString) shouldEqual Some("0.5")
      DoubleNumber.div(sample2, sample1).map(_.toString) shouldEqual Some("2.0")
      DoubleNumber.div(sample2, sample0) shouldEqual None
    }

    "fromInt: int to Instance number:" in {
      DoubleNumber.fromInt(3).toString shouldBe "3.0"
    }

    "fromStr: String to Instance number:" in {
      DoubleNumber.fromStr("3.14").map(_.toString) shouldBe Some("3.14")
      DoubleNumber.fromStr("x3.14") shouldBe None
    }
  }
}
