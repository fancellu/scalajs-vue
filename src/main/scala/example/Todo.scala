package example

import scala.scalajs.js
import scala.scalajs.js.annotation._
import scala.scalajs.js.Dynamic.literal
import org.scalajs.dom
import com.felstar.scalajs.vue._

@JSExportTopLevel("Todo")
object Todo {

  @js.native
  trait DemoVue extends ComponentPublicInstance {
    var title: String = js.native
    var n: Double = js.native
    var todos: js.Array[DemoVueTodo] = js.native
  }

  type DemoVueMethod = js.ThisFunction0[DemoVue, _]

  @js.native
  trait DemoVueTodo extends js.Object {
    val id: Int = js.native
    var done: Boolean = js.native
    var content: String = js.native
  }

  object DemoVueTodo {
    private var lastId = 0
    def apply(done: Boolean, content: String): DemoVueTodo = {
      lastId += 1
      literal(id = lastId, done = done, content = content).asInstanceOf[DemoVueTodo]
    }
  }

  @JSExport
  // we return the DemoVue so we can use it back in JS
  def main(): DemoVue = {

    val tasks = js.Array("Learn JavaScript", "Learn Vue.js", "Learn Scala.js")

    def ts = new js.Date().toLocaleTimeString()

    val app = Vue.createApp(
      literal(
        // Vue 3: data must be a function returning a fresh object
        data = (() =>
          literal(
            message = "Hello Vue.js!!!!!",
            title = "Todo App",
            todos = tasks.map(content => DemoVueTodo(done = content == tasks.head, content = content)),
            barValue = 100,
            n = 0
          )): js.Function0[js.Object],
        // js.ThisFunction would be fine, just trying to be more type specific
        methods = literal(
          clickHandler = ((demoVue: DemoVue) => demoVue.n -= 1): DemoVueMethod,
          addTask = ((demoVue: DemoVue) => demoVue.todos.push(DemoVueTodo(false, s"new $ts"))): DemoVueMethod,
          // Vue 3 tracks plain index assignment and splice, so Vue.set/Vue.delete are gone
          change1st = ((demoVue: DemoVue) => demoVue.todos(0) = DemoVueTodo(false, ts)): DemoVueMethod,
          remove = ((demoVue: DemoVue, idx: Int) => demoVue.todos.splice(idx, 1)): js.ThisFunction1[DemoVue, Int, _],
          flipAll = ((demoVue: DemoVue) => demoVue.todos.foreach(td => td.done = !td.done)): DemoVueMethod,
          // Vue 3 dropped filters; plain methods called from the template do the same job
          reverse = (value: js.Any) => value.toString.reverse,
          wrap = (value: js.Any, begin: String, end: String) => begin + value.toString + end,
          extract = (array: js.Array[js.Dynamic], field: String) =>
            if (js.isUndefined(array)) array else array.map(_.selectDynamic(field))
        ),
        computed = literal(
          todosComputed = ((demoVue: DemoVue) => demoVue.todos.map(_.content)): js.ThisFunction0[DemoVue, js.Array[String]]
        )
      )
    )

    app.component(
      "my-component",
      literal(
        props = js.Array("myMsg"),
        template = "<p>A custom component with msg {{myMsg}} <slot>default content</slot></p>"
      )
    )

    // function shorthand: runs on mounted and updated
    app.directive(
      "mydirective",
      ((el: dom.HTMLElement, binding: DirectiveBinding) =>
        el.innerHTML = s"This comes from my-directive with value ${binding.value}"): DirectiveHook
    )

    val demoVue = app.mount("#demo").asInstanceOf[DemoVue]

    demoVue.$watch("title", (newValue: String, oldValue: String) => println(s"title changed from $oldValue to $newValue"))

    println(s"Mounted with Vue ${Vue.version}")

    demoVue
  }
}
