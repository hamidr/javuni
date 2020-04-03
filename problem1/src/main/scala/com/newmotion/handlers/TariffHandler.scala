package com.newmotion.handlers

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import com.newmotion.Utils._
import com.newmotion.actors.TarifRequestActor
import com.newmotion.models.Tariff
import com.newmotion.repositories.TariffRepository

import scala.concurrent.ExecutionContext

class TariffHandler(
  tariffRepository: TariffRepository
)(override implicit val actorSystem: ActorSystem,
  override implicit val executionContext: ExecutionContext,
  override implicit val materializer: Materializer) extends Handler {

  import com.newmotion.Utils.formats

  def createActor(imperativeRequestContext: ImperativeRequestContext): ActorRef =
    actorSystem.actorOf(TarifRequestActor.props(imperativeRequestContext, tariffRepository))

  def handleSetTriffRequest(ctx: ImperativeRequestContext, tariff: Tariff): Unit =
    createActor(ctx) ! InputRequest(TarifRequestActor.SetTariff(tariff))

  def routes: Route = pathPrefix("tariffs") {
    post {
      entity(as[Tariff]) { data =>
        imperativelyComplete(handleSetTriffRequest(_, data))
      }
    }
  }
}
