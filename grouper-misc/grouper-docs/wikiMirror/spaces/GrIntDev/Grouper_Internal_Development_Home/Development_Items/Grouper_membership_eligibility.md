---
title: "Grouper membership eligibility"
space: GrIntDev
pageId: 48792634
version: 2
lastUpdated: 2026-07-12T06:45:35.121Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792634/Grouper+membership+eligibility
---

This is a design page for a future feature of Grouper regarding membership requirements and grace periods for manual groups.

## Current options

- [Grouper rules](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548037/Grouper+rules+patterns) to remove members or set end dates
  
  - Before v5 these are tedious to implement, v5 has a UI which makes it a little easier though would be tedious to implement on many groups and the user needs to know what they are doing
- Composites can keep invalid entities out of the policy groups
  
  - The underlying manual groups still need cleanup
- [Grouper membership eligibility requirements](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549482/Grouper+membership+eligibility+requirements)
  
  - Step in the right direction. Users can identify a coarse-grained group that members are required to be in or they are vetoed or removed
  - No end dates available
- Something custom
  
  - Identify groups with attributes and use a GSH change log consumer and daemon to implement the logic
  - Would not be re-usable to other institutions and would be tedious to set up

## Identify lifecycle events

It would be useful to identify some reference groups which are useful for membership eligibilty.

Folders could be used for change in org or job or title.

ABAC attributes could be leveraged to see when attributes change.

Similar to "Grouper membership eligibility requirements" which lifecycle event could be protected by a security group so only certain people can see or use them (or it could use the underlying Grouper privileges).

## Screens for membership eligibility

Similar to the new rules screen, users can enter:

- What the requirements are based on standard lifecycle events
  
  - Include custom eligibility groups?
- The optional grace period (could be some date in an attribute on the membership so as not to affect assigned end date?)
  
  - Is the grace period for all requirements or does each requirement have its own grace period?
- Email notifications to users or managers (daily batch for all groups with eligibility requirements?)

Screen to see current state of members and history of actions performed. Show current state of eligibility. Show dates of built in lifecycle events?

Ability to undo grace period actions for selected memberships.

## Built-in attributes for groups / folders

These eligibility requirements can be assigned to groups

Requirements could be assigned at the folder level and apply to groups inside (manual groups only)

## Built-in attributes for memberships

When an eligibility action is performed on a membership it could assign metadata to the membership to keep track of the state and the grace period. These attributes inform the UI and allow the system to know which memberships are available for undo
