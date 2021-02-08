enablePlugins(JavaAppPackaging)

name := "akka-http-microservice"
organization := "com.theiterators"
version := "1.0"
scalaVersion := "3.0.0-M3"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaHttpV      = "10.2.3"
  val akkaV          = "2.6.12"
  val scalaTestV     = "3.2.3"
  val circeV         = "0.14.0-M3"
  //val akkaHttpCirceV = "1.35.3"

  Seq(
    "com.typesafe.akka" %  "akka-actor_2.13" % akkaV,
    "com.typesafe.akka" %  "akka-stream_2.13" % akkaV,
    "com.typesafe.akka" %  "akka-http_2.13" % akkaHttpV,
    "io.circe"          %% "circe-core" % circeV,
    "io.circe"          %% "circe-parser" % circeV,
    "io.circe"          %% "circe-generic" % circeV,
    // no release for circe 0.14.0-M3 yet, so it's copy-pasted right now
    //"de.heikoseeberger" % "akka-http-circe_2.13" % akkaHttpCirceV,
    "com.typesafe.akka" %  "akka-testkit_2.13" % akkaV,
    "com.typesafe.akka" %  "akka-http-testkit_2.13" % akkaHttpV % "test",
    "org.scalatest"     %% "scalatest" % scalaTestV % "test"
  )
}

Revolver.settings
