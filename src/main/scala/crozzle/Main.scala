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
import crozzle.model.{Player, Score, SimpleSlackMessage}


object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {

    val log: Logger[IO] = Slf4jLogger.getLoggerFromName[IO]("CrozzleMain")

    val transactor: Resource[IO, HikariTransactor[IO]] = for {
      ce <- ExecutionContexts.fixedThreadPool[IO](16)
      te <- ExecutionContexts.cachedThreadPool[IO]
      xa <- getTransactor[IO](ce, te)
    } yield xa

    val crobieInterpreter: CrobieInterpreter[IO] = new CrobieInterpreter[IO]()

    val crozzleService: CrozzleService[IO] = new CrozzleService(transactor, crobieInterpreter)

    // TODO: scratch work; move web server stuff to its own package
    def runResultHandler(res: Either[Throwable, (Player, Score)]): IO[Response[IO]] = {
      res match {
        case Left(t) => log.error(s"In Run Handler Left: Error: ${t.getMessage} \n ${t.getCause}") *>
          InternalServerError(s"Fatal Error: ${t.getMessage}")
        case Right((player, score)) => Ok(SimpleSlackMessage.defaultSlackMessage(player, score).asJson)
      }
    }

    val crozzleServiceRoute: HttpRoutes[IO] = HttpRoutes.of[IO] {
      case GET -> Root / "status" => Ok("Crozzle ALL SYSTEMS NOMINAL")
      case POST -> Root / "crozzle" => Ok("In Crozzle Post")
      case req @ POST -> Root => {
        req.decode[UrlForm]{ form =>
          val x = for {
            _ <- log.info(s"Received message: ${form.values.mkString("\n")}")
            playerAndScore <- crozzleService.run(form)
            _ <- log.info(s"CrozzleService.run successful")
          } yield playerAndScore

          val out = x.attempt.flatMap(maybeSuccess => runResultHandler(maybeSuccess))
          out
        }
      }
    }


    val httpApp = Router("/" -> crozzleServiceRoute).orNotFound

    val serverBuilder = BlazeServerBuilder[IO].bindHttp(8080, "localhost").withHttpApp(httpApp)

    //TODO: scratch work; to tidy up. Also figure out http4s shutdown hooks
    val x: IO[ExitCode] = IO(println("start")) *> serverBuilder.serve.compile.drain.as(ExitCode.Success) <* IO(println("shutdown"))
    x

  }
}
