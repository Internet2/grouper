---
title: "Grouper messaging send receive example"
space: Grouper
pageId: 28547347
version: 6
lastUpdated: 2026-07-01T05:47:02.147Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547347/Grouper+messaging+send+receive+example
---

Configure a message sender (change log format) in grouper-loader.properties

```
#####################################
## Messaging integration with change log
#####################################
# note, change "messagingSample" in key to be the name of the consumer.  e.g. changeLog.consumer.myAzureConsumer.class
changeLog.consumer.messagingSample.class = edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerToMessage
changeLog.consumer.messagingSample.quartzCron = 
changeLog.consumer.messagingSample.messagingSystemName = grouperBuiltinMessaging
changeLog.consumer.messagingSample.queueOrTopicName = sampleMessagingQueue
changeLog.consumer.messagingSample.messageQueueType = queue

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
## Messaging listener using the messaging API
#####################################
# note, change "messagingListener" in key to be the name of the listener.  e.g. messaging.listener.myAzureListener.class
# extends edu.internet2.middleware.grouper.messaging.MessagingListenerBase
# this listener will just print out messages: edu.internet2.middleware.grouper.messaging.MessagingListenerPrint
#

messaging.listener.messagingListenerSample.class = edu.internet2.middleware.grouper.messaging.MessagingListenerPrint
messaging.listener.messagingListenerSample.quartzCron = 0 * * * * ?
messaging.listener.messagingListenerSample.messagingSystemName = grouperBuiltinMessaging
messaging.listener.messagingListenerSample.queueName = sampleMessagingQueue

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
Change log entry: {"event":[{"changeLogTypeId":"79fc53db683646f5a72b33fb60cb0fde","contextId":"3acb507a84c34935b788e2f9c2c4b0bf","createdOnDb":1459133488055000,"sequenceNumber":512,"changeLogTypeCategory":"membership","changeLogTypeAction":"addMembership","field_id":"4c4dac01a91046059fdbaf209172ff5a","field_fieldName":"members","field_subjectId":"GrouperSystem","field_sourceId":"g:isa","field_membershipType":"flattened","field_groupId":"5b4f7610e2104aa295ddb58df237356f","field_groupName":"test:testGroup","field_memberId":"12cf184d43d342a49c6a28c8a45c39ff","field_fieldId":"48855dad8b58446fa9eee00dfd6ca211","field_subjectIdentifier0":"GrouperSystem"}]}
Change log entry: membership -> deleteMembership, null
Change log entry: {"event":[{"changeLogTypeId":"0d304c632bc34416a1f4fa3c7f4b11ef","contextId":"0a26c32974544c0995fd86ab39012ddc","createdOnDb":1459133493360000,"sequenceNumber":513,"changeLogTypeCategory":"membership","changeLogTypeAction":"deleteMembership","field_id":"4c4dac01a91046059fdbaf209172ff5a","field_fieldName":"members","field_subjectId":"GrouperSystem","field_sourceId":"g:isa","field_membershipType":"flattened","field_groupId":"5b4f7610e2104aa295ddb58df237356f","field_groupName":"test:testGroup","field_memberId":"12cf184d43d342a49c6a28c8a45c39ff","field_fieldId":"48855dad8b58446fa9eee00dfd6ca211","field_subjectIdentifier0":"GrouperSystem"}]}

```

See processed messages in the database

```
select * from grouper_message where queue_name = 'sampleMessagingQueue'
```

