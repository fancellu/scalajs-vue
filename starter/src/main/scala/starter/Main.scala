package starter

import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal
import com.felstar.scalajs.vue._
import com.felstar.scalajs.vuetify.Vuetify

/** Mounts a small Vuetify app on #app. Start editing here.
  *
  * The component uses the Composition API: `setup` creates reactive state with `Vue.ref` /
  * `Vue.computed` and returns what the template needs. The template is compiled in the browser
  * by vue.global.js, so it can use Vuetify components directly.
  */
object Main {

  private val template = """
    <v-app>
      <v-main>
        <v-container class="py-12" style="max-width: 560px">
          <v-card rounded="lg" border flat>
            <v-card-item prepend-icon="mdi-hand-wave" :title="'Hello, ' + (name || 'stranger') + '!'" subtitle="Scala.js + Vue 3 + Vuetify 4"></v-card-item>
            <v-card-text>
              <v-text-field v-model="name" label="Your name" variant="outlined" class="mb-4"></v-text-field>
              <div class="d-flex align-center ga-3">
                <v-btn color="primary" variant="flat" prepend-icon="mdi-plus" @click="count++">Clicked {{ count }} {{ count === 1 ? 'time' : 'times' }}</v-btn>
                <v-btn variant="text" @click="count = 0">Reset</v-btn>
              </div>
              <p class="mt-4 mb-0 text-medium-emphasis">Doubled in Scala: {{ doubled }}</p>
            </v-card-text>
            <v-card-actions>
              <v-spacer></v-spacer>
              <v-btn variant="text" prepend-icon="mdi-theme-light-dark" @click="toggleTheme">Toggle theme</v-btn>
            </v-card-actions>
          </v-card>
        </v-container>
      </v-main>
    </v-app>
  """

  def main(args: Array[String]): Unit = {
    val vuetify = Vuetify.createVuetify(literal(theme = literal(defaultTheme = "system")))

    val app = Vue.createApp(
      literal(
        template = template,
        setup = (() => {
          val name = Vue.ref("")
          val count = Vue.ref(0)
          val doubled = Vue.computed(() => count.value * 2)
          literal(
            name = name,
            count = count,
            doubled = doubled,
            toggleTheme = (() =>
              vuetify.theme.change(if (vuetify.theme.current.value.dark.asInstanceOf[Boolean]) "light" else "dark")
            ): js.Function0[js.Promise[Unit]]
          )
        }): js.Function0[js.Object]
      )
    )

    app.use(vuetify)
    app.mount("#app")
  }
}
