---
title: "Assign someone to be able to manage a group"
space: Grouper
pageId: 28545434
version: 12
lastUpdated: 2024-05-21T16:18:20.237Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545434/Assign+someone+to+be+able+to+manage+a+group
---

## Summary

You can assign privileges to an entity or entities (individual users, or a group) to allow them to manage a group. This involves granting either the **UPDATE** or **ADMIN** permission.

## Privilege requirements

You need **ADMIN** on the group to able to assign privileges on the group. If you are a Grouper sysadmin you inherently have this privilege on every group.

## Procedure

1. Navigate to the group's membership page. Click on the **Privileges** tab.
2. Click the **Add members** button.
3. Enter your search criteria in the **Member name or ID field**, and select the appropriate subject form the list. You can search for an individual person, or a group.
4. In the **Assign these privileges**section of the page, select the checkbox for "**UPDATE**" (can modify group membership) or "**ADMIN**" (can modify group membership, change group name, or delete the group).  
    
  
  
  - Choose the simplest permission that will suffice, keeping in mind that anyone with ADMIN privilege can rename or delete the group, whether intentionally or accidentally.
  - If the user can manage the group, but is not a member of the group, unselect the checkbox for "**MEMBER**".
5. Click the **Add** button. Note that the selected person or group is now listed in the list of entities with privileges, with the appropriate privilege(s) assigned.
