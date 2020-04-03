package aggregateservice

import aggregateservice.Number._
import spray.json.DefaultJsonProtocol._

import scala.concurrent.{ExecutionContext, Future}

final case class AggregateRequest(function: String, values: String, valueType: String)
final case class Result(result: String)
final case class Error(message: String)
final case class AggregateResponse(result: Option[Result], error: Option[Error])

object AggregateResponse {
  def result: String => AggregateResponse = value => AggregateResponse(Some(Result(value)), None)
  def error: String => AggregateResponse = value => AggregateResponse(None, Some(Error(value)))
}

object AggregateServiceProtocol {
  implicit val requestFormat = jsonFormat3(AggregateRequest)
  implicit val errorFormat = jsonFormat1(Error)
  implicit val resultFormat = jsonFormat1(Result)
  implicit val aggregateResponseFormat = jsonFormat2(AggregateResponse.apply)
}

object AggregateService {
  type ErrorValue = String

  def validateOnError[T](errorValue: ErrorValue): Option[T] => Either[ErrorValue, T] = {
    case Some(t) => Right(t)
    case None => Left(errorValue)
  }

  def mainFlow: AggregateRequest => AggregateResponse = { case AggregateRequest(f, values, vtype) =>
    val result = for {
      valueType <- TypeHolder.identifyType.andThen(validateOnError(s"Received unknown value type: $vtype"))(vtype)
      function <- valueType.identifyFunction.andThen(validateOnError(s"Received unknown aggregate function: $f"))(f)
      values <- validateOnError("invalid input values")(NumberOp.convertCsvNumbers(valueType.impl)(values))
    } yield function(values)

    result match {
      case Right(value) => AggregateResponse.result(value.toString)
      case Left(errorMsg) => AggregateResponse.error(errorMsg)
    }
  }

  def doAggregate(req: AggregateRequest)(implicit ec: ExecutionContext): Future[AggregateResponse] =
    Future { mainFlow(req) }
}