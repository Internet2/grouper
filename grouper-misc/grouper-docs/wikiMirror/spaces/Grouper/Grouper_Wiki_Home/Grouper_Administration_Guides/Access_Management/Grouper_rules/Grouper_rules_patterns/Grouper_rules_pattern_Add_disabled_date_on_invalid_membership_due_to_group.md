---
title: "Grouper rules pattern - Add disabled date on invalid membership due to group"
space: Grouper
pageId: 28555684
version: 21
lastUpdated: 2026-07-01T05:37:38.743Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555684/Grouper+rules+pattern+-+Add+disabled+date+on+invalid+membership+due+to+group
---

[Grouper rules](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545173/Grouper+rules)

 > Grouper rules are a long-standing core feature, present in all currently supported releases (verified in the supported releases). This pattern is assigned to a group by assigning attributes from the built-in `etc:attribute:rules` definitions; those definitions are not granted to ordinary users by default, so this rule is normally set by a Grouper administrator who can **ADMIN** the rule group and the qualifying group.

  > **VERIFY:** This page overlaps with "Grouper rules pattern - Add disabled date on invalid membership" and may be a candidate to merge into it (the original page carried a "merge" TODO). This "due to group" variant specifically documents `RuleApi.groupIntersection`, where the qualifying condition is membership in another group. Confirm with the sponsor whether to keep it separate or merge.

 

## Overview

 Two groups are involved:

 

