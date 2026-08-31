---
title: "Grouper messaging send receive change log adapter example"
space: Grouper
pageId: 28547351
version: 4
lastUpdated: 2016-03-28T06:47:46.451Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547351/Grouper+messaging+send+receive+change+log+adapter+example
---

Configure a message sender (change log format) in grouper-loader.properties

```
#####################################
## Messaging integration with change log
#####################################
# note, change "messagingSample" in key to be the name of the consumer.  e.g. changeLog.consumer.myAzureConsumer.class
changeLog.consumer.changeLogMessagingSample.class = edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerToMessage
changeLog.consumer.changeLogMessagingSample.quartzCron = 
changeLog.consumer.changeLogMessagingSample.messagingSystemName = grouperBuiltinMessaging
changeLog.consumer.changeLogMessagingSample.queueOrTopicName = sampleChangeLogMessagingQueue
changeLog.consumer.changeLogMessagingSample.messageQueueType = queue

```

As long as this is not edited in grouper-loader.properties, and the messagingSystemName is grouperBuiltinMessaging, the queue and privileges will be created:

```
#####################################
## Messaging overall settings for daemon jobs
#####################################
# auto create built in queues, topics, privileges
loader.messaging.settings.autocreate.objects = true
```

Make a messaging listener that will print out messages from that queue

```
#####################################
## Messaging listener using the change log consumer API
#####################################
# note, change "messagingListenerChangeLogConsumer" in key to be the name of the listener.  e.g. messaging.listener.myAzureListener.class
#
# keep this class to be MessagingListenerToChangeLogConsumer
messaging.listener.messagingListenerChangeLogConsumerPrint.class = edu.internet2.middleware.grouper.messaging.MessagingListenerToChangeLogConsumer
messaging.listener.messagingListenerChangeLogConsumerPrint.changeLogConsumerClass = edu.internet2.middleware.grouper.changeLog.consumer.PrintTest
messaging.listener.messagingListenerChangeLogConsumerPrint.quartzCron = 0 * * * * ?
messaging.listener.messagingListenerChangeLogConsumerPrint.messagingSystemName = grouperBuiltinMessaging
messaging.listener.messagingListenerChangeLogConsumerPrint.queueName = sampleChangeLogMessagingQueue

```

Create events via GSH

```
GrouperSession grouperSession = GrouperSession.startRootSession();
Group group = new GroupSave(grouperSession).assignName("test:testGroup").assignCreateParentStemsIfNotExist(true).save();
addMember("test:testGroup", "GrouperSystem");
delMember("test:testGroup", "GrouperSystem");
```

See the output

```
Change log entry: membership -> addMembership, null
Change log entry: membership -> deleteMembership, null
```

Query messages

```
select * from grouper_message where queue_name = 'sampleChangeLogMessagingQueue'
```

Output

