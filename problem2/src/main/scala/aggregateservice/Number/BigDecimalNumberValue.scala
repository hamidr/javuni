package aggregateservice.Number

import scala.util.Try

case class BigDecimalNumberValue(value: BigDecimal) extends Number[BigDecimal]

object BigDecimalNumber extends NumberOp[BigDecimal] {
  def zero: Number[BigDecimal] = point(0)
  def point: BigDecimal => Number[BigDecimal] = n => BigDecimalNumberValue(n)
  def plus: (Number[BigDecimal], Number[BigDecimal]) => Number[BigDecimal] =
    (n1, n2) => point(n1.value + n2.value)

  def gt: (Number[BigDecimal], Number[BigDecimal]) => Number[BigDecimal] = (n1, n2) =>
    if (n1.value >= n2.value) n1
    else n2

  def div: (Number[BigDecimal], Number[BigDecimal]) => Option[Number[BigDecimal]] = {
    case (_, n) if n.value == 0 => None
    case (n, m) => Some(point(n.value / m.value))
  }

  def fromInt: Int => Number[BigDecimal] = n => point(BigDecimal(n))

  def fromStr: String => Option[Number[BigDecimal]] =
    str => Try(BigDecimal(str)).toOption.map(BigDecimalNumberValue)
}