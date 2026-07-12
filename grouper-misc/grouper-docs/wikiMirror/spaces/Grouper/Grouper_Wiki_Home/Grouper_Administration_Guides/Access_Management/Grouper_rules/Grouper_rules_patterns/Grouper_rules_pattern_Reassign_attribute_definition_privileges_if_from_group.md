---
title: "Grouper rules pattern - Reassign attribute definition privileges if from group"
space: Grouper
pageId: 28554738
version: 7
lastUpdated: 2026-07-01T05:39:44.806Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554738/Grouper+rules+pattern+-+Reassign+attribute+definition+privileges+if+from+group
---

[Grouper rules](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545173/Grouper+rules)

If an attribute definition is created, and the parent stem CREATE privilege is inherited from a group(s), then remove the individual ATTR_ADMIN privilege from the created attribute definition, and assign the ATTR_ADMIN privilege to the stem CREATE group(s). Note, if the user is a wheel or root, then just remove the individual assignment.

Todo: Check to see if this is automatic in Grouper.

### Java example

```
    //add a rule on stem2 saying if you create a group underneath, then remove admin if in another group which has create on stem
    AttributeAssign attributeAssign = stem2
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
    
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckTypeName(), RuleCheckType.attributeDefCreate.name());
    
    //can be SUB or ONE for if in this folder, or in this and all subfolders
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckStemScopeName(), Stem.Scope.SUB.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumName(), RuleThenEnum.reassignAttributeDefPrivilegesIfFromGroup.name());
    
    //should be valid
    String isValidString = attributeValueDelegate.retrieveValueString(
        RuleUtils.ruleValidName());
  
    if (!StringUtils.equals("T", isValidString)) {
      throw new RuntimeException(isValidString);
    }

```

### GSH shorthand method

```
RuleApi.reassignAttributeDefPrivilegesIfFromGroup(SubjectFinder.findRootSubject(), stem2, Stem.Scope.SUB);

```

### GSH test case

```
TODO

```

### GSH daemon

There is no daemon for this rule
