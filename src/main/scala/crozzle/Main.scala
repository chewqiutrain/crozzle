package crozzle

import cats.implicits._
import cats.syntax._
import cats.effect._

import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts

import crozzle.db.getTransactor
import crozzle.data.CrobieInterpreter
import crozzle.model.{Player, Score}
import crozzle.http.WebServer
import crozzle.service.CrozzleServiceNew

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

    val crozzleService: CrozzleServiceNew[IO] = new CrozzleServiceNew[IO](crobieInterpreter, transactor)

    val host = "localhost"
    val port = 8080
    val webServer = new WebServer[IO](host, port)(crozzleService)

    //TODO: scratch work; to tidy up.
    val x: IO[ExitCode] = IO(println("start")) *> webServer.server.serve.compile.drain.as(ExitCode.Success) <* IO(println("shutdown"))
    x

  }
}
