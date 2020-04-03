package com.newmotion.repositorySpecs

import com.newmotion.models.{Currency, Tariff}
import org.scalatest.AsyncFlatSpec
import org.scalatest.mockito.MockitoSugar
import com.newmotion.repositories.TariffRepository
import com.github.nscala_time.time.Imports._


class TariffRepositorySpec extends AsyncFlatSpec with MockitoSugar {
  //Lucky me I don't have to mock DB!

  val tariffTemplate = Tariff(Currency.EUR, None, None, None, DateTime.now())

  "Last tariff" should "be None when no tariff is defined" in {
    val repo = new TariffRepository()

    repo.getLastTariff() map { tariff =>
      assert(tariff.isEmpty)
    }
  }

  "Last tariff" should "be the most recent tariff" in {
    val repo = new TariffRepository()
    val t1 = tariffTemplate.copy(activeStarting = DateTime.now.minusMinutes(100))
    val t2 = tariffTemplate.copy(activeStarting = DateTime.now.minusMinutes(90))
    val t3 = tariffTemplate.copy(activeStarting = DateTime.now.minusMinutes(80))

    for {
      _ <- repo.create(t1)
      _ <- repo.create(t2)
      _ <- repo.create(t3)
      Some(last) <- repo.getLastTariff()
    } yield assert(last == t3)
  }

  "In effect tariff" should "be accessible by startTime of a session" in {
    val repo = new TariffRepository()
    val t1 = tariffTemplate.copy(startFee = Option(1.0), activeStarting = DateTime.now.minusMinutes(100))
    val t2 = tariffTemplate.copy(startFee = Option(2.0), activeStarting = DateTime.now.minusMinutes(90))
    val t3 = tariffTemplate.copy(startFee = Option(3.0), activeStarting = DateTime.now.minusMinutes(80))

    for {
      _ <- repo.create(t1)
      _ <- repo.create(t2)
      _ <- repo.create(t3)
      Some(e1) <- repo.getInEffectTariff(t1.activeStarting.plusMinutes(5))
      Some(e2) <- repo.getInEffectTariff(t2.activeStarting.plusMinutes(5))
      Some(e3) <- repo.getInEffectTariff(t3.activeStarting.plusMinutes(5))

      none <- repo.getInEffectTariff(t1.activeStarting.minusMinutes(5))
    } yield assert(e1 == t1 && e2 == t2 && e3 == t3 && none.isEmpty)
  }


}
