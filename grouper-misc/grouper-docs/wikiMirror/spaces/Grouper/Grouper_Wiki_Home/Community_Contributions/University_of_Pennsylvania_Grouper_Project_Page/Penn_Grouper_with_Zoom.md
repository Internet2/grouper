---
title: "Penn Grouper with Zoom"
space: Grouper
pageId: 28544180
version: 15
lastUpdated: 2026-07-12T05:48:00.376Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544180/Penn+Grouper+with+Zoom
---

## Accounts

Penn had many accounts with Zoom. Each school and center had a contract with Zoom. In the summer of 2020 we consolidated most of the accounts that we could into the main account and more closely managed the few subaccounts.

Various conditions cause users/groups to be eligible for Zoom.

## Access to main account

With Zoom, you should have your license in one account. If you move between accounts, it is confusing to the user.

To be able to have a license in the main account, here is the logic.

## Zoom entitlements

Users SSO to zoom and are sent with a group and a role. They must have one and only one group (described below), and one and only one role (described below) . If they are not eligible, they should go from SSO to a custom error page that gives them context about why they do not have access, or give them a link to the URL of the subaccount or other account they should be using.

[Zoom custom UI](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554923/Grouper+Custom+UI+example+Zoom)

## Primary affiliation

We have primary affiliation in Grouper, and we want to pull people from groups based on this, but when you join too many tables together, it kills performance, so we will "SQL sync" this data to a table, so the performance is good when we make complex queries. This views tells us the primary affiliation of a user based on group.

```
CREATE OR REPLACE VIEW penngrouper.penn_primary_affiliation_v
AS SELECT grouper_memberships_lw_v.subject_id AS penn_id,
    substr(grouper_memberships_lw_v.group_name::text, length('penn:community:affiliationPrimary:affiliationPrimary_'::text) + 1, length(grouper_memberships_lw_v.group_name::text)) AS affiliation
   FROM grouper_memberships_lw_v
  WHERE grouper_memberships_lw_v.list_name::text = 'members'::text AND grouper_memberships_lw_v.subject_source::text = 'pennperson'::text AND grouper_memberships_lw_v.group_name::text ~~ 'penn:community:affiliationPrimary:affiliationPrimary_%'::text;
```

We start by making a table based on this view

```
create table penn_primary_affiliation as select * from penn_primary_affiliation_v;

-- then add some indexes and a primary key or whatever
```

Keep these in sync with a Grouper daemon job

grouper.client.properties:

```
grouperClient.syncTable.primaryAffiliation.databaseFrom = grouper
grouperClient.syncTable.primaryAffiliation.databaseTo = grouper
grouperClient.syncTable.primaryAffiliation.tableFrom = penn_primary_affiliation_v
grouperClient.syncTable.primaryAffiliation.tableTo = penn_primary_affiliation
grouperClient.syncTable.primaryAffiliation.columns = penn_id, affiliation
grouperClient.syncTable.primaryAffiliation.primaryKeyColumns = penn_id, affiliation
```

goruper-loader.properties

```
otherJob.primaryAffiliation.quartzCron = 0 45 * * * ?
otherJob.primaryAffiliation.class = edu.internet2.middleware.grouper.app.tableSync.TableSyncOtherJob
otherJob.primaryAffiliation.syncType = fullSyncFull
otherJob.primaryAffiliation.grouperClientTableSyncConfigKey = primaryAffiliation
```

Now the table is in sync hourly with easier to access data

## Zoom automated includes

There is a folder with automated includes for users in the main account. Generally "groups" in Zoom dont mean much, but we organize our users in a couple dozen dept groups so that help desk workers know in Zoom who should be supporting the user.

The loader query for this starts with a query from Grouper which determines the primary affiliation for the user (described above). The dept is different for students or employees, so the population is split, put in equivalent groups based on grouper data, and unioned back. We need this query so we dont have to manually setup a dozen groups each with half a dozen groups inside. Too much manual work and something could go wrong. Of course there could be better refernce groups to work with, but there is ultimate flexibility with SQL

