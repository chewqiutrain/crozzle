package crozzle.model

import java.util.UUID

import cats.Show
import doobie.util.{Read, Write}
import doobie.postgres.implicits._ //for UUID

case class Player(player_id: UUID, player_name: String)

object Player {
  def apply(player_id: UUID, player_name: String): Player = new Player(player_id, player_name)

  implicit val readPlayer: Read[Player] = Read[(UUID, String)].map[Player](i => apply(i._1, i._2))
  implicit val writePlayer: Write[Player] = Write[(UUID, String)].contramap(p => (p.player_id, p.player_name))

  implicit val showPlayer: Show[Player] = Show.show[Player](p => s"Player[id: ${p.player_id} | name: ${p.player_name}]")
\
}
