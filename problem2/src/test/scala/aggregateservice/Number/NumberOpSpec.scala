package aggregateservice.Number

import aggregateservice.Number.TypeHolder.LongType
import cats.data.NonEmptyList
import org.scalatest.{Matchers, WordSpec}

class NumberOpSpec extends WordSpec with Matchers {
  val sample = NonEmptyList.fromListUnsafe(List[Int](2,3,5,4,5)).map(LongNumber.fromInt)

  "sum" should {
    "add all values inside a non empty list" in {
      NumberOp.sum(LongType.impl)(sample).toString shouldEqual "19"
    }
  }

  "max" should {
    "maximum value in the list" in {
      NumberOp.max(LongType.impl)(sample).toString shouldEqual "5"
    }
  }

  "max" should {
    "mean value in the list" in {
      NumberOp.mean(LongType.impl)(sample).toString shouldEqual "3"
    }
  }
}
