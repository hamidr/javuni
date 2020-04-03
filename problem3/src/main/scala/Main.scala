
import cats.effect.{ExitCode, IO, IOApp}
import db.FileStorage
import org.http4s.HttpApp
import org.http4s.server.blaze.BlazeServerBuilder
import fs2.Stream
import http.EndPoints
import logic.{Filters, StreamLogic}
import models.AppConfig
import cats.implicits._

final case class IllegalConfigRules(unknownRules: Seq[String]) extends
  IllegalStateException(s"Unknown filters: ${unknownRules}")

object Main extends IOApp {
  def server(ipAddress: String, port: Int)(routes: HttpApp[IO]): Stream[IO, ExitCode] =
    BlazeServerBuilder[IO]
      .bindHttp(port, ipAddress)
      .withHttpApp(routes)
      .serve

  override def run(args: List[String]): IO[ExitCode] = {
    val job = for {
      cfg <- Stream.eval(AppConfig.load[IO])
      storage = FileStorage[IO](cfg.dir, cfg.readChunkSize)
      filters <- Stream.fromEither[IO](Filters[IO](cfg.filteringRules).leftMap(IllegalConfigRules))
      logic = StreamLogic[IO](cfg.processChunkSize, storage, filters)
      routes = EndPoints.build[IO](logic)
      ec <- server(cfg.ipAddress, cfg.port)(routes)
    } yield ec

    job.compile.lastOrError
  }
}