enablePlugins(ScalaJSPlugin, WorkbenchPlugin)

name := "Scala.js+Vue.js example"

version := "2.6.10"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "0.9.7"
)

