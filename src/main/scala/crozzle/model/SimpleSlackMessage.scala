package crozzle.model

import io.circe._
import io.circe.literal._
import io.circe.syntax._

case class SimpleSlackMessage(text: String)

object SimpleSlackMessage {

  implicit val encoder = Encoder.instance[SimpleSlackMessage]{ slackMsg =>
    json"""{"channel":"CLLSWPZ1T","text":${slackMsg.text}}"""
  }

  implicit val decoder = Decoder.instance[SimpleSlackMessage]{ hc => 
    hc.downField("text")
      .as[String]
      .map(apply)
  }

  def defaultSlackMessage(player: Player, score: Score): SimpleSlackMessage = {
    val text = s"Thank you ${player.player_name}, your score of ${score.score} seconds for ${score.game_date} has been received."
    SimpleSlackMessage(text)
  }
}
