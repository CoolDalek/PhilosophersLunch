lazy val projectName = "PhilosophersLunch"

name := projectName

version := "0.2"

scalaVersion := "2.13.6"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % "2.6.15",
  "ch.qos.logback" % "logback-classic" % "1.2.4",
  "ch.qos.logback" % "logback-core" % "1.2.4",
  "com.github.pureconfig" %% "pureconfig" % "0.16.0",
)

enablePlugins(JavaAppPackaging)

lazy val buildProject = taskKey[Unit]("Build project")

lazy val makeConfigurable = taskKey[Unit]("Make staged project configurable")

makeConfigurable := {
  (Universal / stage).value
  MakeConfigurable(projectName)
}

lazy val zipProject = taskKey[Unit]("Zip built project.")

zipProject := {
  makeConfigurable.value
  ZipProject(projectName)
}

buildProject := {
  zipProject.value
}