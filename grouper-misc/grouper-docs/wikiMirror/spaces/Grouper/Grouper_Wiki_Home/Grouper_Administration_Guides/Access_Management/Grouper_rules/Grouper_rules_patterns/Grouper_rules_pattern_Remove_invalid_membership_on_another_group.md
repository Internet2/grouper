---
title: "Grouper rules pattern - Remove invalid membership on another group"
space: Grouper
pageId: 28555183
version: 7
lastUpdated: 2026-07-01T05:38:45.910Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555183/Grouper+rules+pattern+-+Remove+invalid+membership+on+another+group
---

Add a rule to a group so that if the users are no longer in the eligible group, they will be removed

Note you can [do this via GSH too](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554980/Grouper+rules+pattern+-+Remove+invalid+membership+due+to+group)

In this example, make an eligible group, lets say "ref:employee"

Todo: Merge this with remove invalid membership due to group.

Make a group of ad hoc people

Add the rule. Note the rule check owner id is the employee group uuid.

Run the daemon and see memberships removed

Now remove subject 3 from its affiliation group

See it removed from the wiki ad hoc group after the rules change log consumer or rules daemon run.
