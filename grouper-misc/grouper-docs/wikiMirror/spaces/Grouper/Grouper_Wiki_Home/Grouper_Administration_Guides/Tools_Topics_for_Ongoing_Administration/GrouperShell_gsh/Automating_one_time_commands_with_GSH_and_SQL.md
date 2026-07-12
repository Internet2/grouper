---
title: "Automating one-time commands with GSH and SQL"
space: Grouper
pageId: 28547354
version: 3
lastUpdated: 2026-07-01T05:47:01.115Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547354/Automating+one-time+commands+with+GSH+and+SQL
---

## Task

There are two existing groups for schools and centers, make a third. And make that group able to manage the other two groups

The two groups (there are more than this, this is an example:

This is what the third will look like (add LocalAdmins)

## SQL to generate GSH

Get the script with Sql (postgres)

```
select 'new GroupSave().assignName("' || prefix_name || 'LocalAdmins").assignDescription("' || name || ' admins in zoom who cannot change account settings").save();' || chr(10)
  || 'new PrivilegeGroupSave().assignField(AccessPrivilege.UPDATE.getField()).assignGroupName("' || prefix_name || 'LocalAdmins").assignSubjectIdentifier("' || prefix_name || 'LocalAdmins").save();' || chr(10)
  || 'new PrivilegeGroupSave().assignField(AccessPrivilege.READ.getField()).assignGroupName("' || prefix_name || 'LocalAdmins").assignSubjectIdentifier("' || prefix_name || 'LocalAdmins").save();' || chr(10)
  || 'new PrivilegeGroupSave().assignField(AccessPrivilege.UPDATE.getField()).assignGroupName("' || prefix_name || 'Admins").assignSubjectIdentifier("' || prefix_name || 'LocalAdmins").save();' || chr(10)
  || 'new PrivilegeGroupSave().assignField(AccessPrivilege.READ.getField()).assignGroupName("' || prefix_name || 'Admins").assignSubjectIdentifier("' || prefix_name || 'LocalAdmins").save();' || chr(10)
  || 'new PrivilegeGroupSave().assignField(AccessPrivilege.UPDATE.getField()).assignGroupName("' || prefix_name || 'Lsps").assignSubjectIdentifier("' || prefix_name || 'LocalAdmins").save();' || chr(10)
  || 'new PrivilegeGroupSave().assignField(AccessPrivilege.READ.getField()).assignGroupName("' || prefix_name || 'Lsps").assignSubjectIdentifier("' || prefix_name || 'LocalAdmins").save();' || chr(10)
|| 'new AttributeAssignSave(GrouperSession.staticGrouperSession()).assignOwnerGroupName("' || prefix_name || 'LocalAdmins").assignNameOfAttributeDefName("penn:etc:attribute:membershipRequirement:membershipRequirementEmployee").save();' || chr(10)
|| 'new AttributeAssignSave(GrouperSession.staticGrouperSession()).assignOwnerGroupName("' || prefix_name || 'Admins").assignNameOfAttributeDefName("penn:etc:attribute:membershipRequirement:membershipRequirementEmployee").save();' || chr(10)
|| 'new AttributeAssignSave(GrouperSession.staticGrouperSession()).assignOwnerGroupName("' || prefix_name || 'Lsps").assignNameOfAttributeDefName("penn:etc:attribute:membershipRequirement:membershipRequirementEmployee").save();'
 as script
from (select substring(substring(name, 1, LENGTH(name) - 6), 
length('penn:isc:ait:apps:zoom:security:schoolCenterAdminsAndLsps:zoom')+1, length(name) - 6 )
as name, substring(name, 1, length(name)-6) as prefix_name, 'penn:isc:ait:apps:zoom:security:schoolCenterAdminsAndLsps:zoom' as prefix from grouper_groups gg 
where name like 'penn:isc:ait:apps:zoom:security:schoolCenterAdminsAndLsps:zoom%Admins'
and name not like '%Local%'
order by 1) as name_prefixes;
```

Result (clean it up if there are double quotes after newlines)

```
new GroupSave().assignName("penn:isc:ait:apps:zoom:security:schoolCenterAdminsAndLsps:zoomAscLocalAdmins").assignDescription("Asc admins in zoom who cannot change account settings").save();
new PrivilegeGroupSave().assignField(AccessPrivilege.UPDATE.getField()).assignGroupName("penn:isc:ait:apps:zoom:security:schoolCenterAdminsAndLsps:zoomAscLocalAdmins").assignSubjectIdentifier("penn:isc:ait:apps:zoom:security:schoolCenterAdminsAndLsps:zoomAscLocalAdmins").save();
new PrivilegeGroupSave().assignField(AccessPrivilege.READ.getField()).assignGroupName("penn:isc:ait:apps:zoom:security:schoolCenterAdminsAndLsps:zoomAscLocalAdmins").assignSubjectIdentifier("penn:isc:ait:apps:zoom:security:schoolCenterAdminsAndLsps:zoomAscLocalAdmins").save();
new PrivilegeGroupSave().assignField(AccessPrivilege.UPDATE.getField()).assignGroupName("penn:isc:ait:apps:zoom:security:schoolCenterAdminsAndLsps:zoomAscAdmins").assignSubjectIdentifier("penn:isc:ait:apps:zoom:security:schoolCenterAdminsAndLsps:zoomAscLocalAdmins").save();
new PrivilegeGroupSave().assignField(AccessPrivilege.READ.getField()).assignGroupName("penn:isc:ait:apps:zoom:security:schoolCenterAdminsAndLsps:zoomAscAdmins").assignSubjectIdentifier("penn:isc:ait:apps:zoom:security:schoolCenterAdminsAndLsps:zoomAscLocalAdmins").save();
new PrivilegeGroupSave().assignField(AccessPrivilege.UPDATE.getField()).assignGroupName("penn:isc:ait:apps:zoom:security:schoolCenterAdminsAndLsps:zoomAscLsps").assignSubjectIdentifier("penn:isc:ait:apps:zoom:security:schoolCenterAdminsAndLsps:zoomAscLocalAdmins").save();
new PrivilegeGroupSave().assignField(AccessPrivilege.READ.getField()).assignGroupName("penn:isc:ait:apps:zoom:security:schoolCenterAdminsAndLsps:zoomAscLsps").assignSubjectIdentifier("penn:isc:ait:apps:zoom:security:schoolCenterAdminsAndLsps:zoomAscLocalAdmins").save();
new AttributeAssignSave(GrouperSession.staticGrouperSession()).assignOwnerGroupName("penn:isc:ait:apps:zoom:security:schoolCenterAdminsAndLsps:zoomAscLocalAdmins").assignNameOfAttributeDefName("penn:etc:attribute:membershipRequirement:membershipRequirementEmployee").save();
new AttributeAssignSave(GrouperSession.staticGrouperSession()).assignOwnerGroupName("penn:isc:ait:apps:zoom:security:schoolCenterAdminsAndLsps:zoomAscAdmins").assignNameOfAttributeDefName("penn:etc:attribute:membershipRequirement:membershipRequirementEmployee").save();
new AttributeAssignSave(GrouperSession.staticGrouperSession()).assignOwnerGroupName("penn:isc:ait:apps:zoom:security:schoolCenterAdminsAndLsps:zoomAscLsps").assignNameOfAttributeDefName("penn:etc:attribute:membershipRequirement:membershipRequirementEmployee").save();
new GroupSave().assignName("penn:isc:ait:apps:zoom:security:schoolCenterAdminsAndLsps:zoomBusinessServicesLocalAdmins").assignDescription("BusinessServices admins in zoom who cannot change account settings").save();
new PrivilegeGroupSave().assignField(AccessPrivilege.UPDATE.getField()).assignGroupName("penn:isc:ait:apps:zoom:security:schoolCenterAdminsAndLsps:zoomBusinessServicesLocalAdmins").assignSubjectIdentifier("penn:isc:ait:apps:zoom:security:schoolCenterAdminsAndLsps:zoomBusinessServicesLocalAdmins").save();
new PrivilegeGroupSave().assignField(AccessPrivilege.READ.getField()).assignGroupName("penn:isc:ait:apps:zoom:security:schoolCenterAdminsAndLsps:zoomBusinessServicesLocalAdmins").assignSubjectIdentifier("penn:isc:ait:apps:zoom:security:schoolCenterAdminsAndLsps:zoomBusinessServicesLocalAdmins").save();
new PrivilegeGroupSave().assignField(AccessPrivilege.UPDATE.getField()).assignGroupName("penn:isc:ait:apps:zoom:security:schoolCenterAdminsAndLsps:zoomBusinessServicesAdmins").assignSubjectIdentifier("penn:isc:ait:apps:zoom:security:schoolCenterAdminsAndLsps:zoomBusinessServicesLocalAdmins").save();
new PrivilegeGroupSave().assignField(AccessPrivilege.READ.getField()).assignGroupName("penn:isc:ait:apps:zoom:security:schoolCenterAdminsAndLsps:zoomBusinessServicesAdmins").assignSubjectIdentifier("penn:isc:ait:apps:zoom:security:schoolCenterAdminsAndLsps:zoomBusinessServicesLocalAdmins").save();
...
```

Add "GrouperSession.startRootSession();" and run that script
