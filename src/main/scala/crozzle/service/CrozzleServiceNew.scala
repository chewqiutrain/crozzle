package crozzle.service

import cats.effect.Effect
import crozzle.data.CrobieRepoAlg
import crozzle.model.{Player, Score}
import crozzle.service.algebra.{PlayerAlg, ScoreAlg}
import doobie.ConnectionIO

import java.util.UUID
class CrozzleServiceNew[F[_]: Effect](crobieInterpreter: CrobieRepoAlg[ConnectionIO])
  extends PlayerAlg[F] with ScoreAlg[F] {

  override def createPlayer(player: Player): F[Either[Throwable, Int]] = ???

  override def readPlayerById(playerId: UUID): F[Either[Throwable, Player]] = ???

  override def readPlayerByName(name: String): F[Either[Throwable, Player]] = ???

  override def createScore(playerId: UUID, score: Int): F[Either[Throwable, Int]] = ???

  override def readPlayerScores(playerId: UUID): F[Either[Throwable, List[Score]]] = ???

}
