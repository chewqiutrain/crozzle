package crozzle.data

import java.time.LocalDate
import java.util.UUID

import cats.data.NonEmptyList
import cats.effect.Effect
import doobie.implicits._
import doobie.free.connection.ConnectionIO
import doobie.postgres.implicits._
import io.chrisdavenport.log4cats.Logger
import crozzle.model.{Player, Score}
import crozzle.db.namedLogHandler
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

class CrobieInterpreter[F[_]](implicit effect: Effect[F]) extends CrobieRepoAlg[ConnectionIO] {

  implicit val logger: Logger[F] = Slf4jLogger.getLoggerFromName("CrobieInterpreter")

  override def fetchPlayerById(player_id: UUID): ConnectionIO[Option[Player]] = {
    sql"""SELECT player_id, player_name
         |  FROM emc.players
         |  WHERE player_id = $player_id""".stripMargin
      .queryWithLogHandler[Player](namedLogHandler("fetchPlayerById"))
      .option
  }

  override def fetchPlayersByName(player_name: String): ConnectionIO[NonEmptyList[Player]] = {
    sql"""SELECT player_id, player_name
         |  FROM emc.players
         |  WHERE player_name = $player_name""".stripMargin
      .queryWithLogHandler[Player](namedLogHandler("fetchPlayersByName"))
      .nel
  }

  override def insertPlayer(player_name: String, application_version: String): ConnectionIO[Player] = {
    sql"""INSERT INTO emc.players (player_name, application_version)
          VALUES ($player_name, $application_version)
          ON CONFLICT (player_id) DO UPDATE
            SET player_name = EXCLUDED.player_name
          RETURNING player_id, player_name"""
      .updateWithLogHandler(namedLogHandler("insertPlayer"))
      .withUniqueGeneratedKeys[Player]("player_id", "player_name")
  }

  override def fetchScoresForPlayer(player_id: UUID): ConnectionIO[NonEmptyList[Score]] = {
    sql"""SELECT score_id, player_id, score, game_date
         |  FROM emc.scores
         |  WHERE player_id = $player_id""".stripMargin
      .queryWithLogHandler[Score](namedLogHandler("fetchScoresForPlayer"))
      .nel
  }

  // handle uniqueness on service side for now, until I figure out a better way of dealing with score_id
  private def insertScore(player_id: UUID, score: Int, game_date: LocalDate, application_version: String): ConnectionIO[Score] = {
    sql"""INSERT INTO emc.scores (player_id, score, game_date, application_version)
          VALUES ($player_id, $score, $game_date, $application_version)
          ON CONFLICT (player_id, game_date, score) DO NOTHING
          RETURNING score_id, player_id, score, game_date
       """
      .updateWithLogHandler(namedLogHandler("insertScore"))
      .withUniqueGeneratedKeys[Score]("player_id", "score", "game_date")
  }

  private def deleteScoreOnDateForPlayer(player_id: UUID, game_date: LocalDate): ConnectionIO[Int] = {
    sql"""DELETE FROM emc.scores
         |  WHERE player_id = $player_id AND game_date = $game_date""".stripMargin
      .updateWithLogHandler(namedLogHandler("deleteScoreOnDateForPlayer"))
      .run
  }

  override def insertScoreForPlayer(player_id: UUID, score: Int, game_date: LocalDate, application_version: String): ConnectionIO[Score] = {
    for {
      _ <- deleteScoreOnDateForPlayer(player_id, game_date)
      inserted <- insertScore(player_id, score, game_date, application_version)
    } yield inserted
  }



}