```
CREATE OR REPLACE VIEW penngrouper.penn_zoom_loaded_groups_v
AS SELECT
        CASE
            WHEN gmlv.group_name::text = 'penn:community:student:primarySchool:primarySchool_AS'::text THEN 'penn:isc:ait:apps:zoom:service:ref:loadedGroups:SAS'::text
            WHEN gmlv.group_name::text = 'penn:community:student:primarySchool:primarySchool_AN'::text THEN 'penn:isc:ait:apps:zoom:service:ref:loadedGroups:ASC'::text
            WHEN gmlv.group_name::text = 'penn:community:student:primarySchool:primarySchool_WH'::text THEN 'penn:isc:ait:apps:zoom:service:ref:loadedGroups:Wharton'::text
            WHEN gmlv.group_name::text = 'penn:community:student:primarySchool:primarySchool_VM'::text THEN 'penn:isc:ait:apps:zoom:service:ref:loadedGroups:Vet'::text
            WHEN gmlv.group_name::text = 'penn:community:student:primarySchool:primarySchool_FA'::text THEN 'penn:isc:ait:apps:zoom:service:ref:loadedGroups:Design'::text
            WHEN gmlv.group_name::text = 'penn:community:student:primarySchool:primarySchool_ED'::text THEN 'penn:isc:ait:apps:zoom:service:ref:loadedGroups:GSE'::text
            WHEN gmlv.group_name::text = 'penn:community:student:primarySchool:primarySchool_EG'::text THEN 'penn:isc:ait:apps:zoom:service:ref:loadedGroups:SEAS'::text
            WHEN gmlv.group_name::text = 'penn:community:student:primarySchool:primarySchool_NU'::text THEN 'penn:isc:ait:apps:zoom:service:ref:loadedGroups:Nursing'::text
            WHEN gmlv.group_name::text = 'penn:community:student:primarySchool:primarySchool_SW'::text THEN 'penn:isc:ait:apps:zoom:service:ref:loadedGroups:SP2'::text
            WHEN gmlv.group_name::text = 'penn:community:student:primarySchool:primarySchool_MD'::text THEN 'penn:isc:ait:apps:zoom:service:ref:loadedGroups:PSOM'::text
            WHEN gmlv.group_name::text = 'penn:community:student:primarySchool:primarySchool_LW'::text THEN 'penn:isc:ait:apps:zoom:service:ref:loadedGroups:Law'::text
            WHEN gmlv.group_name::text = 'penn:community:student:primarySchool:primarySchool_DM'::text THEN 'penn:isc:ait:apps:zoom:service:ref:loadedGroups:Dental_Medicine'::text
            ELSE 'penn:isc:ait:apps:zoom:service:ref:loadedGroups:pennGeneral'::text
        END AS group_name,
    'pennperson'::text AS subject_source_id,
    gmlv.subject_id
   FROM penn_primary_affiliation ppa,
    grouper_memberships_lw_v gmlv
  WHERE ppa.affiliation::text = 'STU'::text A
```

## Zoom automated excludes

There is a folder with automated excludes for users from the main account. We want the dept who is excluding the user to be known, so the custom UI can point the user to the right place, or so the subaccount can leverage the same group as an include in that subaccout. If a user is in the auto-excludes and auto-includes, the autoexcludes take precendence.

The loader query is similar to the include query. It splits students and top and unions with employees at bottom. Note that determining the correct group is a little complicated.

