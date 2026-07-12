---
title: "Migrate a GSH change-log daemon to compiled Java"
space: Grouper
pageId: 28555818
version: 3
lastUpdated: 2026-07-01T05:37:24.645Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555818/Migrate+a+GSH+change-log+daemon+to+compiled+Java
---

## Today vs compiled

A change-log script consumer (`EsbPublisherChangeLogScript`) runs a Groovy body today:

changeLog.consumer.myConsumer.publisher.class = edu.internet2.middleware.grouper.app.loader.EsbPublisherChangeLogScript changeLog.consumer.myConsumer.quartzCron = 0 * * * * ? changeLog.consumer.myConsumer.changeLogScriptType = gsh # default changeLog.consumer.myConsumer.changeLogScriptSource = ... groovy ... # or changeLogFileName To convert: create a compiled GSH template (`templateType=daemonChangeLog`, `templateMode=compiled`) whose body extends `GrouperTemplateDaemonChangeLog` and implements `processRecords` (returning the last successfully processed sequence number as a `long`, or `-1` for no advance), then switch the consumer to it:

changeLog.consumer.myConsumer.publisher.class = edu.internet2.middleware.grouper.app.loader.EsbPublisherChangeLogScript changeLog.consumer.myConsumer.quartzCron = 0 * * * * ? changeLog.consumer.myConsumer.changeLogScriptType = compiledJava changeLog.consumer.myConsumer.gshTemplateConfigId = myChangeLogTemplate 

## Before (interpreted change-log body)

// gsh_builtin_esbEventContainers is prepended; return the last sequence processed long lastSequenceProcessed = -1; for (EsbEventContainer esbEventContainer : gsh_builtin_esbEventContainers) { // ... handle the event ... lastSequenceProcessed = esbEventContainer.getSequenceNumber(); } return lastSequenceProcessed; 

## After (compiled GrouperTemplateDaemonChangeLog)

package edu.institution.grouper.gsh; import edu.internet2.middleware.grouper.app.gsh.template.GrouperTemplateDaemonChangeLog; import edu.internet2.middleware.grouper.app.loader.EsbPublisherChangeLogScript; import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEventContainer; public class MyChangeLogTemplate extends GrouperTemplateDaemonChangeLog { @Override public long processRecords(EsbPublisherChangeLogScript esbPublisherChangeLogScript) { long lastSequenceProcessed = -1; for (EsbEventContainer esbEventContainer : esbPublisherChangeLogScript.getEsbEventContainers()) { // ... handle esbEventContainer.getEsbEvent() ... lastSequenceProcessed = esbEventContainer.getSequenceNumber(); } return lastSequenceProcessed; } }
