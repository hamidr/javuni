package com.newmotion

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Timers}
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{DateTime => _, _}
import akka.http.scaladsl.server.{RequestContext, Route, RouteResult}
import akka.stream.Materializer
import com.github.nscala_time.time.Imports._
import com.newmotion.models.Currency
import com.newmotion.models.Currency.CurrencyType
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.JsonAST.JString
import org.json4s.native.Serialization
import org.json4s.{CustomSerializer, DefaultFormats, native}

import scala.concurrent.{ExecutionContext, Promise}

object Utils {

  implicit val formats = DefaultFormats + new DateTimeSerializer + new CurrencySerializer

  // Json4s serializer for [[DateTime]]
  class DateTimeSerializer extends CustomSerializer[DateTime](format => ({
      case JString(s) => DateTime.parse(s)
    }, {
      case date: DateTime => JString(date.toString)
    }
  ))


  class CurrencySerializer extends CustomSerializer[CurrencyType](format => ({
    case JString(s) => Currency.withName(s)
  }, {
    case e :CurrencyType => JString(e.toString)
  }
  ))


  // a custom directive
  def imperativelyComplete(inner: ImperativeRequestContext => Unit): Route = { ctx: RequestContext =>
    val p = Promise[RouteResult]()
    inner(new ImperativeRequestContext(ctx, p))
    p.future
  }

  final case class InputRequest[T](request: T)

  trait ActorPerRequest extends Actor with ActorLogging with Timers {
    def requestCtx: ImperativeRequestContext

    protected def ok[T](result: T) = {
      requestCtx.replyWith(result)
      context.stop(self)
    }

    protected def created[T](obj: T) = {
      requestCtx.replyWith[T](obj, StatusCodes.Created)
      context.stop(self)
    }

    protected def notFound = {
      requestCtx.replyWith(StatusCodes.NotFound)
      context.stop(self)
    }

    protected def failure(cause: Throwable) = {
      requestCtx.fail(cause)
      context.stop(self)
    }

    def conflict(reason: String = "Undefined") = {
      requestCtx.replyWith(reason, StatusCodes.Conflict)
      context.stop(self)
    }
  }

  trait Handler extends Json4sSupport {
    implicit def materializer: Materializer
    implicit def actorSystem: ActorSystem
    implicit def executionContext: ExecutionContext

    implicit val serialization = native.Serialization // or native.Serialization

    def createActor(imperativeRequestContext: ImperativeRequestContext): ActorRef
    def routes: Route
  }

  final class ImperativeRequestContext(ctx: RequestContext, promise: Promise[RouteResult]) {
    private implicit val ec = ctx.executionContext

    def complete(obj: ToResponseMarshallable): Unit = ctx.complete(obj)
        .onComplete(promise.complete)

    def fail(error: Throwable): Unit = ctx.fail(error)
      .onComplete(promise.complete)

    def entity: MessageEntity = ctx.request.entity

    def replyWith(status: StatusCode) =
      complete(HttpResponse(status = status))

    def replyWith[T](result: T, status: StatusCode =  StatusCodes.OK) = {
      val data =  Map(
        "result" -> status.reason(),
        "data" -> result
      )

      val response = HttpResponse(
        status = status,
        entity = HttpEntity(ContentTypes.`application/json`, Serialization.write(data))
      )
      complete(response)
    }
  }
}
