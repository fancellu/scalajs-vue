// Checks each single-demo page (demo-*.html?demo=<id>) and the plain Hello World page.
// Usage: node scripts/test-pages.mjs [serverUrl] [dev|opt] [screenshotDir]   (with sbt dev running)
import { launch, sleep } from "./cdp.mjs";

const base = process.argv[2] ?? "http://localhost:12345";
const variant = process.argv[3] ?? "dev";
const outDir = process.argv[4]; // optional: save a few screenshots there
const b = await launch();
await b.viewport(1280, 1000);
await b.scheme(false);
const results = [];
const check = (name, ok, detail) => results.push(`${ok ? "PASS" : "FAIL"} ${name}${ok ? "" : "  -> " + JSON.stringify(detail)}`);

const titles = { todos: "Todos", basics: "Basics", temperature: "Temperature", timer: "Timer", grid: "Data grid", fetch: "Fetching data", circles: "Circle drawer" };
for (const [id, title] of Object.entries(titles)) {
  await b.send("Page.navigate", { url: `${base}/demo-${variant}.html?demo=${id}` });
  for (let i = 0; i < 50 && !(await b.evaluate("!!document.querySelector('.v-main h1')").catch(() => false)); i++) await sleep(100);
  await sleep(500);
  const r = await b.evaluate(`({ h1: document.querySelector('h1')?.textContent.trim(), drawer: !!document.querySelector('.v-navigation-drawer'),
    appBar: !!document.querySelector('.v-app-bar'), body: document.querySelector('.v-main .v-card') !== null, title: document.title,
    source: document.querySelector('header a')?.href })`);
  check(`single ${id}: own page, no gallery chrome`, r.h1 === title && !r.drawer && !r.appBar && r.body && r.title.startsWith(title) && r.source.endsWith(`/example/vuetify/${r.source.split('/').pop()}`), r);
  if (id === "timer" || id === "grid") outDir && await b.shot(`${outDir}/single-${variant}-${id}.png`, false);
}
await b.send("Page.navigate", { url: `${base}/demo-${variant}.html?demo=nope` });
await sleep(1500);
check("single: unknown ?demo falls back to the first example", (await b.evaluate("document.querySelector('h1')?.textContent.trim()")) === "Todos");

// plain hello world
await b.send("Page.navigate", { url: `${base}/hello-${variant}.html` });
for (let i = 0; i < 50 && !(await b.evaluate("!!document.querySelector('.hello h1')").catch(() => false)); i++) await sleep(100);
const h = await b.evaluate(`(async () => {
  const tick = () => Vue.nextTick();
  const start = { h1: document.querySelector('h1').textContent, reversed: document.querySelector('strong').textContent };
  const input = document.querySelector('#message');
  input.value = 'Scala.js'; input.dispatchEvent(new Event('input')); await tick();
  const typed = { h1: document.querySelector('h1').textContent, reversed: document.querySelector('strong').textContent };
  const counter = [...document.querySelectorAll('button')][0];
  counter.click(); await tick(); const one = counter.textContent;
  counter.click(); await tick(); const two = counter.textContent;
  [...document.querySelectorAll('button')][1].click(); await tick();
  return { start, typed, one, two, afterReset: [document.querySelector('h1').textContent, counter.textContent],
    vuetifyLoaded: typeof Vuetify !== 'undefined', vuetifyCss: [...document.styleSheets].some(s => (s.href || '').includes('vuetify')),
    note: document.querySelector('.note').textContent.trim() };
})()`);
check("hello: renders 'Hello Vue!' reversed in Scala", h.start.h1 === "Hello Vue!" && h.start.reversed === "!euV olleH", h);
check("hello: v-model updates heading and reversed text", h.typed.h1 === "Scala.js" && h.typed.reversed === "sj.alacS", h);
check("hello: counter pluralises", h.one === "Clicked 1 time" && h.two === "Clicked 2 times", h);
check("hello: reset", h.afterReset[0] === "Hello Vue!" && h.afterReset[1] === "Clicked 0 times", h);
check("hello: no Vuetify JS or CSS on the page", !h.vuetifyLoaded && !h.vuetifyCss, h);
if (outDir) await b.shot(`${outDir}/hello-${variant}-light.png`, false);
await b.scheme(true); await sleep(300);
if (outDir) await b.shot(`${outDir}/hello-${variant}-dark.png`, false);

const bad = b.consoleLines.filter((l) => /^(error|warning|EXCEPTION|LOG-ERROR)/.test(l) || /\[Vue warn\]/.test(l));
check("no console errors or Vue warnings", bad.length === 0, bad);
console.log(results.join("\n"));
b.close();
process.exit(results.some((x) => x.startsWith("FAIL")) ? 1 : 0);
