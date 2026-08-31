---
title: "Assign Attribute Definition Name Inheritance"
space: Grouper
pageId: 28548441
version: 10
lastUpdated: 2026-07-01T05:44:31.488Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548441/Assign+Attribute+Definition+Name+Inheritance
---

[Grouper Web Services](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544233/Grouper+Web+Services)

#### Description

Assign attribute definition name inheritance based on lookups by name or ID. This is new as of Grouper v2.1. Note: attribute definition name inheritance is only used for permissions (e.g. if the permission names are an org chart there would be inheritance)

#### Features

- Pass in one parent attribute def name lookup by name or id
- Pass in one or many child attribute def name lookup by name or id (note, only the non-lite service can pass more than one child lookup)
- Pass in whether this an assignment or a removal of an assignment
- Can replace the current child list with the inputted list (non-lite only)
- Can pass in a txType so that you can run all the changes in one transaction, or just finish the work that is possible with no enclosing transaction
- Returns if it was successful or not, and returns a string detailing how many inserts, deletes, and no-ops there were (if the inheritance or lack of already existed in that state)
- Can actAs another user

#### assignAttributeDefNameInheritance Lite service

- Accepts one attribute def name child to assign or unassign...
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#assignAttributeDefNameInheritanceLite-edu.internet2.middleware.grouper.misc.GrouperVersion-java.lang.String-java.lang.String-java.lang.String-java.lang.String-boolean-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-) (click on assignAttributeDefNameInheritanceLite), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#assignAttributeDefNameInheritanceLite-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAssignAttributeDefNameInheritanceLiteRequest-) (click on assignAttributeDefNameInheritanceLite)
- For REST, the request can put data in query string (in URL or request body)
- REST request (colon is escaped to %3A): PUT /grouper-ws/servicesRest/v2_1_000/attributeDefNames
  
  - Note: if passing data in request body e.g. actAs, use a POST
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/attribute/WsRestAssignAttributeDefNameInheritanceLiteRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsAssignAttributeDefNameInheritanceResults.html)
- [Response codes](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsAssignAttributeDefNameInheritanceResults.WsAssignAttributeDefNameInheritanceResultsCode.html)
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/assignAttributeDefNameInheritance/) (all files with "Lite" in them, click on "download" to see file)

#### assignAttributeDefNameInheritance service

- Accepts multiple attributeDefName children to assign or unassign
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#assignAttributeDefNameInheritance-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameLookup-edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameLookup:A-boolean-java.lang.Boolean-edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup-edu.internet2.middleware.grouper.hibernate.GrouperTransactionType-edu.internet2.middleware.grouper.ws.coresoap.WsParam:A-) (click on assignAttributeDefNameInheritance), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#assignAttributeDefNameInheritance-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.rest.attribute.WsRestAssignAttributeDefNameInheritanceRequest-) (click on assignAttributeDefNameInheritance)
- REST request (colon is escaped to %3A): POST /grouper-ws/servicesRest/v2_1_000/attributeDefNames
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/attribute/WsRestAssignAttributeDefNameInheritanceRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsAssignAttributeDefNameInheritanceResults.html)
- [Response codes](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsAssignAttributeDefNameInheritanceResults.WsAssignAttributeDefNameInheritanceResultsCode.html)
- Returns an overall status, and a status for each assignment
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/assignAttributeDefNameInheritance/) (all files without "Lite" in them, click on "download" to see files)
