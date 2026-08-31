---
title: "Grouper daemon log"
space: Grouper
pageId: 28554879
version: 4
lastUpdated: 2018-01-04T17:04:52.990Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554879/Grouper+daemon+log
---

Note, the grouper_loader_log table in the DB has loader log data.

This will log to a file for the loader and other daemon jobs more information than what is in the database. You need grouper API v2.3.0+ patch 77.

The log will have one entry for an action. And some actions can be omitted if too verbose.

Log entries are linked together by an overallId and subjobId. A subjob is managing a group in a list of groups job.

## Configure

In the log4j.properties, configure the loader to a file for debug

```
log4j.appender.grouper_daemon                           = org.apache.log4j.DailyRollingFileAppender
log4j.appender.grouper_daemon.File                      = ${grouper.home}logs/grouper_daemon.log
log4j.appender.grouper_daemon.DatePattern               = '.'yyyy-MM-dd
log4j.appender.grouper_daemon.MaxBackupIndex            = 30
log4j.appender.grouper_daemon.layout                    = org.apache.log4j.PatternLayout
log4j.appender.grouper_daemon.layout.ConversionPattern  = %d{ISO8601}: %m%n

 
# daemon log
log4j.logger.edu.internet2.middleware.grouper.app.loader.GrouperLoaderLog = DEBUG, grouper_daemon
log4j.additivity.edu.internet2.middleware.grouper.app.loader.GrouperLoaderLog = false

```

In the grouper-loader.properties adjust the defaults:

```
##################################
## Daemon logging
##################################

# When running the daemon log, do you want to log these various things?

# overall log for a job
daemon.log.logEnabled_overallLog = true

# subjob log for a job (e.g. if a job manages a lite of groups)
daemon.log.logEnabled_subjobLog = true

# groups being created or deleted
daemon.log.logEnabled_groupManagement = true

# memberships being created or deleted
daemon.log.logEnabled_membershipManagement = true

# if each logger map should have an id
daemon.log.logIdsEnabled = false

```

## Simple SQL group loader logs

```
2017-08-19 15:48:45,729: logType: membershipManagement, overallId: TGTZ5LS0, groupName: test:testLoader, subject: Subject id: test.subject.1, sourceId: jdbc, operation: add, success: true, threadId: 30, elapsed: 58 ms
2017-08-19 15:48:45,730: logType: membershipManagement, overallId: TGTZ5LS0, groupName: test:testLoader, subject: Subject id: test.subject.0, sourceId: jdbc, operation: add, success: true, threadId: 28, elapsed: 59 ms
2017-08-19 15:48:45,730: logType: membershipManagement, overallId: TGTZ5LS0, groupName: test:testLoader, subject: Subject id: test.subject.2, sourceId: jdbc, operation: add, success: true, threadId: 29, elapsed: 59 ms
2017-08-19 15:48:45,742: logType: overallLog, overallId: TGTZ5LS0, dryRun: false, jobName: SQL_SIMPLE__test:testLoader__ccf74f3b4d0743428f7d72a14d8d81db, status: SUCCESS, jobType: SQL_SIMPLE, host: ISC15-0009-WD, dbName: grouper, query: SELECT 'jdbc' AS subject_source_id, subjectId AS subject_id FROM SUBJECT WHERE subjectId IN ('test.subject.0', 'test.subject.1', 'test.subject.2'), rowsFromExternal: 3, rowsFromGrouper: 0, deleteCount: 0, insertCount: 3, updateCount: 0, totalCount: 3, millisGetData: 25, millisLoadData: 70, threadId: 1, elapsed: 156 ms
```

## Simple SQL list of groups loader logs

