package example.plain

import scala.scalajs.js
import scala.scalajs.js.annotation._
import scala.scalajs.js.Dynamic.literal
import com.felstar.scalajs.vue._

/** Plain Vue, no Vuetify: the page (hello-dev.html) loads only Vue and a few lines of CSS.
  * Uses the Composition API: `setup` returns refs, a computed value and a function to the template.
  */
@JSExportTopLevel("HelloWorld")
object HelloWorld {

  private val template = """
    <main class="hello">
      <h1>{{ message }}</h1>

      <label for="message">Message</label>
      <input id="message" v-model="message" autocomplete="off">

      <p>Reversed in Scala: <strong>{{ reversed }}</strong></p>

      <p>
        <button @click="count++">Clicked {{ count }} {{ count === 1 ? 'time' : 'times' }}</button>
        <button class="secondary" @click="reset">Reset</button>
      </p>

      <p class="note">Rendered by Vue {{ vueVersion }} from Scala.js. No Vuetify on this page.</p>
    </main>
  """

  @JSExport
  def main(selector: String): ComponentPublicInstance =
    Vue
      .createApp(
        literal(
          template = template,
          setup = (() => {
            val message = Vue.ref("Hello Vue!")
            val count = Vue.ref(0)
            val reversed = Vue.computed(() => message.value.reverse)
            literal(
              message = message,
              count = count,
              reversed = reversed,
              vueVersion = Vue.version,
              reset = (() => { message.value = "Hello Vue!"; count.value = 0 }): js.Function0[Unit]
            )
          }): js.Function0[js.Object]
        )
      )
      .mount(selector)
}
