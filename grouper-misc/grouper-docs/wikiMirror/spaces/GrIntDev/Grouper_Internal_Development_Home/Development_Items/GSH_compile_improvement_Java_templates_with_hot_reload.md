---
title: "GSH compile improvement — Java templates with hot-reload"
space: GrIntDev
pageId: 48792491
version: 3
lastUpdated: 2026-07-12T06:45:23.770Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792491/GSH+compile+improvement+Java+templates+with+hot-reload
---

**Status:** v7 implementation complete — Phases 1–7 shipped on `GROUPER_7_BRANCH`; only Phase 8 (v9/v10 cutover) and a few explicitly-deferred items remain (see "Deferred" below)  
**Shipped (GROUPER_7_BRANCH):** `GRP-7006` Phase 2 javac wrapper · `GRP-7010` Phase 4a registry · `GRP-7011` Phases 1+3 type framework · `GRP-7026` (+commit 2) Phase 4b/c foundation + shared dispatch helper + real Java stack traces · `GRP-7027` library · `GRP-7028` daemon · `GRP-7029` provisioner · `GRP-7030` change-log daemon + custom UI · `GRP-7031` report · `GRP-7032` hook · `GRP-7033` Phase 5 mode picker + compile-on-save · `GRP-7034` inventory compile-status columns · `GRP-7035` base-class validation on save · `GRP-7036` inventory filters · Phase 6 docs (compiled `aiGsh` instructions/examples + wiki drafts).  
**Deferred (future JIRAs, not v7-blocking):** async background pre-warm + progress bar for the inventory (synchronous-with-cache is sufficient now); converting the `aiGsh.txt` example bodies to compiled Java; all Phase 8 v9/v10 cutover work (remove Groovy path, the v9 upgrade-check that fails on remaining interpreted templates, drop the Groovy dependency).  
**Target branches:** `GROUPER_7_BRANCH` (build new infrastructure, additive), v8 (continues to support both old and new), `GROUPER_9_BRANCH` / v10 (removal of Groovy and jar-based hooks)  
**Release cadence note:** Grouper odd-number major versions (v7, v9) are dev releases where new enhancements land; even-number major versions (v8, v10) are stable releases without major new enhancements. The migration window for institutions to convert Groovy artifacts and jar-based hooks to the new model is the v7/v8 lifetime. v9/v10 removes the old paths.  
**Scope:** every Groovy evaluation site in running Grouper, plus jar-deploy custom hooks. After v9/v10, no Groovy remains in the running product — only the standalone `gsh` terminal binary keeps it — and jar-deploy hooks are no longer supported (custom hooks live in config as DB-stored Java source).

## Problem

Today's GSH artifacts are authored as Groovy script bodies stored in DB-backed config. The pain points are real and accumulating:

- **Errors only show up at runtime.** A typo in a saved Groovy body — `session.stoop()` instead of `session.stop()`, or a misspelled variable on a `def` — passes save with no warning, then explodes the first time a daemon, rule, or admin click fires it. Often at 3am. The author who introduced the bug is not in the loop.
- **Grouper upgrades are silently risky.** When Grouper itself upgrades and an API signature or library version changes, every Groovy template and daemon body that touches the changed surface is potentially broken — and there is no way to find out without running each one. Exercising hundreds of templates and daemons against a new Grouper version is not feasible for any institution, so upgrades land with latent breakage that surfaces over weeks as individual artifacts happen to fire.
- **Dynamic Groovy is slow for CPU-bound work.** Every method call goes through MetaClass lookup. Templates that iterate large membership sets, walk change-log batches, or do tight string manipulation run 3–20× slower than equivalent Java.
- **Error reporting is jury-rigged.** Today's GSH template runtime contains complex logic to back-calculate line numbers of errors out of Groovy stack traces — because Groovy invents its own internal class names that don't map cleanly back to the user's source. Java stack traces give us real line numbers for free, and that whole block of complexity goes away.
- **Authoring tooling is weak.** IDE refactor, find-usages, debugger, JUnit testing against template logic — all marginal in Groovy compared to Java. Today there is no practical way for a developer to attach a debugger to a running template; Groovy's invented class names defeat IDE breakpoints.
- **Arbitrary syntactic limitations.** Current GSH has annoying rules that have nothing to do with the author's intent: lines that are valid Java cannot wrap (e.g. concatenating strings across lines with a leading `+` on the next line breaks), the `$` dollar sign cannot appear in a double-quoted string, etc. These are Groovy-parser side effects that template authors hit constantly and have to work around. None of them apply in real Java.
- **No type-checked contract for site-local code.** Institution-authored Groovy bodies grow over time and become impossible to refactor safely because nothing checks call sites.

