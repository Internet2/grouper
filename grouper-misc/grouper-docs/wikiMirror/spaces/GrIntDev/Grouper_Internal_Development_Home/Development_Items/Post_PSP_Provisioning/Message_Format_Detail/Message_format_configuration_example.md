---
title: "Message format configuration example"
space: GrIntDev
pageId: 48795896
version: 16
lastUpdated: 2026-07-12T17:04:05.282Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48795896/Message+format+configuration+example
---

This is an example of configuring Grouper messages in the upcoming [Grouper PSP NG](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792599/Post+PSP+Provisioning)

There could be a grouper default message config file, e.g. grouperMessageDefault.properties. We could also have defaults for SCIM or whatever else built in preconfigured formats

```
allMessages.securityUri = grouperMessageFormat:JOSE
```

Maybe the institution has some defaults in testUniversity.properties and would be in the messaging config file folder on the file system. Perhaps it could also be pulled from the classpath or database or web or something

```
inheritFrom = classpath:messaging/grouperMessageDefault.properties
 
sendFrom = grouper@example.com
 
messagingImplementation = edu.internet2.middleware.grouper.messaging.systems.GrouperActiveMqMessaging
 
server = prod.activeMq.testUniversity.edu
 
pass = /something/private/grouperActivemq.pass
 
joseSenderCertificate = /something/private/grouperActivemqCert.der
 
joseSenderKey = /something/private/grouperActivemqCert.key
```

To send out all messages to a consumer using the default config, formats, etc, maybe this is for banner, so the file would be called banner.properties

```
inheritFrom = file:testUniversity.properties
 
sendTo = bannerGrouper@example.com
 
joseReceiverCertificate = /something/private/bannerGrouperCert.der
```

To send out messages about membership changes in a certain folder without encryption to a wiki application, do this in a file called testUniversityWiki.properties

```
inheritFrom = file:testUniversity.properties

sendTo = testUniversityWiki@example.com

allMessages.securityUri = grouperMessageFormat:Plain
 
allMessages.includeOnlyEventTypes = MEMBERSHIP_ADD, MEMBERSHIP_UPDATE, MEMBERSHIP_DELETE
 
allMessages.filter.groupNameRegex = apps:wiki:groups:.*
```

To send out messages about membership changes in a certain folder without encryption to an email application with a custom format, do this in a file called testUniversityEmailGroups.properties

```
inheritFrom = file:testUniversity.properties

sendTo = testUniversityEmailGroups@example.com

allMessages.securityUri = grouperMessageFormat:Plain

allMessages.includeOnlyEventTypes = MEMBERSHIP_ADD, MEMBERSHIP_UPDATE, MEMBERSHIP_DELETE

allMessages.filter.groupNameRegex = apps:email:groups:.*
 
# make a message like this: {"event": "add", "mailList": "it-staff", "userId": "jsmith"}
 
messageType.MEMBERSHIP_ADD.process.0.0.transform = originalMessage = result
 
messageType.MEMBERSHIP_ADD.process.1.0.transform = result = $newObject$

messageType.MEMBERSHIP_ADD.process.2.0.transform = result.event = add

messageType.MEMBERSHIP_ADD.process.3.0.transform = result.mailList = ${grouperGetGroupExtension(originalMessage.payload.groupName)}

messageType.MEMBERSHIP_ADD.process.4.0.transform = result.userId = ${originalMessage.payload.subjectId}

messageType.MEMBERSHIP_UPDATE.process.0.0.transform = originalMessage = result

messageType.MEMBERSHIP_UPDATE.process.1.0.transform = result = $newObject$

messageType.MEMBERSHIP_UPDATE.process.2.0.transform = result.event = change

messageType.MEMBERSHIP_UPDATE.process.3.0.transform = result.mailList = ${grouperGetGroupExtension(originalMessage.payload.groupName)}

messageType.MEMBERSHIP_UPDATE.process.4.0.transform = result.userId = ${originalMessage.payload.subjectId}

messageType.MEMBERSHIP_DELETE.process.0.0.transform = originalMessage = result

messageType.MEMBERSHIP_DELETE.process.1.0.transform = result = $newObject$

messageType.MEMBERSHIP_DELETE.process.2.0.transform = result.event = delete

messageType.MEMBERSHIP_DELETE.process.3.0.transform = result.mailList = ${grouperGetGroupExtension(originalMessage.payload.groupName)}

messageType.MEMBERSHIP_DELETE.process.4.0.transform = result.userId = ${originalMessage.payload.subjectId}

```

Comments: can we do java class or script instead of property file transformations? Yes, we should

Can we do shared secret instead of JOSE certificates? Yes, and we need to decide what the default will be

Comments from DaveL

```
I'm not sure I like the transformation part.  
Secondly, I think I'd rather have individual provisioning from specific 
groups/folders configured directly on that group via ldap a-la-grouper-loader-ldap.  
 
That way when a person is looking at the group in the UI they can 
see where it's going and what it's going out as.
 
Forgot to add that config via attribute allows the grouper admins 
to delegate the management of provisioning to those who would 
ultimately manage the provisioned system.
```

**See Also**

Grouper Messaging System

[Grouper Messaging](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547740/Grouper+messaging+built+in) Built-In

[Message Format Detail](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793311/Message+Format+Detail)

[Grouper Messaging System Development Guide](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548809/Grouper+Messaging+System+development+guide)
