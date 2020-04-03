package logic

import cats.data.NonEmptyList
import cats.effect.Concurrent
import fs2.Pipe
import logic.processes._
import models.Word

object Filters {
  type Process[F[_]] = Pipe[F, Word, Word]

  def apply[F[_]: Concurrent](wantedFilters: Set[String]): Either[Seq[String], Process[F]] = {
    val rules: Map[String, Process[F]] = Map(
      ("aggressive_word", AggressiveWords[F]),
      ("unknown_word", UnknownWords[F]),
    )

    val availableFilters: Set[String] = rules.keys.toSet

    val unknownFilters = (wantedFilters -- availableFilters).toList
    val knownFilters = (availableFilters & wantedFilters).toList

    if (unknownFilters.nonEmpty)
      Left(unknownFilters)
    else
      NonEmptyList.fromList(knownFilters)
      .flatMap { filters =>
        NonEmptyList.fromList(rules.filter(kv => filters.find(_ == kv._1).isDefined).values.toList)
      } map { processes =>
        processes.tail.fold(processes.head)((acc, e) => acc.andThen(e))
      } toRight(unknownFilters)
  }
}