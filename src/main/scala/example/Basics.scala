package example

import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal
import org.scalajs.dom
import com.felstar.scalajs.vue._

/** The original demo's bindings: v-model, v-text, methods used as filters, a custom directive,
  * a child component with props and a slot, class/style bindings and click handlers.
  */
object Basics {

  @js.native
  trait Vm extends ComponentPublicInstance {
    var title: String = js.native
    var n: Int = js.native
    var barValue: Int = js.native
  }

  /** Child component, registered locally below. */
  private val infoCard = literal(
    props = js.Array("msg"),
    template = """
      <v-alert variant="tonal" color="secondary" icon="mdi-puzzle-outline" :title="'msg prop = ' + msg">
        <slot>default slot content</slot>
      </v-alert>
    """
  )

  /** `v-shout`: the function shorthand runs on both mounted and updated. */
  private val shout: DirectiveHook =
    (el: dom.HTMLElement, binding: DirectiveBinding) => el.textContent = s"${binding.value}".toUpperCase + "!"

  private val template = """
    <v-row>
      <v-col cols="12" md="6">
        <v-card border flat rounded="lg" class="h-100">
          <v-card-item prepend-icon="mdi-swap-horizontal" title="Two-way binding" subtitle="v-model, v-text and methods as filters"></v-card-item>
          <v-card-text>
            <v-text-field v-model="title" label="Title" variant="outlined" class="mb-2"></v-text-field>
            <v-table density="compact">
              <tbody>
                <tr><td class="text-medium-emphasis">v-text</td><td v-text="title"></td></tr>
                <tr><td class="text-medium-emphasis">reverse(title)</td><td>{{ reverse(title) }}</td></tr>
                <tr><td class="text-medium-emphasis">wrap(title, '«', '»')</td><td>{{ wrap(title, '«', '»') }}</td></tr>
                <tr><td class="text-medium-emphasis">v-shout directive</td><td v-shout="title"></td></tr>
              </tbody>
            </v-table>
          </v-card-text>
        </v-card>
      </v-col>

      <v-col cols="12" md="6">
        <v-card border flat rounded="lg" class="h-100">
          <v-card-item prepend-icon="mdi-gesture-tap" title="Events" subtitle="A Scala method and an inline expression"></v-card-item>
          <v-card-text class="text-center">
            <div class="text-display-medium font-weight-medium my-4 tabular">{{ n }}</div>
            <div class="d-flex justify-center flex-wrap ga-2">
              <v-btn variant="tonal" prepend-icon="mdi-minus" @click="decrement">Scala decrement</v-btn>
              <v-btn color="primary" variant="flat" prepend-icon="mdi-plus" @click="n++">Inline n++</v-btn>
            </div>
          </v-card-text>
        </v-card>
      </v-col>

      <v-col cols="12" md="6">
        <v-card border flat rounded="lg" class="h-100">
          <v-card-item prepend-icon="mdi-palette-outline" title="Class and style bindings" subtitle=":style width with a CSS transition"></v-card-item>
          <v-card-text>
            <div class="d-flex align-center ga-2 mb-4">
              <v-btn icon="mdi-minus" variant="tonal" size="small" aria-label="Shrink" @click="barValue = Math.max(0, barValue - 20)"></v-btn>
              <v-btn icon="mdi-plus" variant="tonal" size="small" aria-label="Grow" @click="barValue = Math.min(100, barValue + 20)"></v-btn>
              <span class="text-body-large ml-2 tabular">{{ barValue }}%</span>
            </div>
            <div class="rounded-pill bg-surface-variant overflow-hidden" style="height: 16px">
              <div class="h-100 rounded-pill bar" :class="barValue >= 80 ? 'bg-secondary' : 'bg-primary'" :style="{ width: barValue + '%' }"></div>
            </div>
          </v-card-text>
        </v-card>
      </v-col>

      <v-col cols="12" md="6">
        <v-card border flat rounded="lg" class="h-100">
          <v-card-item prepend-icon="mdi-puzzle-outline" title="Child component" subtitle="A prop and a slot, registered from Scala"></v-card-item>
          <v-card-text class="d-flex flex-column ga-3">
            <info-card msg="hello">Special content passed through the slot</info-card>
            <info-card msg="no slot content"></info-card>
          </v-card-text>
        </v-card>
      </v-col>

      <v-col cols="12">
        <v-card border flat rounded="lg">
          <v-card-item prepend-icon="mdi-code-json" title="Component data" subtitle="$data, updated live"></v-card-item>
          <v-card-text>
            <pre class="code-block">{{ $data }}</pre>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  """

  val component: js.Object = literal(
    template = template,
    components = literal("info-card" -> infoCard),
    directives = literal(shout = shout),
    data = (() => literal(title = "Hello Scala.js", n = 0, barValue = 40)): js.Function0[js.Object],
    methods = literal(
      // Vue 3 dropped filters; plain methods called from the template do the same job
      reverse = (value: js.Any) => value.toString.reverse,
      wrap = (value: js.Any, begin: String, end: String) => begin + value.toString + end,
      decrement = ((vm: Vm) => vm.n -= 1): js.ThisFunction0[Vm, Unit]
    ),
    mounted = ((vm: Vm) => {
      vm.$watch("title", (newValue: String, oldValue: String) => println(s"title changed from $oldValue to $newValue"))
      ()
    }): js.ThisFunction0[Vm, Unit]
  )
}
