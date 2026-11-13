import java.io.File
import java.net.{BindException, InetSocketAddress}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}

import com.sun.net.httpserver.{HttpExchange, HttpHandler, HttpServer}

/** Tiny static file server for the demo pages, built on the JDK's HttpServer (no dependencies).
  * Files are read on every request with caching disabled, so a page reload always picks up the
  * latest linked JS and the HTML straight from examples/src/main/resources.
  */
object DevServer {
  private var server: Option[HttpServer] = None

  /** @param mounts URL prefix ("/", "/dev/", ...) -> directory served under it */
  def start(port: Int, mounts: Seq[(String, File)]): Unit = synchronized {
    stop()
    val s =
      try HttpServer.create(new InetSocketAddress("localhost", port), 0)
      catch {
        case e: BindException =>
          throw new RuntimeException(s"Port $port is already in use (another sbt still serving?)", e)
      }
    mounts.foreach { case (prefix, dir) =>
      s.createContext(prefix, new DirHandler(prefix, dir.toPath.toAbsolutePath.normalize))
    }
    s.start()
    server = Some(s)
  }

  def stop(): Unit = synchronized {
    server.foreach(_.stop(0))
    server = None
  }

  /** Opens `url` in the default browser, unless -Ddemo.browser=false. Never fails the build. */
  def openBrowser(url: String, info: String => Unit, warn: String => Unit): Unit =
    if (sys.props.get("demo.browser").contains("false")) info("Not opening a browser (demo.browser=false)")
    else {
      val os = sys.props.getOrElse("os.name", "").toLowerCase
      val command =
        if (os.contains("win")) Seq("rundll32", "url.dll,FileProtocolHandler", url)
        else if (os.contains("mac")) Seq("open", url)
        else Seq("xdg-open", url)
      try { new ProcessBuilder(java.util.Arrays.asList(command.toArray*)).start(); () }
      catch { case e: Exception => warn(s"Couldn't open a browser (${e.getMessage}); open $url yourself") }
    }

  private val contentTypes = Map(
    "html" -> "text/html; charset=utf-8",
    "js" -> "text/javascript; charset=utf-8",
    "map" -> "application/json; charset=utf-8",
    "json" -> "application/json; charset=utf-8",
    "css" -> "text/css; charset=utf-8"
  )

  private class DirHandler(prefix: String, root: Path) extends HttpHandler {
    def handle(ex: HttpExchange): Unit =
      try {
        val path = ex.getRequestURI.getPath
        if (path == "/") {
          ex.getResponseHeaders.add("Location", "/index-dev.html")
          ex.sendResponseHeaders(302, -1)
        } else {
          val file = root.resolve(path.stripPrefix(prefix).stripPrefix("/")).normalize
          if (file.startsWith(root) && Files.isRegularFile(file)) {
            val name = file.getFileName.toString
            val ext = name.substring(name.lastIndexOf('.') + 1)
            val body = Files.readAllBytes(file)
            ex.getResponseHeaders.add("Content-Type", contentTypes.getOrElse(ext, "application/octet-stream"))
            ex.getResponseHeaders.add("Cache-Control", "no-store")
            ex.sendResponseHeaders(200, body.length.toLong)
            ex.getResponseBody.write(body)
          } else {
            val body = s"Not found: $path".getBytes(StandardCharsets.UTF_8)
            ex.sendResponseHeaders(404, body.length.toLong)
            ex.getResponseBody.write(body)
          }
        }
      } finally ex.close()
  }
}
