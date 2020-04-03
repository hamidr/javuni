package com.newmotion.repositories

import com.newmotion.models.{Tariff}

import scala.concurrent.{ExecutionContext, Future}
import scala.collection.immutable.SortedSet
import java.util.concurrent.atomic.AtomicReference
import com.github.nscala_time.time.Imports._


class TariffRepository()(implicit ec: ExecutionContext)
{
  import Tariff.ordering

  private[this] var table = SortedSet[Tariff]()

  def create(tariff: Tariff): Future[Unit] = Future {
    this.synchronized {
        this.table = this.table + tariff
    }
  }

  def getLastTariff(): Future[Option[Tariff]] = Future {
    this.synchronized {
      table.headOption
    }
  }

  def getInEffectTariff(startTime: DateTime) = Future {
    this.synchronized(table).find(_.activeStarting <= startTime)
  }
}
