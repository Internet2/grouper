---
title: "Message Receive"
space: Grouper
pageId: 28548106
version: 10
lastUpdated: 2026-11-20T21:03:29.141Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548106/Message+Receive
---

[Grouper Web Services](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544233/Grouper+Web+Services)

#### Description

Receive a message from a queue or topic. This service is available in version v2.3.0+. There is no lite version for this service.

#### Features

- Receive messages
- Wait a small amount of time (e.g. 20 seconds) until message is delivered (e.g. SQS long polling)
- Can actAs another user
- External messaging systems (RabbitMQ, AWS SQS, ActiveMQ): as of v7.9.1, v6.5.1 and v4.26.1, a caller who is not wheel or root can use one only if they are a member of the group in grouper.properties grouper.messaging.system.<messagingSystemName>.allowedGroupName; if that is not set, only wheel or root can. Only wheel or root can autocreate queues or topics. Built-in messaging keeps its own per-queue/topic permissions.
- For 2.4+ with all patches applied, you can set exchange type for rabbitMq by sending WsParam with name "exchangeType". The value can be one of DIRECT, TOPIC, HEADERS, or FANOUT

#### Acknowledge message service

- Accepts queue or topic
- Documentation: [SOAP](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#receiveMessage-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.rest.messaging.WsRestReceiveMessageRequest-) (click on receiveMessage), [REST](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/GrouperServiceRest.html#receiveMessage-edu.internet2.middleware.grouper.misc.GrouperVersion-edu.internet2.middleware.grouper.ws.rest.messaging.WsRestReceiveMessageRequest-)
- (see documentation above for details): [Request object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/rest/messaging/WsRestReceiveMessageRequest.html), [response object](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsMessageResults.html)
- [Response codes](https://software.internet2.edu/grouper/doc/master/grouper-ws-parent/grouper-ws/apidocs/edu/internet2/middleware/grouper/ws/coresoap/WsMessageResults.WsMessageResultsCode.html)
- [Samples](https://github.com/Internet2/grouper/tree/master/grouper-ws/grouper-ws/doc/samples/receiveMessage) (all files with "Lite" in them)
