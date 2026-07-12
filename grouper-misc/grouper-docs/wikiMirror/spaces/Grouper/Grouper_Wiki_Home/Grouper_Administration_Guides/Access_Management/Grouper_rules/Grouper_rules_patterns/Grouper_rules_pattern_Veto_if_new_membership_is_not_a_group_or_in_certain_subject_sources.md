---
title: "Grouper rules pattern - Veto if new membership is not a group or in certain subject sources"
space: Grouper
pageId: 28554971
version: 8
lastUpdated: 2026-07-01T05:39:14.132Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554971/Grouper+rules+pattern+-+Veto+if+new+membership+is+not+a+group+or+in+certain+subject+sources
---

[Grouper rules](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545173/Grouper+rules)

For a policy group, make sure the subject added to the group is a group, not individuals

Add this rule to the group where the membership is added. Todo: Add an if condition enum to check membership source. Instead of checking if it's a group, check to see if it's in the list of allowed sources and out of denied sources.'

# Configure rule for v5+

# Configure rule for v4 and previous

### Allow EL for GrouperSystem

in grouper.properties

Key: **rules.accessToApiInEl.group**

Value: Some group name, e.g. **etc:rulesAccessToApiInEl**

Add GrouperSystem to that group

### GSH example

```
grouperSession = GrouperSession.startRootSession();
ruleGroup = GroupFinder.findByName(grouperSession, "test:testGroup", true);
AttributeAssign attributeAssign = ruleGroup.getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
attributeValueDelegate.assignValue(RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
attributeValueDelegate.assignValue(RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
attributeValueDelegate.assignValue(RuleUtils.ruleCheckTypeName(), RuleCheckType.membershipAdd.name());
attributeValueDelegate.assignValue(RuleUtils.ruleIfConditionElName(), "\${safeSubject.sourceId != 'g:gsa'}");
attributeValueDelegate.assignValue(RuleUtils.ruleThenEnumName(), RuleThenEnum.veto.name());
attributeValueDelegate.assignValue(RuleUtils.ruleThenEnumArg0Name(), "rule.entity.must.be.a.group");
attributeValueDelegate.assignValue(RuleUtils.ruleThenEnumArg1Name(), "Entity must be a group");
String isValidString = attributeValueDelegate.retrieveValueString(RuleUtils.ruleValidName());
if (!GrouperUtil.equals("T", isValidString)) {throw new RuntimeException(isValidString);}   

```
