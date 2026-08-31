---
title: "Grouper messaging system"
space: Grouper
pageId: 28544824
version: 40
lastUpdated: 2026-07-01T05:47:59.755Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544824/Grouper+messaging+system
---

> The recommended approach for messaging in Grouper v2.6+ is:
> 
> 
> 
> 1. Use a provisioner instead of messaging so that full and incremental syncs can occur and all the [provisioning framework](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544760/Grouper+provisioning+framework) features can be used
> 2. If you still want to use messaging, consider using a messaging provisioner (such as Amazon AWS SNS/SQS, ActiveMQ, RabbitMQ) instead of the messaging change log. There is more granular control of which objects are eligible for the messaging, and information is kept/displayed about when messages are sent
> 3. If you still want to use the messaging change log consumer, it is still supported

## Overview

The Grouper messaging system is a Java implementation of the `GrouperMessagingSystem` interface. It allows messages to be sent to and received from a messaging system.

The built-in implementation is:

- [Grouper database (default)](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547740/Grouper+messaging+built+in)

Newer options (v2.5+) that leverage external messaging systems are:

- [Amazon AWS SNS/SQS](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548136/Grouper+Messaging+with+AWS+SQS)
- [ActiveMQ](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547408/Grouper+Messaging+with+ActiveMQ)
- [RabbitMQ](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548652/Grouper+Messaging+with+RabbitMQ)

Messages must:

- support 100kB in size
- support ordered messaging (unless ordered messaging consumers are not used)
- support bulk methods, but the implementation can process them one at a time (if there is an error, block until all are successful)

## Subpages

## ESB change log consumer configuration

Configure an ESB change log consumer in `grouper-loader.properties`:

```
#####################################
## Messaging integration with ESB, send change log entries to a messaging system
#####################################

# note, change "messagingEsb" in key to be the name of the consumer.  e.g. changeLog.consumer.myAzureConsumer.class
# note, routingKey property is valid only for rabbitmq. For other messaging systems, it is ignored.
#changeLog.consumer.messagingEsb.class = edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer

# quartz cron
#changeLog.consumer.messagingEsb.quartzCron = 0 * * * * ?

# el filter
#changeLog.consumer.messagingEsb.elfilter = event.eventType eq 'GROUP_DELETE' || event.eventType eq 'GROUP_ADD' || event.eventType eq 'MEMBERSHIP_DELETE' || event.eventType eq 'MEMBERSHIP_ADD'

# publishing class
#changeLog.consumer.messagingEsb.publisher.class = edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbMessagingPublisher

# messaging system name
#changeLog.consumer.messagingEsb.publisher.messagingSystemName = grouperBuiltinMessaging

# routing key
#changeLog.consumer.messagingEsb.publisher.routingKey =

# EL replacement definition. groupName is the variable for the name of the group. grouperUtil is the class GrouperUtil can be used for utility methods.
#changeLog.consumer.messagingEsb.regexRoutingKeyReplacementDefinition = ${groupName.replaceFirst('hawaii.edu', 'group.modify').replace(':enrolled', '').replace(':waitlisted', '').replace(':withdrawn', '')}

# replace routing key with periods
#changeLog.consumer.messagingEsb.replaceRoutingKeyColonsWithPeriods = true

# queue or topic
#changeLog.consumer.messagingEsb.publisher.messageQueueType = queue

# queue or topic name
#changeLog.consumer.messagingEsb.publisher.queueOrTopicName = abc

# exchange type for rabbitmq. valid options are DIRECT, TOPIC, HEADERS, FANOUT
#changeLog.consumer.messagingEsb.publisher.exchangeType =

# key for optional extra arguments for rabbitmq. For each key, set up a corresponding value having the same index
#changeLog.consumer.messagingEsb.publisher.queueArgs.0.key = x-queue-type

# value for optional extra arguments for rabbitmq. Each index should have a corresponding key
#changeLog.consumer.messagingEsb.publisher.queueArgs.0.value = quorum

# if you want to bump up the number of change log entries for a particular consumer, you can enter that here, per change log consumer
# defaults to grouper-loader.properties changeLog.changeLogConsumerBatchSize which defaults to 1000
#changeLog.consumer.messagingEsb.changeLogConsumerBatchSize =

```

## Java interfaces

The `GrouperMessagingSystem` interface is located in the GrouperClient (package `edu.internet2.middleware.grouperClient.messaging`):

```java
package edu.internet2.middleware.grouperClient.messaging;

/**
 * Represents the methods that a messaging system
 * needs to support
 */
public interface GrouperMessagingSystem {

  /**
   * send a message to a queue name.  Note, the recipient could be a
   * queue or a topic (generally always one or the other) based on the
   * implementation of the messaging system.  Messages must be delievered
   * in the order that collection iterator designates.  If there is a problem
   * delivering the messages, the implementation should log, wait (back off)
   * and retry until it is successful.
   * @param grouperMessageSendParam has the queue or topic, and the message(s) and perhaps args
   * @return result
   */
  public GrouperMessageSendResult send(GrouperMessageSendParam grouperMessageSendParam);

  /**
   * this will generally block until there are messages to process.  These messages
   * are ordered in the order that they were sent.
   * @param grouperMessageReceiveParam grouper messaging receive param
   * @return a message or multiple messages.  It will block until there are messages
   * available for this recipient to process
   */
  public GrouperMessageReceiveResult receive(GrouperMessageReceiveParam grouperMessageReceiveParam);

  /**
   * tell the messaging system that these messages are processed
   * generally the message system will use the message id.  Note, the objects
   * sent to this method must be the same that were received in the
   * receiveMessages method.  If there is a problem
   * delivering the messages, the implementation should wait (back off)
   * and retry until it is successful.  Alternatively the message should be
   * returned to queue, returned to end of queue, or sent to another queue
   * @param grouperMessageAcknowledgeParam
   * @return result
   */
  public GrouperMessageAcknowledgeResult acknowledge(GrouperMessageAcknowledgeParam grouperMessageAcknowledgeParam);

}

```

The `GrouperMessage` interface (also in the GrouperClient) has a default implementation that can be used. The message contents are encrypted and carry metadata.

```java
package edu.internet2.middleware.grouperClient.messaging;

/**
 * grouper message sent to/from grouper messaging systems
 */
public interface GrouperMessage {

  /**
   * member id of a subjcet that sent the message
   * @return the from member id
   */
  public String getFromMemberId();

  /**
   * @param fromMemberId1 the from to set
   */
  public void setFromMemberId(String fromMemberId1);

  /**
   * @return the id
   */
  public String getId();

  /**
   * @param id1 the id to set
   */
  public void setId(String id1);

  /**
   * @return the message
   */
  public String getMessageBody();

  /**
   * @param message1 the message to set
   */
  public void setMessageBody(String message1);
}

```

## See also

- [Manage and send/receive built-in messages with GrouperShell (gsh)](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545249/GrouperShell+gsh)
- [Change log consumers](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545225/Change+log+consumers)
