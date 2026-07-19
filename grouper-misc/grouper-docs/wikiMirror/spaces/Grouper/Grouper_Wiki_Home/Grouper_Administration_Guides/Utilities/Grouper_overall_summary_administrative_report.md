---
title: "Grouper overall summary administrative report"
space: Grouper
pageId: 28545058
version: 20
lastUpdated: 2026-07-12T15:26:39.024Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545058/Grouper+overall+summary+administrative+report
---

There is a Grouper report that you can have emailed to you daily, or you can run it manually from [GSH](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545249/GrouperShell+gsh). This report shows a summary of the total state of your grouper installation (e.g. how many memberships/groups/etc, how many unresolvable subjects, etc), and it shows the status of the last day's worth of activity (e.g. how many new membership, how many loader errors).

To run this report from [GSH](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545249/GrouperShell+gsh), just run this command: GrouperReport.report();

e.g.

```
groovy:000> GrouperReport.report();
===> Grouper daily report

----------------
OVERALL:
----------------
environment:           DEV
memberships:           40
groups:                9

...

 

```

To have the report emailed to you, configure these properties in grouper.properties:

```
# in cases where grouper is logging or emailing, it will use this to differentiate test vs dev vs prod
grouper.env.name = TEST
...

#smtp server is a domain name or dns name, must be simple clear text stmp with no authentication
#mail.smtp.server = whatever.school.edu

#leave blank if unauthenticated
#mail.smtp.user =

#leave blank if unauthenticated
#mail.smtp.pass =

#this is the default email address where mail from grouper will come from
#mail.from.address = noreply@example.com

#this is the subject prefix of emails, which will help differentiate prod vs test vs dev etc
#mail.subject.prefix = TEST: 

```

Then you can configure the relevant properties in grouper-loader.properties:

```
#quartz cron-like schedule for daily grouper report, the default is 7am every day: 0 0 7 * * ?
#leave blank to disable this
daily.report.quartz.cron = 0 0 7 * * ?

#comma separated email addresses to email the daily report
daily.report.emailTo = mchyzer@example.com, mchyzer@example.com

#if you put a directory here, the daily reports will be saved there, and you can
#link up to a web service or store them or whatever.  e.g. /home/grouper/reports/
daily.report.saveInDirectory =  
```

#### Sample report:

Note, it will also show (if applicable), up to 50 unresolvable subjects, all the loader jobs with errors, etc...

