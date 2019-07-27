package crozzle.model

import java.time.LocalDate
import java.util.UUID

import doobie.util.{Read, Write}
import doobie.postgres.implicits._ // for UUID

case class Score(score_id: UUID, player_id: UUID, score: Integer, game_date: LocalDate)

object Score {
  def apply(score_id: UUID, player_id: UUID, score: Integer, game_date: LocalDate): Score = new Score(score_id, player_id, score, game_date)

  implicit val readScore: Read[Score] = Read[(UUID, UUID, Int, LocalDate)].map[Score](i => apply(i._1, i._2, i._3, i._4))
  implicit val writeScore: Write[Score] = Write[(UUID, UUID, Int, LocalDate)].contramap(s => (s.score_id, s.player_id, s.score, s.game_date))

}
