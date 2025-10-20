package com.kaizo

import cats.effect.*
import io.circe.*
import org.http4s.*
import org.http4s.circe.jsonOf
import io.circe.generic.auto._

import java.time.{Instant, OffsetDateTime, ZoneId, ZoneOffset}

case class ZendeskTicket(
  url: String,
  id: Long,
  createdAt: OffsetDateTime,
  updatedAt: Option[OffsetDateTime],
)

final case class ApiResponse(
  tickets: List[ZendeskTicket],
  endTime: Option[OffsetDateTime]
)

type Token = String

final case class Customer(
  id: Int,
  domain: String,
  token: Token,
  queueAt: OffsetDateTime,
  startFrom: Option[OffsetDateTime]
)

object Codecs {
  private def toOffsetDateTime(seconds: Long): OffsetDateTime =
    OffsetDateTime.ofInstant(Instant.ofEpochSecond(seconds), ZoneOffset.UTC)

  implicit val decoderZendeskTicket: Decoder[ZendeskTicket] = Decoder.instance { c =>
    for
      url <- c.downField("url").as[String]
      id <- c.downField("id").as[Long]
      createdAt <- c.downField("created_at").as[OffsetDateTime]
      updatedAt <- c.downField("updated_at").as[Option[OffsetDateTime]]
    yield ZendeskTicket(url, id, createdAt, updatedAt)
  }

  implicit val decoderApiResponse: Decoder[ApiResponse] = Decoder.instance { c =>
    for
      tickets <- c.downField("tickets").as[List[ZendeskTicket]]
      endTime <- c.downField("end_time").as[Option[Long]].map(_.map(toOffsetDateTime))
    yield ApiResponse(tickets, endTime)
  }

  implicit val customerEntityDecoder: EntityDecoder[IO, Customer] = jsonOf[IO, Customer]
  implicit def apiResponseEntityDecoder[F[_]: Concurrent]: EntityDecoder[F, ApiResponse] = jsonOf[F, ApiResponse]
}