---
title: "Add Member"
space: Grouper
pageId: 28548727
version: 20
lastUpdated: 2026-07-01T05:43:39.489Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548727/Add+Member
---

[Grouper Web Services](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544233/Grouper+Web+Services)

#### Description

Add member will add or replace the membership of a group. This affects only direct memberships, not indirect memberships. If the user is already a member of the group it is still a success

#### Swagger / OpenAPI definition

You can see the swagger from your web service server: [https://server.institution.edu/grouper-ws/docs](https://server.institution.edu/grouper-ws/docs)

[Swagger Lite on demo server](https://grouperdemo.internet2.edu/swagger/v4/#/Grouper/addMemberLite) (note version in URL)

[Swagger non-Lite on demo server](https://grouperdemo.internet2.edu/swagger/v4/#/Grouper/addMember) (note version in URL)

#### Features

- Can assign membership to a "field" or "list", this is a custom type of membership to the group
- Lookup subjects by subject lookup (by id, source, identifier, etc). To add a group to another group, lookup the groupToAdd by putting the group uuid (e.g. fa2dd790-d3f9-4cf4-ac41-bb82e63bff66) in the subject id of the subject lookup. Optionally you can use g:gsa as the source id.
- Lookup groups by group lookup (by name or uuid)
- Returns group / subject information, can be detailed or not
- Can actAs another user
- You can specify addExternalSubjectIfNotFound to T or F, if this is a search by id or identifier, with no source, or the external source, and the subject is not found, then add an external subject (if the user is allowed to do this)

#### Add member Lite service

- Accepts one group and one member to add direct membership to group
- subjectId is required in REST before 2.6.17
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#addMemberLite-edu.internet2.middleware.grouper.misc.GrouperVersion-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-edu.internet2.middleware.grouper.Field-boolean-boolean-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.sql.Timestamp-java.sql.Timestamp-boolean-) (click on addMemberLite), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#addMemberLite-edu.internet2.middleware.grouper.misc.GrouperVersion-java.lang.String-java.lang.String-java.lang.String-edu.internet2.middleware.grouper.ws.rest.member.WsRestAddMemberLiteRequest-) (click on addMemberLite)
- For REST, the request can put data in query string (in URL or request body)
- REST request (colon is escaped to %3A): PUT /grouper-ws/servicesRest/v1_3_000/groups/aStem%3AaGroup/members/10021368
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/member/WsRestAddMemberLiteRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsAddMemberLiteResult.html)
- [Response codes](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsAddMemberLiteResult.WsAddMemberLiteResultCode.html)
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/addMember/) (all files with "Lite" in them, click on "download" to see file)

#### Add member service

- Accepts one group and many members to add direct membership to group
- Can either add multiple members to a group, or can replace all existing members
- Can operate in one transaction, or can let each membership add in its own separate unit
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#addMember-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.coresoap.WsGroupLookup-edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup:A-boolean-edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup-edu.internet2.middleware.grouper.Field-edu.internet2.middleware.grouper.hibernate.GrouperTransactionType-boolean-boolean-java.lang.String:A-edu.internet2.middleware.grouper.ws.coresoap.WsParam:A-java.sql.Timestamp-java.sql.Timestamp-boolean-) (click on addMember), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#addMember-edu.internet2.middleware.grouper.misc.GrouperVersion-java.lang.String-edu.internet2.middleware.grouper.ws.rest.member.WsRestAddMemberRequest-) (click on addMember)
- REST request (colon is escaped to %3A): PUT /grouper-ws/servicesRest/v1_3_000/groups/aStem%3AaGroup/members
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/member/WsRestAddMemberRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsAddMemberResults.html)
- [Response codes overall](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsAddMemberResults.WsAddMemberResultsCode.html), [response codes for each assignment](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsAddMemberResult.WsAddMemberResultCode.html)
- Returns an overall status, and a status for each assignment
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/addMember/) (all files without "Lite" in them, click on "download" to see files)
