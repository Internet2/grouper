---
title: "Grouper rules pattern - Remove invalid membership due to group 2"
space: Grouper
pageId: 28554956
version: 7
lastUpdated: 2026-07-12T05:10:32.090Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554956/Grouper+rules+pattern+-+Remove+invalid+membership+due+to+group+2
---

In this case we want the rule to fire when a user is removed from a group, but not do this with the daemon (since newly added members shouldnt be removed)

This example has a group of people who claim a license.

Another group has users who have claimed their license (in that group), and have logged in to the SP in the last 60 days, or have claimed their license in the last 60 days.

Assign this rule to the group where the membership should be removed.

TODO merge with Remove invalid membership due to group

Add the rule on the group where users are removed (Have claimed Jira license group). Note: runDaemon is F so that we are not caught in the race condition where a user claims their license but they are not added to the loaded group yet. This could be handled by another rule which adds to the "allowed to have license" group when they claim their license.
