---
title: "Delete Member"
space: Grouper
pageId: 28548758
version: 11
lastUpdated: 2026-07-01T05:43:34.100Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548758/Delete+Member
---

[Grouper Web Services](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544233/Grouper+Web+Services)

#### Description

Delete member will delete or replace the membership of a group. This affects only direct memberships, not indirect memberships. If the user is in an indirect membership, this is still a success

#### Features

- Can unassign membership to a "field" or "list", this is a custom type of membership to the group
- Lookup subjects by subject lookup (by id, source, identifier, etc)
- Lookup groups by group lookup (by name or uuid)
- Returns group / subject information, can be detailed or not
- Can actAs another user

#### Delete member Lite service

- Accepts one group and one member to delete direct membership to group
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#deleteMemberLite-edu.internet2.middleware.grouper.misc.GrouperVersion-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-edu.internet2.middleware.grouper.Field-boolean-boolean-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-) (click on deleteMemberLite), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#deleteMemberLite-edu.internet2.middleware.grouper.misc.GrouperVersion-java.lang.String-java.lang.String-java.lang.String-edu.internet2.middleware.grouper.ws.rest.member.WsRestDeleteMemberLiteRequest-) (click on deleteMemberLite)
- For REST, the request can put data in query string (in URL or request body)
- REST request (colon is escaped to %3A): DELETE /grouper-ws/servicesRest/v1_3_000/groups/aStem%3AaGroup/members/10021368
  
  - Note: if passing data in request body e.g. actAs, use a POST
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/member/WsRestDeleteMemberLiteRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsDeleteMemberLiteResult.html)
- [Response codes](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsDeleteMemberLiteResult.WsDeleteMemberLiteResultCode.html)
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/deleteMember/) (all files with "Lite" in them, click on "download" to see file)

#### Delete member service

- Accepts one group and many members to delete direct membership to group
- Can either delete multiple members to a group, or can replace all existing members
- Can operate in one transaction, or can let each membership delete in its own separate unit
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#deleteMember-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.coresoap.WsGroupLookup-edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup:A-edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup-edu.internet2.middleware.grouper.Field-edu.internet2.middleware.grouper.hibernate.GrouperTransactionType-boolean-boolean-java.lang.String:A-edu.internet2.middleware.grouper.ws.coresoap.WsParam:A-) (click on deleteMember), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#deleteMember-edu.internet2.middleware.grouper.misc.GrouperVersion-java.lang.String-edu.internet2.middleware.grouper.ws.rest.member.WsRestDeleteMemberRequest-) (click on deleteMember)
- REST request (colon is escaped to %3A): POST /grouper-ws/servicesRest/v1_3_000/groups/aStem%3AaGroup/members
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/member/WsRestDeleteMemberRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsDeleteMemberResults.html)
- [Response codes overall](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsDeleteMemberResults.WsDeleteMemberResultsCode.html), [response codes for each assignment](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsDeleteMemberResult.WsDeleteMemberResultCode.html)
- Returns an overall status, and a status for each assignment
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/deleteMember/) (all files without "Lite" in them, click on "download" to see files)
