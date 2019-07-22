name := "crozzle"

version := "0.1"

scalaVersion := "2.12.8"

mainClass in (Compile, run) := Some("crozzle.Main")

scalacOptions += "-Ypartial-unification"

lazy val slf4j = "org.slf4j" % "slf4j-api" % "1.7.25"
lazy val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.2.3"

libraryDependencies ++= Seq(logbackClassic, slf4j)

val http4sVersion = "0.20.6"
lazy val http4s_dsl = "org.http4s" %% "http4s-dsl" % http4sVersion
lazy val http4s_blaze_server = "org.http4s" %% "http4s-blaze-server" % http4sVersion
lazy val http4s_blaze_client = "org.http4s" %% "http4s-blaze-client" % http4sVersion

libraryDependencies ++= Seq(http4s_dsl, http4s_blaze_server, http4s_blaze_client)


val log4catsVersion = "0.3.0"
lazy val log4cats_core = "io.chrisdavenport" %% "log4cats-core"  % log4catsVersion
lazy val log4cats_slf4j = "io.chrisdavenport" %% "log4cats-slf4j" % log4catsVersion

libraryDependencies ++= Seq(log4cats_core, log4cats_slf4j)

val catsVersion = "1.6.1"
lazy val cats_core = "org.typelevel" %% "cats-core" % catsVersion

val catsEffectVersion = "1.3.1"
lazy val cats_effect = "org.typelevel" %% "cats-effect" % catsEffectVersion

libraryDependencies ++= Seq(cats_core, cats_effect)


val fs2Version = "1.0.5"
lazy val fs2_core = "co.fs2" %% "fs2-core" % fs2Version

libraryDependencies += fs2_core

val doobieVersion = "0.7.0"
lazy val doobie_core = "org.tpolecat" %% "doobie-core" % doobieVersion
lazy val doobie_hikari = "org.tpolecat" %% "doobie-hikari" % doobieVersion
lazy val doobie_pg = "org.tpolecat" %% "doobie-postgres" % doobieVersion
lazy val doobie_scalatest = "org.tpolecat" %% "doobie-scalatest" % doobieVersion % "test"


libraryDependencies ++= Seq(doobie_core, doobie_hikari, doobie_pg, doobie_scalatest)