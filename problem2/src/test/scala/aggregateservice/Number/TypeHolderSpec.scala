package aggregateservice.Number

import aggregateservice.Number.TypeHolder.{BigDecimalType, DoubleType, LongType}
import cats.data.NonEmptyList
import org.scalatest.{Matchers, WordSpec}

class TypeHolderSpec extends WordSpec with Matchers {
  val sample = NonEmptyList.fromListUnsafe(List[Int](2,3,5,4,5)).map(LongNumber.fromInt)

  "identityType" should {
    "receive a string and return the specific type" in {
      TypeHolder.identifyType("double") shouldBe Some(DoubleType)
      TypeHolder.identifyType("long") shouldBe Some(LongType)
      TypeHolder.identifyType("bigdecimal") shouldBe Some(BigDecimalType)
      TypeHolder.identifyType("binary") shouldBe None
    }
  }

  "indentifyFunction" should {
    "receive an string and return a function" in {
      val max  = LongType.identifyFunction("max")
      val mean = LongType.identifyFunction("mean")
      val sum  = LongType.identifyFunction("sum")
      val none  = LongType.identifyFunction("none")

      max.isDefined shouldEqual true
      mean.isDefined shouldEqual true
      sum.isDefined shouldEqual true
      none.isDefined shouldEqual false
      max.map(_.apply(sample).toString) shouldEqual Some("5")
      mean.map(_.apply(sample).toString) shouldEqual Some("3")
      sum.map(_.apply(sample).toString) shouldEqual Some("19")
    }
  }

}
