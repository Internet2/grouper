---
title: "Penn organizational hierarchy - legacy version before improvements"
space: Grouper
pageId: 28555169
version: 5
lastUpdated: 2026-07-01T05:38:48.053Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555169/Penn+organizational+hierarchy+-+legacy+version+before+improvements
---

This is Penn's experience implementing the Grouper organization hierarchy.

This is the size of our org implementation:

- 27,000 people in orgs at Penn
- 2,200 orgs (700 leaf nodes, 1500 rollup nodes)
- 3,000 org groups (more due to include/exclude lists)
- 500,000 org memberships (there are a lot due to the rollups, and include/exclude lists)

Performance:

- Org loader: 1 minute 20 seconds (after the first load), which equates to 630 groups and 24000 immediate memberships per minute
- Rollup loader: 1 minute 40 seconds (after first load), which equates to 1000 groups per minute
- Center loader: 40 seconds
- Consultant loader (currently one group managed): 4 seconds

#### General approach

- We have a view of orgs and parent id's, and a view of assignments of org to person
- Since we want the orgs in a tree structure in Grouper, and the loader expects the group_name matched with person id, we use the built-in grouper hook on pre-run of loader job, to take the org table and calculate the hierarchical group names. You could do this part a variety of ways, including a db function (e.g. PLSQL for oracle)
  
  - The builtin hook keeps the group names in grouperorgs_hierarchical table (ddlutils will auto-create this table). The most important part of this table is the parent stem name col
- There are a few loader jobs:
  
  - One that manages all the leaf node orgs groups (assigns people to org groups)
  - One that manages all the non-leaf rollup nodes (assigns orgs to parent org groups)
  - One that manages Penn "centers" which are our high level organizations (assigns direct center children orgs to center groups)
  - One that manages contractor groups. We dont have contractors assigned in our org structure, so we manage them through attributes in our person database. Then we can manually make other groups next to the org rollup which includes the org rollup and the contractor group
