---
title: "Read the members of a group"
space: Grouper
pageId: 28545369
version: 8
lastUpdated: 2026-07-12T15:26:43.000Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545369/Read+the+members+of+a+group
---

## Summary

You can read the members in a group, and use various filters.

## Privilege requirements

You need READ or ADMIN on the group to be able to read it members. If you are a Grouper sysadmin or readonly sysadmin you can also READ the members. If you do not see the "Members" tab, or cannot find the group at all, then you cannot READ it memberships.

## Find the group

You can find a Group in Grouper in various ways: searching, navigating, favorites, home screen panels, bookmark, etc

## See the members

When you click on a group in the UI the members tab shows the members by default.

Under Membership you will see

| Membership type | Description |
| --- | --- |
| Direct | The member is immediately in the group and can be removed |
| Indirect | The member is immediately in another group which is immediately in this group. The member cannot be removed, but the group could be removed |
| Direct, indirect | The member is immediately in the group and can be removed, though they will still be an indirect member of the group. |

## Filter by membership type

You can filter by membership type. This can be useful to see which members can be removed, or to see a smaller list. Note, you can also visualize the group to see the policy

## Find a member of a group

Click Advanced, and filter by a user name or group name to find a member of the group

## Use a group filter

If your institution has group filters configured, you can use that to help deprovision users who do not fit certain requirements. Click "Advanced" to see the "Group filter" input.

## Show enabled / disabled dates

Click "Advanced" and choose which enabled / disabled status to filter on

## Point in time

Click "Advanced" and filter by point in time. You will see multiple entries if entities were added and removed and then added to the group again