```
  create or replace
view penngrouper.penn_zoom_loaded_excl_gr_v as
select
	case
		when gmlv.group_name::text = any (array['penn:community:student:primaryDivision:primaryDivision_BMP'::text, 'penn:community:student:primaryDivision:primaryDivision_MDP'::text, 'penn:community:student:primaryDivision:primaryDivision_MED'::text]) then 'penn:isc:ait:apps:zoom:service:ref:loadedGroupsForExclude:psomExcludeLoaded'::text
		else null::text
	end as group_name,
	'pennperson'::text as subject_source_id,
	gmlv.subject_id
from
	penn_primary_affiliation ppa,
	grouper_memberships_lw_v gmlv
where
	ppa.affiliation::text = 'STU'::text
	and ppa.penn_id::text = gmlv.subject_id::text
	and gmlv.subject_source::text = 'pennperson'::text
	and gmlv.list_name::text = 'members'::text
	and (gmlv.group_name::text = any (array['penn:community:student:primaryDivision:primaryDivision_BMP'::text, 'penn:community:student:primaryDivision:primaryDivision_MDP'::text, 'penn:community:student:primaryDivision:primaryDivision_MED'::text]))
union all
select
	case
		when gmlv.group_name::text ~~ 'penn:community:employee:affiliationsByCenter:affiliationsByCenter_89:%'::text then 'penn:isc:ait:apps:zoom:service:ref:loadedGroupsForExclude:hireItExcludeLoaded'::text
		when gmlv.group_name::text ~~ 'penn:community:employee:affiliationsByCenter:affiliationsByCenter_87:%'::text then 'penn:isc:ait:apps:zoom:service:ref:loadedGroupsForExclude:financeExcludeLoaded'::text
		when gmlv.group_name::text = 'penn:community:employee:org:WXPN:WXPN_rolluporg'::text then 'penn:isc:ait:apps:zoom:service:ref:loadedGroupsForExclude:xpnExcludeLoaded'::text
		when gmlv.group_name::text ~~ 'penn:community:employee:affiliationsByCenter:affiliationsByCenter_79:%'::text then 'penn:isc:ait:apps:zoom:service:ref:loadedGroupsForExclude:publicsafetyExcludeLoaded'::text
		when gmlv.group_name::text ~~ 'penn:community:employee:affiliationsByCenter:affiliationsByCenter_24:%'::text then 'penn:isc:ait:apps:zoom:service:ref:loadedGroupsForExclude:driaExcludeLoaded'::text
		when gmlv.group_name::text ~~ 'penn:community:employee:affiliationsByCenter:affiliationsByCenter_40:%'::text then 'penn:isc:ait:apps:zoom:service:ref:loadedGroupsForExclude:psomExcludeLoaded'::text
		when gmlv.group_name::text ~~ 'penn:community:employee:affiliationsByCenter:affiliationsByCenter_93:%'::text then 'penn:isc:ait:apps:zoom:service:ref:loadedGroupsForExclude:businessServicesExcludeLoaded'::text
		when gmlv.group_name::text ~~ 'penn:community:employee:affiliationsByCenter:affiliationsByCenter_50:%'::text then 'penn:isc:ait:apps:zoom:service:ref:loadedGroupsForExclude:libraryExcludeLoaded'::text
		when gmlv.group_name::text ~~ 'penn:community:employee:affiliationsByCenter:affiliationsByCenter_90:%'::text then 'penn:isc:ait:apps:zoom:service:ref:loadedGroupsForExclude:darExcludeLoaded'::text
		when gmlv.group_name::text ~~ 'penn:community:employee:affiliationsByCenter:affiliationsByCenter_85:%'::text then
		case
			when (exists (
			select
				1
			from
				grouper_memberships_lw_v gmlv2
			where
				gmlv2.list_name::text = 'members'::text
				and (gmlv2.group_name::text = any (array['penn:community:employee:org:8525:8525_personorg'::character varying::text, 'penn:community:employee:org:8524:8524_personorg'::character varying::text, 'penn:community:employee:org:8534:8534_personorg'::character varying::text, 'penn:community:employee:org:8530:8530_personorg'::character varying::text, 'penn:community:employee:org:8535:8535_personorg'::character varying::text, 'penn:community:employee:org:8558:8558_personorg'::character varying::text]))
				and gmlv2.member_id::text = gmlv.member_id::text)) then 'penn:isc:ait:apps:zoom:service:ref:loadedGroupsForExclude:hireItExcludeLoaded'::text
			else 'penn:isc:ait:apps:zoom:service:ref:loadedGroupsForExclude:vpulExcludeLoaded'::text
		end
		when gmlv.group_name::text ~~ 'penn:community:employee:affiliationsByCenter:affiliationsByCenter_86:%'::text then 'penn:isc:ait:apps:zoom:service:ref:loadedGroupsForExclude:chasExcludeLoaded'::text
		else null::text
	end as group_name,
	'pennperson'::text as subject_source_id,
	gmlv.subject_id
from
	penn_primary_affiliation ppa,
	grouper_memberships_lw_v gmlv
where
	(ppa.affiliation::text = any (array['CTWK'::character varying::text, 'ERF'::character varying::text, 'FAC'::character varying::text, 'SERV'::character varying::text, 'STAF'::character varying::text, 'TEMP'::character varying::text]))
	and ppa.penn_id::text = gmlv.subject_id::text
	and gmlv.subject_source::text = 'pennperson'::text
	and gmlv.list_name::text = 'members'::text
	and (gmlv.group_name::text = 'penn:community:employee:org:WXPN:WXPN_rolluporg'::text
	or gmlv.group_name::text ~~ 'penn:community:employee:affiliationsByCenter:affiliationsByCenter_89:%'::text
	or gmlv.group_name::text ~~ 'penn:community:employee:affiliationsByCenter:affiliationsByCenter_90:%'::text
	or gmlv.group_name::text ~~ 'penn:community:employee:affiliationsByCenter:affiliationsByCenter_85:%'::text
	or gmlv.group_name::text ~~ 'penn:community:employee:affiliationsByCenter:affiliationsByCenter_40:%'::text
	or gmlv.group_name::text ~~ 'penn:community:employee:affiliationsByCenter:affiliationsByCenter_50:%'::text
	or gmlv.group_name::text ~~ 'penn:community:employee:affiliationsByCenter:affiliationsByCenter_87:%'::text
	or gmlv.group_name::text ~~ 'penn:community:employee:affiliationsByCenter:affiliationsByCenter_93:%'::text
	or gmlv.group_name::text ~~ 'penn:community:employee:affiliationsByCenter:affiliationsByCenter_86:%'::text
	or gmlv.group_name::text ~~ 'penn:community:employee:affiliationsByCenter:affiliationsByCenter_24:%'::text
	or gmlv.group_name::text ~~ 'penn:community:employee:affiliationsByCenter:affiliationsByCenter_79:%'::text)
	and gmlv.group_name::text !~~ 'penn:community:employee:affiliationsByCenter:affiliationsByCenter_%_ERF'::text;
```

## Zoom security privileges

Each dept has helpdesk workers. There are administrative roles for these users in Zoom so they can manage their users. Local Support Providers are expected to only manage their dept's meetings and users. The groups of these users are ad hoc though they must be active at penn and must be enrolled in MFA. The "superadmins" for the dept can manage the list of admins and superadmins (delegation).

LSPs are the junior admin role

Admin is the other role

## Ad hoc includes groups

Each dept has an ad hoc includes group that takes precedence over automated includes and excludes.

A dept might claim or sponsor a person if

- They do not have the right affiliation
- or: They should show up in their dept's zoom "group" instead of another
- or: They are slated for a subaccount based on their primary affiliation but should be in the main account

Each dept has their own ad hoc override group and any helpdesk employee can add/remove people from their group

Note these group extensions in Grouper match the "group" names in Zoom. Note: some depts have added their own automatic or manual groups to their ad hoc group

The PSOM includes are interesting... the PSOM population is intersected with the "loaded from zoom" group of people with an active user status in Zoom in the main account. These are added as includes. So essentially if you have an account in zoom and you are in PSOM, then you will be able to sign in

## Ad hoc excludes groups

Each dept has an ad hoc excludes group that they can remove people from their Zoom group (which if they arent in another Zoom group in the main account would take them out of the main account entirely). This level of overrides has the highest priority.

