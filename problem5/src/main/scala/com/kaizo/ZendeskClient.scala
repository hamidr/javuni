package com.kaizo

import cats.effect.IO
import org.http4s.client.Client
import org.http4s.client.middleware.Logger
import org.http4s.headers.*
import org.http4s.{AuthScheme, Credentials, Headers, MediaType, Method, Request, Uri}

import java.time.OffsetDateTime

trait IZendeskClient:
  def tickets(uri: String, token: Token, startTime: OffsetDateTime): IO[ApiResponse]
end IZendeskClient

class ZendeskClient(
  client: Client[IO]
) extends IZendeskClient:

  private val PER_PAGE = 100 //TODO: possible needs to be in config

  private val loggerClient = Logger[IO](
    logHeaders = false,
    logBody = false,
    redactHeadersWhen = _ => false,
    logAction = None//Some((msg: String) => IO.println(msg))
  )(client)

  private def buildRequest(domain: String, token: Token, startTime: OffsetDateTime): Request[IO] =
    val targetUri = Uri.unsafeFromString(domain) / "api" / "v2" / "incremental" / "tickets.json"
    val time = startTime.toEpochSecond
    val withParams = targetUri.withQueryParam("start_time", time)
      .withQueryParam("per_page", PER_PAGE)
    Request[IO](
      method = Method.GET,
      uri = withParams,
      headers = Headers(
        Authorization(Credentials.Token(AuthScheme.Bearer, token)),
        Accept(MediaType.application.json),
      )
    )
  end buildRequest

  override def tickets(domain: String, token: Token, startTime: OffsetDateTime): IO[ApiResponse] =
    import Codecs.*
    val request = buildRequest(domain, token, startTime)
    loggerClient.expect[ApiResponse](request)
  end tickets

end ZendeskClient

