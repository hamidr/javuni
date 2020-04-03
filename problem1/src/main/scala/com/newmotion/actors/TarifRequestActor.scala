package com.newmotion.actors

import akka.actor.{Props, Status}
import com.newmotion.Utils.ImperativeRequestContext
import com.newmotion.models.Tariff
import akka.pattern.pipe
import com.newmotion.Utils._
import com.github.nscala_time.time.Imports._
import com.newmotion.repositories.TariffRepository

import scala.concurrent.{ExecutionContext, Future}

/**
  * TariffRequestActor's companion object containing all the supported actions
  */
object TarifRequestActor {
  case class SetTariff(tariff: Tariff)
  case class ValidationChecked(tariff: Tariff)
  case class Created(tariff: Tariff)
  case object InvalidReq
  case class ReadyToCreate(currentTariff: Tariff)

  def props(ctx: ImperativeRequestContext, tariffRepository: TariffRepository)(implicit ec: ExecutionContext) =
    Props(new TarifRequestActor(ctx, tariffRepository))
}

/**
  * A class to represent an actor for setting the Tariffs using actor per request pattern.
  *
  * @param requestCtx A wrapper context for sending back the deserved data to the client
  * @param tariffRepository TariffRepository
  * @param ec ExecutionContext
  */
class TarifRequestActor(
  override val requestCtx: ImperativeRequestContext,
  tariffRepository: TariffRepository
)(implicit ec: ExecutionContext) extends ActorPerRequest {

  import TarifRequestActor._

  override def receive: Receive = {
    case InputRequest(req: SetTariff) =>
      self ! req

    case SetTariff(obj)         => this.setTariff(obj)
    case ValidationChecked(obj) => this.validationChecked(obj)
    case act: ReadyToCreate     => this.readyToCreate(act)

    case InvalidReq            => this.conflict("Invalid Tariff")
    case Created(tariff)       => this.created(tariff)
    case Status.Failure(cause) => this.failure(cause)

    case e =>
      log.error(s"Unwanted message to actor $e")
  }

  /**
    * Internal actor reaction packed in a method
    * Set a tariff as an in effect Tariff at the time
    * @param tariff A Tariff
    *
    */
  private def setTariff(tariff: Tariff): Unit = {
    if (validateTariffInput(tariff))
      sender() ! ValidationChecked(tariff)
    else
      sender() ! InvalidReq
  }

  /**
    * Internal actor reaction packed in a method
    * Validate a tariff and message back a "ReadyToCreate" if its ready to create otherwise an InvalidReq message
    * @param tariff A Tariff
    */
  private def validationChecked(tariff: Tariff): Unit = {
    checkWithLatestTariff(tariff)
      .map {
        case true => ReadyToCreate(tariff)
        case _ => InvalidReq
      }
      .pipeTo(sender())
  }

  /**
    * Internal actor reaction packed in a method
    * When called appends the requested tariff and a "Created" message to the sender
    * @param act
    */
  private def readyToCreate(act: ReadyToCreate): Unit = {
    val ReadyToCreate(current) = act
    tariffRepository.create(current).map(_ => Created(current)).pipeTo(sender())
  }

  /**
    * Check if a Tariff is valid to proceed
    * @param tariff
    * @return True as Valid and False as Invalid
    */
  def validateTariffInput(tariff: Tariff): Boolean = {
    List(tariff.feePerKWh, tariff.hourlyFee, tariff.startFee).exists(_.isDefined) &&
      tariff.activeStarting > DateTime.now()
  }

  /**
    * Check if the input tariff is older than the latest tariff
    * @param inputTariff
    * @return A boolean to represent the validity
    */
  def checkWithLatestTariff(inputTariff: Tariff): Future[Boolean] = {
    for {
      lastTariff <- tariffRepository.getLastTariff()
    } yield {
      val validation = lastTariff.forall(inputTariff.activeStarting > _.activeStarting)
      validation
    }
  }
}