## Zoom groups

Each user in zoom should be sent from SSO with one and only one group. If they have multiple groups then the experience is confusing. We manage this single group requirement with daemon jobs. These groups are sent as an entitlement through SAML Shibboleth SSO.

This is a difficult proposition since there is a lot of logic to determine if there is a group, and if so, which one it should be

1. Manual excludes
  
  
  
  1. If someone is in a manual exclude then they are not in the main account
  2. We keep track of who excluded them (one and only one), and the most recent exclude wins. This is for the custom error page
2. Manual overrides (includes)
  
  
  
  1. If someone is not in the manual exclude, and they are in the manual include, then they are included
  2. If they are in multiple manual include groups, the most recent wins
3. Automatic excludes
  
  
  
  1. If someone is not in the above cases, and they are in the automatic excludes, then they are excluded
  2. There should be one and only one automatic exclude for a person since it is based on primary affiliation and primary dept
4. Automatic includes (fac/stu/staf/etc)
  
  
  
  1. If someone does not match the above cases, and are automatically included, then they are included
  2. There should be one and only one automatic include for a person since it is based on primary affiliation and primary dept

If someone is not in a dept, then they are put in the "pennGeneral" group which is the catchall.

There is a query that could figure out this logic, but it does not perform well, so we will do a temporary table with the first part of the query

Note, these point in time views now exist in Grouper, but we set this up before that happened, so these use copies of those views. You can substitute the Penn views with the new Grouper views to make this work

This helper view lists all the cases the user has and the timestamp from Grouper Point-in-Time

This is the view. Note in some cases we need to edit the extension since some folders use different naming conventions (oops). Note the 4 unions of 4 cases listed above (auto excluded, ad hoc included, etc)

```

 create or replace
view penngrouper.penn_zoom_groups_v as
select
	'override'::text as the_type,
	gg.extension as group_extension,
	gpm.subject_id,
	ppmlv.the_start_time / 1000000 as the_start_time_seconds
from
	penn_pit_memberships_lw_v ppmlv,
	grouper_pit_groups gpg,
	grouper_pit_members gpm,
	grouper_pit_fields gpf,
	grouper_groups gg
where
	gpg.id::text = ppmlv.owner_group_id::text
	and gpg.source_id::text = gg.id::text
	and ppmlv.member_id::text = gpm.id::text
	and gpm.subject_source::text = 'pennperson'::text
	and gpf.name::text = 'members'::text
	and gpf.id::text = ppmlv.field_id::text
	and ppmlv.the_active = 'T'::text
	and gg.name::text ~~ 'penn:isc:ait:apps:zoom:service:ref:groupsOverride:%'::text
union all
select
	'loaded'::text as the_type,
	gg.extension as group_extension,
	gpm.subject_id,
	ppmlv.the_start_time / 1000000 as the_start_time_seconds
from
	penn_pit_memberships_lw_v ppmlv,
	grouper_pit_groups gpg,
	grouper_pit_members gpm,
	grouper_pit_fields gpf,
	grouper_groups gg
where
	gpg.id::text = ppmlv.owner_group_id::text
	and gpg.source_id::text = gg.id::text
	and ppmlv.member_id::text = gpm.id::text
	and gpm.subject_source::text = 'pennperson'::text
	and gpf.name::text = 'members'::text
	and gpf.id::text = ppmlv.field_id::text
	and ppmlv.the_active = 'T'::text
	and gg.name::text ~~ 'penn:isc:ait:apps:zoom:service:ref:loadedGroups:%'::text
union all
select
	'excludeAdhoc'::text as the_type,
	"substring"(gg.extension::text, 1, length(gg.extension::text) - length('AdhocExcludeFromZoom'::text)) as group_extension,
	gpm.subject_id,
	ppmlv.the_start_time / 1000000 as the_start_time_seconds
from
	penn_pit_memberships_lw_v ppmlv,
	grouper_pit_groups gpg,
	grouper_pit_members gpm,
	grouper_pit_fields gpf,
	grouper_groups gg
where
	gpg.id::text = ppmlv.owner_group_id::text
	and gpg.source_id::text = gg.id::text
	and ppmlv.member_id::text = gpm.id::text
	and gpm.subject_source::text = 'pennperson'::text
	and gpf.name::text = 'members'::text
	and gpf.id::text = ppmlv.field_id::text
	and ppmlv.the_active = 'T'::text
	and gg.name::text ~~ 'penn:isc:ait:apps:zoom:service:ref:excludeAdHoc:%'::text
union all
select
	'excludeLoaded'::text as the_type,
	"substring"(gg.extension::text, 1, length(gg.extension::text) - length('ExcludeLoaded'::text)) as group_extension,
	gmlv.subject_id,
	'-1'::integer as the_start_time_seconds
from
	grouper_memberships_lw_v gmlv,
	grouper_groups gg
where
	gmlv.subject_source::text = 'pennperson'::text
	and gmlv.list_name::text = 'members'::text
	and gmlv.group_name::text ~~ 'penn:isc:ait:apps:zoom:service:ref:loadedGroupsForExclude:%'::text
	and gmlv.group_name::text = gg.name::text;
```

Now we sync that to a table and index it

