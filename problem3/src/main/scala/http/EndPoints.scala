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
      Ok(":)")
  }

  def healthCheck: HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root / "healthcheck" =>
      Ok(":))")
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
