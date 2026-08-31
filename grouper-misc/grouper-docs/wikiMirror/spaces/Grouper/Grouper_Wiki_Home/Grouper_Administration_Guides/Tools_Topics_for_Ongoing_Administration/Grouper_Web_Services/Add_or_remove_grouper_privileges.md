---
title: "Add or remove grouper privileges"
space: Grouper
pageId: 28547803
version: 10
lastUpdated: 2026-07-01T05:46:08.504Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547803/Add+or+remove+grouper+privileges
---

[Grouper Web Services](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544233/Grouper+Web+Services)

#### Description

"[Add or remove grouper privileges](https://bugs.internet2.edu/jira/browse/GRP-179)" will add or remove privileges for a subject and (group or stem). If you are adding a privilege and it already exists (immediate privilege), then it will not fail, and it will notify you in the response (still considered a success). If you are removing a privilege, and there was no immediate privileges to remove, it will be a success, and will notify you by response code. If you remove a privilege, and there is an "effective" privilege still there (which means the subject has the privilege, just not directly), it will be a success, and you will be notified via response code.

#### Features

- Will only edit the privileges the web service user (or actAs) is allowed to see
- Lookup subjects/members by subject lookup (by id, source, identifier, etc)
- Lookup groups/stems by group lookup or stem lookup (name or uuid)
- Returns subject information of the subject
- Returns the group or stem information
- Can actAs another user
- Failsafe, will not fail if adding a privilege that is already there, or removing one that is already gone
- Descriptive response codes give information about the existing privileges
- If replaceAllExisting is T, then allowed must be set to T. This will assign the provided privilege(s) to the provided subject(s), and remove it from all other subjects who are assigned. If F or blank, assign or remove (depending on value provided in 'allowed') the provided privilege(s) from the provided subject(s)

#### Assign grouper privileges Lite service

- Accepts one subject, one privilege type and name, if allowed, and one group or stem lookup
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#assignGrouperPrivilegesLite-edu.internet2.middleware.grouper.misc.GrouperVersion-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-edu.internet2.middleware.grouper.privs.PrivilegeType-edu.internet2.middleware.grouper.privs.Privilege-boolean-java.lang.String-java.lang.String-java.lang.String-boolean-java.lang.String-boolean-java.lang.String-java.lang.String-java.lang.String-java.lang.String-) (click on assignGrouperPrivilegesLite), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#assignGrouperPrivilegesLite-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.rest.group.WsRestAssignGrouperPrivilegesLiteRequest-) (click on assignGrouperPrivileges)
- For REST, a request body is required (probably via POST)
- REST request (colon is escaped to %3A): PUT (or POST) /grouper-ws/servicesRest/v1_4_000/grouperPrivileges
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/group/WsRestAssignGrouperPrivilegesLiteRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsAssignGrouperPrivilegesLiteResult.html)
- [Response codes](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsAssignGrouperPrivilegesLiteResult.WsAssignGrouperPrivilegesLiteResultCode.html)
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/assignGrouperPrivileges/) (all files with "Lite" in them, click on "download" to see file)

#### Assign grouper privileges service

- Accepts one subject, one privilege type and name, if allowed, and one group or stem lookup
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#assignGrouperPrivileges-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup:A-edu.internet2.middleware.grouper.ws.coresoap.WsGroupLookup-edu.internet2.middleware.grouper.ws.coresoap.WsStemLookup-edu.internet2.middleware.grouper.privs.PrivilegeType-edu.internet2.middleware.grouper.privs.Privilege:A-boolean-boolean-edu.internet2.middleware.grouper.hibernate.GrouperTransactionType-edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup-boolean-java.lang.String:A-boolean-edu.internet2.middleware.grouper.ws.coresoap.WsParam:A-) (click on assignGrouperPrivileges), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#assignGrouperPrivileges-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.rest.group.WsRestAssignGrouperPrivilegesRequest-) (click on assignGrouperPrivileges)
- For REST, a request body is required (probably via POST)
- REST request (colon is escaped to %3A): PUT (or POST) /grouper-ws/servicesRest/v1_4_000/grouperPrivileges
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/group/WsRestAssignGrouperPrivilegesRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsAssignGrouperPrivilegesResult.html)
- [Response codes](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsAssignGrouperPrivilegesResult.WsAssignGrouperPrivilegesResultCode.html)
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/assignGrouperPrivileges/) (all files with "Lite" in them, click on "download" to see file)