```
create table penn_zoom_groups as select * from penn_zoom_groups_v;

CREATE INDEX penn_zoom_groups_idx ON penngrouper.penn_zoom_groups USING btree (the_type, subject_id, the_start_time_seconds);
```

grouper.client.properties

```
grouperClient.syncTable.zoomGroups.databaseFrom = grouper
grouperClient.syncTable.zoomGroups.databaseTo = grouper
grouperClient.syncTable.zoomGroups.tableFrom = penn_zoom_groups_v
grouperClient.syncTable.zoomGroups.tableTo = penn_zoom_groups
grouperClient.syncTable.zoomGroups.primaryKeyColumns = the_type, group_extension, subject_id, the_start_time_seconds
grouperClient.syncTable.zoomGroups.columns = the_type, group_extension, subject_id, the_start_time_seconds
```

grouper-loader.properties

```
otherJob.zoomGroups.class = edu.internet2.middleware.grouper.app.tableSync.TableSyncOtherJob
otherJob.zoomGroups.quartzCron = 0 02,17,32,47 * * * ?
otherJob.zoomGroups.grouperClientTableSyncConfigKey = zoomGroups
otherJob.zoomGroups.syncType = fullSyncFull
```

Now we have the data we need in an easy to retrieve format to be able to select which group someone should be in (if any). We just need a loader job based on that table

```
 create or replace
view penngrouper.penn_zoom_groups_calculated_v as
select
	gm.subject_id,
	case
		when (exists (
		select
			1
		from
			penn_zoom_groups pzg
		where
			pzg.subject_id::text = gm.subject_id::text
			and pzg.the_type = 'override'::text)) 
        then (
		select
			pzg.group_extension
		from
			penn_zoom_groups pzg
		where
			pzg.subject_id::text = gm.subject_id::text
			and pzg.the_type = 'override'::text
			and pzg.the_start_time_seconds = ((
			select
				max(pzg2.the_start_time_seconds) as max
			from
				penn_zoom_groups pzg2
			where
				gm.subject_id::text = pzg2.subject_id::text
				and pzg2.the_type = 'override'::text))
		limit 1)
		when (exists (
		select
			1
		from
			penn_zoom_groups pzg
		where
			pzg.subject_id::text = gm.subject_id::text
			and pzg.the_type = 'excludeLoaded'::text)) 
        then null::character varying
		else (
		select
			pzg.group_extension
		from
			penn_zoom_groups pzg
		where
			pzg.subject_id::text = gm.subject_id::text
			and pzg.the_type = 'loaded'::text
		limit 1)
	end as group_extension
from
	grouper_members gm
where
	gm.subject_source::text = 'pennperson'::text
	and (gm.subject_id::text in (
	select
		pzg.subject_id
	from
		penn_zoom_groups pzg
	where
		pzg.the_type = any (array['override'::text, 'loaded'::text])))
	and not (gm.subject_id::text in (
	select
		pzg.subject_id
	from
		penn_zoom_groups pzg
	where
		pzg.the_type = 'excludeAdhoc'::text));
```

We need one exclude reason for the error page, so we can do a similar view and loader job for that

```

CREATE OR REPLACE VIEW penngrouper.penn_zoom_groups_calc_excl_v
AS SELECT gm.subject_id,
        CASE
            WHEN (EXISTS ( SELECT 1
               FROM penn_zoom_groups pzg
              WHERE pzg.subject_id::text = gm.subject_id::text AND pzg.the_type = 'excludeAdhoc'::text)) 
            THEN ( SELECT pzg.group_extension
               FROM penn_zoom_groups pzg
              WHERE pzg.subject_id::text = gm.subject_id::text AND pzg.the_type = 'excludeAdhoc'::text AND pzg.the_start_time_seconds = (( SELECT max(pzg2.the_start_time_seconds) AS max
                       FROM penn_zoom_groups pzg2
                      WHERE gm.subject_id::text = pzg2.subject_id::text AND pzg2.the_type = 'excludeAdhoc'::text))
             LIMIT 1)
            WHEN (EXISTS ( SELECT 1
               FROM penn_zoom_groups pzg
              WHERE pzg.subject_id::text = gm.subject_id::text AND pzg.the_type = 'override'::text)) THEN NULL::character varying
            ELSE ( SELECT pzg.group_extension
               FROM penn_zoom_groups pzg
              WHERE pzg.subject_id::text = gm.subject_id::text AND pzg.the_type = 'excludeLoaded'::text
             LIMIT 1)
        END AS group_extension
   FROM grouper_members gm
  WHERE gm.subject_source::text = 'pennperson'::text AND (gm.subject_id::text IN ( SELECT pzg.subject_id
           FROM penn_zoom_groups pzg
          WHERE pzg.the_type = ANY (ARRAY['excludeLoaded'::text, 'excludeAdhoc'::text])));
```

## Zoom can log in to main account

In the diagram above you see one "zoomCanLogIn" policy group that contains all the "zoom group" groups as members. If someone is in this group, then our IdP lets them SSO to Zoom. If not they go to the custom dynamic Grouper error page

## Zoom roles

Each user in zoom should be sent from SSO with one and only one role. If they have multiple roles then zoom will use one of them and the experience can be choatic. We manage this single role requirement with a daemon job. These roles (modeled as policy groups in Grouper) are sent as an entitlement through SAML Shibboleth SSO.

