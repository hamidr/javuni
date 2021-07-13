import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.util.Try
import scala.util.Random
import cats.effect.kernel.Resource
import cats.effect.{ExitCode, IO, IOApp}
import org.http4s.{Method, Request, Response, Uri}
import fs2.{INothing, Pipe, Stream}
import io.circe.{Encoder, Json}
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.client.Client
import org.http4s.Status.Successful
import org.http4s.circe.jsonEncoderOf
import io.circe.syntax._
import cats.implicits._

import java.time.{Clock, ZonedDateTime}

opaque type DeviceId = String
object DeviceId:
  def generate: IO[DeviceId] =
    IO.delay(java.util.UUID.randomUUID().toString)

  extension (id: DeviceId)
    def extract: String = id


final case class IoTData private(
  id: DeviceId,
  thermostat: Double,
  heartRate: Double,
  carFuel: Double,
  time: ZonedDateTime
)

object IoTData:
  val rangeFrom = 0.0
  val rangeTo = 100.0

  def generate: DeviceId => IO[IoTData] = id =>
    val randomIO: IO[Double] = IO.delay(Random.between(rangeFrom, rangeTo))
    val now = IO.delay(ZonedDateTime.now(Clock.systemUTC()))
    for
      thermostat <- randomIO
      heartRate <- randomIO
      carFuel <- randomIO
      time <- now
    yield IoTData(id, thermostat, heartRate, carFuel, time)

object Encoders:
  val IoTData2Json: Encoder[IoTData] = Encoder.instance { data =>
    Json.obj(
      "id" -> data.id.extract.asJson,
      "thermostat" -> data.thermostat.asJson,
      "heartRate" -> data.heartRate.asJson,
      "carFuel" -> data.carFuel.asJson,
      "time" -> data.time.asJson
    )
  }

object ErrorHelpers:
  extension [T](lr: Either[Throwable, T])
    def enrichError(str: String): Either[Throwable, T] =
      lr.leftMap(err => InvalidArgs(s"$str ${err.getMessage}"))

final case class InvalidArgs(str: String)
  extends Exception(s"Invalid arguments. \nReason: $str\nCorrect order: TOTAL_DEVICES API_ADDRESS DELAY_ON_EACH_API_CALL HOW_LONG")

object Main extends IOApp {
  def buildClient: Resource[IO, Client[IO]] =
    BlazeClientBuilder[IO](ExecutionContext.global).resource

  def generateIoTData(id: DeviceId): Stream[IO, IoTData] =
    Stream.repeatEval(IoTData.generate(id))

  def IoTData2Request(apiAddress: Uri): Pipe[IO, IoTData, Request[IO]] =
    _.map { data =>
      Request(method = Method.POST, uri = apiAddress)
        .withEntity(data)(jsonEncoderOf[IO, IoTData](Encoders.IoTData2Json))
    }

  def printId(data: IoTData): IO[Unit] =
    IO.println(s"${data.id.toString} ")

  def checkRequestErrors: Pipe[IO, Either[Throwable, Response[IO]], Unit] = _.evalMap {
    case Right(Successful(_)) => IO.print("(S) ")
    case Right(r) => IO.println(s"API call failed with ${r.status}")
    case Left(ex) => IO.println(s"Error ${ex.getMessage}")
  }

  def debugStream[A]: Pipe[IO, A, A] =
    _.evalTap( x => IO.println(s"DEBUG: $x"))

  def throttle[A](delay: FiniteDuration): Pipe[IO, A, A] =
    Stream.awakeEvery[IO](delay).zipRight

  def emitFlow(apiAddress: Uri): Pipe[IO, IoTData, Unit] = dataStream =>
    Stream.resource(buildClient).flatMap { client =>
      dataStream
        .through(_.evalTap(printId))
        .through(IoTData2Request(apiAddress))
        .flatMap(client.stream(_).attempt)
        .through(checkRequestErrors)
    }

  def generateDeviceIds: Stream[IO, DeviceId] =
    Stream.repeatEval(DeviceId.generate)

  def validateArgs: List[String] => Either[Throwable, (Long, Uri, FiniteDuration, FiniteDuration)] =
    case c :: a :: Duration((d1,d2)) :: Duration(t1,t2) :: Nil =>
      import ErrorHelpers._
      for
        clientNumber <- Try {c.toLong}.toEither.enrichError("On TOTAL_DEVICES with")
        apiAddress <- Uri.fromString(a).enrichError("On API_ADDRESS with")
        delay <- Try { FiniteDuration(d1, d2) }.toEither.enrichError("On DELAY_ON_EACH_API_CALL with")
        timeout <- Try { FiniteDuration(t1, t2) }.toEither.enrichError("On HOW_LONG with")
      yield (clientNumber, apiAddress, delay, timeout)

    case _ => Left(InvalidArgs("Wrong number/typed of arguments were passed"))

  override def run(args: List[String]): IO[ExitCode] =
    IO.fromEither(validateArgs(args)).flatMap {
      case (simulationNumber, apiAddress, delayUnit, forTimeout) =>
        val flow: Pipe[IO, IoTData, INothing] =
        _.through(throttle(delayUnit))
          .through(emitFlow(apiAddress))
          .attempt
          .drain

        IO.println(s"Simulating $simulationNumber devices, using $apiAddress for URL with delay of $delayUnit for $forTimeout long.") *>
          generateDeviceIds
            .take(simulationNumber)
            .map(generateIoTData)
            .map(_.through(flow))
            .parJoinUnbounded
            .interruptAfter(forTimeout)
            .compile
            .drain
            .as(ExitCode.Success)
      }
      .onError(ex =>
        IO.println(ex.getMessage))
  end run
}