// Drives every gallery example through the real UI in headless Chrome; prints PASS/FAIL per check.
// Usage: node scripts/test-gallery.mjs [pageUrl]   (default http://localhost:12345/index-dev.html, with sbt dev running)
import { launch, sleep } from "./cdp.mjs";

const base = process.argv[2] ?? "http://localhost:12345/index-dev.html";
const b = await launch();
await b.viewport(1280, 1000);
await b.scheme(false);

const results = [];
const check = (name, ok, detail) => results.push(`${ok ? "PASS" : "FAIL"} ${name}${ok ? "" : "  -> " + JSON.stringify(detail)}`);
const run = (js) => b.evaluate(`(async () => { ${js} })()`);

const helpers = `
  window.$ = (s, root = document) => root.querySelector(s);
  window.$$ = (s, root = document) => [...root.querySelectorAll(s)];
  window.wait = (ms) => new Promise(r => setTimeout(r, ms));
  window.settle = (ms = 700) => Vue.nextTick().then(() => wait(ms));
  window.btn = (label, root = document) => $$('button', root).find(x => x.textContent.trim() === label);
  window.typeInto = (input, value) => { input.focus(); input.value = value; input.dispatchEvent(new Event('input', { bubbles: true })); };
  window.text = (s) => ($(s)?.textContent ?? '').replace(/\\s+/g, ' ').trim();
  window.route = async (id) => { location.hash = '#/' + id; await settle(500); };
`;
const page = async (route) => {
  await b.goto(`${base}#/${route}`);
  await b.evaluate(helpers);
  await run("await settle(300)");
};

// ---------- shell ----------
await page("todos");
let r = await run(`
  const before = $('.v-application').classList.contains('v-theme--dark');
  $('.v-app-bar button[aria-label*="theme"]').click(); await settle(300);
  const after = $('.v-application').classList.contains('v-theme--dark');
  $$('.v-navigation-drawer a').find(a => a.textContent.includes('Timer')).click(); await settle(500);
  return { before, after, heading: text('h1'), title: document.title, hash: location.hash };`);
check("theme toggle switches light -> dark", r.before === false && r.after === true, r);
check("drawer link routes to Timer and sets document.title", r.heading === "Timer" && r.title.startsWith("Timer") && r.hash === "#/timer", r);
r = await run(`await route('nope'); return text('h1');`);
check("unknown route falls back to the first example", r === "Todos", r);
await b.scheme(false);

// ---------- todos ----------
await page("todos");
r = await run(`
  const count = () => $$('.v-main .v-list .v-list-item').length;
  const start = count();
  const input = $('input[type=text]');
  typeInto(input, 'Write tests'); input.dispatchEvent(new KeyboardEvent('keyup', { key: 'Enter', bubbles: true })); await settle();
  const added = count(), lastText = text('.v-main .v-list .v-list-item:last-child .v-list-item-title'), cleared = input.value === '';
  $$('.v-main .v-list .v-list-item')[1].click(); await settle();
  const toggled = $$('.v-main .v-list .v-list-item')[1].querySelector('.text-decoration-line-through') !== null;
  $$('.v-main .v-list .v-list-item')[2].querySelector('button[aria-label^="Remove"]').click(); await settle();
  const afterRemove = count(), toggledStill = $$('.v-main .v-list .v-list-item')[1].querySelector('.text-decoration-line-through') !== null;
  $$('.v-chip').find(c => c.textContent.includes('Active')).click(); await settle();
  const activeOnly = $$('.v-main .v-list .v-list-item').every(li => !li.querySelector('.text-decoration-line-through'));
  const activeCount = count();
  $$('.v-chip').find(c => c.textContent.includes('All')).click(); await settle();
  btn('Clear done').click(); await settle();
  const afterClear = count();
  return { start, added, lastText, cleared, toggled, afterRemove, toggledStill, activeOnly, activeCount, afterClear, saved: JSON.parse(localStorage.getItem('scalajs-vue-todos')).length };`);
check("todos: starts with 4", r.start === 4, r);
check("todos: Enter adds 'Write tests' and clears the field", r.added === 5 && r.lastText === "Write tests" && r.cleared, r);
check("todos: clicking an item toggles it", r.toggled, r);
check("todos: remove button removes only that item", r.afterRemove === 4 && r.toggledStill, r);
check("todos: Active filter hides done items", r.activeOnly && r.activeCount === 2, r);
check("todos: Clear done leaves the 2 active", r.afterClear === 2, r);
check("todos: list saved to localStorage", r.saved === 2, r);
await page("todos");
r = await run(`return $$('.v-main .v-list .v-list-item').map(li => text('.v-list-item-title') && li.textContent.trim());`);
check("todos: list survives a reload", r.length === 2, r);

