---
title: "Message Send"
space: Grouper
pageId: 28547578
version: 6
lastUpdated: 2026-07-01T05:46:32.568Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547578/Message+Send
---

[Grouper Web Services](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544233/Grouper+Web+Services)

#### Description

Send a message to a queue or topic. This service is available in version v2.3.0+. There is no lite version for this service.

#### Features

- Send one or multiple messages
- Can actAs another user
- For 2.4+ with all patches applied, you can set exchange type for rabbitMq by sending WsParam with name "exchangeType". The value can be one of DIRECT, TOPIC, HEADERS, or FANOUT

#### Acknowledge message service

- Accepts the message and queue or topic
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/GrouperServiceLogic.html#sendMessage-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouperClient.messaging.GrouperMessageQueueType-java.lang.String-java.lang.String-java.lang.String-java.lang.Boolean-edu.internet2.middleware.grouper.ws.coresoap.WsMessage:A-edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup-edu.internet2.middleware.grouper.ws.coresoap.WsParam:A-) (click on sendMessage), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#sendMessage-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.rest.messaging.WsRestSendMessageRequest-)
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/messaging/WsRestSendMessageRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsMessageResults.html)
- [Response codes](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsMessageResults.WsMessageResultsCode.html)
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/sendMessage) (all files with "Lite" in them)
