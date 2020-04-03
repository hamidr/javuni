package com.newmotion.repositorySpecs

import com.github.nscala_time.time.Imports._
import com.newmotion.models.{Currency, Session, SessionFee, Tariff}
import org.scalatest.AsyncFlatSpec
import org.scalatest.mockito.MockitoSugar
import com.newmotion.repositories.SessionFeeRepository

class SessionRepositorySpec extends AsyncFlatSpec with MockitoSugar {

  val templateTariff = Tariff(
    currency = Currency.EUR,
    startFee = Option(0.20),
    hourlyFee = None,
    feePerKWh = None,
    activeStarting = DateTime.lastHour()
  )

  val customerSessionTemplate = Session(
    customerId = "customer_n",
    startTime = DateTime.lastHour().minusMinutes(10),
    endTime = DateTime.lastHour(),
    volume = 1.00
  )

  def templateSessionFee(id: String, n: Int) = SessionFee(
    session = customerSessionTemplate.copy(
      customerId = id,
      startTime = customerSessionTemplate.startTime.plusMinutes(n)
    ),
    tariff = templateTariff,
    fee = 0.20
  )

  "SessionFeeRepository" must "return None when there is no record for an ID" in {
    val repo = new SessionFeeRepository()
    val sessionFee1= templateSessionFee("user1", 2)
    val sessionFee2= templateSessionFee("user1", 3)
    val sessionFee3= templateSessionFee("user1", 4)

    for {
      _ <- repo.create(sessionFee1)
      _ <- repo.create(sessionFee2)
      _ <- repo.create(sessionFee3)

      none <- repo.findSessionsByCustomerID("user2")
    } yield {
      assert(none.isEmpty)
    }
  }

  "Multiple sessions for a cutomer" should "be accessible at once when asked" in {
    val repo = new SessionFeeRepository()
    val sessionFee1= templateSessionFee("user1", 2)
    val sessionFee2= templateSessionFee("user1", 3)
    val sessionFee3= templateSessionFee("user1", 4)

    for {
      _ <- repo.create(sessionFee1)
      _ <- repo.create(sessionFee2)
      _ <- repo.create(sessionFee3)

      Some(l1) <- repo.findSessionsByCustomerID("user1")
    } yield {
      assert(List(sessionFee1, sessionFee2, sessionFee3) == l1)
    }
  }

}