---
title: "Grouper daemon \"other job\" GSH script to delete old database records"
space: Grouper
pageId: 28560208
version: 3
lastUpdated: 2026-07-01T05:36:03.254Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560208/Grouper+daemon+other+job+GSH+script+to+delete+old+database+records
---

Job to delete records in cache table older than one day

1. Write a query that will work against some database configured in Grouper (or one which can be reached over DB link). Note, you dont need a commit
  
  
  ```
  delete from ngss_wsproxy_cache where the_timestamp < systimestamp-1
  ```
2. Write a GSH script that works
  
  
  ```
      GrouperStartup.startup();
      int records = new edu.internet2.middleware.grouperClient.jdbc.GcDbAccess().connectionName("pennCommunity").sql("delete from ngss_wsproxy_cache where the_timestamp < systimestamp-6").executeSql();
      int total = new edu.internet2.middleware.grouperClient.jdbc.GcDbAccess().connectionName("pennCommunity").sql("select count(1) from ngss_wsproxy_cache").select(int.class);
  
  
  ```
3. Add in the data to go to logs
  
  
  ```
      GrouperStartup.startup();
      int records = new edu.internet2.middleware.grouperClient.jdbc.GcDbAccess().connectionName("pennCommunity").sql("delete from ngss_wsproxy_cache where the_timestamp < systimestamp-6").executeSql();
      OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().addDeleteCount(records);
      int total = new edu.internet2.middleware.grouperClient.jdbc.GcDbAccess().connectionName("pennCommunity").sql("select count(1) from ngss_wsproxy_cache").select(int.class);
      OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().setTotalCount(total);
  
  
  ```
4. Change the newlines to $newline$ and make an other job script config
  
  
  ```
  otherJob.deleteNgssWsProxyCache.class = edu.internet2.middleware.grouper.app.loader.OtherJobScript
  otherJob.deleteNgssWsProxyCache.quartzCron = 11 11 * * * ?
  otherJob.deleteNgssWsProxyCache.scriptType = gsh
  otherJob.deleteNgssWsProxyCache.scriptSource = GrouperStartup.startup();$newline$int records = new edu.internet2.middleware.grouperClient.jdbc.GcDbAccess().connectionName("pennCommunity").sql("delete from ngss_wsproxy_cache where the_timestamp < systimestamp-1").executeSql();$newline$OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().addDeleteCount(records);$newline$int total = new edu.internet2.middleware.grouperClient.jdbc.GcDbAccess().connectionName("pennCommunity").sql("select count(1) from ngss_wsproxy_cache").select(int.class);$newline$OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().setTotalCount(total);
  
  
  ```
5. Inject that into the UI configuration screen, named as grouper-loader.properties
6. Schedule jobs
7. Run the job and check results
