package crozzle.model

import java.time.LocalDate
import java.util.UUID

import cats.Show
import doobie.util.{Read, Write}
import doobie.implicits.legacy.localdate._ // for LocalDate
import doobie.postgres.implicits._ // needed for UUID
import io.circe.{ Encoder, Decoder }
import io.circe.generic.semiauto._

case class Score(score_id: UUID, player_id: UUID, score: Int, game_date: LocalDate)

object Score {
  def apply(score_id: UUID, player_id: UUID, score: Integer, game_date: LocalDate): Score = new Score(score_id, player_id, score, game_date)

  implicit val readScore: Read[Score] = Read[(UUID, UUID, Int, LocalDate)].map[Score](i => apply(i._1, i._2, i._3, i._4))
  implicit val writeScore: Write[Score] = Write[(UUID, UUID, Int, LocalDate)].contramap(s => (s.score_id, s.player_id, s.score, s.game_date))

  implicit val circeDecoder: Decoder[Score] = deriveDecoder[Score]
  implicit val circeEncoder: Encoder[Score] = deriveEncoder[Score]

  implicit val showScore: Show[Score] = Show.show[Score](s => s"Score[player: ${s.player_id} | score: ${s.score} | gameDate: ${s.game_date.toString}]")
}
