---
title: "Grouper rules glossary"
space: Grouper
pageId: 28549338
version: 2
lastUpdated: 2026-07-01T05:42:16.310Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549338/Grouper+rules+glossary
---

| Term | Definition |
| --- | --- |
| Rule | Something that listens for certain actions, checks for conditions, and performs an action |
| [Pattern](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548037/Grouper+rules+patterns) | Type of rule that solves an existing use case with a certain action, condition, and result. e.g. "[remove invalid membership due to group](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554956/Grouper+rules+pattern+-+Remove+invalid+membership+due+to+group+2)" |
| Custom pattern | A grouper sysadmin can configure a custom pattern (can select any action, condition, and result).    Non sysadmins can use built-in patterns only. |
| Assigned (on) | Which owner has the attribute assignment |
| Fires when | The action that is being listened for (e.g. flattened membership add in a certain group) |
| "Fires when" owner | The group / folder / etc that is checked for the "fires when" action. e.g. if the "fires when" is "Membership add (flattened) in group",   then the owner is the group being checked. If the "fires when" owner is "this group / folder" then the "fires when" owner is the group / folder   where the rule is assigned. |
| Condition | This is optional, if something should be checked for the rule to proceed after the "fires when" action happens |
| Condition owner | The group / folder / etc that is checked for the condition. e.g. if the condition is "entity is a direct member of the group",   then the owner is the group being checked |
| Condition JEXL script | If the condition requires using JEXL expression language, this is the JEXL expression that should return a boolean for if the rule   should continue firing. The variables available in JEXL depend on the condition type, and the source code should be consulted.   Only Grouper sysadmins can configure JEXL scripts. |
| Result | The action that occurs when the rule "fires when" happens and the optional condition is true |
| Result JEXL script | If the result requires using JEXL expression language, this is the JEXL expression that should perform the action of the result.    The variables available in JEXL depend on the result type, and the source code should be consulted.   Only Grouper sysadmins can configure JEXL scripts. |
| Fires immediately | If the rule happens in the context of the existing transaction. For "fires when" flattened, it is a change log    consumer and not immediately (i.e. will happen in the next minute generally) |
| Rule is valid | This is true if the "fires when", condition, and result are configured in a way that makes sense. If not valid there will be   an attribute assigned to indicate that, and the rule will be disabled |
| Daemon | If the rule is daemonable (e.g. not an email rule), then a nightly daemon can run (unless configured not to) so make sure   the data is consistent |
| Folder scope | When a "fires when" or condition references a folder, this is the determination of if it applies to all objects including subfolders   (most common), or if it is only object directly in the folder (and not subfolders). |
| Flattened | A flattened membership add is when the member was not in the group by any path and is now in the group by some path.   "Fires when" events should generally be flattened. |
| Direct | A membership is added to a group directly (i.e. can be removed directly from group).    This is not due to a membership in a group which is a member of another group.   Conditions about memberships might be direct if checking to see if a membership should be removed as a result (since only    direct memberships can be removed) |
| Assignment owner |  |
