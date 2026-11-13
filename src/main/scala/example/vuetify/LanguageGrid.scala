package example.vuetify

import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal
import com.felstar.scalajs.vue._

/** Grid with sort and filter: the rows start life as Scala case classes and are converted to plain
  * JS objects for Vuetify's v-data-table, which does the searching, sorting and paging.
  */
object LanguageGrid {

  final case class Language(name: String, year: Int, creator: String, paradigm: String, typing: String) {
    def toJs: js.Object =
      literal(name = name, year = year, creator = creator, paradigm = paradigm, typing = typing)
  }

  val languages: Seq[Language] = Seq(
    Language("Scala", 2004, "Martin Odersky", "Functional + OO", "Static"),
    Language("JavaScript", 1995, "Brendan Eich", "Multi-paradigm", "Dynamic"),
    Language("TypeScript", 2012, "Anders Hejlsberg", "Multi-paradigm", "Static"),
    Language("Java", 1995, "James Gosling", "Object-oriented", "Static"),
    Language("Kotlin", 2011, "JetBrains", "Multi-paradigm", "Static"),
    Language("Haskell", 1990, "Haskell committee", "Functional", "Static"),
    Language("OCaml", 1996, "Inria", "Functional", "Static"),
    Language("F#", 2005, "Don Syme", "Functional", "Static"),
    Language("Elm", 2012, "Evan Czaplicki", "Functional", "Static"),
    Language("Clojure", 2007, "Rich Hickey", "Functional", "Dynamic"),
    Language("Erlang", 1986, "Joe Armstrong et al.", "Functional", "Dynamic"),
    Language("Elixir", 2012, "José Valim", "Functional", "Dynamic"),
    Language("Python", 1991, "Guido van Rossum", "Multi-paradigm", "Dynamic"),
    Language("Ruby", 1995, "Yukihiro Matsumoto", "Object-oriented", "Dynamic"),
    Language("Go", 2009, "Griesemer, Pike, Thompson", "Concurrent", "Static"),
    Language("Swift", 2014, "Chris Lattner", "Multi-paradigm", "Static"),
    Language("C#", 2000, "Anders Hejlsberg", "Multi-paradigm", "Static")
  )

  private val rows: js.Array[js.Object] = js.Array(languages.map(_.toJs): _*)

  @js.native
  trait Vm extends ComponentPublicInstance {
    var typing: String = js.native
  }

  private val template = """
    <v-card border flat rounded="lg">
      <div class="d-flex align-center flex-wrap ga-3 pa-4">
        <v-text-field
          v-model="search"
          label="Search"
          prepend-inner-icon="mdi-magnify"
          variant="outlined"
          density="compact"
          hide-details
          single-line
          clearable
          class="search-field"></v-text-field>
        <v-spacer></v-spacer>
        <v-btn-toggle v-model="typing" mandatory density="compact" variant="outlined" divided color="primary">
          <v-btn value="all">All</v-btn>
          <v-btn value="Static">Static</v-btn>
          <v-btn value="Dynamic">Dynamic</v-btn>
        </v-btn-toggle>
      </div>
      <v-divider></v-divider>
      <v-data-table
        :headers="headers"
        :items="filtered"
        :search="search"
        v-model:sort-by="sortBy"
        :items-per-page="10"
        mobile-breakpoint="sm"
        hover>
        <template #item.name="{ value }">
          <span class="font-weight-medium">{{ value }}</span>
        </template>
        <template #item.typing="{ value }">
          <v-chip size="small" variant="tonal" :color="value === 'Static' ? 'primary' : 'secondary'">{{ value }}</v-chip>
        </template>
      </v-data-table>
    </v-card>
  """

  val component: js.Object = literal(
    template = template,
    data = (() =>
      literal(
        search = "",
        typing = "all",
        sortBy = js.Array(literal(key = "year", order = "asc")),
        headers = js.Array(
          literal(title = "Language", key = "name"),
          literal(title = "Appeared", key = "year", align = "end"),
          literal(title = "Creator", key = "creator"),
          literal(title = "Paradigm", key = "paradigm"),
          literal(title = "Typing", key = "typing")
        )
      )): js.Function0[js.Object],
    computed = literal(
      filtered = ((vm: Vm) =>
        if (vm.typing == "all") rows
        else rows.filter(_.asInstanceOf[js.Dynamic].typing.asInstanceOf[String] == vm.typing)): js.ThisFunction0[Vm, js.Array[js.Object]]
    )
  )
}
