package crozzle.http

import cats.data.Kleisli
import cats.implicits._
import cats.effect.{ConcurrentEffect, Timer}
import org.http4s.implicits._
import org.http4s.dsl.Http4sDsl
import org.http4s.circe.CirceEntityCodec._
import org.http4s.circe._
import org.http4s.EntityEncoder

import crozzle.service.CrozzleServiceNew

import scala.util.Try

import org.http4s.{HttpRoutes, Request, Response}
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder

import scala.concurrent.ExecutionContext
import java.util.UUID

import crozzle.model.Player._
import io.circe.syntax._
import io.circe.Encoder

class WebServer[F[_]: Timer](host: String, port: Int)(crozzleService: CrozzleServiceNew[F])(implicit ec: ExecutionContext, F: ConcurrentEffect[F]) extends Http4sDsl[F] {

  object idQueryParamMatcher extends QueryParamDecoderMatcher[String]("id")
  object playerNameQueryParamMatcher extends QueryParamDecoderMatcher[String]("name")

  def handleServiceError[A](e: Either[Throwable, A])(implicit en: Encoder[A]): F[Response[F]] = {
    e.fold(
      t => InternalServerError(t.toString),
      a => Ok(a)
    )
  }

  val statusRoutes = HttpRoutes.of[F] {
    case GET -> Root / "ping" => Ok("pong")
  }


  val playerRoutes = HttpRoutes.of[F] {
    case GET -> Root / "player" :? idQueryParamMatcher(maybePlayerId) => {
      val resp = for {
        maybeParsedPlayerId <- F.fromTry(Try(UUID.fromString(maybePlayerId)))
        res <- crozzleService.readPlayerById(maybeParsedPlayerId)
        out <- handleServiceError(res)
      } yield out

      resp
    }

    //TODO: take entity instead of query param
    case POST -> Root / "player" :? playerNameQueryParamMatcher(playerName) => {
      val resp = for {
        res <- crozzleService.createPlayer(playerName)
        out <- handleServiceError(res)
      } yield out

      resp
    }
  }

  val routes = statusRoutes <+> playerRoutes

  val app: Kleisli[F, Request[F], Response[F]] = Router("/" -> routes).orNotFound

  val server: BlazeServerBuilder[F] = BlazeServerBuilder[F](ec).bindHttp(port, host).withHttpApp(app)

}
