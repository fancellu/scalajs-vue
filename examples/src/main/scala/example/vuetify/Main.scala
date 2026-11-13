package example.vuetify

import scala.scalajs.js
import scala.scalajs.js.URIUtils
import scala.scalajs.js.annotation._
import scala.scalajs.js.Dynamic.literal
import org.scalajs.dom
import com.felstar.scalajs.vue._
import com.felstar.scalajs.vuetify.{Vuetify, VuetifyInstance}

final case class Example(
  id: String,
  title: String,
  subtitle: String,
  description: String,
  icon: String,
  file: String,
  component: js.Object
)

/** Entry points for the Vuetify examples:
  *  - `ScalaVue.main("#app")`: the gallery, an app shell (app bar, navigation drawer, hash routing) around all of them
  *  - `ScalaVue.single("#app")`: one example on its own page, picked by the `?demo=<id>` query parameter
  */
@JSExportTopLevel("ScalaVue")
object Main {

  private val sourceBase = "https://github.com/fancellu/scalajs-vue/blob/master/examples/src/main/scala/example/vuetify/"

  val examples: Seq[Example] = Seq(
    Example("todos", "Todos", "Lists, filters, transitions",
      "Options API data, computed values and methods, plus a deep watcher that keeps the list in localStorage.",
      "mdi-format-list-checks", "Todos.scala", Todos.component),
    Example("basics", "Basics", "Bindings and directives",
      "v-model, methods used as filters, a custom directive, a child component with a slot, and class and style bindings.",
      "mdi-school-outline", "Basics.scala", Basics.component),
    Example("temperature", "Temperature", "Composition API",
      "Two-way conversion where both fields are writable computed values built in setup() from Scala.",
      "mdi-thermometer", "Temperature.scala", Temperature.component),
    Example("timer", "Timer", "Lifecycle hooks and timers",
      "onMounted starts an interval and onUnmounted clears it. Drag the duration while it runs.",
      "mdi-timer-outline", "Timer.scala", Timer.component),
    Example("grid", "Data grid", "Sort and filter a table",
      "Scala case classes rendered by v-data-table with search, sortable columns and paging.",
      "mdi-table", "LanguageGrid.scala", LanguageGrid.component),
    Example("fetch", "Fetching data", "fetch and Futures",
      "Loads the latest vuejs/core commits from the GitHub API, with loading and error states.",
      "mdi-cloud-download-outline", "FetchCommits.scala", FetchCommits.component),
    Example("circles", "Circle drawer", "SVG, undo and redo",
      "Undo and redo backed by an immutable Scala history that Vue never makes reactive.",
      "mdi-vector-circle", "CircleDrawer.scala", CircleDrawer.component)
  )

  @js.native
  trait Shell extends ComponentPublicInstance {
    var current: String = js.native
    var drawer: js.Any = js.native
  }

  private def known(id: String): String = if (examples.exists(_.id == id)) id else examples.head.id

  private def routeFromHash(): String = known(dom.window.location.hash.stripPrefix("#").stripPrefix("/"))

  /** `?demo=timer` -> "timer" */
  private def demoFromQuery(): String =
    known(
      dom.window.location.search.stripPrefix("?").split("&").toSeq
        .map(_.split("=", 2))
        .collectFirst { case Array("demo", value) => URIUtils.decodeURIComponent(value) }
        .getOrElse("")
    )

  /** Example title, description and source link; shared by the gallery and the single-example page. */
  private val header = """
    <header class="mb-6">
      <div class="d-flex align-center flex-wrap ga-4">
        <v-avatar color="primary" variant="tonal" rounded="lg" size="48">
          <v-icon :icon="example.icon"></v-icon>
        </v-avatar>
        <h1 class="text-headline-medium ma-0 flex-grow-1">{{ example.title }}</h1>
        <v-btn variant="tonal" prepend-icon="mdi-code-tags" :href="example.source" target="_blank" rel="noopener">
          {{ example.file }}
        </v-btn>
      </div>
      <p class="text-body-large text-medium-emphasis ma-0 mt-3">{{ example.description }}</p>
    </header>
  """

  private val singleTemplate = """
    <v-app>
      <v-main>
        <v-container class="page py-8">
          HEADER
          <component :is="example.component"></component>
        </v-container>
      </v-main>
    </v-app>
  """.replace("HEADER", header)

