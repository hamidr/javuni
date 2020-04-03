package com.newmotion.handlers

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives.{pathPrefix, post}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import com.newmotion.Utils._
import com.newmotion.actors.SessionRequestActor
import com.newmotion.models.Session
import com.newmotion.repositories.{SessionFeeRepository, TariffRepository}

import scala.concurrent.ExecutionContext

class SessionHandler(
  sessionFeeRepository: SessionFeeRepository,
  tariffRepository: TariffRepository
)(override implicit val actorSystem: ActorSystem,
  override implicit val executionContext: ExecutionContext,
  override implicit val materializer: Materializer) extends Handler {

  import com.newmotion.Utils.formats

  def createActor(imperativeRequestContext: ImperativeRequestContext): ActorRef =
    actorSystem.actorOf(SessionRequestActor.props(imperativeRequestContext, sessionFeeRepository, tariffRepository))

  def handlePostRequest(ctx: ImperativeRequestContext, session: Session): Unit =
    createActor(ctx) ! InputRequest(SessionRequestActor.SetSession(session))


  def handleGetRequest(ctx: ImperativeRequestContext, id: String): Unit =
    createActor(ctx) ! InputRequest(SessionRequestActor.Invoice(id))

  def routes: Route = pathPrefix("sessions") {
    post {
      entity(as[Session]) { data =>
        imperativelyComplete(handlePostRequest(_, data))
      }
    } ~ get {
      path(Segment) { id =>
          imperativelyComplete(handleGetRequest(_, id))
      }
    }
  }
}
