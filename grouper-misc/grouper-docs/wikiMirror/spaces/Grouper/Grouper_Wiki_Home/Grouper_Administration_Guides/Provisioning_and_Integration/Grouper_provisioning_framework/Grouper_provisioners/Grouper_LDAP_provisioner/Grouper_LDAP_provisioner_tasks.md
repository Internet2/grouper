---
title: "Grouper LDAP provisioner tasks"
space: Grouper
pageId: 28560334
version: 9
lastUpdated: 2026-07-01T05:35:48.119Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560334/Grouper+LDAP+provisioner+tasks
---

**This page is for the Grouper Development Team**

| Name | Description | Date | Status |
| --- | --- | --- | --- |
| LDAP container | Document how to run LDAP container for unit tests |  | DONE |
| LDAP container data | Populate the LDAP container with users and groups. We can have various OU's for various situations |  | DONE |
| Grouper subject source for LDAP | Unit test subject override map source to connect to LDAP |  | DONE |
| Refactor group dn config | Maybe we need a parent DN, the bushy or flat, and the translation from group to cn |  |  |
| Simple group membership provisioner full sync, synchronous | Provision subject IDs to a group attribute in LDAP. Assume groups already exist |  |  |
| LDAP provisioning diagnostics | Add some configs needed for this, then have a diagnostics page (like subject API diagnostics)   to run through configs and make sure valid (without doing a full sync) |  |  |
| isActiveDirectory | Is this in the LDAP config? Remove from LDAP provisioning config? Drive other defaults from this |  |  |
| Simple user attribute provisioner full sync, synchronous | Provision group names to a user attribute in LDAP. Assume users already exist |  |  |
| Verify sync table cache attributes | Verify the memberFromId2, groupToId3, etc attributes |  |  |
| Link up the full sync to the sync table jobs and logs | Make sure the status of the group is logged to the correct tables |  |  |
| Subject API link for group memberships | Look up subject in subject source and get an attribute to use for group membership list |  |  |
| Subject API link for user attributes | Look up subject in subject source and get an attribute to use for looking up user in ldap |  |  |
| Ensure subject attribute is cached in sync tables | Ensure the subject attribute is cached in sync tables |  |  |
| Ensure subject attribute set by USDU | Ensure USDU run will process all subjects and set attributes in sync tables |  |  |
| Target user link | Look up target subject before provisioning group memberships. Provision based on user attribute |  |  |
| Ensure target user link cached in sync tables | Target user link should be cached in sync tables |  |  |
| Target group link | Look up target group before provisioning user attributes. Provision based on group attribute |  |  |
| Ensure target group link cached in sync tables | Target group link should be cached in sync tables |  |  |
| userSearchAttributes | Ensure the correct attribute are retrieved by ldap, or the default ones (ones we know about) |  |  |
| createMissingUsers | If false, and not exist, error. Create users as needed using the user LDIF.    Document which objects are available and merge helper methods from the    PSPNG util to the grouper util helper var? |  |  |
| userAttributesMultivalued | Why do we need this? When creating users? |  |  |
| createMissingGroups | If false, and not exist, error. Create groups as needed using the group LDIF.    Document which objects are available. |  |  |
| groupAttributesMultivalued | Why do we need this? When creating groups? |  |  |
| deleteInTargetIfInTargetAndNotGrouper | Grouper is system of record |  |  |
| deleteInTargetIfDeletedInGrouper | How do we do this? Incremental or PIT if deleted in last day? Might need to adjust the workflow to    accommodate if full sync and group deletes |  |  |
| Review all configs and make sure implemented | Make sure all other configs are either implemented or on the below list |  |  |
| membershipFields | Get grouper data from other fields (members, admin, managers, readers) |  |  |
| Error handling with cache | Make sure the logic uses cached values unless there is an error, then resolve everything from scratch |  |  |
| Subject sources to provision | Make a duplicate subject source and make sure the provisioner only provisions configured sources |  |  |
| Allow clean subject link in full provisioning | Pass flag from UI in full sync message to resolve all subjects |  |  |