## Goal

Every Groovy evaluation site in running Grouper becomes Java: compiled, type-checked at save, hot-reloadable across a multi-JVM cluster, full JIT speed. The Groovy authoring path keeps working through v7/v8; v9/v10 removes it. The only Groovy that remains after v9/v10 is the standalone `gsh` terminal binary, which is out of scope.

Second outcome: a **unified admin inventory** of every piece of custom Java running anywhere in the deployment. Once the compile-on-save / per-class-classloader / hot-reload infrastructure exists, it's general-purpose — hooks and future extension points that today require jar deploys can adopt the same pattern.

## In-scope types

| # | Type | What fires it |
| --- | --- | --- |
| 1 | GSH template | Admin click in UI, WS call |
| 2 | Scripted daemon | Scheduler on a cron |
| 3 | Custom UI | UI screen render or action |
| 4 | ABAC pattern | Constructing a script for an ABAC scripted group |
| 5 | GSH provisioner | Provisioning loop / change-log dispatcher |
| 6 | JEXL script tester | Per-user, per-click test from the admin UI screen |
| 7 | Hooks | Grouper event hook (group/membership/stem/attribute/audit/etc.) |
| 8 | Library template | Called by other GSH templates for shared logic — no direct dispatcher |

Types 1–5 are persistent configured artifacts whose body becomes a class that's compiled, cached, run many times, holds per-node state in statics, hot-reloads on edit. Type 6 (JEXL script tester) is per-user per-click — same compiler and classloader machinery, different lifetime. Type 7 (hooks) removes the jar-deploy requirement for custom hooks. Type 8 (library template) is shared logic callable by other templates.

**One base class per type, matching the type's framework.** The compile/load infrastructure doesn't care which base; the registry returns `Class<?>` and each type's dispatcher casts via `asSubclass()`.

