---
name: scalajs-vue-facades
description: Writing Scala.js code for Vue 3 and Vuetify 4 without a bundler, in any project. Use when writing or extending Scala.js facades for Vue or Vuetify, defining Vue components from Scala (Options API or Composition API), wiring templates, reactivity, watchers, directives, async fetch, timers or theming, or testing such pages in headless Chrome. Covers the type ascriptions Scala.js needs, the pitfalls that compile but misbehave, and Vue 2 to Vue 3 differences.
---

# Scala.js + Vue 3 (+ Vuetify 4), no bundler

Verified with Scala 2.13 / Scala.js 1.22 / Vue 3.5 / Vuetify 4.3. Reference implementation:
https://github.com/fancellu/scalajs-vue (`com.felstar.scalajs.vue`, `com.felstar.scalajs.vuetify`, `example.*`).

## Setup

- Load the global builds before your linked JS: `vue.global.js` (dev, includes the template compiler) or
  `vue.global.prod.js`, then optionally `vuetify.js` (UMD, defines `Vuetify`) and its CSS, then `main.js`.
- Facades bind to the globals with `@JSGlobal("Vue")` / `@JSGlobal("Vuetify")`; library users need no bundler.
- Export the entry point: `@JSExportTopLevel("ScalaVue") object Main { @JSExport def main(selector: String) = ... }`,
  then call `ScalaVue.main("#app")` from a later `<script>`. Scala.js 1.x forbids dots in export names
  (`"example.Todo"` no longer works). With the default `NoModule` output, exports are top-level bindings visible to later scripts.
- Put templates in Scala strings (`template = """..."""`), compiled at runtime. This avoids in-DOM template limits
  (no self-closing custom tags, lower-cased attributes, table parsing).

## Writing facades

```scala
@js.native @JSGlobal("Vue")
object Vue extends js.Object {
  def createApp(root: js.Object): App = js.native
  def ref[T](value: T): Ref[T] = js.native
  def computed[T](getter: js.Function0[T]): Ref[T] = js.native
  @JSName("computed")                       // same JS function, own Scala name:
  def writableComputed[T](o: WritableComputedOptions[T]): Ref[T] = js.native  // lambdas can't choose between overloads
  def watch(src: js.Any, cb: js.Function2[_, _, Any], options: js.Object = js.native): WatchHandle = js.native
  def markRaw[T <: js.Any](value: T): T = js.native
  def onMounted(hook: js.Function0[Any]): Unit = js.native
}
@js.native trait Ref[T] extends js.Object { var value: T = js.native }
@js.native trait App extends js.Object { def mount(target: String | dom.Element): ComponentPublicInstance = js.native }
```

- `= js.native` on every member; `= js.native` as a parameter default means "omit when not given".
- A `@js.native` object can't have a Scala companion: put type aliases in a `package object`.
- Callback params as `js.Function2[_, _, Any]` accept Scala lambdas with any concrete parameter types.
- Keep facades honest to the current API; drop members Vue 3 removed rather than leaving them to fail at runtime.

## Options API components

```scala
@js.native trait Vm extends ComponentPublicInstance {   // typed `this`
  var todos: js.Array[Todo] = js.native
  var filter: String = js.native
}

val component: js.Object = literal(
  template = """<v-list><v-list-item v-for="t in visible" :key="t.id" @click="toggle(t.id)">{{ t.content }}</v-list-item></v-list>""",
  data = (() => literal(filter = "all", todos = js.Array[Todo]())): js.Function0[js.Object],
  computed = literal(
    visible = ((vm: Vm) => if (vm.filter == "all") vm.todos else vm.todos.filter(_.done)): js.ThisFunction0[Vm, js.Array[Todo]]
  ),
  methods = literal(
    toggle = ((vm: Vm, id: Int) => vm.todos.find(_.id == id).foreach(t => t.done = !t.done)): js.ThisFunction1[Vm, Int, Unit],
    reverse = (s: String) => s.reverse                       // no `this` needed: plain function
  ),
  watch = literal(
    todos = literal(deep = true, handler = ((ts: js.Array[Todo]) => save(ts)): js.Function1[js.Array[Todo], Any])
  ),
  components = literal("info-card" -> infoCard),             // local registration, kebab-case keys
  directives = literal(shout = shoutHook)                    // v-shout
)
```

- Always ascribe function types inside `literal(...)`: `js.Function0[js.Object]` for `data`, `js.ThisFunctionN[Vm, ...]`
  where Vue passes the instance as `this` (methods, computed getters, watch handlers, lifecycle hooks).
- Inside `literal(...)` the expected type is `js.Any`, so `opt.getOrElse("")` or `if (c) dyn else ""` infer `Object`
  and fail to compile ("found Object, required js.Any"). Bind to a typed `val x: String = ...` first.
- `data` must be a function returning a fresh object (Vue 3).
- To store a value on the instance outside `data`, prefer a Scala closure (Composition API) over mutating the proxy.

## Composition API

