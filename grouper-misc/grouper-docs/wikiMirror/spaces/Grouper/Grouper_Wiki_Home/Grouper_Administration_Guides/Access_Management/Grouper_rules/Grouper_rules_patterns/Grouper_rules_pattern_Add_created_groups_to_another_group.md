---
title: "Grouper rules pattern - Add created groups to another group"
space: Grouper
pageId: 28554677
version: 12
lastUpdated: 2026-07-01T05:39:53.882Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554677/Grouper+rules+pattern+-+Add+created+groups+to+another+group
---

You can add this rule to a group where the groups would be added. There is no daemon for this pattern, so after you set it up, if there are existing groups to be added, you need to add them manually after setting up the rule. For instance, if there are 5 groups in the stem that should be in the group, add those manually. New groups will get added.

## Configure rule for v5+

The optional regex is new for v4.17.8, v5.18.1

## Configure rule for v4 and previous

| Attribute | Attribute metadata | Value |
| --- | --- | --- |
| rule |  |  |
|  | ruleActAsSubjectId | GrouperSystem |
|  | ruleActAsSubjectSourceId | g:isa |
|  | ruleCheckStemScope | SUB/ONE |
|  | ruleCheckType | groupCreate |
|  | ruleThenEl | ``` ${ruleUtils.group(null, 'destination:groupOfGroups', null, true, true).addMember(ruleUtils.group(null, groupName, null, true, true).toSubject(), false)} ``` |
|  | ruleValid | T |
