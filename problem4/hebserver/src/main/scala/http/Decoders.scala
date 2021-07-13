package http

import io.circe.{Decoder, DecodingFailure}
import models._

import java.time.ZonedDateTime
import scala.math.Ordered.orderingToOrdered

object Decoders {
  val json2IoTRecord: Decoder[IoTRecord] = Decoder.instance { c =>
    for {
      id <- c.downField("id").as[String]
      thermostat <- c.downField("thermostat").as[Double]
      heartRate <- c.downField("heartRate").as[Double]
      carFuel <- c.downField("carFuel").as[Double]
      time <- c.downField("time").as[ZonedDateTime].map(_.toInstant)
    } yield IoTRecord(id, thermostat, heartRate, carFuel, time)
  }

  val json2ReadingsQuery: Decoder[ReadingsQuery] = Decoder.instance { c =>
    implicit val eOp = json2OpFunc
    implicit val eCat = json2DeviceType
    for {
      from <- c.downField("from").as[ZonedDateTime].map(_.toInstant)
      to <- c.downField("to").as[ZonedDateTime].map(_.toInstant)
      _ <- {
        if (from <= to) Right(())
        else Left(DecodingFailure("From is lesser than To", Nil))
      }
      operations <- c.downField("operations").as[Set[OpFunc]]
      sensors <- c.downField("sensors").as[Set[SensorType]]
    } yield ReadingsQuery(from, to, sensors, operations)
  }

  val json2OpFunc: Decoder[OpFunc] = Decoder.instance { c =>
    c.as[String].map(_.toLowerCase).flatMap {
      case "min" => Right(Min)
      case "max" => Right(Max)
      case "average" => Right(Average)
      case "median" => Right(Median)
      case _ => Left(DecodingFailure("Unknown operation", Nil))
    }
  }

  val json2DeviceType: Decoder[SensorType] = Decoder.instance { c =>
    c.as[String].map(_.toLowerCase).flatMap {
      case "thermostat" => Right(Thermostat)
      case "heartrate" => Right(HeartRate)
      case "carfuel" => Right(CarFuel)
      case _ => Left(DecodingFailure("Unknown category", Nil))
    }
  }
}