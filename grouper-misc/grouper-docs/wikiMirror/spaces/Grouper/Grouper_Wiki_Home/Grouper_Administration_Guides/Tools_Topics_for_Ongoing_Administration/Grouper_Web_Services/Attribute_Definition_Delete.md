---
title: "Attribute Definition Delete"
space: Grouper
pageId: 28548722
version: 7
lastUpdated: 2026-07-01T05:43:40.465Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548722/Attribute+Definition+Delete
---

[Grouper Web Services](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544233/Grouper+Web+Services)

#### Description

Delete attribute definitions based on name or ID. This is new as of Grouper v2.3.0

#### Features

- Delete an attribute definition
- Lookup the attribute definition be name, id, or index
- Non-lite service can delete multiple attributeDef at once
- Can pass in a txType so that you can run all the deletes in one transaction, or just finish the work that is possible with no enclosing transaction
- Returns attribute definition(s), and the result code of if it was deleted
- Can actAs another user

#### attributeDefDelete Lite service

- Accepts one attribute def to delete...
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#attributeDefDeleteLite-edu.internet2.middleware.grouper.misc.GrouperVersion-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-) (click on attributeDefDeleteLite), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#attributeDefDeleteLite-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAttributeDefDeleteLiteRequest-)
- For REST, the request can put data in query string (in URL or request body)
- REST request (colon is escaped to %3A): DELETE /grouper-ws/servicesRest/v2_3_000/attributeDefs
  
  - Note: if passing data in request body e.g. actAs, use a POST
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/attribute/WsRestAttributeDefDeleteLiteRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsAttributeDefDeleteLiteResult.html)
- [Response codes](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsAttributeDefDeleteLiteResult.WsAttributeDefDeleteLiteResultCode.html)
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/attributeDefDelete) (all files with "Lite" in them, click on "download" to see file)

#### attributeDefDelete service

- Accepts multiple attributeDefs to delete
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#attributeDefDelete-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefLookup:A-edu.internet2.middleware.grouper.hibernate.GrouperTransactionType-edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup-edu.internet2.middleware.grouper.ws.coresoap.WsParam:A-) (click on attributeDefDelete), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#attributeDefNameDelete-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAttributeDefNameDeleteRequest-)
- REST request (colon is escaped to %3A): DELETE /grouper-ws/servicesRest/v2_3_000/attributeDefs
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/attribute/WsRestAttributeDefDeleteRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsAttributeDefDeleteResult.html)
- [Response codes](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsAttributeDefDeleteResult.WsAttributeDefDeleteResultCode.html)
- Returns an overall status, and a status for each delete
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/attributeDefSave) (all files with "Lite" in them, click on "download" to see file)
