---
title: "Grouper rules pattern - Add self privileges to new groups"
space: Grouper
pageId: 28555612
version: 7
lastUpdated: 2024-04-20T17:12:27.288Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555612/Grouper+rules+pattern+-+Add+self+privileges+to+new+groups
---

If you want members in a group to be able to read the memberships (or other privileges) of the group (and view that it exists), then you can put a rule on a folder so that new groups will have a privilege assigned so that the group itself is assigned to be a READer of the group

Add this rule to the folder where the group is created.

# Configure rule for v5+

******

# Configure rule for v4 and previous

TODO: Change for Enum ***assignSelfGroupPrivilege***

```
grouperSession = GrouperSession.startRootSession();
folder = StemFinder.findByName(grouperSession, "testFolder");
AttributeAssign attributeAssign = folder.getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
attributeValueDelegate.assignValue(RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
attributeValueDelegate.assignValue(RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
attributeValueDelegate.assignValue(RuleUtils.ruleCheckTypeName(), RuleCheckType.groupCreate.name());
attributeValueDelegate.assignValue(RuleUtils.ruleCheckStemScopeName(), Stem.Scope.SUB.name());
attributeValueDelegate.assignValue(RuleUtils.ruleThenElName(),"${ruleElUtils.assignGroupPrivilege(groupId, 'g:gsa', groupId, null, 'read')}");

```
