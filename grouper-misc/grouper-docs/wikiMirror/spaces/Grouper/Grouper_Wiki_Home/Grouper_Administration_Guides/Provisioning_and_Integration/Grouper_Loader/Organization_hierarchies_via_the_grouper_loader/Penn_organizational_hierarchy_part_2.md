---
title: "Penn organizational hierarchy - part 2"
space: Grouper
pageId: 28554176
version: 13
lastUpdated: 2026-07-01T05:40:59.354Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554176/Penn+organizational+hierarchy+-+part+2
---

[This is continued from part 1](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28673890/Penn+organizational+hierarchy+-+part+1)

#### Rollup orgs

- Now we need a meta view which has information about the rollup group: note the top two levels are filtered out
  
  
  ```
  CREATE OR REPLACE FORCE VIEW ORG_LOADER_ROLLUP_META_V
  (
     GROUP_NAME,
     GROUP_DISPLAY_NAME,
     GROUP_DESCRIPTION,
     READERS,
     VIEWERS,
     ORG_ID,
     GROUP_OVERALL_NAME,
     PARENT_ID
  )
  AS
       SELECT DISTINCT
                 'penn:community:employee:org:'
              || olv.org_name
              || ':'
              || olv.org_name
              || '_rolluporg_systemOfRecord'
                 AS group_name,
                 'penn:community:employee:org:'
              || olv.org_name
              || ' - '
              || olv.ORG_DISPLAY_NAME
              || ':'
              || olv.org_name
              || ' - '
              || olv.ORG_DISPLAY_NAME
              || ' system of record'
                 AS group_display_name,
                 'Members of '
              || olv.org_name
              || ' and all groups underneath the hierarchy'
                 AS group_description,
              'penn:community:employee:orgSecurity:orgReaders' AS readers,
              'penn:community:employee:orgSecurity:orgViewers' AS viewers,
              olv.org_name AS org_id,
                 'penn:community:employee:org:'
              || olv.org_name
              || ':'
              || olv.org_name
              || '_rolluporg'
                 AS group_overall_name,
              olv.PARENT_ID
         FROM org_list_v olv
        WHERE     EXISTS
                     (SELECT OLV2.ORG_NAME
                        FROM org_list_v olv2
                       WHERE OLV2.PARENT_ID = OLV.ORG_NAME)
              AND olv.PAYROLL_FLAG = 'N'
              AND olv.ORG_NAME NOT IN ('TOPU', 'UNIV', 'NOTU')
     ORDER BY 2;
  
  ```

- This has data which looks like this:

| GROUP_NAME | GROUP_DISPLAY_NAME | GROUP_DESCRIPTION | READERS | VIEWERS | ORG_ID | GROUP_OVERALL_NAME | PARENT_ID |
| --- | --- | --- | --- | --- | --- | --- | --- |
| penn:community:employee:org:00XX:00XX_rolluporg_systemOfRecord | penn:community:employee:org:00XX - General University Parent:00XX - General University Parent system of record | Members of 00XX and all groups underneath the hierarchy | penn:community:employee:orgSecurity:orgReaders | penn:community:employee:orgSecurity:orgViewers | 00XX | penn:community:employee:org:00XX:00XX_rolluporg | UOTH |
| penn:community:employee:org:02XX:02XX_rolluporg_systemOfRecord | penn:community:employee:org:02XX - School of Arts and Sciences Parent:02XX - School of Arts and Sciences Parent system of record | Members of 02XX and all groups underneath the hierarchy | penn:community:employee:orgSecurity:orgReaders | penn:community:employee:orgSecurity:orgViewers | 02XX | penn:community:employee:org:02XX:02XX_rolluporg | USCH |

- Now the rollups, make a view which assigns the rollup. Note this is based on the meta view, so it only includes groups in the meta view

```
CREATE OR REPLACE FORCE VIEW ORG_LOADER_ROLLUP_V
(
   GROUP_NAME,
   MEMBER_GROUP_NAME
)
   BEQUEATH DEFINER
AS
   (SELECT DISTINCT
           rollup_parent.GROUP_NAME AS group_name,
           /* records which are rollups directly under the rollup */
           rollup_child.GROUP_OVERALL_NAME AS subject_identifier
      FROM ORG_LOADER_ROLLUP_META_V rollup_parent,
           ORG_LOADER_ROLLUP_META_V rollup_child
     WHERE rollup_child.PARENT_ID = rollup_parent.ORG_ID)
   UNION
   /* payroll orgs (which hold people) directly under the rollup */
   (SELECT DISTINCT
           rollup_parent.GROUP_NAME,
              'penn:community:employee:org:'
           || olv_child.ORG_NAME
           || ':'
           || olv_child.ORG_NAME
           || '_personorg'
              AS subject_identifier
      FROM ORG_LOADER_ROLLUP_META_V rollup_parent, org_list_v olv_child
     WHERE     olv_child.PARENT_ID = rollup_parent.org_id
           AND olv_child.PAYROLL_FLAG = 'Y');

```

- The data for this view looks like this

