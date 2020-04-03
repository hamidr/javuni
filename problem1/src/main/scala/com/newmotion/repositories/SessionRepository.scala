package com.newmotion.repositories

import com.newmotion.models.SessionFee

import scala.concurrent.{ExecutionContext, Future}
import scala.collection.concurrent.TrieMap
import com.github.nscala_time.time.Imports._

class SessionFeeRepository()(implicit ec: ExecutionContext)
{
  //A table indexed by CustomerId
  private val table = TrieMap[String, List[SessionFee]]()

  def create(session: SessionFee): Future[Unit] = Future {
    val customerId = session.session.customerId

    synchronized {
      val customerSessions = table.getOrElse(customerId, List())
      val updated = session :: customerSessions
      table.update(customerId, updated)
    }
  }

  def findSessionsByCustomerID(customerId: String): Future[Option[List[SessionFee]]] = Future {
      table.get(customerId).map(_.sortBy(_.session.startTime))
  }
}
