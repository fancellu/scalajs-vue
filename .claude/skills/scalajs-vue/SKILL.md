---
name: scalajs-vue
description: Working on the scalajs-vue repo (Scala.js bindings for Vue 3 and Vuetify 4, plus example apps). Use whenever editing, building, running, testing or documenting this repo - its facades, examples, HTML pages, build.sbt, the dev server, the browser-test scripts, EXAMPLES.md or screenshots. Covers the repo map, sbt 2 commands and their Windows quirks, how to add an example, how to verify changes, and Vuetify 4 traps already hit here. For general Scala.js + Vue patterns, also load scalajs-vue-facades.
---

# scalajs-vue

Scala 2.13.18, Scala.js 1.22.0, scalajs-dom 2.8.1, sbt 2.0.12, Vue 3.5.45, Vuetify 4.3.1.
No npm and no bundler: Vue and Vuetify load from CDNs as globals, Scala.js links to `main.js`.

## Map

| Path | What |
| --- | --- |
| `src/main/scala/com/felstar/scalajs/vue/Vue.scala` | Vue 3 facade: `Vue` (global object), `App`, `ComponentPublicInstance`, `DirectiveBinding`, `Ref`, `WatchHandle`, `WritableComputedOptions` |
| `.../vue/package.scala` | `WatchCallback`, `DirectiveHook` type aliases (a `@js.native` object can't have a Scala companion, so aliases live here) |
| `src/main/scala/com/felstar/scalajs/vuetify/Vuetify.scala` | Vuetify facade: `createVuetify`, `VuetifyInstance`, `ThemeInstance` |
| `src/main/scala/example/plain/` | Vue-only examples (`HelloWorld`, exported as `HelloWorld.main`) |
| `src/main/scala/example/vuetify/` | Vuetify examples, one self-contained file each; `Main.scala` = gallery `ScalaVue.main` + single page `ScalaVue.single` + the `examples` registry |
| `src/main/resources/` | Pages in `-dev`/`-opt` pairs: `index` (gallery), `demo` (`?demo=<id>`), `hello`; shared CSS `vuetify-examples.css`, `hello.css` |
| `build.sbt` | Dev server commands, the `demos` list, and `dist` |
| `vercel.json`, `scripts/vercel-build.sh` | Vercel deployment of `target/dist` |
| `project/DevServer.scala` | JDK `HttpServer` static server + `openBrowser` |
| `scripts/` | Headless Chrome (CDP) tests and screenshot generator |
| `EXAMPLES.md`, `docs/screenshots/` | Per-example docs with screenshots |

## Build and run

Always run sbt from the repo root.

| Command | Does |
| --- | --- |
| `sbt dev` (= `sbt run`) | `fastLinkJS`, serve on `http://localhost:12345`, then `~fastLinkJS` |
| `sbt serve` | Link and serve, back to the shell without watching |
| `sbt "demo timer"` / `sbt demoTimer` | Like `dev`, and opens the browser on that demo alone. Ids: `gallery hello todos basics temperature timer grid fetch circles` |
| `sbt fullLinkJS` | Needed before any `-opt` page shows current code |
| `-Ddemo.browser=false` | Don't open a browser (always use this when automating) |
| `sbt dist` | `fullLinkJS` + static site in `target/dist` (`index.html` = gallery, `demo.html`, `hello.html`, CSS, `opt/`); what Vercel deploys |

Deploying: `vercel.json` (framework none, `outputDirectory: target/dist`, `cleanUrls`) runs
`scripts/vercel-build.sh`, which downloads Temurin JDK 21 + the sbt launcher (Vercel's Amazon Linux 2023 image has no
Java) and runs `sbt --server -batch dist`. `--server` matters: with sbt 2 the launcher otherwise starts a thin client
plus a background sbt server that outlives the build. Tested in a `public.ecr.aws/amazonlinux/amazonlinux:2023`
container (add `tar gzip` there; Vercel's image has them). Vercel reads `vercel.json` (or `vercel.ts`), never YAML. New pages must be added to
the `dist` command's page list. Test the output locally by serving `target/dist` at the site root and running
`node scripts/test-gallery.mjs http://localhost:<port>/`.

Server mounts: `/` -> `src/main/resources` (HTML/CSS edits are live), `/dev/` -> fastLinkJS output, `/opt/` -> fullLinkJS
output. Linker output lives under `target/out/sjs1/scala-2.13.18/...` (sbt 2 layout); never hardcode it, the build
reads `scalaJSLinkerOutputDirectory`.

## sbt 2 and Windows gotchas

- `%%` is platform-aware in sbt 2; `%%%` no longer exists.
- The meta-build (`build.sbt`, `project/*.scala`) is Scala 3: write `xs*`, not `xs: _*`, or you get warnings.
- Dev server commands are `Command`s, not tasks, so sbt 2's task cache can't skip them.
- `sbt --client` breaks on Windows when the launcher path contains spaces (`Program Files (x86)`). To link while
  `sbt dev` runs, stop it and use `sbt -batch fastLinkJS fullLinkJS`.
- A `cd` into a subfolder that persists makes the next `sbt` start a stray build there (creates `target/`). Use absolute paths.
- Killing a backgrounded shell on Windows leaves the sbt JVM alive and port 12345 busy. Start sbt with a marker
  (`-Dsbt.testmarker=devcheck`) and kill exactly that JVM:
  `powershell -NoProfile -Command "(Get-CimInstance Win32_Process -Filter \"Name='java.exe'\" | Where-Object { $_.CommandLine -like '*sbt.testmarker=devcheck*' }).ProcessId"` then `taskkill //PID <pid> //F`.
  Never kill other `java.exe` processes; they belong to the user.
- `reload` in the shell is safe: `Global / onUnload` stops the server.
- JDK 17+. `DevServer` deliberately avoids `SimpleFileServer` (JDK 18+) so JitPack's JDK 17 still compiles the build.

## Adding an example

Vuetify:
1. New file in `src/main/scala/example/vuetify/` exposing `val component: js.Object` (copy the closest existing one).
2. `Example(id, title, subtitle, description, icon, file, component)` entry in `Main.scala` (`subtitle` must fit the drawer, ~25 chars).
3. Add the id to `demos` in `build.sbt` (gives `demo<Id>`).
4. Section + screenshot in `EXAMPLES.md` (add a `shot(...)` line to `scripts/screenshots.mjs`), and checks in `scripts/test-gallery.mjs`.

Plain: an object with `@JSExportTopLevel("Name")` and `@JSExport def main(selector: String)`, a `name-dev.html` /
`name-opt.html` pair loading only Vue, and an entry in `demos`.

Templates are Scala triple-quoted strings compiled at runtime by `vue.global.js`: kebab-case tags, self-closing is
fine, no in-DOM parsing traps. Don't use `s"..."` on a template that contains `$vuetify` or `$data`; splice shared
fragments with `.replace("HEADER", header)` as `Main.scala` does.

When changing a page, edit the `-dev` file and regenerate its `-opt` twin; they differ only in script tags:
`sed -e 's#vue.global.js#vue.global.prod.js#' -e 's#dist/vuetify.js#dist/vuetify.min.js#' -e 's#src="dev/main.js"#src="opt/main.js"#' X-dev.html > X-opt.html`.

## Verifying a change

1. `sbt -batch fastLinkJS fullLinkJS`: must compile with no warnings (`-deprecation -feature` are on).
2. Start `sbt -Ddemo.browser=false -Dsbt.testmarker=devcheck dev` in the background; wait for `Monitoring source files`.
3. Run, against both builds:
   - `node scripts/test-gallery.mjs` and `node scripts/test-gallery.mjs http://localhost:12345/index-opt.html` (36 checks)
   - `node scripts/test-pages.mjs` and `node scripts/test-pages.mjs http://localhost:12345 opt` (14 checks)
4. Look at the UI, don't just trust the checks: `node scripts/screenshots.mjs` (writes `docs/screenshots`), then view the PNGs, light and dark.
5. Kill the marked sbt JVM.

Traps when writing browser checks (all hit here):
- Scope selectors to `.v-main`; the navigation drawer is also a `.v-list`.
- List transitions keep leaving items in the DOM ~0.5 s; wait ~700 ms before indexing list items.
- `<div id="demo">` makes `window.demo` exist before the app mounts; wait for real mounted state.
- Synthetic key presses on a `v-slider` thumb need a `Vue.nextTick` between them.
- Read values (e.g. table footer) before an action that changes them.
- `Page.captureScreenshot` with `captureBeyondViewport` greys the page; use a tall viewport and a clip.
- The fetch example calls the real GitHub API (60 unauthenticated requests/hour); an error alert counts as a pass.

## Vuetify 4 notes (as used here)

- UMD from jsDelivr (`vuetify@4.3.1/dist/vuetify.js|.min.js|.min.css`); cdnjs stops at 4.0.2. A version bump touches all
  Vuetify pages (`index-*`, `demo-*`).
- The UMD `createVuetify()` registers every component and directive; `app.use(vuetify)` is the whole setup.
- Styles live in CSS cascade layers, so plain unlayered CSS in `vuetify-examples.css` wins without `!important`.
- Default theme is `system`. Switch with `vuetify.theme.change("dark")`; templates read `$vuetify.theme.current.dark`.
- MD3 typography classes: `text-headline-medium`, `text-body-large`, `text-display-small`, ...
- `surface-variant` is dark in the light theme; for neutral tracks use `rgba(var(--v-theme-on-surface), .12)`.
- `v-data-table mobile-breakpoint="sm"` stacks rows on phones; `v-number-input` needs `:precision` for decimals and emits `null` when cleared.
- No CSS reset any more: give `h1`/`p` explicit `ma-0` etc.

## Keep in step

README (short), `EXAMPLES.md` (+ regenerate screenshots), `SKILLS.md`, and this file.
Commits: no AI attribution lines. Work branch so far: `upgrade-vue3-scalajs1`. Source links in `Main.scala` point at `blob/master/...`.
