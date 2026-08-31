---
title: "Find Attribute Definition Names"
space: Grouper
pageId: 28548599
version: 11
lastUpdated: 2026-07-01T05:44:04.231Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548599/Find+Attribute+Definition+Names
---

[Grouper Web Services](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544233/Grouper+Web+Services)

#### Description

Find attribute definition names based on name, search filter, permission name inheritance, etc. This is new as of Grouper v2.1

#### Features

- Search by a list of names or ids
- Search by a search filter
- Search filter can "split scope" like a combobox, where the term is split by whitespace and appended to wildcards in an AND clause. e.g. "pto permissions" would look for attribute definition names which have the word "pto" and "permissions" somewhere in them
- Case insensitive
- Can search by permission inheritance based on an attribute def name (from permission) and if you want the other attribute def names which inherit directly, directly and indirectly, or which imply this attribute def name (directly, or both)
- Can filter by the types of objects this attribute def name is assignable to, or which type it is
- Can search by the attribute definition (to find all its names)
- Can sort and/or page the results of individual filter types: parent stem, and approximate group name. Can sort on name, displayName, extension, or displayExtension.
- Returns attribute definition names
- Can actAs another user
- Search by serviceRole for a user in v2.2+. Send a subject and a serviceRole (user or admin) to get the services that the subject is a user or admin in. The calling subject must be able to READ groups that subject is in the service. The subject does not need privileges on the service attribute.

#### Find attributeDefNames Lite service

- Accepts one query to search... can query by one name or id, or can filter
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#findAttributeDefNamesLite-edu.internet2.middleware.grouper.misc.GrouperVersion-java.lang.String-java.lang.Boolean-java.lang.String-java.lang.String-edu.internet2.middleware.grouper.attr.assign.AttributeAssignType-edu.internet2.middleware.grouper.attr.AttributeDefType-java.lang.String-java.lang.String-java.lang.Integer-java.lang.Integer-java.lang.String-java.lang.Boolean-edu.internet2.middleware.grouper.ws.rest.attribute.WsInheritanceSetRelation-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-edu.internet2.middleware.grouper.service.ServiceRole-java.lang.Boolean-java.lang.String-java.lang.String-java.lang.Boolean-) (click on findAttributeDefNamesLite), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#findAttributeDefNamesLite-edu.internet2.middleware.grouper.misc.GrouperVersion-java.lang.String-edu.internet2.middleware.grouper.ws.rest.attribute.WsRestFindAttributeDefNamesLiteRequest-) (click on findAttributeDefNamesLite)
- For REST, the request can put data in query string (in URL or request body)
- REST request (colon is escaped to %3A): GET /grouper-ws/servicesRest/v2_1_000/attributeDefNames
  
  - Note: if passing data in request body e.g. actAs, use a POST
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/attribute/WsRestFindAttributeDefNamesLiteRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsFindAttributeDefNamesResults.html)
- [Response codes](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsFindAttributeDefNamesResults.WsFindAttributeDefNamesResultsCode.html)
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/findAttributeDefNames/) (all files with "Lite" in them, click on "download" to see file)

#### Find attributeDefNames service

- Accepts multiple query objects for names and/or ids
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#findAttributeDefNames-edu.internet2.middleware.grouper.misc.GrouperVersion-java.lang.String-java.lang.Boolean-edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefLookup-edu.internet2.middleware.grouper.attr.assign.AttributeAssignType-edu.internet2.middleware.grouper.attr.AttributeDefType-edu.internet2.middleware.grouper.ws.coresoap.WsAttributeDefNameLookup:A-java.lang.Integer-java.lang.Integer-java.lang.String-java.lang.Boolean-edu.internet2.middleware.grouper.ws.rest.attribute.WsInheritanceSetRelation-edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup-edu.internet2.middleware.grouper.ws.coresoap.WsParam:A-edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup-edu.internet2.middleware.grouper.service.ServiceRole-java.lang.Boolean-java.lang.String-java.lang.String-java.lang.Boolean-) (click on findAttributeDefNames), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#findAttributeDefNames-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.rest.attribute.WsRestFindAttributeDefNamesRequest-) (click on findAttributeDefNames)
- REST request (colon is escaped to %3A): POST /grouper-ws/servicesRest/v2_1_000/attributeDefNames
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/attribute/WsRestFindAttributeDefNamesRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsFindAttributeDefNamesResults.html)
- [Response codes](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsFindAttributeDefNamesResults.WsFindAttributeDefNamesResultsCode.html)
- Returns an overall status, and a status for each assignment
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/findAttributeDefNames/) (all files without "Lite" in them, click on "download" to see files)
