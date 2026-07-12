---
title: "Grouper provisioning SCIM loading data from target to Grouper"
space: Grouper
pageId: 28564309
version: 5
lastUpdated: 2026-07-01T05:35:20.390Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28564309/Grouper+provisioning+SCIM+loading+data+from+target+to+Grouper
---

In Grouper v4.14+ and v5.11+ you can load user data from the target to Grouper.

In the full sync it will sync all users and attributes. All SCIM built-in attributes and custom attributes those will be pulled down.

This is loaded to two tables in the Grouper database and can be used to make a loader to load groups or for reporting or for and entity resolver to provision values back (e.g. manager uuid)

## Configure loading

## Data for users

## Data for user attributes

id links back to a user