```
2017-08-19 16:32:34,871: logType: groupManagement, overallId: TGT1PXVH, subjobId: TGT1PXVK, groupName: test:testGroup2, operation: INSERT, success: true, threadId: 29, elapsed: 452 ms
2017-08-19 16:32:34,882: logType: groupManagement, overallId: TGT1PXVH, subjobId: TGT1PXVJ, groupName: test:testGroup1, operation: INSERT, success: true, threadId: 28, elapsed: 479 ms
2017-08-19 16:32:35,064: logType: membershipManagement, overallId: TGT1PXVH, subjobId: TGT1PXVK, groupName: test:testGroup2, subject: Subject id: test.subject.3, sourceId: jdbc, operation: add, success: true, threadId: 32, elapsed: 179 ms
2017-08-19 16:32:35,073: logType: membershipManagement, overallId: TGT1PXVH, subjobId: TGT1PXVK, groupName: test:testGroup2, subject: Subject id: test.subject.9, sourceId: jdbc, operation: add, success: true, threadId: 35, elapsed: 102 ms
2017-08-19 16:32:35,074: logType: membershipManagement, overallId: TGT1PXVH, subjobId: TGT1PXVK, groupName: test:testGroup2, subject: Subject id: test.subject.0, sourceId: jdbc, operation: add, success: true, threadId: 34, elapsed: 167 ms
2017-08-19 16:32:35,074: logType: membershipManagement, overallId: TGT1PXVH, subjobId: TGT1PXVJ, groupName: test:testGroup1, subject: Subject id: test.subject.8, sourceId: jdbc, operation: add, success: true, threadId: 50, elapsed: 154 ms
2017-08-19 16:32:35,074: logType: membershipManagement, overallId: TGT1PXVH, subjobId: TGT1PXVJ, groupName: test:testGroup1, subject: Subject id: test.subject.6, sourceId: jdbc, operation: add, success: true, threadId: 49, elapsed: 167 ms
2017-08-19 16:32:35,082: logType: membershipManagement, overallId: TGT1PXVH, subjobId: TGT1PXVJ, groupName: test:testGroup1, subject: Subject id: test.subject.4, sourceId: jdbc, operation: add, success: true, threadId: 42, elapsed: 138 ms
2017-08-19 16:32:35,082: logType: membershipManagement, overallId: TGT1PXVH, subjobId: TGT1PXVJ, groupName: test:testGroup1, subject: Subject id: test.subject.5, sourceId: jdbc, operation: add, success: true, threadId: 45, elapsed: 166 ms
2017-08-19 16:32:35,095: logType: membershipManagement, overallId: TGT1PXVH, subjobId: TGT1PXVK, groupName: test:testGroup2, subject: Subject id: test.subject.1, sourceId: jdbc, operation: add, success: true, threadId: 39, elapsed: 200 ms
2017-08-19 16:32:35,095: logType: membershipManagement, overallId: TGT1PXVH, subjobId: TGT1PXVJ, groupName: test:testGroup1, subject: Subject id: test.subject.7, sourceId: jdbc, operation: add, success: true, threadId: 47, elapsed: 165 ms
2017-08-19 16:32:35,095: logType: membershipManagement, overallId: TGT1PXVH, subjobId: TGT1PXVJ, groupName: test:testGroup1, subject: Subject id: test.subject.3, sourceId: jdbc, operation: add, success: true, threadId: 31, elapsed: 167 ms
2017-08-19 16:32:35,095: logType: membershipManagement, overallId: TGT1PXVH, subjobId: TGT1PXVK, groupName: test:testGroup2, subject: Subject id: test.subject.7, sourceId: jdbc, operation: add, success: true, threadId: 38, elapsed: 164 ms
2017-08-19 16:32:35,095: logType: membershipManagement, overallId: TGT1PXVH, subjobId: TGT1PXVJ, groupName: test:testGroup1, subject: Subject id: test.subject.2, sourceId: jdbc, operation: add, success: true, threadId: 46, elapsed: 168 ms
2017-08-19 16:32:35,099: logType: membershipManagement, overallId: TGT1PXVH, subjobId: TGT1PXVK, groupName: test:testGroup2, subject: Subject id: test.subject.4, sourceId: jdbc, operation: add, success: true, threadId: 33, elapsed: 193 ms
2017-08-19 16:32:35,099: logType: membershipManagement, overallId: TGT1PXVH, subjobId: TGT1PXVK, groupName: test:testGroup2, subject: Subject id: test.subject.5, sourceId: jdbc, operation: add, success: true, threadId: 36, elapsed: 140 ms
2017-08-19 16:32:35,099: logType: membershipManagement, overallId: TGT1PXVH, subjobId: TGT1PXVJ, groupName: test:testGroup1, subject: Subject id: test.subject.0, sourceId: jdbc, operation: add, success: true, threadId: 43, elapsed: 178 ms
2017-08-19 16:32:35,099: logType: membershipManagement, overallId: TGT1PXVH, subjobId: TGT1PXVJ, groupName: test:testGroup1, subject: Subject id: test.subject.9, sourceId: jdbc, operation: add, success: true, threadId: 44, elapsed: 155 ms
2017-08-19 16:32:35,099: logType: membershipManagement, overallId: TGT1PXVH, subjobId: TGT1PXVK, groupName: test:testGroup2, subject: Subject id: test.subject.2, sourceId: jdbc, operation: add, success: true, threadId: 37, elapsed: 107 ms
2017-08-19 16:32:35,099: logType: membershipManagement, overallId: TGT1PXVH, subjobId: TGT1PXVK, groupName: test:testGroup2, subject: Subject id: test.subject.8, sourceId: jdbc, operation: add, success: true, threadId: 41, elapsed: 106 ms
2017-08-19 16:32:35,105: logType: membershipManagement, overallId: TGT1PXVH, subjobId: TGT1PXVK, groupName: test:testGroup2, subject: Subject id: test.subject.6, sourceId: jdbc, operation: add, success: true, threadId: 40, elapsed: 115 ms
2017-08-19 16:32:35,108: logType: membershipManagement, overallId: TGT1PXVH, subjobId: TGT1PXVJ, groupName: test:testGroup1, subject: Subject id: test.subject.1, sourceId: jdbc, operation: add, success: true, threadId: 48, elapsed: 177 ms
2017-08-19 16:32:35,134: logType: subjobLog, overallId: TGT1PXVH, subjobId: TGT1PXVK, parentJobName: SQL_GROUP_LIST__test:testGroupListLoader__73640c2920954ad69a9a36ad0751a57d, groupName: test:testGroup2, dryRun: false, jobName: subjobFor_test:testGroup2, status: SUCCESS, jobType: SQL_GROUP_LIST, host: ISC15-0009-WD, rowsFromExternal: 10, rowsFromGrouper: 0, deleteCount: 0, insertCount: 10, updateCount: 0, totalCount: 10, millisGetData: 24, millisLoadData: 686, threadId: 29, elapsed: 737 ms
2017-08-19 16:32:35,146: logType: subjobLog, overallId: TGT1PXVH, subjobId: TGT1PXVJ, parentJobName: SQL_GROUP_LIST__test:testGroupListLoader__73640c2920954ad69a9a36ad0751a57d, groupName: test:testGroup1, dryRun: false, jobName: subjobFor_test:testGroup1, status: SUCCESS, jobType: SQL_GROUP_LIST, host: ISC15-0009-WD, rowsFromExternal: 10, rowsFromGrouper: 0, deleteCount: 0, insertCount: 10, updateCount: 0, totalCount: 10, millisGetData: 7, millisLoadData: 706, threadId: 28, elapsed: 749 ms
2017-08-19 16:32:35,154: logType: overallLog, overallId: TGT1PXVH, dryRun: false, jobName: SQL_GROUP_LIST__test:testGroupListLoader__73640c2920954ad69a9a36ad0751a57d, status: SUCCESS, jobType: SQL_GROUP_LIST, host: ISC15-0009-WD, dbName: grouper, query: SELECT 'jdbc' AS subject_source_id, subjectId AS subject_id, 'test:testGroup1' AS group_name FROM SUBJECT UNION SELECT 'jdbc' AS subject_source_id, subjectId AS subject_id, 'test:testGroup2' AS group_name FROM SUBJECT, metadataRowCount: 0, rowsFromExternal: 20, groupSizeExternal: 2, deleteCount: 0, insertCount: 20, updateCount: 0, totalCount: 20, millisGetData: 40, millisLoadData: 751, threadId: 1, elapsed: 841 ms

```

## Simple LDAP group loader logs

```
 2017-08-19 16:05:29,887: logType: membershipManagement, overallId: TGT0Q243, groupName: test:testLoaderLdapSimple, subject: Subject id: 293ABE32-109C-11DF-8000-0003BA2FA263, sourceId: cmuDirectory, operation: add, success: true, threadId: 28, elapsed: 102 ms
2017-08-19 16:05:29,890: logType: membershipManagement, overallId: TGT0Q243, groupName: test:testLoaderLdapSimple, subject: Subject id: AE334D56-7E40-11DD-8001-0003BA2FA263, sourceId: cmuDirectory, operation: add, success: true, threadId: 35, elapsed: 82 ms
2017-08-19 16:05:29,893: logType: membershipManagement, overallId: TGT0Q243, groupName: test:testLoaderLdapSimple, subject: Subject id: 55F202B2-72F9-11E0-8001-00144F799A7A, sourceId: cmuDirectory, operation: add, success: true, threadId: 31, elapsed: 109 ms
2017-08-19 16:05:29,895: logType: membershipManagement, overallId: TGT0Q243, groupName: test:testLoaderLdapSimple, subject: Subject id: 00000000-0000-1000-3F70-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 39, elapsed: 86 ms
2017-08-19 16:05:29,895: logType: membershipManagement, overallId: TGT0Q243, groupName: test:testLoaderLdapSimple, subject: Subject id: 1A97ED54-3C6D-11DE-8001-0003BA2FA263, sourceId: cmuDirectory, operation: add, success: true, threadId: 34, elapsed: 71 ms
2017-08-19 16:05:29,895: logType: membershipManagement, overallId: TGT0Q243, groupName: test:testLoaderLdapSimple, subject: Subject id: 2BF70E82-BD36-11D9-8000-0003BA2FA263, sourceId: cmuDirectory, operation: add, success: true, threadId: 36, elapsed: 86 ms
2017-08-19 16:05:29,899: logType: membershipManagement, overallId: TGT0Q243, groupName: test:testLoaderLdapSimple, subject: Subject id: D6914E6E-E0D7-11DF-8001-00144F799A7A, sourceId: cmuDirectory, operation: add, success: true, threadId: 37, elapsed: 71 ms
2017-08-19 16:05:29,900: logType: membershipManagement, overallId: TGT0Q243, groupName: test:testLoaderLdapSimple, subject: Subject id: 7404BCDC-9794-11DE-8001-0003BA2FA263, sourceId: cmuDirectory, operation: add, success: true, threadId: 38, elapsed: 75 ms
2017-08-19 16:05:29,902: logType: membershipManagement, overallId: TGT0Q243, groupName: test:testLoaderLdapSimple, subject: Subject id: 8B075248-925C-11E2-8000-00144F799A7A, sourceId: cmuDirectory, operation: add, success: true, threadId: 32, elapsed: 94 ms
2017-08-19 16:05:29,908: logType: membershipManagement, overallId: TGT0Q243, groupName: test:testLoaderLdapSimple, subject: Subject id: FD517D74-2DFD-11E7-8001-FFFF16E013AC, sourceId: cmuDirectory, operation: add, success: true, threadId: 40, elapsed: 80 ms
2017-08-19 16:05:29,909: logType: membershipManagement, overallId: TGT0Q243, groupName: test:testLoaderLdapSimple, subject: Subject id: 4A10366C-D7F4-11D5-8000-080020CC75D3, sourceId: cmuDirectory, operation: add, success: true, threadId: 33, elapsed: 83 ms
2017-08-19 16:05:29,925: logType: membershipManagement, overallId: TGT0Q243, groupName: test:testLoaderLdapSimple, subject: Subject id: 07696C48-CBA2-11D9-8001-0003BA2FA263, sourceId: cmuDirectory, operation: add, success: true, threadId: 35, elapsed: 32 ms
2017-08-19 16:05:29,927: logType: membershipManagement, overallId: TGT0Q243, groupName: test:testLoaderLdapSimple, subject: Subject id: 8F2D682C-6261-11DF-8000-0003BA2FA263, sourceId: cmuDirectory, operation: add, success: true, threadId: 34, elapsed: 29 ms
2017-08-19 16:05:29,929: logType: membershipManagement, overallId: TGT0Q243, groupName: test:testLoaderLdapSimple, subject: Subject id: 74A3CF56-C72C-11E5-8000-00144F799A7A, sourceId: cmuDirectory, operation: add, success: true, threadId: 36, elapsed: 31 ms
2017-08-19 16:05:29,931: logType: membershipManagement, overallId: TGT0Q243, groupName: test:testLoaderLdapSimple, subject: Subject id: 00000000-0000-1000-79FA-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 28, elapsed: 37 ms
2017-08-19 16:05:29,958: logType: overallLog, overallId: TGT0Q243, dryRun: false, jobName: LDAP_SIMPLE__test:testLoaderLdapSimple__f879e0c235594fefbc54f229b5f1deac, status: SUCCESS, jobType: LDAP_SIMPLE, host: ISC15-0009-WD, serverId: personLdap, filter: (& (cmuAndrewCommonNamespaceId=*dest*) (objectClass=cmuPerson)), subjectAttribute: guid, searchDn: ou=person, rowsFromExternal: 15, rowsFromGrouper: 0, deleteCount: 0, insertCount: 15, updateCount: 0, totalCount: 15, millisGetData: 312, millisLoadData: 478, threadId: 1, elapsed: 976 ms

```

