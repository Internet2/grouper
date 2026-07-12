---
title: "Unique object names in Grouper"
space: Grouper
pageId: 28549091
version: 8
lastUpdated: 2026-06-11T18:09:43.483Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549091/Unique+object+names+in+Grouper
---

> Available since the Grouper `v2.2.1` patch, and present through current releases (confirmed in `v4` and `v7`). It could likely be installed in earlier versions as well.

## Overview

Grouper can have objects of different types that share the same fully qualified name (the ID path shown in the UI). If you are provisioning to a system that cannot allow this, you can enable a set of hooks that disallow it at object creation and update time. For instance, in Active Directory you might not be able to have an OU (folder) with the same fully qualified name as a group.

These hooks cover four object types: group, folder, attribute definition, and attribute definition name. If an object is created or renamed to a name already used by a different object type, the operation fails with a `HookVeto` exception.

## Configuration

Add the following to `grouper.properties`:

```
hooks.group.class = edu.internet2.middleware.grouper.hooks.examples.UniqueObjectGroupHook
hooks.stem.class = edu.internet2.middleware.grouper.hooks.examples.UniqueObjectStemHook
hooks.attributeDef.class = edu.internet2.middleware.grouper.hooks.examples.UniqueObjectAttributeDefHook
hooks.attributeDefName.class = edu.internet2.middleware.grouper.hooks.examples.UniqueObjectAttributeDefNameHook
```

Each `hooks.*.class` property accepts a comma-separated list of classes, so if your deployment already registers a hook of one of these types, append the matching `UniqueObject…` class rather than replacing the existing one.

Because these are configured in `grouper.properties`, enabling them requires server-side access to the Grouper configuration; once enabled, the name-uniqueness check applies to all object creates and updates.

## Reference

Grouper issue: GRP-1134
