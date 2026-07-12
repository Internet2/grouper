---
title: "Grouper rules pattern - Inherited privileges on folders"
space: Grouper
pageId: 28554708
version: 14
lastUpdated: 2026-07-01T05:39:49.840Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554708/Grouper+rules+pattern+-+Inherited+privileges+on+folders
---

[Grouper rules](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545173/Grouper+rules)

If a folder is created under folder a:b, then apply privileges to the folder of CREATE,STEM to group a:security:admins

You should use the inherited privileges screen to control this. This rule is on the folder where folders are created.

# Configure rule for v5+

# Configure rule for v4 and previous

### Java example

```
//add a rule on stem2 saying if you create a group underneath, then assign a reader group
    AttributeAssign attributeAssign = stem
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();

    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();

    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), actAs.getSourceId());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectIdName(), actAs.getId());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckTypeName(), RuleCheckType.stemCreate.name());

    //can be SUB or ONE for if should be in all descendants or just on children
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckStemScopeName(), stemScope.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumName(), RuleThenEnum.assignStemPrivilegeToStemId.name());

    //this is the subject string for the subject to assign to
    //e.g. sourceId :::::: subjectIdentifier
    //or sourceId :::: subjectId
    //or :::: subjectId
    //or sourceId ::::::: subjectIdOrIdentifier
    //etc
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumArg0Name(), subjectToAssign.getSourceId() + " :::: " + subjectToAssign.getId());

    //possible privileges are stem and create
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumArg1Name(), Privilege.stringValue(privileges));

    //should be valid
    String isValidString = attributeValueDelegate.retrieveValueString(
        RuleUtils.ruleValidName());

    if (!StringUtils.equals("T", isValidString)) {
      throw new RuntimeException(isValidString);
    }

```

### GSH shorthand method

```
RuleApi.inheritFolderPrivileges(SubjectFinder.findRootSubject(), stem2, Stem.Scope.SUB, groupA.toSubject(), Privilege.getInstances("stem, create"));

```

### GSH test case

```
gsh 0% grouperSession = GrouperSession.startRootSession();
edu.internet2.middleware.grouper.GrouperSession: 09aad006bc554a1dbc8cbe684dad5508,'GrouperSystem','application'
gsh 1% stem2 = new edu.internet2.middleware.grouper.StemSave(grouperSession).assignName("stem2").assignCreateParentStemsIfNotExist(true).save();
stem: name='stem2' displayName='stem2' uuid='b79a373db8304cb9b8c193d3ab1684ca'
gsh 2% groupA = new GroupSave(grouperSession).assignName("stem1:admins").assignCreateParentStemsIfNotExist(true).save();
group: name='stem1:admins' displayName='stem1:admins' uuid='d94dcd40fe414881bdff1eb90b93cc56'
gsh 3% addMember("stem1:admins", "test.subject.0");
true
gsh 4% subjectActAs = SubjectFinder.findByIdAndSource("GrouperSystem", "g:isa", true);
subject: id='GrouperSystem' type='application' source='g:isa' name='GrouperSysAdmin'
gsh 6% RuleApi.inheritFolderPrivileges(subjectActAs, stem2, Stem.Scope.SUB, groupA.toSubject(), Privilege.getInstances("create, stem"));
gsh 7% stemB = new edu.internet2.middleware.grouper.StemSave(grouperSession).assignName("stem2:b").assignCreateParentStemsIfNotExist(true).save();
stem: name='stem2:b' displayName='stem2:b' uuid='8dc178c0e8cd40f2b1958b87c32a99be'
gsh 8% hasPriv("stem2:b", "test.subject.0", Privilege.getInstance("create"))
true
gsh 9% hasPriv("stem2:b", "test.subject.0", Privilege.getInstance("stem"))
true
gsh 10% stemD = new edu.internet2.middleware.grouper.StemSave(grouperSession).assignName("stem3:d").assignCreateParentStemsIfNotExist(true).save();
stem: name='stem3:d' displayName='stem3:d' uuid='8a7f434822524652bd3e8d820e48978b'
gsh 11% hasPriv("stem3:d", "test.subject.0", Privilege.getInstance("create"))
false
gsh 12% hasPriv("stem3:d", "test.subject.0", Privilege.getInstance("stem"))
false
gsh 13% stemC = new edu.internet2.middleware.grouper.StemSave(grouperSession).assignName("stem2:sub:c").assignCreateParentStemsIfNotExist(true).save();
stem: name='stem2:sub:c' displayName='stem2:sub:c' uuid='4d2a5eff7f1c4dd8b0726ff86760d0d3'
gsh 15% hasPriv("stem2:sub:c", "test.subject.0", Privilege.getInstance("create"))
true
gsh 17% hasPriv("stem2:sub:c", "test.subject.0", Privilege.getInstance("stem"))
true
gsh 18%

```

