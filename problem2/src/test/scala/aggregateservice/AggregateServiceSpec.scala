package aggregateservice

import org.scalatest.{Matchers, WordSpec}

class AggregateServiceSpec extends WordSpec with Matchers {
  "validateOnError" in {
    val some = Option(1)
    val none = Option.empty[Int]
    AggregateService.validateOnError("Hello world")(some) shouldEqual Right(1)
    AggregateService.validateOnError("Hello world")(none) shouldEqual Left("Hello world")
  }
}