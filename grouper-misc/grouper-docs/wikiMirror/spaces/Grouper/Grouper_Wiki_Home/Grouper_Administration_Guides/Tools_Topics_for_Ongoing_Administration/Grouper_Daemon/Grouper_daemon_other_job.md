---
title: "Grouper daemon \"other job\""
space: Grouper
pageId: 28548317
version: 9
lastUpdated: 2026-07-12T15:26:53.672Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548317/Grouper+daemon+other+job
---

A Grouper "other job" is able to be scheduled and run as a grouper daemon. The schedule is a quartz cron.

The Java class should extend: edu.internet2.middleware.grouper.app.loader.OtherJobBase

Configure the job like this in grouper-loader.properties

See also Grouper daemon "other job" to run a script

```
otherJob.<name>.class = some.package.SomeClassThatExtendsOtherJobBase
    
# 4am
otherJob.<name>.quartzCron = 0 0 4 * * ?

# otherJob.<name>.priority = 

```

## Other job log store thread

When programming for an OtherJobBase, you should update the loader log periodically so grouper knows progress is being made (also set the inserts/updates/message/etc). This is easy when using the other job log store thread. Register a lightweight log store method (that needs to finish in less than 1 second). Note, do not store the hib3GrouperLoaderLog in the changeLoaderLog method, since it will be stored in batch by the thread.

Here is an example, the abbreviate report sets counts and then the message is stored in the log. Note it is deregistered at end in a finally block.

```
        OtherJobLogUpdater otherJobLogUpdater = new OtherJobLogUpdater() {
          
          @Override
          public void changeLoaderLogJavaObjectWithoutStoringToDb() {
            Hib3GrouperLoaderLog hib3GrouperLoaderLog = theOtherJobInput.getHib3GrouperLoaderLog();
            String logMessage = SyncToGrouperFromSqlDaemon.this.generateAbbreviatedReport();
            hib3GrouperLoaderLog.setJobMessage(logMessage);
          }
        };
        try {
          SyncToGrouperFromSqlDaemon.this.otherJobLogUpdaterRegister(otherJobLogUpdater);
          syncToGrouper.syncLogic();
        } catch (RuntimeException re) {
          runtimeException = re;
        } finally {
          SyncToGrouperFromSqlDaemon.this.otherJobLogUpdaterDeregister(otherJobLogUpdater);
        }
```

By default this will save every 60 seconds. You can edit that in grouper-loader.properties

```
# if a loader is registered to update the loader log table periodically, do this after this many seconds
# {valueType: "integer", defaultValue: "60"}
loader.otherJobUpdateLoaderLogDbAfterSeconds = 60
```
