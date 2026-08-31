---
title: "Grouper rules pattern - Veto if not eligible due to group"
space: Grouper
pageId: 28555443
version: 12
lastUpdated: 2026-07-01T05:38:12.347Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555443/Grouper+rules+pattern+-+Veto+if+not+eligible+due+to+group
---

[Grouper rules](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545173/Grouper+rules)

If a user is not an employee, do not allow to be added to application group

You should consider using [membership requirements](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549482/Grouper+membership+eligibility+requirements) instead of this.

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
        RuleUtils.ruleCheckOwnerNameName(), "stem:a");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckTypeName(), RuleCheckType.membershipAdd.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleIfConditionEnumName(), RuleIfConditionEnum.groupHasNoImmediateEnabledMembership.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleIfOwnerNameName(), "stem:b");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumName(), RuleThenEnum.veto.name());

    //key which would be used in UI messages file if applicable
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumArg0Name(), "rule.entity.must.be.a.member.of.stem.b");

    //error message (if key in UI messages file not there)
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumArg1Name(), "Entity cannot be a member of stem:a if not a member of stem:b");

    //should be valid
    String isValidString = attributeValueDelegate.retrieveValueString(
        RuleUtils.ruleValidName());

    if (!StringUtils.equals("T", isValidString)) {
      throw new RuntimeException(isValidString);
    }

```

### GSH shorthand method

```
RuleApi.vetoMembershipIfNotInGroup(actAsSubject, ruleGroup, mustBeInGroup,
        "rule.entity.must.be.a.member.of.stem.b", "Entity cannot be a member of stem:a if not a member of stem:b");

```

### GSH test case

```
gsh 0% grouperSession = GrouperSession.startRootSession();
edu.internet2.middleware.grouper.GrouperSession: 9df8fdf1c6dd4629b6c9dacd7e0f6f4a,'GrouperSystem','application'
gsh 1% groupA = new GroupSave(grouperSession).assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();
group: name='stem:a' displayName='stem:a' uuid='de3c5d56d14840ee9c9bded29f7f86b5'
gsh 2% groupB = new GroupSave(grouperSession).assignName("stem:b").assignCreateParentStemsIfNotExist(true).save();
group: name='stem:b' displayName='stem:b' uuid='fc1a3465730a4f0e86d6b0c74dcd8fcb'
gsh 3% subjectActAs = SubjectFinder.findByIdAndSource("GrouperSystem", "g:isa", true);
subject: id='GrouperSystem' type='application' source='g:isa' name='GrouperSysAdmin'
gsh 8% RuleApi.vetoMembershipIfNotInGroup(subjectActAs, groupA, groupB, "rule.entity.must.be.a.member.of.stem.b", "Entity cannot be a member of stem:a if not a member of stem:b");
gsh 9% addMember("stem:b", "test.subject.1");
true
gsh 10% addMember("stem:a", "test.subject.1");
true
gsh 11% addMember("stem:a", "test.subject.0");
// Error: unable to evaluate command: Sourced file: inline evaluation of: ``addMember("stem:a", "test.subject.0");'' : Error invoking compiled command: : Error in compiled command: edu.internet2.middleware.grouper.rules.RuleVeto: rule.entity.must.be.a.member.of.stem.b: Entity cannot be a member of stem:a if not a member of stem:b,
, group name: stem:a, subject: Subject id: test.subject.0, sourceId: jdbc, field: members
gsh 12% hasMember("stem:a", "test.subject.0");
false
gsh 13% hasMember("stem:a", "test.subject.1");
true
gsh 14%

```
