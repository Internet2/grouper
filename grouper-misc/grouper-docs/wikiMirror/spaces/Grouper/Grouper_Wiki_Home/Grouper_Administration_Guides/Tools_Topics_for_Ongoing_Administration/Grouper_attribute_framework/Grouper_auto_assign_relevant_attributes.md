---
title: "Grouper auto assign relevant attributes"
space: Grouper
pageId: 28547690
version: 7
lastUpdated: 2026-07-12T15:26:47.911Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547690/Grouper+auto+assign+relevant+attributes
---

If a Grouper function is controlled by attributes but there is not a suitable UI to assign them, and it follows the ["marker" and "name-value pair" pattern](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548217/Pattern+Attribute+Marker+and+attached+Name-Value+pair+s), then the names of the name-value pairs can be auto-assigned.

This is controlled by attributes, assign these to an attribute definition (doesnt matter which attribute definition).

This specifies that when a recent memberships marker is assigned, then assign the three attributes on that assignment.

Here is an example:

Assign the recent memberships marker

And instantly all three metadata attributes automatically appear with no values

Of course when configuring the auto assign attributes, the metadata on those are auto assigned too

This is implemented with a hook, if there is a problem with it, disable in grouper.properties

```
# if the auto assign attribute assign hook should be auto registered
# {valueType: "boolean", required: true}
grouperHook.attributeAssign.autoAssign.autoRegister = false

```
