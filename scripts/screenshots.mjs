// Screenshots for EXAMPLES.md, taken from the optimized pages and clipped to their content.
// Usage: node scripts/screenshots.mjs [serverUrl] [outDir]   (run sbt fullLinkJS and sbt dev first)
import { launch, sleep } from "./cdp.mjs";
import { writeFileSync } from "node:fs";

const base = process.argv[2] ?? "http://localhost:12345";
const outDir = process.argv[3] ?? "docs/screenshots";
const b = await launch();

async function shot(name, { url, width = 1100, height = 900, dark = false, prep = "", clipSelector = ".v-main .v-container", wait = 900 }) {
  await b.viewport(width, height);
  await b.scheme(dark);
  await b.send("Page.navigate", { url: `${base}/${url}` });
  for (let i = 0; i < 60 && !(await b.evaluate("!!document.querySelector('#app h1')").catch(() => false)); i++) await sleep(100);
  await sleep(wait);
  if (prep) await b.evaluate(`(async () => { ${prep} })()`);
  await sleep(400);
  const h = clipSelector
    ? await b.evaluate(`Math.ceil(document.querySelector(${JSON.stringify(clipSelector)}).getBoundingClientRect().bottom + 8)`)
    : height;
  const res = await b.send("Page.captureScreenshot", { format: "png", clip: { x: 0, y: 0, width, height: Math.min(h, height), scale: 1 } });
  writeFileSync(`${outDir}/${name}.png`, Buffer.from(res.result.data, "base64"));
  console.log(`${name}.png ${width}x${Math.min(h, height)}`);
}

const svgClicks = `
  const svg = document.querySelector('svg.drawing');
  const at = (x, y) => { const r = svg.getBoundingClientRect(); return { clientX: r.left + x * r.width / 600, clientY: r.top + y * r.height / 360, bubbles: true }; };
  for (const [x, y] of [[140, 120], [260, 200], [420, 140], [330, 90]]) { svg.dispatchEvent(new MouseEvent('click', at(x, y))); await Vue.nextTick(); }
  document.querySelectorAll('svg.drawing circle')[1].dispatchEvent(new MouseEvent('click', { bubbles: true }));`;

await shot("gallery", { url: "index-opt.html#/todos", width: 1280, height: 760, clipSelector: null });
await shot("gallery-dark", { url: "index-opt.html#/grid", width: 1280, height: 760, dark: true, clipSelector: null });
await shot("hello", { url: "hello-opt.html", width: 900, height: 600, clipSelector: "main.hello" });
await shot("todos", { url: "demo-opt.html?demo=todos" });
await shot("basics", { url: "demo-opt.html?demo=basics", height: 1400 });
await shot("temperature", { url: "demo-opt.html?demo=temperature" });
await shot("timer", { url: "demo-opt.html?demo=timer", wait: 4200 });
await shot("grid", { url: "demo-opt.html?demo=grid", height: 1000 });
await shot("fetch", { url: "demo-opt.html?demo=fetch", wait: 2500 });
await shot("circles", { url: "demo-opt.html?demo=circles", prep: svgClicks });
console.log("console:", b.consoleLines.filter((l) => !/^(log|info):/.test(l)).join(" | ") || "clean");
b.close();
