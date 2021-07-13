package http

import cats.effect.IO
import org.http4s._
import org.http4s.dsl.Http4sDsl
import cats.implicits._
import logics.IProcessLogic
import io.circe.Decoder
import io.circe.syntax.EncoderOps
import models.{IoTRecord, ProcessedData, ReadingsQuery}
import org.http4s.server.Router
import org.http4s.implicits._
import org.http4s.circe._

case object LimitError extends Exception("Requested query records requires too much computation")
case object TimeRangeError extends Exception("Invalid time range")
case object NoOpPassed extends Exception("No Operations passed")

final class EndPoints private(ctrl: IProcessLogic)
  extends Http4sDsl[IO] {

  def routes: HttpRoutes[IO] = readiness <+> iotEndPoints

  def readiness: HttpRoutes[IO] = HttpRoutes.of {
    case GET -> Root / "status" => Ok()
  }

  implicit val iotEncoder: EntityEncoder[IO, IoTRecord] =
    jsonEncoderOf[IO, IoTRecord](Encoders.ioTRecord2Json)

  implicit val ProcessedDataEncoder: EntityEncoder[IO, List[ProcessedData]] = {
    implicit val _1 = Encoders.processedData2Json
    jsonEncoderOf[IO, List[ProcessedData]]
  }

  implicit val iotDecoder: EntityDecoder[IO, IoTRecord] = {
    implicit val decoder: Decoder[IoTRecord] = Decoders.json2IoTRecord
    jsonOf[IO, IoTRecord]
  }

  implicit val readingsQueryDecoder: EntityDecoder[IO, ReadingsQuery] = {
    implicit val decoder: Decoder[ReadingsQuery] = Decoders.json2ReadingsQuery
    jsonOf[IO, ReadingsQuery]
  }

  def badRequest(ex: Throwable): IO[Response[IO]] =
    BadRequest.apply(Seq("Error" -> ex.getMessage).asJson)

  def validateQuery: ReadingsQuery => IO[Unit] = {
    case ReadingsQuery(from, to, sensors, ops) =>
      IO.raiseWhen(from.isAfter(to))(TimeRangeError) *>
        IO.raiseWhen(ops.isEmpty || sensors.isEmpty)(NoOpPassed)
  }

  def iotEndPoints: HttpRoutes[IO] = HttpRoutes.of {
    case req @ POST -> Root / "devices" =>
      req.as[IoTRecord].flatMap(ctrl.create).attempt.flatMap {
        case Right(_) => Created()
        case Left(ex) => IO.println(ex.toString) *> badRequest(ex)
      }

    case req @ GET -> Root / "devices" / "stats" =>
      req.as[ReadingsQuery]
        .flatMap(d => validateQuery(d).as(d))
        .flatMap(ctrl.retrieve).attempt.flatMap {
          case Right(r) => Ok(r.sortBy(_.name.fieldName))
          case Left(ex) => IO.println(ex.toString) *> badRequest(ex)
        }
  }
}

object EndPoints {
  def build(ctrl: IProcessLogic): HttpApp[IO] =
    Router[IO]("/" -> new EndPoints(ctrl).routes).orNotFound
}
