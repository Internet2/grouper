---
key: GRP-87
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-87
type: Bug
status: Resolved
resolution: Fixed
priority: Major
reporter: Chris Hyzer <mchyzer@example.com>
assignee: Chris Hyzer <mchyzer@example.com>
created: 2008-02-07T05:54:07.075+0000
updated: 2008-02-07T05:56:27.648+0000
resolved: 2008-02-07T05:56:27.649+0000
components: [UI]
fixVersions: [1.3.0]
labels: []
links: []
---

# GRP-87  error message is not shown correctly for creating groups/stems

Create a group with extension "test".  This is not valid since there is no stem.

You see error message:

All Groups  - Create group
Could not create group. Error is {0}.


## Comments

### mchyzer - 2008-02-07T05:56:27.605+0000

In the nav.properties, when there are params, the single quotes need to be escaped.  So if you change:

FROM (these are the only two affected messages):
stems.message.error.add-problemCould not create stem. Error is '{0}'.
groups.message.error.add-problem=Could not create group. Error is '{0}'.

TO:
#note: the single quotes need to be escaped in a message that has params, 
#that is why there are two single quotes in a row
stems.message.error.add-problemCould not create stem. Error is ''{0}''.
groups.message.error.add-problem=Could not create group. Error is ''{0}''.

Then you see the right error message:

All Groups  - Create group
Could not create group. Error is 'cannot create groups at root stem level'.

I committed this to HEAD (1.3).  If we re-release 1.2, we should put this in there.

Chris