```
 Grouper daily report

----------------
OVERALL:
----------------
environment:           DEV
memberships:           40
groups:                9
members:               16
folders:               4
unresolvable subjects: 0

----------------
WITHIN LAST DAY:
----------------
new memberships:       40
new groups:            9
updated groups:        0
new folders:           3

----------------
LOADER SUMMARY WITHIN LAST DAY
----------------
jobs:                  4
successes:             3
errors:                0
unresolvable subjects: 0
inserts:               0
updates:               0
deletes:               0
processing time:       12,250ms

----------------
LOADER JOBS NON-ERROR
----------------
job:               MAINTENANCE_grouperReport (0 total count)
status:            SUCCESS, started: 2008-11-07 16:09:00.0 (9141ms)
inserts/updates/deletes: 0/0/0
unresolvable subjects: 0

job:               SQL_SIMPLE_faculty (12,324 total count)
status:            SUCCESS, started: 2008-11-07 13:10:00.0 (1340ms)
inserts/updates/deletes: 10/3/7
unresolvable subjects: 3

----------------
GROUPER INFO
----------------
version: 1.4.0 build date: null
os.name: Windows XP
os.arch: x86
os.version: 5.1
java.version: 1.5.0_11
java.vendor: Sun Microsystems Inc.
java.class.path: C:\mchyzer\isc\dev\grouper-qs-1.2.0\grouper\build;C:\mchyzer\isc\dev\grouper-qs-1.2.0\grouper\lib\grouper\activation.jar;C:\mchyzer\isc\dev\grouper-qs-1.2.0\grouper\lib\grouper\antlr.jar;C:\mchyzer\isc\dev\grouper-qs-1.2.0\grouper\lib\grouper\asm.jar;C:\mchyzer\isc\dev\grouper-qs-1.2.0\grouper\lib\grouper\asm-attrs.jar;C:\mchyzer\isc\dev\grouper-qs-1.2.0\grouper\lib\grouper\asm-util.jar;C:\mchyzer\isc\dev\grouper-qs-1.2.0\grouper\lib\grouper\backport-util-concurrent.jar;C:\mchyzer\isc\dev\grouper-qs-1.2.0\grouper\lib\grouper\bsh.jar;C:\mchyzer\isc\dev\grouper-qs-1.2.0\grouper\lib\grouper\c3p0.jar;C:\mchyzer\isc\dev\grouper-qs-1.2.0\grouper\lib\grouper\cglib.jar;C:\mchyzer\isc\dev\grouper-qs-1.2.0\grouper\lib\grouper\commons-beanutils.jar;C:\mchyzer\isc\dev\grouper-qs-1.2.0\grouper\lib\grouper\commons-betwixt.jar;C:\mchyzer\isc\dev\grouper-qs-1.2.0\grouper\lib\grouper\commons-cli.jar;C:\mchyzer\isc\dev\grouper-qs-1.2.0\grouper\lib\grouper\commons-collections.jar;C:\mchyzer\isc\dev\grouper-qs-1.2.0\grouper\lib\grouper\commons-digester.jar;C:\mchyzer\isc\dev\grouper-qs-1.2.0\grouper\lib\grouper\commons-lang.jar;C:\mchyzer\isc\dev\grouper-qs-1.2.0\grouper\lib\grouper\commons-logging.jar;C:\mchyzer\isc\dev\grouper-qs-1.2.0\grouper\lib\grouper\commons-math.jar;C:\mchyzer\isc\dev\grouper-qs-1.2.0\grouper\lib\grouper\DdlUtils.jar;C:\mchyzer\isc\dev\grouper-qs-1.2.0\grouper\lib\grouper\dom4j.jar;C:\mchyzer\isc\dev\grouper-qs-1.2.0\grouper\lib\grouper\ehcache.jar;C:\mchyzer\isc\dev\grouper-qs-1.2.0\grouper\lib\grouper\hibernate.jar;C:\mchyzer\isc\dev\grouper-qs-1.2.0\grouper\lib\grouper\invoker.jar;C:\mchyzer\isc\dev\grouper-qs-1.2.0\grouper\lib\grouper\jakarta-oro.jar;C:\mchyzer\isc\dev\grouper-qs-1.2.0\grouper\lib\grouper\jsr107cache.jar;C:\mchyzer\isc\dev\grouper-qs-1.2.0\grouper\lib\grouper\jta.jar;C:\mchyzer\isc\dev\grouper-qs-1.2.0\grouper\lib\grouper\jug.jar;C:\mchyzer\isc\dev\grouper-qs-1.2.0\grouper\lib\grouper\log4j.jar;C:\mchyzer\isc\dev\grouper-qs-1.2.0\grouper\lib\grouper\mailapi.jar;C:\mchyzer\isc\dev\grouper-qs-1.2.0\grouper\lib\grouper\morphString.jar;C:\mchyzer\isc\dev\grouper-qs-1.2.0\grouper\lib\grouper\odmg.jar;C:\mchyzer\isc\dev\grouper-qs-1.2.0\grouper\lib\grouper\p6spy.jar;C:\mchyzer\isc\dev\grouper-qs-1.2.0\grouper\lib\grouper\quartz.jar;C:\mchyzer\isc\dev\grouper-qs-1.2.0\grouper\lib\grouper\smtp.jar;C:\mchyzer\isc\dev\grouper-qs-1.2.0\grouper\lib\grouper\subject.jar;C:\mchyzer\isc\dev\grouper-qs-1.2.0\grouper\lib\jdbcSamples\hsqldb.jar;C:\mchyzer\isc\dev\grouper-qs-1.2.0\grouper\lib\jdbcSamples\mysql-connector-java-bin.jar;C:\mchyzer\isc\dev\grouper-qs-1.2.0\grouper\lib\jdbcSamples\ojdbc14.jar;C:\mchyzer\isc\dev\grouper-qs-1.2.0\grouper\lib\jdbcSamples\postgresql.jar;C:\mchyzer\isc\dev\grouper-qs-1.2.0\grouper\lib\test\junit.jar;C:\mchyzer\isc\dev\grouper-qs-1.2.0\grouper\lib\ant\ant-contrib.jar;C:\mchyzer\isc\dev\grouper-qs-1.2.0\grouper\lib\grouper\commons-discovery.jar;C:\mchyzer\isc\dev\grouper-qs-1.2.0\grouper\lib\grouper\jamon.jar;C:\mchyzer\isc\dev\grouper-qs-1.2.0\grouper\lib\grouper\ant.jar
heapSize: 6 MB
heapMaxSize: 63 MB
heapSizeFree: 1 MB

privileges.access.interface: edu.internet2.middleware.grouper.GrouperAccessAdapter
privileges.naming.interface: edu.internet2.middleware.grouper.GrouperNamingAdapter
privileges.access.cache.interface:
privileges.naming.cache.interface:
groups.wheel.use: true
groups.wheel.group: penn:etc:sysAdminGroup

source: id=pennperson name=Penn person class=GrouperJdbcSourceAdapter
source: id=g:gsa name=Grouper: Group Source Adapter class=GrouperSourceAdapter
source: id=g:isa name=Grouper: Internal Source Adapter class=InternalSourceAdapter
source: id=jdbc name=JDBC Source Adapter class=GrouperJdbcSourceAdapter
source: id=servPrinc name=Kerberos service principals class=GrouperJdbcSourceAdapter

hibernate.dialect: org.hibernate.dialect.Oracle10gDialect
hibernate.connection.driver_class: oracle.jdbc.driver.OracleDriver
hibernate.cache.provider_class: org.hibernate.cache.EhCacheProvider

```

#### See also

[Grouper Diagnostics](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548954/Grouper+health+check+endpoint+healthcheck+status+diagnostics)

[Grouper Reports](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554409/Grouper+reporting)
