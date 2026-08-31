---
title: "Debugging a compiled Java GSH template in an IDE"
space: Grouper
pageId: 28555781
version: 3
lastUpdated: 2026-07-01T05:37:29.679Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555781/Debugging+a+compiled+Java+GSH+template+in+an+IDE
---

A compiled GSH template (Grouper v7.3.0+) is real Java with a deterministic class name (derived from your source, not invented by the Groovy engine), so a developer with a Grouper development environment can attach a debugger, set breakpoints in the template source, and step through it — something the interpreted Groovy path effectively prevents, because Groovy's runtime-invented class names defeat IDE breakpoint resolution.

This is an advanced workflow for non-trivial template work. For everyday authoring, compile-on-save plus the real stack traces in the logs are usually enough.

## Prerequisites

- A local Grouper development environment you can run and attach to (the daemon, UI, or WS process — whichever dispatches your template type).
- The template's Java source in your IDE as a normal class (same package and class name as the deployed template), so the IDE can map breakpoints to lines.

## Steps

1. **Run the Grouper process with debugging enabled.** Start the JVM with the standard remote-debug agent, for example:-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8000
2. **Put the template source in your IDE.** Create a class with the same package and class name as the template (the same source you saved in the config). The IDE compiles it for breakpoint mapping; the running Grouper uses its own compiled copy.
3. **Attach a remote debugger** from the IDE to the JVM's debug port (8000 above).
4. **Set breakpoints** in the template's method (`gshRunLogic`, `runDaemon`, `processRecords`, `runReport`, `runOnJoin`/`runOnLeave`, or the hook event method).
5. **Fire the template** the way it normally runs (click the action in the UI, run the daemon job, trigger the event) and the debugger stops at your breakpoint. Inspect variables, step, and evaluate expressions as with any Java code.

## Notes

- **Class identity.** The template runs in its own per-template classloader. The class name and line numbers match your source, so breakpoints resolve and stack traces read normally.
- **Hot-reload.** Saving the template in the UI produces a new class version on the next execute. If you change the source, re-save it (and keep your IDE copy in sync) so the running class and your breakpoints line up.
- **Multi-JVM.** In a clustered deployment, attach to the specific node that runs the template (the daemon node for daemons, a UI node for custom UI and template runs, etc.).
- **Exceptions.** A compiled template's exceptions carry the real Java stack trace with real line numbers in the logs — often enough to locate a problem without attaching a debugger at all.