## LDAP groups from attributes

```
2017-08-19 16:38:23,411: logType: groupManagement, overallId: TGT1T2R0, subjobId: TGT1T2R1, groupName: test:groups:English, operation: INSERT, success: true, threadId: 55, elapsed: 420 ms
2017-08-19 16:38:24,108: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2R1, groupName: test:groups:English, subject: Subject id: 00000000-0000-1000-63DF-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 61, elapsed: 101 ms
2017-08-19 16:38:24,119: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2R1, groupName: test:groups:English, subject: Subject id: 00000000-0000-1000-639B-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 69, elapsed: 83 ms
2017-08-19 16:38:24,136: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2R1, groupName: test:groups:English, subject: Subject id: C3CD7636-C6CA-11E3-8001-00144F799A7A, sourceId: cmuDirectory, operation: add, success: true, threadId: 66, elapsed: 109 ms
2017-08-19 16:38:24,138: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2R1, groupName: test:groups:English, subject: Subject id: 00000000-0000-1000-910C-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 63, elapsed: 132 ms
2017-08-19 16:38:24,138: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2R1, groupName: test:groups:English, subject: Subject id: 4BFBF45C-1290-11E6-8001-00144F799A7A, sourceId: cmuDirectory, operation: add, success: true, threadId: 65, elapsed: 100 ms
2017-08-19 16:38:24,138: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2R1, groupName: test:groups:English, subject: Subject id: 00000000-0000-1000-6FA4-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 62, elapsed: 139 ms
2017-08-19 16:38:24,138: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2R1, groupName: test:groups:English, subject: Subject id: 00000000-0000-1000-6EDD-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 59, elapsed: 139 ms
2017-08-19 16:38:24,139: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2R1, groupName: test:groups:English, subject: Subject id: 00000000-0000-1000-910B-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 68, elapsed: 114 ms
2017-08-19 16:38:24,139: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2R1, groupName: test:groups:English, subject: Subject id: B8F3E2F6-5281-11D6-8001-080020CC75D3, sourceId: cmuDirectory, operation: add, success: true, threadId: 67, elapsed: 115 ms
2017-08-19 16:38:24,145: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2R1, groupName: test:groups:English, subject: Subject id: 1D4A7274-7CA1-11D8-8000-080020CC75D3, sourceId: cmuDirectory, operation: add, success: true, threadId: 60, elapsed: 101 ms
2017-08-19 16:38:24,149: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2R1, groupName: test:groups:English, subject: Subject id: 00000000-0000-1000-6D65-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 64, elapsed: 107 ms
2017-08-19 16:38:24,224: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2R1, groupName: test:groups:English, subject: Subject id: 00000000-0000-1000-910F-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 69, elapsed: 94 ms
2017-08-19 16:38:24,234: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2R1, groupName: test:groups:English, subject: Subject id: 0B5DEB68-61AF-11E0-8000-00144F799A7A, sourceId: cmuDirectory, operation: add, success: true, threadId: 61, elapsed: 118 ms
2017-08-19 16:38:24,252: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2R1, groupName: test:groups:English, subject: Subject id: 00000000-0000-1000-6E86-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 67, elapsed: 104 ms
2017-08-19 16:38:24,260: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2R1, groupName: test:groups:English, subject: Subject id: 446399DA-447D-11D5-8000-080020CC75D3, sourceId: cmuDirectory, operation: add, success: true, threadId: 62, elapsed: 106 ms
2017-08-19 16:38:24,260: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2R1, groupName: test:groups:English, subject: Subject id: 00000000-0000-1000-910D-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 63, elapsed: 110 ms
2017-08-19 16:38:24,260: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2R1, groupName: test:groups:English, subject: Subject id: 15525E2A-9064-11E2-8001-00144F799A7A, sourceId: cmuDirectory, operation: add, success: true, threadId: 65, elapsed: 110 ms
2017-08-19 16:38:24,415: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2R1, groupName: test:groups:English, subject: Subject id: 31B5E1EA-2CF7-11DB-8001-0003BA2FA263, sourceId: cmuDirectory, operation: add, success: true, threadId: 59, elapsed: 264 ms
2017-08-19 16:38:24,418: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2R1, groupName: test:groups:English, subject: Subject id: 00000000-0000-1000-910A-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 60, elapsed: 264 ms
2017-08-19 16:38:24,430: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2R1, groupName: test:groups:English, subject: Subject id: 00000000-0000-1000-4CA7-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 66, elapsed: 278 ms
2017-08-19 16:38:24,431: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2R1, groupName: test:groups:English, subject: Subject id: 9EA2E558-D980-11D9-8000-0003BA2FA263, sourceId: cmuDirectory, operation: add, success: true, threadId: 64, elapsed: 268 ms
2017-08-19 16:38:24,432: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2R1, groupName: test:groups:English, subject: Subject id: 5493D30C-A2EE-11D7-8000-080020CC75D3, sourceId: cmuDirectory, operation: add, success: true, threadId: 68, elapsed: 271 ms
2017-08-19 16:38:24,469: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2R1, groupName: test:groups:English, subject: Subject id: 00000000-0000-1000-63BB-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 69, elapsed: 237 ms
2017-08-19 16:38:24,498: logType: subjobLog, overallId: TGT1T2R0, subjobId: TGT1T2R1, parentJobName: LDAP_GROUPS_FROM_ATTRIBUTES__test:loaderLdapGroupsFromAttributes__39619a2492b24b028a9bb246a74a5ec2, groupName: test:groups:English, dryRun: false, jobName: subjobFor_test:groups:English, status: SUCCESS, jobType: LDAP_GROUPS_FROM_ATTRIBUTES, host: ISC15-0009-WD, rowsFromExternal: 23, rowsFromGrouper: 0, deleteCount: 0, insertCount: 23, updateCount: 0, totalCount: 23, millisGetData: 6398, millisLoadData: 1479, threadId: 55, elapsed: 1521 ms
2017-08-19 16:38:24,522: logType: groupManagement, overallId: TGT1T2R0, subjobId: TGT1T2R7, groupName: test:groups:Robotics, operation: INSERT, success: true, threadId: 56, elapsed: 383 ms
2017-08-19 16:38:24,582: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2R7, groupName: test:groups:Robotics, subject: Subject id: 5921D34C-CD09-11E2-8000-00144F799A7A, sourceId: cmuDirectory, operation: add, success: true, threadId: 55, elapsed: 32 ms
2017-08-19 16:38:24,595: logType: subjobLog, overallId: TGT1T2R0, subjobId: TGT1T2R7, parentJobName: LDAP_GROUPS_FROM_ATTRIBUTES__test:loaderLdapGroupsFromAttributes__39619a2492b24b028a9bb246a74a5ec2, groupName: test:groups:Robotics, dryRun: false, jobName: subjobFor_test:groups:Robotics, status: SUCCESS, jobType: LDAP_GROUPS_FROM_ATTRIBUTES, host: ISC15-0009-WD, rowsFromExternal: 1, rowsFromGrouper: 0, deleteCount: 0, insertCount: 1, updateCount: 0, totalCount: 1, millisGetData: 7547, millisLoadData: 443, threadId: 56, elapsed: 477 ms
2017-08-19 16:38:25,534: logType: groupManagement, overallId: TGT1T2R0, subjobId: TGT1T2SD, groupName: test:groups:Biological Sciences, operation: INSERT, success: true, threadId: 58, elapsed: 279 ms
2017-08-19 16:38:25,973: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2SD, groupName: test:groups:Biological Sciences, subject: Subject id: 326DC664-8CB3-11DA-8001-0003BA2FA263, sourceId: cmuDirectory, operation: add, success: true, threadId: 56, elapsed: 110 ms
2017-08-19 16:38:25,982: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2SD, groupName: test:groups:Biological Sciences, subject: Subject id: 407FACE8-085B-11E6-8000-00144F799A7A, sourceId: cmuDirectory, operation: add, success: true, threadId: 69, elapsed: 116 ms
2017-08-19 16:38:25,982: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2SD, groupName: test:groups:Biological Sciences, subject: Subject id: 68C0C292-00D1-11D6-8001-080020CC75D3, sourceId: cmuDirectory, operation: add, success: true, threadId: 66, elapsed: 123 ms
2017-08-19 16:38:25,982: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2SD, groupName: test:groups:Biological Sciences, subject: Subject id: E422231E-0B7E-11E6-8001-00144F799A7A, sourceId: cmuDirectory, operation: add, success: true, threadId: 55, elapsed: 118 ms
2017-08-19 16:38:25,985: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2SD, groupName: test:groups:Biological Sciences, subject: Subject id: 1D197096-E33F-11E4-8001-00144F799A7A, sourceId: cmuDirectory, operation: add, success: true, threadId: 59, elapsed: 118 ms
2017-08-19 16:38:25,985: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2SD, groupName: test:groups:Biological Sciences, subject: Subject id: F23F559C-77B8-11D7-8000-080020CC75D3, sourceId: cmuDirectory, operation: add, success: true, threadId: 64, elapsed: 115 ms
2017-08-19 16:38:25,985: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2SD, groupName: test:groups:Biological Sciences, subject: Subject id: DB4D0F4C-3DC0-11D6-8000-080020CC75D3, sourceId: cmuDirectory, operation: add, success: true, threadId: 63, elapsed: 121 ms
2017-08-19 16:38:25,985: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2SD, groupName: test:groups:Biological Sciences, subject: Subject id: 7C771DDC-6A5B-11D7-8000-080020CC75D3, sourceId: cmuDirectory, operation: add, success: true, threadId: 60, elapsed: 119 ms
2017-08-19 16:38:25,995: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2SD, groupName: test:groups:Biological Sciences, subject: Subject id: D8BBF34C-5F61-11D7-8000-080020CC75D3, sourceId: cmuDirectory, operation: add, success: true, threadId: 68, elapsed: 129 ms
2017-08-19 16:38:25,995: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2SD, groupName: test:groups:Biological Sciences, subject: Subject id: 0AF315E4-C6CA-11E3-8000-00144F799A7A, sourceId: cmuDirectory, operation: add, success: true, threadId: 65, elapsed: 122 ms
2017-08-19 16:38:26,004: logType: subjobLog, overallId: TGT1T2R0, subjobId: TGT1T2SD, parentJobName: LDAP_GROUPS_FROM_ATTRIBUTES__test:loaderLdapGroupsFromAttributes__39619a2492b24b028a9bb246a74a5ec2, groupName: test:groups:Biological Sciences, dryRun: false, jobName: subjobFor_test:groups:Biological Sciences, status: SUCCESS, jobType: LDAP_GROUPS_FROM_ATTRIBUTES, host: ISC15-0009-WD, rowsFromExternal: 10, rowsFromGrouper: 0, deleteCount: 0, insertCount: 10, updateCount: 0, totalCount: 10, millisGetData: 8662, millisLoadData: 741, threadId: 58, elapsed: 771 ms
2017-08-19 16:38:26,208: logType: groupManagement, overallId: TGT1T2R0, subjobId: TGT1T2SJ, groupName: test:groups:Mechanical Engineering, operation: INSERT, success: true, threadId: 57, elapsed: 99 ms
2017-08-19 16:38:27,001: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2SJ, groupName: test:groups:Mechanical Engineering, subject: Subject id: 00000000-0000-1000-7003-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 60, elapsed: 83 ms
2017-08-19 16:38:27,010: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2SJ, groupName: test:groups:Mechanical Engineering, subject: Subject id: 5921D34C-CD09-11E2-8000-00144F799A7A, sourceId: cmuDirectory, operation: add, success: true, threadId: 58, elapsed: 88 ms
2017-08-19 16:38:27,022: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2SJ, groupName: test:groups:Mechanical Engineering, subject: Subject id: 00000000-0000-1000-90C8-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 69, elapsed: 99 ms
2017-08-19 16:38:27,022: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2SJ, groupName: test:groups:Mechanical Engineering, subject: Subject id: 00000000-0000-1000-70A3-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 68, elapsed: 101 ms
2017-08-19 16:38:27,022: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2SJ, groupName: test:groups:Mechanical Engineering, subject: Subject id: F893646C-60A6-11D6-8000-080020CC75D3, sourceId: cmuDirectory, operation: add, success: true, threadId: 55, elapsed: 102 ms
2017-08-19 16:38:27,022: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2SJ, groupName: test:groups:Mechanical Engineering, subject: Subject id: 00000000-0000-1000-6480-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 66, elapsed: 96 ms
2017-08-19 16:38:27,022: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2SJ, groupName: test:groups:Mechanical Engineering, subject: Subject id: 00000000-0000-1000-90C5-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 63, elapsed: 101 ms
2017-08-19 16:38:27,033: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2SJ, groupName: test:groups:Mechanical Engineering, subject: Subject id: 00000000-0000-1000-6D83-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 65, elapsed: 109 ms
2017-08-19 16:38:27,033: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2SJ, groupName: test:groups:Mechanical Engineering, subject: Subject id: 891C00EE-A0BE-11D8-8000-080020CC75D3, sourceId: cmuDirectory, operation: add, success: true, threadId: 64, elapsed: 110 ms
2017-08-19 16:38:27,033: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2SJ, groupName: test:groups:Mechanical Engineering, subject: Subject id: 00000000-0000-1000-64CD-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 56, elapsed: 112 ms
2017-08-19 16:38:27,043: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2SJ, groupName: test:groups:Mechanical Engineering, subject: Subject id: 3AE63A5A-5414-11D6-8000-080020CC75D3, sourceId: cmuDirectory, operation: add, success: true, threadId: 59, elapsed: 119 ms
2017-08-19 16:38:27,075: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2SJ, groupName: test:groups:Mechanical Engineering, subject: Subject id: 344EC5E8-A0E5-11E2-8000-00144F799A7A, sourceId: cmuDirectory, operation: add, success: true, threadId: 58, elapsed: 59 ms
2017-08-19 16:38:27,078: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2SJ, groupName: test:groups:Mechanical Engineering, subject: Subject id: 00000000-0000-1000-6379-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 60, elapsed: 71 ms
2017-08-19 16:38:27,115: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2SJ, groupName: test:groups:Mechanical Engineering, subject: Subject id: 00000000-0000-1000-4E87-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 55, elapsed: 84 ms
2017-08-19 16:38:27,118: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2SJ, groupName: test:groups:Mechanical Engineering, subject: Subject id: 00000000-0000-1000-90C1-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 66, elapsed: 88 ms
2017-08-19 16:38:27,118: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2SJ, groupName: test:groups:Mechanical Engineering, subject: Subject id: 00000000-0000-1000-6669-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 69, elapsed: 87 ms
2017-08-19 16:38:27,120: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2SJ, groupName: test:groups:Mechanical Engineering, subject: Subject id: 00000000-0000-1000-90C3-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 63, elapsed: 90 ms
2017-08-19 16:38:27,127: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2SJ, groupName: test:groups:Mechanical Engineering, subject: Subject id: 00000000-0000-1000-90CD-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 68, elapsed: 97 ms
2017-08-19 16:38:27,159: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2SJ, groupName: test:groups:Mechanical Engineering, subject: Subject id: 00000000-0000-1000-90CA-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 56, elapsed: 110 ms
2017-08-19 16:38:27,168: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2SJ, groupName: test:groups:Mechanical Engineering, subject: Subject id: 00000000-0000-1000-6D27-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 64, elapsed: 120 ms
2017-08-19 16:38:27,168: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2SJ, groupName: test:groups:Mechanical Engineering, subject: Subject id: 00000000-0000-1000-122F-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 59, elapsed: 102 ms
2017-08-19 16:38:27,169: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2SJ, groupName: test:groups:Mechanical Engineering, subject: Subject id: 00000000-0000-1000-6785-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 65, elapsed: 128 ms
2017-08-19 16:38:27,185: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2SJ, groupName: test:groups:Mechanical Engineering, subject: Subject id: 00000000-0000-1000-12E4-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 58, elapsed: 94 ms
2017-08-19 16:38:27,189: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2SJ, groupName: test:groups:Mechanical Engineering, subject: Subject id: 00000000-0000-1000-43AD-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 60, elapsed: 98 ms
2017-08-19 16:38:27,203: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2SJ, groupName: test:groups:Mechanical Engineering, subject: Subject id: 00000000-0000-1000-6BF3-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 69, elapsed: 64 ms
2017-08-19 16:38:27,204: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2SJ, groupName: test:groups:Mechanical Engineering, subject: Subject id: 00000000-0000-1000-90C2-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 66, elapsed: 70 ms
2017-08-19 16:38:27,204: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2SJ, groupName: test:groups:Mechanical Engineering, subject: Subject id: 00000000-0000-1000-90C7-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 63, elapsed: 70 ms
2017-08-19 16:38:27,204: logType: membershipManagement, overallId: TGT1T2R0, subjobId: TGT1T2SJ, groupName: test:groups:Mechanical Engineering, subject: Subject id: 242C0320-F09C-11E4-8001-00144F799A7A, sourceId: cmuDirectory, operation: add, success: true, threadId: 55, elapsed: 78 ms
2017-08-19 16:38:27,215: logType: subjobLog, overallId: TGT1T2R0, subjobId: TGT1T2SJ, parentJobName: LDAP_GROUPS_FROM_ATTRIBUTES__test:loaderLdapGroupsFromAttributes__39619a2492b24b028a9bb246a74a5ec2, groupName: test:groups:Mechanical Engineering, dryRun: false, jobName: subjobFor_test:groups:Mechanical Engineering, status: SUCCESS, jobType: LDAP_GROUPS_FROM_ATTRIBUTES, host: ISC15-0009-WD, rowsFromExternal: 28, rowsFromGrouper: 0, deleteCount: 0, insertCount: 28, updateCount: 0, totalCount: 28, millisGetData: 9516, millisLoadData: 1097, threadId: 57, elapsed: 1130 ms
2017-08-19 16:38:27,219: logType: overallLog, overallId: TGT1T2R0, dryRun: false, jobName: LDAP_GROUPS_FROM_ATTRIBUTES__test:loaderLdapGroupsFromAttributes__39619a2492b24b028a9bb246a74a5ec2, status: SUCCESS, jobType: LDAP_GROUPS_FROM_ATTRIBUTES, host: ISC15-0009-WD, serverId: personLdap, filter: (&(objectClass=cmuPerson)(cmuAndrewId=al*)(|(cmuDepartment=Mechanical Engineering)(cmuDepartment=Biological Sciences)(cmuDepartment=English))), subjectAttribute: cmuAndrewCommonNamespaceId, searchDn: ou=person, rowsFromExternal: 62, groupSizeExternal: 4, deleteCount: 0, insertCount: 62, updateCount: 0, totalCount: 62, millisGetData: 314, millisLoadData: 10624, threadId: 49, elapsed: 10972 ms

```

