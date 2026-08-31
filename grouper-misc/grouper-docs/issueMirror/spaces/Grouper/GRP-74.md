---
key: GRP-74
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-74
type: Bug
status: Resolved
resolution: Fixed
priority: Major
reporter: Gary Brown <gary.brown@example.com>
assignee: Gary Brown <gary.brown@example.com>
created: 2007-12-11T14:51:21.444+0000
updated: 2007-12-13T09:38:02.803+0000
resolved: 2007-12-13T09:38:02.865+0000
components: [UI]
fixVersions: [HEAD]
labels: []
links: []
---

# GRP-74  JSP errors in Tiles badly handled

It can be difficult to track down the cause of JSP errors - particularly for sites who are customising the UI. Also, a user currently gets a stacktrace.

I intend to take advantage of how tiles are generally implemented to trap errors in JSPs close to their occurrence, flagging an error but allowing the rest of the page to render. 

## Comments

### Gary Brown - 2007-12-13T09:38:02.731+0000

Now catch exceptions in dynamicTile.jsp or TileRecorderTag. A nav property jsp.error defines text (currently jspErr!) which appears in place of the failed tile. The HTML source will have details of the Exception. The Tile history of debug mode also indicates where exceptions occurred and the actual exception.