```
ID                               SENT_TIME_MICROS GET_ATTEMPT_TIME_MILLIS GET_ATTEMPT_COUNT STATE         GET_TIME_MILLIS FROM_MEMBER_ID                   QUEUE_NAME                    MESSAGE_BODY                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  HIBERNATE_VERSION_NUMBER ATTEMPT_TIME_EXPIRES_MILLIS 
-------------------------------- ---------------- ----------------------- ----------------- ------------- --------------- -------------------------------- ----------------------------- --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- ------------------------ --------------------------- 
abf5c09d478c461cbdba383b8cc8c36b 1459135864111000 1459135866870           1                 PROCESSED     1459135866908   12cf184d43d342a49c6a28c8a45c39ff sampleChangeLogMessagingQueue {"event":[{"changeLogTypeId":"67911ea515f7480bb8aa363913bae196","contextId":"fefeb029bad24a7bbbccd3c9ca730348","createdOnDb":1459135800303000,"sequenceNumber":519,"changeLogTypeCategory":"attributeDefName","changeLogTypeAction":"addAttributeDefName","field_id":"d5e2e4797f7f47d98cb4b1b128f11dcd","field_attributeDefId":"48194ee1d1fc490a8bf6cd519a4c569a","field_name":"etc:attribute:messages:grouperMessageQueues:sampleChangeLogMessagingQueue","field_stemId":"4f6befbf61e845048949c69988e97335"}]}                                                                                                                                                                                                                               2                        1459136160297               
78a894922a7b4e2fbf773712e49366d9 1459135864197000 1459135866876           1                 PROCESSED     1459135866918   12cf184d43d342a49c6a28c8a45c39ff sampleChangeLogMessagingQueue {"event":[{"changeLogTypeId":"bb3dd51d5a3b443bb33a76d55e42169a","contextId":"fefeb029bad24a7bbbccd3c9ca730348","createdOnDb":1459135800307000,"sequenceNumber":520,"changeLogTypeCategory":"attributeDefNameSet","changeLogTypeAction":"addAttributeDefNameSet","field_id":"52d8f023771a454b80e6b838212a1e41","field_type":"self","field_ifHasAttributeDefNameId":"d5e2e4797f7f47d98cb4b1b128f11dcd","field_thenHasAttributeDefNameId":"d5e2e4797f7f47d98cb4b1b128f11dcd","field_parentAttrDefNameSetId":"52d8f023771a454b80e6b838212a1e41","field_depth":"0"}]}                                                                                                                                                                              2                        1459136160297               
8c6ffc63b10c4062985cf4ea2642d771 1459135864200000 1459135866882           1                 PROCESSED     1459135866925   12cf184d43d342a49c6a28c8a45c39ff sampleChangeLogMessagingQueue {"event":[{"changeLogTypeId":"0cb099fc58de4b4a9394c1fc26c30070","contextId":"8bd9d6ae6c3f4f1abe50f638d15db3ea","createdOnDb":1459135800484000,"sequenceNumber":521,"changeLogTypeCategory":"attributeAssign","changeLogTypeAction":"addAttributeAssign","field_id":"8a717241cb4c4333a6675580d86fe5a2","field_attributeDefNameId":"d5e2e4797f7f47d98cb4b1b128f11dcd","field_attributeAssignActionId":"7d282b660ef64bc68430a76c6d0d430c","field_assignType":"any_mem","field_ownerId1":"bd83c57c43e747b7b4055a29e51e7dce","field_ownerId2":"12cf184d43d342a49c6a28c8a45c39ff","field_attributeDefNameName":"etc:attribute:messages:grouperMessageQueues:sampleChangeLogMessagingQueue","field_action":"receive","field_disallowed":"F"}]}       2                        1459136160297               
a68b34e129564741bdb2c3ea0a43e7cb 1459135924023000 1459135927856           1                 PROCESSED     1459135927992   12cf184d43d342a49c6a28c8a45c39ff sampleChangeLogMessagingQueue {"event":[{"changeLogTypeId":"0cb099fc58de4b4a9394c1fc26c30070","contextId":"737e0d0b0f944f7cbfc6e4db38072ccf","createdOnDb":1459135864098000,"sequenceNumber":522,"changeLogTypeCategory":"attributeAssign","changeLogTypeAction":"addAttributeAssign","field_id":"6fb44d44b24f41159ae0de234f73923c","field_attributeDefNameId":"d5e2e4797f7f47d98cb4b1b128f11dcd","field_attributeAssignActionId":"daffb9d7a18a432da05839565d189969","field_assignType":"any_mem","field_ownerId1":"bd83c57c43e747b7b4055a29e51e7dce","field_ownerId2":"12cf184d43d342a49c6a28c8a45c39ff","field_attributeDefNameName":"etc:attribute:messages:grouperMessageQueues:sampleChangeLogMessagingQueue","field_action":"send_to_queue","field_disallowed":"F"}]} 2                        1459136210756               
5ce8355fd09e4c54b2a73a3e3db2c71f 1459147264043000 1459147267614           1                 GET_ATTEMPTED (null)          12cf184d43d342a49c6a28c8a45c39ff sampleChangeLogMessagingQueue {"event":[{"changeLogTypeId":"79fc53db683646f5a72b33fb60cb0fde","contextId":"e8d766d21c5b462a81079072387500be","createdOnDb":1459147220097000,"sequenceNumber":523,"changeLogTypeCategory":"membership","changeLogTypeAction":"addMembership","field_id":"3a2703bee8a24db5b8c1b6ed4ddc7e57","field_fieldName":"members","field_subjectId":"GrouperSystem","field_sourceId":"g:isa","field_membershipType":"flattened","field_groupId":"5b4f7610e2104aa295ddb58df237356f","field_groupName":"test:testGroup","field_memberId":"12cf184d43d342a49c6a28c8a45c39ff","field_fieldId":"48855dad8b58446fa9eee00dfd6ca211","field_subjectIdentifier0":"GrouperSystem"}]}                                                                              1                        1459147547011               
6d81706e4d264da0867a81b82838a02e 1459147264051000 1459147267661           1                 GET_ATTEMPTED (null)          12cf184d43d342a49c6a28c8a45c39ff sampleChangeLogMessagingQueue {"event":[{"changeLogTypeId":"0d304c632bc34416a1f4fa3c7f4b11ef","contextId":"b1251f6de6794f8a9ec47edaef96af87","createdOnDb":1459147225213000,"sequenceNumber":524,"changeLogTypeCategory":"membership","changeLogTypeAction":"deleteMembership","field_id":"3a2703bee8a24db5b8c1b6ed4ddc7e57","field_fieldName":"members","field_subjectId":"GrouperSystem","field_sourceId":"g:isa","field_membershipType":"flattened","field_groupId":"5b4f7610e2104aa295ddb58df237356f","field_groupName":"test:testGroup","field_memberId":"12cf184d43d342a49c6a28c8a45c39ff","field_fieldId":"48855dad8b58446fa9eee00dfd6ca211","field_subjectIdentifier0":"GrouperSystem"}]}                                                                           1                        1459147547011               
6 row(s) in 71 ms
```
