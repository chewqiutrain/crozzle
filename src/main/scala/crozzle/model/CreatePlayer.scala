package crozzle.model

import io.circe.{ Encoder, Decoder }
import io.circe.generic.semiauto._

final case class CreatePlayer(name: String)

object CreatePlayer {
  implicit val circeDecoder: Decoder[CreatePlayer] = deriveDecoder[CreatePlayer]
  implicit val circeEncoder: Encoder[CreatePlayer] = deriveEncoder[CreatePlayer]
}
