---
key: GRP-58
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-58
type: Bug
status: Resolved
resolution: Fixed
priority: Major
reporter: Gary Brown <gary.brown@example.com>
assignee: Gary Brown <gary.brown@example.com>
created: 2007-11-17T14:26:51.986+0000
updated: 2007-11-17T14:30:48.422+0000
resolved: 2007-11-17T14:30:48.423+0000
components: [UI]
fixVersions: [1.2.1]
labels: []
links: []
---

# GRP-58  Incorrect default search logic would cause two API searches when only one was configured

The media.properties:

search.default.search-in-display-name-or-extension, and
search.default.search-in-name-or-extension

control the default group search. Two attributes can be searched, but the default is for displayName. The code incorrectly tests for an 'empty' value and so also searches the 'extension' attribute.

In addition, if the search is carried out against two attributes the merging of results is very inefficient.

## Comments

### Gary Brown - 2007-11-17T14:30:48.421+0000

Now test empty String AND null. Use HashSets for merging two result sets.

This code could be rewritten to use a UnionQueryFilter