---
title: "Bad memberships finder v2.5 update"
space: Grouper
pageId: 28554401
version: 3
lastUpdated: 2026-07-01T05:40:33.691Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554401/Bad+memberships+finder+v2.5+update
---

[Bad membership finder](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549224/Bad+Membership+Finder) will be re-written in v2.5.X to address performance issues. In some databases it takes hours and hours to complete. In some database crashes (mysql). And the fixes for composites take too long.

## Bad membership finder change log consumer

1. Look at change log entries, run every 10 minutes
2. If a composite was changed more than 10 (configurable) minutes ago
  
  1. If not, dont indicate done with that part of change log, send it back (this wont cause an error right? maybe we need a setting in change log to only send messages X minutes old so it doesnt think there is a problem)
3. And if it has not be processed before the event occurred (keep track in marker and name/value pair attribute on composite)
4. Process it

1. If a message from Grouper built in messages comes in to process a composite, process if more than 10 minutes old
2. And if it has not be processed after the event occurred (keep track in marker and name/value pair attribute on composite)
3. Process it

Processing

1. See if we can process multiple groups at once, if so, batch it up, if not, one at a time
2. Perhaps later consider how big the group is (once we cache an approx group size)
3. Compare what members should be in group with the composite, and fix it if necessary
4. If there is a fix it should cause message to go back and process group again to see if fixed

## Bad membership finder daemon

1. Asynchronous daemon intended to run nightly
2. Can run at same time as incremental
3. Process all groups (in batches? individually?)
4. If there is a fix needed, send a message to the incremental to process it
5. No need to run full multiple times
6. No need to split out this part of daemon from other parts of bad membership daemon
