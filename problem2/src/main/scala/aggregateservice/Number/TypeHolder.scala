package aggregateservice.Number

import cats.data.NonEmptyList

trait TypeHolder {
  type N
  type AggregateFunction = NonEmptyList[Number[N]] => String

  implicit def impl: NumberOp[N]

  def identifyFunction: String => Option[AggregateFunction] = { str =>
    val f = str match {
      case "max" => Some(NumberOp.max[N])
      case "mean" => Some(NumberOp.mean[N])
      case "sum" => Some(NumberOp.sum[N])
      case _ => None
    }

    f.map(_.andThen(_.toString))
  }
}

object TypeHolder {
  case object LongType extends TypeHolder {
    override type N = Long
    override implicit val impl: NumberOp[Long] = LongNumber
  }

  case object DoubleType extends TypeHolder {
    override type N = Double
    implicit val impl: NumberOp[Double] = DoubleNumber
  }

  case object BigDecimalType extends TypeHolder {
    override type N = BigDecimal
    implicit val impl: NumberOp[BigDecimal] = BigDecimalNumber
  }

  def identifyType[T]: String => Option[TypeHolder] = {
    case "long" => Some(LongType)
    case "double" => Some(DoubleType)
    case "bigdecimal" => Some(BigDecimalType)
    case _ => None
  }
}