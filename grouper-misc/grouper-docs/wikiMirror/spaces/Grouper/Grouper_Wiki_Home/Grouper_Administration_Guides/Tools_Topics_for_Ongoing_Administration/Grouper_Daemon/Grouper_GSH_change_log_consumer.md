---
title: "Grouper GSH change log consumer"
space: Grouper
pageId: 28547983
version: 6
lastUpdated: 2026-07-01T05:45:37.269Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547983/Grouper+GSH+change+log+consumer
---

GSH [change log consumers](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545225/Change+log+consumers) are available in Grouper 4.1.5+.

## Configure a new GSH change log consumer in the UI

Here is the source for this example:

```
long lastSequenceProcessed = -1;
for (EsbEventContainer esbEventContainer : gsh_builtin_esbEventContainers) { 
  EsbEvent esbEvent = esbEventContainer.getEsbEvent(); 
  gsh_builtin_debugMap.put(esbEventContainer.getSequenceNumber() + "_" + esbEvent.getGroupName(), esbEvent.getSourceId() + "_" + esbEvent.getSubjectId()); 
  gsh_builtin_hib3GrouperLoaderLog.addInsertCount(1); 
  lastSequenceProcessed = esbEventContainer.getSequenceNumber(); 
} 
return lastSequenceProcessed;

```

The output map will be printed to the daemon logs

```
method: dispatchEventList, eventCount: 1, lastSequenceAvailable: 1902, changeLogFileType: script, 1902_test:testGroup: jdbc_test.subject.3, lastSequenceProcessed: 1902, tookMillis: 755
```

Here is another example that uses a file logger

```
    long lastSequenceProcessed = -1;
    String logLabel = "changeLogGshTest";
    try {
      boolean loggerInitted = GrouperLoaderLogger.initializeThreadLocalMap(logLabel);
    
      gsh_builtin_hib3GrouperLoaderLog.appendJobMessage("Starting testIIQChangeLogScript");
      for (EsbEventContainer esbEventContainer : gsh_builtin_esbEventContainers) {
        EsbEvent esbEvent = esbEventContainer.getEsbEvent();
        gsh_builtin_debugMap.put(esbEventContainer.getSequenceNumber() + "_" + esbEvent.getGroupName(), esbEvent.getSourceId() + "_" + esbEvent.getSubjectId());
        gsh_builtin_hib3GrouperLoaderLog.appendJobMessage(esbEventContainer.getSequenceNumber() + "_" + esbEvent.getGroupName() + ", " + esbEvent.getSourceId() + "_" + esbEvent.getSubjectId());
        gsh_builtin_hib3GrouperLoaderLog.addUpdateCount(1);
        lastSequenceProcessed = esbEventContainer.getSequenceNumber();
        GrouperLoaderLogger.addLogEntry(logLabel, "" + esbEventContainer.getSequenceNumber(), esbEvent.getGroupName() + ", " + esbEvent.getSourceId() + "_" + esbEvent.getSubjectId());
      }
    } finally {
      GrouperLoaderLogger.doTheLogging(logLabel);
    }
    return lastSequenceProcessed;

```

### Stopping Daemon Jobs

In Grouper 5.8.1+, it's possible to stop running daemon jobs (that are not stuck). For example, the job might be in the middle of a lot of changes that are being processed and you'd like to stop it. To allow this functionality with your job, you would need to add the following line of code to various places in your script. For example, you may add it within a loop or after some amount of work is done.

```
GrouperDaemonUtils.stopProcessingIfJobPaused();
```

Normally, this line of code executes very quickly and doesn't do anything. But if a Grouper Admin goes into the Daemon Jobs page in the Grouper UI and disables your job, after a few seconds (~10s), the Grouper Daemon engine will realize that the job has been disabled. After that, the next time your script executes the line of code above, it will throw a RuntimeException. So assuming your script doesn't catch the exception, the job will then end.
