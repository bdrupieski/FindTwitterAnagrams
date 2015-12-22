import sbt._
import Keys._

object CommonSettings {

  val projectSettings = Seq(
    organization := "com.briandrupieski",
    scalaVersion := Dependencies.scala,
    resolvers ++= Dependencies.resolvers,
    fork in Test := true,
    parallelExecution in Test := true
  )

  def BaseProject(name: String): Project = (
    Project(name, file(name))
      settings(projectSettings:_*)
    )
}