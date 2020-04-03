package com.newmotion.models

import com.github.nscala_time.time.Imports._

object Currency extends Enumeration {
  type CurrencyType = Value
  val EUR = Value
}

import Currency._

object Tariff {
  implicit val ordering = new Ordering[Tariff] {
    override def compare(x: Tariff, y: Tariff): Int =
      y.activeStarting.compare(x.activeStarting)
  }
}

case class Tariff(
  currency: CurrencyType = EUR,
  startFee: Option[Double] = None,
  hourlyFee: Option[Double] = None,
  feePerKWh: Option[Double] = None,
  activeStarting: DateTime
)