| `templateType` | Base class the template extends |
| --- | --- |
| `gsh` | `GshTemplateV2` (existing) |
| `abac` | `GshTemplateV2` (existing) |
| `provisioner` | `GshTemplateProvisionerBase` (existing — the framework's natural provisioner base; the legacy "wrap in GshTemplateV2 and attach via output" pattern is dropped) |
| `daemon` | `GrouperTemplateDaemon` (new; method `runDaemon(OtherJobTemplateInput)`) |
| `daemonChangeLog` | `GrouperTemplateDaemonChangeLog` (new; method `processRecords(EsbPublisherChangeLogScript)` returning `long`) |
| `report` | `GrouperTemplateReport` (new; method `runReport(GshReportRuntime)`) |
| `customUi` | `GrouperTemplateCustomUi` (new, in the grouper-ui module; methods `runOnJoin` / `runOnLeave` with throws-default per action; more action methods added as the framework grows) |
| `hook` | one of the 17 existing hook abstract classes — `GroupHooks`, `MembershipHooks`, `StemHooks`, etc. No new base class is introduced for hooks; the author picks the existing framework class that matches the event surface they care about. |
| `library` | (none required — library templates are plain classes with methods that other templates call via the registry) |

Each type's dispatcher (scheduler for daemons, change-log processor, `ReportConfigType.GSH`, `CustomUiContainer`, hooks framework, `GshTemplateExec`) resolves through the registry, casts the `Class<?>` to the appropriate base, instantiates, and calls the type-appropriate method. A clean `ClassCastException` surfaces if the author got the base wrong.

### Type 6 — JEXL script tester

Per-user, per-click. The screen's inputs are assembled into a small Java class with one method per part, compiled, defined in a per-user-session `ByteArrayClassLoader`, executed, then dropped. Each click is a fresh compile (~100–300ms, invisible for a debug tool); statics make no sense in this lifetime. Same compile + classloader machinery as everything else — stack traces carry real Java line numbers.

### Type 8 — library template

A plain Java class whose methods are called by other templates. Runs in its own per-template classloader, hot-reloadable on save. No dispatcher of its own — invocation happens from other templates via the "Shared logic" section below. No required base class — library templates are just classes with methods.

### Type 7 — hooks

Today: jar-deploy custom hooks, register the FQN in `grouper.properties`, restart to change. Now: hook source can live in config (or a container file), compile on save, hot-reload on edit. The hook registration path gains a branch for DB-stored sources alongside the existing classpath-FQN path.

Performance is full JIT speed — hooks fire on real Grouper events (millions of invocations per day at large institutions) and the per-template classloader doesn't slow them down. Jar-deployed hooks coexist with DB-stored hooks during v7/v8; v9/v10 removes the jar path. Phase 7 details (which hook interfaces light up first, error handling, etc.) are worked through then.

## Why Java only

Groovy can't deliver the defining requirement: *does this code compile, against the actual Grouper API surface, before it runs?* Dominant Groovy style — calls on `def` variables, dynamic property access, anything routed through MetaClass — gets no compile-time checking. The only Groovy variant that does is `@CompileStatic` with explicit types everywhere, which is Java in different syntax. Shipping a system that *claims* compile-check-on-save but silently skips checking the parts that matter most is worse than the status quo.

Setting aside compile-checking: Groovy is 3–20× slower on CPU-bound code (without `@CompileStatic`), its runtime-invented class names defeat IDE debugging and stack-trace line numbers, and supporting it as a second backend doubles documentation/test/UI surface area for zero added user capability.

Existing Groovy templates convert to Java reliably via AI — most GSH bodies are variable declarations, SQL strings, Grouper API calls, and conditionals that translate one-to-one. Authors paste the body into Claude; support is also available through the incommon-grouper Slack channel. Phase 6 ships a conversion guide with worked examples.

**Imports.** Today the GSH runtime prepends imports automatically. With Java templates, the author writes their own `import` statements. AI assistance produces the correct imports; the IDE-debugging workflow auto-imports as you type. Phase 6 docs include the common import block.

## Unified admin inventory — existing GSH template screen

The existing GSH template admin UI screen is extended in place to become the single pane of glass for **all custom Java (and remaining Groovy) across every type** — GSH templates, scripted daemons, custom UI, ABAC patterns, GSH provisioners, JEXL script tester definitions, and hooks. No new screen is built. One row per custom artifact regardless of type, with at minimum these columns:

- **Type** — template, daemon, custom UI, ABAC, provisioner, hook, etc.
- **Name / id** — identifier of the artifact.
- **Mode** — interpreted Groovy/Java (legacy) or compiled Java.
- **Source location** — for Java artifacts: inline config or container file. (Hooks, until the v9/v10 cutover, can also be jar-deployed; those show as their own kind.)
- **Compile status** — for Java artifacts, whether the source compiles successfully on the currently running Grouper (operator already knows which version that is).
- **Last compiled** — timestamp.

Upgrade-readiness check is implicit: opening the screen on a JVM for the first time after a Grouper upgrade kicks off a background compile of every listed Java artifact, with a progress indicator at the top and per-row status updates as each finishes. Operators open this one screen after an upgrade and watch the broken-artifact list materialize — instead of discovering breakage weeks later when each artifact happens to fire.

Filters answer the obvious questions: migration backlog (mode = interpreted), broken artifacts (compile status = failed), ownership view (filter by type or author). Drill-in opens the existing edit screen.

## Runtime design

### One classloader per template

Each compiled Java template lives in its own `ByteArrayClassLoader` with Grouper's app classloader as parent. One template = one class = one loader. Hot-reload = drop old loader, create new one in the registry. In-flight executions hold the old loader; once they drain it's unreferenced and GC'd. Standard pattern (Jenkins pipelines, Drools, Camel).

### Source location — always dynamically compiled

Every Java GSH template is dynamically compiled at runtime — there is no pre-compiled / classpath path for templates themselves. (Helper classes used *by* templates are a separate concern; see the "Shared logic" subsection below.)

A template's Java source can live in one of two places, set per-template via config:

| Source location | Where the source string comes from | Hot-reload? | Typical use |
| --- | --- | --- | --- |
| Inline in config | The existing `gshTemplate` config property holds the source body (when `gshTemplateSourceType=textArea`, the default). Grouper config itself layers from properties files in the container with database overrides, per the existing config system. | Yes — change the value, next execute recompiles | Default. Edited in the admin UI, or pushed via config management. |
| Container file | The existing `gshTemplateFileName` config property holds an absolute path to a source file in the container (when `gshTemplateSourceType=file`). Pointed-to file is read on each execute. | Yes — change the file contents, next execute recompiles | Institutions that prefer to keep template source in a git-tracked file baked into the container image. |

Each template is configured with one source location or the other — it's a normal config choice, not an override relationship. Both paths feed the same per-template `ByteArrayClassLoader` machinery with the same source-hash invalidation. The registry doesn't know or care which location produced a given source string; it just hashes, caches, and recompiles when the hash changes.

### Shared logic and inter-template calls

Templates can't share logic directly — each has its own per-template classloader, so a template's helpers aren't visible to another template's `import`. Same constraint as today, now explicit. Three ways to share, in order of type-safety:

#### Option A — helpers in an institution shared jar

Put shared logic in a regular Java class inside a jar deployed alongside `grouper.jar` (e.g. `institution-shared.jar`). The jar lives on the app classloader so every template can `import` from it. Direct calls, full type-checking, full IDE support. Static state is shared across all templates on the JVM.

// In institution-shared.jar, parent classloader package edu.institution.grouper.lib; public class InstitutionSubjectHelper { public static String resolveAffiliation(String userId) { ... } } // In any DB template String affiliation = InstitutionSubjectHelper.resolveAffiliation(userId); **Trade-off:** not hot-reloadable. Changing the helper requires rebuilding the jar and redeploying the container.

#### Option B — library template + interface in the parent jar

Interface in the shared jar declares the methods; implementation is a library template (DB-stored, hot-reloadable). Other templates fetch the instance through the registry, cast to the parent-loader interface, call directly. Interface lives in the parent loader so both templates agree on the same `Class` object — type-checked at compile time.

// In institution-shared.jar, parent classloader — the type contract package edu.institution.grouper.lib; public interface InstitutionSubjectResolver { String resolveAffiliation(String userId); String resolveOrgCode(String userId); } // As a DB template, type=library, hot-reloadable implementation. // Library templates have no required base class — they're plain Java // classes whose methods are called by other templates. public class InstitutionSubjectResolverImpl implements InstitutionSubjectResolver { @Override public String resolveAffiliation(String userId) { ... } @Override public String resolveOrgCode(String userId) { ... } } // In another DB template — calls the library InstitutionSubjectResolver resolver = (InstitutionSubjectResolver) GshTemplateRegistry.instanceForTemplate("InstitutionSubjectResolverImpl"); String affiliation = resolver.resolveAffiliation(userId); Direct `invokevirtual` through the interface, full JIT speed. **Trade-off:** the interface is in the parent jar, so adding a method or changing a signature still requires a deploy. Hot-reload only covers the implementation. Best when interface signatures are stable and the implementation evolves.

#### Option C — library template called via reflection (no parent-jar interface)

Many institutions find jar build-and-deploy cycles to be friction they'd rather avoid for institution-internal helpers. Skip the interface entirely. Put the shared methods on a library template and call them by name via `GrouperUtil.callMethod` — wraps Java reflection, doesn't throw checked exceptions.

// As a DB template, type=library — no parent-jar interface needed, // no required base class. Just the institution's own methods. public class InstitutionSubjectResolverImpl { public String resolveAffiliation(String userId) { ... } public String resolveOrgCode(String userId) { ... } } // In another DB template — call by method name through GrouperUtil Object resolver = GshTemplateRegistry.instanceForTemplate("InstitutionSubjectResolverImpl"); String affiliation = (String) GrouperUtil.callMethod( resolver.getClass(), resolver, "resolveAffiliation", String.class, userId); String orgCode = (String) GrouperUtil.callMethod( resolver.getClass(), resolver, "resolveOrgCode", String.class, userId); A good option, not a fallback. The string method name isn't compile-checked, but method names are stable and reviewable, and you sidestep the jar build-and-deploy cycle entirely. **Trade-off:** typos don't surface until call time. AI assistance during authoring produces correct call sites.

#### Calling a non-library template by name (different from Options A–C)

For invoking another full GSH template (with its own inputs/outputs), use the existing template-invocation API. Works without reflection — `GshTemplateExec` and the input/output types live in the parent classloader.

// Build the input the called template expects (parent-loader types) GshTemplateInput childInput = new GshTemplateInput(); childInput.assignParamValue("userId", in.retrieveInputParamValue("userId")); // Invoke the other template by configId — no import of its class GshTemplateExecOutput childOutput = new GshTemplateExec() .assignConfigId("computeStaffAffiliation") .assignGshTemplateInput(childInput) .execute(); String affiliation = childOutput.getGshTemplateOutput() .retrieveSingleOutputLineValue("affiliation"); 

### Source-hash cache at the registry layer

The registry holds, per template config id, a tuple of `(sourceHash, ByteArrayClassLoader, Class)`. Each `resolve(templateName, javaSource)` call:

1. Hashes the provided source.
2. Compares to the cached source hash for this template.
3. If same → returns the cached class. Statics on the class survive.
4. If different → compiles via `javax.tools.JavaCompiler`, defines the class in a fresh `ByteArrayClassLoader`, atomically swaps the registry entry, returns the new class. Old loader drains and is GC'd.

Registry itself has no TTL — every `resolve` hashes the source it's handed. Mechanically identical to `GshTemplateExec` line 742. Multi-JVM freshness rides the existing `GrouperConfig` cadence — same as V2 Groovy today.

**Upstream callers may cache for performance.** The hook framework (Phase 7) wraps the registry in a ~30s per-JVM cache of resolved instances, refreshed asynchronously (hot path never blocks) and busted on UI save (saving admin sees their change within ~1s). Similar caches may appear in other Phase 7 dispatchers if profiling needs them. Cadence decisions live above the registry, where each dispatcher knows its invocation pattern.

### Static state on a template class

Static fields persist across executions on the same JVM until source change (new loader → new class → fresh statics) or JVM restart. Useful for per-node caches, computed-once lazy initializers, invocation counters.

Boundaries for template authors:

- Statics persist **across executions on the same JVM**.
- Statics are **wiped on save** (source change → new version → fresh class).
- Statics are **not shared across JVMs**. Two nodes accumulate independently.
- Statics are wiped on JVM restart.
- For authoritative state (membership data, audit trails, anything cross-cluster), **use the database**. Statics are per-node caches, not source of truth.
- Templates must not register themselves or any of their classes in long-lived global maps (JDBC drivers, bean introspectors, log4j contexts, MBean servers, etc.). Such registrations pin the classloader and prevent unload. Internal statics on the template class are fine.

### Performance

Once loaded, classes from a `ByteArrayClassLoader` run at full JIT speed — HotSpot doesn't care where bytecode came from. One-time costs: `defineClass` (~1–5 ms, paid on first execute after a source change) and normal JIT warmup.

### IDE debugging

Each template compiles to a class with a deterministic name (from the template config id, not invented by Groovy). A developer with a Grouper dev environment can attach an IDE debugger, set breakpoints in the template source, and step through. Advanced workflow for non-trivial work — surfaces bugs faster than print-debugging. Today's Groovy path effectively denies this: Groovy's runtime-invented class names break IDE breakpoint resolution.

## Compile on save

Save through the template config UI invokes `javax.tools.JavaCompiler` (in-process, JDK-only, no extra dependency) — in-memory source and output, classpath = running JVM's classpath, errors collected with line/column.

Single **Save** button — no separate Validate. Save runs the compiler and, on success, type-specific validation (correct framework base, required methods overridden). All before any config is written:

- Compile error → save blocked, diagnostics render inline with line/column + caret. No config change.
- Compile OK but type validation fails (wrong superclass, missing required override) → save blocked with a clear message. No config change.
- Both pass → config persists; the registry picks up the new source on its next resolve.

The existing saved version stays active on any failure path. Author iterates on a broken template without bricking the running one. A **"last compiled OK" badge** on the list page makes template health visible at a glance.

This is the largest user-facing improvement. Typos surface on the screen of the author who introduced them, before they ever run in production. Operators get an upgrade-readiness signal too — opening the inventory screen after a Grouper upgrade drives a fresh compile against the new API surface, turning "latent breakage discovered over weeks" into "list of things to fix on one screen."

## Phased plan

### Phase 0 — investigation

Read-only mapping of the current GSH execution path and template config storage. Confirms entry points, caching mechanism, multi-JVM invalidation. **Complete.**

### Phase 1 — per-type base classes

New bases per type. `GshTemplateV2` unchanged (stays the base for `gsh` and `abac`):

- `GrouperTemplateDaemon` (grouper) — abstract `runDaemon(OtherJobTemplateInput)`
- `GrouperTemplateDaemonChangeLog` (grouper) — abstract `processRecords(EsbPublisherChangeLogScript) → long`
- `GrouperTemplateReport` (grouper) — abstract `runReport(GshReportRuntime)`
- `GrouperTemplateCustomUi` (grouper-ui) — non-abstract `runOnJoin` / `runOnLeave` with throws-default per action; future action methods added the same way
- `hook`, `provisioner`, `library`: no new base — reuse the framework's existing class (or none for library)

New input beans where the framework's input doesn't carry template-specific fields: `OtherJobTemplateInput extends OtherJobInput` (adds gshTemplateConfigId, gshTemplateConfig); `CustomUiTemplateInput` bundles the four legacy positional args plus config fields.

No lifecycle hooks (`validateInput`, `dryRun`, etc.) added preemptively. Save-time base-class validation deferred — each Phase 7 dispatcher does its own `asSubclass()` cast; the registry returns `Class<?>`.

### Phase 2 — Java compiler wrapper

New library-level class `GshTemplateJavaCompiler` wrapping `ToolProvider.getSystemJavaCompiler()`. Source in, bytecode + diagnostics out. JUnit tests for compile success, syntax error, unresolved import, type error. Standalone, no integration yet.

### Phase 3 — config storage

No new DB table, no new source-storage properties — existing config infrastructure already covers source location (`gshTemplateSourceType` picks between inline `gshTemplate` and container `gshTemplateFileName`) and template type (`templateType` + `GshTemplateType` enum). Net new:

- **One new property** `grouperGshTemplate.{id}.templateMode` — values `interpreted` (default, legacy) or `compiled` (new Java path).
- **Six new `GshTemplateType` values**: `daemon`, `daemonChangeLog`, `report`, `customUi`, `hook`, `library`. Existing `gsh`, `abac`, `provisioner` unchanged.
- **New enum `GshTemplateMode`** with the two values and a `valueOfIgnoreCase` helper.
- **Externalized text** for the new property and enum values in `[grouper.textNg.en.us](http://grouper.textNg.en.us).base.properties`.
- **Read code** in `GshTemplateConfig` with default `interpreted`.

Existing rows default to `templateMode=interpreted` + existing `templateType`. Java source goes in the existing `gshTemplate` / `gshTemplateFileName` properties; the dispatcher reads `templateMode` to route — interpreted → Groovy engine; compiled → Phase 4a registry.

#### Mode and type interaction

Mode picker above type picker on the edit screen. Available types depend on mode:

| `templateMode` | Available `templateType` options |
| --- | --- |
| `interpreted` (or blank) | `gsh`, `abac`, `provisioner` |
| `compiled` | all of the above plus `daemon`, `daemonChangeLog`, `report`, `customUi`, `hook`, `library` |

Mode is per-template, not global. Compiled and interpreted coexist freely across v7/v8 — an institution can have any mix on the same JVM, even within the same templateType.

### Phase 4 — runtime registry and dispatcher integration

- **4a** — Complete (GRP-7010). `GshTemplateClassLoaderRegistry` with `ByteArrayClassLoader`, lazy compile-and-load, source-hash invalidation. Resolves to `Class<?>`; each dispatcher casts. The registry deliberately does *not* read source — the caller hands it the source string.
- **4b** — foundation: branch at the main GSH execution entry point `GshTemplateExec` (`templateMode == compiled` → registry; else current Groovy path), plus a shared dispatch helper the per-type dispatchers reuse. Covers `gsh` and `abac` (both run through `GshTemplateExec` / `GshTemplateV2`). See the dispatcher-integration table under Phase 7.
- **4c** — retire the back-calculate-line-number-from-Groovy logic (`GrouperGroovysh.handleGshException`) for Java templates (Java stack traces carry real line numbers).

#### 4b foundation — shared dispatch helper

Every new-type dispatcher (Phase 7) repeats the same six steps: read its `gshTemplateConfigId`, load `GshTemplateConfig` and read the source (inline `gshTemplate` or container `gshTemplateFileName` per `gshTemplateSourceType`), `resolve()` through the registry, `asSubclass()` to the expected base, instantiate, then call the type-appropriate method. 4b extracts steps into one place:

- **Source-read** moves out of the private `GshTemplateExec.getGshTemplateFromConfig` into a public `GshTemplateConfig.readSource()`, reused by both `GshTemplateExec` and the helper.
- **`GshTemplateCompiledDispatch.instantiate(configId, config, Class<T> baseClass) → T`** (grouper module, so grouper-ui can call it too): resolves via the registry, casts with a clear error if the author extended the wrong base, instantiates. Parse/compile failures surface the registry diagnostics.

No separate multi-JVM invalidation — source freshness on peer JVMs rides the existing config layer's cadence, same as V2 Groovy today.

### Phase 5 — UI

Shipped — GRP-7033 (mode picker + compile-on-save), GRP-7035 (base-class validation on save), GRP-7034 (inventory compile-status columns), GRP-7036 (inventory filters + filter-panel layout).

- Done (GRP-7033). Mode picker on the template config page: `Interpreted Groovy/Java (legacy)` or `Compiled Java`, with the type options gated by mode.
- Done. For compiled Java: inline source editor or a container-file-path field per template (existing `gshTemplateSourceType`).
- Done (GRP-7033 compile-on-save + GRP-7035 base-class match). Single **Save** button runs compile + base-class validation before persisting; on failure the existing saved version stays active and the error renders inline with line + column.
- Done (GRP-7034). Inventory list shows a per-row compile status (Compiled OK / Compile failed with diagnostics tooltip / Source file missing) with last-compiled time — the "last compiled OK" indicator.
- Done (GRP-7034 columns + GRP-7036 filters), with one part deferred. The existing GSH templates list is the **unified inventory** — every compiled artifact is a `grouperGshTemplate` config — with type, mode, source location, compile status, and filters (mode/status/type). Compile status is computed synchronously on render and cached by source hash; the *async background pre-warm + progress indicator* is deferred (synchronous-with-cache handled 40 templates with no visible delay).
- Done. Externalized text in `[grouper.textNg.en.us](http://grouper.textNg.en.us).base.properties` only.

### Phase 6 — documentation and conversion tooling

Shipped — compiled `aiGsh` instruction set + examples in the repo; three Confluence wiki pages drafted as copy-paste HTML under `grouper/temp/trash/gshCompiled/`.

- Drafted. Wiki page: writing GSH templates in Java (`gshCompiled/writing-gsh-templates-in-java.html`). Sentence-case headers.
- Done (separate compiled files, version-gated to 7.3.0+). AI instructions: new `aiGshInstructionsCompiled.txt` + `aiGshCompiled.txt` (one worked example per type); the legacy `aiGshInstructions.txt`/`aiGsh.txt` stay for interpreted templates and older versions. Deferred: converting the `aiGsh.txt` example bodies to compiled Java.
- Drafted. Migration guide (`gshCompiled/migrating-a-groovy-gsh-template-to-java.html`) with worked before/after, the AI-assist workflow, and a prominent "must migrate before v9" callout.
- Drafted. IDE-debugging guide (`gshCompiled/debugging-a-compiled-gsh-template-in-an-ide.html`).

### Phase 7 — per-type dispatcher integration and migration

Shipped — one JIRA per type: GRP-7027 library, GRP-7028 daemon, GRP-7029 provisioner, GRP-7030 change-log daemon + custom UI, GRP-7031 report, GRP-7032 hook. (gsh/abac landed with the GRP-7026 foundation.)

Each wires its dispatcher (`GshTemplateExec` for gsh/abac, `GshTemplateProvisionerFactory`, `OtherJobScript`, `EsbPublisherChangeLogScript`, `ReportConfigType.GSH`, `CustomUiContainer`, the hooks framework) to route compiled templates through the registry via the GRP-7026 shared dispatch helper. Existing Groovy artifacts and jar-based hooks keep working alongside the new path through v7/v8; v9/v10 removes them.  
**Mapping finding:** only the `gsh`/`abac`/`provisioner` path is template-config-driven today. The other dispatchers run *raw inline Groovy* with no `GshTemplateConfig` link — so "branch on `templateMode`" is literally true only for the main entry point. For the new types each dispatcher needs **new wiring**: a config property to reach a `gshTemplateConfigId`, then the shared 4b helper.

| Type | Dispatcher (current state) | New wiring |
| --- | --- | --- |
| `gsh`, `abac` | `GshTemplateExec.execute()` — config-driven; V1 Groovy interp or V2 compiled-Groovy `GshTemplateV2` subclass, source-hash cache ~L737–804 | 3rd branch: `templateMode==compiled` → registry (this is 4b foundation) |
| `provisioner` | `GrouperProvisioner.provision()` via `GshTemplateProvisionerBase` — separate from `GshTemplateExec` | compiled body extends `GshTemplateProvisionerBase` directly; legacy "wrap in GshTemplateV2 and attach via output" pattern dropped |
| `daemon` | `OtherJobScript.run()` — `scriptType=gsh` runs `GrouperUtil.gshRunScript(rawSource)`; not template-linked | new `scriptType=compiledJava` + `otherJob.{job}.gshTemplateConfigId`; build `OtherJobTemplateInput` via `copyFieldsTo`; `runDaemon` |
| `daemonChangeLog` | `EsbPublisherChangeLogScript.dispatchEventList` — inline Groovy via `gshRunScriptReturnResult` | consumer config → `gshTemplateConfigId`; `processRecords(this)` → set returned `long` on `provisioningSyncConsumerResult.setLastProcessedSequenceNumber` |
| `customUi` | `CustomUiContainer.gshRunScript()` (grouper-ui) — inline Groovy; join/leave share one script, distinguished by `cu_joinGroupButtonPressed`/`cu_leaveGroupButtonPressed` flags | config link; route to `runOnJoin`/`runOnLeave` off the button flag |
| `report` | `ReportConfigType.GSH` — `reportConfigScript` attribute holds raw Groovy | new report config attribute for template id + mode (+ UI); `runReport(gshReportRuntime)` with ThreadLocal set |
| `hook` | hooks framework — `hooks.<domain>.class=FQN` classpath only; instances cached for JVM lifetime in `GrouperHookType.hookTypeMap` | new `hooks.<domain>.gshTemplateConfigIds` merged into resolution alongside FQN classes; ~30s reload cache busted on UI save |
| `library` | none — resolved on demand by other templates | public `GshTemplateClassLoaderRegistry.instanceForTemplate(configId)` accessor; no base class |

**Recommended sequencing:** 4b foundation (gsh/abac + helper) → `library` and `daemon` (cheapest, validate the helper across dispatchers) → `provisioner`, `daemonChangeLog`, `customUi` → `report` and `hook` last (both add persistent config surfaces and UI; hooks is hottest-path and most invasive).

### Phase 8 — v9/v10 cutover

On `GROUPER_9_BRANCH` and continuing through v10, once adoption is confirmed across the v7/v8 migration window:

- Remove Groovy execution from the dispatcher across all six Groovy types (1–6).
- Remove the back-calculate-line-number-from-Groovy logic entirely.
- Remove the jar-deploy hook registration path. All custom hooks must be DB-stored Java source from this point.
- Add upgrade tasks that fail clearly if any config row still has `templateMode=interpreted` or any hook config still references a classpath-only hook FQN that doesn't ship with Grouper, pointing to the migration guide in each error.
- Remove Groovy-related documentation pages.
- Drop the Groovy runtime dependency from the relevant Maven modules. The standalone `gsh` terminal binary keeps Groovy as its own concern; nothing in running Grouper depends on it.

Institutions use the v7/v8 lifetime as the migration window. The inventory screen makes the backlog visible (filter on mode = interpreted, or jar-deployed hooks). v9/v10 surfaces remaining old artifacts via clear upgrade-task failures with pointers to fix.

### Future work

**Command-line `gsh` compile mode.** The standalone `gsh` terminal binary also interprets `.gsh` files via Groovy today. A future enhancement adds `gsh --compile somefile.java` that reuses `GshTemplateJavaCompiler` + `ByteArrayClassLoader` to run compiled Java instead. Needs a new base (working name `GrouperShellScript`) with a single override method. CLI-only — no template config, no dispatcher work. Own JIRA when picked up.

**Other customization areas.** Once the compile-on-save + per-class-classloader + hot-reload infrastructure exists it's a general facility. Future candidates that today require jar deploys (subject-source customizations, audit formatters, WS responders, report generators) can adopt the same pattern with no new architecture. Decisions case by case.

## Open items

- **Naming.** Phase 3 only adds `templateMode`; everything else reuses existing config keys. Confirm `templateMode` / `GshTemplateMode` during Phase 3 implementation.
- **Stale bytecode across Grouper upgrades.** Bytecode is never persisted — each JVM compiles from source on first execute, automatically picking up the new API surface. Combined with the inventory-screen pre-warm, this is a major improvement over Groovy's "find out at runtime."
- **File-source accessibility.** File-source templates (`gshTemplateSourceType=file`) can break at runtime due to deploy mismatches (file removed, permissions changed); inline-config templates can't. Registry surfaces the error in the inventory screen's compile-status column.
- **Adoption metric for v9/v10 cutover.** Inventory screen makes the backlog visible interactively. Open question: also surface a count in the daily admin email / startup log for institutions that don't routinely visit the screen?
- **Deterministic class names.** Compiled class names derive deterministically from the template config id (plus source hash, to keep versions distinct within a JVM lifetime). Phase 4 picks the exact scheme.
- **Short-lived instance / source cache above the registry (Phase 4b).** Phase 4a returns a `Class`; per-execute instantiation + hash compare is wasteful for high-rate types. A ~30s per-template instance cache one layer above the registry, with async refresh and UI-save bust, handles this. Design when we reach 4b; the registry stays the source of truth either way.
- **Same-FQN collision across templates.** "Same FQN" = same package + class name. Two templates with the same FQN load fine (each in its own classloader, distinct `Class` objects) but confuse typed cross-template calls and IDE debugging. Phase 5 validates on save that no other active template shares the FQN; Phase 4a doesn't enforce (registry doesn't know about other templates).
