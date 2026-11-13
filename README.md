![scalajs-vue Logo](http://felstar.com/projects/scalajs-vue/img/scalajs-vue-cliff.png)
# scalajs-vue

## Scala.js bindings for Vue.js 3 and an example application

To use as a library, just pull in from

[https://jitpack.io/#fancellu/scalajs-vue](https://jitpack.io/#fancellu/scalajs-vue)

[![CI](https://github.com/fancellu/scalajs-vue/actions/workflows/ci.yml/badge.svg)](https://github.com/fancellu/scalajs-vue/actions/workflows/ci.yml)

Example app showing the use of [Scala.js](https://www.scala-js.org/) with [Vue.js](https://vuejs.org/),
styled with [Vuetify](https://vuetifyjs.com/).
The bindings for Vue.js are in `com.felstar.scalajs.vue`, and a small Vuetify facade is in
`com.felstar.scalajs.vuetify`.

Built with Scala 2.13.18, Scala.js 1.22.0, scalajs-dom 2.8.1 and sbt 2.0.12, against Vue 3.5.45 and Vuetify 4.3.1.

## Examples

There's a plain-Vue Hello World (`example.plain`) and seven examples styled with Vuetify
(`example.vuetify`): todos, bindings, a temperature converter, a timer, a data grid, fetching from
the GitHub API, and a circle drawer with undo/redo. **See [EXAMPLES.md](EXAMPLES.md)** for
screenshots and what each one shows.

![The example gallery](docs/screenshots/gallery.png)

## Get started

You need a JDK (17 or later) and [sbt](https://www.scala-sbt.org/) installed. Any sbt launcher works;
it downloads the sbt version named in `project/build.properties`.

Run

    sbt dev

(`sbt run` does the same). This downloads dependencies, links the JavaScript, starts a small web
server and then relinks whenever you change the Scala sources.

Open [http://localhost:12345/index-dev.html](http://localhost:12345/index-dev.html) in your browser
and pick an example from the drawer. Do open the Console to see debug messages.

To run a single demo on its own page and open your browser on it, use its shortcut
(all listed in [EXAMPLES.md](EXAMPLES.md)), e.g.

    sbt demoTimer
    sbt demoHello

Vue (the `vue.global.js` build, which includes the template compiler) and Vuetify (its UMD build) are
loaded from CDNs as the globals `Vue` and `Vuetify`, so there is no npm or bundler step. The page also
loads the Roboto font and the Material Design Icons font that Vuetify uses.

## Demo

[live demo](http://dinofancellu.com/demo/scalajsVue/)

## Development

If you change the page (inside `/src/main/resources`) or an example (inside `/src/main/scala/example`),
`sbt dev` picks it up: the HTML is served straight from `src/main/resources`, and the Scala is
relinked on save. You then just have to refresh the page to see the new version. Hopefully having
example applications will make it clearer how to use Vue from Scala, i.e. monkey see, monkey do.

To add an example, see [Adding an example](EXAMPLES.md#adding-an-example).

The server maps

| URL | Serves |
| --- | --- |
| `/` | `src/main/resources` |
| `/dev/main.js` | `fastLinkJS` output |
| `/opt/main.js` | `fullLinkJS` output |

`sbt serve` starts the server and returns to the sbt shell without watching.

## Browser tests and screenshots

[`scripts/`](scripts) has small Node scripts that drive headless Chrome over the DevTools protocol.
They need Node 22+ and Chrome or Edge (set `CHROME=/path/to/chrome` if it isn't found), and expect
`sbt dev` to be running; run `sbt fullLinkJS` first for the `-opt` pages.

    node scripts/test-gallery.mjs                    # clicks through every gallery example
    node scripts/test-gallery.mjs http://localhost:12345/index-opt.html
    node scripts/test-pages.mjs [url] [dev|opt]      # single-demo pages and Hello World
    node scripts/screenshots.mjs                     # regenerates docs/screenshots for EXAMPLES.md

The tests print `PASS`/`FAIL` per check and exit non-zero on any failure. The Fetching data checks
call the real GitHub API, so they need a network connection (a rate-limit error still passes, as the
page shows it).

## The optimized version

Run `sbt fullLinkJS` (with `sbt dev` running in another terminal, or followed by `sbt serve` in
the sbt shell) and open [http://localhost:12345/index-opt.html](http://localhost:12345/index-opt.html)
for an optimized version of the final application, using the production builds of Vue and Vuetify.
Every page comes in a `-dev` and an `-opt` pair (`index`, `demo`, `hello`); the `-opt` one differs
only in its script tags, so copy over any changes you make to the dev page. Shared styles live in
`vuetify-examples.css` and `hello.css`.

## Moving from the Vue 2 bindings

Vue 3 changed its API, so these bindings did too:

* `new Vue(options)` is now `Vue.createApp(options)`, followed by `app.mount("#el")`. `data` must be a function.
* Global `Vue.component` / `Vue.directive` / `Vue.use` are now methods on the `App`.
* The component instance type is `ComponentPublicInstance` (it was the `Vue` class).
* Filters are gone; use methods in templates instead, e.g. `{{ reverse(title) }}`.
* `Vue.set` / `Vue.delete` are gone; plain assignment and `splice` are reactive in Vue 3.
* Event bus methods (`$on`, `$off`, `$once`) and `$children` are gone.
* Directives receive a `DirectiveBinding` (`value`, `oldValue`, `arg`, `modifiers`, ...).
* The reactivity API is available too: `Vue.ref`, `Vue.reactive`, `Vue.computed`,
  `Vue.writableComputed`, `Vue.watch`, `Vue.markRaw`, and the `onMounted` / `onUnmounted` hooks.
* Scala.js 1.x no longer allows dots in top-level export names, so the demo is exported as `ScalaVue`
  (it was `example.Todo`).

## Status

This is just a quick proof of concept and some bindings for those wanting to use Vue.js from Scala.js. Feel free to get in contact with any issues etc.
