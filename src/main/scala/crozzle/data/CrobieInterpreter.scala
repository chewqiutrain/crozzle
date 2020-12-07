package crozzle.data

import java.time.LocalDate
import java.util.UUID

import cats.data.NonEmptyList
import cats.effect.Effect
import doobie.implicits._
import doobie.free.connection.ConnectionIO
import doobie.implicits.legacy.localdate._ // needed for localdate
import doobie.postgres.implicits._ // needed for UUID
import io.chrisdavenport.log4cats.Logger
import crozzle.model.{Player, Score}
import crozzle.db.namedLogHandler
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger

class CrobieInterpreter[F[_]](implicit effect: Effect[F]) extends CrobieRepoAlg[ConnectionIO] {

  implicit val logger: Logger[F] = Slf4jLogger.getLoggerFromName("CrobieInterpreter")

  override def fetchPlayerById(playerId: UUID): ConnectionIO[Option[Player]] = {
    sql"""SELECT player_id, player_name
         |  FROM emc.players
         |  WHERE player_id = $playerId""".stripMargin
      .queryWithLogHandler[Player](namedLogHandler("fetchPlayerById"))
      .option
  }

  override def fetchPlayersByName(playerName: String): ConnectionIO[List[Player]] = {
    sql"""SELECT player_id, player_name
         |  FROM emc.players
         |  WHERE player_name = $playerName""".stripMargin
      .queryWithLogHandler[Player](namedLogHandler("fetchPlayersByName"))
      .to[List]
  }

  override def insertPlayer(playerName: String, applicationVersion: String): ConnectionIO[Player] = {
    sql"""INSERT INTO emc.players (player_name, application_version)
          VALUES ($playerName, $applicationVersion)
          ON CONFLICT (player_id) DO UPDATE
            SET player_name = EXCLUDED.player_name
          RETURNING player_id, player_name"""
      .updateWithLogHandler(namedLogHandler("insertPlayer"))
      .withUniqueGeneratedKeys[Player]("player_id", "player_name")
  }

  override def fetchScoresForPlayer(playerId: UUID): ConnectionIO[List[Score]] = {
    sql"""SELECT score_id, player_id, score, game_date
         |  FROM emc.scores
         |  WHERE player_id = $playerId""".stripMargin
      .queryWithLogHandler[Score](namedLogHandler("fetchScoresForPlayer"))
      .to[List]
  }

  // handle uniqueness on service side for now, until I figure out a better way of dealing with score_id
  private def insertScore(playerId: UUID, score: Int, gameDate: LocalDate, applicationVersion: String): ConnectionIO[Score] = {
    sql"""INSERT INTO emc.scores (player_id, score, game_date, application_version)
          VALUES ($playerId, $score, $gameDate, $applicationVersion)
          ON CONFLICT (player_id, game_date, score) DO NOTHING
          RETURNING score_id, player_id, score, game_date
       """
      .updateWithLogHandler(namedLogHandler("insertScore"))
      .withUniqueGeneratedKeys[Score]("player_id", "score", "game_date")
  }

  private def deleteScoreOnDateForPlayer(playerId: UUID, gameDate: LocalDate): ConnectionIO[Int] = {
    sql"""DELETE FROM emc.scores
         |  WHERE player_id = $playerId AND game_date = $gameDate""".stripMargin
      .updateWithLogHandler(namedLogHandler("deleteScoreOnDateForPlayer"))
      .run
  }

  override def insertScoreForPlayer(playerId: UUID, score: Int, gameDate: LocalDate, applicationVersion: String): ConnectionIO[Score] = {
    for {
      _ <- deleteScoreOnDateForPlayer(playerId, gameDate)
      inserted <- insertScore(playerId, score, gameDate, applicationVersion)
    } yield inserted
  }



}