This is a similar concept to the single group algorithm, but it is a lot simpler.

- If a user is an admin, then they have the Admin role
- If a user is an LSP (Local Service Provider), then they have the LSP role
- Else the user is a Member

Loader view:

```

-- penngrouper.penn_zoom_roles_calculated_v source
 create or replace
view penngrouper.penn_zoom_roles_calculated_v as
select
	gm.subject_id,
	case
		when (exists (
		select
			1
		from
			grouper_memberships_lw_v
		where
			grouper_memberships_lw_v.group_name::text = 'penn:isc:ait:apps:zoom:security:zoomSchoolCenterAdmins'::text
			and grouper_memberships_lw_v.list_name::text = 'members'::text
			and grouper_memberships_lw_v.subject_source::text = 'pennperson'::text
			and gm.id::text = grouper_memberships_lw_v.member_id::text)) 
        then 'penn:isc:ait:apps:zoom:service:policy:rolesToGoToZoom:Admin'::text
		when (exists (
		select
			1
		from
			grouper_memberships_lw_v
		where
			grouper_memberships_lw_v.group_name::text = 'penn:isc:ait:apps:zoom:security:zoomSchoolCenterLsps'::text
			and grouper_memberships_lw_v.list_name::text = 'members'::text
			and grouper_memberships_lw_v.subject_source::text = 'pennperson'::text
			and gm.id::text = grouper_memberships_lw_v.member_id::text)) 
        then 'penn:isc:ait:apps:zoom:service:policy:rolesToGoToZoom:LSP'::text
		else 'penn:isc:ait:apps:zoom:service:policy:rolesToGoToZoom:Member'::text
	end as group_name
from
	grouper_members gm
where
	(exists (
	select
		1
	from
		grouper_memberships_lw_v
	where
		grouper_memberships_lw_v.group_name::text = 'penn:isc:ait:apps:zoom:service:policy:zoomCanLogIn'::text
		and grouper_memberships_lw_v.list_name::text = 'members'::text
		and grouper_memberships_lw_v.subject_source::text = 'pennperson'::text
		and gm.id::text = grouper_memberships_lw_v.member_id::text));
```

## Provisioning to zoom groups

The Grouper Zoom provisioner can send memberships to Zoom via real time and batch web service calls. We set this up and it worked but then we decided to only use SAML entitlements instead. We might use this in the future for deprovisioning

Setup the Zoom provisioner endpoint

grouper-loader.properties

```
zoom.pennZoomProd.endpoint = https://api.zoom.us/v2
zoom.pennZoomProd.masterAccountId = ********
zoom.pennZoomProd.jwtApiKey = ********
zoom.pennZoomProd.jwtApiSecretPassword = *******
zoom.pennZoomProd.folderToProvision = penn:isc:ait:apps:zoom:service:policy:groups
zoom.pennZoomProd.subjectAttributeForZoomEmail = EPPN
zoom.pennZoomProd.sourcesForSubjects = pennperson# One issue with Zoom is users sign up with email address and we had licenses there before SSO 
# and some subaccounts dont use SSO.  People at Penn do not all use pennkey@example.com by default.  
# So, this is experimental, but we dont have email as a "subject identifier" so there is an email lookup table 
# used to help match user when loading them from Zoom, or in the feature to normalize the email address 
# (change it from what it was to pennkey@example.com).  
zoom.pennZoomProd.emailLookupDbConfigId = pennCommunity
zoom.pennZoomProd.emailLookupQuery = select LOWER_EMAIL_ADDRESS, CHAR_PENN_ID, 'pennperson' as subject_source_id from person_source_email_lookup where lower_email_address in ($$lowerEmailAddresses$$)
zoom.pennZoomProd.groupNameToNormalizeEmailAddress = penn:isc:ait:apps:zoom:service:policy:zoomNormalizeEmailAddress

# This group can be used to delete users.  The provisioner will delete their license in Zoom 
# and remove them from this group.  Or it can just log it while testing
zoom.pennZoomProd.groupNameToDeleteUsers = penn:isc:ait:apps:zoom:service:ref:zoomDeprovisioning:zoomDeprovisionNow
zoom.pennZoomProd.logUserDeletesInsteadOfDeleting = true

```

This is the real time provisioning config:

grouper-loader.properties

```
changeLog.consumer.zoomEsbProd.zoomConfigId = pennZoomProd
changeLog.consumer.zoomEsbProd.class = edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer
changeLog.consumer.zoomEsbProd.quartzCron = 0 * 0,1,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23 * * ?
changeLog.consumer.zoomEsbProd.publisher.class = edu.internet2.middleware.grouper.app.zoom.ZoomEsbPublisher
changeLog.consumer.zoomEsbProd.elfilter = (event.sourceId == null || event.sourceId eq 'pennperson') && (event.groupName =~ '^penn:isc:ait:apps:zoom:service:policy:groups:.*$' || event.name =~ '^penn:isc:ait:apps:zoom:service:policy:groups:.*$' || event.groupName eq 'penn:isc:ait:apps:zoom:service:policy:zoomNormalizeEmailAddress' || event.name eq 'penn:isc:ait:apps:zoom:service:policy:zoomNormalizeEmailAddress' || event.groupName eq 'penn:isc:ait:apps:zoom:service:ref:zoomDeprovisioning:zoomDeprovisionNow' || event.name eq 'penn:isc:ait:apps:zoom:service:ref:zoomDeprovisioning:zoomDeprovisionNow' ) && (event.eventType eq 'GROUP_DELETE' || event.eventType eq 'GROUP_ADD' || event.eventType eq 'GROUP_UPDATE' || event.eventType eq 'MEMBERSHIP_DELETE' || event.eventType eq 'MEMBERSHIP_ADD' || event.eventType eq 'MEMBERSHIP_UPDATE')
changeLog.consumer.zoomEsbProd.publisher.addSubjectAttributes = EPPN

```

