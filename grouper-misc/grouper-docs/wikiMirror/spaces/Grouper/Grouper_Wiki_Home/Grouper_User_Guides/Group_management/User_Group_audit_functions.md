---
title: "User/Group audit functions"
space: Grouper
pageId: 28545440
version: 8
lastUpdated: 2026-04-05T21:22:25.783Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545440/User+Group+audit+functions
---

Auditing gives you the ability to view the log history of a specific user, their memberships, and privileges in the UI. This can be viewed by browsing to the desired user, selecting "more actions" and then choosing the audit type you wish to review.

### Subject screen menu

### Membership audit log

### Export audit logs

In v7.1.0+ you can export certain audit screens, which takes into account the filter used on the screen.

**User guide**

An **Export** button appears next to the filter button on each audit log screen:

- **Group audit log** – Navigate to a group → More actions → View audits. Requires ADMIN privilege on the group.
- **Folder audit log** – Navigate to a folder → More actions → View audits. Requires VIEW privilege on the folder.
- **Subject audit log** – Navigate to a subject → More actions → View audits. Requires permission to view subject audits.

To export:

1. Optionally set date filters (since, before, between, on a date) and/or audit type filter (memberships, actions, privileges)
2. Optionally check "Show extended results" to include additional columns in the export
3. Click **Export**

The CSV file will download with a filename based on the entity name (e.g., `groupAuditLog_myGroup.csv`).

**Standard columns:** Date, Actor, Engine, Summary

**Extended columns** (when "Show extended results" is checked): Date, Actor, Engine, Summary, Duration, Query count, Server username, Server, User IP address, Entry ID, Raw description

The export includes the most recent entries up to a configurable maximum (default 10,000). To export a specific window of audit history, narrow the date range filter before exporting.

**Configuration**

The maximum number of exported entries is configurable per entity type. Set to `-1` for no limit. These can be set in the Grouper UI configuration screen or in `grouper.properties`:

| Property | Default | Description |
| --- | --- | --- |
| `grouper.audit.export.maximumGroupExportEntries` | `10000` | Max entries exported from the group audit log |
| `grouper.audit.export.maximumSubjectExportEntries` | `10000` | Max entries exported from the subject audit log |
| `grouper.audit.export.maximumStemExportEntries` | `10000` | Max entries exported from the folder audit log |
