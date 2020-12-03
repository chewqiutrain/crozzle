package crozzle.http

import cats.data.Kleisli
import cats.implicits._
import org.http4s.implicits._
import org.http4s.dsl.Http4sDsl
import cats.effect.{ConcurrentEffect, Timer}
//import crozzle.service.CrozzleServiceNew
import org.http4s.{HttpRoutes, Request, Response}
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder

import scala.concurrent.ExecutionContext

class WebServer[F[_]: ConcurrentEffect: Timer](host: String, port: Int)(implicit ec: ExecutionContext) extends Http4sDsl[F] {

  object idQueryParamMatcher extends QueryParamDecoderMatcher[String]("id")

  val statusRoutes = HttpRoutes.of[F] {
    case GET -> Root / "ping" => Ok("pong")
  }

  val playerRoutes = HttpRoutes.of[F] {
    case GET -> Root / "player" :? idQueryParamMatcher(maybePlayerId) =>
      Ok(maybePlayerId)
  }

  val routes = statusRoutes <+> playerRoutes

  val app: Kleisli[F, Request[F], Response[F]] = Router("/" -> routes).orNotFound

  val server: BlazeServerBuilder[F] = BlazeServerBuilder[F](ec).bindHttp(port, host).withHttpApp(app)

}
