import sbt.librarymanagement.ConflictWarning

enablePlugins(JavaAppPackaging)

name := "pekko-http-microservice"
organization := "com.theiterators"
version := "1.0"
scalaVersion := "3.2.2"

conflictWarning := ConflictWarning.disable

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

resolvers += "Apache Snapshots" at "https://repository.apache.org/content/repositories/snapshots/"

libraryDependencies ++= {
  val pekkoHttpV      = "0.0.0+4275-e7598916-SNAPSHOT"
  val pekkoV          = "0.0.0+26572-982780b0-SNAPSHOT"
  val circeV         = "0.14.4"
  val scalaTestV     = "3.2.15"
  
  Seq(
    "org.apache.pekko" %% "pekko-actor" % pekkoV,
    "org.apache.pekko" %% "pekko-stream" % pekkoV,
    "org.apache.pekko" %% "pekko-testkit" % pekkoV,
    "io.circe"          %% "circe-core" % circeV,
    "io.circe"          %% "circe-parser" % circeV,
    "io.circe"          %% "circe-generic" % circeV,
    "org.scalatest"     %% "scalatest" % scalaTestV % "test"
  ) ++ Seq(
    "org.apache.pekko" %% "pekko-http" % pekkoHttpV,
    "org.apache.pekko" %% "pekko-http-testkit" % pekkoHttpV % "test"
  ).map(_.cross(CrossVersion.for3Use2_13))
}

Revolver.settings
