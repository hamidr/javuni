package com.newmotion.ApiSpecs

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.github.nscala_time.time.Imports._
import com.newmotion.RestService
import com.newmotion.TestUtils._
import com.newmotion.models.{Currency, Tariff}
import com.newmotion.repositories.{SessionFeeRepository, TariffRepository}
import org.json4s.native.Serialization
import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.{BeforeAndAfter, Matchers, WordSpec}

class TariffApiSpec extends WordSpec with Matchers with ScalatestRouteTest with BeforeAndAfter {

  val repo = new TariffRepository()
  val sessionRepo = new SessionFeeRepository()
  val service = new MockRestService(repo, sessionRepo)

  override def afterAll() {
    service.shutdown
    super.afterAll()
  }

  import com.newmotion.Utils.formats

  val tariffData = Tariff(
    currency = Currency.EUR,
    startFee = Some(0.20),
    hourlyFee = Some(1.00),
    feePerKWh = Some(0.25),
    activeStarting = DateTime.nextMinute().fixMe
  )

  "POST /tariffs" should {
    "set the in effect Tariff" in {
      val postData = Serialization.write(tariffData)

      val httpEntity = HttpEntity(ContentTypes.`application/json`, postData)

      Post("/tariffs", httpEntity) ~> service.routes ~> check {
        status shouldEqual StatusCodes.Created
        val Some(tariff) = repo.getInEffectTariff(DateTime.nextHour()).futureValue
        tariff shouldEqual tariffData
      }
    }

    "return conflict if activeStarting is less than now" in {
      val postData = Serialization.write(tariffData.copy(activeStarting = DateTime.lastMinute()))

      val httpEntity = HttpEntity(ContentTypes.`application/json`, postData)
      Post("/tariffs", httpEntity) ~> service.routes ~> check {
        status shouldEqual StatusCodes.Conflict
      }
    }
  }
}
