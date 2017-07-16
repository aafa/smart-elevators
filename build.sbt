import sbt.Keys._

lazy val root: Project = (project in file("."))
  .settings(
    name := "Smart elevators",
    version := "1.0",
    scalaVersion := "2.11.11",
    libraryDependencies += "org.scala-js"               %%% "scalajs-dom"   % "0.9.0",
    libraryDependencies += "com.lihaoyi"                %%% "scalatags"     % "0.6.5",
    libraryDependencies += "org.scalatest"              %% "scalatest"      % "3.0.1" % "test",
    libraryDependencies += "com.github.julien-truffaut" %%% "monocle-core"  % "1.4.0",
    libraryDependencies += "com.github.julien-truffaut" %%% "monocle-macro" % "1.4.0",
    skip in packageJSDependencies := false,
    addCompilerPlugin("org.scalamacros" %% "paradise" % "2.1.0" cross CrossVersion.full)
  )
  .enablePlugins(ScalaJSPlugin, WorkbenchPlugin)
