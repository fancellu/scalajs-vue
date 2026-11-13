package example.vuetify

import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal
import org.scalajs.dom
import com.felstar.scalajs.vue._

/** 7GUIs circle drawer. The model and its undo/redo history are immutable Scala values held outside
  * Vue's reactivity (`Vue.markRaw`); after every change the current circles are copied into reactive
  * data for the SVG to render.
  */
object CircleDrawer {

  final case class Circle(id: Int, x: Double, y: Double, r: Double)

  final case class History(past: List[Vector[Circle]], present: Vector[Circle], future: List[Vector[Circle]]) {
    def commit(next: Vector[Circle]): History = History(present :: past, next, Nil)
    def undo: History = past match {
      case previous :: rest => History(rest, previous, present :: future)
      case Nil              => this
    }
    def redo: History = future match {
      case next :: rest => History(present :: past, next, rest)
      case Nil          => this
    }
  }

  /** Mutable box for the immutable history; marked raw so Vue never proxies it. */
  final class Store(var history: History, var lastId: Int)

  @js.native
  trait Vm extends ComponentPublicInstance {
    val store: js.Any = js.native
    var circles: js.Array[js.Object] = js.native
    var canUndo: Boolean = js.native
    var canRedo: Boolean = js.native
    var selected: Int = js.native
    var dialog: Boolean = js.native
    var radius: Double = js.native
  }

  private val width = 600.0
  private val height = 360.0
  private val defaultRadius = 20.0

  private def store(vm: Vm): Store = vm.store.asInstanceOf[Store]

  private def show(vm: Vm, circles: Vector[Circle]): Unit =
    vm.circles = js.Array(circles.map(c => literal(id = c.id, x = c.x, y = c.y, r = c.r): js.Object): _*)

  private def update(vm: Vm, history: History): Unit = {
    store(vm).history = history
    show(vm, history.present)
    vm.canUndo = history.past.nonEmpty
    vm.canRedo = history.future.nonEmpty
  }

  private def resized(vm: Vm): Vector[Circle] =
    store(vm).history.present.map(c => if (c.id == vm.selected) c.copy(r = vm.radius) else c)

  private val template = """
    <v-card border flat rounded="lg">
      <div class="d-flex align-center flex-wrap ga-2 pa-3">
        <v-btn variant="tonal" prepend-icon="mdi-undo" :disabled="!canUndo" @click="undo">Undo</v-btn>
        <v-btn variant="tonal" prepend-icon="mdi-redo" :disabled="!canRedo" @click="redo">Redo</v-btn>
        <v-btn variant="text" prepend-icon="mdi-resize" :disabled="selected < 0" @click="openAdjust">Adjust diameter</v-btn>
        <v-spacer></v-spacer>
        <span class="text-body-medium text-medium-emphasis">{{ circles.length }} circles</span>
      </div>
      <v-divider></v-divider>

      <svg viewBox="0 0 600 360" class="d-block w-100 bg-surface-light drawing" role="img" aria-label="Drawing area" @click="addAt">
        <circle
          v-for="c in circles"
          :key="c.id"
          :cx="c.x"
          :cy="c.y"
          :r="c.r"
          fill="rgb(var(--v-theme-primary))"
          :fill-opacity="c.id === selected ? 0.35 : 0.04"
          stroke="rgb(var(--v-theme-primary))"
          stroke-width="2"
          class="circle"
          @click.stop="selected = c.id"
          @contextmenu.prevent="selected = c.id; openAdjust()"></circle>
      </svg>

      <div class="text-body-small text-medium-emphasis pa-3">
        Click empty space to add a circle. Click a circle to select it; right-click it (or use Adjust diameter) to resize.
      </div>

      <v-dialog v-model="dialog" max-width="420">
        <v-card title="Adjust diameter" rounded="lg">
          <v-card-text>
            <v-slider v-model="radius" :min="5" :max="120" :step="1" color="primary" hide-details aria-label="Radius">
              <template #append><span class="tabular">{{ radius * 2 }}px</span></template>
            </v-slider>
          </v-card-text>
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn color="primary" variant="flat" @click="dialog = false">Done</v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>
    </v-card>
  """

  val component: js.Object = literal(
    template = template,
    data = (() =>
      literal(
        store = Vue.markRaw(new Store(History(Nil, Vector.empty, Nil), 0).asInstanceOf[js.Any]),
        circles = js.Array[js.Object](),
        canUndo = false,
        canRedo = false,
        selected = -1,
        dialog = false,
        radius = defaultRadius
      )): js.Function0[js.Object],
    methods = literal(
      addAt = ((vm: Vm, e: dom.MouseEvent) => {
        val rect = e.currentTarget.asInstanceOf[dom.Element].getBoundingClientRect()
        val x = (e.clientX - rect.left) * width / rect.width
        val y = (e.clientY - rect.top) * height / rect.height
        val s = store(vm)
        s.lastId += 1
        update(vm, s.history.commit(s.history.present :+ Circle(s.lastId, x, y, defaultRadius)))
        vm.selected = s.lastId
      }): js.ThisFunction1[Vm, dom.MouseEvent, Unit],
      openAdjust = ((vm: Vm) =>
        store(vm).history.present.find(_.id == vm.selected).foreach { c =>
          vm.radius = c.r
          vm.dialog = true
        }): js.ThisFunction0[Vm, Unit],
      undo = ((vm: Vm) => update(vm, store(vm).history.undo)): js.ThisFunction0[Vm, Unit],
      redo = ((vm: Vm) => update(vm, store(vm).history.redo)): js.ThisFunction0[Vm, Unit]
    ),
    watch = literal(
      // live preview while the slider moves; nothing enters the history yet
      radius = ((vm: Vm) => if (vm.dialog) show(vm, resized(vm))): js.ThisFunction0[Vm, Unit],
      // closing the dialog commits the resize as one undoable step
      dialog = ((vm: Vm, open: Boolean) =>
        if (!open) {
          val next = resized(vm)
          if (next != store(vm).history.present) update(vm, store(vm).history.commit(next))
        }): js.ThisFunction1[Vm, Boolean, Unit]
    )
  )
}
