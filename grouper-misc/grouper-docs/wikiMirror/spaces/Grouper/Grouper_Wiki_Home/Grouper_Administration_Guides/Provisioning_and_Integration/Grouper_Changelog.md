---
title: "Grouper Changelog"
space: Grouper
pageId: 28544508
version: 14
lastUpdated: 2026-11-27T12:01:05.458Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544508/Grouper+Changelog
---

Grouper changes cause change log temp entries, these get processed, and then are pumped to change log consumers. The first process is the change log temp processor

In v4.11.0+ and v5.8.0+ The change log consumer run continuously looking for new work. The composite processor will make sure composite changes are made (and not in a transaction with the change that edited the composite or other membership). In order to see progress show subjobs on the daemon screen

Here is the composite daemon with subjobs

Note you will see these in a constant running state which is normal

This is the second process...

Breakdown of time used to process 1000 consecutive temp change log entries (first process) for membership adds to the same group:

- 3.2% - starting, stopping, committing overall transactions

- 8% - deleting from temp change log

- 10% - querying member in point in time

- 10% - verifying membership doesn't already exist in point in time

- 1.5% - querying group in point in time

- 1.5% - querying field in point in time

- 7.3% - checking if membership is being re-enabled (was previously disabled)

- 15.4% - finding flattened memberships

- 14.8% - saving change log entries

- 12.5% - other overhead with writing flattened memberships in change log (e.g. querying data to put in the change log)

- 11% - saving point in time memberships

 

## Change log monitor screen

*v7.10.0+*

The change log monitor shows how many changes are waiting to be processed, how far behind each change log consumer is, and what is causing a backlog. Use it when composites, rules or provisioning are slow to pick up changes. Only Grouper sysadmins (wheel group or root) can see it. In the UI, go to "Miscellaneous" and click "Change log monitor".

The screen has these sections:

- **"Temp change log"**: the number of pending changes in `grouper_change_log_entry_temp`, the oldest pending change, how many were moved to the change log in the last hour, the estimated time to process the pending changes at that rate, and the latest run of the `CHANGE_LOG_changeLogTempToChangeLog` daemon. If changes are pending but none moved in the last hour, it says so; check the daemon logs. The pending count can go up while the daemon is working, because consumers such as composites, rules, provisioners and the loader make more changes as they run.
- **"Change log consumers"**: for each consumer, how many change log entries it is behind (its last processed sequence in `grouper_change_log_consumer` against the latest sequence), its oldest unprocessed change, when it last moved forward, and its latest run, with a "View logs" link. The consumers furthest behind are first. It flags consumers that are not configured anymore, and consumers that missed entries because they were deleted before being processed (the change log is kept for `loader.retain.db.change_log_entry.days`, default 14, in grouper-loader.properties).
- **"Progress"**: charts of the changes moved from the temp change log, and of the entries each consumer processed, for the "Last hour" up to the "Last 7 days". They are read from the daemon logs (`grouper_loader_log`), so they only go back as far as those are kept (`loader.retain.db.logs.days`, default 7). Times are in your browser's time zone.
- **"What is pending"**: click "Show what is pending" to see the pending changes by type (category and action), the 20 groups and folders with the most pending membership and privilege changes, and the 20 actions that caused the most pending changes. Changes made by one action (for example one loader job run, one import, or deleting a big group) share a context id, and the action's audit entry (action, who, description) is shown if it was audited. This reads every pending change, which can take a while when a lot are pending, so it only runs when you ask.

Click "Refresh" to update the screen.
