// Minimal Chrome DevTools Protocol driver for headless Chrome, shared by the screenshot and test scripts.
// Needs Node 22+ (built-in fetch and WebSocket) and Chrome or Edge; set CHROME=/path/to/chrome if it isn't found.
import { spawn } from "node:child_process";
import { existsSync, mkdtempSync, writeFileSync } from "node:fs";
import { join } from "node:path";
import { tmpdir } from "node:os";

export const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

export function findChrome() {
  if (process.env.CHROME) return process.env.CHROME;
  const candidates = {
    win32: [
      "C:/Program Files/Google/Chrome/Application/chrome.exe",
      "C:/Program Files (x86)/Google/Chrome/Application/chrome.exe",
      "C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe",
    ],
    darwin: ["/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"],
  }[process.platform] ?? ["/usr/bin/google-chrome", "/usr/bin/chromium", "/usr/bin/chromium-browser"];
  const found = candidates.find((p) => existsSync(p));
  if (!found) throw new Error("Chrome not found; set CHROME=/path/to/chrome");
  return found;
}

export async function launch(chromePath = findChrome(), port = 9333) {
  const profile = mkdtempSync(join(tmpdir(), "cdp-"));
  const chrome = spawn(chromePath, [
    "--headless=new", "--disable-gpu", "--no-first-run", "--hide-scrollbars",
    `--remote-debugging-port=${port}`, `--user-data-dir=${profile}`, "about:blank",
  ], { stdio: "ignore" });
  let target;
  for (let i = 0; i < 50 && !target; i++) {
    try {
      const list = await (await fetch(`http://127.0.0.1:${port}/json/list`)).json();
      target = list.find((t) => t.type === "page");
    } catch { await sleep(200); }
  }
  const ws = new WebSocket(target.webSocketDebuggerUrl);
  await new Promise((r) => ws.addEventListener("open", r));
  let id = 0;
  const pending = new Map();
  const consoleLines = [];
  ws.addEventListener("message", (ev) => {
    const msg = JSON.parse(ev.data);
    if (msg.id && pending.has(msg.id)) { pending.get(msg.id)(msg); pending.delete(msg.id); }
    if (msg.method === "Runtime.consoleAPICalled")
      consoleLines.push(`${msg.params.type}: ${msg.params.args.map((a) => a.value ?? a.description).join(" ")}`);
    if (msg.method === "Runtime.exceptionThrown")
      consoleLines.push(`EXCEPTION: ${msg.params.exceptionDetails.exception?.description ?? msg.params.exceptionDetails.text}`);
    if (msg.method === "Log.entryAdded" && msg.params.entry.level === "error")
      consoleLines.push(`LOG-ERROR: ${msg.params.entry.text} ${msg.params.entry.url ?? ""}`);
  });
  const send = (method, params = {}) => new Promise((r) => { const i = ++id; pending.set(i, r); ws.send(JSON.stringify({ id: i, method, params })); });
  const evaluate = async (expression) => {
    const res = await send("Runtime.evaluate", { expression, awaitPromise: true, returnByValue: true });
    if (res.result?.exceptionDetails) throw new Error(res.result.exceptionDetails.exception?.description ?? JSON.stringify(res.result.exceptionDetails));
    return res.result?.result?.value;
  };
  await send("Runtime.enable");
  await send("Page.enable");
  await send("Log.enable");
  return {
    send, evaluate, consoleLines,
    async viewport(width, height, mobile = false) {
      await send("Emulation.setDeviceMetricsOverride", { width, height, deviceScaleFactor: 1, mobile });
    },
    async scheme(dark) {
      await send("Emulation.setEmulatedMedia", { features: [{ name: "prefers-color-scheme", value: dark ? "dark" : "light" }] });
    },
    async goto(url) {
      await send("Page.navigate", { url });
      for (let i = 0; i < 100; i++) {
        if (await evaluate("!!(window.app && document.querySelector('.v-main'))").catch(() => false)) break;
        await sleep(100);
      }
    },
    async shot(file, fullPage = true) {
      let clip;
      if (fullPage) {
        const m = await send("Page.getLayoutMetrics");
        const { width, height } = m.result.cssContentSize;
        clip = { x: 0, y: 0, width, height: Math.min(height, 4000), scale: 1 };
      }
      const res = await send("Page.captureScreenshot", { format: "png", captureBeyondViewport: fullPage, ...(clip ? { clip } : {}) });
      writeFileSync(file, Buffer.from(res.result.data, "base64"));
    },
    close() { ws.close(); chrome.kill(); },
  };
}
