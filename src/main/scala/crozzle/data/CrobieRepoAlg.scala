package crozzle.data

import java.time.LocalDate
import java.util.UUID

import cats.data.NonEmptyList
import crozzle.model.{Player, Score}

trait CrobieRepoAlg[F[_]] {
  def fetchPlayerById(playerId: UUID): F[Option[Player]]
  def fetchPlayersByName(playerName: String): F[List[Player]]
  def insertPlayer(playerName: String, applicationVersion: String): F[Player]

  def fetchScoresForPlayer(playerId: UUID): F[List[Score]]
  def insertScoreForPlayer(playerId: UUID, score: Int, gameDate: LocalDate, applicationVersion: String): F[Score]
}