// ---------- basics ----------
await page("basics");
r = await run(`
  typeInto($('input[type=text]'), 'Scala'); await settle(200);
  const cells = $$('.v-table td').map(td => td.textContent.trim());
  btn('Scala decrement').click(); await settle(100);
  const afterDec = text('.text-display-medium');
  btn('Inline n++').click(); btn('Inline n++').click(); await settle(100);
  const afterInc = text('.text-display-medium');
  $('button[aria-label="Grow"]').click(); await settle(100);
  const width = $('.bar').style.width;
  return { cells, afterDec, afterInc, width, data: text('pre.code-block'), alerts: $$('.v-alert').map(a => a.textContent.replace(/\\s+/g, ' ').trim()) };`);
check("basics: v-text, reverse, wrap, v-shout follow the title",
  JSON.stringify(r.cells) === JSON.stringify(["v-text", "Scala", "reverse(title)", "alacS", "wrap(title, '«', '»')", "«Scala»", "v-shout directive", "SCALA!"]), r.cells);
check("basics: Scala decrement then inline n++ twice", r.afterDec === "-1" && r.afterInc === "1", r);
check("basics: style binding grows the bar to 60%", r.width === "60%", r.width);
check("basics: $data shows live state", r.data.includes('"title": "Scala"') && r.data.includes('"n": 1'), r.data);
check("basics: child component renders prop and slot / default slot",
  r.alerts[0]?.includes("msg prop = hello") && r.alerts[0]?.includes("Special content") && r.alerts[1]?.includes("default slot content"), r.alerts);

// ---------- temperature ----------
await page("temperature");
r = await run(`
  const [c, f] = $$('.v-number-input input');
  const start = [c.value, f.value];
  typeInto(c, '100'); await settle(200);
  const boiling = f.value;
  typeInto(f, '32'); await settle(200);
  const freezing = [c.value, text('.v-chip')];
  typeInto(c, ''); await settle(200);
  const cleared = f.value;
  return { start, boiling, freezing, cleared };`);
check("temperature: starts 20 / 68", r.start[0] === "20.0" && r.start[1] === "68.0", r);
check("temperature: 100 C -> 212 F", r.boiling === "212.0", r);
check("temperature: 32 F -> 0 C and 'Freezing'", r.freezing[0] === "0.0" && r.freezing[1] === "Freezing", r);
check("temperature: clearing a field keeps the other value", r.cleared === "32.0", r);

// ---------- timer ----------
await page("timer");
r = await run(`
  await wait(1500);
  const elapsed = parseFloat(text('.text-display-small'));
  btn('Reset').click(); await settle(100);
  const reset = parseFloat(text('.text-display-small'));
  return { elapsed, reset };`);
check("timer: counts up in real time", r.elapsed >= 1.2 && r.elapsed <= 2.5, r);
check("timer: Reset goes back to ~0", r.reset < 0.5, r);
const logsBefore = b.consoleLines.filter((l) => l.includes("interval cleared")).length;
await run(`await route('grid')`);
await sleep(200);
check("timer: leaving the page clears the interval", b.consoleLines.filter((l) => l.includes("interval cleared")).length === logsBefore + 1, b.consoleLines);

// ---------- grid ----------
await page("grid");
r = await run(`
  const names = () => $$('.v-data-table tbody tr').map(tr => tr.querySelector('td')?.textContent.trim());
  const first = names();
  const footer = text('.v-data-table-footer__info');
  typeInto($('.v-data-table').closest('.v-card').querySelector('input[type=text]'), 'odersky'); await settle(300);
  const searched = names();
  typeInto($('.v-card input[type=text]'), ''); await settle(200);
  btn('Dynamic').click(); await settle(300);
  const typings = $$('.v-data-table tbody tr .v-chip').map(c => c.textContent.trim());
  $$('.v-data-table thead th').find(th => th.textContent.includes('Language')).click(); await settle(300);
  const byName = names();
  return { first: first.slice(0, 3), firstCount: first.length, footer, searched, typings, byName };`);