- We will run all these jobs once daily after our payroll jobs run
- Grouper include/exclude automatically creates include and exclude lists which are tacked on to the system of record group. This is useful in loader jobs since the loader system of record list is resynced with the DB query with each run, so manual changes will be undone. Penn actually only needs "include" lists, not exclude at this point (use case is VP's are in a different org than the org they manage, so we want to add them in). So we will have include/exclude groups on the rollup groups, the centers groups, and the contractor groups. We dont think we will need them on the leaf nodes, though if we have a need later, we can add it in. The reason not to do this is performance, it creates a bunch more groups and memberships.

Here are the steps to creating a loader SQL_GROUP_LIST job:

- First of all, you should prefer using views, and simply put simple select from the view in the loader config. It is also easy to tell what the loader is going to do without hunting through the loader configs, and easy to make changes at runtime (though I believe the loader allows runtime query changes as well)
- Note: if you are using include/exclude, then the group names should have the system of record suffix which is configured in the grouper.properties
- Make a view of groups where each row represents a group.
  
  - There is a col for the group name, display name (optional), description (optional), and security (e.g. readers, viewers, etc: optional)
  - This view will set these attributes of group and auto-create groups which have no members (might be useful for orgs since apps can refer to groups which have no members)
  - Note: if you can give groups a unique suffix in the stem structure, then the job can use the setting "grouperLoaderGroupsLike" which will delete groups which are no longer in the group list
- Make the query which returns the subjectId (and sourceId if not the default loader source), and group name
  
  - For leaf nodes, this is generally a simple query that assigns people to org groups
  - For rollup nodes, this can be a little complex.
    
    - First of all, you might union the direct rollup children with the direct rollup leaf nodes
    - The subjectId for groups is the group_id. for Grouper 1.4, you can join to the grouper_attributes table. For 1.5 you can simply join to the grouper_groups table. In both, you can join to grouper_groups_v if you like. What I did for 1.4 is: grouper_attributes ga, grouper_fields gf where gf.NAME = 'name' AND gf.ID = ga.field_id and ga.VALUE = ocrv.MEMBER_GROUP_NAME. I would definitely keep these query in a view.
    - You should also specify the source id for groups: 'g:gsa' as SUBJECT_SOURCE_ID
- Configure the config group for each loader job. I put this next to the top level loaded stem. I generally do this in GSH, though you could also do this in the UI
- Kick off the loader job manually in GSH so you can verify the results without waiting for the cron to run
- Restart your loader so it picks up the new job

#### Person orgs (leaf nodes)

- Lets make a function which strips out special chars:

```
CREATE OR REPLACE PACKAGE AUTHZADM.authzadm_pkg
AS

   FUNCTION remove_special_chars (the_input varchar2)
      RETURN varchar2;

END authzadm_pkg;
/

CREATE OR REPLACE PACKAGE BODY AUTHZADM.authzadm_pkg
AS

   FUNCTION remove_special_chars (the_input varchar2)
      RETURN varchar2
   AS
     the_string varchar2(4000);
   BEGIN
     --take out anything not alphanumeric, space, underscore, or dash
     the_string := REGEXP_REPLACE(the_input, '[^a-zA-Z0-9 _\-]', '');
     the_string := trim(the_string);
     return the_string;
   END;

END authzadm_pkg;
/

```

* Implement the view of orgs. Note, in the view you can easily use unions in the sql to end the hierarchy at a different node. At first we were going to do this, then we decided against it. However, you will see that we do filter out certain branches and nodes. Also note we shorten the names of some top level nodes.

```
CREATE OR REPLACE VIEW ORG_LIST_V
(ORG_NAME, ORG_DISPLAY_NAME, ORG_DESCRIPTION, PARENT_ID, PAYROLL_FLAG,
 CENTER_CODE, CENTER_NAME, center_code_assign)
AS
select trim(organization_code) org_name,
decode(organization_code,      'UNIV',      'Penn',
    'NOTU',     'Not Acad',
    'TOPU',     'Top',
    authzadm_pkg.remove_special_chars(nvl(oc.description, oc.ORG_SHORT_NAME))) org_display_name,
authzadm_pkg.remove_special_chars(nvl(oc.description, oc.ORG_SHORT_NAME)) org_description,
trim(parent_org_code)  parent_id,
oc.PAYROLL_FLAG,
authzadm_pkg.remove_special_chars(oc.CENTER_CODE) center_code, authzadm_pkg.remove_special_chars(oc.CENTER_NAME) center_name,
/* if the parent is in the same center, dont list it, only list it if the parent is in a different center */
(select authzadm_pkg.remove_special_chars(oc.CENTER_CODE) from diradmin.dir_org_codes oc2
where oc.PARENT_ORG_CODE = oc2.ORGANIZATION_CODE and oc.CENTER_CODE != oc2.CENTER_CODE ) as center_code_assign
from diradmin.dir_org_codes oc where oc.ENABLED = 'Y'
and oc.organization_code not in ( 'DEAD', 'BUD5', 'RADA', 'T', 'CAOP')
and oc.PARENT_ORG_CODE not in ( 'DEAD', 'BUD5', 'RADA', 'T', 'CAOP')
 and oc.description is not null

```

- This shows the following data (2200 rows)
  
  
  ```
  ORG_NAME ORG_DISPLAY_NAME                       ORG_DESCRIPTION                      PARENT_ID PAYROLL_FLAG CENTER_CODE CENTER_NAME
  78YY     ACP Other Parent                       ACP Other Parent                     78XX      N            78          Audit Compliance and Privacy
  9985     AG-Center for School Study Councils    AG-Center for School Study Councils  AG32      N            99          External Organizations (Agency Funds)
  4602     AG-Institute on Aging                  AG-Institute on Aging                IAGE      Y            40          School of Medicine
  IAGE     AG-Institute on Aging Parent           AG-Institute on Aging Parent         SOMI      N            40          School of Medicine
  
  ```
- Here is the view which assigns people to orgs. This view makes sure the person has at least one active job in that org (doesnt have to be the primary job)

```
CREATE OR REPLACE VIEW ORG_ASSIGN_V
(ORG_CODE, PENN_ID)
AS
Select distinct p.job_org_code org_code, a.v_penn_id penn_id
from comadmin.ssn4_affiliation_view a, comadmin.pennpay_appointment_v p
Where a.v_penn_id = p.penn_id And a.v_source = 'PENNPAY' and a.V_ACTIVE_CODE = 'A'
and p.JOB_ACTIVE_CODE = 'A'

```

* This will give the following data (cleansed). The penn_id is the subject_id of the person

```
ORG_CODE    PENN_ID
0007        12345678
8105        12345679

```

* Now configure the grouper.properties. Add the hook, and the org management section

```
hooks.loader.class=edu.internet2.middleware.grouper.hooks.examples.HierarchicalOrgLoaderHook

```

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

* Add the tables:

```
[appadmin@lorenzo bin]$ ./gsh.sh -registry -check
Using GROUPER_HOME: /opt/appserv/tomcat_3c/webapps/fastGrouperProdDaemon/WEB-INF/bin/..
Using GROUPER_CONF: /opt/appserv/tomcat_3c/webapps/fastGrouperProdDaemon/WEB-INF/bin/../classes
Using JAVA: /opt/appserv/java5/bin/java
using MEMORY: 64m-512m
Grouper starting up: version: 1.4.2, build date: 2009/05/19 16:13:03, env: PROD
grouper.properties read from: /opt/appserv/tomcat_3c/webapps/fastGrouperProdDaemon/WEB-INF/classes/grouper.properties
Grouper current directory is: /opt/appserv/tomcat_3c/webapps/fastGrouperProdDaemon/WEB-INF/bin
log4j.properties read from:   /opt/appserv/tomcat_3c/webapps/fastGrouperProdDaemon/WEB-INF/classes/log4j.properties
Grouper is logging to file:   /opt/appserv/tomcat_3c/logs/fastGrouper/grouper_error.log, at min level WARN for package: edu.internet2.middleware.grouper, based on log4j.properties
grouper.hibernate.properties: /opt/appserv/tomcat_3c/webapps/fastGrouperProdDaemon/WEB-INF/classes/grouper.hibernate.properties
grouper.hibernate.properties: schema@jdbc:oracle:thin:@dbserver.whatever.whatever:1521:sid
sources.xml read from:        /opt/appserv/tomcat_3c/webapps/fastGrouperProdDaemon/WEB-INF/classes/sources.xml
sources.xml jdbc source id:   pennperson: GrouperJdbcConnectionProvider
sources.xml groupersource id: g:gsa
sources.xml jdbc source id:   servPrinc: GrouperJdbcConnectionProvider
(note, might need to type in your response multiple times (Java stdin is flaky))
(note, you can allow and deny db urls and users in the grouper.properties)
Are you sure you want to schemaexport all tables (dropThenCreate=F,writeAndRunScript=F) in db user 'schema', db url 'jdbc:oracle:thin:@dbserver.whatever.whatever:1521:sid'? (y|n):
y
Continuing...
Grouper ddl object type 'GrouperOrg' has dbVersion: 0 and java version: 1
Grouper database schema DDL requires updates
(should run script manually and carefully, in sections, verify data before drop statements, backup/export important data before starting, follow change log on confluence, dont run exact same script in multiple envs - generate a new one for each env),
script file is:
/opt/appserv/tomcat_3c/webapps/fastGrouperProdDaemon/WEB-INF/ddlScripts/grouperDdl_20090519_16_44_25_124.sql
Note: this script was not executed due to option passed in
To run script via gsh, carefully review it, then run this:
gsh -registry -runsqlfile /opt/appserv/tomcat_3c/webapps/fastGrouperProdDaemon/WEB-INF/ddlScripts/grouperDdl_20090519_16_44_25_124.sql

```

* Now I will carefully inspect and run the sql file, which adds the hierarchy tables in grouper (grouperorg tables and view)

```
[appadmin@lorenzo bin]$ ./gsh.sh -registry -runsqlfile /opt/appserv/tomcat_3c/webapps/fastGrouperProdDaemon/WEB-INF/ddlScripts/grouperDdl_20090519_16_44_25_124.sql

```

* Make a view about the person org metadata

```
CREATE OR REPLACE VIEW ORG_LOADER_PERSON_META_V
(GROUP_NAME, group_display_name, readers, viewers, org_id)
AS
select distinct gh.ORG_HIERARCHICAL_STEM || ':' || gh.ORG_ID || '_personorg' as group_name,
gh.ORG_HIERARCHICAL_STEM || ' - ' || olv.ORG_DISPLAY_NAME || ':' || gh.ORG_ID || ' - ' || olv.ORG_DISPLAY_NAME as group_display_name,
'penn:community:employee:orgSecurity:orgReaders' as readers,
'penn:community:employee:orgSecurity:orgViewers' as viewers,
gh.org_id as org_id
from grouperorgs_hierarchical gh, org_list_v olv
where gh.ORG_ID = olv.ORG_NAME
and olv.PAYROLL_FLAG = 'Y' order by 2

```

* This data looks like this

```
GROUP_NAME                                                                GROUP_DISPLAY_NAME                                                                                                                                                             READERS                                         VIEWERS                                         ORG_ID
penn:community:employee:org:TOPU:NOTU:HCAG:21XX:2100:2100_personorg       penn:community:employee:org:TOPU:NOTU:HCAG:21XX:2100 - Health System:2100 - Health System                                                                                      penn:community:employee:orgSecurity:orgReaders  penn:community:employee:orgSecurity:orgViewers  2100
penn:community:employee:org:TOPU:NOTU:HCAG:21XX:2101:2101_personorg       penn:community:employee:org:TOPU:NOTU:HCAG:21XX:2101 - Hospital of the University of Pennsylvania:2101 - Hospital of the University of Pennsylvania	                         penn:community:employee:orgSecurity:orgReaders  penn:community:employee:orgSecurity:orgViewers  2101
penn:community:employee:org:TOPU:NOTU:HCAG:99XX:AG02:9931:9931_personorg  penn:community:employee:org:TOPU:NOTU:HCAG:99XX:AG02:9931 - Organic Letters Journal:9931 - Organic Letters Journal	                                                         penn:community:employee:orgSecurity:orgReaders  penn:community:employee:orgSecurity:orgViewers  9131
penn:community:employee:org:TOPU:NOTU:HCAG:99XX:AG02:9979:9979_personorg  penn:community:employee:org:TOPU:NOTU:HCAG:99XX:AG02:9979 - American Academy of Political and Social Science:9979 - American Academy of Political and Social Science	         penn:community:employee:orgSecurity:orgReaders  penn:community:employee:orgSecurity:orgViewers  9979
penn:community:employee:org:TOPU:NOTU:HCAG:99XX:AG87:9940:9940_personorg  penn:community:employee:org:TOPU:NOTU:HCAG:99XX:AG87:9940 - Hillel Foundation:9940 - Hillel Foundation	                                                                 penn:community:employee:orgSecurity:orgReaders  penn:community:employee:orgSecurity:orgViewers  9940

```

* Penn has two types of orgs: orgs that hold people, and orgs that dont. So I will create them separately, here is a view of orgs that hold people, that the loader will use. Note: these will not be include/exclude since we only need that on the higher level groups (rollups). Note, each group here should end in _personorg so we can know which groups are managed by this loader process.

```
CREATE OR REPLACE VIEW ORG_LOADER_PERSON_V
(GROUP_NAME, SUBJECT_ID)
AS
select distinct olpmv.GROUP_NAME , oav.PENN_ID as subject_id
from ORG_LOADER_PERSON_META_V olpmv, org_assign_v oav
where olpmv.org_id = oav.ORG_CODE

```

- Add the config group. Note, there are no org members yet (1=0), so I can inspect the grouperorgs_hierarchical table
  
  
  ```
  [appadmin@lorenzo bin]$ ./gsh.sh
  Type help() for instructions
  gsh 0% GSH_DEBUG=true
  true
  gsh 1% grouperSession = GrouperSession.startRootSession();
  edu.internet2.middleware.grouper.GrouperSession: 9702ff7015a84c019ae58ce6ae950115,'GrouperSystem','application'
  gsh 2% stem = StemFinder.findByName(grouperSession, "penn:community:employee");
  stem: name='penn:community:employee' displayName='penn:community:employee' uuid='3cb63130-03e1-4b60-8f01-1454ee3c9588'
  gsh 4% group = addGroup("penn:community:employee", "orgConfig", "orgConfig");
  group: name='penn:community:employee:orgConfig' displayName='penn:community:employee:orgConfig' uuid='f36a72d38053405d997ee8fc6eb66ff4'
  gsh 5% groupAddType("penn:community:employee:orgConfig", "grouperLoader");
  true
  gsh 6% setGroupAttr("penn:community:employee:orgConfig", "grouperLoaderDbName", "grouper");
  true
  gsh 7% setGroupAttr("penn:community:employee:orgConfig", "grouperLoaderQuartzCron", "0 46 6 * * ? ");
  true
  gsh 8% setGroupAttr("penn:community:employee:orgConfig", "grouperLoaderQuery", "select group_name, subject_id from org_loader_person_v where 1=0");
  true
  gsh 9% setGroupAttr("penn:community:employee:orgConfig", "grouperLoaderScheduleType", "CRON");
  true
  gsh 10% setGroupAttr("penn:community:employee:orgConfig", "grouperLoaderType", "SQL_GROUP_LIST");
  true
  
  ```
  
  * Run the job
  
  
  ```
  [appadmin@lorenzo bin]$ ./gsh.sh
  Type help() for instructions
  gsh 0% grouperSession = GrouperSession.startRootSession();
  edu.internet2.middleware.grouper.GrouperSession: 6e1432f7de314aeca2f927f939f1a5be,'GrouperSystem','application'
  gsh 1% group = GroupFinder.findByName(grouperSession, "penn:community:employee:orgConfig");
  group: name='penn:community:employee:orgConfig' displayName='penn:community:employee:orgConfig' uuid='f36a72d38053405d997ee8fc6eb66ff4'
  gsh 2% loaderRunOneJob(group);
  loader ran successfully, inserted 0 memberships, deleted 0 memberships, total membership count: 0
  
  ```
  
  * Inspect the grouperorgs_hierarchy table. Note, there were some problems, so we adding the function to strip bad chars and trim the data... also adjust the org_loader_person_v (so the names and everything are ok). Here is what the org_loader_person_v looks like (person data scrubbed)
  
  
  ```
  GROUP_NAME                                                                      SUBJECT_ID
  penn:community:employee:org:TOPU:UNIV:USCH:51XX:DPDN:5188:5188_personorg        12345678
  penn:community:employee:org:TOPU:UNIV:USTU:85XX:CRSC:ASPP:8508:8508_personorg   12345679
  penn:community:employee:org:TOPU:UNIV:USCH:36XX:APPC:3604:3604_personorg        12345680
  penn:community:employee:org:TOPU:UNIV:USCH:02XX:GRAD:GRAO:0315:0315_personorg   12345681
  
  ```
  
  * Add the group query, and fix the member query (take out 1=0)
  
  
  ```
  gsh 5% setGroupAttr("penn:community:employee:orgConfig", "grouperLoaderQuery", "select group_name, subject_id from org_loader_person_v");
  true
  gsh 6% setGroupAttr("penn:community:employee:orgConfig", "grouperLoaderGroupQuery", "select olpmv.GROUP_NAME as group_name, olpmv.GROUP_DISPLAY_NAME as group_display_name, olpmv.READERS, olpmv.VIEWERS, olpmv.ORG_ID from ORG_LOADER_PERSON_META_V olpmv");
  true
  gsh 7% setGroupAttr("penn:community:employee:orgConfig", "grouperLoaderGroupsLike", "penn:community:employee:org:%_personorg");
  true
  gsh 8% loaderRunOneJob(group);
  
  ```

- This created 736 org groups with 33k members

#### Rollup orgs

- Now we need a meta view which has information about the rollup group: note the top two levels are filtered out
  
  
  ```
  CREATE OR REPLACE VIEW ORG_LOADER_ROLLUP_META_V
  (GROUP_NAME, GROUP_DISPLAY_NAME, GROUP_DESCRIPTION, READERS, VIEWERS,
   ORG_ID, GROUP_OVERALL_NAME, PARENT_ID)
  AS
  select distinct gh.ORG_HIERARCHICAL_STEM || ':' || gh.ORG_ID || '_rolluporg_systemOfRecord' as group_name,
  gh.ORG_HIERARCHICAL_STEM || ' - ' || olv.ORG_DISPLAY_NAME || ':' || gh.ORG_ID || ' - ' || olv.ORG_DISPLAY_NAME || ' system of record' as group_display_name,
  gh.ORG_HIER_ALL_SOR_DESCRIPTION as group_description,
  'penn:community:employee:orgSecurity:orgReaders' as readers,
  'penn:community:employee:orgSecurity:orgViewers' as viewers,
  gh.org_id as org_id,
  gh.ORG_HIERARCHICAL_STEM || ':' || gh.ORG_ID || '_rolluporg' as group_overall_name,
  olv.PARENT_ID
  from grouperorgs_hierarchical gh, org_list_v olv
  where gh.ORG_ID = olv.ORG_NAME and gh.ORG_HIER_ALL_NAME is not null
  and olv.PAYROLL_FLAG = 'N' and olv.ORG_NAME not in ('TOPU', 'UNIV', 'NOTU') order by 2
  
  ```
  
  * This has data which looks like this:
  
  
  ```
  GROUP_NAME                                                                          GROUP_DISPLAY_NAME                                                                                                                                                           GROUP_DESCRIPTION	                                  READERS                                         VIEWERS                                         ORG_ID  GROUP_OVERALL_NAME
  penn:community:employee:org:TOPU:NOTU:HCAG:HCAG_rolluporg_systemOfRecord            penn:community:employee:org:TOPU:NOTU:HCAG - Health Care and Agencies:HCAG - Health Care and Agencies system of record                                                       Members of HCAG and all groups underneath the hierarchy  penn:community:employee:orgSecurity:orgReaders  penn:community:employee:orgSecurity:orgViewers  HCAG    penn:community:employee:org:TOPU:NOTU:HCAG:HCAG_rolluporg
  penn:community:employee:org:TOPU:NOTU:HCAG:21XX:21XX_rolluporg_systemOfRecord       penn:community:employee:org:TOPU:NOTU:HCAG:21XX - University of Pennsylvania Health System:21XX - University of Pennsylvania Health System system of record                  Members of 21XX and all groups underneath the hierarchy  penn:community:employee:orgSecurity:orgReaders  penn:community:employee:orgSecurity:orgViewers  21XX    penn:community:employee:org:TOPU:NOTU:HCAG:21XX:21XX_rolluporg
  penn:community:employee:org:TOPU:NOTU:HCAG:99XX:99XX_rolluporg_systemOfRecord       penn:community:employee:org:TOPU:NOTU:HCAG:99XX - External Organizations Parent:99XX - External Organizations Parent system of record                                        Members of 99XX and all groups underneath the hierarchy  penn:community:employee:orgSecurity:orgReaders  penn:community:employee:orgSecurity:orgViewers  99XX    penn:community:employee:org:TOPU:NOTU:HCAG:99XX:99XX_rolluporg
  penn:community:employee:org:TOPU:NOTU:HCAG:99XX:AG02:AG02_rolluporg_systemOfRecord  penn:community:employee:org:TOPU:NOTU:HCAG:99XX:AG02 - Agencies for SAS:AG02 - Agencies for SAS system of record                                                             Members of AG02 and all groups underneath the hierarchy  penn:community:employee:orgSecurity:orgReaders  penn:community:employee:orgSecurity:orgViewers  AG02    penn:community:employee:org:TOPU:NOTU:HCAG:99XX:AG02:AG02
  penn:community:employee:org:TOPU:NOTU:HCAG:99XX:AG04:AG04_rolluporg_systemOfRecord  penn:community:employee:org:TOPU:NOTU:HCAG:99XX:AG04 - Agencies for Provost Interdisciplinary Center:AG04 - Agencies for Provost Interdisciplinary Center system of record	 Members of AG04 and all groups underneath the hierarchy  penn:community:employee:orgSecurity:orgReaders  penn:community:employee:orgSecurity:orgViewers  AG04    penn:community:employee:org:TOPU:NOTU:HCAG:99XX:AG04:AG04
  penn:community:employee:org:TOPU:NOTU:HCAG:99XX:AG06:AG06_rolluporg_systemOfRecord  penn:community:employee:org:TOPU:NOTU:HCAG:99XX:AG06 - Agencies for School of Nursing:AG06 - Agencies for School of Nursing system of record                                 Members of AG06 and all groups underneath the hierarchy  penn:community:employee:orgSecurity:orgReaders  penn:community:employee:orgSecurity:orgViewers  AG06    penn:community:employee:org:TOPU:NOTU:HCAG:99XX:AG06:AG06
  
  ```
- Now the rollups, make a view which assigns the rollup. Note this is based on the meta view, so it only includes groups in the meta view
  
  
  ```
  CREATE OR REPLACE VIEW ORG_LOADER_ROLLUP_V
  (GROUP_NAME, MEMBER_GROUP_NAME)
  AS
  (select distinct rollup_parent.GROUP_NAME as group_name,
  /* records which are rollups directly under the rollup */
  rollup_child.GROUP_OVERALL_NAME as subject_identifier
  from ORG_LOADER_ROLLUP_META_V rollup_parent, ORG_LOADER_ROLLUP_META_V rollup_child
  where rollup_child.PARENT_ID = rollup_parent.ORG_ID)
  union
  /* payroll orgs (which hold people) directly under the rollup */
  (select distinct rollup_parent.GROUP_NAME ,
  gh_member.ORG_HIERARCHICAL_STEM || ':' || gh_member.ORG_ID || '_personorg' as subject_identifier
  from grouperorgs_hierarchical gh_member, ORG_LOADER_ROLLUP_META_V rollup_parent, org_list_v olv_child
  where olv_child.PARENT_ID = rollup_parent.org_id and olv_child.ORG_NAME = gh_member.org_id
  and olv_child.PAYROLL_FLAG = 'Y' )
  
  ```
  
  * The data for this view looks like this
  
  
  ```
  GROUP_NAME                                                                              MEMBER_GROUP_NAME
  penn:community:employee:org:TOPU:NOTU:HCAG:99XX:AG02:AG02_rolluporg_systemOfRecord      penn:community:employee:org:TOPU:NOTU:HCAG:99XX:AG02:9938:9938_rolluporg
  penn:community:employee:org:TOPU:NOTU:HCAG:99XX:AG02:AG02_rolluporg_systemOfRecord      penn:community:employee:org:TOPU:NOTU:HCAG:99XX:AG02:9979:9979_personorg
  penn:community:employee:org:TOPU:NOTU:HCAG:99XX:AG04:AG04_rolluporg_systemOfRecord      penn:community:employee:org:TOPU:NOTU:HCAG:99XX:AG04:9992:9992_rolluporg
  penn:community:employee:org:TOPU:NOTU:HCAG:99XX:AG06:AG06_rolluporg_systemOfRecord      penn:community:employee:org:TOPU:NOTU:HCAG:99XX:AG06:9907:9907_rolluporg
  penn:community:employee:org:TOPU:NOTU:HCAG:99XX:AG07:AG07_rolluporg_systemOfRecord      penn:community:employee:org:TOPU:NOTU:HCAG:99XX:AG07:9902:9902_rolluporg
  penn:community:employee:org:TOPU:NOTU:HCAG:99XX:AG07:AG07_rolluporg_systemOfRecord      penn:community:employee:org:TOPU:NOTU:HCAG:99XX:AG07:9911:9911_rolluporg
  penn:community:employee:org:TOPU:NOTU:HCAG:99XX:AG07:AG07_rolluporg_systemOfRecord      penn:community:employee:org:TOPU:NOTU:HCAG:99XX:AG07:9912:9912_rolluporg
  penn:community:employee:org:TOPU:NOTU:HCAG:99XX:AG07:AG07_rolluporg_systemOfRecord      penn:community:employee:org:TOPU:NOTU:HCAG:99XX:AG07:9913:9913_rolluporg
  penn:community:employee:org:TOPU:NOTU:HCAG:99XX:AG07:AG07_rolluporg_systemOfRecord      penn:community:employee:org:TOPU:NOTU:HCAG:99XX:AG07:9914:9914_rolluporg
  
  ```

- Use the rollup view, join to group id's
  
  
  ```
  
  CREATE OR REPLACE VIEW ORG_LOADER_ROLLUP2_V
  (GROUP_NAME, SUBJECT_ID, SUBJECT_SOURCE_ID, subject_group_name)
  AS
  select olrv.GROUP_NAME group_name, ga.GROUP_ID as subject_id, 'g:gsa' as SUBJECT_SOURCE_ID,
  olrv.MEMBER_GROUP_NAME subject_group_name
  from org_loader_rollup_v olrv, grouper_attributes ga, grouper_fields gf
  where gf.NAME = 'name' AND gf.ID = ga.field_id and ga.VALUE = olrv.MEMBER_GROUP_NAME
  
  ```
  
  * The data from this view looks like
  
  
  ```
  
  GROUP_NAME                                                                      SUBJECT_ID                         SUBJECT_SOURCE_ID  SUBJECT_GROUP_NAME
  penn:community:employee:org:TOPU:UNIV:UADM:UADM_rolluporg_systemOfRecord        e5c4834ca09e45f8b635422cc1b6d4c1   g:gsa              penn:community:employee:org:TOPU:UNIV:UADM:88XX:88XX_personorg
  penn:community:employee:org:TOPU:NOTU:HCAG:99XX:99XX_rolluporg_systemOfRecord   0dd6217005be4b2996574fbcac0c1eec   g:gsa              penn:community:employee:org:TOPU:NOTU:HCAG:99XX:AG13:AG13_rolluporg
  penn:community:employee:org:TOPU:NOTU:HCAG:99XX:99XX_rolluporg_systemOfRecord   a2af451090644ed4b1d1d0ed57adf5d4   g:gsa              penn:community:employee:org:TOPU:NOTU:HCAG:99XX:AG98:AG98_rolluporg
  penn:community:employee:org:TOPU:UNIV:UADM:UADM_rolluporg_systemOfRecord        2fca9dbef7144c198b50bf48163d4cd4   g:gsa              penn:community:employee:org:TOPU:UNIV:UADM:90XX:90XX_rolluporg
  penn:community:employee:org:TOPU:UNIV:UADM:90XX:90XX_rolluporg_systemOfRecord   68e1396ccda643aa8357926de4a0f700   g:gsa              penn:community:employee:org:TOPU:UNIV:UADM:90XX:ALUM:ALUM_rolluporg
  penn:community:employee:org:TOPU:NOTU:HCAG:99XX:99XX_rolluporg_systemOfRecord   5a13844e363c4e6fbbc95015969b81c1   g:gsa              penn:community:employee:org:TOPU:NOTU:HCAG:99XX:AG24:AG24_rolluporg
  penn:community:employee:org:TOPU:NOTU:HCAG:99XX:99XX_rolluporg_systemOfRecord   3ad2e77634c3426c8a2e6e4777f7a350   g:gsa              penn:community:employee:org:TOPU:NOTU:HCAG:99XX:AG26:AG26_rolluporg
  penn:community:employee:org:TOPU:TOPU_rolluporg_systemOfRecord                  25d090972daa47b690b30eb8a9220de5   g:gsa              penn:community:employee:org:TOPU:UNIV:UNIV_rolluporg
  
  ```
  
  * Create the config group for the rollups, and execute the loader job
  
  
  ```
  
  gsh 4% rollupGroup = addGroup("penn:community:employee", "orgRollupConfig", "orgRollupConfig");
  group: name='penn:community:employee:orgRollupConfig' displayName='penn:community:employee:orgRollupConfig' uuid='8b329babf720413d81f5004fb3729c02'
  gsh 6% groupAddType("penn:community:employee:orgRollupConfig", "grouperLoader");
  true
  gsh 7% setGroupAttr("penn:community:employee:orgRollupConfig", "grouperLoaderDbName", "grouper");
  true
  gsh 8% setGroupAttr("penn:community:employee:orgRollupConfig", "grouperLoaderQuartzCron", "0 06 7 * * ? ");
  true
  gsh 9% setGroupAttr("penn:community:employee:orgRollupConfig", "grouperLoaderQuery", "select GROUP_NAME, SUBJECT_ID, SUBJECT_SOURCE_ID from ORG_LOADER_ROLLUP2_V");
  true
  gsh 10% setGroupAttr("penn:community:employee:orgRollupConfig", "grouperLoaderScheduleType", "CRON");
  true
  gsh 12% setGroupAttr("penn:community:employee:orgRollupConfig", "grouperLoaderType", "SQL_GROUP_LIST");
  true
  gsh 13% setGroupAttr("penn:community:employee:orgRollupConfig", "grouperLoaderGroupQuery", "select group_name, group_display_name, group_description, readers, viewers  from org_loader_rollup_meta_v");
  true
  gsh 14% setGroupAttr("penn:community:employee:orgRollupConfig", "grouperLoaderGroupsLike", "penn:community:employee:org:%_rolluporg_systemOfRecord");
  true
  gsh 15% setGroupAttr("penn:community:employee:orgRollupConfig", "grouperLoaderGroupTypes", "addIncludeExclude");
  true
  gsh 18% rollupGroup = GroupFinder.findByName(grouperSession, "penn:community:employee:orgRollupConfig");
  group: name='penn:community:employee:orgRollupConfig' displayName='penn:community:employee:orgRollupConfig' uuid='8b329babf720413d81f5004fb3729c02'
  gsh 19% loaderRunOneJob(rollupGroup);
  
  ```
- There are 1461 rollup orgs, 407k memberships. Here is a screen of the rollups orgs

- Here are the immediate members of a rollup org (immediate rollups underneath, or person orgs if directly underneath)

- Here are all members in a rollup group

#### Centers (can be high level orgs or across the hierarchy)

- Create a view of the list of centers

```

CREATE OR REPLACE VIEW ORG_CENTER_LIST_V
(CENTER_CODE, CENTER_NAME)
AS
select distinct olv.CENTER_CODE, olv.CENTER_NAME from org_list_v olv where center_code is not null

```

- This data looks like

```

CENTER_CODE  CENTER_NAME
26           University Museum
90           Development and Alumni Relations
87           Division of Finance
99           External Organizations Agency Funds
91           Information Systems and Computing

```

- Make a meta view aboup all the "center" groups

```

CREATE OR REPLACE VIEW ORG_CENTER_META_V
(GROUP_OVERALL_NAME, GROUP_NAME, GROUP_DISPLAY_NAME, GROUP_DESCRIPTION, READERS,
 VIEWERS, CENTER_CODE, CENTER_NAME)
AS
select 'penn:community:employee:center:' || oclv.CENTER_CODE || '_center:' || oclv.CENTER_CODE || '_center' as group_overall_name,
'penn:community:employee:center:' || oclv.CENTER_CODE || '_center:' || oclv.CENTER_CODE || '_center_systemOfRecord' as group_name,
'penn:community:employee:center:' || oclv.CENTER_CODE || ' center ' || oclv.CENTER_NAME || ':' || oclv.CENTER_CODE || ' center ' || oclv.CENTER_NAME || ' system of record' as group_display_name,
'Center ' || oclv.CENTER_CODE || ' ' || oclv.CENTER_NAME || ' is a rollup of orgs and their includes/excludes' as group_description,
'penn:community:employee:orgSecurity:orgReaders' as readers,
'penn:community:employee:orgSecurity:orgViewers' as viewers,
center_code, center_name from org_center_list_v oclv

```

- This data looks like this

```

GROUP_OVERALL_NAME                                  GROUP_NAME	                                                       GROUP_DISPLAY_NAME                                                                                                                            GROUP_DESCRIPTION                                                                              READERS                                         VIEWERS                                         CENTER_CODE  CENTER_NAME
penn:community:employee:center:26_center:26_center  penn:community:employee:center:26_center:26_center_systemOfRecord  penn:community:employee:center:26 center University Museum:26 center University Museum system of record                                       Center 26 University Museum is a rollup of orgs and their includes/excludes                    penn:community:employee:orgSecurity:orgReaders  penn:community:employee:orgSecurity:orgViewers  26           University Museum
penn:community:employee:center:90_center:90_center  penn:community:employee:center:90_center:90_center_systemOfRecord  penn:community:employee:center:90 center Development and Alumni Relations:90 center Development and Alumni Relations system of record         Center 90 Development and Alumni Relations is a rollup of orgs and their includes/excludes     penn:community:employee:orgSecurity:orgReaders  penn:community:employee:orgSecurity:orgViewers  90           Development and Alumni Relations
penn:community:employee:center:87_center:87_center  penn:community:employee:center:87_center:87_center_systemOfRecord  penn:community:employee:center:87 center Division of Finance:87 center Division of Finance system of record                                   Center 87 Division of Finance is a rollup of orgs and their includes/excludes                  penn:community:employee:orgSecurity:orgReaders  penn:community:employee:orgSecurity:orgViewers  87           Division of Finance
penn:community:employee:center:99_center:99_center  penn:community:employee:center:99_center:99_center_systemOfRecord  penn:community:employee:center:99 center External Organizations Agency Funds:99 center External Organizations Agency Funds system of record   Center 99 External Organizations Agency Funds is a rollup of orgs and their includes/excludes  penn:community:employee:orgSecurity:orgReaders  penn:community:employee:orgSecurity:orgViewers  99           External Organizations Agency Funds
penn:community:employee:center:91_center:91_center  penn:community:employee:center:91_center:91_center_systemOfRecord  penn:community:employee:center:91 center Information Systems and Computing:91 center Information Systems and Computing system of record       Center 91 Information Systems and Computing is a rollup of orgs and their includes/excludes    penn:community:employee:orgSecurity:orgReaders  penn:community:employee:orgSecurity:orgViewers  91           Information Systems and Computing
penn:community:employee:center:79_center:79_center  penn:community:employee:center:79_center:79_center_systemOfRecord  penn:community:employee:center:79 center Division of Public Safety:79 center Division of Public Safety system of record                       Center 79 Division of Public Safety is a rollup of orgs and their includes/excludes            penn:community:employee:orgSecurity:orgReaders  penn:community:employee:orgSecurity:orgViewers  79           Division of Public Safety

```

- Make a view of which children (org nodes) are in each center (direct children not grandchildren which will be effective members anyways)

```

CREATE OR REPLACE VIEW ORG_CENTER_ROLLUP_V
(GROUP_NAME, MEMBER_GROUP_NAME)
AS
(select distinct ocmv.GROUP_NAME as group_name,
/\* records which are rollups directly under the rollup \*/
rollup_child.GROUP_OVERALL_NAME as subject_identifier
from org_center_meta_v ocmv, ORG_LOADER_ROLLUP_META_V rollup_child, org_list_v olv_child
where olv_child.CENTER_CODE_ASSIGN = ocmv.CENTER_CODE and olv_child.ORG_NAME = rollup_child.ORG_ID )
union all
/\* payroll orgs (which hold people) directly under the rollup \*/
(select distinct ocmv.GROUP_NAME ,
olpmv.GROUP_NAME as subject_identifier
from org_center_meta_v ocmv, org_list_v olv_child, org_loader_person_meta_v olpmv
where olv_child.CENTER_CODE_ASSIGN = ocmv.CENTER_CODE and olv_child.ORG_NAME = olpmv.ORG_ID)

```

- This data looks like this

```

GROUP_NAME                                                         MEMBER_GROUP_NAME
penn:community:employee:center:86_center:86_center_systemOfRecord  penn:community:employee:org:TOPU:UNIV:USTU:86XX:86XX_rolluporg
penn:community:employee:center:98_center:98_center_systemOfRecord  penn:community:employee:org:TOPU:UNIV:UADM:98XX:98XX_rolluporg
penn:community:employee:center:32_center:32_center_systemOfRecord  penn:community:employee:org:TOPU:UNIV:USCH:32XX:32XX_rolluporg
penn:community:employee:center:02_center:02_center_systemOfRecord  penn:community:employee:org:TOPU:UNIV:USCH:02XX:WLNT:PSYC:0120:0120_personorg
penn:community:employee:center:02_center:02_center_systemOfRecord  penn:community:employee:org:TOPU:UNIV:USCH:02XX:DRLB:PHYS:0118:0118_personorg

```

- Join this with the grouper registry to get the group ids

```

CREATE OR REPLACE VIEW ORG_CENTER_ROLLUP2_V
(GROUP_NAME, SUBJECT_ID, SUBJECT_SOURCE_ID, SUBJECT_GROUP_NAME)
AS
select ocrv.GROUP_NAME group_name, ga.GROUP_ID as subject_id, 'g:gsa' as SUBJECT_SOURCE_ID,
ocrv.MEMBER_GROUP_NAME subject_group_name
from org_center_rollup_v ocrv, grouper_attributes ga, grouper_fields gf
where gf.NAME = 'name' AND gf.ID = ga.field_id and ga.VALUE = ocrv.MEMBER_GROUP_NAME

```

- This data looks like this

```

GROUP_NAME                                                         SUBJECT_ID                        SUBJECT_SOURCE_ID  SUBJECT_GROUP_NAME
penn:community:employee:center:98_center:98_center_systemOfRecord  7121d78fafc3405a97adf76e48ae3390  g:gsa              penn:community:employee:org:TOPU:UNIV:UADM:98XX:98XX_rolluporg
penn:community:employee:center:86_center:86_center_systemOfRecord  29a0985125aa4943933a011bad47d2e2  g:gsa              penn:community:employee:org:TOPU:UNIV:USTU:86XX:86XX_rolluporg
penn:community:employee:center:99_center:99_center_systemOfRecord  f7a98c679d674711aa272fbeda85cd79  g:gsa              penn:community:employee:org:TOPU:NOTU:HCAG:99XX:99XX_rolluporg
penn:community:employee:center:32_center:32_center_systemOfRecord  196282f7423e4a9ba2fd0f990ae4e067  g:gsa              penn:community:employee:org:TOPU:UNIV:USCH:32XX:32XX_rolluporg
penn:community:employee:center:02_center:02_center_systemOfRecord  a3344e870adf4c408e88ceac40d9e595  g:gsa              penn:community:employee:org:TOPU:UNIV:USCH:02XX:WLNT:PSYC:0120:0120_personorg
penn:community:employee:center:02_center:02_center_systemOfRecord  873b1c843bfa4a198e26ffa75707cb85  g:gsa              penn:community:employee:org:TOPU:UNIV:USCH:02XX:DRLB:PHYS:0118:0118_personorg

```

- Add this config group

```

gsh 4% centerGroup = addGroup("penn:community:employee", "centerRollupConfig", "centerRollupConfig");
group: name='penn:community:employee:centerRollupConfig' displayName='penn:community:employee:centerRollupConfig' uuid='8b329babf720413d81f5004fb3729c02'
gsh 6% groupAddType("penn:community:employee:centerRollupConfig", "grouperLoader");
true
gsh 7% setGroupAttr("penn:community:employee:centerRollupConfig", "grouperLoaderDbName", "grouper");
true
gsh 8% setGroupAttr("penn:community:employee:centerRollupConfig", "grouperLoaderQuartzCron", "0 36 7 * * ? ");
true
gsh 9% setGroupAttr("penn:community:employee:centerRollupConfig", "grouperLoaderQuery", "select GROUP_NAME, SUBJECT_ID, SUBJECT_SOURCE_ID from ORG_CENTER_ROLLUP2_V");
true
gsh 10% setGroupAttr("penn:community:employee:centerRollupConfig", "grouperLoaderScheduleType", "CRON");
true
gsh 12% setGroupAttr("penn:community:employee:centerRollupConfig", "grouperLoaderType", "SQL_GROUP_LIST");
true
gsh 13% setGroupAttr("penn:community:employee:centerRollupConfig", "grouperLoaderGroupQuery", "select group_name, group_display_name, group_description, readers, viewers  from org_center_meta_v");
true
gsh 14% setGroupAttr("penn:community:employee:centerRollupConfig", "grouperLoaderGroupsLike", "penn:community:employee:center:%_center_systemOfRecord");
true
gsh 15% setGroupAttr("penn:community:employee:centerRollupConfig", "grouperLoaderGroupTypes", "addIncludeExclude");
true
gsh 18% centerGroup = GroupFinder.findByName(grouperSession, "penn:community:employee:centerRollupConfig");
group: name='penn:community:employee:centerRollupConfig' displayName='penn:community:employee:centerRollupConfig' uuid='8b329babf720413d81f5004fb3729c02'
gsh 19% loaderRunOneJob(centerGroup);

```

- This created 41 centers, with 91k membership. Here are the centers
- Here is a look at one center

- Here are the immediate members of a system of record group of a center
- Here are all members of a center

#### Contractors and one-offs

We dont have groupings of contractors in our payroll system, but we can set a flag in "Penn Community" our person database. So lets organize a way to automatically make groups based on flags. Lets make a table which identifies the flags we are looking for, and which group extension

```

CREATE TABLE ORG_SPONSOR_GROUP (
  GROUP_EXTENSION    VARCHAR2(128 CHAR)         NOT NULL,
  LIKE_STRING_UPPER  VARCHAR2(128 CHAR)
)

```

* Currently we only have one row

```

GROUP_EXTENSION    LIKE_STRING_UPPER
96XX_consultants   96XX

```

* Lets make the meta view that describes the groups managed by this loader

```

CREATE OR REPLACE VIEW ORG_SPONSOR_META_V
(GROUP_NAME, GROUP_DISPLAY_NAME, GROUP_DESCRIPTION, READERS, VIEWERS,
 GROUP_OVERALL_NAME, GROUP_EXTENSION)
AS
select 'penn:community:employee:sponsororg:' || osg.GROUP_EXTENSION || '_sponsororg:' || osg.GROUP_EXTENSION || '_sponsororg_systemOfRecord' as group_name,
'penn:community:employee:sponsororg:' || osg.GROUP_EXTENSION || '_sponsororg:' || osg.GROUP_EXTENSION || '_sponsororg system of record' as group_display_name,
'people in penn community with a substring of ' || osg.LIKE_STRING_UPPER || ' in their sponsor_org field somewhere' as group_description,
'penn:community:employee:orgSecurity:orgReaders' as readers,
'penn:community:employee:orgSecurity:orgViewers' as viewers,
'penn:community:employee:sponsororg:' || osg.GROUP_EXTENSION || '_sponsororg:' || osg.GROUP_EXTENSION || '_sponsororg' as group_overall_name,
osg.GROUP_EXTENSION
from org_sponsor_group osg

```

* The data from that view looks like this

```

GROUP_NAME                                                                                                 GROUP_DISPLAY_NAME                                                                                           GROUP_DESCRIPTION                                                                       READERS                                         VIEWERS                                         GROUP_OVERALL_NAME                                                                           GROUP_EXTENSION
penn:community:employee:sponsororg:96XX_consultants_sponsororg:96XX_consultants_sponsororg_systemOfRecord  penn:community:employee:sponsororg:96XX_consultants_sponsororg:96XX_consultants_sponsororg system of record  people in penn community with a substring of 96XX in their sponsor_org field somewhere  penn:community:employee:orgSecurity:orgReaders  penn:community:employee:orgSecurity:orgViewers  penn:community:employee:sponsororg:96XX_consultants_sponsororg:96XX_consultants_sponsororg   96XX_consultants

```

* Make an assignment view which links up people with their one-off group

```

CREATE OR REPLACE VIEW ORG_SPONSOR_LIST_V
(GROUP_NAME, SUBJECT_ID)
AS
(Select distinct osmv.GROUP_NAME as group_name, sav.v_penn_id as subject_id
from ORG_SPONSOR_group osg, comadmin.ssn4_affiliation_view sav, ORG_SPONSOR_META_V osmv
Where sav.v_active_code = 'A'
and upper(sav.v_sponsor_org) like '%' || osg.LIKE_STRING_UPPER || '%'
and osmv.GROUP_EXTENSION = osg.GROUP_EXTENSION)

```

* The data from that view looks like this

```

GROUP_NAME                                                                                                 SUBJECT_ID
penn:community:employee:sponsororg:96XX_consultants_sponsororg:96XX_consultants_sponsororg_systemOfRecord  10128438
penn:community:employee:sponsororg:96XX_consultants_sponsororg:96XX_consultants_sponsororg_systemOfRecord  68214103
penn:community:employee:sponsororg:96XX_consultants_sponsororg:96XX_consultants_sponsororg_systemOfRecord  10135159
penn:community:employee:sponsororg:96XX_consultants_sponsororg:96XX_consultants_sponsororg_systemOfRecord  10114304

```

* Create the config group, and run the loader

```

gsh 4% sponsorGroup = addGroup("penn:community:employee", "orgSponsorConfig", "orgSponsorConfig");
group: name='penn:community:employee:orgSponsorConfig' displayName='penn:community:employee:orgSponsorConfig' uuid='8b329babf720413d81f5004fb3729c02'
gsh 6% groupAddType("penn:community:employee:orgSponsorConfig", "grouperLoader");
true
gsh 7% setGroupAttr("penn:community:employee:orgSponsorConfig", "grouperLoaderDbName", "grouper");
true
gsh 8% setGroupAttr("penn:community:employee:orgSponsorConfig", "grouperLoaderQuartzCron", "0 16 7 * * ? ");
true
gsh 9% setGroupAttr("penn:community:employee:orgSponsorConfig", "grouperLoaderQuery", "select GROUP_NAME, SUBJECT_ID from ORG_SPONSOR_LIST_V");
true
gsh 10% setGroupAttr("penn:community:employee:orgSponsorConfig", "grouperLoaderScheduleType", "CRON");
true
gsh 12% setGroupAttr("penn:community:employee:orgSponsorConfig", "grouperLoaderType", "SQL_GROUP_LIST");
true
gsh 13% setGroupAttr("penn:community:employee:orgSponsorConfig", "grouperLoaderGroupQuery", "select group_name, group_display_name, group_description, readers, viewers  from org_sponsor_meta_v");
true
gsh 14% setGroupAttr("penn:community:employee:orgSponsorConfig", "grouperLoaderGroupsLike", "penn:community:employee:sponsororg:%_sponsororg_systemOfRecord");
true
gsh 15% setGroupAttr("penn:community:employee:orgSponsorConfig", "grouperLoaderGroupTypes", "addIncludeExclude");
true
gsh 18% sponsorGroup = GroupFinder.findByName(grouperSession, "penn:community:employee:orgSponsorConfig");
group: name='penn:community:employee:orgSponsorConfig' displayName='penn:community:employee:orgSponsorConfig' uuid='8b329babf720413d81f5004fb3729c02'
gsh 19% loaderRunOneJob(sponsorGroup);

```

- Here is an attribute based (sponsor) group

- Here are the members of an attribute based sponsor group

#### Putting it all together

We need a group for all employees in Facilities and Real Estate Services. We need to add the VP (who is not in the Facilities org), and we need to add the contractors. I will add this in the org structure, so people can easily find it.

- Add the VP to the includes list of the org. Note, this means that for all purposes, additions are considered part of the org, and part of the center (since the org is part of the center)
- Now make a group which has the org group, and the consultants
- Here are the two members
- Here are all members
- Protect a web resource in apache by requiring members to be in this group (note: authnz_ldap apache module grabs all members of the group unless a patch is applied that Penn developed which is not yet public)
  
  
  ```
  
  <Directory "[directory]">
    CosignProtected on
    AuthType Cosign
    CosignRequireFactor UPENN.EDU
    AuthzLDAPAuthoritative on
    AuthLDAPCompareDNOnServer on
    AuthLDAPBindPassword [password]
    AuthLDAPBindDN uid=[service principal],ou=entities,dc=upenn,dc=edu
    AuthLDAPLimitAttribute cn # custom attribute via local patch
    AuthLDAPURL
  ldaps://url.ldap.private/cn=penn:community:employee:org:TOPU:UNIV:UADM:96XX:96XX_andConsultants,ou=groups,dc=upenn,dc=edu?hasMember
    require ldap-dn cn=penn:community:employee:org:TOPU:UNIV:UADM:96XX:96XX_andConsultants,ou=groups,dc=upenn,dc=edu
  </Directory>
  
  ```
