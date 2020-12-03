package crozzle

import cats.implicits._
import cats.syntax._
import cats.effect._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.circe.syntax._

import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import doobie.implicits._
import crozzle.db.getTransactor
import crozzle.data.CrobieInterpreter
import crozzle.model.{Player, Score}

import crozzle.http.WebServer
import scala.concurrent.ExecutionContext.Implicits.global



object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {

    val log: Logger[IO] = Slf4jLogger.getLoggerFromName[IO]("CrozzleMain")

    val transactor: Resource[IO, HikariTransactor[IO]] = for {
      ce <- ExecutionContexts.fixedThreadPool[IO](16)
      te <- ExecutionContexts.cachedThreadPool[IO]
      xa <- getTransactor[IO](ce, te)
    } yield xa

    val crobieInterpreter: CrobieInterpreter[IO] = new CrobieInterpreter[IO]()

//    val crozzleService: CrozzleService[IO] = new CrozzleService(transactor, crobieInterpreter)

    val host = "localhost"
    val port = 8080
    val webServer = new WebServer[IO](host, port)

    //TODO: scratch work; to tidy up. Also figure out http4s shutdown hooks
    val x: IO[ExitCode] = IO(println("start")) *> webServer.server.serve.compile.drain.as(ExitCode.Success) <* IO(println("shutdown"))
    x

  }
}