### GSH daemon test case

Run the above GSH and then continue below

```
gsh 18% revokePriv("stem2:sub:c", "test.subject.0", Privilege.getInstance("create"))
false
gsh 19% status = GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);
loader ran successfully: Ran rules daemon, changed 0 records
gsh 20% hasPriv("stem2:sub:c", "test.subject.0", Privilege.getInstance("create"))
true

```

Another GSH test

```
Type help() for instructions
gsh 0% grouperSession = GrouperSession.startRootSession();
edu.internet2.middleware.grouper.GrouperSession: 867846c824334805bc59a369c009acc3,'GrouperSystem','application'
gsh 1% stem_a = new StemSave(grouperSession).assignName("a").assignCreateParentStemsIfNotExist(true).save();
stem: name='a' displayName='a' uuid='30809211370c43a3b234243234234' 
gsh 2% stem_a_b = new StemSave(grouperSession).assignName("a:b").assignCreateParentStemsIfNotExist(true).save();
stem: name='a:b' displayName='a:b' uuid='30809211370c43a3b231231231442' 
gsh 3% stem_a_b_c = new StemSave(grouperSession).assignName("a:b:c").assignCreateParentStemsIfNotExist(true).save();
stem: name='a:b:c' displayName='a:b:c' uuid='30809211234234243231231442' 
gsh 4% stem_a_c = new StemSave(grouperSession).assignName("a:c").assignCreateParentStemsIfNotExist(true).save();
stem: name='a:b:c' displayName='a:b:c' uuid='30809211234234243231231442' 
gsh 5% stem_a_b.hasCreate(SubjectFinder.findById("test.subject.2"));
false
gsh 6% stem_a_b_c.hasCreate(SubjectFinder.findById("test.subject.2"));
false
gsh 7% stem_a_c.hasCreate(SubjectFinder.findById("test.subject.2"));
false
gsh 8% RuleApi.inheritFolderPrivileges(SubjectFinder.findRootSubject(), stem_a, Stem.Scope.SUB, SubjectFinder.findById("test.subject.2"), Privilege.getInstances("stem, create"));
edu.internet2.middleware.grouper.attr.assign.AttributeAssign: AttributeAssign[id=681b3033fc044c25b4c4a4ffdbd3958c,action=assign,attributeDefName=etc:attribute:rules:rule,
  stem=Stem[displayName=a,name=a,uuid=ba7b1db6dda044e3933b0bc0df2f9398,creator=f7c2ea49e9de4a1e8e2f46aaf8603092]]
gsh 9% stem_a_b_c.hasCreate(SubjectFinder.findById("test.subject.2"));
false
gsh 10% RuleApi.runRulesForOwner(stem_a)
1
gsh 11% stem_a_c.hasCreate(SubjectFinder.findById("test.subject.2"));
true
gsh 12% stem_a_b_c.hasCreate(SubjectFinder.findById("test.subject.2"));
true
gsh 13% stem_a_b.hasCreate(SubjectFinder.findById("test.subject.2"));
true
gsh 14% 

```

sdaf
