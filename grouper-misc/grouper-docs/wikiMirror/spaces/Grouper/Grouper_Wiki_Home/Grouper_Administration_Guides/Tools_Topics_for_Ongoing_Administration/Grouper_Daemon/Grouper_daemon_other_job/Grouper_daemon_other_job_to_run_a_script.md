---
title: "Grouper daemon \"other job\" to run a script"
space: Grouper
pageId: 28555309
version: 19
lastUpdated: 2026-07-01T05:38:33.622Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555309/Grouper+daemon+other+job+to+run+a+script
---

In Grouper v2.5.23+ you can configure Grouper to schedule a SQL or GSH script to run in the daemon container like any other daemon job. Will show up on the daemon list UI page. You can see logs, start it, schedule it, etc.

- [GSH script to run a GSH template with inputs](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548143/Grouper+custom+template+via+GSH+invoked+by+daemon+-+load+group+attributes)

### Configure in daemon UI

grouper-loader.properties

| Config attribute | Value |
| --- | --- |
| otherJob.<configId>.class | edu.internet2.middleware.grouper.app.loader.OtherJobScript |
| otherJob.<configId>.quartzCron | 0 0 0 * * ? (nightly at midnight) |
| otherJob.<configId>.scriptType | sql or gsh   Note, sql runs against the grouper database. To run against a different database, use a GSH script. [Here is an example](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560208/Grouper+daemon+other+job+GSH+script+to+delete+old+database+records) |
| otherJob.<configId>.scriptSource | this is the SQL or GSH script, use $newline$ to separate lines.  note: in SQL you should commit after DML commands.  Mutually exclusive with "fileName" |
| otherJob.<configId>.fileName | fileName of script to run.  Mutually exclusive with "scriptSource" |
| otherJob.<configId>.connectionName | if SQL this is the connection name to use (v2.5.40+) |

Note: The output of the script (or the error) is in the daemon log (can view from UI).

SQL logs details from edu.internet2.middleware.grouper.ddl.GrouperAntProject at INFO level if you want to get them from log4j.

### Update the loader log from GSH (v2.5.26+)

```
import edu.internet2.middleware.grouper.app.loader.OtherJobScript;

// in v2.6.8+ you can do this null safe which works in daemon and standalone program
Hib3GrouperLoaderLog hib3GrouperLoaderLog = OtherJobScript.retrieveHib3GrouperLoaderLogNotNull();
hib3GrouperLoaderLog.setInsertCount(GrouperUtil.intObjectValue(debugMap.get("insertsCount"), true));
hib3GrouperLoaderLog.setTotalCount(totalCount);

// in v2.6.7 and earlier...

// if you want to know the jobName ( value in the grouper-loader.properties config ).
jobName = OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().getJobName();
// jobName can help you lookup other config vars for the script. ( you may need to do more parsing/fixing of the string )//edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig.retrieveConfig().propertyValueString(...)
// or for grouper.properties config
//edu.internet2.middleware.grouper.cfg.GrouperConfig.retrieveConfig().propertyValueString(...)

// generally you will add to the counts
OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().addInsertCount(5);
OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().addTotalCount(6);
OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().addUpdateCount(7);
OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().addDeleteCount(8);
OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().addUnresolvableSubjectCount(1);

// if you just want to set the count you can do that too.  Other fields in Hib3GrouperLoaderLog can be set too
OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().setInsertCount(1);
OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().setTotalCount(2);
OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().setUpdateCount(3);
OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().setDeleteCount(4);
OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().setUnresolvableSubjectCount(1);

// if you just want to get the count you can do that too. Other fields in Hib3GrouperLoaderLog can be set too
OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().getInsertCount();
OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().getTotalCount();
OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().getUpdateCount();
OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().getDeleteCount();
OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().getUnresolvableSubjectCount();// and you can just "increment" the UnresolvableSubjectCount (only)
OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().incrementUnresolvableSubjectCount();// for long running scripts you likely should also set this from time to time...
OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().setLastUpdated(Timestamp lastUpdated1);

// this goes to daemon logs
OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().setJobMessage("SUCCESS: ran job 1 and 2");

// or append
OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().appendJobMessage("something");
```

### Stopping Daemon Jobs

In Grouper 5.8.1+, it's possible to stop running daemon jobs (that are not stuck). For example, the job might be in the middle of a lot of changes that are being processed and you'd like to stop it. To allow this functionality with your job, you would need to add the following line of code to various places in your script. For example, you may add it within a loop or after some amount of work is done.

| `GrouperDaemonUtils.stopProcessingIfJobPaused();` |
| --- |

Normally, this line of code executes very quickly and doesn't do anything. But if a Grouper Admin goes into the Daemon Jobs page in the Grouper UI and disables your job, after a few seconds (~10s), the Grouper Daemon engine will realize that the job has been disabled. After that, the next time your script executes the line of code above, it will throw a RuntimeException. So assuming your script doesn't catch the exception, the job will then end.

Note that if you're managing your own imports (i.e. using the Lightweight GSH option), then you'll need to import the following:

```
import edu.internet2.middleware.grouper.app.loader.GrouperDaemonUtils;
```

### Example 1: schedule a SQL script with the script in the config file

grouper-loader.properties

