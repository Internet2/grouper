---
title: "Get Permission Assignments"
space: Grouper
pageId: 28547822
version: 11
lastUpdated: 2026-07-01T05:11:39.428Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547822/Get+Permission+Assignments
---

[Grouper Web Services](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544233/Grouper+Web+Services)

#### Description

Get permission assignments. These permissions can be on roles or subjects (note if assignment is assigned directly to a subject, it is in the context of a role).

You can lookup permissions by attribute definition, attribute definition name, role name or uuid, or subject. You can filter by action. Note you must pass in at least an attribute definition, attribute definition name, role, or subject, and you can mix and match.

All returned permission assignments will be filtered for security based on the logged in or acted as user (security rules are on [attribute framework wiki](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544741/Grouper+attribute+framework))

The returned data will include the permission assignments, and a normalized list of references (role, attribute definitions, attribute names (if requested with includeAttributeDefNames=T), subjects, etc)

You can lookup assignments by multiple owners, definitions, subjects, actions, etc (non-lite operation only)

If you want to return details on the assignment (e.g. the depth of each hierarchy etc), pass in the param: includePermissionAssignDetail=T

If you want to return the underlying attribute assignment objects, pass in the param: includeAttributeAssignments=T

If there are limits or other metadata on the permission, to read those, pass in includeAttributeAssignments=T and includeAssignmentsOnAssignments=T. Note these attribute assignments on assignments are only on the immediate assignment, not effective.

#### Features

- Can base permission assign list based on action, active, etc
- Lookup owner or other objects by object lookup (by id, name, etc)
- Returns role / subject information etc, can be detailed or not
- Can actAs another user
- For 2.0+, you can pass in pointInTimeFrom and pointInTimeTo to check permissions at a certain point in time in the past, or in a date range. This should be formatted: yyyy/MM/dd HH:mm:ss.SSS

#### Get permission assignments lite service

- Accepts one role, or one subject, or attribute definition, or attribute definition name to get permission assignments for. You can mix and match, but at least one must be passed in (e.g. you can query for a subject's permissions in a role)
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#getPermissionAssignmentsLite-edu.internet2.middleware.grouper.misc.GrouperVersion-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-boolean-boolean-boolean-boolean-java.lang.String-java.lang.String-java.lang.String-boolean-java.lang.String-boolean-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.sql.Timestamp-java.sql.Timestamp-boolean-edu.internet2.middleware.grouper.permissions.PermissionEntry.PermissionType-edu.internet2.middleware.grouper.permissions.PermissionProcessor-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-boolean-) (click on getPermissionAssignmentsLite), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#getPermissionAssignmentsLite-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.rest.permission.WsRestGetPermissionAssignmentsLiteRequest-) (click on getPermissionAssignmentsLite)
- For REST, the request can put data in query string (in URL or request body)
- REST request (colon is escaped to %3A):
  
  - GET /grouper-ws/servicesRest/v1_6_000/permissionAssignments
  - Note: if passing data in request body e.g. actAs, use a POST
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/permission/WsRestGetPermissionAssignmentsLiteRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/soap/WsGetPermissionAssignmentsResults.html)
- [Response codes](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsGetPermissionAssignmentsResults.WsGetPermissionAssignmentsResultsCode.html)
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/getPermissionAssignments/) (all files with "Lite" in them, click on "download" to see file)

#### Get permission assignments service

- Accepts multiple roles or subjects or attribute definitions (or combination) etc to retrieve lists of permission assignments
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#getPermissionAssignments-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefLookup:A-edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameLookup:A-edu.internet2.middleware.grouper.ws.coresoap.WsGroupLookup:A-edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup:A-java.lang.String:A-boolean-boolean-boolean-boolean-edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup-boolean-java.lang.String:A-boolean-edu.internet2.middleware.grouper.ws.coresoap.WsParam:A-java.lang.String-java.sql.Timestamp-java.sql.Timestamp-boolean-edu.internet2.middleware.grouper.permissions.PermissionEntry.PermissionType-edu.internet2.middleware.grouper.permissions.PermissionProcessor-edu.internet2.middleware.grouper.ws.coresoap.WsPermissionEnvVar:A-boolean-) (click on getPermissionAssignments), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#getPermissionAssignments-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.rest.permission.WsRestGetPermissionAssignmentsRequest-) (click on getPermissionAssignments)
- REST request (colon is escaped to %3A):
  
  - POST /grouper-ws/servicesRest/v1_6_000/permissionAssignments
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/permission/WsRestGetPermissionAssignmentsRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/soap/WsGetPermissionAssignmentsResults.html)
- [Response codes overall](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsGetPermissionAssignmentsResults.WsGetPermissionAssignmentsResultsCode.html)
- Returns an overall status
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/getPermissionAssignments/) (all files without "Lite" in them, click on "download" to see files)
