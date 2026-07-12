---
title: "Grouper rules pattern - Forever membership"
space: Grouper
pageId: 28554699
version: 10
lastUpdated: 2026-07-01T05:39:50.893Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554699/Grouper+rules+pattern+-+Forever+membership
---

[Grouper rules](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545173/Grouper+rules)

 > Grouper rules are a long-standing core feature, present in all currently supported releases (verified in the supported releases). Configuring a rule assigns attributes from the built-in `etc:attribute:rules` definitions to the owner group; those definitions are not granted to ordinary users by default, so rules are normally set by a Grouper administrator who can also **ADMIN** the owner group.

 

## Overview

 A source group has fluctuating memberships, and you want a "forever membership" group that represents everyone who has ever been a member of the source group. Set a rule on the forever group whose `ruleCheckOwnerName` is the name of the source group, using `flattenedMembershipAdd` to include indirect memberships or `membershipAdd` for direct memberships only.

 

 

## Attribute setup on the forever group

 Assign the `etc:attribute:rules:rule` marker to the forever group with the following attribute values:

 

| Attribute | Value |
| --- | --- |
| `ruleActAsSubjectId` | `GrouperSystem` |
| `ruleActAsSubjectSourceId` | `g:isa` |
| `ruleCheckOwnerName` | name of the source group (example: `basis:sourceGroupName`) |
| `ruleCheckType` | `membershipAdd` (direct memberships) or `flattenedMembershipAdd` (include indirect memberships) |
| `ruleIfConditionEnum` (optional) | `groupHasNoImmediateEnabledMembership` |
| `ruleThenEnum` | `addMemberToOwnerGroup` |
| `ruleRunDaemon` | `false` |

 

## v5 example

 

 

## GSH example

 
```java
grouperSession = GrouperSession.startRootSession();
overallGroup = GroupFinder.findByName(grouperSession, "penn:library:services:faculty:facultyexpress");

attributeAssign = overallGroup.getAttributeDelegate().assignAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();

attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleCheckOwnerNameName(), "penn:library:services:faculty:facultyexpress_systemOfRecord");
attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleCheckTypeName(), RuleCheckType.membershipAdd.name());
attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleIfConditionEnumName(), RuleIfConditionEnum.groupHasNoImmediateEnabledMembership.name());
attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleThenEnumName(), RuleThenEnum.addMemberToOwnerGroup.name());
attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleRunDaemonName(), "false");

```