```
otherJob.scriptSql.class = edu.internet2.middleware.grouper.app.loader.OtherJobScript
otherJob.scriptSql.quartzCron = 0 0 0 * * ?
otherJob.scriptSql.scriptType = sql
otherJob.scriptSql.scriptSource = update grouper_groups set description = 'whatever' where name = 'a:b';$newline$update grouper_stems set description = 'whatever' where name='d:e';$newline$commit;
```

Job summary

```
scriptType: sql
scriptSource: update grouper_groups set description = 'whatever' where name = 'a:b';
update grouper_stems set description = 'whatever' where name = 'd:e';
commit;

connecting to jdbc:mysql://localhost:3306/grouper_v2_5?useSSL=false
Loading com.mysql.jdbc.Driver using system loader.
Executing resource: /Users/mchyzer/git/grouper_v2_5/grouper/ddlScripts/grouperDdl_20200414_17_27_15_852.sql
SQL:  update grouper_groups set description = 'whatever' where name = 'a:b'
1 rows affected
SQL:  update grouper_stems set description = 'whatever' where name = 'd:e'
1 rows affected
SQL:  commit
0 rows affected
Committing transaction
3 of 3 SQL statements executed successfully

Script was executed successfully

```

### Example 2: schedule a SQL script with a script in a file in your container

```
File:   /opt/grouper/scripts/mySqlScript.sql

update grouper_groups set description = 'whatever' where name = 'a:b';
update grouper_stems set description = 'whatever' where name='d:e';
commit;

File: grouper-loader.properties

otherJob.scriptSqlFile.class = edu.internet2.middleware.grouper.app.loader.OtherJobScript
otherJob.scriptSqlFile.quartzCron = 0 0 0 * * ?
otherJob.scriptSqlFile.scriptType = sql
otherJob.scriptSqlFile.fileName = /opt/grouper/scripts/mySqlScript.sql
```

Job message

```
scriptType: sql
fileName: /opt/grouper/scripts/mySqlScript.sql

connecting to jdbc:mysql://localhost:3306/grouper_v2_5?useSSL=false
Loading com.mysql.jdbc.Driver using system loader.
Executing resource: /opt/grouper/scripts/mySqlScript.sql
SQL:  update grouper_groups set description = 'whatever2' where name = 'a:b'
1 rows affected
SQL:  update grouper_stems set description = 'whatever2' where name='d:e'
1 rows affected
SQL:  commit
0 rows affected
Committing transaction
3 of 3 SQL statements executed successfully

Script was executed successfully

```

### Example 3: schedule a GSH script with a script in the config file

grouper-loader.properties

```
otherJob.scriptGsh.class = edu.internet2.middleware.grouper.app.loader.OtherJobScript
otherJob.scriptGsh.quartzCron = 0 0 0 * * ?
otherJob.scriptGsh.scriptType = gsh
otherJob.scriptGsh.scriptSource = GrouperSession grouperSession = GrouperSession.startRootSession();$newline$new GroupSave(grouperSession).assignName("stem1:a").assignCreateParentStemsIfNotExist(true).save();

```

Job message

```
scriptType: gsh
scriptSource: GrouperSession grouperSession = GrouperSession.startRootSession(); 
 new GroupSave(grouperSession).assignName("stem1:a").assignCreateParentStemsIfNotExist(true).save();

(2.5.0-beta-2, JVM: 1.8.0_161)
Type ':help' or ':h' for help.
-------------------------------------------------------------------------------
groovy:000> :load '/Users/mchyzer/git/grouper_v2_5/grouper/target/classes/groovysh.profile'
groovy:000> GrouperSession grouperSession = GrouperSession.startRootSession();
===> b37a372289bd430a887239801a1bab90,'GrouperSystem','application'
groovy:000> new GroupSave(grouperSession).assignName("stem1:a").assignCreateParentStemsIfNotExist(true).save();
===> Group[name=stem1:a,uuid=cb7b2bead8ee433aa912ac7ec321a532]
groovy:000> :exit

```

### Example 4: schedule a GSH script from a file in your container

```
File:   /opt/grouper/scripts/myGshScript.gsh

GrouperSession grouperSession = GrouperSession.startRootSession(); 
new GroupSave(grouperSession).assignName("stem1:a").assignCreateParentStemsIfNotExist(true).save();

File: grouper-loader.properties

otherJob.scriptGshFile.class = edu.internet2.middleware.grouper.app.loader.OtherJobScript
otherJob.scriptGshFile.quartzCron = 0 0 0 * * ?
otherJob.scriptGshFile.scriptType = gsh
otherJob.scriptGshFile.fileName = /opt/grouper/scripts/myGshScript.gsh
```

Job message

```
scriptType: gsh
fileName: /opt/grouper/scripts/myGshScript.gsh

(2.5.0-beta-2, JVM: 1.8.0_161)
Type ':help' or ':h' for help.
-------------------------------------------------------------------------------
groovy:000> :load '/Users/mchyzer/git/grouper_v2_5/grouper/target/classes/groovysh.profile'
groovy:000> GrouperSession grouperSession = GrouperSession.startRootSession();
===> b37a372289bd430a887239801a1bab90,'GrouperSystem','application'
groovy:000> new GroupSave(grouperSession).assignName("stem1:a").assignCreateParentStemsIfNotExist(true).save();
===> Group[name=stem1:a,uuid=cb7b2bead8ee433aa912ac7ec321a532]
groovy:000> :exit

```
