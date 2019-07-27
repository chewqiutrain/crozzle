package crozzle.data

import java.time.LocalDate
import java.util.UUID

import cats.data.NonEmptyList
import crozzle.model.{Player, Score}

trait CrobieRepoAlg[F[_]] {
  def fetchPlayerById(player_id: UUID): F[Option[Player]]
  def fetchPlayersByName(player_name: String): F[NonEmptyList[Player]]
  def insertPlayer(player_name: String, application_version: String): F[Player]

  def fetchScoresForPlayer(player_id: UUID): F[NonEmptyList[Score]]
  def insertScoreForPlayer(player_id: UUID, score: Int, game_date: LocalDate, application_version: String): F[Score]
}