## LDAP group list

```
2017-08-19 16:46:05,312: logType: groupManagement, overallId: TGT1T2S9, subjobId: TGT1T2TA, groupName: test:groupList:SOFTDIST2:SYSTEM:FACULTY_EMERITI, operation: INSERT, success: true, threadId: 70, elapsed: 224 ms
2017-08-19 16:46:05,330: logType: subjobLog, overallId: TGT1T2S9, subjobId: TGT1T2TA, parentJobName: LDAP_GROUP_LIST__test:ldapLoaderGroupList__1570e4c697a747289d4a62554bf9c7e8, groupName: test:groupList:SOFTDIST2:SYSTEM:FACULTY_EMERITI, dryRun: false, jobName: subjobFor_test:groupList:SOFTDIST2:SYSTEM:FACULTY_EMERITI, status: SUCCESS, jobType: LDAP_GROUP_LIST, host: ISC15-0009-WD, rowsFromExternal: 0, rowsFromGrouper: 0, deleteCount: 0, insertCount: 0, updateCount: 0, totalCount: 0, millisGetData: 24, millisLoadData: 226, threadId: 70, elapsed: 264 ms
2017-08-19 16:46:06,431: logType: groupManagement, overallId: TGT1T2S9, subjobId: TGT1T2TD, groupName: test:groupList:SOFTDIST2:SYSTEM:ADMINS, operation: INSERT, success: true, threadId: 72, elapsed: 1331 ms
2017-08-19 16:46:06,433: logType: groupManagement, overallId: TGT1T2S9, subjobId: TGT1T2TB, groupName: test:groupList:SOFTDIST2:SYSTEM:DEPT_ADMINS, operation: INSERT, success: true, threadId: 73, elapsed: 1336 ms
2017-08-19 16:46:06,433: logType: groupManagement, overallId: TGT1T2S9, subjobId: TGT1T2TC, groupName: test:groupList:SOFTDIST2:SYSTEM:LOG_VIEWERS, operation: INSERT, success: true, threadId: 71, elapsed: 1334 ms
2017-08-19 16:46:06,713: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TC, groupName: test:groupList:SOFTDIST2:SYSTEM:LOG_VIEWERS, subject: Subject id: 00000000-0000-1000-7362-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 79, elapsed: 58 ms
2017-08-19 16:46:06,716: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TC, groupName: test:groupList:SOFTDIST2:SYSTEM:LOG_VIEWERS, subject: Subject id: E7EECF6A-5D94-11DD-8001-0003BA2FA263, sourceId: cmuDirectory, operation: add, success: true, threadId: 76, elapsed: 59 ms
2017-08-19 16:46:06,718: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TC, groupName: test:groupList:SOFTDIST2:SYSTEM:LOG_VIEWERS, subject: Subject id: 764C4A50-5A89-11DB-8001-0003BA2FA263, sourceId: cmuDirectory, operation: add, success: true, threadId: 77, elapsed: 68 ms
2017-08-19 16:46:06,732: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TC, groupName: test:groupList:SOFTDIST2:SYSTEM:LOG_VIEWERS, subject: Subject id: CCDCA6B6-FB63-11DE-8000-0003BA2FA263, sourceId: cmuDirectory, operation: add, success: true, threadId: 70, elapsed: 82 ms
2017-08-19 16:46:06,734: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TC, groupName: test:groupList:SOFTDIST2:SYSTEM:LOG_VIEWERS, subject: Subject id: 00000000-0000-1000-1126-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 75, elapsed: 85 ms
2017-08-19 16:46:06,734: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TC, groupName: test:groupList:SOFTDIST2:SYSTEM:LOG_VIEWERS, subject: Subject id: 6801E430-3183-11DA-8000-0003BA2FA263, sourceId: cmuDirectory, operation: add, success: true, threadId: 78, elapsed: 57 ms
2017-08-19 16:46:06,734: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TC, groupName: test:groupList:SOFTDIST2:SYSTEM:LOG_VIEWERS, subject: Subject id: AC8CD8FA-6318-11DF-8000-0003BA2FA263, sourceId: cmuDirectory, operation: add, success: true, threadId: 80, elapsed: 58 ms
2017-08-19 16:46:06,736: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TC, groupName: test:groupList:SOFTDIST2:SYSTEM:LOG_VIEWERS, subject: Subject id: 00000000-0000-1000-0001-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 74, elapsed: 81 ms
2017-08-19 16:46:06,753: logType: subjobLog, overallId: TGT1T2S9, subjobId: TGT1T2TC, parentJobName: LDAP_GROUP_LIST__test:ldapLoaderGroupList__1570e4c697a747289d4a62554bf9c7e8, groupName: test:groupList:SOFTDIST2:SYSTEM:LOG_VIEWERS, dryRun: false, jobName: subjobFor_test:groupList:SOFTDIST2:SYSTEM:LOG_VIEWERS, status: SUCCESS, jobType: LDAP_GROUP_LIST, host: ISC15-0009-WD, rowsFromExternal: 8, rowsFromGrouper: 0, deleteCount: 0, insertCount: 8, updateCount: 0, totalCount: 8, millisGetData: 35, millisLoadData: 1637, threadId: 71, elapsed: 1680 ms
2017-08-19 16:46:06,925: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TB, groupName: test:groupList:SOFTDIST2:SYSTEM:DEPT_ADMINS, subject: Subject id: 00000000-0000-1000-0263-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 70, elapsed: 87 ms
2017-08-19 16:46:06,957: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TB, groupName: test:groupList:SOFTDIST2:SYSTEM:DEPT_ADMINS, subject: Subject id: 00000000-0000-1000-8652-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 77, elapsed: 119 ms
2017-08-19 16:46:06,964: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TB, groupName: test:groupList:SOFTDIST2:SYSTEM:DEPT_ADMINS, subject: Subject id: 00000000-0000-1000-7337-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 71, elapsed: 113 ms
2017-08-19 16:46:06,965: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TB, groupName: test:groupList:SOFTDIST2:SYSTEM:DEPT_ADMINS, subject: Subject id: 156D59B4-9566-11D6-8001-080020CC75D3, sourceId: cmuDirectory, operation: add, success: true, threadId: 75, elapsed: 115 ms
2017-08-19 16:46:06,966: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TB, groupName: test:groupList:SOFTDIST2:SYSTEM:DEPT_ADMINS, subject: Subject id: 00000000-0000-1000-71C6-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 82, elapsed: 128 ms
2017-08-19 16:46:06,979: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TB, groupName: test:groupList:SOFTDIST2:SYSTEM:DEPT_ADMINS, subject: Subject id: 00000000-0000-1000-4F22-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 74, elapsed: 138 ms
2017-08-19 16:46:06,980: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TB, groupName: test:groupList:SOFTDIST2:SYSTEM:DEPT_ADMINS, subject: Subject id: 6B58D064-1A93-11D5-8000-080020CC75D3, sourceId: cmuDirectory, operation: add, success: true, threadId: 79, elapsed: 136 ms
2017-08-19 16:46:06,981: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TB, groupName: test:groupList:SOFTDIST2:SYSTEM:DEPT_ADMINS, subject: Subject id: 00000000-0000-1000-33ED-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 80, elapsed: 138 ms
2017-08-19 16:46:06,992: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TB, groupName: test:groupList:SOFTDIST2:SYSTEM:DEPT_ADMINS, subject: Subject id: 00000000-0000-1000-0359-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 76, elapsed: 149 ms
2017-08-19 16:46:06,996: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TB, groupName: test:groupList:SOFTDIST2:SYSTEM:DEPT_ADMINS, subject: Subject id: 00000000-0000-1000-2A02-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 81, elapsed: 158 ms
2017-08-19 16:46:06,997: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TB, groupName: test:groupList:SOFTDIST2:SYSTEM:DEPT_ADMINS, subject: Subject id: 00000000-0000-1000-51FC-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 78, elapsed: 146 ms
2017-08-19 16:46:06,997: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TB, groupName: test:groupList:SOFTDIST2:SYSTEM:DEPT_ADMINS, subject: Subject id: 00000000-0000-1000-005C-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 70, elapsed: 65 ms
2017-08-19 16:46:07,022: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TB, groupName: test:groupList:SOFTDIST2:SYSTEM:DEPT_ADMINS, subject: Subject id: 00000000-0000-1000-2A49-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 82, elapsed: 46 ms
2017-08-19 16:46:07,024: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TB, groupName: test:groupList:SOFTDIST2:SYSTEM:DEPT_ADMINS, subject: Subject id: 00000000-0000-1000-043E-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 75, elapsed: 49 ms
2017-08-19 16:46:07,024: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TB, groupName: test:groupList:SOFTDIST2:SYSTEM:DEPT_ADMINS, subject: Subject id: 00000000-0000-1000-06F8-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 77, elapsed: 61 ms
2017-08-19 16:46:07,040: logType: subjobLog, overallId: TGT1T2S9, subjobId: TGT1T2TB, parentJobName: LDAP_GROUP_LIST__test:ldapLoaderGroupList__1570e4c697a747289d4a62554bf9c7e8, groupName: test:groupList:SOFTDIST2:SYSTEM:DEPT_ADMINS, dryRun: false, jobName: subjobFor_test:groupList:SOFTDIST2:SYSTEM:DEPT_ADMINS, status: SUCCESS, jobType: LDAP_GROUP_LIST, host: ISC15-0009-WD, rowsFromExternal: 15, rowsFromGrouper: 0, deleteCount: 0, insertCount: 15, updateCount: 0, totalCount: 15, millisGetData: 33, millisLoadData: 1927, threadId: 73, elapsed: 1973 ms
2017-08-19 16:46:07,404: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TD, groupName: test:groupList:SOFTDIST2:SYSTEM:ADMINS, subject: Subject id: 00000000-0000-1000-1126-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 76, elapsed: 212 ms
2017-08-19 16:46:07,413: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TD, groupName: test:groupList:SOFTDIST2:SYSTEM:ADMINS, subject: Subject id: 00000000-0000-1000-5FAF-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 77, elapsed: 222 ms
2017-08-19 16:46:07,413: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TD, groupName: test:groupList:SOFTDIST2:SYSTEM:ADMINS, subject: Subject id: 80126D8E-4A35-11DC-8001-0003BA2FA263, sourceId: cmuDirectory, operation: add, success: true, threadId: 74, elapsed: 219 ms
2017-08-19 16:46:07,414: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TD, groupName: test:groupList:SOFTDIST2:SYSTEM:ADMINS, subject: Subject id: F99323EC-9FEE-11E1-8000-00144F799A7A, sourceId: cmuDirectory, operation: add, success: true, threadId: 79, elapsed: 218 ms
2017-08-19 16:46:07,427: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TD, groupName: test:groupList:SOFTDIST2:SYSTEM:ADMINS, subject: Subject id: 09326596-2750-11E6-8001-FFFF9E690280, sourceId: cmuDirectory, operation: add, success: true, threadId: 80, elapsed: 235 ms
2017-08-19 16:46:07,429: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TD, groupName: test:groupList:SOFTDIST2:SYSTEM:ADMINS, subject: Subject id: 79D82B22-9CD0-11D8-8000-080020CC75D3, sourceId: cmuDirectory, operation: add, success: true, threadId: 82, elapsed: 237 ms
2017-08-19 16:46:07,429: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TD, groupName: test:groupList:SOFTDIST2:SYSTEM:ADMINS, subject: Subject id: B72723FA-194A-11E4-8000-00144F799A7A, sourceId: cmuDirectory, operation: add, success: true, threadId: 70, elapsed: 237 ms
2017-08-19 16:46:07,429: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TD, groupName: test:groupList:SOFTDIST2:SYSTEM:ADMINS, subject: Subject id: E5701B7A-A5BD-11E1-8000-00144F799A7A, sourceId: cmuDirectory, operation: add, success: true, threadId: 75, elapsed: 236 ms
2017-08-19 16:46:07,431: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TD, groupName: test:groupList:SOFTDIST2:SYSTEM:ADMINS, subject: Subject id: 64D81E60-9792-11E6-8000-FFFF9E690280, sourceId: cmuDirectory, operation: add, success: true, threadId: 78, elapsed: 241 ms
2017-08-19 16:46:07,431: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TD, groupName: test:groupList:SOFTDIST2:SYSTEM:ADMINS, subject: Subject id: 33C3753E-2986-11E5-8000-FFFF9E690280, sourceId: cmuDirectory, operation: add, success: true, threadId: 73, elapsed: 237 ms
2017-08-19 16:46:07,431: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TD, groupName: test:groupList:SOFTDIST2:SYSTEM:ADMINS, subject: Subject id: CCDCA6B6-FB63-11DE-8000-0003BA2FA263, sourceId: cmuDirectory, operation: add, success: true, threadId: 81, elapsed: 237 ms
2017-08-19 16:46:07,499: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TD, groupName: test:groupList:SOFTDIST2:SYSTEM:ADMINS, subject: Subject id: 4D5E00D0-A4FB-11E6-8001-FFFF9E690280, sourceId: cmuDirectory, operation: add, success: true, threadId: 76, elapsed: 85 ms
2017-08-19 16:46:07,504: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TD, groupName: test:groupList:SOFTDIST2:SYSTEM:ADMINS, subject: Subject id: 3D250A02-BC4A-11D4-8001-080020CC75D3, sourceId: cmuDirectory, operation: add, success: true, threadId: 79, elapsed: 81 ms
2017-08-19 16:46:07,505: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TD, groupName: test:groupList:SOFTDIST2:SYSTEM:ADMINS, subject: Subject id: 04AFBC16-B7C8-11DE-8000-0003BA2FA263, sourceId: cmuDirectory, operation: add, success: true, threadId: 74, elapsed: 82 ms
2017-08-19 16:46:07,516: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TD, groupName: test:groupList:SOFTDIST2:SYSTEM:ADMINS, subject: Subject id: 00000000-0000-1000-0270-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 75, elapsed: 79 ms
2017-08-19 16:46:07,517: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TD, groupName: test:groupList:SOFTDIST2:SYSTEM:ADMINS, subject: Subject id: 6AF6F83E-B187-11E6-8000-FFFF9E690280, sourceId: cmuDirectory, operation: add, success: true, threadId: 70, elapsed: 81 ms
2017-08-19 16:46:07,519: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TD, groupName: test:groupList:SOFTDIST2:SYSTEM:ADMINS, subject: Subject id: 1084ED4C-F103-11E5-8000-FFFF9E690280, sourceId: cmuDirectory, operation: add, success: true, threadId: 77, elapsed: 96 ms
2017-08-19 16:46:07,520: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TD, groupName: test:groupList:SOFTDIST2:SYSTEM:ADMINS, subject: Subject id: 00000000-0000-1000-625E-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 82, elapsed: 82 ms
2017-08-19 16:46:07,520: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TD, groupName: test:groupList:SOFTDIST2:SYSTEM:ADMINS, subject: Subject id: 00000000-0000-1000-114A-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 78, elapsed: 72 ms
2017-08-19 16:46:07,535: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TD, groupName: test:groupList:SOFTDIST2:SYSTEM:ADMINS, subject: Subject id: 678C81B6-B1D6-11E0-8000-00144F799A7A, sourceId: cmuDirectory, operation: add, success: true, threadId: 73, elapsed: 84 ms
2017-08-19 16:46:07,535: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TD, groupName: test:groupList:SOFTDIST2:SYSTEM:ADMINS, subject: Subject id: 9BC445FA-32BF-11E4-8000-00144F799A7A, sourceId: cmuDirectory, operation: add, success: true, threadId: 81, elapsed: 84 ms
2017-08-19 16:46:07,535: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TD, groupName: test:groupList:SOFTDIST2:SYSTEM:ADMINS, subject: Subject id: E7EECF6A-5D94-11DD-8001-0003BA2FA263, sourceId: cmuDirectory, operation: add, success: true, threadId: 80, elapsed: 99 ms
2017-08-19 16:46:07,593: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TD, groupName: test:groupList:SOFTDIST2:SYSTEM:ADMINS, subject: Subject id: 00000000-0000-1000-0001-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 79, elapsed: 77 ms
2017-08-19 16:46:07,619: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TD, groupName: test:groupList:SOFTDIST2:SYSTEM:ADMINS, subject: Subject id: 00000000-0000-1000-40B7-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 74, elapsed: 103 ms
2017-08-19 16:46:07,621: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TD, groupName: test:groupList:SOFTDIST2:SYSTEM:ADMINS, subject: Subject id: E4A179D2-5656-11DE-8000-0003BA2FA263, sourceId: cmuDirectory, operation: add, success: true, threadId: 77, elapsed: 92 ms
2017-08-19 16:46:07,621: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TD, groupName: test:groupList:SOFTDIST2:SYSTEM:ADMINS, subject: Subject id: 5B5A4904-53F1-11D9-8001-0003BA2FA263, sourceId: cmuDirectory, operation: add, success: true, threadId: 78, elapsed: 92 ms
2017-08-19 16:46:07,621: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TD, groupName: test:groupList:SOFTDIST2:SYSTEM:ADMINS, subject: Subject id: C86167F2-8500-11DB-8000-0003BA2FA263, sourceId: cmuDirectory, operation: add, success: true, threadId: 76, elapsed: 106 ms
2017-08-19 16:46:07,621: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TD, groupName: test:groupList:SOFTDIST2:SYSTEM:ADMINS, subject: Subject id: 00000000-0000-1000-7362-0800207F02E6, sourceId: cmuDirectory, operation: add, success: true, threadId: 70, elapsed: 93 ms
2017-08-19 16:46:07,621: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TD, groupName: test:groupList:SOFTDIST2:SYSTEM:ADMINS, subject: Subject id: 83E90F78-A944-11DF-8000-00144F799A7A, sourceId: cmuDirectory, operation: add, success: true, threadId: 82, elapsed: 93 ms
2017-08-19 16:46:07,621: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TD, groupName: test:groupList:SOFTDIST2:SYSTEM:ADMINS, subject: Subject id: A24F4118-0A25-11DE-8000-0003BA2FA263, sourceId: cmuDirectory, operation: add, success: true, threadId: 75, elapsed: 98 ms
2017-08-19 16:46:07,621: logType: membershipManagement, overallId: TGT1T2S9, subjobId: TGT1T2TD, groupName: test:groupList:SOFTDIST2:SYSTEM:ADMINS, subject: Subject id: AC8CD8FA-6318-11DF-8000-0003BA2FA263, sourceId: cmuDirectory, operation: add, success: true, threadId: 80, elapsed: 66 ms
2017-08-19 16:46:07,638: logType: subjobLog, overallId: TGT1T2S9, subjobId: TGT1T2TD, parentJobName: LDAP_GROUP_LIST__test:ldapLoaderGroupList__1570e4c697a747289d4a62554bf9c7e8, groupName: test:groupList:SOFTDIST2:SYSTEM:ADMINS, dryRun: false, jobName: subjobFor_test:groupList:SOFTDIST2:SYSTEM:ADMINS, status: SUCCESS, jobType: LDAP_GROUP_LIST, host: ISC15-0009-WD, rowsFromExternal: 31, rowsFromGrouper: 0, deleteCount: 0, insertCount: 31, updateCount: 0, totalCount: 31, millisGetData: 35, millisLoadData: 2523, threadId: 72, elapsed: 2564 ms
2017-08-19 16:46:07,643: logType: overallLog, overallId: TGT1T2S9, dryRun: false, jobName: LDAP_GROUP_LIST__test:ldapLoaderGroupList__1570e4c697a747289d4a62554bf9c7e8, status: SUCCESS, jobType: LDAP_GROUP_LIST, host: ISC15-0009-WD, serverId: personLdap, filter: (&(objectClass=cmuGroup)(cn=softdist2:system*)), subjectAttribute: member, searchDn: ou=group, rowsFromExternal: 54, groupSizeExternal: 3, deleteCount: 0, insertCount: 54, updateCount: 0, totalCount: 54, millisGetData: 251, millisLoadData: 2574, threadId: 47, elapsed: 2864 ms

```

