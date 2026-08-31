---
title: "Configuration and UUID's, name, and idIndexes"
space: GrIntDev
pageId: 48792687
version: 3
lastUpdated: 2026-07-12T07:01:12.809Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792687/Configuration+and+UUID+s+name+and+idIndexes
---

Objects in Grouper have multiple identifiers including:

| Object identifier | Description |
| --- | --- |
| UUID | Globally unique identifier |
| idIndex | Integer which is unique in the object type |
| name | Fully qualified system name including all an |
| display name | Fully qualified name on UI |

Objects are needed in various places in Grouper to configure things

| Configuration location | Description |
| --- | --- |
| Config files | The value might be an object or list of objects (e.g. groups or folders) |
| Rules | Attribute values could have an object or list of objects |
| Hooks | Java code could refer to objects (hopefully through configs) |

Here are the issues with using various types of object identifers

| Object identifier | Issues |
| --- | --- |
| UUID | If you export/import the UUID is probably different in different envs  If you delete and recreate the object with same name, it will have a different UUID |
| idIndex | If you export/import the idIndex is probably different in different envs.  If you try to import the same idIndex it could cause conflicts in future  If you delete and recreate the object with same name, it will have a different idIndex |
| name | If you rename the object then the old pointer will be broken |
| display name | Changes too frequently and shouldnt be used for configuration |

## Future direction of configuring objects

After some discussion, the Grouper teams decided to configure objects using the object name. If there is a rename in Grouper then:

1. Look for attributes values that have that old name (directly or comma-separated), and edit it with new name
2. Look for database configs that have that old name (directly or comma-separated), and edit it with new name

This way we can support renames and export/imports.... we need to implement this in a future version of Grouper
