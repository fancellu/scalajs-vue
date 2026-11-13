package example.vuetify

import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal
import scala.util.Try
import org.scalajs.dom
import com.felstar.scalajs.vue._

/** Options API: data, computed, methods and a deep watcher that saves the list to localStorage. */
object Todos {

  @js.native
  trait Todo extends js.Object {
    val id: Int = js.native
    var done: Boolean = js.native
    var content: String = js.native
  }

  @js.native
  trait Vm extends ComponentPublicInstance {
    var newTodo: String = js.native
    var filter: String = js.native
    var todos: js.Array[Todo] = js.native
  }

  private val storageKey = "scalajs-vue-todos"
  private var lastId = 0

  private def todo(content: String, done: Boolean = false): Todo = {
    lastId += 1
    literal(id = lastId, done = done, content = content).asInstanceOf[Todo]
  }

  private def initialTodos(): js.Array[Todo] = {
    val saved = Try(Option(dom.window.localStorage.getItem(storageKey))).toOption.flatten
      .flatMap(json => Try(js.JSON.parse(json).asInstanceOf[js.Array[Todo]]).toOption)
    saved match {
      case Some(todos) =>
        lastId = todos.foldLeft(lastId)((max, t) => max max t.id)
        todos
      case None =>
        js.Array(todo("Learn JavaScript", done = true), todo("Learn Vue.js"), todo("Learn Scala.js"), todo("Try Vuetify"))
    }
  }

  private val template = """
    <v-card border flat rounded="lg">
      <v-card-text class="pb-2">
        <v-text-field
          v-model="newTodo"
          label="What needs doing?"
          variant="outlined"
          prepend-inner-icon="mdi-plus"
          hide-details
          @keyup.enter="add">
          <template #append-inner>
            <v-btn color="primary" variant="flat" :disabled="!newTodo.trim()" @click="add">Add</v-btn>
          </template>
        </v-text-field>
      </v-card-text>

      <div class="d-flex align-center flex-wrap ga-2 px-4">
        <v-chip-group v-model="filter" mandatory color="primary">
          <v-chip value="all" variant="outlined">All · {{ todos.length }}</v-chip>
          <v-chip value="active" variant="outlined">Active · {{ remaining }}</v-chip>
          <v-chip value="done" variant="outlined">Done · {{ todos.length - remaining }}</v-chip>
        </v-chip-group>
        <v-spacer></v-spacer>
        <v-btn variant="text" prepend-icon="mdi-swap-horizontal" :disabled="!todos.length" @click="flipAll">Flip all</v-btn>
        <v-btn variant="text" prepend-icon="mdi-broom" :disabled="remaining === todos.length" @click="clearDone">Clear done</v-btn>
      </div>

      <v-divider class="mt-2"></v-divider>

      <v-list class="py-0">
        <v-slide-x-transition group>
          <v-list-item v-for="todo in visible" :key="todo.id" @click="todo.done = !todo.done">
            <template #prepend>
              <v-list-item-action start>
                <v-checkbox-btn :model-value="todo.done" color="primary" tabindex="-1"></v-checkbox-btn>
              </v-list-item-action>
            </template>
            <v-list-item-title :class="{ 'text-decoration-line-through text-medium-emphasis': todo.done }">
              {{ todo.content }}
            </v-list-item-title>
            <template #append>
              <v-btn icon="mdi-close" variant="text" size="small" :aria-label="'Remove ' + todo.content" @click.stop="remove(todo.id)"></v-btn>
            </template>
          </v-list-item>
        </v-slide-x-transition>
      </v-list>

      <div v-if="!visible.length" class="text-center text-medium-emphasis pa-8">
        <v-icon icon="mdi-check-all" size="40" class="mb-2"></v-icon>
        <div>Nothing here</div>
      </div>
    </v-card>
  """

  val component: js.Object = literal(
    template = template,
    data = (() => literal(newTodo = "", filter = "all", todos = initialTodos())): js.Function0[js.Object],
    computed = literal(
      remaining = ((vm: Vm) => vm.todos.count(!_.done)): js.ThisFunction0[Vm, Int],
      visible = ((vm: Vm) =>
        vm.filter match {
          case "active" => vm.todos.filter(!_.done)
          case "done"   => vm.todos.filter(_.done)
          case _        => vm.todos
        }): js.ThisFunction0[Vm, js.Array[Todo]]
    ),
    methods = literal(
      add = ((vm: Vm) => {
        val content = vm.newTodo.trim
        if (content.nonEmpty) {
          vm.todos.push(todo(content))
          vm.newTodo = ""
        }
      }): js.ThisFunction0[Vm, Unit],
      // Vue 3 tracks splice and index assignment, no Vue.set / Vue.delete needed
      remove = ((vm: Vm, id: Int) => {
        val i = vm.todos.indexWhere(_.id == id)
        if (i >= 0) vm.todos.splice(i, 1)
      }): js.ThisFunction1[Vm, Int, Unit],
      flipAll = ((vm: Vm) => vm.todos.foreach(t => t.done = !t.done)): js.ThisFunction0[Vm, Unit],
      clearDone = ((vm: Vm) => vm.todos = vm.todos.filter(!_.done)): js.ThisFunction0[Vm, Unit]
    ),
    watch = literal(
      todos = literal(
        deep = true,
        handler = ((todos: js.Array[Todo]) =>
          Try(dom.window.localStorage.setItem(storageKey, js.JSON.stringify(todos)))): js.Function1[js.Array[Todo], Any]
      )
    )
  )
}
