- make a queue for messaging

GrouperSession grouperSession = GrouperSession.startRootSession();
GrouperBuiltinMessagingSystem.createQueue("remedy_queue");
Subject subject = SubjectFinder.findById("GrouperSystem");
GrouperBuiltinMessagingSystem.allowSendToQueue("remedy_queue", subject);
GrouperBuiltinMessagingSystem.allowReceiveFromQueue("remedy_queue", subject);

- configure a changelog consumer to send certain groups to the message system in grouper-loader.properties

changeLog.consumer.remedyEsb.class = edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer
changeLog.consumer.remedyEsb.quartzCron = 0 * * * * ?
changeLog.consumer.remedyEsb.elfilter = event.groupName =~ '^remedy\\:groups\\:.*$' && (event.eventType eq 'GROUP_DELETE' || event.eventType eq 'GROUP_ADD' || event.eventType eq 'MEMBERSHIP_DELETE' || event.eventType eq 'MEMBERSHIP_ADD')
changeLog.consumer.remedyEsb.publisher.class = edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbMessagingPublisher
changeLog.consumer.remedyEsb.publisher.messagingSystemName = grouperBuiltinMessaging
# queue or topic
changeLog.consumer.remedyEsb.publisher.messageQueueType = queue
changeLog.consumer.remedyEsb.publisher.queueOrTopicName = remedy_queue

- create a group, change a membership and run the change log consumer

ADD

GrouperSession grouperSession = GrouperSession.startRootSession();
Group group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("remedy:groups:someGroup").save();
Subject subject = SubjectFinder.findById("GrouperSystem");
group.addMember(subject, false);
GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_remedyEsb");

DELETE

GrouperSession grouperSession = GrouperSession.startRootSession();
Group group = new GroupSave(grouperSession).assignName("remedy:groups:someGroup").save();
Subject subject = SubjectFinder.findById("GrouperSystem");
group.deleteMember(subject, false);
GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_remedyEsb");


- configure the grouper.client.properties

# if using include/exclude in grouper then exclude these groups in remedy
grouperRemedy.ignoreGroupSuffixes = _systemOfRecord, _includes, _excludes, _systemOfRecordAndIncludes, _includesMinusExcludes

# if there is a suffix...
grouperRemedy.subjectIdSuffix = TODO

# if require...
grouperRemedy.requireGroupInGrouper = a:b:c

grouperRemedy.folder.name.withRemedyGroups = 

grouperRemedy.deleteGroupsInRemedyWhichArentInGrouper
