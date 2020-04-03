package models

import typeclasses.Byteable

import scala.util.Try

final case class WordOccurrence(word: Word, occurrences: Int)

object WordOccurrence {
  val separator: Byte = ','

  implicit final case object ByteableWordOccurrence extends Byteable[WordOccurrence] {
    def toBytes: WordOccurrence => Vector[Byte] = { case WordOccurrence(word, occurrences) =>
      val wordBytes: Vector[Byte] = Byteable[Word].toBytes(word)
      val occ = occurrences.toString.toVector.map(_.toByte)
      wordBytes.appended(separator)
        .appendedAll(occ)
    }

    def toT: Vector[Byte] => Option[WordOccurrence] = { vec =>
      Try {
        val (a, b) = vec.span(_ != separator)
        val word = Byteable[Word].toT(a)
        val occurrences = b.map(_.toChar).drop(1).mkString.toInt
        word.map(WordOccurrence(_, occurrences))
      }.toOption.flatten
    }
  }
}