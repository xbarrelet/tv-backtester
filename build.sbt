ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.1"
val AkkaVersion = "2.9.1"

resolvers += "Akka library repository".at("https://repo.akka.io/maven")

lazy val root = (project in file("."))
  .settings(
    name := "tv-backtester",
    idePackagePrefix := Some("ch.xavier"),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
      "ch.qos.logback" % "logback-classic" % "1.4.12",
      "org.scalatest" %% "scalatest" % "3.2.15" % "test",
      "com.microsoft.playwright" % "playwright" % "1.31.0"
    )
  )
