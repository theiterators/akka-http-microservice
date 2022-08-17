import sbt.librarymanagement.ConflictWarning

enablePlugins(JavaAppPackaging)

name := "url_shortener"
organization := "com.github.gilcu2"
version := "0.1.0"
scalaVersion := "3.1.3"

conflictWarning := ConflictWarning.disable

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

resolvers += "Nexus" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies ++= {
  val akkaHttpV = "10.2.9"
  val akkaV = "2.6.19"
  val scalaTestV = "3.2.13"
  val circeV = "0.14.2"
  val akkaHttpCirceV = "1.39.2"
  val rediscalaVersion = "1.9.0"
  val scalaLoggingVersion = "3.9.4"
  val picoHashidsVersion = "4.5.153"


  Seq(
    "io.circe" %% "circe-core" % circeV,
    "io.circe" %% "circe-parser" % circeV,
    "io.circe" %% "circe-generic" % circeV,
    "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,

    "org.scalatest" %% "scalatest" % scalaTestV % Test,
    "org.scalatest" %% "scalatest-flatspec" % scalaTestV % Test,
  ) ++ Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-actor-typed" % akkaV,
    "com.typesafe.akka" %% "akka-stream" % akkaV,
    "com.typesafe.akka" %% "akka-http" % akkaHttpV,
    "de.heikoseeberger" %% "akka-http-circe" % akkaHttpCirceV,
    "com.github.etaty" %% "rediscala" % rediscalaVersion,
    "com.dispalt" %% "pico-hashids" % picoHashidsVersion,

    "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaV % Test,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpV % Test,
  ).map(_.cross(CrossVersion.for3Use2_13))
}

Test / scalacOptions += "-Wconf:msg=is not declared `infix`:s,msg=is declared 'open':s"
Revolver.settings
