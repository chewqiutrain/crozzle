package crozzle

import scala.util.Try

// maybe not the best name..
object StringSyntax {

  private def safeToInt(s: String): Option[Int] =
    Try(s.toInt).toOption

  private def getMaybeTimeInts(a: Array[String]): Option[(Int, Int)] = {
    val min = a.head
    val sec = a.tail.head
    for {
      secInt <- safeToInt(sec)
      minStr = if (min.isEmpty) "0" else min
      minInt <- safeToInt(minStr)
    } yield (minInt, secInt)
  }


  private def parseBy(s: String)(delimiter: String): Option[(Int, Int)] = {
    val arr: Array[String] = s.split(delimiter)
    arr.length match {
      case 2 => getMaybeTimeInts(arr)
      case _ => None
    }
  }


  val parseByColon: String => Option[(Int, Int)] = parseBy(_)(":")
  val parseByDecimal: String => Option[(Int, Int)] = parseBy(_)("\\.")
  val parseByComma: String => Option[(Int, Int)] = parseBy(_)(",")


  def parseTime(s: String): Option[(Int, Int)] = {
    parseByColon(s) orElse
      parseByDecimal(s) orElse
      parseByComma(s)
  }

}
