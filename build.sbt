enablePlugins(JavaAppPackaging)

name := "akka-http-microservice"
organization := "com.theiterators"
version := "1.0"
scalaVersion := "2.13.2"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaHttpV      = "10.2.3"
  val akkaV          = "2.6.12"
  val scalaTestV     = "3.2.4"
  val circeV         = "0.13.0"
  val akkaHttpCirceV = "1.35.3"

  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-stream" % akkaV,
    "com.typesafe.akka" %% "akka-http" % akkaHttpV,
    "io.circe"          %% "circe-core" % circeV,
    "io.circe"          %% "circe-generic" % circeV,
    "de.heikoseeberger" %% "akka-http-circe" % akkaHttpCirceV,
    "com.typesafe.akka" %% "akka-testkit" % akkaV,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpV % "test",
    "org.scalatest"     %% "scalatest" % scalaTestV % "test"
  )
}

Revolver.settings
