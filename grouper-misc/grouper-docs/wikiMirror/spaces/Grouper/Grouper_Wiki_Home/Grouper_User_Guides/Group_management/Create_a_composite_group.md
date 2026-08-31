---
title: "Create a composite group"
space: Grouper
pageId: 28545340
version: 17
lastUpdated: 2026-07-28T15:21:15.238Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545340/Create+a+composite+group
---

## Summary

Grouper allows you to create composites in a couple of ways.

- UNION are the adding of 2 or more groups as members of a third, the same as [adding individual members](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544779/Add+a+member+entity+or+group+to+a+group).

In addition, Grouper allows you to use two existing groups (called "factors") to define a third (composite) group. You may combine two groups in the following ways:

- INTERSECTION includes entities that belong to both of two original (factor) groups – "members-in-common"
- COMPLEMENT includes entities that belong to the primary ("left) factor group who are not also members of the secondary ("right") factor group – "left minus right"

> Prior to 4.11.0, Grouper would make composite membership adjustments whenever a change was made to the composite or its factors. For groups that were factors in many composites(e.g. reference groups), a membership change could result in many composite adjustments and have a considerable performance impact.
> 
> Starting with 4.11.0, the job CHANGE_LOG_consumer_findBadMemberships was renamed to CHANGE_LOG_consumer_compositeMemberships and handles these adjustments with improvements in batch lookups and multiple threads. This long-running job checks, makes changes, and sleeps for 500ms before checking again.
> 
> This change may create a different user experience for end users familiar with prior versions and proper communication about the change is recommended.
> 
> For adding and removing direct members, there is an exception (based on regex of group names) to allow for the old behavior (having the composite calculations done in the same transaction) using composites.synchronousCalculationGroupNameRegex  in grouper.properties.

## Requirements

UPDATE GROUP privilege or greater is required on the composite group and VIEW,READ GROUP on the factor groups.

## Process

To create a composite group:

For this example, we are going to create a group itsAffiliates whose members are both in UNCG-affiliate as well as the ITS department's system-of-record.

1. [Create the group](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545455/Create+a+group) that will contain the composite (e.g. uncg:apps:spartApp:ref:itsAffiliates)
2. Either create the factor groups like the composite was created, or note the location of the existing groups. In this example, we will be using uncg:reference:UNCG-affiliate and uncg:org:DEPT-ITS-23101:ref:DEPT-ITS-23101_systemOfRecord.
3. In the composite group, go to "Group actions", then**"**Edit composite"**** ****
4. Set **Composite** to **Yes**, then find your first and second factors by searching for them in the given fields and selecting the group when it's returned.
5. Select the desired operation from the drop-down.
6. Click "Save". This will assign the composite to the group.

## Convert an existing group with members to a composite

> The Edit composite screen requires the group to have no direct members. In these versions an existing group that already has members can instead be converted to a composite in place, without churning the change log or point-in-time (PIT) history for members whose membership does not change.

When a group is converted to a composite in place:

- Members who are also in the composite result keep their membership: the row is relabeled from a direct (immediate) membership to a composite membership, with no change log entry and no PIT record, so provisioning and history are not disturbed.
- Members who are not in the composite result are removed, producing one membership delete each.
- Members newly included by the composite are added, producing one membership add each.

The conversion runs in a single transaction. For the largest reference groups, run it during a maintenance window, since it holds that transaction until every relabeled row is committed.
