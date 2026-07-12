---
title: "Grouper membership eligibility requirements"
space: Grouper
pageId: 28549482
version: 21
lastUpdated: 2026-07-01T05:41:56.371Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549482/Grouper+membership+eligibility+requirements
---

This applies to Grouper v2.6.16+   
  
To enforce membership eligibility, you can use a composite, rules, JEXL scripted groups, or you can use this new feature. You can link an attribute with an eligibility group so that immediate memberships (not effective, composite, loaded) from a (probably) manual group will be veto-ed or removed when users are no longer eligible because they are no longer effectively in the eligible population group.

## Steps to implement

These are explained below

1. Identify coarse grained populations that manual groups could be constrained to
2. Create attribute def(s) and attribute names. 
  
  1. Make as many attribute defs as you want (equal to or less than the number of populations)
  2. The number of attribute names is 1-to-1 to the coarse grained populations.
3. Allow certain groups to be able to read and assign the attribute (e.g. power users)
4. Configure the attribute in grouper.properties to be linked to an eligibility group (e.g. employees)
5. Configure the veto text in the externalized text file
6. Configure the attribute to be assigned in the group edit screen (optional)
  
  1. We can allow attributes on stem edit screens in future
7. Assign the attribute to groups or folders
  
  1. Note, loader groups are not affected
8. Membership hook will veto membership adds if the user is not eligible
9. Change log consumer will remove members when no longer eligible
10. Full sync daemon will make sure everything is correct
11. When members are removed a record is kept in the grouper_mship_req_change table
  
  1. A simple GSH script could easily rollback changes made by this module

## Identify coarse grained populations

Note, these do not need to be direct members.

## Create attributes

These settings need to be exactly like this. Needs to not be multi-assignable or have a value...

## Allow certain people to be able to read and assign the attribute (e.g. power users)

## Configure the attribute in grouper.properties to be linked to an eligibility group (e.g. employees)

```
# ui key to externalize text (error message)
grouper.membershipRequirement.requireEmployee.uiKey = vetoRequireEmployee

# attribute name that signifies this requirement
grouper.membershipRequirement.requireEmployee.attributeName = etc:attribute:membershipRequirement:requireEmployee

# group name which is the population group
grouper.membershipRequirement.requireEmployee.requireGroupName = ref:employee

# if the overall hook is enabled, is the hook for this specific config enabled?  defaults to true.
grouper.membershipRequirement.requireEmployee.hookEnable = true
```

Here is the base config which has general settings

```
#########################################
## Custom veto composites membership requirement
## This feature allows users to auto-veto ineligible members or remove them when they become ineligible.
## Note that each custom composite also needs to be defined in the Grouper UI text properties in order 
## to provide a friendly description in the UI.  customCompositeMinusEmployees and customCompositeIntersectIt are also defined as examples there.
#########################################

# how long should logs of membership requirement logs be stored in database?
grouper.membershipRequirement.keepLogsForDays = 90

# should hook for membership veto be enabled
grouper.membershipRequirement.hookEnable = true

# should changeLog for membership veto change log be enabled in general
grouper.membershipRequirement.changeLogEnable = true

# ui key to externalize text
#grouper.membershipRequirement.someConfigId.uiKey = customVetoCompositeRequireEmployee

# attribute name that signifies this requirement
#grouper.membershipRequirement.someConfigId.attributeName = etc:attribute:customComposite:requireEmployee

# group name which is the population group
#grouper.membershipRequirement.someConfigId.requireGroupName = org:centralIt:staff:itStaff

# if the overall hook is enabled, is the hook for this specific config enabled?  defaults to true.
#grouper.membershipRequirement.someConfigId.hookEnable = true
```

## Configure the veto text in the externalized text file

"grouper.text.en.us.properties" (veto.membershipVeto.customComposite.%uiKey% = %text you want displayed as error%)

```
veto.membershipVeto.customComposite.vetoRequireEmployee = Only employees can be members of this group
```

## Configure the attribute to be assigned in the group edit screen (optional)

grouper.properties

```
groupScreen.attribute.requireEmployee.attributeName = etc:attribute:membershipRequirement:requireEmployee
groupScreen.attribute.requireEmployee.label = Require employee
groupScreen.attribute.requireEmployee.description = Members of this group (or groups in folder) will be required to be employees, otherwise they will be vetoed or removed
groupScreen.attribute.requireEmployee.index = 1
 
```

## Assign the attribute to groups or folders

Note, this will remove direct members. Note, be careful about service principals, maybe those need to be in a different manual group?

Groups:

Folders

## Membership hook will veto membership adds if the user is not eligible

## Change log consumer will remove members when no longer eligible

## Full sync daemon will make sure everything is correct

## When members are removed a record is kept in the grouper_mship_req_change table

```
select
  gmrc.the_timestamp,
  gg.name as removed_from,
  gm.description,
  gg_elig.name as eligibility_group,
  gadn.name as attribute_name,
  gmrc.config_id,
  case
    when gmrc.engine = 'F' then 'fullDaemon'
    when gmrc.engine = 'C' then 'changeLog'
    else gmrc.engine
  end as engine
from
  grouper_mship_req_change gmrc,
  grouper_members gm,
  grouper_groups gg,
  grouper_groups gg_elig,
  grouper_attribute_def_name gadn
where
  gmrc.member_id = gm.id
  and gmrc.group_id = gg.id
  and gmrc.attribute_def_name_id = gadn.id
  and gmrc.require_group_id = gg_elig.id
order by
  the_timestamp desc;
```

## TO DO

More features can be added to this:

1. Notifications (to managers or users)
2. Grace periods
3. Read-only mode
4. Exclude groups which are "exclude" type (doesn't exist yet)
5. Exclude groups by regex
6. Include only manual groups
7. Constrain subject sources
8. Remove when membership remove in folder (e.g. job or title changes)
9. Loader can restrict ineligible members
  
  1. Confirm that JEXL scripted groups are affected correctly
