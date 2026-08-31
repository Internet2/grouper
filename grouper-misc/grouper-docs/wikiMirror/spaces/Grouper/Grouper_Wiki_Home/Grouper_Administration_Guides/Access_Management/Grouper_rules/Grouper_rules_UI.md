---
title: "Grouper rules UI"
space: Grouper
pageId: 28549263
version: 29
lastUpdated: 2026-07-01T05:42:24.461Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549263/Grouper+rules+UI
---

This is in Grouper v5+. This UI shows rules, allows adding, editing, deleting rules. The rules are edited by "pattern". The underlying storage of the rule definition has not changed and is still assignments in the attribute framework. Glossary

## Patterns

[This is the list of rules patterns that are available in the UI](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548037/Grouper+rules+patterns)

## Privileges

| Action | Entity |
| --- | --- |
| All actions | Sysadmin user |
| See all rules | Readonly sysadmin |
| Perform add / edit / delete actions on rules in general | If there is a grouper.properties   ``` rules.restrictRulesUiToMembersOfThisGroupName = etc:rulesEditors ```  If the user using the UI is in the etc:rulesEditors group.  This does not apply to sysadmin or readonly sysadmin.  If this is not configured then there is no global rules group.  Whether this configuration is set or not, the following object privileges apply. |
| Use email in the result clause | If there is a grouper.properties   ``` rules.restrictRulesEmailSendersToMembersOfThisGroupName = etc:rulesEmailResultAllowed ```  If the user using the UI is in the etc:rulesEmailResultAllowed group.  This does not apply to sysadmin or readonly sysadmin.  If this is not configured then there is no global rules group.  Whether this configuration is set or not, the following object privileges apply. |
| Edit rules on a group | Group ADMIN privilege on the assignment owner |
| View rules on a group | Group READ privilege on the group |
| Edit rules on a folder | Folder ADMIN privilege on the assignment owner |
| View rules on a folder | Folder CREATE privilege on the folder, or you have inherited (group) READ on the folder |
| Use a group in a "fires when", condition, or result in edit mode | This is context specific based on the "fires when", condition, result.    - Anything with memberships, the user needs READ on the group being used (e.g. flattenedMembershipAdd)  If editing a rule, and a group is used in the rule that the user cannot see the group (see below), then the input will be populated but will get a validation error on submit. |
| Use a folder in a "fires when", condition, or result in edit mode | This is context specific based on the "fires when", condition, result.    - Anything with memberships, the user needs inherited READ on the folder being used (e.g. flattenedMembershipAddInFolder) - "fires when": groupCreate requires ADMIN on the folder or inherited group ADMIN on the folder - "fires when": stemCreate requires ADMIN on the folder or inherited stem ADMIN on the folder - "fires when": attributeDefCreate requires ADMIN on the folder or inherited attributeDef ADMIN on the folder  If editing a rule, and a folder is used in the rule that the user cannot see the folder (see below), then the input will be populated but will get a validation error on submit. |
| See a group/folders in the list of rules for a group (fires, condition, result, assigned on) | If the group (where clicking rules button) is a REF or BASIS type or in config list and the user is not an ADMIN of the group, then the user needs VIEW on the group being used (fires, condition, result, assigned on)  If the group is not REF or BASIS or the user is an ADMIN of the group (where clicking rules button), all are viewable   ``` rules.groupNamesRestrictRules = a:b:c, d:e:f ``` |
| See a group/folder in the list of rules for a folder (fires, condition, result, assigned on) | If the folder (where clicking rules button) is a REF or BASIS type or in config list and the user is not an ADMIN of the folder, then the user needs VIEW (or UI-type view) on the folder being used (fires, condition, result, assigned on)  If the folder is not REF or BASIS or the user is an ADMIN of the folder (where clicking rules button), all are viewable   ``` rules.stemNamesRestrictRules = a:b:c, d:e:f  ``` |

## Screen - folder rules - custom pattern