```scala
setup = (() => {
  val celsius = Vue.ref(20.0)
  val fahrenheit = Vue.writableComputed(WritableComputedOptions[Any](
    () => celsius.value * 9 / 5 + 32,
    { case f: Double if !f.isNaN => celsius.value = (f - 32) * 5 / 9; case _ => () }))  // fields emit null when cleared
  var handle: Option[SetIntervalHandle] = None                                         // plain Scala state
  Vue.onMounted(() => handle = Some(js.timers.setInterval(100)(tick())))
  Vue.onUnmounted(() => handle.foreach(js.timers.clearInterval))
  literal(celsius = celsius, fahrenheit = fahrenheit, reset = (() => celsius.value = 0): js.Function0[Unit])
}): js.Function0[js.Object]
```

Refs returned from `setup` are unwrapped in the template (`v-model="fahrenheit"` calls the setter).

## Reactivity with Scala data

- Vue 3 tracks `js.Array` `push`, `splice` and index assignment: no `Vue.set` / `Vue.delete`. Scala `ArrayOps`
  (`filter`, `foreach`, `count`, `indexWhere`) work on reactive arrays.
- Convert case classes explicitly: `def toJs: js.Object = literal(name = name, year = year)`.
- Keep immutable Scala models (e.g. undo/redo history of `Vector[Shape]`) out of reactivity: hold them in a mutable box
  stored as `Vue.markRaw(box.asInstanceOf[js.Any])` in `data`, and copy the current view into reactive arrays after each change.
- Component definitions passed to `<component :is>` must be `Vue.markRaw`'d if they sit in reactive data.
- Parse JSON as `js.Dynamic`; check `x != null && !js.isUndefined(x)` before reading nested fields.

## Async and timers

```scala
import scala.scalajs.js.Thenable.Implicits._
private implicit val ec: ExecutionContext = JSExecutionContext.queue   // avoids the global-EC compiler warning
dom.fetch(url).toFuture
  .flatMap(r => if (r.ok) r.json().toFuture else Future.failed(new Exception(s"HTTP ${r.status}")))
  .onComplete { result => if (request == latestRequest) { /* update vm */ } }   // ignore stale responses
```

Use `js.timers.setInterval` / `clearInterval`, and always clear in `onUnmounted` / `unmounted`.

## Vue 2 -> Vue 3 checklist

`new Vue(...)` -> `Vue.createApp(...).mount(sel)`; global `component`/`directive`/`use` move to the `App`; filters
-> methods (`{{ reverse(title) }}`); `$on`/`$off`/`$once`, `$children`, `$destroy`, `$set`/`$delete` are gone;
directives get `(el, binding: DirectiveBinding)` with no `expression`; transition class `.x-enter` -> `.x-enter-from`;
`:key` on a stable id, not the object.

## Vuetify 4 from Scala

```scala
val vuetify = Vuetify.createVuetify(literal(theme = literal(
  defaultTheme = "system",
  themes = literal(light = literal(colors = literal(background = "#F4F5F7", primary = "#C2261D"))))))
app.use(vuetify)
vuetify.theme.change("dark")        // templates: $vuetify.theme.current.dark
```

- UMD on jsDelivr (cdnjs lags); its `createVuetify` registers all components. Needs the MDI icon font CSS; Roboto is the default font.
- Styles use cascade layers: your unlayered CSS overrides Vuetify without `!important`.
- No global CSS reset: add `ma-0` to headings and paragraphs.
- MD3 class names (`text-headline-medium`, `text-body-large`); elevation 0-5; buttons no longer uppercase; grid uses `gap`.
- `surface-variant` is dark in light themes; for subtle backgrounds use `rgba(var(--v-theme-on-surface), .12)`.
- Template slot names with dots work in string templates: `<template #item.name="{ value }">`.
- `v-navigation-drawer v-model="drawer"` with `drawer = null` lets Vuetify pick permanent (desktop) or overlay (phone).
- Inside Options API code, `this.$vuetify.display.mobile` / `theme` are available via a global mixin.

## Testing without a test framework

Node 22+ has `fetch` and `WebSocket`, enough to drive Chrome over the DevTools protocol:
launch `chrome --headless=new --remote-debugging-port=9333 --user-data-dir=<tmp>`, read `/json/list`, open the
page's `webSocketDebuggerUrl`, enable `Runtime`/`Page`/`Log`, then `Runtime.evaluate` async snippets with
`awaitPromise: true, returnByValue: true`. Collect `Runtime.consoleAPICalled` / `exceptionThrown` and fail on
`[Vue warn]`. Emulate dark mode with `Emulation.setEmulatedMedia` (`prefers-color-scheme`).

Traps: wait for mounted state (an element `id` creates a same-named `window` global early); wait out list
transitions before indexing; scope selectors away from navigation lists; tick between synthetic key events;
screenshot with a tall viewport + `clip`, not `captureBeyondViewport` (it greys the page); look at the screenshots,
passing checks don't catch bad colours.
