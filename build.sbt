enablePlugins(JavaAppPackaging)

name := "akka-http-microservice"
organization := "com.theiterators"
version := "1.0"
scalaVersion := "2.11.7"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaV = "2.4.1"
  val akkaStreamV = "2.0.1"
  val slickV = "3.1.1"
  val scalaTestV = "2.2.5"
  val slf4jNoV: String = "1.6.4"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-stream-experimental" % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-core-experimental" % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-testkit-experimental" % akkaStreamV,
    "com.typesafe.slick" %% "slick" % slickV,
    "mysql" % "mysql-connector-java" % "5.1.38",
    "org.slf4j" % "slf4j-nop" % slf4jNoV,
    "com.h2database" % "h2" % "1.4.187",
    "org.scalatest" %% "scalatest" % scalaTestV % "test"
  )
}

Revolver.settings