| GROUP_NAME | MEMBER_GROUP_NAME |
| --- | --- |
| penn:community:employee:org:00XX:00XX_rolluporg_systemOfRecord | penn:community:employee:org:0001:0001_personorg |
| penn:community:employee:org:00XX:00XX_rolluporg_systemOfRecord | penn:community:employee:org:0007:0007_personorg |
| penn:community:employee:org:02XX:02XX_rolluporg_systemOfRecord | penn:community:employee:org:BIOB:BIOB_rolluporg |

- Use the rollup view, join to group id's

```
CREATE OR REPLACE FORCE VIEW ORG_LOADER_ROLLUP2_V
(
   GROUP_NAME,
   SUBJECT_ID,
   SUBJECT_SOURCE_ID,
   SUBJECT_GROUP_NAME
)
AS
   SELECT olrv.GROUP_NAME group_name,
          gg.ID AS subject_id,
          'g:gsa' AS SUBJECT_SOURCE_ID,
          olrv.MEMBER_GROUP_NAME subject_group_name
     FROM org_loader_rollup_v olrv, grouper_groups gg
    WHERE gg.NAME = olrv.MEMBER_GROUP_NAME;
COMMENT ON TABLE ORG_LOADER_ROLLUP2_V IS 'shows rollup group assignments';

```

- The data from this view looks like

| GROUP_NAME | SUBJECT_ID | SUBJECT_SOURCE_ID | SUBJECT_GROUP_NAME |
| --- | --- | --- | --- |
| penn:community:employee:org:TOPU:UNIV:UADM:UADM_rolluporg_systemOfRecord | e5c4834ca09e45f8b635422cc1b6d4c1 | g:gsa | penn:community:employee:org:TOPU:UNIV:UADM:88XX:88XX_personorg |
| penn:community:employee:org:TOPU:NOTU:HCAG:99XX:99XX_rolluporg_systemOfRecord | 0dd6217005be4b2996574fbcac0c1eec | g:gsa | penn:community:employee:org:TOPU:NOTU:HCAG:99XX:AG13:AG13_rolluporg |
| penn:community:employee:org:TOPU:NOTU:HCAG:99XX:99XX_rolluporg_systemOfRecord | a2af451090644ed4b1d1d0ed57adf5d4 | g:gsa | penn:community:employee:org:TOPU:NOTU:HCAG:99XX:AG98:AG98_rolluporg |
| penn:community:employee:org:TOPU:UNIV:UADM:UADM_rolluporg_systemOfRecord | 2fca9dbef7144c198b50bf48163d4cd4 | g:gsa | penn:community:employee:org:TOPU:UNIV:UADM:90XX:90XX_rolluporg |

- Create the config group for the rollups, and execute the loader job

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
gsh 13% setGroupAttr("penn:community:employee:orgRollupConfig", "grouperLoaderGroupQuery", "select group_name, group_display_name, group_description, readers, viewers from org_loader_rollup_meta_v");
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

- Here are all members in a rollup group

#### Centers (can be high level orgs or across the hierarchy)

- Create a view of the list of centers

```
CREATE OR REPLACE FORCE VIEW ORG_CENTER_LIST_V
(
   CENTER_CODE,
   CENTER_NAME
)
AS
   SELECT DISTINCT olv.CENTER_CODE, olv.CENTER_NAME
     FROM org_list_v olv
    WHERE center_code IS NOT NULL;
COMMENT ON TABLE ORG_CENTER_LIST_V IS 'list of centers to make groups for';

```

- This data looks like

| CENTER_CODE | CENTER_NAME |
| --- | --- |
| 26 | University Museum |
| 90 | Development and Alumni Relations |
| 87 | Division of Finance |

- Make a meta view aboup all the "center" groups

```
CREATE OR REPLACE FORCE VIEW ORG_CENTER_META_V
(
   GROUP_OVERALL_NAME,
   GROUP_NAME,
   GROUP_DISPLAY_NAME,
   GROUP_DESCRIPTION,
   READERS,
   VIEWERS,
   CENTER_CODE,
   CENTER_NAME
)
AS
   SELECT    'penn:community:employee:center:'
          || oclv.CENTER_CODE
          || '_center:'
          || oclv.CENTER_CODE
          || '_center'
             AS group_overall_name,
             'penn:community:employee:center:'
          || oclv.CENTER_CODE
          || '_center:'
          || oclv.CENTER_CODE
          || '_center_systemOfRecord'
             AS group_name,
             'penn:community:employee:center:'
          || oclv.CENTER_CODE
          || ' center '
          || oclv.CENTER_NAME
          || ':'
          || oclv.CENTER_CODE
          || ' center '
          || oclv.CENTER_NAME
          || ' system of record'
             AS group_display_name,
             'Center '
          || oclv.CENTER_CODE
          || ' '
          || oclv.CENTER_NAME
          || ' is a rollup of orgs and their includes/excludes'
             AS group_description,
          'penn:community:employee:orgSecurity:orgReaders' AS readers,
          'penn:community:employee:orgSecurity:orgViewers' AS viewers,
          center_code,
          center_name
     FROM org_center_list_v oclv;
COMMENT ON TABLE ORG_CENTER_META_V IS 'list of groups managed by the center loader job';

```

