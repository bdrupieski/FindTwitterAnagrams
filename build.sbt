name := "FindTwitterAnagrams"

version := "1.0"

scalaVersion := "2.10.4"

mainClass in Compile := Some("SaveTweetsToDatabase")

libraryDependencies += "org.twitter4j" % "twitter4j-stream" % "4.0.4"
libraryDependencies += "com.typesafe" % "config" % "1.3.0"
libraryDependencies += "com.typesafe.slick" %% "slick" % "3.1.0"
libraryDependencies += "com.h2database" % "h2" % "1.4.190"
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"
libraryDependencies += "com.typesafe.scala-logging" % "scala-logging-slf4j_2.10" % "2.1.2"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.3"