package logic.processes

import logic.Filters.Process

object UnknownWords {
  val knownWords = List(
    "hello",
    "world",
    "test",
    "i",
    "am",
    "just",
    "writing",
    "here"
  )

  def apply[F[_]]: Process[F] = {
    _.filter { w => knownWords.contains(w.value) }
  }
}
