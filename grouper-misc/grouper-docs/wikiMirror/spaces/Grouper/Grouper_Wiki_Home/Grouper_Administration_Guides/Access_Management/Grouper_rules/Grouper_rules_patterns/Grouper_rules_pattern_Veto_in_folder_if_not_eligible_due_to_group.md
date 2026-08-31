---
title: "Grouper rules pattern - Veto in folder if not eligible due to group"
space: Grouper
pageId: 28555579
version: 21
lastUpdated: 2026-07-01T05:37:51.909Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555579/Grouper+rules+pattern+-+Veto+in+folder+if+not+eligible+due+to+group
---

[Grouper rules](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545173/Grouper+rules)

If a user is not an employee, do not allow to be added to any group in a folder. This is a special rule in that only one can fire, and it needs to be hierarchical. i.e. if things are restricted at an ancestor folder, but opened up in a descendant folder, then allow. The ruleCheckType subjectAssignInStem uses a custom rules engine processor to accomplish this. subjectAssignInStem affects all group memberships, group/folder/attributeDef privileges, and permissions (by folder of attributeDefNameName).

Note, the ruleCheckArg0 is the subject source. If it is blank, then the rule applies to all subject sources. If it is filled in, then the rule only applies to that subject source.

(v2.4 patch) Rules change log consumer will check to see if any memberships need to be removed (based on rule)

(v2.4 patch) Rules daemon will look at memberships in the folder and make sure memberships are valid (based on rule)

1. Get all enabled memberships in folder which are not in group, and see if there are exceptions (e.g. allow group if should allow), remove things not allowed
2. Get all enabled privileges in folder which are not in group, see if there are exceptions (e.g. allow group if should allow), remove things not allowed
3. Get all enabled permissions in folder which are not in group, see if there are exceptions (e.g. allow group if should allow), remove things not allowed
4. Make sure there is a clear log of what happens

Add this rule to the folder that has the groups where the membership is added.

# Configure rule for v5+

# Configure rule for v4 and previous

### Java example

```
//add a rule on stem:a saying if not in stem:b, then dont allow add to stem:a
    AttributeAssign attributeAssign = restrictedStem
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
 
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");

    //subject use means membership add, privilege assign, permission assign, etc.
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckTypeName(), RuleCheckType.subjectAssignInStem.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckStemScopeName(), "SUB");
    
    //this is optional to restrict to source.  I think you will want to do that, or you
    //would need to have all the usable groups in the allowed group...
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckArg0Name(), "jdbc");

    
    attributeValueDelegate.assignValue(
        RuleUtils.ruleIfConditionEnumName(), RuleIfConditionEnum.groupHasNoEnabledMembership.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleIfOwnerNameName(), employeeGroup.getName());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumName(), RuleThenEnum.veto.name());
    
    //key which would be used in UI messages file if applicable
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumArg0Name(), "rule.entity.must.be.a.member.of.etc.employee");
    
    //error message (if key in UI messages file not there)
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumArg1Name(), "Entity cannot be assigned if not a member of etc:employee");
 
    //should be valid
    String isValidString = attributeValueDelegate.retrieveValueString(
        RuleUtils.ruleValidName());
 
    if (!StringUtils.equals("T", isValidString)) {
      throw new RuntimeException(isValidString);
    }

```

### GSH shorthand method

```
RuleApi.vetoSubjectAssignInFolderIfNotInGroup(SubjectFinder.findRootSubject(), restrictedStem, employeeGroup, false, "jdbc", Scope.SUB,
        "rule.entity.must.be.a.member.of.etc.employee", "Entity cannot be assigned if not a member of etc:employee");

```

### GSH test case

