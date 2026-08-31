---
title: "Roadmap: interpreted Groovy or only compiled Java"
space: Grouper
pageId: 48168966
version: 9
lastUpdated: 2026-07-15T16:51:29.181Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/48168966/Roadmap+interpreted+Groovy+or+only+compiled+Java
---

Grouper can author a GSH template, provisioner, or daemon two ways: interpreted Groovy (the original) and compiled Java (v7.3.0+, described on the parent page [Writing GSH templates in Java](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549690/Writing+GSH+templates+in+Java)). This page records the roadmap question — do we keep supporting both going forward, or standardize on compiled Java only — and the reasoning behind it.

> **Compiled does not mean jars.** You paste Java source into the GSH template screen (or put the source in a file in the container) exactly as you do with Groovy today; Grouper compiles it dynamically when you save it. The authoring and deployment workflow is identical to interpreted — there is no build, no jar, and no redeploy. Only the language and the compile-on-save check differ.

## Where this could go

This is a potential direction — interpreted mode keeps working today, so nothing you have breaks. The table below is the analysis for a direction into changing how institutional code is managed:

- For new work, write compiled Java. Every template type supports it: gsh, abac, provisioner, daemon, daemonChangeLog, report, customUi, hook, and library.
- Migrate existing interpreted templates and scripts over time — help is offered below, and AI has made this nearly free.
- Proposed roadmap: interpreted stays available through v7 and v8, and is removed in v9 or v11. That gives people time to migrate gracefully.

The case for standardizing: less technical debt, one consistent language, all institutional code consolidated in one inventory (the GSH template screen) with compile status visible at upgrade time, better performance, and real object-oriented Java.

## Comparison

Both options across the top, criteria down the left. Each cell shows which option is better for that criterion — green is better, red is not better, and equal criteria are unshaded. These are objective technical differences — which template types are available, when errors are caught, the execution model, performance, and tooling — not a matter of language preference or style.

| **Criterion** | **Keep interpreted (Groovy)** | **Only compiled Java** | **Description and finding** |
| --- | --- | --- | --- |
| **Available template types** | — | **Better** | Interpreted supports only gsh, abac, and provisioner. Compiled supports every type: gsh, abac, provisioner, daemon, daemonChangeLog, report, customUi, hook, and library. If you need any of the newer types, compiled is the only option. |
| **Groovy language support** | **Better** | — | Only interpreted mode runs Groovy. If you specifically want Groovy's dynamic typing and scripting idioms, that is the one thing compiled Java does not give you. |
| **Technical debt / maintenance** | — | **Better** | One language and one execution path is less to maintain, patch, test, and document. Supporting both keeps two dispatchers, two sets of docs, and two failure modes alive indefinitely. There is a lot of code to make each work. |
| **Consistency** | — | **Better** | One language means every template reads the same way; reviewers and new staff learn a single model instead of first working out which mode a template uses. |
| **No migration needed** | **Better** | — | Keeping interpreted means existing Groovy templates and scripts run unchanged with zero conversion work. This is the main cost of standardizing — but AI has greatly reduced it (see migration help below), and the interpreted version keeps running until you flip the mode. |
| **Upgrade safety (compile on upgrade)** | — | **Better** | Compiled templates report compile status against the running Grouper on the inventory screen, so a signature that changed in an upgrade shows up at save/upgrade time. An interpreted script surfaces the same break only when it next runs — often a daemon in the middle of the night or whenever someone runs a GSH template. |
| **Confidence from a clean compile (before running)** | — | **Better** | Java is fully type-checked, so a clean compile rules out typos, wrong argument types, and calls to methods that do not exist — across the whole template, not just the lines that happen to run. Dynamic Groovy defers those checks to runtime, so a bug in a rarely-hit branch can surface months later in production. This holds with no upgrade involved: more of the code is known-good before it ever executes. |
| **Performance** | — | **Better** | Compiled runs as JIT Java with no interpreter overhead. A production email-routing daemon ran about 15% faster (around 30 seconds on a ~180 second job) after conversion. I/O-bound jobs gain less; loop-heavy ones gain more. We need to keep Grouper lead and reduce hardware requirements. |
| **Terseness for quick one-off scripts** | Equal | Equal | Groovy is slightly terser. With AI now writing and converting institutional code, that terseness is no longer a real advantage — a compiled template's class-plus-method boilerplate is generated instantly. |
| **Object-oriented / language features** | — | **Better** | Real classes, inheritance, type checking, array initializers, switch statements, and a literal $ in a string all work; the Groovy workarounds go away. |
| **Tooling and debugging** | — | **Better** | Full IDE support — refactor, find-usages, JUnit — a real debugger, and real stack traces with real line numbers. |
| **All institutional code in one place** | — | **Better** | Only compiled supports daemon, report, hook, and the other types as GSH templates, so all institutional code — templates, provisioners, daemons — is consolidated on the one GSH template screen with type, mode, and compile status in a single view. Interpreted daemons and scripts live outside it (e.g. as OtherJobScript config), scattered across files and hosts. |
| **Documentation and cross-institution code sharing** | — | **Better** | A single language is far easier to document once and to share between institutions. Multiple languages are harder to harmonize; if institutions are not aligned on one language, shared examples and libraries fragment. |

## Migration help

Migration effort is the only real cost of standardizing, and AI has made it nearly free. **Reach out in Slack and I (Chris Hyzer) will convert your interpreted templates, provisioners, and GSH daemons or scripts to compiled Java for you using Claude, instantly.** Conversion is behavior-preserving (same logic, same config), the interpreted version keeps running until you flip the mode — and in practice the conversion pass usually surfaces and fixes latent bugs along the way.
