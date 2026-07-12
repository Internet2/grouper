---
title: "Grouper rules pattern - Remove invalid membership due to group"
space: Grouper
pageId: 28554980
version: 18
lastUpdated: 2026-07-01T05:39:13.117Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554980/Grouper+rules+pattern+-+Remove+invalid+membership+due+to+group
---

[Grouper rules](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545173/Grouper+rules)

If an entity is no longer a member of the employee group, remove them from the group for application X.

Note, you can key off of membershipRemove or flattenedMembershipRemove

Assign this rule to the group where the membership should be removed.

# Configure rule for v5+

# Configure rule for v4 and previous

### Java example

```
    AttributeAssign attributeAssign = ruleGroup
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();

    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();

    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), actAs.getSourceId());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectIdName(), actAs.getId());

    //note "mustBeInGroup" is the group (e.g. employees)
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckOwnerIdName(), mustBeInGroup.getId());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckTypeName(),
        RuleCheckType.flattenedMembershipRemove.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleIfConditionEnumName(),
        RuleIfConditionEnum.thisGroupHasImmediateEnabledMembership.name());
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
 RuleApi.groupIntersection(subjectActAs, ruleGroup, mustBeInGroup)

```

### GSH test case

```
gsh 0% grouperSession = GrouperSession.startRootSession();
edu.internet2.middleware.grouper.GrouperSession: d711e17ed44842a68b885bca5f294ab3,'GrouperSystem','application'
gsh 1% groupA = new GroupSave(grouperSession).assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();
group: name='stem:a' displayName='stem:a' uuid='4bc47ab6a6704132a73a31d34b83164b'
gsh 2% groupB = new GroupSave(grouperSession).assignName("stem:b").assignCreateParentStemsIfNotExist(true).save();
group: name='stem:b' displayName='stem:b' uuid='22c410c494934a3baff8555940853ad1'
gsh 3% subjectActAs = SubjectFinder.findByIdAndSource("GrouperSystem", "g:isa", true);
subject: id='GrouperSystem' type='application' source='g:isa' name='GrouperSysAdmin'
gsh 4% RuleApi.groupIntersection(subjectActAs, groupA, groupB);
gsh 5% addMember("stem:a", "test.subject.0");
true
gsh 6% addMember("stem:b", "test.subject.0");
true
gsh 7% delMember("stem:b", "test.subject.0");
true
gsh 8% hasMember("stem:a", "test.subject.0");
false
gsh 9%

```

### GSH daemon test case

Run the above test case, then continue below:

```
gsh 9% addMember("stem:a", "test.subject.0");
true
gsh 10% status = GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);
loader ran successfully: Ran rules daemon, changed 0 records
gsh 11% hasMember("stem:a", "test.subject.0");
false

```

### Real world example

There is an includes list for IT staff at Penn. But anyone in that list must be an active employee or health system employee.

```
gsh 0% grouperSession = GrouperSession.startRootSession();
edu.internet2.middleware.grouper.GrouperSession: 6d8c3f9ea4c64e569e8d1bb292e989d1,'GrouperSystem','application'
gsh 1% itStaff_includes = GroupFinder.findByName(grouperSession, "penn:community:employee:itStaff_includes");
group: name='penn:community:employee:itStaff_includes' displayName='penn:community:employee:itStaff_includes' uuid='59cd11b40d49446099e0409f755d9679' 
gsh 2% employeeIncludingUphs = GroupFinder.findByName(grouperSession, "penn:community:employeeIncludingUphs");
group: name='penn:community:employeeIncludingUphs' displayName='penn:community:employeeIncludingUphs' uuid='b0758e19dcd4431798cd5bfcfeb6ea66' 
gsh 3% subjectActAs = SubjectFinder.findByIdAndSource("GrouperSystem", "g:isa", true);
subject: id='GrouperSystem' type='application' source='g:isa' name='GrouperSysAdmin' 
gsh 4% RuleApi.groupIntersection(subjectActAs, itStaff_includes, employeeIncludingUphs);
edu.internet2.middleware.grouper.attr.assign.AttributeAssign: AttributeAssign[id=b3849718eab34496a162165f29ba6b92,action=assign,attributeDefName=penn:etc:attribute:rules:rule,
  group=Group[name=penn:community:employee:itStaff_includes,uuid=59cd11b40d49446099e0409f755d9679]]

```
