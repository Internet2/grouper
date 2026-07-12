---
title: "Attribute Definition Save"
space: Grouper
pageId: 28547682
version: 8
lastUpdated: 2026-07-01T05:46:22.415Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547682/Attribute+Definition+Save
---

[Grouper Web Services](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544233/Grouper+Web+Services)

#### Description

Add or edit attribute definitions based on name or ID. This is new as of Grouper v2.3.0

#### Features

- Add or edit an attribute definition
- If you are editing an existing attribute definition, you can look it up by name or id
- You can specify a saveMode of INSERT, UPDATE, or INSERT_OR_UPDATE. If you specify INSERT and the attributeDef exists, it will be an error. If you specify UPDATE and the attributeDef doesnt exist, it will be an error.
- Non-lite service can save multiple attributeDef at once
- You can have this operation automatically create parent stems if they do not exist
- Can pass in a txType so that you can run all the saves in one transaction, or just finish the work that is possible with no enclosing transaction
- Can set the name, and description of an attribute def
- Returns attribute definition(s), and the result code of if it was inserted or updated
- Can actAs another user

#### attributeDefSave Lite service

- Accepts one attribute def to save...
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#attributeDefSaveLite-edu.internet2.middleware.grouper.misc.GrouperVersion-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.Boolean-java.lang.Boolean-java.lang.Boolean-java.lang.Boolean-java.lang.Boolean-java.lang.Boolean-java.lang.Boolean-java.lang.Boolean-java.lang.Boolean-java.lang.Boolean-java.lang.Boolean-java.lang.Boolean-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-edu.internet2.middleware.grouper.misc.SaveMode-java.lang.Boolean-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-) (click on attributeDefSaveLite), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#attributeDefSaveLite-edu.internet2.middleware.grouper.misc.GrouperVersion-java.lang.String-edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAttributeDefSaveLiteRequest-)
- For REST, the request can put data in query string (in URL or request body)
- REST request (colon is escaped to %3A): PUT /grouper-ws/servicesRest/v2_3_000/attributeDefs
  
  - Note: if passing data in request body e.g. actAs, use a POST
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/attribute/WsRestAttributeDefSaveLiteRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsAttributeDefSaveLiteResult.html)
- [Response codes](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsAttributeDefSaveLiteResult.WsAttributeDefSaveLiteResultCode.html)
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/attributeDefSave) (all files with "Lite" in them, click on "download" to see file)

#### attributeDefSave service

- Accepts multiple attributeDefs to save
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#attributeDefSave-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefToSave:A-edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup-edu.internet2.middleware.grouper.hibernate.GrouperTransactionType-edu.internet2.middleware.grouper.ws.coresoap.WsParam:A-) (click on attributeDefSave), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#attributeDefSave-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAttributeDefSaveRequest-)
- REST request (colon is escaped to %3A): POST /grouper-ws/servicesRest/v2_3_000/attributeDefs
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/attribute/WsRestAttributeDefSaveRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsAttributeDefSaveResult.html)
- [Response codes](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsAttributeDefSaveResult.WsAttributeDefSaveResultCode.html)
- Returns an overall status, and a status for each assignment
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/attributeDefSave) (all files with "Lite" in them, click on "download" to see file)
