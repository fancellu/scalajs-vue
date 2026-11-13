// Two projects:
//   core      the Scala.js bindings (com.felstar.scalajs.vue / .vuetify); the only published module,
//             so the JitPack jar holds just the bindings: "com.github.fancellu" % "scalajs-vue" % "<tag>"
//   examples  the demo apps and their pages; depends on core, never published
// The root project only aggregates them.

ThisBuild / organization := "com.github.fancellu"
ThisBuild / version := "3.5.45"
ThisBuild / scalaVersion := "2.13.18"
ThisBuild / scalacOptions ++= Seq("-deprecation", "-feature")

lazy val root = (project in file("."))
  .aggregate(core, examples)
  .settings(
    name := "scalajs-vue-root",
    publish / skip := true
  )

lazy val core = (project in file("core"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "scalajs-vue",
    // sbt 2: %% is platform-aware, so this resolves scalajs-dom_sjs1_2.13
    libraryDependencies += "org.scala-js" %% "scalajs-dom" % "2.8.1"
  )

lazy val examples = (project in file("examples"))
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(core)
  .settings(
    name := "scalajs-vue-examples",
    publish / skip := true
  )

// ---------------------------------------------------------------------------
// Demo dev server
//
//   sbt dev   (or sbt run)  link, serve on http://localhost:12345, relink on every change
//   sbt serve               link and serve, then return to the sbt shell
//   sbt "demo timer"        like dev, but opens your browser on just that demo
//   sbt demoTimer           shortcut for the above; one per demo, listed in `demos` below
//
// Serves examples/src/main/resources at /, the fastLinkJS output at /dev/ and the fullLinkJS
// output at /opt/. These are commands rather than tasks so sbt 2's task cache can't skip them.
// Add -Ddemo.browser=false to stop `demo` opening a browser.
// ---------------------------------------------------------------------------

val devServerPort = settingKey[Int]("Port for the demo dev server")

devServerPort := 12345

// demo id -> page, relative to the server root. Vuetify ids must match example.vuetify.Main.examples.
val demos: Seq[(String, String)] =
  Seq(
    "gallery" -> "index-dev.html",
    "hello" -> "hello-dev.html" // example.plain.HelloWorld: Vue only, no Vuetify
  ) ++ Seq("todos", "basics", "temperature", "timer", "grid", "fetch", "circles").map { id =>
    id -> s"demo-dev.html?demo=$id"
  }

def startServer(state: State): State = {
  val extracted = Project.extract(state)
  val (linked, _) = extracted.runTask(examples / Compile / fastLinkJS, state)
  val port = extracted.get(devServerPort)
  DevServer.start(
    port,
    Seq(
      "/dev/" -> extracted.get(examples / Compile / fastLinkJS / scalaJSLinkerOutputDirectory),
      "/opt/" -> extracted.get(examples / Compile / fullLinkJS / scalaJSLinkerOutputDirectory),
      "/" -> extracted.get(examples / Compile / resourceDirectory)
    )
  )
  val log = linked.log
  log.info(s"Serving http://localhost:$port/index-dev.html (fastLinkJS)")
  log.info(s"        http://localhost:$port/index-opt.html (run fullLinkJS first)")
  linked
}

commands += Command.command("serve")(startServer)

addCommandAlias("dev", "; serve ; ~examples/fastLinkJS")

// A browser app can't run under Scala.js's Node-based `run`, so `run` starts the dev server.
addCommandAlias("run", "dev")

commands += Command("demo", ("demo <name>", s"Serve one demo and open it: ${demos.map(_._1).mkString(", ")}"), "") { _ =>
  import sbt.internal.util.complete.DefaultParsers._
  Space ~> NotSpace.examples(demos.map(_._1)*)
} { (state, id) =>
  demos.collectFirst { case (`id`, page) => page } match {
    case Some(page) =>
      val served = startServer(state)
      val url = s"http://localhost:${Project.extract(state).get(devServerPort)}/$page"
      served.log.info(s"Demo '$id': $url")
      DevServer.openBrowser(url, served.log.info(_), served.log.warn(_))
      "~examples/fastLinkJS" :: served
    case None =>
      state.log.error(s"Unknown demo '$id'. Try one of: ${demos.map(_._1).mkString(", ")}")
      state.fail
  }
}

// One shortcut per demo: demoGallery, demoHello, demoTodos, demoTimer, ...
demos.map { case (id, _) => addCommandAlias(s"demo${id.capitalize}", s"demo $id") }.flatten

Global / onUnload := (Global / onUnload).value.andThen { s => DevServer.stop(); s }

// ---------------------------------------------------------------------------
// Static site for hosting (Vercel: see vercel.json and scripts/sbt.sh)
//
//   sbt dist   fullLinkJS, then assemble target/dist:
//                index.html  the Vuetify gallery (example.vuetify.Main, ScalaVue.main)
//                demo.html   one Vuetify example, ?demo=<id>
//                hello.html  the plain Vue Hello World
//                *.css, opt/main.js
// ---------------------------------------------------------------------------

commands += Command.command("dist") { state =>
  val extracted = Project.extract(state)
  val (linked, _) = extracted.runTask(examples / Compile / fullLinkJS, state)
  val resources = extracted.get(examples / Compile / resourceDirectory)
  val out = extracted.get(LocalRootProject / baseDirectory) / "target" / "dist"
  IO.delete(out)
  Seq("index", "demo", "hello").foreach(page => IO.copyFile(resources / s"$page-opt.html", out / s"$page.html"))
  resources.listFiles().filter(_.getName.endsWith(".css")).foreach(css => IO.copyFile(css, out / css.getName))
  IO.copyDirectory(extracted.get(examples / Compile / fullLinkJS / scalaJSLinkerOutputDirectory), out / "opt")
  linked.log.info(s"Static site in $out (index.html = gallery)")
  linked
}
