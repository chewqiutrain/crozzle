package crozzle.service

import cats.effect.{Effect, Resource}
import cats.implicits._
import crozzle.data.CrobieRepoAlg
import crozzle.model.{Player, Score}
import crozzle.service.algebra.{PlayerAlg, ScoreAlg}
import doobie.ConnectionIO
import doobie.util.transactor.Transactor
import doobie.implicits._

import java.time.LocalDate
import java.util.UUID

class CrozzleServiceNew[F[_]: Effect](crobieInterpreter: CrobieRepoAlg[ConnectionIO], xa: Resource[F, Transactor[F]])
  extends PlayerAlg[F] with ScoreAlg[F] {

  val appVersion = "0.0.1"

  override def createPlayer(name: String): F[Either[Throwable, Int]] = {
    val prog = for {
      inserted <- crobieInterpreter.insertPlayer(name, appVersion)
    } yield inserted

    val x = xa.use{ r => prog.transact(r).attempt }
    val y = x.map(e => e.map(_ => 1))

    y
  }


  override def readPlayerById(playerId: UUID): F[Either[Throwable, Player]] = {
    lazy val notFound = AsyncConnectionIO.raiseError[Player](new Exception("Not found"))

    val prog = for {
      res <- crobieInterpreter.fetchPlayerById(playerId)
      o <- res.fold(notFound)(_.pure[ConnectionIO])
    } yield o

    val x: F[Either[Throwable, Player]] = xa.use{ r => prog.transact(r).attempt }

    x
  }


  override def readPlayersByName(name: String): F[Either[Throwable, List[Player]]] = {
    lazy val notFound = AsyncConnectionIO.raiseError[Unit](new Exception("Not found"))

    val prog = for {
      res <- crobieInterpreter.fetchPlayersByName(name)
      _ <- if (res.isEmpty) notFound else AsyncConnectionIO.unit
    } yield res

    val x = xa.use{ r =>prog.transact(r).attempt }

    x
  }


  override def createScore(playerId: UUID, score: Int): F[Either[Throwable, Int]] = {
    lazy val notFound = AsyncConnectionIO.raiseError[Unit](new Exception("Not found"))

    val prog = for {
      maybePlayer <- crobieInterpreter.fetchPlayerById(playerId)
      _ <- notFound.whenA(maybePlayer.isEmpty)
      scoreInserted <- crobieInterpreter.insertScoreForPlayer(playerId, score, LocalDate.now(), appVersion)
    } yield scoreInserted

    val x = xa.use{ r => prog.transact(r).attempt }

    val y = x.map(e => e.map(_ => 1))

    y
  }


  override def readPlayerScores(playerId: UUID): F[Either[Throwable, List[Score]]] = {
    lazy val notFound = AsyncConnectionIO.raiseError[Unit](new Exception("Not found"))

    val prog = for {
      maybePlayer <- crobieInterpreter.fetchPlayerById(playerId)
      _ <- notFound.whenA(maybePlayer.isEmpty)
      maybeScores <- crobieInterpreter.fetchScoresForPlayer(playerId)
    } yield maybeScores

    xa.use{ r => prog.transact(r).attempt }
  }

}
