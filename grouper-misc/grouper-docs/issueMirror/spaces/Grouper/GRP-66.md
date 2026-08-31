---
key: GRP-66
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-66
type: Bug
status: Open
resolution: Unresolved
priority: Minor
reporter: Joy Veronneau <jv11@example.com>
assignee: Tom Barton  (internet2.edu) <tbarton@example.com>
created: 2007-11-20T21:04:38.997+0000
updated: 2008-07-23T03:36:15.885+0000
resolved: 
components: []
fixVersions: []
labels: []
links: []
---

# GRP-66  Need to be able to flag groups as not allowed in composites and other restrictions for very large groups

We have some very large groups, one is 40,000 members, one is 190,000 members, and we have several more.  We would like these to be restricted from being used in composite groups, since replication to the directory will be a problem because the directory keeps them in a flat structure.  What might be nice is to be able to have a maximum value that we can set so that groups over a certain size cannot be included in composites. There may need to be other places we use that restriction - for example, we don't want to allow someone to add a file of 190,000 subjects to a group using the add members from file feature in the UI.  Ditto web services.

## Comments

### tbarton - 2007-11-20T21:20:01.500+0000

Do you also want to restrict flagged groups from being made members of another group? That operation also causes many indirect memberships to be computed, just as with composites.

### jv11@example.com - 2007-11-20T21:26:22.095+0000

Yes, good idea.

### mchyzer - 2008-07-23T03:36:15.827+0000

Let me know if you want me to write a built-in hook for this with config in the grouper.properties.  Just assign the bug to me, should not be too complicated...