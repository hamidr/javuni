package com.kaizo

import cats.effect.IO
import doobie.*
import doobie.implicits.*
import doobie.util.transactor.Transactor.Aux

import java.time.OffsetDateTime

trait IApiLogger:
  def error(id: Int, msg: String): IO[Unit]

  //TODO: Get logs to analyze the status of each call somehow
end IApiLogger

private final class ApiLogger(
  xa: Aux[IO, Unit]
) extends IApiLogger:

  override def error(id: Int, msg: String): IO[Unit] =
    sql"""
      INSERT INTO api_status (customer_id, status, message)
      VALUES (${id}, FALSE, ${msg});
    """.update.run.transact(xa).void
  end error

end ApiLogger

object ApiLogger:
  private val setupSchema: ConnectionIO[Int] = {
    sql"""
      CREATE TABLE IF NOT EXISTS api_status (
        customer_id INTEGER NOT NULL,
        status BOOLEAN NOT NULL,
        message TEXT,
        createdAt TIMESTAMPTZ NOT NULL DEFAULT current_timestamp::TIMESTAMPTZ
      );
      """.update.run
  }

  def init(xa: Aux[IO, Unit]): IO[IApiLogger] =
    setupSchema
      .transact(xa)
      .as(xa)
      .map(ApiLogger(_))
  end init
end ApiLogger