---
title: "Grouper Messaging with AWS SQS"
space: Grouper
pageId: 28548136
version: 16
lastUpdated: 2026-07-01T05:45:13.612Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548136/Grouper+Messaging+with+AWS+SQS
---

> The info on this page applies to Grouper 2.5 and above.

Grouper AWS SQS messaging system works with Standard and FIFO queues.

It uses BasicAWSCredentials method to authenticate with AWS. You need to configure the accessKey and secretKey properties in grouper.client.properties file. You don't need to enter your accessKey and secretKey in plain text. You can encrypt both of the properties using encrypt.key property.

You can follow this link: [http://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/sqs-setting-up.html](http://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/sqs-setting-up.html) to setup SQS and get your access key and secret key.

Notes:

- We don't support creating the queues. Setting autoCreateObjects property to true will do nothing. The name of the queue you send in the method calls must exist already.
- Grouper messaging with SQS only supports queue types so setting queueType property to topic will throw IllegalArgumentException
- Make sure you call acknowledge method after receiving the messages otherwise you will keep receiving the same messages over and over.
- We use message based deduplication for FIFO types. We assign a UUID to each message to make it unique.
- return_to_end_of_queue acknowledgment type can only work with FIFO type.

All the properties you can set in grouper.client.properties for AWS SQS.

```
# name of a messaging system. note, "awsSystem" can be arbitrary
grouper.messaging.system.awsSystem.name = awsSystem

# class that implements edu.internet2.middleware.grouperClient.messaging.GrouperMessagingSystem
grouper.messaging.system.awsSystem.class = edu.internet2.middleware.grouperMessagingAWS.GrouperMessagingSqsSystem

# sqs settings
grouper.messaging.system.awsSystem.accessKey = 

grouper.messaging.system.awsSystem.secretKey = 

grouper.messaging.system.awsSystem.defaultPageSize = 5

#SQS can return maximum of 10 messages at once so don't set the value higher than 10.
grouper.messaging.system.awsSystem.maxPageSize = 10

# default system settings to this messaging system, note, there is only one level of inheritance. (optional)
grouper.messaging.system.awsSystem.defaultSystemName = awsSystem

```

grouper-loader.properties file must have the same messaging system name as defined above. See example below:

```
changeLog.consumer.esb.publisher.messagingSystemName = awsSystem
```
