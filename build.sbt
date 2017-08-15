
enablePlugins(ScalaJSPlugin, WorkbenchPlugin)


name := "Scala.js+Vue.js example"

version := "2.2.4"

scalaVersion := "2.12.3"

libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "0.9.3"
)
