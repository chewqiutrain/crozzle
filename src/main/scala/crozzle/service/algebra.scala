package crozzle.service

import crozzle.model.{ Player, Score }

import java.util.UUID

object algebra {

  trait PlayerAlg[F[_]] {
    def createPlayer(name: String): F[Either[Throwable, Int]]

    def readPlayerById(playerId: UUID): F[Either[Throwable, Player]]
    def readPlayersByName(name: String): F[Either[Throwable, List[Player]]]
  }

  trait ScoreAlg[F[_]] {
    def createScore(playerId: UUID, score: Int): F[Either[Throwable, Int]]
    def readPlayerScores(playerId: UUID): F[Either[Throwable, List[Score]]]
  }

}
