---
title: "Assign Attribute Def Actions"
space: Grouper
pageId: 28547961
version: 10
lastUpdated: 2026-07-01T05:45:40.227Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547961/Assign+Attribute+Def+Actions
---

[Grouper Web Services](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544233/Grouper+Web+Services)

#### Description

Assign attribute def actions associated with a Permission Definition (AttributeDef). This service is available in version v2.3.0+. There is no lite version for this service.

#### Features

- Can add/remove/replace actions
- Lookup attribute definition by attributeDef lookup (by id, system name, etc)
- Can actAs another user

#### Assign attribute def actions service

- Accepts one attribute definition and optionally one action to query action(s)
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#assignAttributeDefActions-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefLookup-java.lang.String:A-boolean-java.lang.Boolean-edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup-edu.internet2.middleware.grouper.ws.coresoap.WsParam:A-) (click on assignAttributeDefActions), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#assignAttributeDefActions-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAssignAttributeDefActionsRequest-)
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/attribute/WsRestAssignAttributeDefActionsRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsAttributeDefAssignActionResults.html)
- [Response codes](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsAttributeDefAssignActionResults.WsAttributeDefAssignActionsResultsCode.html)
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/assignAttributeDefActions) (all files with "Lite" in them)
