package aggregateservice

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives.{entity, _}
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

import scala.util.{Failure, Success}

class AggregateServiceRest {
  implicit val system = ActorSystem("aggregate-system")

  val config = ConfigFactory.load()
  val host   = config.getString("http.host")
  val port   = config.getInt("http.port")

  import aggregateservice.AggregateServiceProtocol._
  import akka.util.Timeout

  import scala.concurrent.duration._

  implicit val timeout          = Timeout(10 seconds)
  implicit val executionContext = system.dispatcher
  implicit val materializer     = ActorMaterializer()

  val route =
    post {
      entity(as[AggregateRequest]) { request =>
        val result = AggregateService.doAggregate(request)
        onComplete(result) {
          case Success(r) => complete(r)
          case Failure(f) => complete(HttpResponse(InternalServerError, entity = s"An exception occurred: ${f.getMessage}"))
        }
      }
    }


  def bind: Unit =
    Http().bindAndHandle(route, host, port).onComplete {
    case Success(address) => println(s"Successfully connected to $address")
    case Failure(t) =>
      println(s"Could not connect to $host:$port, ${t.getMessage}")
      system.terminate()
  }
}

object AggregateServiceLauncher extends App {
  val service = new AggregateServiceRest
  service.bind
}