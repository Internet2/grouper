---
title: "Get Groups"
space: Grouper
pageId: 28547773
version: 10
lastUpdated: 2026-07-01T05:46:13.471Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547773/Get+Groups
---

[Grouper Web Services](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544233/Grouper+Web+Services)

#### Description

Get groups will get the groups that a subject is in

#### Features

- Can base member list based on memberfilter (e.g. All, Immediate, Effective, Composite)
- Lookup subjects by subject lookup (by id, source, identifier, etc)
- Lookup groups by group lookup (by name or uuid)
- Returns group / subject information, can be detailed or not
- Can actAs another user
- For 2.0+, you can pass in pointInTimeFrom and pointInTimeTo to check members at a certain point in time in the past, or in a date range. This should be formatted: yyyy/MM/dd HH:mm:ss.SSS

#### Get groups Lite service

- Accepts one subject to list groups
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#getGroupsLite-edu.internet2.middleware.grouper.misc.GrouperVersion-java.lang.String-java.lang.String-java.lang.String-edu.internet2.middleware.grouper.ws.member.WsMemberFilter-java.lang.String-java.lang.String-java.lang.String-boolean-boolean-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-edu.internet2.middleware.grouper.ws.query.StemScope-java.lang.String-java.lang.Integer-java.lang.Integer-java.lang.String-java.lang.Boolean-java.sql.Timestamp-java.sql.Timestamp-java.lang.Boolean-java.lang.String-java.lang.String-java.lang.Boolean-) (click on getGroupsLite), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#getGroupsLite-edu.internet2.middleware.grouper.misc.GrouperVersion-java.lang.String-java.lang.String-edu.internet2.middleware.grouper.ws.rest.group.WsRestGetGroupsLiteRequest-) (click on getGroupsLite)
- For REST, the request can put data in query string (in URL or request body)
- REST request (colon is escaped to %3A): GET /grouper-ws/servicesRest/v1_3_000/subjects/10021368/groups
  
  - Note: if passing data in request body e.g. actAs, use a POST
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/group/WsRestGetGroupsLiteRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/soap/WsGetGroupsLiteResult.html)
- [Response codes](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsGetGroupsLiteResult.WsGetGroupsLiteResultCode.html)
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/getGroups/) (all files with "Lite" in them, click on "download" to see file)

#### Get groups service

- Accepts multiple subjects to retrieve multiple lists of groups
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#getGroups-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup:A-edu.internet2.middleware.grouper.ws.member.WsMemberFilter-edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup-boolean-boolean-java.lang.String:A-edu.internet2.middleware.grouper.ws.coresoap.WsParam:A-java.lang.String-java.lang.String-edu.internet2.middleware.grouper.ws.coresoap.WsStemLookup-edu.internet2.middleware.grouper.ws.query.StemScope-java.lang.String-java.lang.Integer-java.lang.Integer-java.lang.String-java.lang.Boolean-java.sql.Timestamp-java.sql.Timestamp-java.lang.Boolean-java.lang.String-java.lang.String-java.lang.Boolean-) (click on getGroups), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#getGroups-edu.internet2.middleware.grouper.misc.GrouperVersion-java.lang.String-java.lang.String-edu.internet2.middleware.grouper.ws.rest.group.WsRestGetGroupsRequest-) (click on getGroups)
- REST request (colon is escaped to %3A): POST /grouper-ws/servicesRest/v1_3_000/subjects/groups
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/group/WsRestGetGroupsRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/soap/WsGetGroupsResults.html)
- [Response codes overall](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsGetGroupsResults.WsGetGroupsResultsCode.html), [response codes for each assignment](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsGetGroupsResult.WsGetGroupsResultCode.html)
- Returns an overall status, and a status for each assignment
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/getGroups/) (all files without "Lite" in them, click on "download" to see files)
