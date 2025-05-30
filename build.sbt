ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.6.1"

// key required since 2.9.6 and dev keys are only valid about 1 month with then manual extension required, the fuck
val AkkaVersion = "2.9.5"

resolvers += "Akka library repository".at("https://repo.akka.io/maven")

lazy val root = (project in file("."))
  .settings(
    name := "tv-backtester",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
      "ch.qos.logback" % "logback-classic" % "1.5.7",
      "com.microsoft.playwright" % "playwright" % "1.48.0",
      "org.tpolecat" %% "doobie-core"      % "1.0.0-RC5",
      "org.tpolecat" %% "doobie-postgres"  % "1.0.0-RC5",
    )
  )

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs@_*) =>
    (xs map {_.toLowerCase}) match {
      case "services" :: xs =>
        MergeStrategy.filterDistinctLines
      case _ => MergeStrategy.discard
    }
  case PathList("reference.conf") => MergeStrategy.concat
  case _                        => MergeStrategy.first
}
