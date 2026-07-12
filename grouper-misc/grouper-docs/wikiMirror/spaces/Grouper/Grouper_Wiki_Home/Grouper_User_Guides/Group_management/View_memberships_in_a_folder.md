---
title: "View memberships in a folder"
space: Grouper
pageId: 28544284
version: 2
lastUpdated: 2026-07-01T05:48:40.003Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544284/View+memberships+in+a+folder
---

## View memberships in a folder

Memberships in a folder can be viewed by clicking the "More" tab → Group memberships for groups under this folder

## Export memberships in a folder

**Folder membership CSV export**

In v7.1.0+, an Export button appears on the group memberships in folder screen. Navigate to a folder, then More actions, then Group memberships groups in this folder. You must have VIEW privilege on the folder, and the export will only include memberships from groups you have READ access to.

To export, optionally set filters such as membership type, member filter text, enabled/disabled status, point-in-time date range, or custom composite. Then click Export. The CSV file will download with a filename based on the folder name, for example `groupMembershipsInFolder_myFolder.csv`.

The columns included in the export depend on the filters selected:

- Standard: Entity name, Folder name, Group name, Membership
- With enabled/disabled filter: Entity name, Folder name, Group name, Enabled/Disabled, Start date, End date, Membership
- With point-in-time filter: Entity name, Folder name, Group name, Start time, End time

If the number of matching memberships exceeds the configured maximum, an error message is displayed showing the actual count and the limit. Narrow your search using filters to reduce the result set.

**Configuration for deployers**

The export performs a count query before fetching results. If the count exceeds the configured maximum, the export is blocked and an error message is shown to the user with the count and the limit. This prevents out-of-memory conditions on the UI container when exporting from large folders.

The maximum can be set in the Grouper UI configuration screen or in `grouper.properties`:

`grouper.membership.export.maximumFolderExportEntries`

Default: 800000. Set to -1 for no limit.

This setting controls the maximum number of membership rows that can be exported in a single CSV download from the folder group memberships screen. The default of 800,000 is generous enough for most use cases while still protecting the UI container from loading millions of rows into memory. Adjust based on the available heap size of your UI containers.
