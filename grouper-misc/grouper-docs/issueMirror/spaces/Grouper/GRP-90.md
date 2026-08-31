---
key: GRP-90
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-90
type: Bug
status: Resolved
resolution: Fixed
priority: Minor
reporter: Gary Brown <gary.brown@example.com>
assignee: Gary Brown <gary.brown@example.com>
created: 2008-02-20T17:15:45.454+0000
updated: 2008-04-02T07:48:28.093+0000
resolved: 2008-04-02T07:48:28.094+0000
components: [UI]
fixVersions: [1.3.0]
labels: []
links: []
---

# GRP-90  UI does not handle situation when a user tries to create a group in a stem where the extension already exists

An exception is caught but then the UI  finds the original stem with the same extension and applies inputs to it. It should just throw an exception and the user should find the original group and apply changes directly to it.

## Comments

### Gary Brown - 2008-04-02T07:48:28.073+0000

Also fixed equivalent problem with stems. Now don't attempt to do a lookup on the current stem + new extension when group / stem add has failed - simply present error to user.

NB When testing the fix I noticed that it is perfectly valid to have a group and a stem with exactly the same name. In some ways this does not matter. I do have code which, given a name, will attempt to resolve it to a group or stem. The particular method is not currently called but there is potential for ambiguity if simply presented with a name. Should we prevent a group and a stem  having an identical name?