---
title: "Organization hierarchies via the grouper loader"
space: Grouper
pageId: 28547893
version: 11
lastUpdated: 2026-07-01T05:45:56.780Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547893/Organization+hierarchies+via+the+grouper+loader
---

[Main Grouper Loader page](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545200/Grouper+Loader)

**NOTE:** Originally Penn used the fully qualified org path including ancesters. This worked fine for four years, but then the controller added a couple high level orgs. Our groups were named:

```

penn:community:employee:org:TOPU:UNIV:USCH:56XX:56YY:5600

```

and they were changed to:

```
			
penn:community:employee:org:TOPU:UNIV:UCAC:UUAC:USCH:56XX:56YY:5600

```

The loader proceeded to empty out and delete all of the org groups, and create the org groups in the new location. This means that applications that used the orgs now allowed no one in. In order to prevent this from happening again, we changed the orgs to not have the ancesters in the group path. like this:

```

penn:community:employee:org:5600:5600

```

Note that each org has its own folder in case there are supporting groups inside (like include/exclude lists). There are still roll-up orgs, but they are not structured in a hierarchy by group name. So the "org" loader hook is not needed, it is just some simple loader jobs.

Here is a demo that shows how to maintain organization charts with Grouper. Here are [notes about Penn's implementation](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28673892/Penn+organizational+hierarchy) of this

# Organizational hierarchies with org name in group name

Note: This is only recommended if you are sure the org structure will not change, or you are ok with change. Is most cases it is not recommended to put the org ancestor structure in the group name.

#### Movies

You need the free [xvid codec](http://www.xvid.org/) to see the movies. Also, pay no attention to my kids running around on the floor above me while I made the movies

- Movie 1
- Movie 2
- Movie 3
- Movie 4

#### Steps

First you need to load the test data, and register the hook, add these settings in the proper place in the grouper.properties file:

```
grouperIncludeExclude.use = true

hooks.loader.class=edu.internet2.middleware.grouper.hooks.examples.HierarchicalOrgLoaderHook

#####################################
## org management
#####################################

# if the orgs table(s) should be included in the DDL (includes the hierarchical table
orgs.includePocOrgsTablesInDdl = true

# loader connection of the database where orgs are (grouper means the grouper db in grouper.hibernate.properties)
orgs.databaseName = grouper

#table name of the org table (can prefix by schema name if you like)
orgs.orgTableName = grouperorgs_poc_orgs

#column names of this table
orgs.orgIdCol = id
orgs.orgNameCol = org_name
orgs.orgDisplayNameCol = org_display_name
orgs.orgParentIdCol = parent_id

#stem where the orgs are, e.g. poc:orgs
orgs.parentStemName = poc:orgs

#org config name
orgs.configGroupName = poc:orgs:orgsConfig

```

Then make sure to load the test subjects, and load the new DDL for the org proof of concept (here is the [subjects.sql](https://raw.githubusercontent.com/Internet2/grouper/master/grouper-qs-builder/subjects.sql) file):

```
gsh -registry -runsqlfile ..\subjects.sql
gsh -registry -runscript

```

Now you should see three new grouperorgs* tables, and 1 new view. Create the config groups, either with GSH or with the UI. Here are the GSH commands:

```
GSH_DEBUG=true
grouperSession = GrouperSession.startRootSession();
addRootStem("poc", "poc - proof of concept");
addStem("poc", "orgs", "Organizations");
addGroup("poc:orgs", "orgsConfig", "orgsConfig");
groupAddType("poc:orgs:orgsConfig", "grouperLoader");
setGroupAttr("poc:orgs:orgsConfig", "grouperLoaderDbName", "grouper");
setGroupAttr("poc:orgs:orgsConfig", "grouperLoaderQuartzCron", "0 46 6 * * ? ");
setGroupAttr("poc:orgs:orgsConfig", "grouperLoaderQuery", "select gh.org_hierarchical_sor_name as group_name, gpoa.subject_id from grouperorgs_hierarchical gh, grouperorgs_poc_org_assign gpoa where gh.org_id = gpoa.org_id");
setGroupAttr("poc:orgs:orgsConfig", "grouperLoaderScheduleType", "CRON");
setGroupAttr("poc:orgs:orgsConfig", "grouperLoaderType", "SQL_GROUP_LIST");
setGroupAttr("poc:orgs:orgsConfig", "grouperLoaderGroupQuery", "select gh.org_hierarchical_sor_name as group_name, gh.org_hierarchical_sor_disp_name as group_display_name, org_description as group_description from grouperorgs_hierarchical gh, grouperorgs_poc_orgs gpo where gh.org_id = gpo.id");
setGroupAttr("poc:orgs:orgsConfig", "grouperLoaderGroupsLike", "poc:orgs:%org%group_systemOfRecord");
setGroupAttr("poc:orgs:orgsConfig", "grouperLoaderGroupTypes", "addIncludeExclude");
addGroup("poc:orgs", "orgsAllConfig", "orgsAllConfig");
groupAddType("poc:orgs:orgsAllConfig", "grouperLoader");
setGroupAttr("poc:orgs:orgsAllConfig", "grouperLoaderDbName", "grouper");
setGroupAttr("poc:orgs:orgsAllConfig", "grouperLoaderQuartzCron", "0 06 7 * * ? ");
setGroupAttr("poc:orgs:orgsAllConfig", "grouperLoaderQuery", "select gam.group_name, ga.group_id as subject_id, ga.value from grouperorgs_all_members_v gam, grouper_attributes ga, grouper_fields gf where gf.type = 'attribute' and gf.name = 'name' and gf.id = ga.field_id and ga.value = gam.member_group_name");
setGroupAttr("poc:orgs:orgsAllConfig", "grouperLoaderScheduleType", "CRON");
setGroupAttr("poc:orgs:orgsAllConfig", "grouperLoaderType", "SQL_GROUP_LIST");
setGroupAttr("poc:orgs:orgsAllConfig", "grouperLoaderGroupQuery", "select gh.org_hier_all_sor_name as group_name, gh.org_hier_all_sor_display_name as group_display_name, org_hier_all_sor_description as group_description from grouperorgs_hierarchical gh, grouperorgs_poc_orgs gpo where gh.org_id = gpo.id and gh.org_hier_all_name is not null");
setGroupAttr("poc:orgs:orgsAllConfig", "grouperLoaderGroupsLike", "poc:orgs:%org%all_systemOfRecord");
setGroupAttr("poc:orgs:orgsAllConfig", "grouperLoaderGroupTypes", "addIncludeExclude");

```

Now run the jobs (note, run the allJob twice [one time only generally] since the first time it creates groups needed in the job):

```
grouperSession = GrouperSession.startRootSession();
group = GroupFinder.findByName(grouperSession, "poc:orgs:orgsConfig");
loaderRunOneJob(group);
allGroup = GroupFinder.findByName(grouperSession, "poc:orgs:orgsAllConfig");
loaderRunOneJob(allGroup);
loaderRunOneJob(allGroup);

```

#### Example at Penn

There is the grouper.properties hook config:

```

#implement a loader hook by extending edu.internet2.middleware.grouper.hooks.LoaderHooks
#hooks.loader.class=edu.yourSchool.it.YourSchoolLoaderHooks
hooks.loader.class=edu.internet2.middleware.grouper.hooks.examples.HierarchicalOrgLoaderHook

```

And the grouper.properties org management config:

```

#####################################
## org management
#####################################

# if the orgs table(s) should be included in the DDL (includes the hierarchical table
orgs.includePocOrgsTablesInDdl = true

# loader connection of the database where orgs are (grouper means the grouper db in grouper.hibernate.properties)
orgs.databaseName = grouper

#table name of the org table (can prefix by schema name if you like)
orgs.orgTableName = org_list_v

#column names of this table
orgs.orgIdCol = org_name
orgs.orgNameCol = org_name
orgs.orgDisplayNameCol = org_display_name
orgs.orgParentIdCol = parent_id

#stem where the orgs are, e.g. poc:orgs
orgs.parentStemName = penn:community:employee:org

#org config name
orgs.configGroupName = penn:community:employee:orgConfig

```

Basically, that hook will when the org loader job runs, and will populate the grouperorgs_hierarchical table. The org_list_v looks like this:

We need this hook and logic since we dont have the whole name of the group in one columns. This could also probably be done with an oracle connect by query, or a view with a procedural function. The generated (via the hook and view above) grouperorgs_hierarchical table looks like this:

Based on that we will make some views that load the groups and memberships. There are two loader jobs, one for the orgs, and one for the org rollups. Below is our orgGroupConfig (list of orgs)

Below is org_loader_person_meta_v view, which is based on in part by the grouperorgs_hierarchical table:

Here is the sanitized org_loader_person_v view:

Below is the org rollup config (adds child groups (not grandchild etc) to the parent. The grandchild is effectively a member since it is a cihld of the child...

Here is the org_loader_rollup_meta_v view:

Below is the org_loader_rollup2_v

sdf

#### To do:

- make orgs work with reset
- make sure deletes work with include/exclude
- unit test
- make possible to do multiple orgs lists
- add option to do include exclude or not
- add a way to put privileges on all groups
- auto-create groups which arent there...
- deprecate GroupSave.saveUnchecked()? 1.5
- test and document removing group since not in groupQuery
- why does loader keep saying "inserted 5 five memberships"
- do we need "all" groups if parents doint have assignments? Or even if they do?
- profile loader? do all in one query?
- add views to hsqldb
- stems are not in alphabetical order
- graceful loader veto?
