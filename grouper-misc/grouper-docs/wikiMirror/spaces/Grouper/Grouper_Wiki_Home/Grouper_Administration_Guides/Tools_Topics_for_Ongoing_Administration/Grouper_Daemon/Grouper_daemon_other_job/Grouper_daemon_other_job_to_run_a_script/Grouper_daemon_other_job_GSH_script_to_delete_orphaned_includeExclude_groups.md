---
title: "Grouper daemon \"other job\" GSH script to delete orphaned includeExclude groups"
space: Grouper
pageId: 28560026
version: 7
lastUpdated: 2026-07-12T04:55:59.676Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560026/Grouper+daemon+other+job+GSH+script+to+delete+orphaned+includeExclude+groups
---

The request is to have a job that deleted include/excludes when the main group is deleted from the loader. These examples are Oracle but can be easily translated to another database

Lets get some test data and make some include/exclude

grouper.properties

```
grouperIncludeExclude.use = true
```

Bounce UI

Create some groups and add include exclude

Delete some of the groups

Create a view of main group names and system of record group names

```
create view delete_incl_excl_main_groups_v as
select id as group_id, name as group_name, name || '_systemOfRecord' as systemOfRecord_group_name
from grouper_groups gg where gg.name like 'test:%'
and name like 'test:%' and exists (select count(1) from grouper_attr_asn_group_v gaagv 
where attribute_def_name_name = 'etc:legacy:attribute:legacyGroupType_addIncludeExclude' and gg.id = gaagv.group_id)
and (name not like '%_includes' and name not like '%_excludes' and name not like '%_systemOfRecord' and name not like '%_systemOfRecordAndIncludes');
```

Create a view of non main groups

```
create view delete_incl_excl_nonmain_gr_v as
select id as meta_group_id, name as meta_group_name, 
case 
  when name like '%_includes' then substr(name, 1, length(name) - length('_includes'))
  when name like '%_excludes' then substr(name, 1, length(name) - length('_excludes'))
  when name like '%_systemOfRecord' then substr(name, 1, length(name) - length('_systemOfRecord'))
  when name like '%_systemOfRecordAndIncludes' then substr(name, 1, length(name) - length('_systemOfRecordAndIncludes'))
end as source_group_name
from grouper_groups 
where name like 'test:%' and (name like '%_includes' or name like '%_excludes' or name like '%_systemOfRecord' or name like '%_systemOfRecordAndIncludes');
```

Query of main group ids to delete

```
select group_id from delete_incl_excl_main_groups_v where not exists (select 1 from grouper_groups gg where gg.name = systemOfRecord_group_name)
```

Query of non–main group ids to delete

```
select meta_group_id from delete_incl_excl_nonmain_gr_v where not exists (select 1 from grouper_groups gg where gg.name = source_group_name)
```

GSH to delete main groups (to delete), then non-main groups (to delete)

```
grouperSession = GrouperSession.startRootSession();
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
groups = new ArrayList();
groupIds = new GcDbAccess().sql("select group_id from delete_incl_excl_main_groups_v where not exists (select 1 from grouper_groups gg where gg.name = systemOfRecord_group_name)").selectList(String.class);
for (i=0;i<groupIds.size();i++) {groups.add(GroupFinder.findByUuid(grouperSession, groupIds.get(i), true));}
for (i=0;i<groups.size();i++) { groups.get(i).delete(); OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().addDeleteCount(1);  } 
groups.clear();
groupIds = new GcDbAccess().sql("select meta_group_id from delete_incl_excl_nonmain_gr_v where not exists (select 1 from grouper_groups gg where gg.name = source_group_name)").selectList(String.class);
for (i=0;i<groupIds.size();i++) {groups.add(GroupFinder.findByUuid(grouperSession, groupIds.get(i), true));}
for (i=0;i<groups.size();i++) { groups.get(i).delete(); OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().addDeleteCount(1);  } 
```

Put that in one GSH script if not in a gsh file in the container...

grouper-loader.properties (note, the script source is all one line, each linebreak signified with $newline$). Again, if you want this in a script file mounted in your container or inn your subimage thats fine too and easier to read)

```
otherJob.deleteIncludeExcludeGsh.class = edu.internet2.middleware.grouper.app.loader.OtherJobScript
otherJob.deleteIncludeExcludeGsh.quartzCron = 0 13 * * * ?
otherJob.deleteIncludeExcludeGsh.scriptType = gsh
otherJob.deleteIncludeExcludeGsh.scriptSource = grouperSession = GrouperSession.startRootSession();$newline$import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;$newline$groups = new ArrayList();$newline$groupIds = new GcDbAccess().sql("select group_id from delete_incl_excl_main_groups_v where not exists (select 1 from grouper_groups gg where gg.name = systemOfRecord_group_name)").selectList(String.class);$newline$for (i=0;i<groupIds.size();i++) {groups.add(GroupFinder.findByUuid(grouperSession, groupIds.get(i), true));}$newline$for (i=0;i<groups.size();i++) { groups.get(i).delete(); OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().addDeleteCount(1);  } $newline$groups.clear();$newline$groupIds = new GcDbAccess().sql("select meta_group_id from delete_incl_excl_nonmain_gr_v where not exists (select 1 from grouper_groups gg where gg.name = source_group_name)").selectList(String.class);$newline$for (i=0;i<groupIds.size();i++) {groups.add(GroupFinder.findByUuid(grouperSession, groupIds.get(i), true));}$newline$for (i=0;i<groups.size();i++) { groups.get(i).delete(); OtherJobScript.retrieveFromThreadLocal().getOtherJobInput().getHib3GrouperLoaderLog().addDeleteCount(1);  } 

```

If you save that as grouper-loader.properties and import into the configuration editor it will get loaded into the database

You need to have Grouper pick up the new jobs

Then you can manually run the job and see some deletes

See the right number of deletes

See the correct groups in the registry
