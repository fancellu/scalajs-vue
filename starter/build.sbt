// A standalone Scala.js + Vue 3 + Vuetify 4 app using scalajs-vue from JitPack.
// Copy this folder to start your own project.

enablePlugins(ScalaJSPlugin)

name := "scalajs-vue-starter"
scalaVersion := "2.13.18"
scalacOptions ++= Seq("-deprecation", "-feature")

resolvers += "jitpack" at "https://jitpack.io"
libraryDependencies += "com.github.fancellu" % "scalajs-vue" % "v3.5.3"

// Run starter.Main.main when the script loads (it mounts the app)
scalaJSUseMainModuleInitializer := true

// Link straight into public/js, so public/index.html works as-is (open it, or serve the folder)
Compile / fastLinkJS / scalaJSLinkerOutputDirectory := baseDirectory.value / "public" / "js"
Compile / fullLinkJS / scalaJSLinkerOutputDirectory := baseDirectory.value / "public" / "js"

// `sbt run`: Scala.js's default `run` executes main in Node.js, which has no page, Vue or Vuetify
// ("Vuetify is not defined"). So `run` here links, opens public/index.html in your browser, then
// relinks on every save; refresh the page to see changes. Add -Dopen.browser=false to skip the browser.
commands += Command.command("openPage") { state =>
  val extracted = Project.extract(state)
  val (linked, _) = extracted.runTask(Compile / fastLinkJS, state)
  val page = (extracted.get(baseDirectory) / "public" / "index.html").getAbsoluteFile
  linked.log.info(s"App page: ${page.toURI.toString.replace("file:/", "file:///")}")
  if (!sys.props.get("open.browser").contains("false")) {
    val os = sys.props.getOrElse("os.name", "").toLowerCase
    val command =
      if (os.contains("win")) Seq("rundll32", "url.dll,FileProtocolHandler", page.getPath)
      else if (os.contains("mac")) Seq("open", page.getPath)
      else Seq("xdg-open", page.getPath)
    try { new ProcessBuilder(java.util.Arrays.asList(command.toArray*)).start(); () }
    catch { case e: Exception => linked.log.warn(s"Couldn't open a browser (${e.getMessage}); open the page above yourself") }
  }
  linked
}

addCommandAlias("run", "; openPage ; ~fastLinkJS")