- The **rule group** — where the rule is assigned and where the disabled (end) date is set on memberships.
- The **qualifying group** — the group a user must stay a member of (the rule's "must be in" group).

 When a user is removed from the qualifying group, and that user's membership in the rule group does not already have an end date, the rule sets a disabled date a fixed number of days in the future on the user's membership in the rule group — giving a grace period rather than removing access immediately. Existing end dates are left untouched, and users who remain in the qualifying group are unaffected. The optional (e.g. nightly) rules daemon also re-checks memberships, so the date is applied even when the change happens outside a live transaction.

 For example, with a course group as the qualifying group and a course wiki group as the rule group: once a student is no longer in the course group, their membership in the course wiki group is set to expire one week later.

 

## Attribute setup on the rule group

 Assign the `etc:attribute:rules:rule` marker to the rule group with the following attribute values:

 

| Attribute | Value |
| --- | --- |
| `ruleActAsSubjectSourceId` | source id of the subject the rule acts as (example: `g:isa` for `GrouperSystem`) |
| `ruleActAsSubjectId` | subject id the rule acts as (example: `GrouperSystem`) |
| `ruleCheckOwnerId` (or `ruleCheckOwnerName`) | the qualifying group the user must stay in — by id, or by full group name (example: `userFolders:testUser:aGroup`) |
| `ruleCheckType` | `flattenedMembershipRemove` (direct and indirect memberships) or `membershipRemove` (direct memberships only) |
| `ruleIfConditionEnum` | `thisGroupHasImmediateEnabledNoEndDateMembership` |
| `ruleThenEnum` | `assignMembershipDisabledDaysForOwnerGroupId` |
| `ruleThenEnumArg0` | number of days in the future to set the disabled date (example: `7`) |
| `ruleThenEnumArg1` | `T` = add the membership to the rule group if it does not exist; `F` = leave missing memberships alone |

 If everything is valid, Grouper adds a `ruleValid` attribute with the value `T`. Until that value is `T` the rule is not in effect; if it holds an error message instead, use it to find the problem.

 

## GSH shorthand method

 The `RuleApi.groupIntersection` convenience method assigns the same rule in one call. The arguments are the act-as subject, the rule group, the qualifying group, and the number of days in the future to set the disabled date.

 
```java
RuleApi.groupIntersection(subjectActAs, ruleGroup, qualifyingGroup, 7);

```

 

## Java example

 Equivalent to the shorthand above, assigning each attribute explicitly. This mirrors what `RuleApi.groupIntersection` does internally (verified in the supported releases); the convenience method uses `flattenedMembershipRemove` and sets `ruleThenEnumArg1` to `F`.

  
```java
    AttributeAssign attributeAssign = ruleGroup
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();

    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();

    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), actAs.getSourceId());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectIdName(), actAs.getId());

    //if the user falls out of mustBeInGroup, then set a disabled date in this group
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckOwnerIdName(), mustBeInGroup.getId());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckTypeName(),
        RuleCheckType.flattenedMembershipRemove.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleIfConditionEnumName(),
        RuleIfConditionEnum.thisGroupHasImmediateEnabledNoEndDateMembership.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumName(), RuleThenEnum.assignMembershipDisabledDaysForOwnerGroupId.name());

    //number of days in future that disabled date should be set
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumArg0Name(), "7");

    //if the membership in owner group doesnt exist, should it be added?  T|F
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumArg1Name(), "F");

    //should be valid
    String isValidString = attributeValueDelegate.retrieveValueString(
        RuleUtils.ruleValidName());

    if (!StringUtils.equals("T", isValidString)) {
      throw new RuntimeException(isValidString);
    }

```

  

## GSH test case

  
```text
gsh 0% grouperSession = GrouperSession.startRootSession();
edu.internet2.middleware.grouper.GrouperSession: f234aa6876784ea0990ae1aba754d5a7,'GrouperSystem','application'
gsh 1% groupA = new GroupSave(grouperSession).assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();
group: name='stem:a' displayName='stem:a' uuid='4354a7db631e42bf93ac08eb5288b2c9'
gsh 2%  groupB = new GroupSave(grouperSession).assignName("stem:b").assignCreateParentStemsIfNotExist(true).save();
group: name='stem:b' displayName='stem:b' uuid='fa2fe9a442a44169875e82954386a332'
gsh 3% subjectActAs = SubjectFinder.findByIdAndSource("GrouperSystem", "g:isa", true);
subject: id='GrouperSystem' type='application' source='g:isa' name='GrouperSysAdmin'
gsh 4% RuleApi.groupIntersection(subjectActAs, groupA, groupB, 7);
gsh 5% addMember("stem:a", "test.subject.0");
true
gsh 6% addMember("stem:b", "test.subject.0");
true
gsh 7% delMember("stem:b", "test.subject.0");
true
gsh 8% hasMember("stem:a", "test.subject.0");
true
gsh 10% subject0 = SubjectFinder.findById("test.subject.0", true);
subject: id='test.subject.0' type='person' source='jdbc' name='my name is test.subject.0'
gsh 11% member0 = MemberFinder.findBySubject(grouperSession, subject0, false);
member: id='test.subject.0' type='person' source='jdbc' uuid='d20d4de2c7074da0a6a286f2b249d5ec'
gsh 12% membership = groupA.getImmediateMembership(Group.getDefaultList(), member0, true, true);
edu.internet2.middleware.grouper.Membership: Membership[createTime=1283754246504,creatorUuid=b0ad34466f1f401ba33c49cba4197cdb,depth=0,listName=members,listType=list,memberUuid=d20d4de2c7074da0a6a286f2b249d5ec,groupId=4354a7db631e42bf93ac08eb5288b2c9,type=immediate,uuid=4e107bdd371f49428ac65615fe43eab6:95f8d90414704e27bd558d41b03f9ba0]
gsh 13% membership.getDisabledTime()
java.sql.Timestamp: 2010-09-13 02:24:20.167
gsh 14%

```

  

## GSH daemon test case

  Run the commands in the GSH test case above, and continue below.

 
```text
//get back to normal data
gsh 13% delMember("stem:a", "test.subject.0");
true

//subject0 should not be there
gsh 15% addMember("stem:a", "test.subject.0");
true

//subject1 is ok
gsh 16% addMember("stem:a", "test.subject.1");
true

//subject2 has a disabled date already, and shouldnt be touched
gsh 17% addMember("stem:a", "test.subject.2");
true

gsh 18% addMember("stem:b", "test.subject.1");
true
gsh 20% subject1 = SubjectFinder.findById("test.subject.1", true);
subject: id='test.subject.1' type='person' source='jdbc' name='my name is test.subject.1'
gsh 21% subject2 = SubjectFinder.findById("test.subject.2", true);
subject: id='test.subject.2' type='person' source='jdbc' name='my name is test.subject.2'
gsh 22% member1 = MemberFinder.findBySubject(grouperSession, subject1, false);
member: id='test.subject.1' type='person' source='jdbc' uuid='328d340a9d6d4774af8d12c1a6753d8e'
gsh 23% member2 = MemberFinder.findBySubject(grouperSession, subject2, false);
member: id='test.subject.2' type='person' source='jdbc' uuid='8f039afe4adf4770ad5ade263031f558'

//set disabled date for subject2
gsh 24% membership = groupA.getImmediateMembership(Group.getDefaultList(), member2, true, true);
edu.internet2.middleware.grouper.Membership: Membership[createTime=1285559733570,creatorUuid=7a06fd612353403dafe003630a5205a7,depth=0,listName=members,listType=list,memberUuid=8f039afe4adf4770ad5ade263031f558,groupId=9c1f128179d444c6970499e1a274dfa4,type=immediate,uuid=d3508dd616954915be37a6bbb2cdbce9:65d65d4d43d049358ca5348e81a6a1a3]
gsh 26% membership.setDisabledTime(new java.sql.Timestamp(System.currentTimeMillis() + (3 * 24 * 60 * 60 * 1000)));
gsh 27% GrouperDAOFactory.getFactory().getMembership().update(membership);

//run the daemon
gsh 28% status = GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);
loader ran successfully: Ran rules daemon, changed 0 records

//all groups should still have the members
gsh 29% hasMember("stem:a", "test.subject.0");
true
gsh 30% hasMember("stem:a", "test.subject.1");
true
gsh 31% hasMember("stem:a", "test.subject.2");
true

//the first one has 7 day forward disabled date
gsh 32% membership = groupA.getImmediateMembership(Group.getDefaultList(), member0, true, true);
edu.internet2.middleware.grouper.Membership: Membership[createTime=1285559719988,creatorUuid=7a06fd612353403dafe003630a5205a7,depth=0,listName=members,listType=list,memberUuid=090557f1838d40c4b594234632d82dae,groupId=9c1f128179d444c6970499e1a274dfa4,type=immediate,uuid=6d4e0e55ad7d496785a5d4df522fbbab:65d65d4d43d049358ca5348e81a6a1a3]
gsh 33% membership.getDisabledTime()
java.sql.Timestamp: 2010-10-03 23:58:37.904

//second one is ok, has no disabled date
gsh 34% membership = groupA.getImmediateMembership(Group.getDefaultList(), member1, true, true);
edu.internet2.middleware.grouper.Membership: Membership[createTime=1285559728869,creatorUuid=7a06fd612353403dafe003630a5205a7,depth=0,listName=members,listType=list,memberUuid=328d340a9d6d4774af8d12c1a6753d8e,groupId=9c1f128179d444c6970499e1a274dfa4,type=immediate,uuid=64ed8f25035d475490f774bd798910a0:65d65d4d43d049358ca5348e81a6a1a3]
gsh 35% membership.getDisabledTime()

//subject2 should keep the old disabled date, 3 days in future
gsh 36% membership = groupA.getImmediateMembership(Group.getDefaultList(), member2, true, true);
edu.internet2.middleware.grouper.Membership: Membership[createTime=1285559733570,creatorUuid=7a06fd612353403dafe003630a5205a7,depth=0,listName=members,listType=list,memberUuid=8f039afe4adf4770ad5ade263031f558,groupId=9c1f128179d444c6970499e1a274dfa4,type=immediate,uuid=d3508dd616954915be37a6bbb2cdbce9:65d65d4d43d049358ca5348e81a6a1a3]
gsh 37% membership.getDisabledTime()
java.sql.Timestamp: 2010-09-29 23:58:07.517
gsh 38%

```

  

## Steps to add the rule via the UI

 Assume there is a qualifying group and a rule group. (The groups can be anywhere in Grouper.) When a membership is removed from the qualifying group, the rule sets the membership in the rule group to expire a fixed number of days later, and can optionally add that membership if it is missing. This date is reset every time the user is removed from the qualifying group.

 

- Navigate to the rule group in the UI.
- Assign the attribute `etc:attribute:rules:rule` to the group (use the "More actions" menu, the "Attribute assignments" item, then the "+ Assign attribute" button, and paste the name into the "Attribute name:" field).
- Add metadata assignments to that attribute (find the "rule" attribute in the list, click the "Actions" button on that row in the "Choose action" column, and select "Add metadata Assignment") for each of the attributes below. The order does not matter much; you can add all the attributes first and then set the values in a second pass.
- Add metadata assignment: `etc:attribute:rules:ruleActAsSubjectSourceId` 
  
  - Add value `g:isa` (add values with the "Actions" button at the end of the new row, then "Add value"; set the value in "Value to add" and click "Submit" to be sent back to the assigned-attributes table).
- Add metadata assignment: `etc:attribute:rules:ruleActAsSubjectId` 
  
  - Add value `GrouperSystem` (via the "Actions" button, as above).
- Add metadata assignment: `etc:attribute:rules:ruleCheckType` 
  
  - Add value `membershipRemove` (direct memberships only) or `flattenedMembershipRemove` (direct and indirect).
- Add metadata assignment: `etc:attribute:rules:ruleCheckOwnerName` 
  
  - Add value = the full path to the qualifying group (example: `userFolders:testUser:aGroup`).
- Add metadata assignment: `etc:attribute:rules:ruleThenEnum` 
  
  - Add value `assignMembershipDisabledDaysForOwnerGroupId`.
- Add metadata assignment: `etc:attribute:rules:ruleThenEnumArg0` 
  
  - Add value = the number of days in the future to disable the membership.
- Add metadata assignment: `etc:attribute:rules:ruleThenEnumArg1` 
  
  - Add value `T` to create the membership if it does not exist, or `F` to not create it.

 If everything is set properly, the system adds a `etc:attribute:rules:ruleValid` metadata assignment with the value `T` — or an error message to help find the problem. Until the value is `T` the rule is not in effect.
