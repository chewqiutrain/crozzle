package crozzle

import java.time.LocalDate

import cats.syntax._
import cats.implicits._
import cats.effect.{Effect, Resource}
import org.http4s.UrlForm
import crozzle.StringSyntax._
import crozzle.data.CrobieInterpreter
import crozzle.model.{Player, Score}
import doobie.hikari.HikariTransactor
import doobie.implicits._
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

import scala.concurrent.duration.Duration


class CrozzleService[F[_]](crobie: Resource[F, HikariTransactor[F]], crobieInterpreter: CrobieInterpreter[F])(implicit FE: Effect[F]) {

  private val log: Logger[F] = Slf4jLogger.getLoggerFromName[F]("CrozzleService")

  def intsToDuration(minInt: Int, secInt: Int): Duration = {
    val min = Duration(minInt, "min")
    val sec = Duration(secInt, "sec")
    min + sec
  }

  def getDuration(s: String): Option[Duration] =
    parseTime(s).map{ case (min, sec) => intsToDuration(min, sec) }

  def getUserAndTime(urlForm: UrlForm): Option[(String, Duration)] = {
    val duration: Option[Duration] = for {
      maybeTime <- urlForm.getFirst("text")
      maybeDuration <- getDuration(maybeTime)
    } yield maybeDuration

    val res: Option[(String, Duration)] = for {
      maybeDuration <- duration
      maybeUser <- urlForm.getFirst("user_name")
    } yield (maybeUser, maybeDuration)

    res
  }

  //scratch; to move.
  case class NameOrTimeFailure(urlForm: UrlForm) extends Throwable
  val applicationVersion = "0.1.0"

  def program(urlForm: UrlForm, xa: HikariTransactor[F]): F[(Player, Score)] = {
    val maybeNameAndTime = getUserAndTime(urlForm)
    val gameDate = LocalDate.now()
    val x = for {
      nameAndScore <- maybeNameAndTime.fold(FE.raiseError[(String, Duration)](NameOrTimeFailure(urlForm)))(x => FE.pure(x))
      _ <- log.info(s"Parsed Name: ${nameAndScore._1} | Score: ${nameAndScore._2.toString} | Assumed game date: ${gameDate.toString}")
      player <- crobieInterpreter.insertPlayer(nameAndScore._1, applicationVersion).transact(xa)
      _ <- log.info(s"Inserted Player | ${player.show}")
      scoreInserted <- crobieInterpreter.insertScoreForPlayer(player.player_id, nameAndScore._2.toSeconds.toInt, gameDate, applicationVersion).transact(xa)
      _ <- log.info(s"Inserted Score | ${scoreInserted.show}")
    } yield (player, scoreInserted)
    x
  }

  def run(urlForm: UrlForm): F[(Player, Score)] = {
    crobie.use{ xa => program(urlForm, xa) }
  }

}