```
gsh 0% grouperSession = GrouperSession.startRootSession();
edu.internet2.middleware.grouper.GrouperSession: 4ed601599087457cb12ab96387a4e2e7,'GrouperSystem','application'
gsh 1% allowedGroup = new GroupSave(grouperSession).assignName("stem:allowed").assignCreateParentStemsIfNotExist(true).save();
group: name='stem:allowed' displayName='stem:allowed' uuid='6139ad6ecc004562ab491d97b9ef5829'
gsh 2% restrictedGroup = new GroupSave(grouperSession).assignName("stem2:restricted").assignCreateParentStemsIfNotExist(true).save();
group: name='stem2:restricted' displayName='stem2:restricted' uuid='fe1f2a4f944141d2b77c7400e191e69e'
gsh 3% employeeGroup = new GroupSave(grouperSession).assignName("etc:employee").assignCreateParentStemsIfNotExist(true).save();
group: name='etc:employee' displayName='etc:employee' uuid='b969b29cb83b48bb99cee3fb71595203'
gsh 4% restrictedStem = StemFinder.findByName(grouperSession, "stem2", true);
stem: name='stem2' displayName='stem2' uuid='ca3cc1e40f1a413ab8862acc5d9c1b29'
gsh 6% RuleApi.vetoSubjectAssignInFolderIfNotInGroup(SubjectFinder.findRootSubject(), restrictedStem, employeeGroup, false, "jdbc", Stem.Scope.SUB, "rule.entity.must.be.a.member.of.etc.employee", "Entity cannot be assigned if not a member of etc:employee");
edu.internet2.middleware.grouper.attr.assign.AttributeAssign: AttributeAssign[id=1567066d80684b849a618e06e89496f1,action=assign,attributeDefName=etc:attribute:rules:rule,
  stem=Stem[displayName=stem2,name=stem2,uuid=ca3cc1e40f1a413ab8862acc5d9c1b29,creator=41cbc09bf1a54ece8a9761ab8ba68970]]
gsh 8% subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
subject: id='test.subject.0' type='person' source='jdbc' name='my name is test.subject.0'
gsh 9% restrictedGroup.addMember(subject0);
edu.internet2.middleware.grouper.rules.RuleVeto: rule.entity.must.be.a.member.of.etc.employee: Entity cannot be assigned if not a member of etc:employee,
gsh 12% allowedGroup.addMember(subject0);
gsh 13% employeeGroup.addMember(subject0);
gsh 14% restrictedGroup.addMember(subject0);
gsh 15%

```

### GSH test case of change log consumer and daemon

```
gsh 0% grouperSession = GrouperSession.startRootSession();

gsh 1% allowedGroup = new GroupSave(grouperSession).assignName("stem:allowed").assignCreateParentStemsIfNotExist(true).save();

gsh 2% restrictedGroup = new GroupSave(grouperSession).assignName("stem2:restricted").assignCreateParentStemsIfNotExist(true).save();

gsh 3% restrictedGroupPart = new GroupSave(grouperSession).assignName("stem2:restrictedPart").assignCreateParentStemsIfNotExist(true).save();

gsh 4% subject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);

gsh 5% subject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);

gsh 6% restrictedGroupPart.addMember(subject1);

gsh 7% loaderRunOneJob("CHANGE_LOG_changeLogTempToChangeLog");

gsh 8% restrictedGroupPart.addMember(subject2);

gsh 9% restrictedGroup.addMember(restrictedGroupPart.toSubject());

gsh 10% employeeGroup = new GroupSave(grouperSession).assignName("etc:employee").assignCreateParentStemsIfNotExist(true).save();

gsh 11% employeeGroupPart = new GroupSave(grouperSession).assignName("etc:employeePart").assignCreateParentStemsIfNotExist(true).save();

gsh 12% employeeGroup.addMember(employeeGroupPart.toSubject());

gsh 13% restrictedStem = StemFinder.findByName(grouperSession, "stem2", true);

gsh 14% RuleApi.vetoSubjectAssignInFolderIfNotInGroup(SubjectFinder.findRootSubject(), restrictedStem, employeeGroup, false, "jdbc", Stem.Scope.SUB, "rule.entity.must.be.a.member.of.etc.employee", "Entity cannot be assigned if not a member of etc:employee");

gsh 15% subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);

gsh 16% restrictedGroupPart.addMember(subject0);
edu.internet2.middleware.grouper.rules.RuleVeto: rule.entity.must.be.a.member.of.etc.employee: Entity cannot be assigned if not a member of etc:employee,

gsh 17% restrictedGroupPart.grantPriv(subject0, AccessPrivilege.READ);
edu.internet2.middleware.grouper.rules.RuleVeto: rule.entity.must.be.a.member.of.etc.employee: Entity cannot be assigned if not a member of etc:employee,

gsh 18% assign permission where permission name is in restricted stem, to individual user who is not an employee
edu.internet2.middleware.grouper.rules.RuleVeto: rule.entity.must.be.a.member.of.etc.employee: Entity cannot be assigned if not a member of etc:employee,

gsh 19% allowedGroup.addMember(subject0);

gsh 20% employeeGroupPart.addMember(subject0);

gsh 21% restrictedGroupPart.addMember(subject0);

gsh 22% restrictedGroup.hasMember(subject2);    //yes

gsh 23% loaderRunOneJob("CHANGE_LOG_changeLogTempToChangeLog");

gsh 24% loaderRunOneJob("CHANGE_LOG_grouperRules");

gsh 25% restrictedGroup.hasMember(subject2);    //no

gsh 26% restrictedGroup.hasMember(subject1);    //yes

gsh 27% status = GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);
loader ran successfully: Ran rules daemon, changed 1 records

gsh 28% restrictedGroup.hasMember(subject1);    //no

```
