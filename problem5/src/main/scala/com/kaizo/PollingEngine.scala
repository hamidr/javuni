package com.kaizo

import cats.effect.{IO, Resource}
import com.kaizo.Utils.*
import fs2.Stream

import java.time.{Instant, OffsetDateTime}

trait IPollingEngine:
  def poll: Stream[IO, Customer]
end IPollingEngine

class PollingEngine(
  zendeskApi: IZendeskClient,
  customerRepo: ICustomerRepo,
  logger: IApiLogger
) extends IPollingEngine:
  private val CONCURRENCY_LEVEL = 10 //TODO: Config value?

  def poll: Stream[IO, Customer] =
    val times = IO.delay( (Utils.now, Utils.nextWindow) )
    for
      _           <- Stream.awakeEvery[IO](TRIGGER_TIME)
      (now, next) <- Stream.eval(times)
      customer    <- customerRepo.readyForQueue(now, next)
    yield customer
  end poll

  def tickets: Stream[IO, ZendeskTicket] = {
    poll
      .parEvalMap(CONCURRENCY_LEVEL) { customer =>
        val fetchFrom = customer.startFrom.getOrElse(oneMinuteAgo)
        zendeskApi.tickets(customer.domain, customer.token, fetchFrom)
          .attempt
          .map((_, customer))
      }.evalTap {
        case (Left(err), customer) =>
          logger.error(customer.id, err.getMessage)
        case (Right(resp), customer) =>
          resp.endTime.fold(IO.unit)(time => customerRepo.updateCursor(customer.id, time))
      }.flatMap {
        case (Right(resp), _) => Stream.emits(resp.tickets)
        case _ => Stream.empty
      }
  }
  end tickets
end PollingEngine