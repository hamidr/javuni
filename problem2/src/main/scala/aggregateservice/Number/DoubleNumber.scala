package aggregateservice.Number

import scala.util.Try

case class DoubleNumberValue(value: Double) extends Number[Double]

object DoubleNumber extends NumberOp[Double] {
  def zero: Number[Double] = point(0)
  def point: Double => Number[Double] = n => DoubleNumberValue(n)
  def plus: (Number[Double], Number[Double]) => Number[Double] =
    (n1, n2) => point(n1.value + n2.value)

  def gt: (Number[Double], Number[Double]) => Number[Double] = (n1, n2) =>
    if (n1.value >= n2.value) n1
    else n2

  def div: (Number[Double], Number[Double]) => Option[Number[Double]] = {
    case (_, n) if n.value == 0 => None
    case (n, m) => Some(point(n.value / m.value))
  }

  def fromInt: Int => Number[Double] = n => point(n.toDouble)

  def fromStr: String => Option[Number[Double]] =
    str => Try(str.toDouble).toOption.map(DoubleNumberValue.apply)
}