import com.lihaoyi.workbench.Plugin._

enablePlugins(ScalaJSPlugin)

workbenchSettings

name := "Scala.js+Vue.js example"

version := "2.2.4"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "0.9.1"
)

bootSnippet := ""

updateBrowsers <<= updateBrowsers.triggeredBy(fastOptJS in Compile)

