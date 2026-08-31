---
title: "Provisioning Consumer Use Cases"
space: GrIntDev
pageId: 48794090
version: 5
lastUpdated: 2026-07-12T06:46:29.930Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48794090/Provisioning+Consumer+Use+Cases
---

## Provisioning Consumer Operations

This page documents the various operations and when a messaging consumer may have to call back into Grouper or other systems for more information

### Create Group

In general, what's needed here is:

- Group Name
- Group ID Path
- Description
- POSIX GID (not a very common case, but common enough to matter) – not provided by the ChangeLog

Possible transformations could be made by the consumer to make the name meet naming standards in the target endpoint.

### Update Group

When a group is updated, we receive a little bit more information via the changelog, so it's not anticipated that a callback would be needed

### Delete Group

Deletion of a group is pretty straight-forward. The provisioner will have to keep in mind that if it receives a Delete Stem

### Add Stem

Depending on the downstream system this may or may not be a NOOP.

### Update Stem

Depending on the downstream system this may or may not be a NOOP.

### Delete Stem

When this happens, we need to also delete the groups under the stem. We will have to keep track of the fact that those groups were deleted to handle any out-of-order later **Delete Group** calls.

### Attribute Def Add/Update/Delete

In this set of operations, attributes may be used to control how a group appears in the endpoint system. One example could be adding a mail attribute to a group in the target system to give it an email address. The changelog entry should contain enough information for handling the management of this.

### Membership Add/Update/Delete

When it comes to memberships, the one thing we need to keep in mind is that the Grouper SubjectID may not be the end-user's ID in the target system. This will necessitate a potential lookup in either grouper, the source of the grouper subjects, or some other translation table.

### Privilege Add/Update/Delete

Like Membership updates, callbacks may be necessary here to perform the SubjectID end-system ID translation
