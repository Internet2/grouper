---
title: "Grouper provisioning matching"
space: Grouper
pageId: 28554238
version: 2
lastUpdated: 2026-07-01T05:40:52.444Z
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
