---
title: "Grouper recent memberships / grace periods"
space: Grouper
pageId: 28545165
version: 23
lastUpdated: 2026-07-12T05:34:53.188Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545165/Grouper+recent+memberships+grace+periods
---

A recent-membership group is a group that contains members who used to be in a certain group a certain number of days ago. You can have multiple recent-membership groups on one group to monitor. (the recentMembershipMarker attribute is assigned to the target group). If a subject is added back to the group to monitor, they will be removed from the recent-membership group. If you want to delegate the ability to assign recent-memberships (by default only admins can do this), just add privileges to the three recentMembership attribute definitions (attr read and attr update).

Note: this feature is based on any type of memberships not just immediate memberships.

If you will have a policy group that includes the eligible group and the grace period (recent-memberships) group, you might want the grace group to include the current memberships. The recent-memberships group could be the overall policy group. Then if the group is provisioned to a system, there will be no "flicker". Flicker is the brief period of time after a subject is removed from the eligible group until it is added to the recent-memberships group (could take a few minutes). If current memberships are included in the recent-memberships group then when a subject is removed from the group to monitor they will be not removed from the recent memberships group (until X days passes).

If you want a grace period group, and you want to know if eligible or in grace, and you don't want flicker, then you need three groups:

1. Eligible group (policy without considering grace period)
2. Grace period policy group: recent-memberships including current memberships (eligible group)
3. Grace period only group: composite of recent-memberships-with-eligible minus eligible).

## Create a new recent-memberships group

1. Create or navigate to the group which holds the recent memberships
2. Click on the More Tab → Loader, and configure a recent memberships loader
  
  
  
  1. Select the "from group"
  2. Select the "days of recent memberships". Note, this can be a decimal
  3. Identify if include current memberships
3. Wait a minute and if the change log is up to date the recent-memberships group will be created and populated
4. Nightly loader job will sync things up but it should be up to date in near real time using the change log
5. Note, if you remove these recent-memberships attributes, the recent-memberships group and members will still be there. You will need to delete the orphaned recent-memberships group

## Configuration

To disabled the loader job edit this in grouper.properties

```
############################################
## Recent-memberships
############################################

# If the recent-memberships loader job should be created and scheduled nightly at 3:41am.  It runs real time too so it shouldn't
# need to run frequently than daily
# {valueType: "boolean", defaultValue: "true"}
grouper.recentMemberships.loaderJob.enable = true

```

To edit the change log consumer edit this in grouper-loader.properties

```
# Recent-memberships consumer will update recent-memberships groups as memberships/attributes change
# {valueType: "class", mustExtendClass: "edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBase"}
changeLog.consumer.recentMemberships.class = edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer

# recent-memberships runs as change log consumer
# {valueType: "string"}
changeLog.consumer.recentMemberships.quartzCron = 

# if this many records happens in one change log session, just do a full loader job
# {valueType: "integer", defaultValue: "100"}
changeLog.consumer.recentMemberships.maxUntilFullSync = 100

# publishing class for recent-memberships
# {valueType: "class", mustExtendClass: "edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbMessagingPublisher"}
changeLog.consumer.recentMemberships.publisher.class = edu.internet2.middleware.grouper.app.serviceLifecycle.GrouperRecentMembershipsChangeLogConsumer

```

## **Internal structures**

- Configuration is stored in attributes on the "to" group
- In order to make the loader query more efficient and simpler to troubleshoot, a view consolidates the attributes into one simple place to get recent membership configuration
- This is synced to a table for performance reasons
- The grouper_time table is consulted to make this database agnostic
- A view on that table the PIT view will show the memberships to be loaded

## Script a recent membership group

Just change the first 4 params and it will do the rest

```
String groupName = "ref:employee60dayNotIncludeCurrent";
String sourceGroupName = "ref:employee";
boolean includeCurrent = false;
int numberOfDays = 60;
GrouperSession grouperSession = GrouperSession.startRootSession();
Group destinationGroup = new GroupSave(grouperSession).assignName(groupName).assignCreateParentStemsIfNotExist(true).save();
Group sourceGroup = GroupFinder.findByName(grouperSession, sourceGroupName, true);
long numberOfMicros = numberOfDays*24*60L*60*1000*1000L;
AttributeAssign attributeAssign = destinationGroup.getAttributeDelegate().assignAttributeByName("etc:attribute:recentMemberships:grouperRecentMembershipsMarker").getAttributeAssign();
attributeAssign.getAttributeValueDelegate().assignValueString("etc:attribute:recentMemberships:grouperRecentMembershipsGroupUuidFrom", sourceGroup.getId());
attributeAssign.getAttributeValueDelegate().assignValueString("etc:attribute:recentMemberships:grouperRecentMembershipsIncludeCurrent", includeCurrent ? "T" : "F");
attributeAssign.getAttributeValueDelegate().assignValueInteger("etc:attribute:recentMemberships:grouperRecentMembershipsMicros", numberOfMicros);

```

**See Also**

[Grouper Deprovisioning](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544732/Grouper+deprovisioning)
