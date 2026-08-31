---
key: GRP-57
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-57
type: Improvement
status: Resolved
resolution: Fixed
priority: Major
reporter: Gary Brown <gary.brown@example.com>
assignee: Blair Christensen <blair@example.com>
created: 2007-11-14T17:58:22.350+0000
updated: 2007-11-14T17:59:22.829+0000
resolved: 2007-11-14T17:59:22.830+0000
components: [UI]
fixVersions: [1.2.1]
labels: []
links: []
---

# GRP-57  Repetitive calls to Member.hasXXX when skipping stems causes slow response times

PrepareRepositoryBrowserAction recursively calls itself if the user browses to a stem where the user only has the option to click on one stem. At Duke and Bristol, users may skip over 5 or more stems. 

The solution is to 'cache' the results of Member.hasXXX calls in the request rather than recompute them.

## Comments

### Gary Brown - 2007-11-14T17:59:22.821+0000

This was actually 'fixed' a few months ago, however, the code changes never made it into CVS - they have now.