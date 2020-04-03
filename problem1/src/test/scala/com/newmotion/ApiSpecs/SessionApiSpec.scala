package com.newmotion.ApiSpecs

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.newmotion.RestService
import org.scalatest.{Matchers, WordSpec}
import com.github.nscala_time.time.Imports._
import com.newmotion.models.{Currency, Session, SessionFee, Tariff}
import com.newmotion.repositories.{SessionFeeRepository, TariffRepository}
import org.json4s.jackson.JsonMethods
import org.json4s.native.Serialization
import org.scalatest.concurrent.ScalaFutures._

class SessionApiSpec extends WordSpec with Matchers with ScalatestRouteTest {
  import com.newmotion.Utils.formats
  import com.newmotion.TestUtils._

  val sessionData = Session(
    customerId = "user1",
    startTime = DateTime.lastHour.fixMe,
    endTime =  DateTime.now.fixMe,
    volume = 14.12
  )

  val tariff1 = Tariff(
    currency = Currency.EUR,
    startFee = Some(0.0),
    hourlyFee = Some(3.1),
    feePerKWh = Some(0.1),
    activeStarting = DateTime.lastHour.minusMinutes(10).fixMe
  )

  val tariff2 = Tariff(
    currency = Currency.EUR,
    startFee = Some(1.0),
    hourlyFee = Some(3.2),
    feePerKWh = Some(3.3),
    activeStarting = DateTime.now.minusDays(1).fixMe
  )

  val tariff3 = Tariff(
    currency = Currency.EUR,
    startFee = Some(2.1),
    hourlyFee = Some(8.1),
    feePerKWh = Some(17.3),
    activeStarting = DateTime.now.minusDays(2).fixMe
  )

  def mockService(
    tariffRepo: TariffRepository = new TariffRepository(),
    sessionRepo: SessionFeeRepository = new SessionFeeRepository()
  ) = MockRestService(tariffRepo, sessionRepo)

  val service = mockService()

  override def afterAll(): Unit = {
    service.shutdown
    super.afterAll()
  }

  "POST /sessions" should {
    "get rejected when there is no tariff" in {
      val postData = Serialization.write(sessionData)

      val httpEntity = HttpEntity(ContentTypes.`application/json`, postData)
      Post("/sessions", httpEntity) ~> service.routes ~> check {
        status shouldEqual StatusCodes.Conflict
      }
    }

    "set and compute the session fees according to its effective tariff" in {
      val postData = Serialization.write(sessionData)
      service.tariffRepository.create(tariff1).futureValue

      val httpEntity = HttpEntity(ContentTypes.`application/json`, postData)
      Post("/sessions", httpEntity) ~> service.routes ~> check {
        status shouldEqual StatusCodes.Created
        val data = (JsonMethods.parse(responseAs[String]) \ "data").extract[SessionFee]
        data.session shouldEqual sessionData
        data.tariff shouldEqual tariff1
        data.fee shouldEqual 4.5120000000000005
      }
    }
  }

  "GET  /sessions/$custormer_id" should {
    "return notfound on nonexistence user" in {
      val httpEntity = HttpEntity(ContentTypes.`application/json`, "")
      Get("/sessions/iamnotauser", httpEntity) ~> service.routes ~> check {
        status shouldEqual StatusCodes.NotFound
      }
    }

    "return the sessions of the requested user" in {
      service.tariffRepository.create(tariff2).futureValue
      service.tariffRepository.create(tariff3).futureValue

      val sessions = List (
        Session(
          customerId = "user2",
          startTime = DateTime.now.minusHours(1).fixMe,
          endTime =  DateTime.now.fixMe,
          volume = 14.12
        ), Session(
          customerId = "user2",
          startTime = DateTime.now.minusHours(1).fixMe,
          endTime =  DateTime.now.minusMinutes(30).fixMe,
          volume = 8.1
        ), Session(
          customerId = "user2",
          startTime = DateTime.now.minusHours(10).fixMe,
          endTime =  DateTime.now.minusHours(3).fixMe,
          volume = 128
        ), Session(
          customerId = "user2",
          startTime = DateTime.now.minusDays(2).plusHours(2).fixMe,
          endTime =  DateTime.now.minusDays(2).plusHours(12).fixMe,
          volume = 140
        )
      )

      for (s <- sessions) {
        val data = Serialization.write(s)
        val ent = HttpEntity(ContentTypes.`application/json`, data)
        Post("/sessions", ent) ~> service.routes ~> check {
          status shouldEqual StatusCodes.Created
        }
      }

      val httpEntity = HttpEntity(ContentTypes.`application/json`, "")
      Get("/sessions/user2", httpEntity) ~> service.routes ~> check {
        status shouldEqual StatusCodes.OK
        val data = (JsonMethods.parse(responseAs[String]) \ "data").extract[List[SessionFee]]

        val total = data.foldLeft(0.0) { (acc, el) =>
          acc + el.fee
        }

        total shouldEqual 2957.772
      }
    }
  }


}
