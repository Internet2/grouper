---
title: "Grouper ABAC with scripted groups"
space: Grouper
pageId: 28544896
version: 110
lastUpdated: 2026-11-20T21:02:07.434Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544896/Grouper+ABAC+with+scripted+groups
---

## Overview

Grouper does a great job with group relationships and group math. [Basis groups](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543159/Folder+and+Group+Design) can be loaded into Grouper but that is a single relationship from a user to an attribute (group). For instance:

- You can load groups that represent every affiliation with the users who have that affiliation (e.g. faculty, staff, contractors)
- You can load groups that represent organizations with the users who are in those organizations (e.g. business school, engineering school)
- But you cannot take those groups and use group math to calculate which users have a specific affiliation in a specific organization (both are many to many relationships). (e.g. staff/contractors in the business school)
- You would need to load the cross product of the data which is not scalable as the number of attributes increases. e.g. a loader that has groups for staff in business school, contractors in nursing school, etc

ABAC allows you to model rows of data for a user, and then make an ABAC script to specify criteria in that row of data. You could instantly make a group for users who have certain affiliations in certain org in their primary job in a full time capacity. Previously you needed to make a loader job to load a group with a SQL query that can join various data elements from a data warehouse.

Expression language (JEXL) scripts implement the part of ABAC that defines who is included in a policy based on attributes of those users. Other parts of ABAC, such as resource or environment attributes, can be handled with Grouper permissions or by the service that protects the resources.

To implement access policies, it has often been necessary to set up intermediate groups, include/exclude, requirement groups, and [allow/deny manual groups](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547660/Grouper+permissions+allow+and+disallow). Grouper has features to help in this area including: [rules](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545173/Grouper+rules), [hooks](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545347/Grouper+Hooks), templates, move/copy, import/export, and GSH scripts. Scripted groups replace many of those intermediate groups with one script.

Benefits:

- No loader job, and no pre-loaded rollups or cartesian products of reference groups, for each policy
- Composites with any number of factors in one script
- Users who can READ the underlying groups and data can build, analyze, and maintain their own policies
- Memberships update in near real time (incremental daemon, v5.18.0+)

