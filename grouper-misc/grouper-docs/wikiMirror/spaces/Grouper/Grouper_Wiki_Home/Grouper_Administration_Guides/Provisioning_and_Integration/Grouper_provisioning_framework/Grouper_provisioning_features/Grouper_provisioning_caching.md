---
title: "Grouper provisioning caching"
space: Grouper
pageId: 28555196
version: 7
lastUpdated: 2026-07-12T05:04:59.336Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555196/Grouper+provisioning+caching
---

Grouper will cache certain information about the target state and provisioning progress.

The cache is in the Grouper registry database in the grouper_sync* tables.

Most of the information is cached automatically, but some of it needs configuration.

There are four cache buckets per group or entity.

In the configuration, select which attributes are cached to which bucket and which representation (target or grouper). Grouper is not as common to cache since the target state is more important or the data doesn't live in Grouper (e.g. UUID is target which is "linked"). You can also cache Subject data.

You should cache:

- Target link information (e.g. DN or UUID) make incremental more efficient so the target does not need to be read to determine the data
- Search / match attribute (so you can find or match if object deleted)

## Caching discussion in regards to deletes

The following attributes are in the Grouper sync objects which are cached by default

- Group: name, extension, uuid, id index
- Entity: subject id, three subject identifiers in the subject source, name, subject id

The following is not cached by default

- Group: display name, display extension, attributes from resolver, id's from target
- Entity: attributes from resolver, id's from target

If you are using data which is not cached by default (e.g. the one search/attribute attribute for a group is translated from display name or display extension), and you are not caching that in a cache bucket, then when you delete the group from Grouper, then the provisioning framework will not know how to retrieve or match that group, and it will not get deleted, and the memberships will not get deleted.

Possible solutions:

1. Cache the search/matching attribute based on display name/extension
2. Search on the cached target ID attribute (assuming that is in a cache bucket), in addition to the attribute based on display name/extension
3. Both numbers 1 and 2

To see if this is the problem when data is not getting deleted

1. If you are retrieving all data in a full sync, you will see in the object logs, that the object is getting retrieved from the target, but it is not matched, and the compare does not delete
2. If you are not retrieving all data in a full sync or you are examining an incremental sync, you will see in the object logs, that the object is not getting retrieved from the target and then obviously the compare does not delete

Note: to help facilitate renames, you need to cache and search/match on an opaque identifier, e.g. in SCIM the "id"
