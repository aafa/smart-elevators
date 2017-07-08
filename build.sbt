import sbt.Keys._

lazy val root: Project = (project in file("."))
  .settings(
    name := "Smart elevators",
    version := "1.0",
    scalaVersion := "2.12.1",
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.3",
    libraryDependencies += "me.chrons" %%% "diode" % "1.1.0",
    libraryDependencies += "com.lihaoyi" %% "scalatags" % "0.6.5",
    skip in packageJSDependencies := false
  )
  .enablePlugins(ScalaJSPlugin, WorkbenchPlugin)