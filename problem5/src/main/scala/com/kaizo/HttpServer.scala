package com.kaizo

import cats.data.Kleisli
import cats.effect.*
import cats.implicits.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.io.*
import org.http4s.implicits.*
import org.http4s.server.middleware.Logger

import java.time.OffsetDateTime

final case class AddCustomerRequest(domain: Uri, token: Token, startFrom: Option[OffsetDateTime])

class HttpServer(repo: ICustomerRepo):
  implicit val addCustomerEncoder: EntityDecoder[IO, AddCustomerRequest] = jsonOf[IO, AddCustomerRequest]

  private def endpoints: HttpRoutes[IO] = HttpRoutes.of[IO]:
    case req@POST -> Root / "customers" =>
      for
        createReq <- req.as[AddCustomerRequest]
        queueTime <- IO.delay(Utils.nextWindow)
        customer <- repo.insert(createReq.domain, createReq.token, queueTime, createReq.startFrom)
        response <- Created(customer.asJson)
      yield response

    case GET -> Root / "customers" / IntVar(id) =>
      repo.findById(id)
        .flatMap(Ok(_))

  def routes: Kleisli[IO, Request[IO], Response[IO]] = Logger.httpRoutes[IO](
    logHeaders = false,
    logBody = true,
    redactHeadersWhen = _ => false,
    logAction = Some((msg: String) => IO.println(msg))
  )(endpoints).orNotFound

end HttpServer

