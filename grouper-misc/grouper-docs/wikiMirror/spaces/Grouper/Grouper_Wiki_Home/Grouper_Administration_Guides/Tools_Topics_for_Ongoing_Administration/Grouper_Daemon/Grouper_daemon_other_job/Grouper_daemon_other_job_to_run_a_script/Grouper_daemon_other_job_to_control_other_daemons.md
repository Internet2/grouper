---
title: "Grouper daemon \"other job\" to control other daemons"
space: Grouper
pageId: 28560302
version: 10
lastUpdated: 2026-07-12T15:27:16.373Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560302/Grouper+daemon+other+job+to+control+other+daemons
---

Note there is earlier support for the script daemon, but works best on version 2.5.43+

Note: [here is an example of kicking off multiple jobs at the same time](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554430/Grouper+custom+template+via+GSH+delegated+VPN+management+-+manage) and waiting for them to finish and checking status

In v5.21.4+ you can use this method to run a daemon and assert SUCCESS:

```
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;

GrouperLoader.runOnceByJobNameLocallyAndAssertSuccess(String jobName)
```

## Simple job dependency

Have a couple of jobs. Set their schedule to not fire (e.g. in 2099)

```
0 0 6 * * ? 2099
```

Set the diagnostics to ignore: /grouper/status?diagnosticType=all

grouper.properties

```
ws.diagnostic.ignore.loader_SQL_SIMPLE__test:loader1__2a5294db09864b50abc976ae44752d33 = true
ws.diagnostic.ignore.loader_SQL_SIMPLE__test:loader2__6fdfe4ded07849bbb34d42683a04368f = true
```

add a script daemon

script is (get job name from daemon screen, NOTE: other loaderRunOneJob examples can be found at [GrouperShell (gsh)](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545249/GrouperShell+gsh) )

```
loaderRunOneJob("SQL_SIMPLE__test:loader1__2a5294db09864b50abc976ae44752d33"); 
loaderRunOneJob("SQL_SIMPLE__test:loader2__6fdfe4ded07849bbb34d42683a04368f");
```

Run the new daemon

See that it ran the other two loader sequentially

Daemon job message

```
scriptType: gsh
scriptSource: loaderRunOneJob("SQL_SIMPLE__test:loader1__2a5294db09864b50abc976ae44752d33"); 
loaderRunOneJob("SQL_SIMPLE__test:loader2__6fdfe4ded07849bbb34d42683a04368f");

groovy:001> loaderRunOneJob("SQL_SIMPLE__test:loader1__2a5294db09864b50abc976ae44752d33"); 
===> loader ran successfully, inserted 0 memberships, deleted 0 memberships, total membership count: 3, unresolvable subjects: 0
groovy:002> loaderRunOneJob("SQL_SIMPLE__test:loader2__6fdfe4ded07849bbb34d42683a04368f");
===> loader ran successfully, inserted 0 memberships, deleted 0 memberships, total membership count: 3, unresolvable subjects: 0
```

## Job dependency with conditional

Here is a sample script ( NOTE: Hib3GrouperLoaderLog.retrieveMostRecentLog is available in versions 2.5.43+ )

```
    import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
    import edu.internet2.middleware.grouper.util.GrouperUtil;
    import edu.internet2.middleware.grouper.app.loader.OtherJobScript;
    
    long startMillis = System.currentTimeMillis();
     
    String job1name = "SQL_SIMPLE__test:loader1__2a5294db09864b50abc976ae44752d33";
    String job2name = "SQL_SIMPLE__test:loader2__6fdfe4ded07849bbb34d42683a04368f";
    
    // run the first job
    loaderRunOneJob(job1name);
     
    // get the most recent loader log for job 1
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = Hib3GrouperLoaderLog.retrieveMostRecentLog(job1name);

    GrouperUtil.assertion(hib3GrouperLoaderLog.getLastUpdated().getTime() > startMillis, "Cant find job1 log!");
    GrouperUtil.assertion("SUCCESS".equals(hib3GrouperLoaderLog.getStatus()), "Job1 not SUCCESS!");
     
    // if the total records were greater than 2, then run job 2
    if (GrouperUtil.intValue(hib3GrouperLoaderLog.getTotalCount(), 0) > 2) {
      loaderRunOneJob(job2name);
      OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().setJobMessage("SUCCESS: ran job 1 and 2");
    } else {
      OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().setJobMessage("WARNING: ran job 1 but had less than 2 total records (" + hib3GrouperLoaderLog.getTotalCount() + ") so not running job 2");
    }

```

Configure job

Run the new daemon, sample job output when success

