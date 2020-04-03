package aggregateservice.Number

import cats.data.NonEmptyList
import scala.util.Try

trait Number[T] {
  def value: T
  override def toString: String = value.toString
}

trait NumberOp[T] {
  def point: T => Number[T]
  def plus: (Number[T], Number[T]) => Number[T]
  def gt: (Number[T], Number[T]) => Number[T]
  def zero: Number[T]
  def div: (Number[T], Number[T]) => Option[Number[T]]
  def fromInt: Int => Number[T]
  def fromStr: String => Option[Number[T]]
}

object NumberOp {
  def sum[T](implicit imp: NumberOp[T]): NonEmptyList[Number[T]] => Number[T] =
    _.foldLeft(imp.zero)(imp.plus)

  def max[T](implicit imp: NumberOp[T]): NonEmptyList[Number[T]] => Number[T] = nums =>
    nums.tail.foldLeft(nums.head)(imp.gt)

  def mean[T](implicit imp: NumberOp[T]): NonEmptyList[Number[T]] => Number[T] = nums =>
    imp.div(sum(imp)(nums), imp.fromInt(nums.length)).getOrElse(imp.zero)

  def convertCsvNumbers[T: NumberOp]: String => Option[NonEmptyList[Number[T]]] = { str =>
    Try(str.split(',').map(_.trim).map(implicitly[NumberOp[T]].fromStr.andThen(_.get))).toOption
      .flatMap(array => NonEmptyList.fromList(array.toList))
  }
}
