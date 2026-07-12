---
title: "Grouper MCP example: bulk operations"
space: Grouper
pageId: 28555539
version: 3
lastUpdated: 2026-07-01T05:38:01.382Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555539/Grouper+MCP+example+bulk+operations
---

Ticket from a school in the University to delete 58 folders and the 6 groups in each folder. This is tedious in the UI to the point that it wouldn’t be possible since you can only delete empty folders, so you could need to delete all ~350 objects (groups and folders). Previously this would be a tier 3 ticket to write a script an run it. Even that task takes a while.

Note: once MCP is delegated to power users, the school will be able to do this themselves and save a ticket! But initially this can be handled with MCP with a tier 1 or 2 ticket.

The MCP requires a scope to limit what it can do, and currently the scope doesnt allow you to scope to folders with more than 500 objects, so the parent folder is not possible (since there are a lot we want to keep there are thousands of groups).

We need to do these in batches.

The ticket sent in a list of policy group names, so we need to get the parent folders in batches of ten. MCP can do that.

We can use a readonly scope for that

Then get the list of folders in batches

Get folders

Now we can change the MCP scope for the first batch so MCP can only operate on those folders

Now we can delete those

```
use mcp and take these folder names,
loop through them:
- delete all the groups in that folder,
- then delete the folder.
folder names:
penn:nursing:kite:NUR-B-LineComputers-GRP,
penn:nursing:kite:NUR-BigFix-ReadOnly-GRP,
penn:nursing:kite:NUR-BitLockerServers-GRP,
penn:nursing:kite:NUR-BitLockerWorkstations-GRP,
penn:nursing:kite:NUR-BlineAdmins-GRP,
penn:nursing:kite:NUR-BlockScreenSaverGPO-GRP,
penn:nursing:kite:NUR-CHOPR-MEDPAR-GRP,
penn:nursing:kite:NUR-Conted-GRP,
penn:nursing:kite:NUR-Copy-GRP,
penn:nursing:kite:NUR-DB-SQLServer-GRP
give me the plan before you do any write tools and let me approve
```

Now run the batch

Now we need to change the scope for the next batch

Then approve the next batch, and run that etc

Final summary
