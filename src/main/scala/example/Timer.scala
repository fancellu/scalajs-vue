package example

import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal
import scala.scalajs.js.timers.{clearInterval, setInterval, SetIntervalHandle}
import com.felstar.scalajs.vue._

/** 7GUIs timer: `onMounted` starts an interval and `onUnmounted` clears it, so leaving the page stops it.
  * The interval handle is a plain Scala `var` captured by the closures; no need to put it on the component.
  */
object Timer {

  private val template = """
    <v-card border flat rounded="lg">
      <v-card-text>
        <div class="d-flex align-baseline justify-space-between mb-3">
          <span class="text-title-medium">Elapsed</span>
          <span class="text-display-small font-weight-medium tabular">{{ (elapsed / 1000).toFixed(1) }}s</span>
        </div>
        <v-progress-linear :model-value="progress" :color="done ? 'secondary' : 'primary'" height="12" rounded></v-progress-linear>

        <div class="d-flex align-baseline justify-space-between mt-8">
          <span class="text-title-medium">Duration</span>
          <span class="text-body-large tabular">{{ (duration / 1000).toFixed(1) }}s</span>
        </div>
        <v-slider v-model="duration" :min="1000" :max="30000" :step="500" color="primary" hide-details aria-label="Duration"></v-slider>
      </v-card-text>
      <v-card-actions class="px-4 pb-4">
        <v-btn color="primary" variant="flat" prepend-icon="mdi-restart" @click="reset">Reset</v-btn>
        <v-chip v-if="done" color="secondary" variant="tonal" prepend-icon="mdi-check" class="ml-2">Done</v-chip>
      </v-card-actions>
    </v-card>
  """

  val component: js.Object = literal(
    template = template,
    setup = (() => {
      val elapsed = Vue.ref(0.0)
      val duration = Vue.ref(15000.0)
      val progress = Vue.computed(() => math.min(100.0, elapsed.value / duration.value * 100))
      val done = Vue.computed(() => elapsed.value >= duration.value)

      var handle: Option[SetIntervalHandle] = None

      Vue.onMounted(() => {
        var last = js.Date.now()
        handle = Some(setInterval(100) {
          val now = js.Date.now()
          elapsed.value = math.min(duration.value, elapsed.value + (now - last))
          last = now
        })
      })

      Vue.onUnmounted(() => {
        handle.foreach(clearInterval)
        println("Timer: interval cleared")
      })

      literal(
        elapsed = elapsed,
        duration = duration,
        progress = progress,
        done = done,
        reset = (() => elapsed.value = 0): js.Function0[Unit]
      )
    }): js.Function0[js.Object]
  )
}
