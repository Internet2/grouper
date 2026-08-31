---
key: GRP-83
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-83
type: Improvement
status: Closed
resolution: Fixed
priority: Minor
reporter: blair christensen. <blair@example.com>
assignee: Tom Zeller <tzeller@example.com>
created: 2008-01-17T04:53:50.703+0000
updated: 2008-04-04T14:24:28.497+0000
resolved: 2008-04-04T14:24:28.501+0000
components: [API]
fixVersions: []
labels: []
links: []
---

# GRP-83  Add "ext.call" Ant target

This Ant target adds the ability to call an arbitrary Ant target in a specific extension.

For instance, to just run the gsh tests:

  % ant ext.call -Dext=gsh-src -Dtarget=test

## Comments

### blair christensen. - 2008-01-17T04:54:21.476+0000

Patch referenced in issue creation.

### tzeller@example.com - 2008-04-04T14:24:28.443+0000

Patch applied.

## Attachments
- add_ext.call_target.patch (2116 bytes) - by blair christensen. on 2008-01-17T04:54:21.420+0000