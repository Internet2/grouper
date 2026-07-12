---
title: "Migrate a GSH scripted daemon to compiled Java"
space: Grouper
pageId: 28555844
version: 2
lastUpdated: 2026-07-01T05:37:17.051Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555844/Migrate+a+GSH+scripted+daemon+to+compiled+Java
---

## Today vs compiled

A scripted daemon (`OtherJobScript`) runs a GSH/Groovy body today:

otherJob.myDaemon.class = edu.internet2.middleware.grouper.app.loader.OtherJobScript otherJob.myDaemon.quartzCron = 0 0 6 * * ? otherJob.myDaemon.scriptType = gsh otherJob.myDaemon.scriptSource = ... groovy ... # or otherJob.myDaemon.fileName To convert: create a compiled GSH template (`templateType=daemon`, `templateMode=compiled`) whose body extends `GrouperTemplateDaemon` and implements `runDaemon`, then point the daemon at it with `scriptType=compiledJava` and a `gshTemplateConfigId`:

otherJob.myDaemon.class = edu.internet2.middleware.grouper.app.loader.OtherJobScript otherJob.myDaemon.quartzCron = 0 0 6 * * ? otherJob.myDaemon.scriptType = compiledJava otherJob.myDaemon.gshTemplateConfigId = myDaemonTemplate Status and counts go on the loader log the framework passes in (`otherJobTemplateInput.getHib3GrouperLoaderLog()`); throw to fail the job. There is no `gsh_builtin_*` scaffolding — you have the input bean directly.

## Before (interpreted gsh daemon body)

// runs with scriptType=gsh; gsh_builtin_* variables are prepended int updated = 0; // ... do work ... gsh_builtin_hib3GrouperLoaderLog.addUpdateCount(updated); 

## After (compiled GrouperTemplateDaemon)

package edu.institution.grouper.gsh; import edu.internet2.middleware.grouper.app.gsh.template.GrouperTemplateDaemon; import edu.internet2.middleware.grouper.app.loader.OtherJobTemplateInput; import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog; public class MyDaemonTemplate extends GrouperTemplateDaemon { @Override public void runDaemon(OtherJobTemplateInput otherJobTemplateInput) { Hib3GrouperLoaderLog loaderLog = otherJobTemplateInput.getHib3GrouperLoaderLog(); int updated = 0; // ... do work ... loaderLog.addUpdateCount(updated); loaderLog.setJobMessage("updated " + updated); } }
