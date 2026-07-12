---
title: "Grouper rules pattern - Veto if not eligible due to folder"
space: Grouper
pageId: 28554765
version: 11
lastUpdated: 2026-07-01T05:39:40.895Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554765/Grouper+rules+pattern+-+Veto+if+not+eligible+due+to+folder
---

[Grouper rules](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545173/Grouper+rules)

If a user is not an employee in a certain org in a folder, do not allow to be added to application group

Add this rule to the group where the membership is being added.

# Configure rule for v5+

# Configure rule for v4 and previous

### Java example

```
    //add a rule on stem:a saying if not in stem:b, then dont allow add to stem:a
    AttributeAssign attributeAssign = ruleGroup
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();

    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();

    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckTypeName(), RuleCheckType.membershipAdd.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleIfConditionEnumName(), RuleIfConditionEnum.noGroupInFolderHasImmediateEnabledMembership.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleIfOwnerNameName(), "stem:orgs:itEmployee");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleIfStemScopeName(), "SUB");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumName(), RuleThenEnum.veto.name());

    //key which would be used in UI messages file if applicable
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumArg0Name(), "rule.entity.must.be.in.IT.employee.to.be.in.group");

    //error message (if key in UI messages file not there)
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumArg1Name(), "Entity cannot be a member of group if not in the IT department org");

    //should be valid
    String isValidString = attributeValueDelegate.retrieveValueString(
        RuleUtils.ruleValidName());

    if (!StringUtils.equals("T", isValidString)) {
      throw new RuntimeException(isValidString);
    }

```

### GSH shorthand method

```
RuleApi.vetoMembershipIfNotInGroupInFolder(SubjectFinder.findRootSubject(), ruleGroup, mustBeInStem, Stem.Scope.SUB, "rule.entity.must.be.in.IT.employee.to.be.in.group", "Entity cannot be a member of group if not in the IT department org");

```

### GSH test case

```
gsh 0% grouperSession = GrouperSession.startRootSession();
edu.internet2.middleware.grouper.GrouperSession: 40ed212f025c46578736f10983e929f7,'GrouperSystem','application'

//here is a group which vould be an application role
gsh 1% ruleGroup = new GroupSave(grouperSession).assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();
group: name='stem:a' displayName='stem:a' uuid='b806f005f9fb4937a4fc6e93256d72b7'

//org groups, IT employees are either programmers or sys admins
gsh 2% groupProgrammers = new GroupSave(grouperSession).assignName("stem:orgs:itEmployee:programmers").assignCreateParentStemsIfNotExist(true).save();
group: name='stem:orgs:itEmployee:programmers' displayName='stem:orgs:itEmployee:programmers' uuid='626eaa3e77fa444c864a690960e0e5da'
gsh 3% groupSysadmins = new GroupSave(grouperSession).assignName("stem:orgs:itEmployee:sysadmins").assignCreateParentStemsIfNotExist(true).save();
group: name='stem:orgs:itEmployee:sysadmins' displayName='stem:orgs:itEmployee:sysadmins' uuid='27553f3a41dc4f87b93957ab9dbd1b0e'
gsh 4% mustBeInStem = StemFinder.findByName(grouperSession, "stem:orgs:itEmployee", true);
stem: name='stem:orgs:itEmployee' displayName='stem:orgs:itEmployee' uuid='d9249cb44e0942dd9d9a4dd972c06c2f'

//setup the rule so that if you arent in the IT department, that you cant be added to the application role
gsh 5% RuleApi.vetoMembershipIfNotInGroupInFolder(SubjectFinder.findRootSubject(), ruleGroup, mustBeInStem, Stem.Scope.SUB, "rule.entity.must.be.in.IT.employee.to.be.in.group", "Entity cannot be a member of group if not in the IT department org");
gsh 6% subject0 = SubjectFinder.findById("test.subject.0", true);
subject: id='test.subject.0' type='person' source='jdbc' name='my name is test.subject.0'

//since this user is not an IT employee, the assignment gets vetoed
gsh 7% ruleGroup.addMember(subject0);
// Error: unable to evaluate command: Sourced file: inline evaluation of: ``ruleGroup.addMember(subject0);'' : Method Invocation ruleGroup.addMember
// See error log for full stacktrace
// caused by: edu.internet2.middleware.grouper.rules.RuleVeto:
// rule.entity.must.be.in.IT.employee.to.be.in.group: Entity cannot be a member of group if not in the IT department org,
, group name: stem:a, subject: Subject id: test.subject.0, sourceId: jdbc, field: members

//add the user to the org, and the user now can be a member of the application role
gsh 8% groupProgrammers.addMember(subject0);
gsh 9% ruleGroup.addMember(subject0);
gsh 10% ruleGroup.hasMember(subject0)
true

//delete the user from the groups, and try the other IT department group, should veto without IT dept, should be OK with
gsh 11% ruleGroup.deleteMember(subject0);
gsh 12% groupProgrammers.deleteMember(subject0);
gsh 13% ruleGroup.addMember(subject0);
// Error: unable to evaluate command: Sourced file: inline evaluation of: ``ruleGroup.addMember(subject0);'' : Method Invocation ruleGroup.addMember
// See error log for full stacktrace
// caused by: edu.internet2.middleware.grouper.rules.RuleVeto:
// rule.entity.must.be.in.IT.employee.to.be.in.group: Entity cannot be a member of group if not in the IT department org,
, group name: stem:a, subject: Subject id: test.subject.0, sourceId: jdbc, field: members
gsh 14% ruleGroup.hasMember(subject0);
false
gsh 15% groupSysadmins.addMember(subject0);
gsh 16% ruleGroup.addMember(subject0);
gsh 17% ruleGroup.hasMember(subject0)
true
gsh 18%

```

### GSH daemon test case

Run the above GSH, then continue below

```
gsh 19% groupSysadmins.deleteMember(subject0);
gsh 20% ruleGroup.hasMember(subject0)
true
gsh 21% status = GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);
loader ran successfully: Ran rules daemon, changed 0 records
gsh 22% ruleGroup.hasMember(subject0)
false

```

sdf
