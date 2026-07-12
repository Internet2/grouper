---
title: "Grouper rules pattern - Remove invalid membership due to folder"
space: Grouper
pageId: 28555618
version: 12
lastUpdated: 2026-07-01T05:37:47.871Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555618/Grouper+rules+pattern+-+Remove+invalid+membership+due+to+folder
---

[Grouper rules](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545173/Grouper+rules)

If an entity falls out of any group in the IT organization groups (meaning not a central IT employee anymore), then remove them from group X

Assign this rule to the group where the membership should be removed.

# Configure rule for v5+

# Configure rule for v4 and previous

### Java example

```
    //add a rule on stem:a saying if you are out of stem:b, then remove from stem:a
    AttributeAssign attributeAssign = groupA
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();

    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();

    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");

    //folder where membership was removed
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckOwnerNameName(), "stem2");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckTypeName(),
        RuleCheckType.membershipRemoveInFolder.name());

    //SUB for all descendants, ONE for just children
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckStemScopeName(),
        Stem.Scope.SUB.name());

    //if there is no more membership in the folder, and there is a membership in the group
    attributeValueDelegate.assignValue(
        RuleUtils.ruleIfConditionEnumName(),
        RuleIfConditionEnum.thisGroupAndNotFolderHasImmediateEnabledMembership.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumName(),
        RuleThenEnum.removeMemberFromOwnerGroup.name());

    //should be valid
    String isValidString = attributeValueDelegate.retrieveValueString(
        RuleUtils.ruleValidName());

    if (!StringUtils.equals("T", isValidString)) {
      throw new RuntimeException(isValidString);
    }

```

### GSH shorthand method

```
RuleApi.groupIntersectionWithFolder(actAsSubject, group, stem, Scope.SUB);

```

### GSH test case

```
gsh 0% grouperSession = GrouperSession.startRootSession();
edu.internet2.middleware.grouper.GrouperSession: d53d7312930347649eda6fab89ad7ada,'GrouperSystem','application'
gsh 1% groupA = new GroupSave(grouperSession).assignName("stem1:a").assignCreateParentStemsIfNotExist(true).save();
group: name='stem1:a' displayName='stem1:a' uuid='6557bf47e6d64c398a10ce4a16661c74'
gsh 2% groupB = new GroupSave(grouperSession).assignName("stem2:b").assignCreateParentStemsIfNotExist(true).save();
group: name='stem2:b' displayName='stem2:b' uuid='9dae005fc0d44358823fc1e1107def92'
gsh 3% groupC = new GroupSave(grouperSession).assignName("stem2:sub:c").assignCreateParentStemsIfNotExist(true).save();
group: name='stem2:sub:c' displayName='stem2:sub:c' uuid='c7290e9b53e045f8a6ee0c3a7f9ecd3f'
gsh 4% stem = StemFinder.findByName(grouperSession, "stem2", true);
stem: name='stem2' displayName='stem2' uuid='5a68107654494485909e58f9b3c02b42'
gsh 5% RuleApi.groupIntersectionWithFolder(SubjectFinder.findRootSubject(), groupA, stem, Stem.Scope.SUB);
gsh 6% addMember("stem2:b", "test.subject.0");
true
gsh 7% addMember("stem1:a", "test.subject.0");
true
gsh 8% delMember("stem2:b", "test.subject.0");
true
gsh 9% hasMember("stem1:a", "test.subject.0");
false
gsh 10% addMember("stem2:sub:c", "test.subject.0");
true
gsh 11% addMember("stem1:a", "test.subject.0");
true
gsh 12% delMember("stem2:sub:c", "test.subject.0");
true
gsh 13% hasMember("stem1:a", "test.subject.0");
false
gsh 14% addMember("stem2:sub:c", "test.subject.0");
true
gsh 15% addMember("stem2:b", "test.subject.0");
true
gsh 16% addMember("stem1:a", "test.subject.0");
true
gsh 17% delMember("stem2:b", "test.subject.0");
true
gsh 18% hasMember("stem1:a", "test.subject.0");
true
gsh 19% delMember("stem2:sub:c", "test.subject.0");
true
gsh 20% hasMember("stem1:a", "test.subject.0");
false
gsh 21%

```

### GSH daemon test case

```
gsh 0% grouperSession = GrouperSession.startRootSession();
edu.internet2.middleware.grouper.GrouperSession: 0d49834d98554f169f051ef935a02a73,'GrouperSystem','application'
gsh 1% groupA = new GroupSave(grouperSession).assignName("stem1:a").assignCreateParentStemsIfNotExist(true).save();
group: name='stem1:a' displayName='stem1:a' uuid='ce47626ff0ec484cbe6eb615b7ed3d45'
gsh 2% groupB = new GroupSave(grouperSession).assignName("stem2:b").assignCreateParentStemsIfNotExist(true).save();
group: name='stem2:b' displayName='stem2:b' uuid='c88ff0d791f143279a3f429734209c1e'
gsh 3% groupC = new GroupSave(grouperSession).assignName("stem2:sub:c").assignCreateParentStemsIfNotExist(true).save();
group: name='stem2:sub:c' displayName='stem2:sub:c' uuid='5ab490b25b9c4f32aa0c7d0d1d6acd8a'
gsh 4% stem = StemFinder.findByName(grouperSession, "stem2", true);
stem: name='stem2' displayName='stem2' uuid='32afc339e5be416d89e6dd03921b9d6f'
gsh 5% RuleApi.groupIntersectionWithFolder(SubjectFinder.findRootSubject(), groupA, stem, Stem.Scope.SUB);
edu.internet2.middleware.grouper.attr.assign.AttributeAssign: AttributeAssign[id=258ed40a395d47dc9e80a5055ce5018c,action=assign,attributeDefName=etc:attribute:rules:rule,
  group=Group[name=stem1:a,uuid=ce47626ff0ec484cbe6eb615b7ed3d45]]
gsh 6% addMember("stem1:a", "test.subject.0");
true
gsh 7% addMember("stem1:a", "test.subject.1");
true
gsh 9% addMember("stem2:b", "test.subject.1");
true
gsh 10% status = GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);
loader ran successfully: Ran rules daemon, changed 0 records
gsh 11% hasMember("stem1:a", "test.subject.0");
false
gsh 12% hasMember("stem1:a", "test.subject.1");
true
gsh 13%

```
