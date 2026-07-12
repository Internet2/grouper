---
title: "Writing GSH templates in Java"
space: Grouper
pageId: 28549690
version: 8
lastUpdated: 2026-07-11T17:25:32.632Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549690/Writing+GSH+templates+in+Java
---

Starting in Grouper v7.3.0+, a GSH template can be authored as **compiled Java** instead of an interpreted Groovy script. The template body is real Java, compiled by the Java compiler (`javac`) against the running Grouper when you save it, loaded into a per-template classloader, and run directly — at full JIT speed, with real Java stack traces.

This page covers how to write compiled templates. Interpreted Groovy templates still work (and remain the only option before v7.3.0); they are documented separately.

## Why compiled Java

- **Errors at save, not at runtime.** A compile error (a typo, a wrong method name, a signature that changed in a Grouper upgrade) is caught when you save the template, with line and column, on the screen of the person who introduced it — not the first time a daemon or rule fires it.
- **Upgrade readiness.** The GSH templates inventory screen shows the compile status of every compiled template against the running Grouper, so after an upgrade you see the broken list on one screen.
- **No Groovy workarounds.** Real Java means multi-line statements, method chaining, multi-line string concatenation, array initializers (`new String[] {"a","b"}`), switch statements, and a literal `$` in a double-quoted string all just work.
- **Real tooling.** Full IDE refactor, find-usages, JUnit, and debugger support, plus real stack traces with real line numbers.
- **Faster.** The body runs as compiled Java at JIT speed, with no Groovy interpreter overhead. In one production measurement — a roughly 180 second hourly email-routing daemon — converting it from interpreted to compiled lowered the median run time about 15% (around 30 seconds). The gain is the interpreter CPU slice, so loop-heavy templates benefit more and I/O-bound ones less.

## Turning on compiled mode

On the GSH template config edit screen, set **Mode** to *Compiled Java* (`templateMode=compiled`) and pick the **Type**. The available types depend on the mode:

| Mode | Available types |
| --- | --- |
| Interpreted Groovy/Java (legacy) | gsh, abac, provisioner |
| Compiled Java | all of the above plus daemon, daemonChangeLog, report, customUi, hook, library |

The source goes in the same place as before — inline in config (the default) or a container file (`gshTemplateSourceType=file` with a `gshTemplateFileName` path). Both feed the same compile-on-save and runtime machinery.

## The class

Every compiled template is one public top-level class with:

- a package declaration (any package, e.g. `package edu.institution.grouper.gsh;`),
- a unique public class name (do not reuse a name used by another template),
- a public no-argument constructor (the implicit default constructor is fine),
- your own `import` statements (write only the imports you need).

## Base class and method per type

| templateType | Extend | Implement / override |
| --- | --- | --- |
| gsh, abac | `GshTemplateV2` | `void gshRunLogic(GshTemplateV2input, GshTemplateV2output)` |
| provisioner | `GshTemplateProvisionerBase` | (provisioner framework methods) |
| daemon | `GrouperTemplateDaemon` | `void runDaemon(OtherJobTemplateInput)` |
| daemonChangeLog | `GrouperTemplateDaemonChangeLog` | `long processRecords(EsbPublisherChangeLogScript)` |
| report | `GrouperTemplateReport` | `void runReport(GshReportRuntime)` |
| customUi | `GrouperTemplateCustomUi` | `runOnJoin` / `runOnLeave` |
| hook | one of the hook base classes (`GroupHooks`, `MembershipHooks`, ...) | the event methods you care about |
| library | (none) | public methods other templates call |

If a compiled class extends the wrong base for its type, the save is blocked with a clear error (and the dispatcher would surface a clear error at run time as well).

## A minimal gsh template

`package edu.institution.grouper.gsh; import edu.internet2.middleware.grouper.GrouperSession; import edu.internet2.middleware.grouper.GroupSave; import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2; import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input; import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output; import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateOutput; public class CreateWorkingGroupTemplate extends GshTemplateV2 { @Override public void gshRunLogic(GshTemplateV2input gshTemplateV2input, GshTemplateV2output gshTemplateV2output) { GshTemplateOutput output = gshTemplateV2output.getGsh_builtin_gshTemplateOutput(); GrouperSession grouperSession = gshTemplateV2input.getGsh_builtin_grouperSession(); String stemName = gshTemplateV2input.getGsh_builtin_ownerStemName(); String extension = gshTemplateV2input.getGsh_builtin_inputString("gsh_input_groupExtension"); if (extension == null || extension.trim().length() == 0) { output.addValidationLine("gsh_input_groupExtension is required"); return; } new GroupSave(grouperSession) .assignName(stemName + ":" + extension) .assignCreateParentStemsIfNotExist(true) .save(); output.addOutputLine("Created group " + stemName + ":" + extension); } }`

## Inputs (gsh and abac)

Input variable names start with `gsh_input_`. Read them from the input bean, e.g. `gshTemplateV2input.getGsh_builtin_inputString("gsh_input_myValue")` (there are typed variants for boolean, integer, file, etc.). Write output with `gshTemplateV2output.getGsh_builtin_gshTemplateOutput().addOutputLine("...")`. ABAC templates should not print to the screen or validate.

## Static state, classloaders, and hot-reload

- Each compiled template lives in its own classloader; one template = one class.
- Static fields persist across executions on the same JVM until the source changes (a save produces a new class with fresh statics) or the JVM restarts. They are **not** shared across cluster nodes.
- For authoritative, cross-cluster state use the database. Statics are per-node caches only.
- Do not register the template's classes in long-lived global maps (JDBC drivers, MBeans, log contexts, etc.); that pins the classloader and prevents reload. Internal statics on the template class are fine.

## Sharing logic between templates

Templates cannot import each other's classes (separate classloaders). Three ways to share:

1. **Shared jar.** Put helpers in a regular jar deployed alongside `grouper.jar`; every template can `import` them. Full type-checking; not hot-reloadable.
2. **Library template via interface.** Declare an interface in the shared jar, implement it in a `library` template, fetch and cast:
  
  `MyInterface impl = (MyInterface) GshTemplateClassLoaderRegistry.instanceForTemplate("myLibraryConfigId"); impl.someMethod(...);`
3. **Library template via reflection.** No shared jar; call by method name:
  
  `Object impl = GshTemplateClassLoaderRegistry.instanceForTemplate("myLibraryConfigId"); String result = (String) GrouperUtil.callMethod(impl.getClass(), impl, "someMethod", String.class, argValue);`

To invoke another full GSH template (with its own inputs/outputs), use `GshTemplateExec` by config id:

`GshTemplateInput childInput = new GshTemplateInput(); childInput.assignName("gsh_input_userId"); childInput.assignValue(userId); GshTemplateExecOutput childOutput = new GshTemplateExec() .assignConfigId("otherTemplateConfigId") .addGshTemplateInput(childInput) .execute();`

## Compile on save and the inventory screen

Save runs the compiler and type-specific validation before persisting. On a compile error the save is blocked and the diagnostics render inline with line and column; the previously saved version stays active, so you can iterate on a broken template without breaking the running one. The GSH templates list shows each template's type, mode, source location, and compile status, with filters for migration backlog (interpreted) and broken artifacts (compile failed).