```
ID                               SENT_TIME_MICROS GET_ATTEMPT_TIME_MILLIS GET_ATTEMPT_COUNT STATE     GET_TIME_MILLIS FROM_MEMBER_ID                   QUEUE_NAME           MESSAGE_BODY                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         HIBERNATE_VERSION_NUMBER ATTEMPT_TIME_EXPIRES_MILLIS 
-------------------------------- ---------------- ----------------------- ----------------- --------- --------------- -------------------------------- -------------------- ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ ------------------------ --------------------------- 
7cb1e8275aa240f49556bd3b88da74c0 1459132920166000 1459133661441           2                 PROCESSED 1459133661588   12cf184d43d342a49c6a28c8a45c39ff sampleMessagingQueue {"event":[{"changeLogTypeId":"67911ea515f7480bb8aa363913bae196","contextId":"5cf984bf68eb4a4eac855bf8e517424f","createdOnDb":1459132860286000,"sequenceNumber":508,"changeLogTypeCategory":"attributeDefName","changeLogTypeAction":"addAttributeDefName","field_id":"bd2623bfcf3f439983a8741239e0a485","field_attributeDefId":"48194ee1d1fc490a8bf6cd519a4c569a","field_name":"etc:attribute:messages:grouperMessageQueues:sampleMessagingQueue","field_stemId":"4f6befbf61e845048949c69988e97335"}]}                                                                                                                                                                                                                               3                        1459133961434               
ce97a597391d49798e23a140d6330ce3 1459132920172000 1459133661448           2                 PROCESSED 1459133661620   12cf184d43d342a49c6a28c8a45c39ff sampleMessagingQueue {"event":[{"changeLogTypeId":"bb3dd51d5a3b443bb33a76d55e42169a","contextId":"5cf984bf68eb4a4eac855bf8e517424f","createdOnDb":1459132860291000,"sequenceNumber":509,"changeLogTypeCategory":"attributeDefNameSet","changeLogTypeAction":"addAttributeDefNameSet","field_id":"81ef12ff98c94c8980f78e128c07aa3a","field_type":"self","field_ifHasAttributeDefNameId":"bd2623bfcf3f439983a8741239e0a485","field_thenHasAttributeDefNameId":"bd2623bfcf3f439983a8741239e0a485","field_parentAttrDefNameSetId":"81ef12ff98c94c8980f78e128c07aa3a","field_depth":"0"}]}                                                                                                                                                                     3                        1459133961434               
77247f1d4fce47ec9f080319dfa73e64 1459132920177000 1459133661451           2                 PROCESSED 1459133661625   12cf184d43d342a49c6a28c8a45c39ff sampleMessagingQueue {"event":[{"changeLogTypeId":"79fc53db683646f5a72b33fb60cb0fde","contextId":"90eea5680b1a433ba02bf20ff50029f9","createdOnDb":1459132860469000,"sequenceNumber":510,"changeLogTypeCategory":"membership","changeLogTypeAction":"addMembership","field_id":"77700ab081514304a4054c28374c12fb","field_fieldName":"members","field_subjectId":"GrouperSystem","field_sourceId":"g:isa","field_membershipType":"flattened","field_groupId":"bd83c57c43e747b7b4055a29e51e7dce","field_groupName":"etc:attribute:messages:grouperMessageRole","field_memberId":"12cf184d43d342a49c6a28c8a45c39ff","field_fieldId":"48855dad8b58446fa9eee00dfd6ca211","field_subjectIdentifier0":"GrouperSystem"}]}                                          3                        1459133961434               
895f81c294ca4f079971e6bcf5b0cab8 1459132920180000 1459133661455           2                 PROCESSED 1459133661632   12cf184d43d342a49c6a28c8a45c39ff sampleMessagingQueue {"event":[{"changeLogTypeId":"0cb099fc58de4b4a9394c1fc26c30070","contextId":"42f07283e6ce4532accdf43324efa529","createdOnDb":1459132860546000,"sequenceNumber":511,"changeLogTypeCategory":"attributeAssign","changeLogTypeAction":"addAttributeAssign","field_id":"3482bb3e88274c71b3666fc72177e9de","field_attributeDefNameId":"bd2623bfcf3f439983a8741239e0a485","field_attributeAssignActionId":"daffb9d7a18a432da05839565d189969","field_assignType":"any_mem","field_ownerId1":"bd83c57c43e747b7b4055a29e51e7dce","field_ownerId2":"12cf184d43d342a49c6a28c8a45c39ff","field_attributeDefNameName":"etc:attribute:messages:grouperMessageQueues:sampleMessagingQueue","field_action":"send_to_queue","field_disallowed":"F"}]} 3                        1459133961434               
4a5a1beb858944b9aa0aa9d0890992ae 1459133700027000 1459133704353           1                 PROCESSED 1459133704361   12cf184d43d342a49c6a28c8a45c39ff sampleMessagingQueue {"event":[{"changeLogTypeId":"0cb099fc58de4b4a9394c1fc26c30070","contextId":"e737953184cc47ecb6e70fa78687998a","createdOnDb":1459133661347000,"sequenceNumber":514,"changeLogTypeCategory":"attributeAssign","changeLogTypeAction":"addAttributeAssign","field_id":"940bb9117c4246548aa0730bf1c7a907","field_attributeDefNameId":"bd2623bfcf3f439983a8741239e0a485","field_attributeAssignActionId":"7d282b660ef64bc68430a76c6d0d430c","field_assignType":"any_mem","field_ownerId1":"bd83c57c43e747b7b4055a29e51e7dce","field_ownerId2":"12cf184d43d342a49c6a28c8a45c39ff","field_attributeDefNameName":"etc:attribute:messages:grouperMessageQueues:sampleMessagingQueue","field_action":"receive","field_disallowed":"F"}]}       2                        1459133982960               
e6676fa3a57c4abda32f920548c8aeb8 1459133760042000 1459133761031           1                 PROCESSED 1459133761045   12cf184d43d342a49c6a28c8a45c39ff sampleMessagingQueue {"event":[{"changeLogTypeId":"79fc53db683646f5a72b33fb60cb0fde","contextId":"49cde92bee2c4199b0fb09e2273448e9","createdOnDb":1459133690988000,"sequenceNumber":515,"changeLogTypeCategory":"membership","changeLogTypeAction":"addMembership","field_id":"a4c4055c370147b69598c07090c237b6","field_fieldName":"members","field_subjectId":"GrouperSystem","field_sourceId":"g:isa","field_membershipType":"flattened","field_groupId":"5b4f7610e2104aa295ddb58df237356f","field_groupName":"test:testGroup","field_memberId":"12cf184d43d342a49c6a28c8a45c39ff","field_fieldId":"48855dad8b58446fa9eee00dfd6ca211","field_subjectIdentifier0":"GrouperSystem"}]}                                                                     2                        1459134048470               
8e589d06df4f4711b87cc47e5be63980 1459133760048000 1459133761035           1                 PROCESSED 1459133761055   12cf184d43d342a49c6a28c8a45c39ff sampleMessagingQueue {"event":[{"changeLogTypeId":"0d304c632bc34416a1f4fa3c7f4b11ef","contextId":"66d2635846b7480cb51a12df13febe49","createdOnDb":1459133696477000,"sequenceNumber":516,"changeLogTypeCategory":"membership","changeLogTypeAction":"deleteMembership","field_id":"a4c4055c370147b69598c07090c237b6","field_fieldName":"members","field_subjectId":"GrouperSystem","field_sourceId":"g:isa","field_membershipType":"flattened","field_groupId":"5b4f7610e2104aa295ddb58df237356f","field_groupName":"test:testGroup","field_memberId":"12cf184d43d342a49c6a28c8a45c39ff","field_fieldId":"48855dad8b58446fa9eee00dfd6ca211","field_subjectIdentifier0":"GrouperSystem"}]}                                                                  2                        1459134048470               
2dd2ee1aaf6c401db4752e43db64dda2 1459133520085000 1459133804943           2                 PROCESSED 1459133805059   12cf184d43d342a49c6a28c8a45c39ff sampleMessagingQueue {"event":[{"changeLogTypeId":"79fc53db683646f5a72b33fb60cb0fde","contextId":"3acb507a84c34935b788e2f9c2c4b0bf","createdOnDb":1459133488055000,"sequenceNumber":512,"changeLogTypeCategory":"membership","changeLogTypeAction":"addMembership","field_id":"4c4dac01a91046059fdbaf209172ff5a","field_fieldName":"members","field_subjectId":"GrouperSystem","field_sourceId":"g:isa","field_membershipType":"flattened","field_groupId":"5b4f7610e2104aa295ddb58df237356f","field_groupName":"test:testGroup","field_memberId":"12cf184d43d342a49c6a28c8a45c39ff","field_fieldId":"48855dad8b58446fa9eee00dfd6ca211","field_subjectIdentifier0":"GrouperSystem"}]}                                                                     3                        1459134083241               
8892f742ca2d49b28dd744cb4d9eba2d 1459133520090000 1459133804947           2                 PROCESSED 1459133805064   12cf184d43d342a49c6a28c8a45c39ff sampleMessagingQueue {"event":[{"changeLogTypeId":"0d304c632bc34416a1f4fa3c7f4b11ef","contextId":"0a26c32974544c0995fd86ab39012ddc","createdOnDb":1459133493360000,"sequenceNumber":513,"changeLogTypeCategory":"membership","changeLogTypeAction":"deleteMembership","field_id":"4c4dac01a91046059fdbaf209172ff5a","field_fieldName":"members","field_subjectId":"GrouperSystem","field_sourceId":"g:isa","field_membershipType":"flattened","field_groupId":"5b4f7610e2104aa295ddb58df237356f","field_groupName":"test:testGroup","field_memberId":"12cf184d43d342a49c6a28c8a45c39ff","field_fieldId":"48855dad8b58446fa9eee00dfd6ca211","field_subjectIdentifier0":"GrouperSystem"}]}                                                                  3                        1459134083241               
9 row(s) in 37 ms
```
