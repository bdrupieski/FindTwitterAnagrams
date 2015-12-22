import sbt._

object Dependencies {

  val scala = "2.11.7"

  val resolvers = DefaultOptions.resolvers(snapshot = true) ++ Seq(
    //"scalaz-releases" at "http://dl.bintray.com/scalaz/releases"
  )

  object loggingDependencies {
    val slfj = "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2"
    val logback = "ch.qos.logback" % "logback-classic" % "1.1.3"
  }

  object databaseDependencies {
    val hikari = "com.typesafe.slick" %% "slick-hikaricp" % "3.1.0"
    val postgres = "org.postgresql" % "postgresql" % "9.4-1206-jdbc4"
    val slick = "com.typesafe.slick" %% "slick" % "3.1.1"
  }

  val scalaTest = "org.scalatest" %% "scalatest" % "2.2.4" % "test"
  val typesafeConfig = "com.typesafe" % "config" % "1.3.0"
  val twitter4j = "org.twitter4j" % "twitter4j-stream" % "4.0.4"

  val allDependencies: Seq[ModuleID] =  Seq(
    loggingDependencies.slfj,
    loggingDependencies.logback,
    databaseDependencies.hikari,
    databaseDependencies.postgres,
    databaseDependencies.slick,
    scalaTest,
    typesafeConfig,
    twitter4j
  )
}

