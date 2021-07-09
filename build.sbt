enablePlugins(JavaAppPackaging)

name := "akka-http-microservice"
organization := "com.theiterators"
version := "1.0"
scalaVersion := "3.0.1"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaHttpV      = "10.2.4"
  val akkaV          = "2.6.15"
  val scalaTestV     = "3.2.9"
  val circeV         = "0.14.1"
  val akkaHttpCirceV = "1.37.0"

  Seq(
    "io.circe"          %% "circe-core" % circeV,
    "io.circe"          %% "circe-parser" % circeV,
    "io.circe"          %% "circe-generic" % circeV,
    "org.scalatest"     %% "scalatest" % scalaTestV % "test"
  ) ++ Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-stream" % akkaV,
    "com.typesafe.akka" %% "akka-http" % akkaHttpV,
    "de.heikoseeberger" %% "akka-http-circe" % akkaHttpCirceV,
    "com.typesafe.akka" %% "akka-testkit" % akkaV,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpV % "test"
  ).map(_.cross(CrossVersion.for3Use2_13))
}

Revolver.settings