> **Blogs and video**
> 
> 
> 
> - [ABAC to the future](https://incommon.org/news/abac-to-the-future/) (November 2025): using ABAC to reduce the burden of loader jobs
> - [Attribute Based Access Control with Grouper](https://incommon.org/news/new-features-with-grouper/) (February 2022)
> - [Video](https://youtu.be/RLr3mhxU35o)

## Use case

At U Penn over 15 years we now have 700 loader jobs. Only Grouper sysadmins can manage these loader jobs for security reasons. It takes tickets to create the job, update the job, and troubleshoot the data. This valuable staff time is greatly reduced with ABAC. These loader daemons generally do not have real time updates since that is difficult to configure for every job, so hourly full syncs are scheduled which waste resources. There is no way to do grace periods on the source data unless the source database keeps data history (which likely is not the case).

Enter Grouper ABAC, with a few data providers (identity data, student data, payroll data, training data, etc), we can replace 2/3 of our loader jobs with ABAC. The data will flow to Grouper in real time (with fewer data feeds by two orders of magnitude it is feasible to configure real time updates). Grouper keeps history on all the data so grace periods at the row level are available. Each data provider has its own security policies at the column level (with three access levels) which can be securely delegated to eligible power users. Users can use ABAC scripts to configure, troubleshoot, and update their own groups.

A **dynamic data dictionary** informs users which attributes are available, what the values mean, and how to use them. The analysis screen shows the numbers of all the parts of the script, and if test users have each attribute or the result. This troubleshooting in SQL is very time consuming. The groups will be updated in real time and the attribute values change. A single ABAC script can use data from any data provider and even Grouper memberships (which cannot be done with loaders without supporting ETL jobs).

Loaders (to build multiple groups at once) can point to Grouper's attribute repository to have consistent data and real time updates. The reduction in loader jobs from external data sources will reduce the network query traffic and database loads. This ABAC data can be used to replace subject sources, provision rich object representations, manage users' lifecycles, and populate reports.

The ability to **manage groups by rows of data** has been requested for years and will revolutionize access management. It further differentiates Grouper from other IAM products.

## Version history

| **Version** | **Feature** |
| --- | --- |
| v2.6.6 | Scripted groups based on group memberships only. Full sync daemon. |
| v5 | Scripts can use [entity data fields and rows](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545275/Grouper+entity+data+fields+for+ABAC). Scripts are converted to SQL. Analyze policy screen. |
| v5.17.2 | Comparison operators (less than, greater than, etc) |
| v5.18.0 | Incremental daemon |
| v6.1.2+, v7.0.4+ | Subject source configuration, restrict who can edit scripted groups, unresolvable and deleted entities are not eligible |
| v6.1.2+, v7.1.0+ | attributeCompare() and hasAttributeBetween() in rows |
| v7.3.0 | Global variables |

## Configure a scripted group

A scripted group is a group whose loader source type is a JEXL script. On the group, open "Edit loader settings", set "Loader" to "Yes, has loader configuration", set "Source type" to "JEXL script", enter the "Entity JEXL script", and click "Save". You can also pick a "pattern" (see [Grouper ABAC policy pattern GSH templates](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/398393363/Grouper+ABAC+policy+pattern+GSH+templates)) instead of typing the script. The script language is described in [Grouper ABAC script syntax](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/397738094/Grouper+ABAC+script+syntax).

To save a script you need UPDATE on the group and READ on each group the script uses. As of v7.9.1 and v6.5.1, each group the script names must also exist under its current name: a script that names a group that does not exist yet, or a group's old name after a rename, is not saved, and a group created in the last minute or so may not be found yet. Scripts already saved keep running. See Administration below for data field privileges and for restricting who can edit scripted groups.

There is one built-in full daemon and one incremental daemon for all scripted groups. You do not add a daemon per group.

The screenshots below are from v2.6. The first shows the "Loader settings" view of a group named testScript with the script `${ entity.memberOf('test:testGroup0') && entity.memberOf('test:testGroup1') && entity.memberOf('test:testGroup2') }`. The second shows the "Edit loader settings" form with the "Loader", "Source type", and "Entity JEXL script" fields and the "Save" button.

## Analyze policy

To confirm a policy is correct, "Edit loader settings" can analyze the script. Optionally enter a "Member name or ID" and click "Analyze". A table lists the population count for the whole script (first row) and then for each part of the script, with each part described in plain language.

In the screenshot, the group activeSomToBeDeprovisionedAbac has 43 members overall. The parts include: has row cp_user with attribute cp_active (1672), member of group penn:ref:member (1509), and has attribute cp_role value desktop-user (1940).

## How it works

### Script to SQL

The script is parsed and converted to SQL. The results represent the members of the group. The diffs will be added or removed from the group.

### Full sync daemon

The built-in daemon OTHER_JOB_grouperLoaderJexlScriptFullSync recalculates every scripted group. By default it runs hourly. In v2.6.6, which has no incremental daemon, a script change takes effect at the next full sync, or run the job yourself.

The screenshots show the job on the "All daemon jobs" screen (filter "jexl", cron `31 19 * * * ?`), and its "Daemon logs" with the job message: jexlScriptGroups, groupsWithInvalidScripts, distinctGroupsInScripts, inserts, deletes, and errors.

### Incremental daemon (v5.18.0+)

- Runs as a change log consumer
- Looks for changes to groups, data rows, and data fields
- Sees which groups have scripts that use those groups, data rows, and fields
- Skips events that occurred before the last successful full sync started
- Skips events that occurred on scripted groups before the last group sync started
- Tracks group syncs with an attribute in the JEXL group attributes
- If a script is added or is changed, it will perform a group sync
- Group syncs and full syncs will track the dependencies for groups, data fields, and data rows in the database
- If the membership delta is 1500 or more, it will do a group sync on the group
- For incremental actions, the query for those affected users are run against the script and the group, and the adds/deletes are performed
- The incremental and full will not run at the same time

### Membership eligibility

- (v7.0.4+ and v6.1.2+) Unresolvables and deleted entities are not eligible to be a member in scripted groups

## Administration

### Restrict who can edit scripted groups

(v7.0.4+ and v6.1.2+) To restrict who can create or edit scripted (ABAC) groups, set `grouper.abac.edit.if.in.group` in `grouper.base.properties` to a group name (e.g. `etc:abacEditors`). Only members of that group who also have admin privilege on the target group will be able to create or edit scripted groups. If blank (the default), any group admin can create or edit scripted groups. Wheel/root users are always allowed regardless of this setting. Note: To use a group you need READ on the group, to analyze a data field/row you need READ in the privacy realm, to save a new ABAC script, you need UPDATE in the privacy realm for any data field/row.

### Max membership size

To prevent users from configuring large groups that may impact Grouper, users can be restricted based on the membership size of the resulting group.

```
# Max ABAC scripted group membership size limits.
# Configure one or more numbered tiers; each tier is a group whose members may save scripted groups up to maxSize.
# When a user is in multiple tier groups, the highest maxSize wins.  If no tiers are
# configured and no default below is set, there is no limit (the feature is off).
# {valueType: "group", regex: "^grouper\\.abac\\.maxMembershipSizeLimit\\.[^.]+\\.groupName$"}
# grouper.abac.maxMembershipSizeLimit.0.groupName =

# Max size for the associated group.
# {valueType: "integer", regex: "^grouper\\.abac\\.maxMembershipSizeLimit\\.[^.]+\\.maxSize$"}
# grouper.abac.maxMembershipSizeLimit.0.maxSize =

# Base max scripted group membership size for ABAC users who are in none of the tier groups
# above.  Blank means unlimited for those users.
# {valueType: "integer"}
grouper.abac.defaultMaxMembershipSizeLimit =
```

### Subject source configuration

This section is for Grouper versions: v7.0.4+ and v6.1.2+. By default, ABAC JEXL script evaluation considers all institution-defined subject sources (e.g. `myPerson`), excluding Grouper-internal sources (`g:gsa`, `g:isa`) and Grouper-managed sources (`grouperEntities`, `grouperExternal`). Three configuration properties in `grouper.properties` control this behavior:

**Global default subject sources**

`grouper.abac.globalDefaultSubjectSourceIds`

Comma-separated list of subject source IDs to include in ABAC script evaluation when no per-group override is set. If blank (default), all non-internal subject sources are used, excluding `g:gsa`, `g:isa`, `grouperEntities`, and `grouperExternal`. Imagine you had 100 ABAC groups, which subject source or sources would the majority have? That should be your default(s). Generally this should be your one person subject source.

Example: `jdbc, pennperson`

**Allow per-group override**

`grouper.abac.allowUserOverrideSubjectSourceIds`

Boolean (default `false`). When `true`, users with admin privileges on a JEXL script group can override the default subject sources on a per-group basis via the UI.

**Available subject sources for override**

`grouper.abac.availableSubjectSourceIds`

Comma-separated list of subject source IDs that users can choose from when overriding the defaults. If blank, the per-group picker is not shown even if `allowUserOverrideSubjectSourceIds` is `true`. The list must contain at least two sources for the picker to appear.

Example: `jdbc, grouperEntities`

**How it works**

The view and edit screens for a JEXL script group always display checkboxes for all non-internal subject sources, showing which sources are active (checked) and which are not (unchecked).

- **View screen**: Checkboxes are always disabled. They reflect the effective sources for the group (per-group override if set, otherwise the global defaults).
- **Edit screen, override not allowed**: Checkboxes are disabled, showing the global defaults.
- **Edit screen, override allowed**: A radio toggle lets the user choose between "Use default subject sources" (shows disabled checkboxes with global defaults) and "Customize subject sources" (shows editable checkboxes from the available list).  
    
  Default:   
    
    
  Customized:

When per-group subject sources are customized, only sources from the `availableSubjectSourceIds` list are accepted. If a per-group override contains sources not in the available list, those sources are silently ignored. If all per-group sources are invalid, the global defaults are used.

**Including Grouper entities or external subjects**

To include `grouperEntities` (local entities) or `grouperExternal` (external subjects) in ABAC evaluation, add them explicitly to the configuration. They are never included by default but can be configured as global defaults, available sources, or both.

For example, to allow ABAC scripts to evaluate both regular subjects and local entities:

```
grouper.abac.globalDefaultSubjectSourceIds = jdbc
grouper.abac.allowUserOverrideSubjectSourceIds = true
grouper.abac.availableSubjectSourceIds = jdbc, grouperEntities
```

With this configuration, JEXL script groups default to evaluating only `jdbc` subjects. Group admins can override a specific group to also include `grouperEntities`, for example to grant access to both people and service accounts based on group membership or data attributes.

**Incremental sync**

Changing the subject source IDs on a JEXL script group triggers the incremental ABAC daemon to recalculate that group's membership. There is no need to wait for or manually trigger a full sync.

## See also

[Access Management Features Overview](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544689/Access+Management+Features+Overview)
