package com.newmotion.actors

import akka.actor.{Props, Status}
import com.newmotion.Utils.{ActorPerRequest, ImperativeRequestContext, InputRequest}
import com.newmotion.models.{Session, SessionFee, Tariff}
import akka.pattern.pipe
import com.newmotion.repositories.{SessionFeeRepository, TariffRepository}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

/**
  * A companion object for SessionRequestActor which contains all the actions this actor can handle
  */
object SessionRequestActor {
  case class SetSession(session: Session)
  case class Compute(session: Session, tariff: Tariff)
  case class SessionsById(customerId: String)
  case class Created(fee: SessionFee)
  case class Invoice(customerId: String)
  case class InvoiceReady(fees: Iterable[SessionFee])
  case object InvoiceNotFound
  case object InEffectTariffNotFound

  def props(
    ctx: ImperativeRequestContext,
    sessionFeeRepository: SessionFeeRepository,
    tariffRepository: TariffRepository
  )(implicit ec: ExecutionContext) = Props(new SessionRequestActor(ctx, sessionFeeRepository, tariffRepository))
}

/**
 * A class to represent an actor for handling all sessions' actions using each actor per request pattern.
 *
 * @constructor Create a SessionRequestActor for handling request actions
 * @param requestCtx Request wrapper used for responding to request
 * @param sessionRepository The repository of SessionFees
 * @param tariffRepository The repository of Tariffs
 */
class SessionRequestActor(
  override val requestCtx: ImperativeRequestContext,
  sessionRepository: SessionFeeRepository,
  tariffRepository: TariffRepository
)(implicit ec: ExecutionContext) extends ActorPerRequest {

  import SessionRequestActor._

  override def receive: Receive = {


    case InputRequest(req: SetSession) =>
      self ! req

    case InputRequest(req: Invoice) =>
      self ! req

    case req: SetSession         => this.setSession(req)
    case InEffectTariffNotFound  => this.inEffectTariffNotFound()
    case computableData: Compute => this.compute(computableData)
    case sessionFee: SessionFee  => this.appendSessionFee(sessionFee)
    case Invoice(id)             => this.invoice(id)
    case data: InvoiceReady      => this.invoiceReady(data)

    case Created(fee)          => this.created(fee)
    case InvoiceNotFound       => this.notFound
    case Status.Failure(cause) => this.failure(cause)

    case _ =>
      log.error("Unwanted message!")
  }

  /**
   * Returns the total fee a session has to pay according to the passing tariff
   * @param session Session value
   * @param tariff Tariff value
   */
  def sessionFeeByTariff(session: Session, tariff: Tariff): Double = {
    val hours = session.durationInHours

    val occupiedChargerFee = tariff.hourlyFee.map(_ * hours).getOrElse(0.0)
    val chargingFee = tariff.feePerKWh.map(_ * session.volume).getOrElse(0.0)

    val totalFee =  tariff.startFee.getOrElse(0.0) + occupiedChargerFee + chargingFee

    totalFee
  }

  /** Internal actor reaction packed in a method
   * A request to append a Session to customers' log
   */
  private def setSession(setSession: SetSession): Future[Unit] = {
    val session = setSession.session

    val origin = sender()

    tariffRepository.getInEffectTariff(session.startTime).map {
      case Some(tariff) => origin ! Compute(session, tariff)
      case None =>         origin ! InEffectTariffNotFound
    } recover {
      case NonFatal(throwable) => self ! Status.Failure(throwable)
    }
  }

  /**
   * Internal actor reaction packed in a method
   * When there is no in effect tariff this method is called which a "Conflict reponse to the HTTP reuqest" 
   * @note ImperativeRequestContext
   */
  private def inEffectTariffNotFound(): Unit =
    conflict("No In Effect Tariff Found")


  /**
   * Internal actor reaction packed in a method
   * A request to compute a session with a tariff
   * @return A "SessionFee" message to the "sender"
   */
  private def compute(data: Compute): Unit = {
    val Compute(session, tariff) = data

    val fee = sessionFeeByTariff(session, tariff)
    sender() ! SessionFee(session, tariff, fee)
  }


  /**
   * Internal actor reaction packed in a method
   * A request to append a computed SessionFee for a customer
   * @return A "Created" message to the "sender"
   */
  private def appendSessionFee(computedSessionFee: SessionFee): Unit = {
    sessionRepository.create(computedSessionFee)
      .map(_ => Created(computedSessionFee))
      .pipeTo(sender())
  }


  /**
   * Internal actor reaction packed in a method
   * @return An "Invoice" message containing all the computed SessionFees to the sender
   */
  private def invoice(customerId: String): Unit = {
    val origin = sender

    sessionRepository.findSessionsByCustomerID(customerId) map {
      case Some(sessions) => origin ! InvoiceReady(sessions)
      case None => origin ! InvoiceNotFound
    } recover {
      case NonFatal(throwable) => self ! Status.Failure(throwable)
    }
  }


  /**
   * Internal actor reaction packed in a method
   * When called the invoice is ready to serve for the requester
   * @note ImperativeRequestContext
   */
  private def invoiceReady(invoiceReady: InvoiceReady): Unit = {
    ok(invoiceReady.fees)
  }
}
