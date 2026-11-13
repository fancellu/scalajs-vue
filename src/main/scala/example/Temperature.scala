package example

import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal
import com.felstar.scalajs.vue._

/** 7GUIs temperature converter, written with the Composition API: `setup` builds refs and
  * writable computed values in Scala and returns them to the template.
  */
object Temperature {

  private def round1(d: Double): Double = math.round(d * 10) / 10.0

  private final case class Feel(label: String, color: String, icon: String)

  private def feel(celsius: Double): Feel = celsius match {
    case c if c <= 0 => Feel("Freezing", "blue", "mdi-snowflake")
    case c if c < 12 => Feel("Cold", "light-blue", "mdi-weather-windy")
    case c if c < 24 => Feel("Comfortable", "secondary", "mdi-weather-partly-cloudy")
    case c if c < 32 => Feel("Warm", "orange", "mdi-weather-sunny")
    case _           => Feel("Hot", "red", "mdi-fire")
  }

  /** Number fields emit null while cleared; keep the last good value instead. */
  private def whenNumber(update: Double => Unit): Any => Unit = {
    case d: Double if !d.isNaN => update(d)
    case _                     => ()
  }

  private val template = """
    <v-card border flat rounded="lg">
      <v-card-text>
        <v-row>
          <v-col cols="12" sm="6">
            <v-number-input v-model="celsiusField" label="Celsius" suffix="°C" variant="outlined" :precision="1" hide-details></v-number-input>
          </v-col>
          <v-col cols="12" sm="6">
            <v-number-input v-model="fahrenheitField" label="Fahrenheit" suffix="°F" variant="outlined" :precision="1" hide-details></v-number-input>
          </v-col>
        </v-row>
        <v-slider v-model="celsius" :min="-30" :max="45" :step="0.5" color="primary" prepend-icon="mdi-thermometer" class="mt-6" hide-details aria-label="Celsius"></v-slider>
        <div class="d-flex justify-center mt-6">
          <v-chip :color="feel.color" :prepend-icon="feel.icon" size="large" variant="tonal">{{ feel.label }}</v-chip>
        </div>
      </v-card-text>
    </v-card>
  """

  val component: js.Object = literal(
    template = template,
    setup = (() => {
      val celsius = Vue.ref(20.0)
      val celsiusField = Vue.writableComputed(
        WritableComputedOptions[Any](() => round1(celsius.value), whenNumber(c => celsius.value = c)))
      val fahrenheitField = Vue.writableComputed(
        WritableComputedOptions[Any](() => round1(celsius.value * 9 / 5 + 32), whenNumber(f => celsius.value = (f - 32) * 5 / 9)))
      val feelNow = Vue.computed(() => {
        val f = feel(celsius.value)
        literal(label = f.label, color = f.color, icon = f.icon)
      })
      literal(celsius = celsius, celsiusField = celsiusField, fahrenheitField = fahrenheitField, feel = feelNow)
    }): js.Function0[js.Object]
  )
}
