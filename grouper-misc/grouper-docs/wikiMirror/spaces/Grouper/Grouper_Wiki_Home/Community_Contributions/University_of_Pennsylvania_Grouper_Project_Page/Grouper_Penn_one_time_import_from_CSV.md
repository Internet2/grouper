---
title: "Grouper Penn one-time import from CSV"
space: Grouper
pageId: 28544515
version: 4
lastUpdated: 2026-07-01T05:48:29.088Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544515/Grouper+Penn+one-time+import+from+CSV
---

This is a common admin activity, getting a CSV and doing a one time import into Grouper.

In this case we have a list of memberships by group extension and subject identifier:

1. Create the groups
2. Add members to the groups by subject identifier
3. Add the groups to an overall "users" group. This can be a loader job so new groups get added also

## Create the groups

CSV provided

Take the CSV, save as XLSX, and run the data filter to get unique group names

Make a formula for the group extensions to create these groups in a folder if not there

```
= "new GroupSave().assignName('penn:isc:ts:nnn:service:policy:" & A1 & "').assignCreateParentStemsIfNotExist(true).save();"
```

```
[tomcat@de6f95646bd0418c9af8c48af12a635d-3242894371 bin]$ ./gsh.sh createGroups.gsh
groovy:000> :load '/opt/grouper/grouperWebapp/WEB-INF/classes/groovysh.profile'
groovy:000> :gshFileLoad 'createGroups.gsh'
===> c8c9ba4b823c45308492240f8faa11f4,'GrouperSystem','application'
===> Group[name=penn:isc:ts:nnn:service:policy:nnnUsersDomain_aarc,uuid=1e87b7654a6d4bad83eee202cc3112d0]
===> Group[name=penn:isc:ts:nnn:service:policy:nnnUsersDomain_ac,uuid=2f955c8b8a884468866ef6cc97e96ef5]
===> Group[name=penn:isc:ts:nnn:service:policy:nnnUsersDomain_acap,uuid=9560fff1587f4a79a4c61fd588a122af]
===> Group[name=penn:isc:ts:nnn:service:policy:nnnUsersDomain_acasa,uuid=42f8dee730974f7896f79e8b30105244]
===> Group[name=penn:isc:ts:nnn:service:policy:nnnUsersDomain_accprogeny,uuid=917ed02527794cc78e1e6acc89bc8ced]

```

## Add members to the groups by subject identifier

This is the CSV, save as excel file

Make a forumula

```
= "new MembershipSave().assignGroupName('penn:isc:ts:nnn:service:policy:" & A2 & "').assignSubjectIdentifier('" & B2 & "').assignSubjectSourceId('pennperson').save();"
```

Copy the script

Run the script

Oops, some subjects in the file are subjectId and from a different subject source, so the script will be changed to this (because MembershipSave cant handle this case quite yet):

```
= "addMember('penn:isc:ts:nnn:service:policy:" & A2 & "', '" & B2 & "');"
```

```
[tomcat@de6f95646bd0418c9af8c48af12a635d-3242894371 bin]$ more assignMemberships.gsh 
GrouperSession.startRootSession();
addMember('penn:isc:ts:nnn:service:policy:nnnUsersDomain_aarc', 'ri***rd');
addMember('penn:isc:ts:nnn:service:policy:nnnUsersDomain_ac', 'al***1');
addMember('penn:isc:ts:nnn:service:policy:nnnUsersDomain_ac', 'a***o');
addMember('penn:isc:ts:nnn:service:policy:nnnUsersDomain_ac', 'a***h');
addMember('penn:isc:ts:nnn:service:policy:nnnUsersDomain_ac', 'c***s');
```

Run the script

```
[tomcat@de6f95646bd0418c9af8c48af12a635d-3242894371 bin]$ ./gsh.sh assignMemberships.gsh 
groovy:000> :load '/opt/grouper/grouperWebapp/WEB-INF/classes/groovysh.profile'
groovy:000> :gshFileLoad 'assignMemberships.gsh'
===> bf4ee877a49a452b98decb61ab254e66,'GrouperSystem','application'
===> true
===> true
===> true
```

## Add the groups to an overall "users" group

This can be a loader job so new groups get added also

Make a query about groups in the folder not including the loaded group

```
select
  gg.id as subject_id,
  'g:gsa' as subject_source_id
from
  grouper_groups gg,
  grouper_stems gs
where
  gg.parent_stem = gs.id
  and gs.name = 'penn:isc:ts:nnn:service:policy'
  and gg.name != 'penn:isc:ts:nnn:service:policy:nnnUsers'
```

Make a loader job on the users group
