enablePlugins(ScalaJSPlugin)

name := "Scala.js+Vue.js example"

version := "3.5.45"

scalaVersion := "2.13.18"

scalacOptions ++= Seq("-deprecation", "-feature")

libraryDependencies ++= Seq(
  // sbt 2: %% is platform-aware, so this resolves scalajs-dom_sjs1_2.13
  "org.scala-js" %% "scalajs-dom" % "2.8.1"
)

// ---------------------------------------------------------------------------
// Demo dev server
//
//   sbt dev   (or sbt run)  link, serve on http://localhost:12345, relink on every change
//   sbt serve               link and serve, then return to the sbt shell
//
// Serves src/main/resources at /, the fastLinkJS output at /dev/ and the fullLinkJS
// output at /opt/. It is a command rather than a task so sbt 2's task cache can't skip it.
// ---------------------------------------------------------------------------

val devServerPort = settingKey[Int]("Port for the demo dev server")

devServerPort := 12345

commands += Command.command("serve") { state =>
  val extracted = Project.extract(state)
  val (linked, _) = extracted.runTask(Compile / fastLinkJS, state)
  val port = extracted.get(devServerPort)
  DevServer.start(
    port,
    Seq(
      "/dev/" -> extracted.get(Compile / fastLinkJS / scalaJSLinkerOutputDirectory),
      "/opt/" -> extracted.get(Compile / fullLinkJS / scalaJSLinkerOutputDirectory),
      "/" -> extracted.get(Compile / resourceDirectory)
    )
  )
  val log = linked.log
  log.info(s"Serving http://localhost:$port/index-dev.html (fastLinkJS)")
  log.info(s"        http://localhost:$port/index-opt.html (run fullLinkJS first)")
  linked
}

addCommandAlias("dev", "; serve ; ~fastLinkJS")

// A browser app can't run under Scala.js's Node-based `run`, so `run` starts the dev server.
addCommandAlias("run", "dev")

Global / onUnload := (Global / onUnload).value.andThen { s => DevServer.stop(); s }
