package com.kaizo

import cats.effect.IO
import doobie.util.log
import doobie.util.transactor.Transactor.Aux
import doobie.{LogHandler, Transactor}

object DB:
  private object SqlLogger extends LogHandler[IO]:
    override def run(logEvent: log.LogEvent): IO[Unit] = logEvent match
      case log.Success(sql, args, label, exec, processing) =>
        IO.println(s"Success: $sql | args: $args | exec: ${exec.toMillis}ms | processing: ${processing.toMillis}ms")
      case log.ProcessingFailure(sql, args, label, exec, processing, failure) =>
        IO.println(s"Processing Failure: $sql | args: $args | error: ${failure.getMessage}")
      case log.ExecFailure(sql, args, label, exec, failure) =>
        IO.println(s"Execution Failure: $sql | args: $args | error: ${failure.getMessage}")
  end SqlLogger

  val xa: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.duckdb.DuckDBDriver",
    "jdbc:duckdb:data.db",
    "", "",
    logHandler = None // Some(SqlLogger)
  )
end DB


