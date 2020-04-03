package com.newmotion.models

import com.github.nscala_time.time.Imports._
import org.joda.time.Minutes

case class Session(
  customerId: String,
  startTime: DateTime,
  endTime: DateTime,
  volume: Double
) {
  def durationInHours: Double   = this.durationInMinutes.toDouble / 60.0
  def durationInMinutes: Int = Minutes.minutesBetween(startTime, endTime).getMinutes
}

object SessionFee {
  implicit val orderSessionsBaseOnStarttime= new Ordering[SessionFee] {
    override def compare(x: SessionFee, y: SessionFee): Int =
      y.session.startTime.compare(x.session.startTime)
  }
}

case class SessionFee(session: Session, tariff: Tariff, fee: Double)
