---
title: "FU Berlin - Using Jabber"
space: Grouper
pageId: 28543981
version: 9
lastUpdated: 2026-07-01T05:49:01.632Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543981/FU+Berlin+-+Using+Jabber
---

### Using Jabber

We're using Jabber for

- sending notifications if a wheel group member is removed from the group
- sending notifications if a target group is deleted

This is how we've done it:

#### Step 1: Copying & pasting XMPP configuration lines

Copy & paste the following lines from grouper-loader.base.properties to grouper-loader.properties and fill them with yout own credentials.

```text
###################################
## XMPP notifications 
## (note, uncomment the consumer class and cron above)
## this will get grouper ws getMembers rest lite xmp: 
## http://anonsvn.internet2.edu/cgi-bin/viewvc.cgi/i2mi/trunk/grouper-ws/grouper-ws/doc/samples/getMembers/WsSampleGetMembersRestLite_xml.txt?view=log
###################################

## general xmpp configuration
xmpp.server.host = jabber.school.edu
xmpp.server.port = 5222
xmpp.user = username
# note, pass can be in an external file with morphstring
xmpp.pass = 
xmpp.resource = grouperServer
```

#### Step 2: Add an additional consumer

Add an additional xmpp consumer to grouper-loader.properties (the properties are set in step 3 to 6).

```text
changeLog.consumer.xmpp.quartzCron = 
changeLog.consumer.xmpp.class = 
changeLog.consumer.xmpp.elfilter = 
changeLog.consumer.xmpp.publisher.class = 
changeLog.consumer.xmpp.publisher.recipient = 
```

#### Step 3: Include the used classes

In our case the default classes are used:

```text
changeLog.consumer.xmpp.class = edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer
changeLog.consumer.xmpp.publisher.class = edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbXmppPublisher
```

#### Step 4: Define the conditions

We have defined two conditions that are to lead to Jabber notifications:

- A target group is deleted:  
  event.eventType eq 'GROUP_DELETE' && event.name =~ '^targets\\:.*$'
- A wheel member is removed from the wheel group:  
  event.eventType eq 'MEMBERSHIP_DELETE' && event.membershipType == 'flattened' && event.groupName == 'etc:wheelGroup'

```text
changeLog.consumer.xmpp.elfilter = (event.eventType eq 'GROUP_DELETE' && event.name =~ '^targets\\:.*$') || (event.eventType eq 'MEMBERSHIP_DELETE' && event.membershipType == 'flattened' && event.groupName == 'etc:wheelGroup')
```

#### Step 5: Define recipients

Define the recipients of the notifications.

```text
changeLog.consumer.xmpp.publisher.recipient = user1@jabber.fu-berlin.de,user2@jabber.fu-berlin.de,user3@jabber.fu-berlin.de
```

#### Step 6: Set quartzCron execution time

e.g. the following line will check if the conditions are met and accordingly send jabber notifications each 10th second of a minute.

```text
changeLog.consumer.xmpp.quartzCron = 10 * * * * ? 
```
