- make a queue for messaging

GrouperSession grouperSession = GrouperSession.startRootSession();
GrouperBuiltinMessagingSystem.createQueue("box_queue");
Subject subject = SubjectFinder.findById("GrouperSystem");
GrouperBuiltinMessagingSystem.allowSendToQueue("box_queue", subject);
GrouperBuiltinMessagingSystem.allowReceiveFromQueue("box_queue", subject);

- configure a changelog consumer to send certain groups to the message system in grouper-loader.properties

changeLog.consumer.boxEsb.class = edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer
changeLog.consumer.boxEsb.quartzCron = 0 * * * * ?
changeLog.consumer.boxEsb.elfilter = event.groupName =~ '^box\\:groups\\:.*$' && (event.eventType eq 'GROUP_DELETE' || event.eventType eq 'GROUP_ADD' || event.eventType eq 'MEMBERSHIP_DELETE' || event.eventType eq 'MEMBERSHIP_ADD')
changeLog.consumer.boxEsb.publisher.class = edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbMessagingPublisher
changeLog.consumer.boxEsb.publisher.messagingSystemName = grouperBuiltinMessaging
# queue or topic
changeLog.consumer.boxEsb.publisher.messageQueueType = queue
changeLog.consumer.boxEsb.publisher.queueOrTopicName = box_queue

- create a group, change a membership and run the change log consumer

ADD

GrouperSession grouperSession = GrouperSession.startRootSession();
Group group = new GroupSave(grouperSession).assignName("box:groups:someGroup").save();
Subject subject = SubjectFinder.findById("GrouperSystem");
group.addMember(subject, false);
GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_boxEsb");

DELETE

GrouperSession grouperSession = GrouperSession.startRootSession();
Group group = new GroupSave(grouperSession).assignName("box:groups:someGroup").save();
Subject subject = SubjectFinder.findById("GrouperSystem");
group.deleteMember(subject, false);
GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_boxEsb");

- create a box app auth token

https://docs.box.com/docs/getting-started-box-platform

Chriss-MacBook-Air:box mchyzer$ openssl genrsa -aes256 -out private_key.pem 2048
Chriss-MacBook-Air:box mchyzer$ openssl rsa -pubout -in private_key.pem -out public_key.pem

sign up for two step authn in box if not SSO

make application in box: https://app.box.com/developers/services
1. authentication type: server
2. user access: all users
3. scopes: manage users, manage app users, manage groups
4. advanced features: none
5. note client_id
6. note client_secret
7. redirect uri: https://localhost
8. under apps in admin console copy the API key from the app page ad paste in


- configure the grouper.client.properties

# if using include/exclude in grouper then exclude these groups in box
grouperBox.ignoreGroupSuffixes = _systemOfRecord, _includes, _excludes, _systemOfRecordAndIncludes, _includesMinusExcludes

# if there is a suffix...
grouperBox.subjectIdSuffix = TODO

# if require...
grouperBox.requireGroupInGrouper = a:b:c

grouperBox.folder.name.withBoxGroups = 

grouperBox.deleteGroupsInBoxWhichArentInGrouper
