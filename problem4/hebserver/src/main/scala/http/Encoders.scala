package http

import io.circe.syntax.EncoderOps
import io.circe.{Encoder, Json}
import models.{IoTRecord, ProcessedData}

object Encoders {
  val ioTRecord2Json: Encoder[IoTRecord] = Encoder.instance { data =>
    Json.obj(
      "id" -> data.id.asJson,
      "thermostat" -> data.thermostat.asJson,
      "heartRate" -> data.heartRate.asJson,
      "carFuel" -> data.carFuel.asJson,
      "time" -> data.time.asJson
    ).dropNullValues
  }

  def setPrecision(value: Double): String = f"$value%1.3f"

  val processedData2Json: Encoder[ProcessedData] = Encoder.instance { data =>
    Json.obj(
      "name" -> data.name.fieldName.asJson,
      "average" -> data.average.map(setPrecision).asJson,
      "median" -> data.median.map(setPrecision).asJson,
      "min" -> data.min.map(setPrecision).asJson,
      "max" -> data.max.map(setPrecision).asJson
    ).dropNullValues
  }
}
