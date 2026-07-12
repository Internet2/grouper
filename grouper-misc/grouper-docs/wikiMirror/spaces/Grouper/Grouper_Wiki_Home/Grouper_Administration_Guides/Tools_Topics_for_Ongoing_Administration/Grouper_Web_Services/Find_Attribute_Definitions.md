---
title: "Find Attribute Definitions"
space: Grouper
pageId: 28547513
version: 8
lastUpdated: 2026-07-01T05:46:36.517Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547513/Find+Attribute+Definitions
---

[Grouper Web Services](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544233/Grouper+Web+Services)

#### Description

Find attribute definitions based on name or ID or other criteria. This is new as of Grouper v2.3.0

#### Features

- Find attribute definitions based on list of names, ids, or indexes)
- Search by wildcard string (search filter)
- Search by parent folder (only in that folder or also subfolders)
- Lookup the attribute definition be name, id, or index
- Non-lite service can search for multiple attributeDefs at once by name/id/index
- Returns attribute definition(s)
- Can page and sort the results
- Can actAs another user

#### findAttributeDef Lite service

- Accepts individual arguments, can only lookup one attributeDef by name/id/index or can search for multiple...
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#findAttributeDefsLite-edu.internet2.middleware.grouper.misc.GrouperVersion-java.lang.String-java.lang.Boolean-java.lang.String-java.lang.String-java.lang.String-java.lang.String-edu.internet2.middleware.grouper.ws.query.StemScope-java.lang.String-java.lang.Boolean-java.lang.Integer-java.lang.Integer-java.lang.String-java.lang.Boolean-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.Boolean-java.lang.String-java.lang.String-java.lang.Boolean-) (click on findAttributeDefsLite), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#findAttributeDefsLite-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.rest.attribute.WsRestFindAttributeDefsLiteRequest-)
- For REST, the request can put data in query string (in URL or request body)
- REST request (colon is escaped to %3A): GET /grouper-ws/servicesRest/v2_3_000/attributeDefs
  
  - Note: if passing data in request body e.g. actAs, use a POST
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/attribute/WsRestFindAttributeDefsLiteRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsFindAttributeDefsResults.html)
- [Response codes](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsFindAttributeDefsResults.WsFindAttributeDefsResultsCode.html)
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/findAttributeDefs) (all files with "Lite" in them, click on "download" to see file)

#### findAttributeDef service

- Accepts multiple arguments, can lookup multiple attributeDefs by name/id/index or can search for multiple
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#findAttributeDefs-edu.internet2.middleware.grouper.misc.GrouperVersion-java.lang.String-java.lang.Boolean-edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefLookup:A-java.lang.String-edu.internet2.middleware.grouper.ws.query.StemScope-java.lang.String-java.lang.Boolean-java.lang.Integer-java.lang.Integer-java.lang.String-java.lang.Boolean-edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup-edu.internet2.middleware.grouper.ws.coresoap.WsParam:A-java.lang.Boolean-java.lang.String-java.lang.String-java.lang.Boolean-) (click on findAttributeDefs), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#findAttributeDefs-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.rest.attribute.WsRestFindAttributeDefsRequest-)
- REST request (colon is escaped to %3A): GET /grouper-ws/servicesRest/v2_3_000/attributeDefs
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/attribute/WsRestFindAttributeDefsRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsFindAttributeDefsResults.WsFindAttributeDefsResultsCode.html)
- [Response codes](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsAttributeDefSaveResult.WsAttributeDefSaveResultCode.html)
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/attributeDefSave) (all files with "Lite" in them, click on "download" to see file)
