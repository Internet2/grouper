---
title: "User Audit Log"
space: Grouper
pageId: 28548937
version: 22
lastUpdated: 2026-07-01T05:43:09.136Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548937/User+Audit+Log
---

## User Auditing

> For Grouper 2.5 and above see [Get Audit Entries](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548223/Get+Audit+Entries)

Groups are often used to control access to resources or to target communications. Group attributes, memberships and privileges may change at any time with potentially important consequences, so simply knowing how a group last changed is insufficient to investigate why, for example, an individual lost access to a resource. An audit log of high level user actions allows administrators to understand the history of groups, group types and stems. Audit entries may be queried by object or the subject responsible for a change.

High level actions are audited. For example if a group is deleted, all of the related memberships and privileges for that group are deleted as well. But there will only be one audit entry for the group delete.

Group admin privilege is required to view the audit log.

> Note that User Auditing, described here, is different from point in time auditing which provides the ability to query the state of Grouper in the past. Point in time auditing allows you to determine all the direct and indirect members that a group had at any point in time, or to determine all the permissions a person had, attributes that were assigned, attribute values over time and it tracks system configuration changes made in the database.

For user auditing, the following fields are stored for each user audit entry:

- Audit type
- Audit action
- Act as member id (if the caller is acting as someone else)
- Context id (associates transactions in the registry)
- Created on timestamp
- Description (paragraph) of change
- Env name (configured in grouper.properties)
- Grouper engine (GSH, UI, WS, etc)
- Grouper version
- Logged in member id
- Server host
- User IP address
- Query count (counts queries in one action for performance profiling)
- Server user name

For each action various additional data is stored, e.g. if a group was created, then the group id, group name, etc are stored

You can import/export auditing data, but this is a different file than the normal Grouper export file, with the same command. You will see two different XML files.
