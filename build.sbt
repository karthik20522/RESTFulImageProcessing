organization := "com.imageprocessing"

version := "0.1"

scalaVersion := "2.10.3"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

resolvers ++= Seq(
  "spray repo" at "http://repo.spray.io/"
)

libraryDependencies ++= {
  val akkaV = "2.1.4"
  val sprayV = "1.1.0"
  Seq(
    "io.spray" % "spray-can" % sprayV,
    "io.spray" % "spray-routing" % sprayV,
    "io.spray" % "spray-testkit" % sprayV,
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-testkit" % akkaV,
    "org.specs2" %% "specs2" % "2.2.3" % "test",
    "org.json4s" %% "json4s-native" % "3.2.6",
    "com.sksamuel.scrimage" % "scrimage-core_2.10" % "1.3.14",
    "com.sksamuel.scrimage" % "scrimage-filters_2.10" % "1.3.14",
    "net.databinder.dispatch" %% "dispatch-core" % "0.11.0",
    "com.github.nscala-time" %% "nscala-time" % "0.8.0"
    /*"org.slf4j"    % "slf4j-api"    % "1.7.1",
	"org.slf4j"    % "log4j-over-slf4j"  % "1.7.1",
	"ch.qos.logback"   % "logback-classic"  % "1.0.3"*/
  )
}

seq(Revolver.settings: _*)