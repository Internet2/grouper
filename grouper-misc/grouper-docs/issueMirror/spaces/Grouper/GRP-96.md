---
key: GRP-96
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-96
type: Bug
status: Resolved
resolution: Fixed
priority: Major
reporter: Chris Hyzer <mchyzer@example.com>
assignee: Chris Hyzer <mchyzer@example.com>
created: 2008-03-19T19:23:03.349+0000
updated: 2008-03-19T19:25:30.205+0000
resolved: 2008-03-19T19:25:30.206+0000
components: [API]
fixVersions: [1.3.0]
labels: []
links: []
---

# GRP-96  grouper db-init doesnt work with mysql

When db-init is used with mysql, it fails since attribute.value is more than 766 chars, and there is a unique constraint on group_id, attribute name, and value.  This unique constraint doesnt reflect how grouper works (puts all attributes in a map for a group, so really the index should be on group_id and name).  I will remove the value col from the unique constraint

## Comments

### mchyzer - 2008-03-19T19:25:30.203+0000

done