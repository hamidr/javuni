package com.kaizo

import cats.effect.{IO, IOApp}
import cats.implicits.*
import com.comcast.ip4s.*
import fs2.{Pipe, Stream}
import org.http4s.*
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder

object App extends IOApp.Simple:
  private val pooledHttpClient = EmberClientBuilder.default[IO].build

  private def buildServer(dbTable: ICustomerRepo): IO[Unit] =
    val httpApp = HttpServer(dbTable).routes
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0") //TODO: Config?
      .withPort(port"8080")    //TODO: Config?
      .withHttpApp(httpApp)
      .build
      .use(_ => IO.never)
  end buildServer

  private def runStream: Stream[IO, Unit] =
    for {
      db <- Stream.eval(IO.delay(DB.xa))
      cRepo <- Stream.eval(CustomerRepo.init(db))
      errRepo <- Stream.eval(ApiLogger.init(db))
      server = Stream.eval(buildServer(cRepo))
      httpClient <- Stream.resource(pooledHttpClient)
      zendeskApi = ZendeskClient(httpClient)
      polling = PollingEngine(zendeskApi, cRepo, errRepo)
      process = polling.tickets
        .through(sink)
      _ <- process.mergeHaltL(server)
    } yield ()
  end runStream

  override def run: IO[Unit] =
    runStream.compile.drain

  private def sink: Pipe[IO, ZendeskTicket, Unit] = _.evalMap { tk =>
    val domain = Uri.unsafeFromString(tk.url).host.get // TODO: Can be fixed! it's not worth the time.
    IO.println(s"domain=${domain} id=${tk.id} createdAt=${tk.createdAt} updatedAt=${tk.updatedAt}")
  }
end App
