---
title: "Attribute Definition Name Delete"
space: Grouper
pageId: 28547901
version: 8
lastUpdated: 2026-07-01T05:45:55.609Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547901/Attribute+Definition+Name+Delete
---

[Grouper Web Services](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544233/Grouper+Web+Services)

#### Description

Delete attribute definition names based on name or ID. This is new as of Grouper v2.1

#### Features

- Delete attribute definition name based on name or ID
- Non-lite service can delete multiple attributeDefNames at once
- Can pass in a txType so that you can run all the deletes in one transaction, or just finish the work that is possible with no enclosing transaction
- Returns attribute definition name(s), and the result code of if it was deleted or not (still a success if it didnt exist, though if the folder didnt exist, that is bad)
- Can actAs another user

#### attributeDefNameDelete Lite service

- Accepts one attribute def name to delete...
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#attributeDefNameDeleteLite-edu.internet2.middleware.grouper.misc.GrouperVersion-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-) (click on attributeDefNameDeleteLite), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#attributeDefNameDeleteLite-edu.internet2.middleware.grouper.misc.GrouperVersion-java.lang.String-edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAttributeDefNameDeleteLiteRequest-) (click on attributeDefNameDeleteLite)
- For REST, the request can put data in query string (in URL or request body)
- REST request (colon is escaped to %3A): DELETE /grouper-ws/servicesRest/v2_1_000/attributeDefNames
  
  - Note: if passing data in request body e.g. actAs, use a POST
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/attribute/WsRestAttributeDefNameDeleteLiteRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsAttributeDefNameDeleteResults.html)
- [Response codes](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsAttributeDefNameDeleteResults.WsAttributeDefNameDeleteResultsCode.html)
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/attributeDefNameDelete/) (all files with "Lite" in them, click on "download" to see file)

#### attributeDefNameDelete service

- Accepts multiple attributeDefNames to delete
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#attributeDefNameDelete-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameLookup:A-edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup-edu.internet2.middleware.grouper.hibernate.GrouperTransactionType-edu.internet2.middleware.grouper.ws.coresoap.WsParam:A-) (click on attributeDefNameDelete), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#attributeDefNameDelete-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAttributeDefNameDeleteRequest-) (click on attributeDefNameDelete)
- REST request (colon is escaped to %3A): POST /grouper-ws/servicesRest/v2_1_000/attributeDefNames
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/attribute/WsRestAttributeDefNameDeleteRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsAttributeDefNameDeleteResults.html)
- [Response codes](//software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsAttributeDefNameDeleteResults.WsAttributeDefNameDeleteResultsCode.html)
- Returns an overall status, and a status for each assignment
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/attributeDefNameDelete/) (all files without "Lite" in them, click on "download" to see files)