## Other daemon jobs

```
2017-08-19 18:10:00,344: logType: overallLog, overallId: TGT46FNR, startTime: Sat Aug 19 18:10:00 EDT 2017, jobName: MAINTENANCE__builtinMessagingDaemon, dryRun: false, quartzCron: 0 10 * * * ?, status: SUCCESS, jobType: MAINTENANCE, host: fastprod-medium-a-01, jobMessage: Ran builtin messaging daemon, deleted 0 processed records, deleted 0 unprocessed records., threadId: 84, elapsed: 43 ms
2017-08-19 18:10:02,078: logType: overallLog, overallId: TGT46FNT, startTime: Sat Aug 19 18:10:02 EDT 2017, jobName: CHANGE_LOG_consumer_awsJira, dryRun: false, quartzCron: 2 * * * * ?, status: SUCCESS, jobType: CHANGE_LOG, host: fastprod-medium-a-01, threadId: 85, elapsed: 27 ms
2017-08-19 18:10:04,073: logType: overallLog, overallId: TGT46FNV, startTime: Sat Aug 19 18:10:04 EDT 2017, jobName: CHANGE_LOG_consumer_grouperRules, dryRun: false, quartzCron: 4 * * * * ?, status: SUCCESS, jobType: CHANGE_LOG, host: fastprod-medium-a-01, threadId: 86, elapsed: 57 ms
2017-08-19 18:10:12,045: logType: overallLog, overallId: TGT46FNX, startTime: Sat Aug 19 18:10:12 EDT 2017, jobName: CHANGE_LOG_consumer_syncGroups, dryRun: false, quartzCron: 12 * * * * ?, status: SUCCESS, jobType: CHANGE_LOG, host: fastprod-medium-a-01, threadId: 87, elapsed: 27 ms
2017-08-19 18:10:50,153: logType: overallLog, overallId: TGT46FNZ, startTime: Sat Aug 19 18:10:50 EDT 2017, jobName: CHANGE_LOG_changeLogTempToChangeLog, dryRun: false, quartzCron: 50 * * * * ?, status: SUCCESS, jobType: CHANGE_LOG, host: fastprod-medium-a-01, jobMessage: Ran the changeLogTempToChangeLog daemon, threadId: 88, elapsed: 112 ms
2017-08-19 18:11:00,186: logType: overallLog, overallId: TGT46FN1, startTime: Sat Aug 19 18:11:00 EDT 2017, jobName: CHANGE_LOG_consumer_pcd, dryRun: false, quartzCron: 0 * * * * ?, status: SUCCESS, jobType: CHANGE_LOG, host: fastprod-medium-a-01, threadId: 89, elapsed: 49 ms
2017-08-19 18:11:14,108: logType: overallLog, overallId: TGT46FN3, startTime: Sat Aug 19 18:11:14 EDT 2017, jobName: CHANGE_LOG_consumer_duo, dryRun: false, quartzCron: 14 * * * * ?, status: SUCCESS, jobType: CHANGE_LOG, host: fastprod-medium-a-01, threadId: 80, elapsed: 89 ms
2017-08-19 18:12:00,118: logType: overallLog, overallId: TGT46FN5, startTime: Sat Aug 19 18:12:00 EDT 2017, jobName: CHANGE_LOG_consumer_pcd, dryRun: false, quartzCron: 0 * * * * ?, status: SUCCESS, jobType: CHANGE_LOG, host: fastprod-medium-a-01, threadId: 81, elapsed: 20 ms
2017-08-19 18:12:12,062: logType: overallLog, overallId: TGT46FN7, startTime: Sat Aug 19 18:12:12 EDT 2017, jobName: CHANGE_LOG_consumer_syncGroups, dryRun: false, quartzCron: 12 * * * * ?, status: SUCCESS, jobType: CHANGE_LOG, host: fastprod-medium-a-01, threadId: 82, elapsed: 35 ms
2017-08-19 18:13:00,179: logType: overallLog, overallId: TGT46FN9, startTime: Sat Aug 19 18:13:00 EDT 2017, jobName: CHANGE_LOG_consumer_pspng_activedirectory, dryRun: false, quartzCron: 0 * * * * ?, status: SUCCESS, jobType: CHANGE_LOG, host: fastprod-medium-a-01, threadId: 83, elapsed: 98 ms

```

## Example logging code

```
    Map<String, Object> debugMap = null;
    long start = System.nanoTime();

    if (SomeSpecializedLoggerClass.isLoggingEnabled()) {
      debugMap = new LinkedHashMap<String, Object>();
      debugMap.put("method", "someMethodName");
      debugMap.put("entered", GrouperUtil.timestampToString(new Date()));
    }

    try {
      //code
      
      if (SomeSpecializedLoggerClass.isLoggingEnabled()) {
        debugMap.put("someKey", someValue);
      }

      //code

    } finally {
    
      if (SomeSpecializedLoggerClass.isLoggingEnabled()) {
        long nanos = System.nanoTime() - start;
        debugMap.put("elapsedMillis", nanos/1000000L);
        SomeSpecializedLoggerClass.log(GrouperUtil.mapToString(debugMap));
      }

    }
```
