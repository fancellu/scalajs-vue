# scalajs-vue starter

A minimal Scala.js app using [scalajs-vue](https://github.com/fancellu/scalajs-vue) from JitPack:
one Vue 3 component styled with Vuetify 4, written in Scala. Copy this folder to start your own project.

It's a standalone sbt build: it uses the published library, not the code in the rest of this repository.

## Run it

You need a JDK (17 or later) and [sbt](https://www.scala-sbt.org/).

```
cd starter
sbt run
```

This links the Scala.js code into `public/js/main.js`, opens `public/index.html` in your browser, and
relinks whenever you save a Scala file; refresh the page to see the change. Add `-Dopen.browser=false`
to skip opening the browser, or use `sbt ~fastLinkJS` and open the page yourself. (Opening the file
directly works; you can also serve the `public` folder with any static server.)

Scala.js's usual `run` would execute the app in Node.js, which has no web page, Vue or Vuetify, so
`build.sbt` redefines `run` to open the page instead.

## What's where

| File | What |
| --- | --- |
| `build.sbt` | Scala.js plugin, the JitPack resolver and `"com.github.fancellu" % "scalajs-vue" % "v3.5.4"`; links into `public/js` |
| `src/main/scala/starter/Main.scala` | The app: creates Vuetify, defines a component with `setup` (refs and a computed value), mounts it on `#app` |
| `public/index.html` | Loads Vue, Vuetify, their CSS and fonts from CDNs, then `js/main.js` |

## Make it your own

- Rename the `starter` package and the project `name` in `build.sbt`.
- Edit the template and `setup` in `Main.scala`. See the
  [examples](https://github.com/fancellu/scalajs-vue/blob/master/EXAMPLES.md) for the Options API, watchers,
  directives, data tables, fetch with `Future`s and more.
- To upgrade, change the version in `build.sbt` to a newer
  [release tag](https://github.com/fancellu/scalajs-vue/releases).

## Production build

```
sbt fullLinkJS
```

writes an optimized `public/js/main.js`. In `public/index.html`, switch to `vue.global.prod.js` and
`vuetify.min.js`, then deploy the `public` folder to any static host.

## Notes

- On macOS and Linux, recent sbt launchers run sbt 2 through a thin client. Give several commands as
  one argument (`sbt "fastLinkJS; fullLinkJS"`) or add `--server` (`sbt --server fastLinkJS fullLinkJS`),
  and stop the background sbt server it leaves running with `sbt shutdown`.
- Vue's template compiler runs in the browser (that's what `vue.global.js` includes), so templates are
  plain strings in Scala and can use Vuetify components directly.
