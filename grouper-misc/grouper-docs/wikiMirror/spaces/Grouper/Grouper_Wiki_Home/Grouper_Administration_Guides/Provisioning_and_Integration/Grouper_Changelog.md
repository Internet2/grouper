---
title: "Grouper Changelog"
space: Grouper
pageId: 28544508
version: 12
lastUpdated: 2026-07-12T05:43:36.680Z
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
