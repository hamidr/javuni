
import cats.effect.{ExitCode, IO, IOApp}
import repositories.{CassandraRecordRepo, InMemoryRepo}
import org.http4s.HttpApp
import fs2.Stream
import http.EndPoints
import logics.ProcessLogic
import org.http4s.blaze.server.BlazeServerBuilder

import scala.concurrent.ExecutionContext

final case class IllegalConfigRules(unknownRules: Seq[String]) extends
  IllegalStateException(s"Unknown filters: ${unknownRules}")

object Main extends IOApp {
  def server(ipAddress: String, port: Int, routes: HttpApp[IO]): Stream[IO, ExitCode] =
    BlazeServerBuilder[IO](ExecutionContext.global)
      .bindHttp(port, ipAddress)
      .withHttpApp(routes)
      .serve

  override def run(args: List[String]): IO[ExitCode] = {
    val ret = for {
      cfg <- Stream.eval(AppConfig.load)
      repository <- cfg.cassandra match {
        case Some(db) => Stream.resource(CassandraRecordRepo.build(db))
        case None => Stream.emit(InMemoryRepo.build)
      }
      controller = ProcessLogic.create(cfg.processingLimit, repository)
      routes = EndPoints.build(controller)
      ec <- server(cfg.ipAddress, cfg.port, routes)
    } yield ec

    ret.compile.lastOrError
  }
}