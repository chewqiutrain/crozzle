package crozzle.service

import crozzle.model.{ Player, Score }

import java.util.UUID

object algebra {

  trait PlayerAlg[F[_]] {
    def createPlayer(player: Player): F[Either[Throwable, Int]]
    def createPlayerScore(playerId: UUID, score: Int): F[Either[Throwable, Int]]

    def readPlayerByName(name: String): F[Either[Throwable, Player]]
    def readPlayerById(playerId: UUID): F[Either[Throwable, Player]]
  }

  trait ScoreAlg[F[_]] {
    def createScore(playerId: UUID, score: Int): F[Either[Throwable, Int]]
    def readPlayerScores(playerId: UUID): F[Either[Throwable, List[Score]]]
    def updatePlayerScore(playerId: UUID, scoreId: UUID, newScore: Int): F[Either[Throwable, Int]]
    def deletePlayerScore(playerId: UUID, scoreId: UUID): F[Either[Throwable, Int]]
  }

}
