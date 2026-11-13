package com.felstar.scalajs.vuetify

import scala.scalajs.js
import scala.scalajs.js.annotation._
import com.felstar.scalajs.vue.Ref

/** Scala.js facade for the Vuetify 4 UMD build (`dist/vuetify.js`), which exposes the `Vuetify` global.
  * Load it with a `<script>` tag after Vue, plus `dist/vuetify.css` and the MDI icon font.
  *
  * {{{
  * val vuetify = Vuetify.createVuetify(literal(theme = literal(defaultTheme = "system")))
  * app.use(vuetify)
  * }}}
  * The UMD build's `createVuetify` registers every Vuetify component and directive on the app.
  */
@js.native
@JSGlobal("Vuetify")
object Vuetify extends js.Object {
  val version: String = js.native
  def createVuetify(options: js.Object = js.native): VuetifyInstance = js.native
}

/** The plugin returned by `createVuetify`; pass it to `app.use`. */
@js.native
trait VuetifyInstance extends js.Object {
  val theme: ThemeInstance = js.native
}

@js.native
trait ThemeInstance extends js.Object {
  /** Selected theme name; `"system"` follows the OS preference. */
  val name: Ref[String] = js.native
  /** Whether the selected theme is `"system"`. */
  val isSystem: Ref[Boolean] = js.native
  /** The theme actually in use, resolved from `"system"` when needed. */
  val current: Ref[js.Dynamic] = js.native
  def change(themeName: String): js.Promise[Unit] = js.native
  /** Switches between `themes` (default `["light", "dark"]`). */
  def toggle(themes: js.Array[String] = js.native): js.Promise[Unit] = js.native
  def cycle(themes: js.Array[String] = js.native): js.Promise[Unit] = js.native
}
