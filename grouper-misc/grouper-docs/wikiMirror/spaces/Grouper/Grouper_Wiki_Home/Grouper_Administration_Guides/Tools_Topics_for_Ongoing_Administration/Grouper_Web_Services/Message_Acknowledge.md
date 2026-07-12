---
title: "Message Acknowledge"
space: Grouper
pageId: 28547906
version: 9
lastUpdated: 2026-07-01T05:45:54.596Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547906/Message+Acknowledge
---

[Grouper Web Services](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544233/Grouper+Web+Services)

#### Description

Acknowledge a message was processed or not. This service is available in version v2.3.0+. There is no lite version for this service.

#### Swagger / OpenAPI definition

You can see the swagger from your web service server: [https://server.institution.edu/grouper-ws/docs](https://server.institution.edu/grouper-ws/docs)

[v4 swagger on demo server](https://grouperdemo.internet2.edu/swagger/v4/#/Grouper/acknowledgeMessage)

#### Features

- Acknowledge one or many messages
- Can say they are processed or other options, e.g. put on another queue (dead letter queue?)
- Lookup message by id
- Can actAs another user

#### Acknowledge message service

- Accepts message ids and how to acknowledge them
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#acknowledge-edu.internet2.middleware.grouper.misc.GrouperVersion-java.lang.String-java.lang.String-edu.internet2.middleware.grouperClient.messaging.GrouperMessageAcknowledgeType-java.lang.String:A-java.lang.String-edu.internet2.middleware.grouperClient.messaging.GrouperMessageQueueType-edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup-edu.internet2.middleware.grouper.ws.coresoap.WsParam:A-) (click on acknowledge), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#acknowledgeMessages-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.rest.messaging.WsRestAcknowledgeMessageRequest-)
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/messaging/WsRestAcknowledgeMessageRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsMessageAcknowledgeResults.html)
- [Response codes](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsMessageAcknowledgeResults.WsMessageAcknowledgeResultsCode.html)
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/acknowledgeMessage) (all files with "Lite" in them)
