package models
import fs2.Chunk
import typeclasses.Byteable

import scala.util.matching.Regex

final case class Word private(value: String)

object Word {
  val separator: Byte = ' '
  val regex = new Regex("[\\p{L}]+")
  val longestWord = 60 //https://en.wikipedia.org/wiki/Longest_words

  def fromString(str: String): Option[Word] =
    if (regex.matches(str) && str.length <= longestWord)
      Some(Word(str))
    else None

  def fromChunkByte[F[_]](chunk: Chunk[Byte]): Option[Word] = {
    val str = chunk
      .filter(byte => regex.matches(byte.toChar.toString))
      .toArray
      .map(_.toChar)
      .mkString
    fromString(str)
  }

  implicit final case object ByteableWord extends Byteable[Word] {
    def toBytes: Word => Vector[Byte] =
      _.value.toVector.map(_.toByte)

    def toT: Vector[Byte] => Option[Word] = vec =>
      fromChunkByte(Chunk.vector(vec))
  }
}
