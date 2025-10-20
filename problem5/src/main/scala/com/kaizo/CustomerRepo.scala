package com.kaizo

import cats.effect.IO
import doobie.*
import doobie.implicits.*
import doobie.util.transactor.Transactor.Aux
import fs2.Stream
import org.http4s.Uri
import doobie.implicits.javatimedrivernative.*

import java.time.OffsetDateTime

trait ICustomerRepo:
  def insert(domain: Uri, token: Token, queueAt: OffsetDateTime, startFrom: Option[OffsetDateTime]): IO[Customer]
  def findById(id: Int): IO[Customer]
  def updateCursor(id: Int, startFrom: OffsetDateTime): IO[Unit]
  def readyForQueue(below: OffsetDateTime, moveTo: OffsetDateTime): Stream[IO, Customer]
end ICustomerRepo

private final class CustomerRepo(
  xa: Aux[IO, Unit]
) extends ICustomerRepo:

  override def insert(domain: Uri, token: Token, queueAt: OffsetDateTime, startFrom: Option[OffsetDateTime]): IO[Customer] =
    val domainStr = domain.renderString
    val query = sql"""
      INSERT INTO customers (domain, token, queue_at, start_from)
      VALUES (
        ${domainStr},
        ${token},
        ${queueAt},
        ${startFrom}
      )
      RETURNING *;
    """.query[Customer].unique
    query.transact(xa)
  end insert

  override def findById(id: Int): IO[Customer] =
    sql"""
      SELECT id, domain, token, queue_at, start_from FROM customers
      WHERE id = ${id};
    """.query[Customer].unique.transact(xa)
  end findById

  def updateCursor(id: Int, startFrom: OffsetDateTime): IO[Unit] =
    sql"""
      UPDATE customers
      SET start_from = ${startFrom},
          updated_at = current_timestamp::TIMESTAMPTZ
      WHERE id = ${id};
    """.update.run.transact(xa).void
  end updateCursor

  override def readyForQueue(below: OffsetDateTime, moveTo: OffsetDateTime): Stream[IO, Customer] =
    sql"""
      UPDATE customers
      SET queue_at = NOW + (60 / 10),
          updated_at = current_timestamp::TIMESTAMPTZ
      WHERE queue_at < NOW
      RETURNING id, domain, token, queue_at, start_from;
      """.query[Customer].stream.transact(xa)
  end readyForQueue
end CustomerRepo

object CustomerRepo:
  private val setupSchema: ConnectionIO[Int] = {
    sql"""
      CREATE SEQUENCE IF NOT EXISTS customers_id_seq START 1;
      CREATE TABLE IF NOT EXISTS customers (
        id INTEGER PRIMARY KEY DEFAULT nextval('customers_id_seq'),
        domain TEXT UNIQUE NOT NULL,
        token TEXT NOT NULL UNIQUE,
        queue_at TIMESTAMPTZ NOT NULL,
        start_from TIMESTAMPTZ DEFAULT NULL,
        created_at TIMESTAMPTZ NOT NULL DEFAULT current_timestamp::TIMESTAMPTZ,
        updated_at TIMESTAMPTZ DEFAULT NULL
      );
      CREATE INDEX IF NOT EXISTS idx_customers_queue_at ON customers(queue_at);
      """.update.run
  }

  def init(xa: Aux[IO, Unit]): IO[ICustomerRepo] =
    setupSchema
      .transact(xa)
      .as(xa)
      .map(CustomerRepo(_))
  end init
end CustomerRepo