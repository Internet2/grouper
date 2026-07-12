---
title: "Grouper Remedy Digital Marketplace integration"
space: Grouper
pageId: 28554312
version: 3
lastUpdated: 2026-07-01T05:40:43.394Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554312/Grouper+Remedy+Digital+Marketplace+integration
---

This integration can be two integrations, to Remedy (SaaS) and to Digital Marketplace which is a helper application for remedy.

This integration is a change log consumer or a message listener for events that should be published to Remedy Digital Marketplace. In Digital marketplace, a group created in Remedy will be created automatically in digital marketplace.

## Common configuration

Configure logging in log4j.properties

```
log4j.appender.grouperDigitalMarketplace                           = org.apache.log4j.DailyRollingFileAppender
log4j.appender.grouperDigitalMarketplace.File                      = logs/grouperDigitalMarketplace.log
log4j.appender.grouperDigitalMarketplace.DatePattern               = '.'yyyy-MM-dd
log4j.appender.grouperDigitalMarketplace.layout                    = org.apache.log4j.PatternLayout
log4j.appender.grouperDigitalMarketplace.layout.ConversionPattern  = %d{ISO8601}: %m%n

log4j.logger.edu.internet2.middleware.grouperRemedy.digitalMarketplace.GrouperDigitalMarketplaceLog =  DEBUG, grouperDigitalMarketplace
log4j.additivity.edu.internet2.middleware.grouperRemedy.digitalMarketplace.GrouperDigitalMarketplaceLog = false

```

Configure the grouper.client.properties

```

#####################################################
## DIGITAL MARKETPLACE
#####################################################

# these are properties to add to grouperClient.properties

# put groups in here which go to remedy, the name in remedy will be the extension here
grouperDigitalMarketplace.folder.name.withDigitalMarketplaceGroups = remedy:groups

# put the comma separated list of sources to send to remedy
grouperDigitalMarketplace.sourcesForSubjects = jdbc

# either have id for subject id or an attribute for the remedy username (e.g. netId)
grouperDigitalMarketplace.subjectAttributeForDigitalMarketplaceUsername = email

# if there is a suffix (e.g. @institution.edu then append that to the subject attribute to make the remedy login id
grouperDigitalMarketplace.subjectIdSuffix = 

# if there is a require group that users must be in to be a user in remedy
grouperDigitalMarketplace.requireGroup = remedy:remedyUser

# how long to cache remedy users in the requireGroup in grouper
grouperDigitalMarketplace.cacheGrouperUsersForMinutes = 60

#the quartz cron is a cron-like string. 
# http://www.quartz-scheduler.org/documentation/quartz-1.x/tutorials/crontrigger
grouperDigitalMarketplace.fullSync.quartzCron = 0 0 5 * * ?

# authentication settings for WS
grouperDigitalMarketplace.url =  https://school-env-dwpcatalog.onbmc.com
grouperDigitalMarketplace.user = 
grouperDigitalMarketplace.pass = 

# should log in the event log if no messages
grouperDigitalMarketplace.logIfNoMessages = false

# messaging config for incremental changes, blank to use default
grouperDigitalMarketplace.messaging.systemName = grouperBuiltinMessaging
# queueName is required for incremental provisioning
grouperDigitalMarketplace.messaging.queueName = digitalMarketplace_queue

# if you want to perform a full sync with each message received (note, assumes only applicable messages are sent)
# note, will wait X 30? seconds, then mark subsequent messages as complete for those 30 seconds
grouperDigitalMarketplace.fullSyncOnMessage = false

# note, this must be at least 5 seconds
grouperDigitalMarketplace.fullSyncOnMessageWaitSeconds = 30

#the quartz cron is a cron-like string. 
# http://www.quartz-scheduler.org/documentation/quartz-1.x/tutorials/crontrigger
# this defaults to every 30 seconds since the messaging long polls for 20 seconds.
grouperDigitalMarketplace.incrementalSync.quartzCron = 0/30 * * * * ?

```

## Configure as a message listener

Send messages from the Remedy Digital Workplace folder to the remedy message queue. Note: edit the filter and subject attributes as needed. Edit the grouper-loader.properties

```
changeLog.consumer.digitalMarketplaceEsb.class = edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer
changeLog.consumer.digitalMarketplaceEsb.quartzCron = 0 * * * * ?
# carefully adjust this filter e.g. for sourceId and groupName
changeLog.consumer.digitalMarketplaceEsb.elfilter = (event.sourceId == null || event.sourceId eq 'jdbc') && (event.groupName =~ '^digitalMarketplace\\:groups\\:.*$' || event.groupName eq 'digitalMarketplace:digitalMarketplaceUser' || event.name =~ '^digitalMarketplace\\:groups\\:.*$' || event.name eq 'digitalMarketplace:digitalMarketplaceUser') && (event.eventType eq 'GROUP_DELETE' || event.eventType eq 'GROUP_ADD' || event.eventType eq 'GROUP_UPDATE' || event.eventType eq 'MEMBERSHIP_DELETE' || event.eventType eq 'MEMBERSHIP_ADD' || event.eventType eq 'MEMBERSHIP_UPDATE')
# if messaging
changeLog.consumer.digitalMarketplaceEsb.publisher.class = edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbMessagingPublisher
# if change log
# changeLog.consumer.digitalMarketplaceEsb.publisher.class = edu.internet2.middleware.grouperRemedy.digitalMarketplace.DigitalMarketplaceEsbPublisher
changeLog.consumer.digitalMarketplaceEsb.publisher.messagingSystemName = grouperBuiltinMessaging
# queue or topic
changeLog.consumer.digitalMarketplaceEsb.publisher.messageQueueType = queue
changeLog.consumer.digitalMarketplaceEsb.publisher.queueOrTopicName = digitalMarketplace_queue
# this is optional if not using "id" for subjectId, need to be a subject attribute in the sources.xml
changeLog.consumer.digitalMarketplaceEsb.publisher.addSubjectAttributes = email

```

Copy the grouper remedy jar and all jars in the lib dir to the same directory as the grouper client jar

Run the service

```
java8/bin/java -cp lib/*:classes edu.internet2.middleware.grouperRemedy.digitalMarketplace.GrouperDigitalMarketplaceSync
```

## Configure as change log consumer

You might want to use a change log consumer since it runs in the same process as the grouper daemon, and the grouper status will monitor to make sure the process is running successfully.

Copy the grouper remedy jar to the grouper daemon lib dir. Edit the configs

Edit the grouper-loader.properties

```
changeLog.consumer.digitalMarketplaceEsb.class = edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer
changeLog.consumer.digitalMarketplaceEsb.quartzCron = 0 * * * * ?
# carefully adjust this filter e.g. for sourceId and groupName
changeLog.consumer.digitalMarketplaceEsb.elfilter = (event.sourceId == null || event.sourceId eq 'jdbc') && (event.groupName =~ '^digitalMarketplace\\:groups\\:.*$' || event.groupName eq 'digitalMarketplace:digitalMarketplaceUser' || event.name =~ '^digitalMarketplace\\:groups\\:.*$' || event.name eq 'digitalMarketplace:digitalMarketplaceUser') && (event.eventType eq 'GROUP_DELETE' || event.eventType eq 'GROUP_ADD' || event.eventType eq 'GROUP_UPDATE' || event.eventType eq 'MEMBERSHIP_DELETE' || event.eventType eq 'MEMBERSHIP_ADD' || event.eventType eq 'MEMBERSHIP_UPDATE')
# if change log
# changeLog.consumer.digitalMarketplaceEsb.publisher.class = edu.internet2.middleware.grouperRemedy.digitalMarketplace.DigitalMarketplaceEsbPublisher
# this is optional if not using "id" for subjectId, need to be a subject attribute in the sources.xml
changeLog.consumer.digitalMarketplaceEsb.publisher.addSubjectAttributes = email

otherJob.digitalMarketplace.class = edu.internet2.middleware.grouperRemedy.digitalMarketplace.DigitalMarketplaceOtherJob
otherJob.digitalMarketplace.quartzCron = 0 15 8 * * ?

```

Bounce the loader

## Sample logs

```
2018-09-24 23:26:00,898: method: RemedyEsbPublisher.dispatchEvent, elapsedMillis: 482
2018-09-24 23:29:00,296: method: retrieveRemedyGroups, getMillis: 152ms, size: 6, elapsedMillis: 153
2018-09-24 23:29:00,296: method: processMessage, eventType: MEMBERSHIP_ADD, groupName: penn:isc:ait:apps:remedy:groups:pg_hire_IT_cl
ients, groupInRemedy: false, elapsedMillis: 153
2018-09-24 23:29:00,296: method: RemedyEsbPublisher.dispatchEvent, elapsedMillis: 156
2018-09-24 23:32:00,476: method: retrieveRemedyGroups, authnMillis: 191ms, getMillis: 115ms, size: 6, elapsedMillis: 308
2018-09-24 23:32:00,476: method: processMessage, eventType: MEMBERSHIP_DELETE, groupName: penn:isc:ait:apps:remedy:groups:pg_hire_IT
_clients, groupInRemedy: false, elapsedMillis: 308
```

## Internal technical details

### Remedy rest API

Remedy API guide: [https://docs.bmc.com/docs/ars91/en/developing-an-api-program-609071507.html](https://docs.bmc.com/docs/ars91/en/developing-an-api-program-609071507.html)

Use Chrome Advanced Rest Client to test calls: go to: chrome ://apps

URL encode params here for testing: [https://meyerweb.com/eric/tools/dencoder/](https://meyerweb.com/eric/tools/dencoder/)

[https://docs.bmc.com/docs/itsm91/bmc-remedy-itsm-integrations-608491969.html](https://docs.bmc.com/docs/itsm91/bmc-remedy-itsm-integrations-608491969.html)

[https://www.scribd.com/doc/2551868/White-Paper-BMC-Service-Request-Management-2-0-Architecture](https://www.scribd.com/doc/2551868/White-Paper-BMC-Service-Request-Management-2-0-Architecture)

### Authentication

- [https://docs.bmc.com/docs/display/public/ars9000/Login+information](https://docs.bmc.com/docs/display/public/ars9000/Login+information)
- [https://www.youtube.com/watch?v=xue9Gx-dbEA&feature=youtu.be](https://www.youtube.com/watch?v=xue9Gx-dbEA&feature=youtu.be)
- [https://school-env-restapi.onbmc.com](https://school-env-restapi.onbmc.com/)

| `HEADER:`     `Content-Type: application/x-www-form-urlencoded`     `REQUEST`     `/api/jwt/loginjwt/login`     `POST with username and password form param`     ``     `RESPONSE`     `200`     `Body:`     `eyJhbXXXXXXXXXXXX` |
| --- |

### All users

| `REQUEST`     `GET /api/arsys/v1/entry/CTM:People?fields=values(Person%20ID,Remedy%20Login%20ID) (` `do` `not URL encode)`     `authorization AR-JWT eyJhbGXXXXXXXXXXXXXX`     ``     `RESPONSE`     `{`     `` `"entries"` `: [`     `` `{`     `` `"values"` `: {`     `` `"Person ID"` `:` `"PPL000000000306"` `,`     `` `"Remedy Login ID"` `:` `"foundationdataadmin"` `,`     `` `"Assignee Groups"` `:` `"1000000023;"`     `` `},`     `` `"_links"` `: {`     `` `"self"` `: [`     `` `{`     `` `"href"` `:` `"[https://school-dev-restapi.onbmc.com/api/arsys/v1/entry/CTM:People/PPL000000000306"](https://school-dev-restapi.onbmc.com/api/arsys/v1/entry/CTM:People/PPL000000000306)`     `` `}`     `` `]`     `` `}`     `` `}`     `` `],`     `` `"_links"` `: {`     `` `"self"` `: [`     `` `{`     `` `"href"` `:` `"[https://school-dev-restapi.onbmc.com/api/arsys/v1/entry/CTM:People"](https://school-dev-restapi.onbmc.com/api/arsys/v1/entry/CTM:People)`     `` `}`     `` `]`     `` `}`     `}` |
| --- |

### One user

| `REQUEST`     `GET /api/arsys/v1/entry/CTM:People?q=%27Remedy%20Login%20ID%` `27` `%` `20` `%3D%` `20` `%22foundationdataadmin%` `22` `&fields=values(Person%20ID,Remedy%20Login%20ID)`     `URL encoded from:` `'Remedy Login ID'` `=` `"foundationdataadmin"`     `authorization AR-JWT eyJhbGXXXXXXXXXXXXXX`     ``     `RESPONSE`     `{`     `` `"entries"` `: [`     `` `{`     `` `"values"` `: {`     `` `"Person ID"` `:` `"PPL000000000306"` `,`     `` `"Remedy Login ID"` `:` `"foundationdataadmin"`     `` `},`     `` `"_links"` `: {`     `` `"self"` `: [`     `` `{`     `` `"href"` `:` `"[https://school-dev-restapi.onbmc.com/api/arsys/v1/entry/CTM:People/PPL000000000306"](https://school-dev-restapi.onbmc.com/api/arsys/v1/entry/CTM:People/PPL000000000306)`     `` `}`     `` `]`     `` `}`     `` `}`     `` `],`     `` `"_links"` `: {`     `` `"self"` `: [`     `` `{`     `` `"href"` `:` `"[https://school-dev-restapi.onbmc.com/api/arsys/v1/entry/CTM:People?q=%27Remedy%20Login%20ID%27%20%3D%20%22foundationdataadmin%22&fields=values(Person%20ID,Remedy%20Login%20ID)"](https://school-dev-restapi.onbmc.com/api/arsys/v1/entry/CTM:People?q=%27Remedy%20Login%20ID%27%20%3D%20%22foundationdataadmin%22&fields=values(Person%20ID,Remedy%20Login%20ID))`     `` `}`     `` `]`     `` `}`     `}` |
| --- |

### All groups

| `REQUEST`     `GET /api/arsys/v1/entry/ENT:SYS-Access%20Permission%20Grps?fields=values(Status,Permission%20Group,Permission%20Group%20ID)`     ``     `RESPONSE`     `{`     `` `"entries"` `: [`     `` `{`     `` `"values"` `: {`     `` `"Status"` `:` `"Enabled"` `,`     `` `"Permission Group"` `:` `"Sample Entitlement Group 1"` `,`     `` `"Permission Group ID"` `:` `2000000001`     `` `},`     `` `"_links"` `: {`     `` `"self"` `: [`     `` `{`     `` `"href"` `:` `"[https://school-env-restapi.onbmc.com/api/arsys/v1/entry/ENT:SYS-Access%20Permission%20Grps/EGP000000000001"](https://school-env-restapi.onbmc.com/api/arsys/v1/entry/ENT:SYS-Access%20Permission%20Grps/EGP000000000001)`     `` `}`     `` `]`     `` `}`     `` `}`     `` `],`     `` `"_links"` `: {`     `` `"self"` `: [`     `` `{`     `` `"href"` `:` `"[https://school-env-restapi.onbmc.com/api/arsys/v1/entry/ENT:SYS-Access%20Permission%20Grps?fields=values(Status,Permission%20Group,Permission%20Group%20ID)"](https://school-env-restapi.onbmc.com/api/arsys/v1/entry/ENT:SYS-Access%20Permission%20Grps?fields=values(Status,Permission%20Group,Permission%20Group%20ID))`     `` `}`     `` `]`     `` `}`     `}` |
| --- |

### All memberships

| `REQUEST`     `GET /api/arsys/v1/entry/ENT:SYS%20People%20Entitlement%20Groups?fields=values(People%20Permission%20Group%20ID,Permission%20Group,Permission%20Group%20ID,Person%20ID,Remedy%20Login%20ID,Status)`     ``     `RESPONSE`     `{`     `` `"entries"` `: [`     `` `{`     `` `"values"` `: {`     `` `"People Permission Group ID"` `:` `"EPG000000000002"` `,`     `` `"Permission Group"` `:` `"2000000001"` `,`     `` `"Permission Group ID"` `:` `2000000001` `,`     `` `"Person ID"` `:` `"PPL000000000405"` `,`     `` `"Remedy Login ID"` `:` `"jsmith"` `,`     `` `"Status"` `:` `"Enabled"`     `` `},`     `` `"_links"` `: {`     `` `"self"` `: [`     `` `{`     `` `"href"` `:` `"[https://school-env-restapi.onbmc.com/api/arsys/v1/entry/ENT:SYS%20People%20Entitlement%20Groups/EPG000000000002"](https://school-env-restapi.onbmc.com/api/arsys/v1/entry/ENT:SYS%20People%20Entitlement%20Groups/EPG000000000002)`     `` `}`     `` `]`     `` `}`     `` `}`     `` `],`     `` `"_links"` `: {`     `` `"self"` `: [`     `` `{`     `` `"href"` `:` `"[https://school-env-restapi.onbmc.com/api/arsys/v1/entry/ENT:SYS%20People%20Entitlement%20Groups?fields=values(People%20Permission%20Group%20ID,Permission%20Group,Permission%20Group%20ID,Person%20ID,Remedy%20Login%20ID,Status)"](https://school-env-restapi.onbmc.com/api/arsys/v1/entry/ENT:SYS%20People%20Entitlement%20Groups?fields=values(People%20Permission%20Group%20ID,Permission%20Group,Permission%20Group%20ID,Person%20ID,Remedy%20Login%20ID,Status))`     `` `}`     `` `]`     `` `}`     `}` |
| --- |

### Create membership

| `FIRST, SEE IF MEMBERSHIP EXISTS AND IS DISABLED`     ``     `IF DOESNT EXIST:`     ``     `REQUEST`     `POST /api/arsys/v1/entry/ENT:SYS%20People%20Entitlement%20Groups`     ``     `` `{`     `` `"values"` `: {`     `` `"Permission Group ID"` `:` `2000000001` `,`     `` `"Permission Group"` `:` `"2000000001"` `,`     `` `"Person ID"` `:` `"PPL000000000616"` `,`     `` `"Remedy Login ID"` `:` `"jsmith"`     `` `}`     `` `}`     ``     `RESPONSE`     `201`     ``     `IF DOES EXIST:`     ``     `REQUEST`     `PUT /api/arsys/v1/entry/ENT:SYS%20People%20Entitlement%20Groups/EPG000000000101`     ``     `` `{`     `` `"values"` `: {`     `` `"Permission Group ID"` `:` `2000000001` `,`     `` `"Permission Group"` `:` `"2000000001"` `,`     `` `"Person ID"` `:` `"PPL000000000616"` `,`     `` `"Remedy Login ID"` `:` `"jsmith"` `,`     `` `"Status"` `:` `"Enabled"`     `` `}`     `` `}` |
| --- |

### Delete membership

| `` `REQUEST`     `PUT /api/arsys/v1/entry/ENT:SYS%20People%20Entitlement%20Groups/EPG000000000101`             `` `{`     `` `"values"` `: {`     `` `"Permission Group ID"` `:` `2000000001` `,`     `` `"Permission Group"` `:` `"2000000001"` `,`     `` `"Person ID"` `:` `"PPL000000000616"` `,`     `` `"Remedy Login ID"` `:` `"jsmith"` `,`     `` `"Status"` `:` `"Delete"`     `` `}`     `` `}` |
| --- |
