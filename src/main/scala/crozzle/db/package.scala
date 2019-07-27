package crozzle

import cats.effect._
import cats.effect.syntax._
import cats.implicits._

import doobie._
//import doobie.implicits._
import doobie.hikari._
import doobie.util.log.{ Success, ProcessingFailure, ExecFailure }
import io.chrisdavenport.log4cats.Logger

import scala.concurrent.ExecutionContext

package object db {
  //hardcode parameters for now
  def getTransactor[M[_] : Async : ContextShift](connectionEC: ExecutionContext, transactionEC: ExecutionContext): Resource[M, HikariTransactor[M]] =
    HikariTransactor.newHikariTransactor[M](
      "org.postgresql.Driver",                        // driver classname
      "jdbc:postgresql://localhost:15435/crobie",   // connect URL
      "postgres",                                   // username
      "12345",                                     // password
      connectionEC,                                     // await connection here
      transactionEC // execute JDBC operations here
    )

  // unconvinced that this is worth the hassle of .runAsync. callback is wrt to logging error?
  def namedLogHandler[F[_]](queryName: String = "Unknown query")(implicit logger: Logger[F], effect: Effect[F]): LogHandler = new LogHandler( logEvent => logEvent match {
      case Success(s, a, e1, e2) =>
        effect.runAsync(
          logger.info(s"""Successful Statement Execution:
                |
                |  ${s.linesIterator.dropWhile(_.trim.isEmpty).mkString("\n  ")}
                |
                | arguments = [${a.mkString(", ")}]
                |   elapsed = ${e1.toMillis} ms exec + ${e2.toMillis} ms processing (${(e1 + e2).toMillis} ms total)
                """.stripMargin))(_ => IO.unit).unsafeRunSync()

      case ProcessingFailure(s, a, e1, e2, t) =>
        effect.runAsync(
          logger.error(s"""Failed Resultset Processing:
                 |
                 |  ${s.linesIterator.dropWhile(_.trim.isEmpty).mkString("\n  ")}
                 |
                 | arguments = [${a.mkString(", ")}]
                 |   elapsed = ${e1.toMillis} ms exec + ${e2.toMillis} ms processing (failed) (${(e1 + e2).toMillis} ms total)
                 |   failure = ${t.getMessage}
                 """.stripMargin))(_ => IO.unit).unsafeRunSync()

      case ExecFailure(s, a, e1, t) =>
        effect.runAsync(
          logger.error(s"""Failed Statement Execution:
                 |
                 |  ${s.linesIterator.dropWhile(_.trim.isEmpty).mkString("\n  ")}
                 |
                 | arguments = [${a.mkString(", ")}]
                 |   elapsed = ${e1.toMillis} ms exec (failed)
                 |   failure = ${t.getMessage}
                 """.stripMargin))(_ => IO.unit).unsafeRunSync()
    }
  )
}
