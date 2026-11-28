---
title: "Grouper provisioning matching"
space: Grouper
pageId: 28554238
version: 4
lastUpdated: 2026-11-27T18:41:02.664Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554238/Grouper+provisioning+matching
---

Matching in provisioning is determining which grouper translated object matching with which objects retrieved from the target.

Searching is using attributes and values from grouper translated objects and using the target API to find target objects by the search attributes and values.

Generally the matching attributes and search attributes will be the same.

## Configuring matching

1. You can configure multiple attributes to match on (or search on)
2. Matching attributes are configured in priority order
3. If objects cannot be matched, then cached (previous IDs) will be used

## Assigning matching IDs

1. Methods like GrouperProvisioningTranslator.idTargetGroups()
2. Go through the grouperTargetGroups (grouper groups translated into target format), and targetProvisioningGroups (groups retrieved from the target)
3. grouperTargetGroups will have the prioritized list of attribute names and values
  
  1. First look at the prioritized list of attributes and the current values
  2. Then add the prioritized list of attributes and past (cached) values

## Matching

1. Loop through the target groups and index the attribute/value tuples with the targetProvisioningGroups
2. Loop through the grouper target groups
  
  1. Start by looking at non-deleted groups (deleted groups could be phantoms from cached data)
  2. Look at current values first
  3. Loop through the priority of attribute names
  4. Skip wrappers that already have matches
  5. See if there is an attribute/value that matches the target
  6. If there are multiple grouper objects that match target objects, that is a validation problem (for that attribute)
  7. If there are multiple target objects that match grouper objects, that is a validation problem (for that attribute)

 

## Re-linking an entity to another target account

*v7.9.1+*

Most provisioners match entities on a name which can change (for example SCIM `userName`, Duo `loginId`, Google email) and then on the target id. If an entity is already linked to a target account and its matching value changes to a value which another target account already holds, matching does not move the entity to that account. The entity gets an object error and keeps its link. This is because anyone else who can create or rename accounts in the target could claim the value ahead of time, and the moved entity would give that account its memberships.

The link is the target value cached at the last sync, from an entity attribute cache with source target (for example the target id). An entity with no such cache, which has never been linked, is matched as before.

Settings in grouper-loader.properties:

- `provisioner.<configId>.trustTargetToRelinkEntities`, v7.10.0+, default `false`: when `true`, matching moves the entity to the other account. Turn it on only if Grouper is the only system which creates or renames accounts in the target. The Configuration review screen warns while it is on.
- `provisioner.<configId>.scim2UpdateConflictRelink`, SCIM provisioners, default `false`: when a SCIM update of an entity returns a conflict (HTTP 409) because another target account already has the identity, allow re-linking the entity to the existing account with the same `externalId`, and dispose of the old account by the delete settings. `externalId` can be written by anyone who can edit the target account, so this is off by default and the entity gets an error instead.

To fix an entity in error, rename or remove the other target account.
