import CommonSettings._

name := "FindTwitterAnagrams"

version := "1.0"

scalaVersion := Dependencies.scala

mainClass in Compile := Some("ListenForAnagrams")

lazy val common = BaseProject("common") settings (libraryDependencies ++= Dependencies.allDependencies)

lazy val sampler = (BaseProject("sampler") settings (libraryDependencies ++= Dependencies.allDependencies)) dependsOn common