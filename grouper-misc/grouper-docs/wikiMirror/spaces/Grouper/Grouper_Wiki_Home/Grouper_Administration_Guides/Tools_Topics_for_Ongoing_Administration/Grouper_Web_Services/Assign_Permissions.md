---
title: "Assign Permissions"
space: Grouper
pageId: 28548564
version: 10
lastUpdated: 2026-07-01T05:44:10.270Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548564/Assign+Permissions
---

[Grouper Web Services](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544233/Grouper+Web+Services)

#### Description

Assign or remove permissions. These permissions can be on roles or subjects (in the context of a role).

You can lookup permissions to assign by attribute definition name, or attribute definition id

All assignments will be filtered for security based on the logged in or acted as user (security rules (on groups or any memberships) are on [attribute framework wiki](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544741/Grouper+attribute+framework)). Generally you need ATTR_UPDATE on the attributeDef of the permission, and UPDATE on the Role (group).

The returned data will include the attribute assignments and a normalized list of references (owner objects e.g. group/etc, attribute definitions, attribute names, etc), if things changed or were already assigned, etc

You can assign multiple permissions to multiple owners, actions, etc (non-lite)

permissionType is a required field (from enum PermissionType), must be: role or role_subject (for permissions assigned to a subject in the context of a role)

permissionAssignOperation is required and is the operation to perform for attribute on owners, from enum PermissionAssignOperation: assign_permission, remove_permission, replace_permissions. In this case, assigning a permission will not assign if already there (but you can edit its metadata e.g. .

#### Features

- Can pass owners, actions, etc. If multiples are passed, then each permission def name (attributeDefName) will be assigned for each action on each owner.
- Lookup owner or other objects by object lookup (by id, name, etc)
- Returns role (group) / subject information, can be detailed or not
- Can actAs another user

#### Assign permissions lite service

- Accepts one role, or one subject/role pair, one action, one permission def name to assign
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#assignPermissionsLite-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.permissions.PermissionEntry.PermissionType-java.lang.String-java.lang.String-edu.internet2.middleware.grouper.permissions.PermissionAssignOperation-java.lang.String-java.sql.Timestamp-java.sql.Timestamp-edu.internet2.middleware.grouper.attr.assign.AttributeAssignDelegatable-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-boolean-java.lang.String-boolean-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.Boolean-) (click on assignPermissionsLite), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#assignPermissionsLite-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.rest.permission.WsRestAssignPermissionsLiteRequest-) (click on assignPermissionsLite)
- For REST, the request can put data in query string (in URL or request body)
- REST request (colon is escaped to %3A):
  
  - PUT /grouper-ws/servicesRest/v1_6_000/assignPermissions
  - Note: if passing data in request body e.g. actAs, use a POST
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/permission/WsRestAssignPermissionsLiteRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsAssignPermissionsLiteResults.html)
- [Response codes](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsAssignAttributesResults.WsAssignAttributesResultsCode.html)
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/assignPermissions/) (all files with "Lite" in them, click on "download" to see file)

#### Assign permission assignments service

- Accepts multiple roles or subject/role pairs, permission definitions, actions, etc to assign
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#assignPermissions-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.permissions.PermissionEntry.PermissionType-edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameLookup:A-edu.internet2.middleware.grouper.permissions.PermissionAssignOperation-java.lang.String-java.sql.Timestamp-java.sql.Timestamp-edu.internet2.middleware.grouper.attr.assign.AttributeAssignDelegatable-edu.internet2.middleware.grouper.ws.coresoap.WsAttributeAssignLookup:A-edu.internet2.middleware.grouper.ws.coresoap.WsGroupLookup:A-edu.internet2.middleware.grouper.ws.coresoap.WsMembershipAnyLookup:A-java.lang.String:A-edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup-boolean-java.lang.String:A-boolean-edu.internet2.middleware.grouper.ws.coresoap.WsParam:A-edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefLookup:A-java.lang.String:A-java.lang.Boolean-) (click on assignPermissions), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#assignPermissions-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.rest.permission.WsRestAssignPermissionsRequest-) (click on assignPermissions)
- REST request (colon is escaped to %3A):
  
  - POST /grouper-ws/servicesRest/v1_6_000/assignPermissions
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/permission/WsRestAssignPermissionsRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsAssignPermissionsResults.html)
- [Response codes overall](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsAssignAttributesResults.WsAssignAttributesResultsCode.html)
- Returns an overall status
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/assignPermissions/) (all files without "Lite" in them, click on "download" to see files)