check("grid: sorted by year, 10 per page of 17", r.first[0] === "Erlang" && r.firstCount === 10 && r.footer.includes("of 17"), r);
check("grid: search finds Scala by creator", r.searched.length === 1 && r.searched[0] === "Scala", r);
check("grid: Dynamic filter shows only dynamic languages", r.typings.length === 6 && r.typings.every((t) => t === "Dynamic"), r);
check("grid: header click sorts by name", r.byName[0] === "Clojure" && r.byName[1] === "Elixir", r);

// ---------- fetch ----------
await page("fetch");
r = await run(`
  for (let i = 0; i < 50 && !($$('.v-main .v-list .v-list-item').length || $('.v-alert')); i++) await wait(200);
  const main = { items: $$('.v-main .v-list .v-list-item').length, alert: text('.v-alert') };
  btn('minor').click();
  await settle(100);
  for (let i = 0; i < 50 && $('.v-btn--loading'); i++) await wait(200);
  return { main, minor: { items: $$('.v-main .v-list .v-list-item').length, alert: text('.v-alert'), first: text('.v-main .v-list .v-list-item .v-list-item-title') } };`);
check("fetch: main shows 5 commits (or a readable error)", r.main.items === 5 || r.main.alert.length > 0, r);
check("fetch: switching to minor reloads", r.minor.items === 5 || r.minor.alert.length > 0, r);
if (r.main.alert || r.minor.alert) results.push(`NOTE fetch showed an error alert: ${r.main.alert || r.minor.alert}`);

// ---------- circle drawer ----------
await page("circles");
r = await run(`
  const svg = $('svg.drawing');
  const at = (x, y) => { const rc = svg.getBoundingClientRect(); return { clientX: rc.left + x * rc.width / 600, clientY: rc.top + y * rc.height / 360, bubbles: true }; };
  const radii = () => $$('svg.drawing circle').map(c => Number(c.getAttribute('r')));
  svg.dispatchEvent(new MouseEvent('click', at(100, 100))); await settle(100);
  svg.dispatchEvent(new MouseEvent('click', at(300, 200))); await settle(100);
  const two = radii().length;
  const pos = $$('svg.drawing circle').map(c => [Math.round(c.getAttribute('cx')), Math.round(c.getAttribute('cy'))]);
  btn('Undo').click(); await settle(100);
  const afterUndo = radii().length;
  btn('Redo').click(); await settle(100);
  const afterRedo = radii().length;
  const first = $$('svg.drawing circle')[0];
  first.dispatchEvent(new MouseEvent('click', { bubbles: true })); await settle(100);
  const selectedFill = first.getAttribute('fill-opacity');
  first.dispatchEvent(new MouseEvent('contextmenu', { bubbles: true, cancelable: true })); await settle(500);
  const dialogOpen = !!$('.v-overlay--active .v-slider');
  const thumb = $('.v-overlay--active .v-slider-thumb');
  for (let i = 0; i < 10; i++) { thumb.dispatchEvent(new KeyboardEvent('keydown', { key: 'ArrowRight', bubbles: true })); await settle(20); }
  await settle(100);
  const preview = radii()[0];
  btn('Done').click(); await settle(500);
  const committed = radii()[0];
  btn('Undo').click(); await settle(100);
  const undoneResize = radii();
  return { two, pos, afterUndo, afterRedo, selectedFill, dialogOpen, preview, committed, undoneResize };`);
check("circles: clicks add circles where clicked", r.two === 2 && r.pos[0][0] === 100 && r.pos[0][1] === 100 && r.pos[1][0] === 300, r);
check("circles: undo / redo", r.afterUndo === 1 && r.afterRedo === 2, r);
check("circles: clicking a circle selects it", r.selectedFill === "0.35", r);
check("circles: right-click opens the resize dialog", r.dialogOpen, r);
check("circles: slider previews and Done commits the new radius", r.preview === 30 && r.committed === 30, r);
check("circles: resize is one undoable step", JSON.stringify(r.undoneResize) === "[20,20]", r);

// ---------- console ----------
const bad = b.consoleLines.filter((l) => /^(error|warning|EXCEPTION|LOG-ERROR)/.test(l) || /\[Vue warn\]|Vuetify/.test(l));
check("no console errors or Vue/Vuetify warnings", bad.length === 0, bad);

console.log(results.join("\n"));
b.close();
process.exit(results.some((x) => x.startsWith("FAIL")) ? 1 : 0);
