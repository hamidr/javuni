package models

import io.circe.{Encoder, JsonObject}
import io.circe.syntax._

/*
  DigestedText is a representation of a segmented processed stream with storing Int.MaxSize elements as its limits
  which is not ideal. You also need to calculate the amount of size occupied by the element and
  multiply it by Int.MaxSize in order to predict the biggest evaluated Map.
  Remember this is not a fully implemented project! Details are important and this is just a way to show off!
  If you need more you need to hire me :))
 */
sealed trait DigestedText {
  def wordOccurrences: Map[Word, Long] //  - How many occurrences of each word
  def uniqueWordCount: Int //   - Number of individual words,
  def textSummary: Seq[Word]
}

object DigestedText {
  def fromMap(map: Map[Word, Long]): DigestedText = new DigestedText {
    val wordOccurrences: Map[Word, Long] = map
    lazy val uniqueWordCount: Int = wordOccurrences.size
    lazy val textSummary: Seq[Word] = wordOccurrences.keys.toSeq
  }

  implicit val encoder: Encoder[DigestedText] = Encoder.encodeJsonObject.contramapObject { s =>
    JsonObject(
      "wordOccurrences" := s.wordOccurrences.map {case (k,v) => (k.value, v)},
      "uniqueWordCount" := s.uniqueWordCount,
      "textSummary" := s.textSummary.map(_.value)
    )
  }
}