This is the full-sync

grouper-loader.properties

```
otherJob.zoomFullSyncProd.class = edu.internet2.middleware.grouper.app.zoom.GrouperZoomFullSync
otherJob.zoomFullSyncProd.quartzCron = 0 20 2 * * ?
otherJob.zoomFullSyncProd.zoomConfigId = pennZoomProd
```

## Loading data from zoom

Note: in v2.6.1+ the zoom connector can sync zoom users to a table in Grouper. This helps since not all users in zoom match to subjects in Grouper. See the [Penn Zoom Deprovisioning](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549445/Penn+Zoom+Deprovisioning) page

There is a "loader" from zoom to load populations. There are a lot of things to load from Zoom. These can be used for some purpose or just for logging, auditing, and troubleshooting.

Config for loader (all these configs are in grouper-loader.properties)

```
otherJob.pennZoomLoader.class = edu.internet2.middleware.grouper.app.zoom.GrouperZoomLoader
otherJob.pennZoomLoader.quartzCron = 0 40 * * * ?
otherJob.pennZoomLoader.zoomConfigId = pennZoomProd
otherJob.pennZoomLoader.zoomLoadGroups = true
otherJob.pennZoomLoader.zoomLoadGroupsFolderName = penn:isc:ait:apps:zoom:service:ref:groupsLoadedFromZoom
otherJob.pennZoomLoader.zoomLoadRoles = true
otherJob.pennZoomLoader.zoomLoadRolesFolderName = penn:isc:ait:apps:zoom:service:ref:rolesLoadedFromZoom
otherJob.pennZoomLoader.zoomLoadSubAccounts = true
otherJob.pennZoomLoader.zoomLoadSubAccountsFolderName = penn:isc:ait:apps:zoom:service:ref:subaccountsLoadedFromZoom
otherJob.pennZoomLoader.zoomLoadUserTypes = true
otherJob.pennZoomLoader.zoomLoadUserTypesFolderName = penn:isc:ait:apps:zoom:service:ref:userTypesLoadedFromZoom
otherJob.pennZoomLoader.zoomLoadUserStatuses = true
otherJob.pennZoomLoader.zoomLoadUserStatusesFolderName = penn:isc:ait:apps:zoom:service:ref:userStatusesLoadedFromZoom

# new in v2.6.1+
otherJob.pennZoomLoader.zoomLoadUsersToTable = true
```

Groups - Note, these numbers are different from the groups to go to zoom since these are people who have actually logged in, not people who are eligible to log in

Roles from zoom

User types

User statuses

Sub accounts

## Scripting the onboarding of a zoom dept

To onboard a new dept in Zoom, a few views needs to be edited with the dept code. Also a bunch of groups need to be setup. Here is a GSH script that can accomplish this

```
GrouperSession grouperSession = GrouperSession.startRootSession();
String prefix = "Xpn";
String prefixLower = prefix.toLowerCase();
Group excludeAdHocGroup = new GroupSave(grouperSession).assignName("penn:isc:ait:apps:zoom:service:ref:excludeAdHoc:" + prefixLower + "AdhocExcludeFromZoom").save();
Group excludeLoadedGroup = new GroupSave(grouperSession).assignName("penn:isc:ait:apps:zoom:service:ref:loadedGroupsForExclude:" + prefixLower + "ExcludeLoaded").save();
Group excludeGroup = new GroupSave(grouperSession).assignName("penn:isc:ait:apps:zoom:service:ref:excludeFromZoom:" + prefixLower + "ExcludeFromZoom").save();
excludeGroup.addMember(excludeAdHocGroup.toSubject(), false);
excludeGroup.addMember(excludeLoadedGroup.toSubject(), false);
Group excludedFromZoom = GroupFinder.findByName(grouperSession, "penn:isc:ait:apps:zoom:service:ref:usersExcludedFromZoom", true);
excludedFromZoom.addMember(excludeGroup.toSubject(), false);
Group schoolLspGroup = new GroupSave(grouperSession).assignName("penn:isc:ait:apps:zoom:security:schoolCenterAdminsAndLsps:zoom" + prefix + "Lsps").save();
Group lsps = GroupFinder.findByName(grouperSession, "penn:isc:ait:apps:zoom:security:zoomSchoolCenterLspsPreCheck", true);
lsps.addMember(schoolLspGroup.toSubject(), false);
Group schoolAdminGroup = new GroupSave(grouperSession).assignName("penn:isc:ait:apps:zoom:security:schoolCenterAdminsAndLsps:zoom" + prefix + "Admins").save();
Group admins = GroupFinder.findByName(grouperSession, "penn:isc:ait:apps:zoom:security:zoomSchoolCenterAdminsPreCheck", true);
admins.addMember(schoolAdminGroup.toSubject(), false);
```