```
SUCCESS: ran job 1 and 2
scriptType: gsh
scriptSource: import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
    import edu.internet2.middleware.grouper.util.GrouperUtil;
    import edu.internet2.middleware.grouper.app.loader.Othe...

groovy:001> import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
groovy:002>     import edu.internet2.middleware.grouper.util.GrouperUtil;
groovy:003>     import edu.internet2.middleware.grouper.app.loader.OtherJobScript;
groovy:004>     
groovy:005>     long startMillis = System.currentTimeMillis();
===> 1613841465448
groovy:006>      
groovy:007>     String job1name = "SQL_SIMPLE__test:loader1__2a5294db09864b50abc976ae44752d33";
===> SQL_SIMPLE__test:loader1__2a5294db09864b50abc976ae44752d33
groovy:008>     String job2name = "SQL_SIMPLE__test:loader2__6fdfe4ded07849bbb34d42683a04368f";
===> SQL_SIMPLE__test:loader2__6fdfe4ded07849bbb34d42683a04368f
groovy:009>     
groovy:010>     // run the first job
groovy:011>     loaderRunOneJob(job1name);
===> loader ran successfully, inserted 0 memberships, deleted 0 memberships, total membership count: 3, unresolvable subjects: 0
groovy:012>      
groovy:013>     // get the most recent loader log for job 1
groovy:014>     Hib3GrouperLoaderLog hib3GrouperLoaderLog = Hib3GrouperLoaderLog.retrieveMostRecentLog(job1name);
===> edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog@6522c248
groovy:015>     GrouperUtil.assertion(hib3GrouperLoaderLog.getLastUpdated().getTime() > startMillis, "Cant find job1 log!");
groovy:016>     GrouperUtil.assertion("SUCCESS".equals(hib3GrouperLoaderLog.getStatus()), "Job1 not SUCCESS!");
groovy:017>      
groovy:018>     // if the total records were greater than 2, then run job 2
groovy:019>     if (GrouperUtil.intValue(hib3GrouperLoaderLog.getTotalCount(), 0) > 2) {
groovy:020>       loaderRunOneJob(job2name);
groovy:021>       OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().setJobMessage("SUCCESS: ran job 1 and 2");
groovy:022>     } else {
groovy:023>       OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().setJobMessage("WARNING: ran job 1 but had less than 2 total records (" + hib3GrouperLoaderLog.getTotalCount() + ") so not running job 2");
groovy:024>     }
```

Make the first job have fewer total results and run

```
WARNING: ran job 1 but had less than 2 total records (0) so not running job 2
scriptType: gsh
scriptSource: import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
    import edu.internet2.middleware.grouper.util.GrouperUtil;
    import edu.internet2.middleware.grouper.app.loader.Othe...

groovy:001> import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
groovy:002>     import edu.internet2.middleware.grouper.util.GrouperUtil;
groovy:003>     import edu.internet2.middleware.grouper.app.loader.OtherJobScript;
groovy:004>     
groovy:005>     long startMillis = System.currentTimeMillis();
===> 1613841870966
groovy:006>      
groovy:007>     String job1name = "SQL_SIMPLE__test:loader1__2a5294db09864b50abc976ae44752d33";
===> SQL_SIMPLE__test:loader1__2a5294db09864b50abc976ae44752d33
groovy:008>     String job2name = "SQL_SIMPLE__test:loader2__6fdfe4ded07849bbb34d42683a04368f";
===> SQL_SIMPLE__test:loader2__6fdfe4ded07849bbb34d42683a04368f
groovy:009>     
groovy:010>     // run the first job
groovy:011>     loaderRunOneJob(job1name);
===> loader ran successfully, inserted 0 memberships, deleted 0 memberships, total membership count: 0, unresolvable subjects: 0
groovy:012>      
groovy:013>     // get the most recent loader log for job 1
groovy:014>     Hib3GrouperLoaderLog hib3GrouperLoaderLog = Hib3GrouperLoaderLog.retrieveMostRecentLog(job1name);
===> edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog@5933df9b
groovy:015>     GrouperUtil.assertion(hib3GrouperLoaderLog.getLastUpdated().getTime() > startMillis, "Cant find job1 log!");
groovy:016>     GrouperUtil.assertion("SUCCESS".equals(hib3GrouperLoaderLog.getStatus()), "Job1 not SUCCESS!");
groovy:017>      
groovy:018>     // if the total records were greater than 2, then run job 2
groovy:019>     if (GrouperUtil.intValue(hib3GrouperLoaderLog.getTotalCount(), 0) > 2) {
groovy:020>       loaderRunOneJob(job2name);
groovy:021>       OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().setJobMessage("SUCCESS: ran job 1 and 2");
groovy:022>     } else {
groovy:023>       OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().setJobMessage("WARNING: ran job 1 but had less than 2 total records (" + hib3GrouperLoaderLog.getTotalCount() + ") so not running job 2");
groovy:024>     }
```

