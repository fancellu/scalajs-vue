package com.felstar.scalajs.vue

import scala.scalajs.js
import scala.scalajs.js.annotation._
import scala.scalajs.js.|
import org.scalajs.dom

/** Scala.js facade for the Vue 3 global build (`vue.global.js`), which exposes everything on
  * the `Vue` global. Load Vue with a plain `<script>` tag before calling into these bindings.
  */
@js.native
@JSGlobal("Vue")
object Vue extends js.Object {
  val version: String = js.native

  // Application
  def createApp(rootComponent: js.Object): App = js.native
  def createApp(rootComponent: js.Object, rootProps: js.Object): App = js.native

  // General
  def nextTick(): js.Promise[Unit] = js.native
  def nextTick(fn: js.Function0[Any]): js.Promise[Unit] = js.native
  def defineComponent(options: js.Object): js.Object = js.native
  def h(`type`: js.Any, props: js.Any = js.native, children: js.Any = js.native): VNode = js.native

  // Reactivity
  def ref[T](value: T): Ref[T] = js.native
  def reactive[T <: js.Any](target: T): T = js.native
  def readonly[T <: js.Any](target: T): T = js.native
  def computed[T](getter: js.Function0[T]): Ref[T] = js.native
  def watch(source: js.Any, callback: WatchCallback, options: js.Object = js.native): WatchHandle = js.native
  def watchEffect(effect: js.Function0[Any]): WatchHandle = js.native
  def toRaw[T](proxy: T): T = js.native
  def unref[T](maybeRef: T | Ref[T]): T = js.native
  def isRef(value: js.Any): Boolean = js.native
  def isReactive(value: js.Any): Boolean = js.native
}

/** Returned by `Vue.createApp`. Registration methods return the app so calls can be chained. */
@js.native
trait App extends js.Object {
  val version: String = js.native
  val config: js.Dynamic = js.native

  def component(name: String): js.UndefOr[js.Object] = js.native
  def component(name: String, definition: js.Object): App = js.native
  def directive(name: String): js.UndefOr[js.Any] = js.native
  /** `definition` is either an object of hooks or a [[DirectiveHook]]. */
  def directive(name: String, definition: js.Any): App = js.native
  def use(plugin: js.Any, options: js.Any*): App = js.native
  def mixin(mixin: js.Object): App = js.native
  def provide(key: js.Any, value: js.Any): App = js.native

  /** Takes a CSS selector or an element; returns the root component instance. */
  def mount(rootContainer: String | dom.Element): ComponentPublicInstance = js.native
  def unmount(): Unit = js.native
}

/** What `this` is inside Options API methods, computed properties and templates.
  * Extend it with a `@js.native` trait to describe your component's data.
  */
@js.native
trait ComponentPublicInstance extends js.Object {
  val $el: dom.Node = js.native
  val $data: js.Dynamic = js.native
  val $props: js.Dynamic = js.native
  val $options: js.Dynamic = js.native
  val $parent: ComponentPublicInstance = js.native
  val $root: ComponentPublicInstance = js.native
  val $refs: js.Dynamic = js.native
  val $slots: js.Dynamic = js.native
  val $attrs: js.Dynamic = js.native

  def $emit(event: String, args: js.Any*): Unit = js.native
  /** Returns a function that stops the watcher. */
  def $watch(source: js.Any, callback: WatchCallback, options: js.Object = js.native): js.Function0[Unit] = js.native
  def $nextTick(): js.Promise[Unit] = js.native
  def $nextTick(fn: js.Function0[Any]): js.Promise[Unit] = js.native
  def $forceUpdate(): Unit = js.native
}

/** Second argument passed to directive hooks. */
@js.native
trait DirectiveBinding extends js.Object {
  val value: js.Any = js.native
  val oldValue: js.Any = js.native
  val arg: js.UndefOr[String] = js.native
  val modifiers: js.Dictionary[Boolean] = js.native
  val instance: ComponentPublicInstance = js.native
  val dir: js.Object = js.native
}

@js.native
trait Ref[T] extends js.Object {
  var value: T = js.native
}

/** Calling it stops the watcher; `pause` and `resume` are available since Vue 3.5. */
@js.native
trait WatchHandle extends js.Function0[Unit] {
  def stop(): Unit = js.native
  def pause(): Unit = js.native
  def resume(): Unit = js.native
}

@js.native
trait VNode extends js.Object