## Subaccount managed in Grouper

We have one subaccount (PSOM) managed in Grouper. We have users and roles setup similar to above. There are two groups for HIPAA and non-HIPAA (if the user attests that they can opt out and record meetings)

Basically there are some loader jobs, one group, one role, and some ad hoc includes/excludes

## Grouper "Custom UI" error page

There is a Grouper [Custom UI](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549064/Grouper+Custom+UI) for Zoom so users get a custom error page so they know why they dont have access or they get a link of a subaccount or other account to go to instead of Penn's main account

If the user is excluded by Wharton, they get this error message

## Grouper "Custom UI" help desk access analysis

There is a separate (for performance reasons) [Custom UI](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549064/Grouper+Custom+UI) for help desk workers

## Daemons

Generally changes made to zoom data will process and sync to weblogin/zoom in less than an hour. The weblogin sync is every 15 minutes.

| **Job** | **Schedule** | **Takes** | **Description** |
| --- | --- | --- | --- |
| [affiliationPrimaryConfig](https://grouper.apps.upenn.edu/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Admin.viewLogs&jobName=SQL_GROUP_LIST__penn:community:employee:affiliationPrimaryConfig__fa9dca910f9a4accb8529dd040dc1198) | Every hour at :40 | 2 minutes | Assigns people to their primary affiliation in PennGroups (Note: this is the primary affil. from PennComm Direct which we think is the Directory primary affil, but not 100% sure) |
| [OTHER_JOB_primaryAffiliation](https://grouper.apps.upenn.edu/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Admin.viewLogs&jobName=OTHER_JOB_primaryAffiliation) | Every hour at :45 | 4 seconds | Temp table of primary affiliation into a table for each calculations (Note: the queries behind this are really complex so we get a view, sync it to a table and use the table for the rest of the job) |
| [loadedGroupsLoader](https://grouper.apps.upenn.edu/grouper/grouperUi/app/UiV2Main.index?operation=UiV2GrouperLoader.viewLogs&groupId=cbdb3feab9b84f3583f874ae557a81b7) | Every 15 minutes at: :01, :16, :31 and :46 | 8 seconds | Loads populations for groups. e.g. if primary employee, and primary center is SAS, then in SAS group |
| [excludedGroupsLoader](https://grouper.apps.upenn.edu/grouper/grouperUi/app/UiV2Main.index?operation=UiV2GrouperLoader.viewLogs&groupId=a565b9b006c148d18f09af7b6b5397e2) | Every 15 minutes at: :01, :16, :31 and :46 | 1 second | Loads populations for automatically managed excludes. e.g. if primary employee, and primary center is Wharton, then in Wharton loaded exclude group |
| [OTHER_JOB_zoomGroups](https://grouper.apps.upenn.edu/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Admin.daemonJobs) | Every 15 minutes at :02, :17, :32 and :47 | 2 seconds | Temp table of overrides, excludes, auto-population and timestamp of when added |
| [calculatedGroupsLoader](https://grouper.apps.upenn.edu/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Admin.viewLogs&jobName=SQL_GROUP_LIST__penn:isc:ait:apps:zoom:service:loader:calculatedGroupsLoader__ab8a0a62db9b4f1f8ed4c8d20314cb5c) | Every 15 minutes at :03, :18, :33 and :48 | 8 seconds | Assigns the proper one group: pennGeneral, SAS, SEAS, Wharton, Nursing, etc |
| [calculatedExcludeGroupsLoader](https://grouper.apps.upenn.edu/grouper/grouperUi/app/UiV2Main.index?operation=UiV2GrouperLoader.viewLogs&groupId=59ee6134481b4cbc94121f8493ac06ad) | Every 15 minutes at :03, :18, :33 and :48 | 8 seconds | Assigns the proper one exclude group to the user for the error page |
| [calculatedRolesLoader](https://grouper.apps.upenn.edu/grouper/grouperUi/app/UiV2Main.index?operation=UiV2GrouperLoader.viewLogs&groupId=ad0373c2fd7c4addb63f837e3e8c2cb1) | Every 15 minutes at :04, :19, :34 and :49 | 9 seconds | Assigns the proper one role: Admin, LSP, or Member |

Loader and provisioning daemons

| **Job** | **Schedule** | **Takes** | **Description** |
| --- | --- | --- | --- |
| [OTHER_JOB_zoomFullSyncProd](https://grouper.apps.upenn.edu/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Admin.daemonJobs) | Daily at 2:20am | ? | Not really used not, but could be a full sync for deprovisioning groups / roles / userStatus / userType / users |
| [CHANGE_LOG_consumer_zoomEsbProd](https://grouper.apps.upenn.edu/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Admin.daemonJobs) | Every minute near top of minute | ? | Not really used not, but could be an incremental sync for deprovisioning groups / roles / userStatus / userType / users |
| [OTHER_JOB_pennZoomLoader](https://grouper.apps.upenn.edu/grouper/grouperUi/app/UiV2Main.index?operation=UiV2Admin.daemonJobs) | Every hour at :40 | 8 seconds | Load users, groups, roles, userTypes, userStatus, subaccounts from zoom into Grouper for reporting purposes |
