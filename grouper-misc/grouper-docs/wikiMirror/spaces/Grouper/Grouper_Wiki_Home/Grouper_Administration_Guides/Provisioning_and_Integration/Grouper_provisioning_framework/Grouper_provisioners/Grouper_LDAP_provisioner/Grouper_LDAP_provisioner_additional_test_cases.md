---
title: "Grouper LDAP provisioner - additional test cases"
space: Grouper
pageId: 28560258
version: 5
lastUpdated: 2022-07-27T18:02:28.757Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560258/Grouper+LDAP+provisioner+-+additional+test+cases
---

> The info on this page applies to Grouper 2.6 and above.

Add these unit tests

## gidnumber missing in target and DN matches

Have multiple search/matching attributes. First look for gidnumber, then look for DN.

Have a group in grouper with dn override and a gid number

Have no group in target with gidnumber, but a dn that matches the override

Grouper should find that group, assign the gidnumber in the target, and manage the group

## DN of user changes full sync

If a full sync occurs and the DN of a user changes, and the DN is the membership attribute value of a group, it should be reflected in the provisioner cached data, and in the group membership lists (for all groups of that user)

## DN of user changes incremental sync

If an incremental sync occurs and the DN of a user changes, if it is not recalc, an error occurs

The membership or user should be recalced, where the new DN will go into cache and in the target

## LDAP provisioning folder metadata for base DN

Allow a folder in grouper to have metadata about the base DN to be provisioned. All other translation rules apply

## Allow delegation for assigning provisioning information

A group of users should be able to view provisioning information for a provisioner

A group of users should be able to view/update provisioning information for a provisioner

## Check name requirements when assigning provisionable

When a group is assigned provisionable, translate the name and check LDAP

Give an error for conflict or warning for something else

Give an error if the validations are not valid (length of DN or CN)

## Check name requirements when renaming groups

When a group is renamed, check to see if provisioning targets are invalid, if so veto

## If a group suddenly fails validation, and has members in target, still manage those members

If a group in target matches a group in grouper, then still manage memberships if another field on group becomes invalid
