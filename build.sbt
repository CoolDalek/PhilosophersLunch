name := "PhilosophersLunch"

version := "0.1"

scalaVersion := "2.13.6"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % "2.6.15",
  "ch.qos.logback" % "logback-classic" % "1.2.4",
  "ch.qos.logback" % "logback-core" % "1.2.4",
  "com.github.pureconfig" %% "pureconfig" % "0.16.0",
)