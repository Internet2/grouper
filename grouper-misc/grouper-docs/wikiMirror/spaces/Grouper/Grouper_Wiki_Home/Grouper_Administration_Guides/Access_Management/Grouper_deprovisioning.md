---
title: "Grouper deprovisioning"
space: Grouper
pageId: 28544732
version: 79
lastUpdated: 2026-07-12T15:26:28.025Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544732/Grouper+deprovisioning
---

There are many deprovisioning features in Grouper, this feature is a manual process to deprovision an individual.

This allows a deprovisioning administrator manually see someone's access and instantly remove it.

### Deprovisioning setup

- You can identify multiple affiliations (relationships) to the institution that have their own deprovisioning settings and a group of deprovisioned users. Generally institutions start with and might only need one for their workforce.
- Groups and folders can be pre-configured to be applicable or excluded for this deprovisioning process for each affiliation

### Deprovisioning process for a user

The administrator manually initiates this process at the time of deprovisioning a user from an affiliation:

- Search for and select a user
- The administrator will be presented with a list of direct memberships and privileges the user has in the configured groups/folders for the affiliation being deprovisioned
  
  
  
  - Checkboxes to remove the membership/privilege have defaults based on group/folder configuration of the group/folder
  - After reviewing the page, and the administrator clicks the deprovision button, the user's selected direct memberships and privileges will be removed
- Added to the deprovisioned group for a configured amount of time.
  
  
  
  - Either this is a short amount of time to let data flow through the institutions systems, or it is a long period of time if there is a worry that systems are deprovisioning users
- While they are deprovisioned, any additions of that user to a configured group (manual or loaded) will be veto'ed
- Loader jobs can be configured to automatically exclude deprovisioned users (since the system of record might not be accurate)

### Group managers

- Can use the Grouper UI to see if there are users in their group who are deprovisioned

### While a user is deprovisioned

- A user is deprovisioned while they are in the deprovisioned group
- If a group manager adds a deprovisioned user to a group where that is not allowed, the action will be veto'ed
- Grouper will notify application administrators where Grouper is not the system of record or where manual deprovisioning is preferred. This is a nightly notification
  
  
  
  - The group manager can certify that the group should have users by certifying the group on a certain date. If there are new deprovisioned user after that date they will get notifications for them.

> **Blog on Deprovisioning**
> 
> Check out the [October 2024 Grouper blog on deprovisioning](https://incommon.org/news/grouper-deprovisioning/?utm_source=2024-10-incommon-news&utm_medium=email&utm_campaign=incommon-news) for a helpful overview of the topic.

Here are workflows around configuring and using deprovisioning.

[Getting started](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549251/Grouper+deprovisioning+getting+started)

[Grouper deprovisioning settings on objects](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549004/Grouper+deprovisioning+settings+on+objects)

[Grouper deprovision process](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549009/Grouper+deprovision+process)

[Grouper deprovisioning report](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548487/Grouper+deprovisioning+report)

[Development notes](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792055/Grouper+deprovisioning+development+notes)

[Grace periods, recent memberships](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545165/Grouper+recent+memberships+grace+periods)

**See Also**

[Slack Use Case from University of Pennsylvania](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544582/Deprovision+reports+from+CSV)

[Grouper Automatically Managed Recent Memberships / Grace Periods](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545165/Grouper+recent+memberships+grace+periods)