- This data looks like this

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
GROUP_NAME MEMBER_GROUP_NAME
penn:community:employee:center:86_center:86_center_systemOfRecord penn:community:employee:org:86XX:86XX_rolluporg
penn:community:employee:center:98_center:98_center_systemOfRecord penn:community:employee:org:98XX:98XX_rolluporg
penn:community:employee:center:32_center:32_center_systemOfRecord penn:community:employee:org:32XX:32XX_rolluporg
penn:community:employee:center:02_center:02_center_systemOfRecord penn:community:employee:org:0120:0120_personorg

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
  
  
  
  | GROUP_OVERALL_NAME | GROUP_NAME | GROUP_DISPLAY_NAME | GROUP_DESCRIPTION | READERS | VIEWERS | CENTER_CODE | CENTER_NAME |
  | --- | --- | --- | --- | --- | --- | --- | --- |
  | penn:community:employee:center:22_center:22_center | penn:community:employee:center:22_center:22_center_systemOfRecord | penn:community:employee:center:22 center Domestic Subsidiaries:22 center Domestic Subsidiaries system of record | Center 22 Domestic Subsidiaries is a rollup of orgs and their includes/excludes | penn:community:employee:orgSecurity:orgReaders | penn:community:employee:orgSecurity:orgViewers | 22 | Domestic Subsidiaries |

- ```
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
  gsh 13% setGroupAttr("penn:community:employee:centerRollupConfig", "grouperLoaderGroupQuery", "select group_name, group_display_name, group_description, readers, viewers from org_center_meta_v");
  true
  gsh 14% setGroupAttr("penn:community:employee:centerRollupConfig", "grouperLoaderGroupsLike", "penn:community:employee:center:%_center_systemOfRecord");
  true
  gsh 15% setGroupAttr("penn:community:employee:centerRollupConfig", "grouperLoaderGroupTypes", "addIncludeExclude");
  true
  gsh 18% centerGroup = GroupFinder.findByName(grouperSession, "penn:community:employee:centerRollupConfig");
  group: name='penn:community:employee:centerRollupConfig' displayName='penn:community:employee:centerRollupConfig' uuid='8b329babf720413d81f5004fb3729c02'
  gsh 19% loaderRunOneJob(centerGroup);
  
  ```
  
  Add this config group

- This created 41 centers, with 91k membership. Here are the centers
- Here is a look at one center

- Here are all members of a center  
    
    
    
  
  
  #### Contractors and one-offs

We dont have groupings of contractors in our payroll system, but we can set a flag in "Penn Community" our person database. So lets organize a way to automatically make groups based on flags. Lets make a table which identifies the flags we are looking for, and which group extension

```
CREATE TABLE ORG_SPONSOR_GROUP (
GROUP_EXTENSION VARCHAR2(128 CHAR) NOT NULL,
LIKE_STRING_UPPER VARCHAR2(128 CHAR)
)

```

* Currently we only have one row

```
GROUP_EXTENSION LIKE_STRING_UPPER
96XX_consultants 96XX

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
GROUP_NAME GROUP_DISPLAY_NAME GROUP_DESCRIPTION READERS VIEWERS GROUP_OVERALL_NAME GROUP_EXTENSION
penn:community:employee:sponsororg:96XX_consultants_sponsororg:96XX_consultants_sponsororg_systemOfRecord penn:community:employee:sponsororg:96XX_consultants_sponsororg:96XX_consultants_sponsororg system of record people in penn community with a substring of 96XX in their sponsor_org field somewhere penn:community:employee:orgSecurity:orgReaders penn:community:employee:orgSecurity:orgViewers penn:community:employee:sponsororg:96XX_consultants_sponsororg:96XX_consultants_sponsororg 96XX_consultants

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
GROUP_NAME SUBJECT_ID
penn:community:employee:sponsororg:96XX_consultants_sponsororg:96XX_consultants_sponsororg_systemOfRecord 10128438
penn:community:employee:sponsororg:96XX_consultants_sponsororg:96XX_consultants_sponsororg_systemOfRecord 68214103
penn:community:employee:sponsororg:96XX_consultants_sponsororg:96XX_consultants_sponsororg_systemOfRecord 10135159
penn:community:employee:sponsororg:96XX_consultants_sponsororg:96XX_consultants_sponsororg_systemOfRecord 10114304

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
gsh 13% setGroupAttr("penn:community:employee:orgSponsorConfig", "grouperLoaderGroupQuery", "select group_name, group_display_name, group_description, readers, viewers from org_sponsor_meta_v");
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
  
  - - -
- Now make a group which has the org group, and the consultants
  
  - - -
- Here are the two members
  
  - - -
- Here are all members
  
  - - -
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
  ldaps://url.ldap.private/cn=penn:community:employee:org:96XX:96XX_andConsultants,ou=groups,dc=upenn,dc=edu?hasMember
  require ldap-dn cn=penn:community:employee:96XX:96XX_andConsultants,ou=groups,dc=upenn,dc=edu
  </Directory>
  
  ```