Make first job fail

```
java.lang.IllegalArgumentException: Error when handling error: Error while running line number 11 command (    loaderRunOneJob(job1name);), ,
Script (8k max):
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
    import edu.internet2.middleware.grouper.util.GrouperUtil;
    import edu.internet2.middleware.grouper.app.loader.OtherJobScript;
    
    long startMillis = System.currentTimeMillis();
     
    String job1name = "SQL_SIMPLE__test:loader1__2a5294db09864b50abc976ae44752d33";
    String job2name = "SQL_SIMPLE__test:loader2__6fdfe4ded07849bbb34d42683a04368f";
    
    // run the first job
    loaderRunOneJob(job1name);
     
    // get the most recent loader log for job 1
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = Hib3GrouperLoaderLog.retrieveMostRecentLog(job1name);

    GrouperUtil.assertion(hib3GrouperLoaderLog.getLastUpdated().getTime() > startMillis, "Cant find job1 log!");
    GrouperUtil.assertion("SUCCESS".equals(hib3GrouperLoaderLog.getStatus()), "Job1 not SUCCESS!");
     
    // if the total records were greater than 2, then run job 2
    if (GrouperUtil.intValue(hib3GrouperLoaderLog.getTotalCount(), 0) > 2) {
      loaderRunOneJob(job2name);
      OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().setJobMessage("SUCCESS: ran job 1 and 2");
    } else {
      OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().setJobMessage("WARNING: ran job 1 but had less than 2 total records (" + hib3GrouperLoaderLog.getTotalCount() + ") so not running job 2");
    }, Output (10k max):
groovy:001> import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
groovy:002>     import edu.internet2.middleware.grouper.util.GrouperUtil;
groovy:003>     import edu.internet2.middleware.grouper.app.loader.OtherJobScript;
groovy:004>     
groovy:005>     long startMillis = System.currentTimeMillis();
===> 1613841962804
groovy:006>      
groovy:007>     String job1name = "SQL_SIMPLE__test:loader1__2a5294db09864b50abc976ae44752d33";
===> SQL_SIMPLE__test:loader1__2a5294db09864b50abc976ae44752d33
groovy:008>     String job2name = "SQL_SIMPLE__test:loader2__6fdfe4ded07849bbb34d42683a04368f";
===> SQL_SIMPLE__test:loader2__6fdfe4ded07849bbb34d42683a04368f
groovy:009>     
groovy:010>     // run the first job
groovy:011>     loaderRunOneJob(job1name);
java.lang.RuntimeException: java.lang.RuntimeException: java.lang.RuntimeException: Problem with query: select name as subject_identifier from grouper_groups where name like '%left%' and sdfasdf,  on db: DB: user: sa, url: jdbc:hsqldb:hsql://localhost:9001/grouper, driver: org.hsqldb.jdbcDriver,
jobName: SQL_SIMPLE__test:loader1__2a5294db09864b50abc976ae44752d33,
Error while running line number 11 command (    loaderRunOneJob(job1name);), 
	at edu.internet2.middleware.grouper.app.loader.GrouperLoader.runOnceByJobName(GrouperLoader.java:1742)
	at edu.internet2.middleware.grouper.app.loader.GrouperLoader.runOnceByJobName(GrouperLoader.java:1686)
	at edu.internet2.middleware.grouper.app.gsh.loaderRunOneJob.invoke(loaderRunOneJob.java:95)
	at edu.internet2.middleware.grouper.app.gsh.loaderRunOneJob$invoke.call(Unknown Source)
	at org.codehaus.groovy.runtime.callsite.CallSiteArray.defaultCall(CallSiteArray.java:48)
	at org.codehaus.groovy.runtime.callsite.AbstractCallSite.call(AbstractCallSite.java:113)
	at org.codehaus.groovy.runtime.callsite.AbstractCallSite.call(AbstractCallSite.java:133)
	at groovysh_evaluate.loaderRunOneJob(groovysh_evaluate:4)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:498)
	at org.codehaus.groovy.reflection.CachedMethod.invoke(CachedMethod.java:98)
	at groovy.lang.MetaMethod.doMethodInvoke(MetaMethod.java:325)
	at groovy.lang.
```
