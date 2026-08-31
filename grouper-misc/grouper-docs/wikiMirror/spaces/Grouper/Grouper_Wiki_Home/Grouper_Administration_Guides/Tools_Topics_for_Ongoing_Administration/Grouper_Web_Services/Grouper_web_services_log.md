---
title: "Grouper web services log"
space: Grouper
pageId: 28548552
version: 8
lastUpdated: 2026-07-01T05:44:12.745Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548552/Grouper+web+services+log
---

This will log to a file for web services. You need Grouper WS v2.3.0+ patch 12.

The log will have one entry for a request.

## Configure

In the log4j.properties, configure the web services to a file for debug

```
log4j.appender.grouper_ws                           = org.apache.log4j.DailyRollingFileAppender
log4j.appender.grouper_ws.File                      = ${grouper.home}logs/grouper_ws.log
log4j.appender.grouper_ws.DatePattern               = '.'yyyy-MM-dd
log4j.appender.grouper_ws.MaxBackupIndex            = 30
log4j.appender.grouper_ws.layout                    = org.apache.log4j.PatternLayout
log4j.appender.grouper_ws.layout.ConversionPattern  = %d{ISO8601}: %m%n

log4j.appender.grouper_ws_longRunning                           = org.apache.log4j.DailyRollingFileAppender
log4j.appender.grouper_ws_longRunning.File                      = ${grouper.home}logs/grouper_ws_longRunning.log
log4j.appender.grouper_ws_longRunning.DatePattern               = '.'yyyy-MM-dd
log4j.appender.grouper_ws_longRunning.MaxBackupIndex            = 30
log4j.appender.grouper_ws_longRunning.layout                    = org.apache.log4j.PatternLayout
log4j.appender.grouper_ws_longRunning.layout.ConversionPattern  = %d{ISO8601}: %m%n

# ws logs
log4j.logger.edu.internet2.middleware.grouper.ws.util.GrouperWsLog = DEBUG, grouper_ws
log4j.additivity.edu.internet2.middleware.grouper.ws.util.GrouperWsLog = false
log4j.logger.edu.internet2.middleware.grouper.ws.util.GrouperWsLongRunningLog = DEBUG, grouper_ws_longRunning
log4j.additivity.edu.internet2.middleware.grouper.ws.util.GrouperWsLongRunningLog = false

```

### Example

```
2018-05-01 18:57:43,764: start: 18:57:43.638, remoteAddr: 127.0.0.1, requestUrl: http://localhost:8088/grouperWs/servicesRest/v2_3_000/permissionAssignments, method: getPermissionAssignments, clientVersion: 2.3.0, immediateOnly: false, includeAssignmentsOnAssignments: false, includeAttributeAssignments: false, includeAttributeDefNames: false, includeGroupDetail: false, includeLimits: false, includePermissionAssignDetail: false, includeSubjectDetail: false, pointInTimeFrom: 2018-05-01 18:57:43.076, pointInTimeTo: 2018-05-01 18:57:43.076, wsAttributeDefLookups: Array size: 1: [0]: name: aStem:permissionDef, userIdLoggedIn: GrouperSystem, userIdLoggedInSource: g:isa, success: T, resultCode: SUCCESS, serverVersion: 2.3.0, resultsSize: 0, elapsedMillis: 126

2018-05-01 21:50:48,283: start: 21:50:48.272, remoteAddr: 127.0.0.1, requestUrl: http://localhost:8088/grouperWs/servicesRest/v2_3_000/attributeDefNames, method: findAttributeDefNames, clientVersion: 2.3.0, wsAttributeDefNameLookups: Array size: 2: [0]: name: aStem1:newAttributeDefName1, [1]: name: aStem1:newAttributeDefName2, userIdLoggedIn: GrouperSystem, userIdLoggedInSource: g:isa, success: T, resultCode: SUCCESS, serverVersion: 2.3.0, resultsSize: 2, elapsedMillis: 11

2018-05-01 19:09:56,943: start: 19:09:56.901, remoteAddr: 127.0.0.1, requestUrl: http://localhost:8088/grouperWs/servicesRest/v2_3_000/memberships, method: getMemberships, clientVersion: 2.3.0, includeGroupDetail: false, includeSubjectDetail: false, subjectAttributeNames: Array size: 2: [0]: a, [1]: name, wsGroupLookups: Array size: 2: [0]: name: aStem:aGroup, [1]: name: aStem:aGroup2, wsMemberFilter: All, userIdLoggedIn: GrouperSystem, userIdLoggedInSource: g:isa, success: T, resultCode: SUCCESS, serverVersion: 2.3.0, resultsSize: 4, elapsedMillis: 41

```

## Request and correlation id

Set these headers for request and correlation id

```
X-Correlation-Id: xyz
X-Request-Id: abc
```

The log messages on the server will look like this

```
2022-12-12T22:36:40,099: [http-nio-8080-exec-7] DEBUG GrouperLogger.debug(62) - [< GrouperSystem - 127.0.0.1 >] - reqId: abc, corrId: xyz, start: 22:36:19.842, remoteAddr: 127.0.0.1, requestUrl: http://localhost:8080/grouper-ws/servicesRest/2.6.0/groups, method: addMember, clientVersion: 2.6.0, includeGroupDetail: false, includeSubjectDetail: false, replaceAllExisting: false, subjectLookups: Array size: 2: [0]: subjectId: test.subject.0, [1]: subjectId: test.subject.1, txType: NONE, wsGroupLookup: name: aStem:aGroup, userIdLoggedIn: GrouperSystem, userIdLoggedInSource: g:isa, successes: 2, failures: 0, success: T, resultCode: SUCCESS, serverVersion: 2.6.0, resultsSize: 2, elapsedMillis: 266

```
