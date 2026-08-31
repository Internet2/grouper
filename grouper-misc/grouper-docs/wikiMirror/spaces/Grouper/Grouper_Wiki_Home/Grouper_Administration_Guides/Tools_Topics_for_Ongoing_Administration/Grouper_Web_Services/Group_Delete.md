---
title: "Group Delete"
space: Grouper
pageId: 28548131
version: 9
lastUpdated: 2026-07-01T05:45:14.620Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548131/Group+Delete
---

[Grouper Web Services](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544233/Grouper+Web+Services)

#### Description

Group delete will insert or update a group's uuid, extension, display name, or description (with restrictions)

#### Features

- If group does not exist, the call will not fail (special result code)
- Lookup group to delete by group lookup (by name or uuid)
- Returns group, can be detailed or not
- Can actAs another user

#### Group delete Lite service

- Accepts one group to delete
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#groupDeleteLite-edu.internet2.middleware.grouper.misc.GrouperVersion-java.lang.String-java.lang.String-java.lang.String-java.lang.String-java.lang.String-boolean-java.lang.String-java.lang.String-java.lang.String-java.lang.String-) (click on groupDeleteLite), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#groupDeleteLite-edu.internet2.middleware.grouper.misc.GrouperVersion-java.lang.String-edu.internet2.middleware.grouper.ws.rest.group.WsRestGroupDeleteLiteRequest-) (click on groupDeleteLite)
- For REST, the request can put data in query string (in URL or request body)
- REST request (colon is escaped to %3A): DELETE /grouper-ws/servicesRest/v1_3_000/groups/aStem%3AaGroup
  
  - Note: if passing data in request body e.g. actAs, use a POST
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/group/WsRestGroupDeleteLiteRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsGroupDeleteLiteResult.html)
- [Response codes](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsGroupDeleteLiteResult.WsGroupDeleteLiteResultCode.html)
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/groupDelete/) (all files with "Lite" in them, click on "download" to see file)

#### Group delete service

- Accepts multiple groups to delete
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#groupDelete-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.coresoap.WsGroupLookup:A-edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup-edu.internet2.middleware.grouper.hibernate.GrouperTransactionType-boolean-edu.internet2.middleware.grouper.ws.coresoap.WsParam:A-) (click on groupDelete), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#groupDelete-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.rest.group.WsRestGroupDeleteRequest-) (click on groupDelete)
- REST request (colon is escaped to %3A): POST /grouper-ws/servicesRest/v1_3_000/groups
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/group/WsRestGroupDeleteRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsGroupDeleteResults.html)
- [Response codes overall](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsGroupDeleteResults.WsGroupDeleteResultsCode.html), [response codes for each assignment](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsGroupDeleteResult.WsGroupDeleteResultCode.html)
- Returns an overall status, and a status for each assignment
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/groupDelete/) (all files without "Lite" in them, click on "download" to see files)
