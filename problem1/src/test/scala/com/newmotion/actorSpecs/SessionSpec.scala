package com.newmotion.actorSpecs

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.newmotion.models.{Currency, Session, SessionFee, Tariff}
import com.github.nscala_time.time.Imports._
import com.newmotion.actors.SessionRequestActor
import com.newmotion.repositories.{SessionFeeRepository, TariffRepository}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._

import scala.collection.immutable.SortedSet
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class SessionSpec()
  extends TestKit(ActorSystem("MySpec2")) with ImplicitSender
    with WordSpecLike
    with Matchers
    with BeforeAndAfterAll
    with MockitoSugar {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  import com.newmotion.actors.SessionRequestActor._

  val templateTariff = Tariff(
    currency = Currency.EUR,
    startFee = Option(0.20),
    hourlyFee = Option(1.00),
    feePerKWh = Option(0.25),
    activeStarting = DateTime.nextDay()
  )

  val customerSessionTemplate = Session(
    customerId = "customer_n",
    startTime = DateTime.lastHour(),
    endTime = DateTime.lastMinute(),
    volume = 1.00
  )

  "A session" must {
    "get rejected without any tariff" in {
      val tariffRepo = mock[TariffRepository]
      val sessionRepo = mock[SessionFeeRepository]
      val session = customerSessionTemplate.copy(startTime = DateTime.lastHour(), endTime = DateTime.now())
      val actor = system.actorOf(SessionRequestActor.props(null, sessionRepo, tariffRepo))

      when (tariffRepo.getInEffectTariff(session.startTime)) thenReturn Future.successful(None)

      actor ! SetSession(session)
      expectMsg(InEffectTariffNotFound)
    }

    "be computable with any tariff" in {
      val actor = system.actorOf(SessionRequestActor.props(null, null, null))

      val session = customerSessionTemplate.copy(
        startTime = DateTime.now.minusHours(2),
        endTime = DateTime.now(),
        volume = 13.3
      )

      val tariff = templateTariff.copy(
        startFee = Option(0.20),
        hourlyFee = Option(1.23),
        feePerKWh = Option(0.25),
        activeStarting = DateTime.now().minusDays(1)
      )

      val fee = 0.20 + (2.0 * 1.23) + (0.25 * 13.3)
      actor ! Compute(session, tariff)
      expectMsg(SessionFee(session, tariff, fee))

      val tariff2 = templateTariff.copy(
        startFee = Option(0.20),
        hourlyFee = Option(1.23),
        feePerKWh = Option(0.25),
        activeStarting = DateTime.now.minusMinutes(35)
      )

      val session2 = session.copy(
        startTime = DateTime.lastHour(),
        endTime = DateTime.now.minusMinutes(30)
      )

      val fee2 = 0.20 + (0.5 * 1.23) + (0.25 * 13.3)
      actor ! Compute(session2, tariff2)
      expectMsg(SessionFee(session2, tariff2, fee2))
    }

    "accept and start for computing" in {
      val tariffRepo = mock[TariffRepository]

      val session = customerSessionTemplate.copy(startTime = DateTime.lastHour(), endTime = DateTime.now())
      val tariff = templateTariff.copy(activeStarting = DateTime.now().minusMinutes(30))

      when(tariffRepo.getInEffectTariff(session.startTime)) thenReturn Future.successful(Option(tariff))

      val actor = system.actorOf(SessionRequestActor.props(null, null, tariffRepo))

      actor ! SetSession(session)
      expectMsg(Compute(session, tariff))
    }
  }

  "An Invoice" must {
    "tell there is no invoice for the user" in {
      val sessionRepo = mock[SessionFeeRepository]
      when(sessionRepo.findSessionsByCustomerID(any())) thenReturn Future.successful(None)

      val actor = system.actorOf(SessionRequestActor.props(null, sessionRepo, null))
      actor ! Invoice("customer_fake")
      expectMsg(InvoiceNotFound)
    }

    "be provided when requested and there is any record for the customer id" in {
      val sessionRepo = mock[SessionFeeRepository]

      val sessionFees = List(
        SessionFee(customerSessionTemplate, templateTariff, 1)
      )

      when(sessionRepo.findSessionsByCustomerID(any())) thenReturn Future.successful(Option(sessionFees))
      val actor = system.actorOf(SessionRequestActor.props(null, sessionRepo, null))
      actor ! Invoice("customer_fake")
      expectMsgPF() {
        case InvoiceReady(fees) => fees.equals(sessionFees)
        case _ => false
      }
    }
  }


}
