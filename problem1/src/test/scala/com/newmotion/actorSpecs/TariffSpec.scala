package com.newmotion.actorSpecs

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.newmotion.models.{Currency, Tariff}
import com.github.nscala_time.time.Imports._
import com.newmotion.actors.TarifRequestActor
import com.newmotion.repositories.TariffRepository
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class TariffSpec()
  extends TestKit(ActorSystem("MySpec")) with ImplicitSender
    with WordSpecLike
    with Matchers
    with BeforeAndAfterAll
    with MockitoSugar {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  import com.newmotion.actors.TarifRequestActor._

  val templateTariff = Tariff(
    currency = Currency.EUR,
    startFee = Option(0.20),
    hourlyFee = Option(1.00),
    feePerKWh = Option(0.25),
    activeStarting = DateTime.nextDay()
  )


  "A Taiff" must {
    "be valid if at least one fee is filled" in {
      val repo = mock[TariffRepository]
      val actor = system.actorOf(TarifRequestActor.props(null, repo))

      val validTariff1 = templateTariff.copy(startFee = None)
      actor ! SetTariff(validTariff1)
      expectMsg(ValidationChecked(validTariff1))

      val validTariff2 = templateTariff.copy(startFee = None, hourlyFee = None)
      actor ! SetTariff(validTariff2)
      expectMsg(ValidationChecked(validTariff2))
    }

    "be invalid if all of its `fee` components are empty (= not defined) at the same time" in {
      val repo = mock[TariffRepository]

      val actor = system.actorOf(TarifRequestActor.props(null, repo))
      val invalidTariff = templateTariff.copy(startFee = None, hourlyFee = None, feePerKWh = None)
      actor ! SetTariff(invalidTariff)

      expectMsg(InvalidReq)
    }

    "be valid if `activeStarting` is later than NOW" in {
      val repo = mock[TariffRepository]
      val actor = system.actorOf(TarifRequestActor.props(null, repo))

      val validTariff = templateTariff.copy(activeStarting = DateTime.nextHour())
      actor ! SetTariff(validTariff)
      expectMsg(ValidationChecked(validTariff))

      val invalidTariff = templateTariff.copy(activeStarting = DateTime.lastMinute())
      actor ! SetTariff(invalidTariff)
      expectMsg(InvalidReq)
    }

    "be valid if `activeStarting` is later than the latest `activeStarting` value in a previous tariff message" in {
      val repo = mock[TariffRepository]
      val actor = system.actorOf(TarifRequestActor.props(null, repo))

      val lastTariff = templateTariff.copy(activeStarting = DateTime.lastMinute())
      when(repo.getLastTariff()) thenReturn Future.successful(Some(lastTariff))

      val validTariff = templateTariff.copy(activeStarting = DateTime.now())
      actor ! ValidationChecked(validTariff)
      expectMsg(ReadyToCreate(validTariff))

      val invalidTariff = templateTariff.copy(activeStarting = DateTime.lastHour())
      actor ! ValidationChecked(invalidTariff)

      expectMsg(InvalidReq)
    }

    "be valid if there is no tariff defined" in {
      val repo = mock[TariffRepository]
      val actor = system.actorOf(TarifRequestActor.props(null, repo))

      when(repo.getLastTariff()) thenReturn Future.successful(None)
      val validTariff = templateTariff.copy(activeStarting = DateTime.nextHour())
      actor ! ValidationChecked(validTariff)
      expectMsg(ReadyToCreate(validTariff))
    }
  }

}
