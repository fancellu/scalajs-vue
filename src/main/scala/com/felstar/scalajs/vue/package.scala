package com.felstar.scalajs

import scala.scalajs.js
import org.scalajs.dom

package object vue {

  /** `(newValue, oldValue) => ...`; Vue also passes a third `onCleanup` argument. */
  type WatchCallback = js.Function2[_, _, Any]

  /** Function shorthand for a directive: runs on both `mounted` and `updated`. */
  type DirectiveHook = js.Function2[dom.HTMLElement, DirectiveBinding, Any]
}
