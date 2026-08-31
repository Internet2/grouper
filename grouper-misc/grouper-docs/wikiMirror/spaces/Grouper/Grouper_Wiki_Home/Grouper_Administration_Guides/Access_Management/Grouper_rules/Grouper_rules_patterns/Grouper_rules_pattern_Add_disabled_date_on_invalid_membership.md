---
title: "Grouper rules pattern - Add disabled date on invalid membership"
space: Grouper
pageId: 28555253
version: 8
lastUpdated: 2026-07-01T05:38:40.814Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555253/Grouper+rules+pattern+-+Add+disabled+date+on+invalid+membership
---

Add this rule to the group that should receive the disabled date.

[Grouper rules](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545173/Grouper+rules)

If an employee is no longer a member of the employee ref group, and that employee ref group is in a policy subgroup, and there should be a grace period assigned, then add that user to a grace period group with an end date. You could also have a rule like this for people added to a lock-out group. The overall policy group would include the policy subgroup and the grace period group.

## Configure rule for v5+

## Configure rule for v4 and previous

### GSH example

```
grouperSession = GrouperSession.startRootSession();
mustBeInGroup = GroupFinder.findByName(grouperSession, "test:base-eligible", true);
ruleGroup = GroupFinder.findByName(grouperSession, "test:grace-eligible", true);
actAs = grouperSession.getSubject();

AttributeAssign attributeAssign = ruleGroup.getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
 
AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
 
attributeValueDelegate.assignValue(RuleUtils.ruleActAsSubjectSourceIdName(), actAs.getSourceId());
attributeValueDelegate.assignValue(RuleUtils.ruleActAsSubjectIdName(), actAs.getId());
 
attributeValueDelegate.assignValue(RuleUtils.ruleCheckOwnerIdName(), mustBeInGroup.getId());
attributeValueDelegate.assignValue(RuleUtils.ruleCheckTypeName(), RuleCheckType.flattenedMembershipRemove.name());
attributeValueDelegate.assignValue(RuleUtils.ruleThenEnumName(), RuleThenEnum.assignMembershipDisabledDaysForOwnerGroupId.name());
 
attributeValueDelegate.assignValue(RuleUtils.ruleThenEnumArg0Name(), "7");
 
attributeValueDelegate.assignValue(RuleUtils.ruleThenEnumArg1Name(), "T");
 
//should be valid
String isValidString = attributeValueDelegate.retrieveValueString(RuleUtils.ruleValidName());
 
if (!"T".equals(isValidString)) {
  throw new RuntimeException(isValidString);
}

```

The base-eligible also has an employee group

When an employee is no longer an employee, you will see a rule fire here:
