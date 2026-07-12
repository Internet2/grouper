---
title: "Grouper Zoom Provisioner"
space: Grouper
pageId: 28555059
version: 18
lastUpdated: 2026-07-01T05:39:03.865Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555059/Grouper+Zoom+Provisioner
---

> The info on this page applies to Grouper v4 and above.

Grouper can sync groups and memberships to Zoom using their web service API. Grouper can also sync memberships with SAML for just-in-time provisioning.

## Create a JSON Web Tokens (JWT) credential

[https://marketplace.zoom.us/docs/guides/auth/jwt](https://marketplace.zoom.us/docs/guides/auth/jwt)

## This is for 2.5.30+

If not on 2.5.30, then make sure you have these jars there

jwt jar

[grouper zoom jar](https://grouperdemo.internet2.edu/grouper/temp/grouper-zoom-2.4.0-SNAPSHOT.jar)

## Debug

Configure in log4j.properties

```
# log commands to WS
log4j.logger.edu.internet2.middleware.grouper.app.zoom.GrouperZoomCommands = DEBUG
# or log all activity
log4j.logger.edu.internet2.middleware.grouper.app.zoom = DEBUG
# or send to special logger
log4j.logger.edu.internet2.middleware.grouper.app.zoom = DEBUG, grouper_zoomLogger
log4j.additivity.edu.internet2.middleware.grouper.app.zoom = false
```

## Configure the provisioner

grouper-loader.properties

```
##############
## Zoom
##############

# endpoint of zoom including the version
# {valueType: "string", defaultValue: "https://api.zoom.us/v2"}
zoom.myConfigId.endpoint = https://api.zoom.us/v2

# zoom jwt api key
# {valueType: "string"}
zoom.myConfigId.jwtApiKey = 

# zoom jwt api key
# {valueType: "password", sensitive: true}
zoom.myConfigId.jwtApiSecretPassword = 

# cache jwt for minutes, must be at least 2, or could be 0 to not cache.  If it is 1 it will be changed to 2.
# {valueType: "integer", defaultValue: "30"}
zoom.myConfigId.cacheJwtForMinutes = 30

# page size when retrieving members, max is 300
# {valueType: "integer", defaultValue: "300"}
zoom.myConfigId.pageSizeMemberships = 300

# base folder
# {valueType: "string"}
zoom.myConfigId.folderToProvision = 

# subject sources to provision
# {valueType: "string", multiple: true}
zoom.myConfigId.sourcesForSubjects = 

# ignore user ids in zoom (dont remove them) e.g. admin ids
# {valueType: "string", multiple: true}
zoom.myConfigId.ignoreUserIds =

# subject attribute to match zoom email address (generally eppn)
# {valueType: "string"}
zoom.myConfigId.subjectAttributeForZoomEmail = 

# when groups are deleted in grouper should they be deleted in the target
# {valueType: "boolean", defaultValue: "true"}
zoom.myConfigId.deleteInTargetIfDeletedInGrouper = true

```

## Deprovisioning

grouper-loader.properties

```
# if deleting users, this is the group of users to delete.  Load this group with your grace period policy
# {valueType: "string"}
# zoom.myConfigId.groupNameToDeleteUsers = a:b:c

# if deleting users, this will just log instead of actually deleting
# {valueType: "boolean", defaultValue: "false"}
# zoom.myConfigId.logUserDeletesInsteadOfDeleting = false
```

Populate a group with a grace period of users to delete. These will be deleted in zoom not disassociated. You might want to use the loaded userType to see if a user has an account when populating this group

## Configure full sync

grouper-loader.properties

```
#################
## Zoom full provisioning
#################

# {valueType: "class", mustExtendClass: "edu.internet2.middleware.grouper.app.loader.OtherJobBase", mustImplementInterface: "org.quartz.Job"}
otherJob.myZoomFull.class = edu.internet2.middleware.grouper.app.zoom.GrouperZoomFullSync

# zoom full cron, default to 2:20
# {valueType: "string"}
otherJob.myZoomFull.quartzCron = 0 20 2 * * ?

# links to zoom config id
# {valueType: "string"}
otherJob.myZoomFull.zoomConfigId = myConfigId

```

## Configure incremental sync

grouper-loader.properties

```
#################
## Zoom incremental provisioning
#################

# esb consumer
# {valueType: "class", required: true, mustExtendClass: "edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer"}
changeLog.consumer.zoomEsbProd.class = edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer

# zoom incremental cron, defualt to every hour except 2am (to not conflict with full)
# {valueType: "string"}
changeLog.consumer.zoomEsbProd.quartzCron = 0 * 0,1,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23 * * ?

# zoom publishing class
# {valueType: "class", mustExtendClass: "edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbMessagingPublisher"}
changeLog.consumer.zoomEsbProd.publisher.class = edu.internet2.middleware.grouper.app.zoom.ZoomEsbPublisher

# el filter
# {valueType: "string"}
changeLog.consumer.zoomEsbProd.elfilter = (event.sourceId == null || event.sourceId eq 'pennperson') && (event.groupName =~ '^penn\:isc\:ait\:apps\:zoom\:service\:policy\:groups\:.*$' || event.name =~ '^penn\:isc\:ait\:apps\:zoom\:service\:policy\:groups\:.*$' ) && (event.eventType eq 'GROUP_DELETE' || event.eventType eq 'GROUP_ADD' || event.eventType eq 'GROUP_UPDATE' || event.eventType eq 'MEMBERSHIP_DELETE' || event.eventType eq 'MEMBERSHIP_ADD' || event.eventType eq 'MEMBERSHIP_UPDATE')

# add subject attributes
# {valueType: "string", multiple: true}
changeLog.consumer.zoomEsbProd.publisher.addSubjectAttributes = EPPN

# zoom config id
# {valueType: "string"}
changeLog.consumer.zoomEsbProd.zoomConfigId = myConfigId

```

## Loading groups / roles / userTypes from zoom

grouper-loader.properties

```
#################
## Zoom loading
#################

# {valueType: "class", mustExtendClass: "edu.internet2.middleware.grouper.app.loader.OtherJobBase", mustImplementInterface: "org.quartz.Job"}
# otherJob.myZoomLoader.class = edu.internet2.middleware.grouper.app.zoom.GrouperZoomLoader

# zoom full cron, default to every hour at 40 minutes after
# {valueType: "string"}
# otherJob.myZoomLoader.quartzCron = 0 40 * * * ?

# links to zoom config id
# {valueType: "string"}
# otherJob.myZoomLoader.zoomConfigId = myConfigId

# if loading groups from zoom to grouper
# {valueType: "boolean", defaultValue: "false"}
# otherJob.myZoomLoader.zoomLoadGroups = false

# if zoomLoadGroups is true then load groups into this folder
# {valueType: "string"}
# otherJob.myZoomLoader.zoomLoadGroupsFolderName = a:b

# if loading roles from zoom to grouper
# {valueType: "boolean", defaultValue: "false"}
# otherJob.myZoomLoader.zoomLoadRoles = false

# if zoomLoadRoles is true then load roles into this folder
# {valueType: "string"}
# otherJob.myZoomLoader.zoomLoadRolesFolderName = c:d

# if loading user types from zoom to grouper
# {valueType: "boolean", defaultValue: "false"}
# otherJob.myZoomLoader.zoomLoadUserTypes = false

# if zoomLoadUserTypes is true then load userTypes into this folder (v2.6.1+)
# {valueType: "string"}
# otherJob.myZoomLoader.zoomLoadUserTypesFolderName = d:e

# if loading users to the grouper_zoom_user table (v2.6.1+) 
# {valueType: "boolean", defaultValue: "false"}
# otherJob.myZoomLoader.zoomLoadUsersToTable = false

```
