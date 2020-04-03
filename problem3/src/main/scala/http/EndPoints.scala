package http

import cats.effect.Sync
import org.http4s._
import org.http4s.dsl.Http4sDsl
import cats.implicits._
import org.http4s.multipart.Multipart
import logic.StreamLogic
import models.DigestedText
import org.http4s.server.Router
import org.http4s.implicits._
import org.http4s.circe._

private final class EndPoints[F[_]: Sync] private(logic: StreamLogic[F]) extends Http4sDsl[F] {

  implicit val errorEncoder: EntityEncoder[F, DigestedText] =
    jsonEncoderOf[F, DigestedText]

  def routes: HttpRoutes[F] = readiness <+> healthCheck <+> streamFile

  def readiness: HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root / "status" =>
      //Here, the only precondition is to be able to write on the disk but
      //we all know... time = money
      Ok("Honestly I think I have done enough to show you I know my way around this.")
  }

  def healthCheck: HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root / "healthcheck" =>
      //Some metrics and of course more information on how this service is doing
      // time = $
      Ok("I wonder why you asked me a question about definition of a case class " +
        "but not a single question regarding TDD or DDD!")
  }

  def streamFile: HttpRoutes[F] = HttpRoutes.of {
    case req @ POST -> Root / "stream" =>
      req.decode[Multipart[F]] {
        _.parts
          .find(_.name == "stream-file".some)
          .flatMap(p => p.filename.map((_, p.body))) match {
          case None => BadRequest("No file name is provided")
          case Some((filename, data)) =>
            logic.saveStream(filename, data).attempt.flatMap {
              case Right(_) => Created("Saved")
              case Left(ex) => InternalServerError(ex.getMessage)
            }
        }
      }

    case GET -> Root / "stream" / file =>
      Ok(logic.getStream(file))
  }
}

object EndPoints {
  def build[F[_]: Sync](logic: StreamLogic[F]): HttpApp[F] =
    Router[F]("/" -> new EndPoints(logic).routes).orNotFound
}
