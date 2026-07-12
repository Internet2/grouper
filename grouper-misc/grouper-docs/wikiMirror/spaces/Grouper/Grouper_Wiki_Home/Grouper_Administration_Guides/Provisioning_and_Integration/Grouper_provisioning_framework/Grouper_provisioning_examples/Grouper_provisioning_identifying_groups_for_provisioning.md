---
title: "Grouper provisioning: identifying groups for provisioning"
space: Grouper
pageId: 28555011
version: 10
lastUpdated: 2026-07-01T05:39:09.073Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555011/Grouper+provisioning+identifying+groups+for+provisioning
---

This is for Grouper 2.5+. Note: we will have an automatic upgrade task to migrate to this system from the legacy pspng provision_to and do_not_provision_to attributes.

To identify a group or folder to be provisioned, use the UI provisioning screens to mark a group or folder as provisionable with the 2.4 generic provisioning attributes.

[See this wiki for an example](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548587/Grouper+provisioning+in+UI)

## Provisionable attribute

Can query with one join to attribute assign and attribute assign value

Note on upgrade to or passed 2.5.42, the full provisioning daemon must run to change provisioningDoProvision from T to provisionerId

TODO Check enabled on both attribute assignments

| Attribute on group/folder | Attribute on assignment | Value | Description |
| --- | --- | --- | --- |
| provisioningMarker |  |  |  |
|  | provisioningTarget | provisionerId | What provisioner is this set of attributes about |
|  | provisioningDoProvision | provisionerId | If provisioning to this target, set the provisionerId here   (or attribute will not be assigned) |
|  | provisioningDirectAssign | T \| F | F if inherited from folder |
|  | provisioningOwnerStemId | abc123 | ID of folder if inherited from there |
|  | provisioningStemScope | ALL \| ONE ? | If assigned to folder, then is it children or descendants |
|  | provisioningMetadataJson | {"a":"b"} | If there is metadata then put name/value pairs here |

### Example

Folder "afolder" provisionable for target  
Folder "afolder : bfolder" not provisionable for target  
Group "afolder : bfolder : cgroup" wouldnt have attributes on it  
Folder "afolder : cfolder" stemScope ONE  
Group "afolder : cfolder : dfolder : egroup" would still get provisioned per the settings on afolder

Folder "afolder : dfolder" provisioning only policy groups  
Group "afolder : dfolder : fgroup" is not a policy group so wouldnt have attributes on it

## Propagating provisioning

Daemon needs to

1. Full sync provisioning attributes
2. Daily
3. Need to not conflict with something doing incremental propogation

Options

1. Provisioning daemon / incremental
2. * In each full daemon / incremental
  
  1. In future could picture a "full attribute provisioning daemon" that doesnt run at same time but doesnt full sync

## New features for provisioning identification

1. By default, if you mark a folder as provisionable, then all groups in that stemScope will be effectively provisionable
2. Globally in the provisioner config, you could set defaults for:
  
  1. If only policy groups (group → grouperObjectTypeMarker → grouperObjectTypeName = "policy") (check enabled)
  2. Regex on group name

## Algorithm (full)

Method kicked off from provisioner full sync

| Step | Description | Details |
| --- | --- | --- |
| Select folders | Select all folders with provisioningTarget = provisionerId | Get all folders and all provisioning attributes for target.  Columns: stemId, stemName, attributeName, attributeValue   ``` SELECT 	gs.id, 	gs.NAME, 	gadn_config.name, 	gaav_config.VALUE_STRING FROM 	grouper_stems gs, 	grouper_attribute_assign gaa_target_marker, 	grouper_attribute_assign gaa_target, 	grouper_attribute_assign_value gaav_target, 	grouper_attribute_def_name gadn_target, 	grouper_attribute_def_name gadn_marker, 	grouper_attribute_assign gaa_config, 	grouper_attribute_assign_value gaav_config, 	grouper_attribute_def_name gadn_config WHERE 	gs.ID = gaa_target_marker.owner_stem_id 	AND gaa_target_marker.enabled = 'T' 	AND gadn_marker.id = gaa_target_marker.attribute_def_name_id 	AND gadn_marker.NAME = 'etc:provisioning:provisioningMarker' 	AND gaa_target_marker.id = gaa_target.owner_attribute_assign_id 	AND gaa_target.enabled = 'T' 	AND gaa_target.attribute_def_name_id = gadn_target.ID 	AND gadn_target.NAME = 'etc:provisioning:provisioningTarget' 	AND gaav_target.ATTRIBUTE_ASSIGN_ID = gaa_target.id 	AND gaav_target.value_string = 'myTargetId' 	AND gaa_target_marker.id = gaa_config.owner_attribute_assign_id 	AND gaa_config.enabled = 'T' 	AND gaav_config.attribute_assign_id = gaa_config.id 	AND gadn_config.id = gaa_config.attribute_def_name_id ``` |
| Select folder id's in folders with attributes | Select all folder id's with an ancestor folder with   provisioningTarget = provisionerId |  |
| Select groups with attribute | Select all groups with provisioningTarget = provisionerId | Get all groups and all provisioning attributes for target |
| Select group id's in folders with attributes | Select all group id's with an ancestor folder with   provisioningTarget = provisionerId | So we know which groups are missing the provisioning   attributes |
| Assign minimal missing provisioning attributes | If a folder/group is in a folder (and stemScope) of a provisionable folder   then make sure theres     - provisioningMarker - provisioningTarget - provisioningOwnerId - provisioningDirectAssign  Update the database and your memory representation |  |
| Check and see if provisioner-wide or in any folder   filtering by policy only | If so, join groups with provisioningTarget with policy join | See which provisioning groups are policy groups |
| Process each group, calculate if provisionable | 1. If regex, see if group name matches 2. If policy only, see if policy group 3. If folder provisionable and in stem scope  Assign the provisioningDoProvision to folders and groups |  |

## Algorithm (incremental)

Method kicked off from provisioner incremental sync

| Step | Description | Details |
| --- | --- | --- |
| Look at actions    - Group create/update - Folder create - Policy group assign/unassign - Provisioning attributes for this target change |  |  |

## Provision based on other critieria

If you want to provision based on other attributes, either assign the provisioning attributes, or have a hook or CLC that assigns the provisioning attributes
