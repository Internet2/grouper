---
title: "Attribute Definition Name Save"
space: Grouper
pageId: 28548312
version: 10
lastUpdated: 2026-07-01T05:44:51.567Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548312/Attribute+Definition+Name+Save
---

[Grouper Web Services](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544233/Grouper+Web+Services)

#### Description

Add or edit attribute definition names based on name or ID. This is new as of Grouper v2.1

#### Features

- Add or edit an attribute definition name
- If you are editing an existing attribute definition name, you can look it up by name or id
- You can specify a saveMode of INSERT, UPDATE, or INSERT_OR_UPDATE. If you specify INSERT and the attributeDefName exists, it will be an error. If you specify UPDATE and the attributeDefName doesnt exist, it will be an error.
- Non-lite service can save multiple attributeDefNames at once
- You can have this operation automatically create parent stems if they do not exist
- Can pass in a txType so that you can run all the saves in one transaction, or just finish the work that is possible with no enclosing transaction
- Can set the name, display name, and description of an attribute def name. Specify the attribute definition it is linked to (by name or id)
- Returns attribute definition name(s), and the result code of if it was inserted or updated
- Can actAs another user

#### attributeDefNameSave Lite service

- Accepts one attribute def name to save...
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#attributeDefNameSaveLite-edu.internet2.middleware.grouper.misc.GrouperVersion-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-edu.internet2.middleware.grouper.misc.SaveMode-java.lang.Boolean-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-) (click on attributeDefNameSaveLite), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#attributeDefNameSaveLite-edu.internet2.middleware.grouper.misc.GrouperVersion-java.lang.String-edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAttributeDefNameSaveLiteRequest-) (click on attributeDefNameSaveLite)
- For REST, the request can put data in query string (in URL or request body)
- REST request (colon is escaped to %3A): PUT /grouper-ws/servicesRest/v2_1_000/attributeDefNames
  
  - Note: if passing data in request body e.g. actAs, use a POST
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/attribute/WsRestAttributeDefNameSaveLiteRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsAttributeDefNameSaveResults.html)
- [Response codes](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsAttributeDefNameSaveResults.WsAttributeDefNameSaveResultsCode.html)
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/attributeDefNameSave/) (all files with "Lite" in them, click on "download" to see file)

#### attributeDefNameSave service

- Accepts multiple attributeDefNames to save
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#attributeDefNameSave-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameToSave:A-edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup-edu.internet2.middleware.grouper.hibernate.GrouperTransactionType-edu.internet2.middleware.grouper.ws.coresoap.WsParam:A-) (click on attributeDefNameSave), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#attributeDefNameSave-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAttributeDefNameSaveRequest-) (click on attributeDefNameSave)
- REST request (colon is escaped to %3A): POST /grouper-ws/servicesRest/v2_1_000/attributeDefNames
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/attribute/WsRestAttributeDefNameSaveRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsAttributeDefNameSaveResults.html)
- [Response codes](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsAttributeDefNameSaveResults.WsAttributeDefNameSaveResultsCode.html)
- Returns an overall status, and a status for each assignment
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/attributeDefNameSave/) (all files without "Lite" in them, click on "download" to see files)