  private val galleryTemplate = """
    <v-app>
      <v-app-bar flat border="b">
        <v-app-bar-nav-icon aria-label="Toggle navigation" @click="drawer = !drawer"></v-app-bar-nav-icon>
        <v-app-bar-title>
          <span class="font-weight-bold">Scala.js</span>
          <span class="text-medium-emphasis"> + Vue</span>
        </v-app-bar-title>
        <template #append>
          <v-btn
            :icon="$vuetify.theme.current.dark ? 'mdi-weather-sunny' : 'mdi-weather-night'"
            :aria-label="$vuetify.theme.current.dark ? 'Switch to light theme' : 'Switch to dark theme'"
            @click="toggleTheme"></v-btn>
          <v-btn icon="mdi-github" href="https://github.com/fancellu/scalajs-vue" target="_blank" rel="noopener" aria-label="Project on GitHub"></v-btn>
        </template>
      </v-app-bar>

      <v-navigation-drawer v-model="drawer">
        <v-list nav>
          <v-list-subheader>Examples</v-list-subheader>
          <v-list-item
            v-for="e in examples"
            :key="e.id"
            :href="'#/' + e.id"
            :active="e.id === current"
            :prepend-icon="e.icon"
            :title="e.title"
            :subtitle="e.subtitle"
            color="primary"
            rounded="lg"></v-list-item>
        </v-list>
        <template #append>
          <div class="pa-4 text-body-small text-medium-emphasis">
            Vue {{ vueVersion }} · Vuetify {{ vuetifyVersion }}
          </div>
        </template>
      </v-navigation-drawer>

      <v-main>
        <v-container class="page py-8">
          HEADER
          <component :is="example.component" :key="example.id"></component>
        </v-container>
      </v-main>
    </v-app>
  """.replace("HEADER", header)

  private def createVuetify(): VuetifyInstance =
    Vuetify.createVuetify(
      literal(
        theme = literal(
          defaultTheme = "system",
          themes = literal(
            // tinted background so white cards stand off the page
            light = literal(colors = literal(background = "#F4F5F7", primary = "#C2261D", secondary = "#2E8B67")),
            dark = literal(colors = literal(primary = "#FF7A6E", secondary = "#42B883"))
          )
        )
      )
    )

  private lazy val examplesJs: js.Array[js.Object] = js.Array(examples.map { e =>
    literal(
      id = e.id,
      title = e.title,
      subtitle = e.subtitle,
      description = e.description,
      icon = e.icon,
      file = e.file,
      source = sourceBase + e.file,
      // component definitions must not be made reactive
      component = Vue.markRaw(e.component)
    ): js.Object
  }: _*)

  private def exampleById(id: String): js.Dynamic =
    examplesJs.find(_.asInstanceOf[js.Dynamic].id.asInstanceOf[String] == id).get.asInstanceOf[js.Dynamic]

  /** One example on its own page (`demo-dev.html?demo=timer`); used by the per-demo sbt commands. */
  @JSExport
  def single(selector: String): ComponentPublicInstance = {
    val example = exampleById(demoFromQuery())
    dom.document.title = s"${example.title} · Scala.js + Vue"
    val app = Vue.createApp(
      literal(
        template = singleTemplate,
        data = (() => literal(example = example)): js.Function0[js.Object]
      )
    )
    app.use(createVuetify())
    app.mount(selector)
  }

  /** The gallery: `ScalaVue.main("#app")`. Returns the root instance for poking at from JS. */
  @JSExport
  def main(selector: String): ComponentPublicInstance = {
    val vuetify = createVuetify()

    val app = Vue.createApp(
      literal(
        template = galleryTemplate,
        data = (() =>
          literal(
            drawer = null, // null lets Vuetify decide: open on desktop, closed on phones
            current = routeFromHash(),
            examples = examplesJs,
            vueVersion = Vue.version,
            vuetifyVersion = Vuetify.version
          )): js.Function0[js.Object],
        computed = literal(
          example = ((vm: Shell) => exampleById(vm.current)): js.ThisFunction0[Shell, js.Dynamic]
        ),
        methods = literal(
          toggleTheme = (() => {
            val theme = vuetify.theme
            theme.change(if (theme.current.value.dark.asInstanceOf[Boolean]) "light" else "dark")
          }): js.Function0[js.Promise[Unit]]
        ),
        watch = literal(
          current = literal(
            immediate = true,
            handler = ((id: String) => dom.document.title = s"${exampleById(id).title} · Scala.js + Vue"): js.Function1[String, Unit]
          )
        ),
        mounted = ((vm: Shell) =>
          dom.window.addEventListener("hashchange", (_: dom.Event) => {
            vm.current = routeFromHash()
            dom.window.scrollTo(0, 0)
            // on phones the drawer is an overlay: close it once an example is picked
            if (vm.asInstanceOf[js.Dynamic].$vuetify.display.mobile.asInstanceOf[Boolean]) vm.drawer = false
          })): js.ThisFunction0[Shell, Unit]
      )
    )

    app.use(vuetify)
    app.mount(selector)
  }
}
