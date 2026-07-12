---
title: "Logging SQL queries in Grouper"
space: Grouper
pageId: 28555451
version: 17
lastUpdated: 2026-07-01T05:38:11.334Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555451/Logging+SQL+queries+in+Grouper
---

> **Applies to:** current supported releases. The custom p6spy jar ships as `p6spy-3.6.0.jar`.
> 
> **Access required:** this is a server-side, sysadmin task, not a Grouper privilege. You need filesystem (or container) access to the Grouper webapp to add the jar and config file, edit `grouper.hibernate.properties`, and restart the process.

 > This technique is meant to be **temporary, for troubleshooting only**. Do not leave it on in production — logging every query is a significant performance hindrance.

  

## Overview

 A custom [p6spy](https://p6spy.readthedocs.io/en/latest/) jar is the best way to log the SQL queries Grouper runs against its database. p6spy is a JDBC proxy driver: once registered, every statement (and optionally each result set) is written to a log file. Grouper ships a customized build of the jar — the stock p6spy jar from the p6spy project will not work with Grouper.

 Note that the logged SQL is **not the exact statement sent to the database**: for prepared statements, the bind parameters are substituted into the query text for readability.

 

## Example: a failing loader query

 This example shows the query and its results from a loader job, captured from the GTE container against MySQL. The query deliberately ends in a `NULL` `subject_id` so it fails, so you can see both the SQL and the resulting error in the log.

 
```sql
SELECT
	subject_id,
	the_order
FROM
	(
	SELECT
		m.subject_id AS subject_id, 1 AS the_order
	FROM
		grouper_members m
	WHERE
		m.subject_id LIKE 'ab%'
    UNION
	SELECT
		NULL AS subject_id, 2 AS the_order
	FROM
		grouper_members) AS the_subquery
ORDER BY
	2
```

 Here is the resulting log message (this message was improved in `v2.5.36+`):

 
```
java.lang.RuntimeException: Result has a null subject_id, please correct the query (maybe just filter where subject_id is not null)
```

 

## Enable p6spy logging

 

### 1. Get the custom p6spy jar

 Add the Grouper-customized p6spy jar to the webapp's `WEB-INF/lib` directory. Modern Grouper builds use `p6spy-3.6.0.jar`.

 > **VERIFY:** Modern Grouper (`v4+`) appears to bundle the custom `p6spy-3.6.0.jar` on the classpath already, so this download step may be unnecessary on a current install — confirm with the sponsor whether the jar ships in the webapp. The command below fetches the older custom jar from the `GROUPER_2_3_BRANCH` for installs that do not include it.

 
```bash
[student@ip-172-31-14-57 ~]$ docker exec -it -u tomcat 401.2.1 bash
[tomcat@b9f6012bfa3b WEB-INF]$ cd lib
[tomcat@b9f6012bfa3b lib]$ pwd
/opt/grouper/grouperWebapp/WEB-INF/lib
[tomcat@b9f6012bfa3b lib]$ wget https://github.com/Internet2/grouper/raw/GROUPER_2_3_BRANCH/grouper/lib/grouper/p6spy.jar
```

 

### 2. Create the spy.properties config file

 Place a `spy.properties` file on the classpath (`WEB-INF/classes`). You can start from the shipped example:

 
```bash
[tomcat@b9f6012bfa3b lib]$ cd ..
[tomcat@b9f6012bfa3b WEB-INF]$ cd classes
[tomcat@b9f6012bfa3b classes]$ wget https://github.com/Internet2/grouper/raw/GROUPER_2_3_BRANCH/grouper/conf/spy.example.properties
[tomcat@b9f6012bfa3b classes]$ mv spy.example.properties spy.properties
[tomcat@b9f6012bfa3b classes]$ vi spy.properties
```

 Set the real underlying driver and the log path. Set `executionthreshold` (in milliseconds) if you only want to log long-running queries, and confirm `excludecategories` is what you want.

 
```text
# e.g. mysql:           com.mysql.cj.jdbc.Driver
# e.g. p6spy (log sql): com.p6spy.engine.spy.P6SpyDriver
#   for p6spy, put the underlying driver in spy.properties
# e.g. oracle:          oracle.jdbc.driver.OracleDriver
# e.g. hsqldb:          org.hsqldb.jdbcDriver
# e.g. postgres:        org.postgresql.Driver
# e.g. SQL Server:      com.microsoft.sqlserver.jdbc.SQLServerDriver
realdriver=com.mysql.cj.jdbc.Driver

logfile=/opt/grouper/logs/grouper/grouperSpy.log

excludecategories=info,debug,resultset,batch,result,commit,rollback

# queries longer then 10 seconds
executionthreshold=10000
```

 

### 3. Register the p6spy driver

 In `grouper.hibernate.properties`, set the connection driver class to the p6spy driver (the real database driver stays in `spy.properties`, from step 2):

 
```text
#   com.p6spy.engine.spy.P6SpyDriver
#   for p6spy, put the underlying driver in spy.properties
# e.g. oracle:          oracle.jdbc.driver.OracleDriver
# e.g. hsqldb:          org.hsqldb.jdbcDriver
# e.g. postgres:        org.postgresql.Driver
# e.g. mssql:           com.microsoft.sqlserver.jdbc.SQLServerDriver
hibernate.connection.driver_class = com.p6spy.engine.spy.P6SpyDriver
```

 

### 4. Restart Grouper

 Bounce Tomcat (or rebuild and run the container) so the new driver and config take effect.

 
```bash
[tomcat@b9f6012bfa3b bin]$ ps -ef | grep tomcat | grep java | grep -v grep
tomcat      86     1  2 Oct16 ?        00:31:51 /usr/lib/jvm/java-1.8.0-amazon-corretto/bin/java -Dnop -Djava.util.logging.manager=org.apache.logging.log4j.jul.LogManager -javaagent:/opt/tomee/lib/openejb-javaagent.jar -Djdk.tls.ephemeralDHKeySize=2048 -Djava.protocol.handler.pkgs=org.apache.catalina.webresources -Dorg.apache.catalina.security.SecurityListener.UMASK=0027 -Xmx1500m -XX:+UseG1GC -XX:+UseStringDeduplication -Dlog4j.configurationFile=/opt/tomee/conf/log4j2.xml -DENV=training -DUSERTOKEN=gte-401.2.1 -Dfile.encoding=UTF-8 -Dignore.endorsed.dirs= -classpath /opt/tomee/bin/*:/opt/tomee/bin/bootstrap.jar:/opt/tomee/bin/tomcat-juli.jar -Dcatalina.base=/opt/tomee -Dcatalina.home=/opt/tomee -Djava.io.tmpdir=/opt/tomee/temp org.apache.catalina.startup.Bootstrap start
[tomcat@b9f6012bfa3b bin]$ kill -KILL 86
[tomcat@b9f6012bfa3b bin]$ ps -ef | grep tomcat | grep java | grep -v grep
tomcat     666     1 99 18:38 ?        00:00:02 /usr/lib/jvm/java-1.8.0-amazon-corretto/bin/java -Dnop -Djava.util.logging.manager=org.apache.logging.log4j.jul.LogManager -javaagent:/opt/tomee/lib/openejb-javaagent.jar -Djdk.tls.ephemeralDHKeySize=2048 -Djava.protocol.handler.pkgs=org.apache.catalina.webresources -Dorg.apache.catalina.security.SecurityListener.UMASK=0027 -Xmx1500m -XX:+UseG1GC -XX:+UseStringDeduplication -Dlog4j.configurationFile=/opt/tomee/conf/log4j2.xml -DENV=training -DUSERTOKEN=gte-401.2.1 -Dfile.encoding=UTF-8 -Dignore.endorsed.dirs= -classpath /opt/tomee/bin/*:/opt/tomee/bin/bootstrap.jar:/opt/tomee/bin/tomcat-juli.jar -Dcatalina.base=/opt/tomee -Dcatalina.home=/opt/tomee -Djava.io.tmpdir=/opt/tomee/temp org.apache.catalina.startup.Bootstrap start
[tomcat@b9f6012bfa3b bin]$ 
```

 

### 5. Read the log

 Open the log file you set as `logfile` (here `/opt/grouper/logs/grouper/grouperSpy.log`) and search for the query you are looking for. Each entry shows the elapsed time, the calling stack, and the SQL:

 
```
2021/06/01 18:47:48:591, 75363ms, statement: ByHqlStatic.java.listSet() line 458, Hib3GroupDAO.java.findAllGroupsSecureHelper() line 3473, Hib3GroupDAO.java.getAllGroupsSecure() line 4108, GroupFinder.java.findGroups() line 1300, GrouperSourceAdapter.java.callback() line 592, GrouperSession.java.callbackGrouperSession() line 1000, GrouperSourceAdapter.java.searchHelper() line 556, GrouperSourceAdapter.java.search() line 718, SubjectCheckConfig.java.checkConfig() line 168, GrouperCheckConfig.java.callback() line 581, GrouperSession.java.callbackGrouperSession() line 1000, GrouperSession.java.internal_callbackRootGrouperSession() line 1069, GrouperSession.java.internal_callbackRootGrouperSession() line 1036, GrouperCheckConfig.java.checkConfig() line 577, GrouperStartup.java.callback() line 349, GrouperSession.java.callbackGrouperSession() line 1000, GrouperSession.java.internal_callbackRootGrouperSession() line 1069, GrouperSession.java.internal_callbackRootGrouperSession() line 1036, GrouperStartup.java.startup() line 296, GrouperLoader.java.main() line 124, CommonServletContainerInitializer.java.run() line 170
   select distinct group0_.id as id1_25_, group0_.hibernate_version_number as hibernat2_25_, group0_.last_membership_change as last_mem3_25_, group0_.last_imm_membership_change as last_imm4_25_, group0_.parent_stem as parent_s5_25_, group0_.creator_id as creator_6_25_, group0_.create_time as create_t7_25_, group0_.modifier_id as modifier8_25_, group0_.modify_time as modify_t9_25_, group0_.name as name10_25_, group0_.display_name as display11_25_, group0_.extension as extensi12_25_, group0_.display_extension as display13_25_, group0_.description as descrip14_25_, group0_.context_id as context15_25_, group0_.alternate_name as alterna16_25_, group0_.type_of_group as type_of17_25_, group0_.id_index as id_inde18_25_, group0_.enabled as enabled19_25_, group0_.enabled_timestamp as enabled20_25_, group0_.disabled_timestamp as disable21_25_ from grouper_groups group0_ where (group0_.id='grouperteststringonstartupasdfghj' or lower(group0_.name) like '%grouperteststringonstartupasdfghj%' or lower(group0_.alternate_name) like '%grouperteststringonstartupasdfghj%' or lower(group0_.display_name) like '%grouperteststringonstartupasdfghj%' or lower(group0_.description) like '%grouperteststringonstartupasdfghj%') and (group0_.type_of_group in ('group' , 'role')) order by group0_.display_name asc
```
