enablePlugins(JavaAppPackaging)

name := "akka-http-microservice"
organization := "com.theiterators"
version := "1.0"
scalaVersion := "3.0.0"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaHttpV      = "10.2.4"
  val akkaV          = "2.6.14"
  val scalaTestV     = "3.2.9"
  val circeV         = "0.14.0"
  val akkaHttpCirceV = "1.36.0"

  Seq(
    "com.typesafe.akka" %  "akka-actor_2.13" % akkaV,
    "com.typesafe.akka" %  "akka-stream_2.13" % akkaV,
    "com.typesafe.akka" %  "akka-http_2.13" % akkaHttpV,
    "io.circe"          %% "circe-core" % circeV,
    "io.circe"          %% "circe-parser" % circeV,
    "io.circe"          %% "circe-generic" % circeV,
    "de.heikoseeberger" % "akka-http-circe_2.13" % akkaHttpCirceV,
    "com.typesafe.akka" %  "akka-testkit_2.13" % akkaV,
    "com.typesafe.akka" %  "akka-http-testkit_2.13" % akkaHttpV % "test",
    "org.scalatest"     %% "scalatest" % scalaTestV % "test"
  )
}

Revolver.settings
