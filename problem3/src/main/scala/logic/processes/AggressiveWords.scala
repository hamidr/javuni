package logic.processes

import logic.Filters.Process

object AggressiveWords {
  val listOfWords = List ( // this can be a DB or service call
    "fuck"
    //or whatever your specification says so.
  )
  def apply[F[_]]: Process[F] = {
    _.filter(w => !listOfWords.contains(w.value))
  }
}
