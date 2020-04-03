package aggregateservice.Number

import scala.util.Try

case class LongNumberValue(value: Long) extends Number[Long]

object LongNumber extends NumberOp[Long] {
  def zero: Number[Long] = point(0)
  def point: Long => Number[Long] = n => LongNumberValue(n)
  def plus: (Number[Long], Number[Long]) => Number[Long] =
    (n1, n2) => point(n1.value + n2.value)

  def gt: (Number[Long], Number[Long]) => Number[Long] = (n1, n2) =>
    if (n1.value >= n2.value) n1
    else n2

  def div: (Number[Long], Number[Long]) => Option[Number[Long]] = {
    case (_, n) if n.value == 0 => None
    case (n, m) => Some(point(n.value / m.value))
  }

  def fromInt: Int => Number[Long] = n => point(n.toLong)

  def fromStr: String => Option[Number[Long]] =
    str => Try(str.toLong).toOption.map(LongNumberValue.apply)
}