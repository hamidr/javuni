package com.newmotion

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration.Duration
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{RejectionHandler, Route}
import akka.stream.ActorMaterializer
import com.newmotion.handlers.{SessionHandler, TariffHandler}
import akka.http.scaladsl.server.Directives._
import com.github.nscala_time.time.Imports.DateTimeZone
import com.newmotion.Server.{sessionFeeRepository, tariffRepository}
import com.newmotion.repositories.{SessionFeeRepository, TariffRepository}

import scala.util.control.NonFatal

trait RestService {
  implicit val system: ActorSystem = ActorSystem("newmotion-server")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = materializer.executionContext

  def sessionFeeRepository: SessionFeeRepository
  def tariffRepository: TariffRepository

  lazy val sessionHandler: SessionHandler = new SessionHandler(sessionFeeRepository, tariffRepository)
  lazy val tariffHandler: TariffHandler = new TariffHandler(tariffRepository)

  implicit def myRejectionHandler: RejectionHandler =
    RejectionHandler.newBuilder()
      .handle {
        case NonFatal(throwable) =>
          this.system.log.error(s"Error: ${throwable.getMessage}")
          complete((StatusCodes.InternalServerError, "Error!"))
      }
      .handleNotFound {
        extractUnmatchedPath { p =>
          complete((StatusCodes.NotFound, s"The path you requested [${p}] does not exist."))
        }
      }
      .result()

  def routes: Route =
    sessionHandler.routes ~
      tariffHandler.routes

  def shutdown = system.terminate()
}

object Server extends App with RestService {

  val sessionFeeRepository: SessionFeeRepository = new SessionFeeRepository()
  val tariffRepository: TariffRepository = new TariffRepository()

  Http().bindAndHandle(routes, "localhost", 8080)
  println(s"Server online at http://localhost:8080/")
  Await.result(system.whenTerminated, Duration.Inf)
}
