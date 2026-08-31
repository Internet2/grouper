---
title: "Get Attribute Assign Actions"
space: Grouper
pageId: 28547609
version: 9
lastUpdated: 2026-07-01T05:46:31.554Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547609/Get+Attribute+Assign+Actions
---

[Grouper Web Services](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544233/Grouper+Web+Services)

#### Description

Get attribute assign actions will give you the permission actions associated with a Permission Definition (AttributeDef). This service is available in version v2.3.0+.

#### Features

- Can query by multiple attribute definitions
- Can query certain actions or get a list of all actions for the attribute definition
- Lookup attribute definitions by attributeDef lookup (by id, system name, etc)
- Returns actions for attribute definitions
- Can actAs another user

#### Get attribute assign actions lite service

- Accepts one attribute definition and optionally one action to query action(s)
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#getAttributeAssignActionsLite-edu.internet2.middleware.grouper.misc.GrouperVersion-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#getAttributeAssignActionsLite-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.rest.attribute.WsRestGetAttributeAssignActionsLiteRequest-)
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/attribute/WsRestGetAttributeAssignActionsLiteRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsGetAttributeAssignActionsResults.html)
- [Response codes](//software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsGetAttributeAssignActionsResults.WsGetAttributeAssignActionsResultsCode.html)
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/getAttributeAssignActions) (all files with "Lite" in them)

#### Get attribute assign actions service

- Accepts multiple attribute definitions and optionally multiple actions to query action(s)
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#getAttributeAssignActions-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefLookup:A-java.lang.String:A-edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup-edu.internet2.middleware.grouper.ws.coresoap.WsParam:A-), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#getAttributeAssignActions-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.rest.attribute.WsRestGetAttributeAssignActionsRequest-)
- REST request: GET /grouperWs/servicesRest/v2_3_000/attributeAssignActions
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/attribute/WsRestGetAttributeAssignActionsRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsGetAttributeAssignActionsResults.html)
- [Response codes](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsGetAttributeAssignActionsResults.WsGetAttributeAssignActionsResultsCode.html)
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/getAttributeAssignActions)  (all files with "Lite" in them)
