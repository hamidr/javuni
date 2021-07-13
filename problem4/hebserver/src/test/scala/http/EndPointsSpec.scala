package http

import cats.effect.IO
import logics.ProcessLogic
import models._
import org.http4s._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import repositories.InMemoryRepo
import cats.effect.unsafe.implicits.global
import org.http4s.Status.{Created, Ok}
import cats.implicits._

import java.time.ZonedDateTime

class EndPointsSpec extends AnyFlatSpec with Matchers  {
  val queryable = ZonedDateTime.now()
  val from = queryable.minusDays(1)
  val to = queryable.plusDays(1)
  val notQueryable = ZonedDateTime.now().plusDays(10)

  val dataSample = Seq(
    IoTRecord("3232421", 2.332, 10.32, 32.33, queryable.toInstant),
    IoTRecord("3232422", 32.332, 2.32, 31.33, queryable.toInstant),
    IoTRecord("3232423", 42.332, 5.32, 32.33, queryable.toInstant),
    IoTRecord("3232424", 62.332, 63.32, 2.33, queryable.toInstant),

    IoTRecord("32324222", 32.332, 23.32, 2.33, notQueryable.toInstant),
    IoTRecord("32324212", 52.332, 43.32, 32.33, notQueryable.toInstant),
    IoTRecord("32324234", 22.332, 63.32, 25.33, notQueryable.toInstant),
    IoTRecord("32324255", 22.132, 13.32, 6.33, notQueryable.toInstant)
  )

  val repository = InMemoryRepo.build
  val storing = dataSample.map(repository.store).sequence
  storing.unsafeRunSync()

  val logic = ProcessLogic.create(1000, repository)
  val endPoints = EndPoints.build(logic)

  val statsUrl = Uri.unsafeFromString("/devices/stats")

  it should "Create a RECORD in DB peacefully" in {
    val value = s"""{"id":"67yuht7yu67u","thermostat":1.0,"heartRate":2.0,"carFuel":3.0,"time":"${notQueryable.toString}"}"""
    val postRequest = Request[IO](method = Method.POST, uri = Uri.unsafeFromString("/devices")).withEntity(value)
    endPoints.run(postRequest).unsafeRunSync().status shouldBe Created
  }

  it should "Return MAX and MIN result of requested data without sensors" in {
    val getData =
      s"""{
         "from":"${from.toString}",
         "to":"${to.toString}",
         "operations":["max", "min"],
         "sensors":["carfuel", "heartRate", "thermostat"]
        }""".stripMargin

    val postRequest = Request[IO](method = Method.GET, uri = statsUrl).withEntity(getData)
    val res = endPoints.run(postRequest).unsafeRunSync()
    val body = res.as[String].unsafeRunSync()
    body shouldBe """[{"name":"carFuel","min":"2.330","max":"32.330"},{"name":"heartRate","min":"2.320","max":"63.320"},{"name":"thermostat","min":"2.332","max":"62.332"}]"""
    res.status shouldBe Ok
  }

  it should "Return MAX and MIN result of requested data with sensors" in {
    val getData =
      s"""{
         "from":"${from.toString}",
         "to":"${to.toString}",
         "operations":["max", "min"],
         "sensors":["carfuel", "thermostat", "heartRate"]
        }""".stripMargin

    val postRequest = Request[IO](method = Method.GET, uri = statsUrl).withEntity(getData)
    val res = endPoints.run(postRequest).unsafeRunSync()
    val body = res.as[String].unsafeRunSync()
    body shouldBe """[{"name":"carFuel","min":"2.330","max":"32.330"},{"name":"heartRate","min":"2.320","max":"63.320"},{"name":"thermostat","min":"2.332","max":"62.332"}]"""
    res.status shouldBe Ok
  }

  it should "Return AVERAGE result of requested data" in {
    val getData =
      s"""{
         "from":"${from.toString}",
         "to":"${to.toString}",
         "operations":["average"],
         "sensors":["carfuel", "thermostat", "heartRate"]
        }""".stripMargin

    val postRequest = Request[IO](method = Method.GET, uri = statsUrl).withEntity(getData)
    val res = endPoints.run(postRequest).unsafeRunSync()
    val body = res.as[String].unsafeRunSync()
    body shouldBe """[{"name":"carFuel","average":"24.580"},{"name":"heartRate","average":"20.320"},{"name":"thermostat","average":"34.832"}]"""
    res.status shouldBe Ok
  }

  it should "Return MEDIAN result of requested data" in {
    val getData =
      s"""{
         "from":"${from.toString}",
         "to":"${to.toString}",
         "operations":["MEDIAN"],
         "sensors":["carfuel", "thermostat", "heartRate"]
        }""".stripMargin

    val postRequest = Request[IO](method = Method.GET, uri = statsUrl).withEntity(getData)
    val res = endPoints.run(postRequest).unsafeRunSync()
    val body = res.as[String].unsafeRunSync()
    body shouldBe """[{"name":"carFuel","median":"31.830"},{"name":"heartRate","median":"3.820"},{"name":"thermostat","median":"37.332"}]"""
    res.status shouldBe Ok
  }
}
