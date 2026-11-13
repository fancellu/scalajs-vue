# Skills

Agent skills for this repository: condensed, tested know-how for working on it with an AI coding agent
(or as a human). They live in [`.claude/skills/`](.claude/skills); Claude Code loads them automatically
when you open the repo, and other tools can read the same Markdown files.

| Skill | Use it for |
| --- | --- |
| [scalajs-vue](.claude/skills/scalajs-vue/SKILL.md) | Working on this repo: map of the code, sbt 2 commands (`dev`, `demo<Name>`) and Windows quirks, adding an example, running the browser tests and regenerating screenshots, Vuetify 4 traps hit here |
| [scalajs-vue-facades](.claude/skills/scalajs-vue-facades/SKILL.md) | Any project using Scala.js with Vue 3 / Vuetify 4 without a bundler: writing facades, Options and Composition API components from Scala, reactivity with Scala data, fetch and timers, Vue 2 to 3 differences, testing in headless Chrome |

In Claude Code they can also be invoked directly as `/scalajs-vue` and `/scalajs-vue-facades`.

See also [README.md](README.md) and [EXAMPLES.md](EXAMPLES.md).
