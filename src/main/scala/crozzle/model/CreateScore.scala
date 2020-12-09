package crozzle.model

import java.util.UUID

import cats.Show
import io.circe.{ Encoder, Decoder }
import io.circe.generic.semiauto._

final case class CreateScore(playerId: UUID, score: Int)

object CreateScore {
  implicit val circeDecoder: Decoder[CreateScore] = deriveDecoder[CreateScore]
  implicit val circeEncoder: Encoder[CreateScore] = deriveEncoder[CreateScore]

  implicit val showCreateScore: Show[CreateScore] = Show.show[CreateScore](cs => s"CreateScore: playerId: ${cs.playerId} | score: ${cs.score}")
}
