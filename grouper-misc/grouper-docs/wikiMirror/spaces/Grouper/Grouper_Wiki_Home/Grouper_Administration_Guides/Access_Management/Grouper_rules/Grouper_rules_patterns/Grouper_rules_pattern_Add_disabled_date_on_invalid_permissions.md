---
title: "Grouper rules pattern - Add disabled date on invalid permissions"
space: Grouper
pageId: 28555625
version: 13
lastUpdated: 2026-07-01T05:37:46.840Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555625/Grouper+rules+pattern+-+Add+disabled+date+on+invalid+permissions
---

[Grouper rules](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545173/Grouper+rules)

 > Grouper rules are a long-standing core feature, present in all currently supported releases (verified in the supported releases). The rule is assigned to a permission definition by assigning attributes from the built-in `etc:attribute:rules` definitions; those definitions are not granted to ordinary users by default, so this rule is normally set by a Grouper administrator who can also **ADMIN** the owner permission definition.

  

## Overview

 Users hold a permission (for example `canLogin`) by being in a role, or by a permission assigned directly to them in a role. You want that permission to lapse once a user is no longer in a qualifying group (for example `stem:employee`) — but on a delay, not immediately, so there is a grace period.

 Assign a rule to the permission definition so that when a user is removed from the qualifying group, and that user's permission assignment does not already have an end date, the rule sets a disabled (end) date a fixed number of days in the future on the user's permission-definition assignments — whether the permission comes through a role or is assigned directly to the user. Existing end dates are left untouched, and users who remain in the qualifying group are unaffected.

 

## Attribute setup on the permission definition

 Assign the `etc:attribute:rules:rule` marker to the permission definition (the `AttributeDef` of type `perm`) with the following attribute values:

 

| Attribute | Value |
| --- | --- |
| `ruleActAsSubjectSourceId` | source id of the subject the rule acts as (example: `g:isa` for `GrouperSystem`) |
| `ruleActAsSubjectId` | subject id the rule acts as (example: `GrouperSystem`) |
| `ruleCheckOwnerId` | id of the qualifying group the user must stay in (example: the `stem:employee` group) |
| `ruleCheckType` | `flattenedMembershipRemove` (include indirect memberships) or `membershipRemove` (direct memberships only) |
| `ruleIfConditionEnum` | `thisPermissionDefHasNoEndDateAssignment` |
| `ruleThenEnum` | `assignDisabledDaysToOwnerPermissionDefAssignments` |
| `ruleThenEnumArg0` | number of days in the future to set the disabled date (example: `7`) |

 

## GSH shorthand method

 The `RuleApi.permissionGroupIntersection` convenience method assigns the same rule in one call. The last argument is the number of days in the future to set the disabled date.

 
```java
RuleApi.permissionGroupIntersection(SubjectFinder.findRootSubject(), permissionDef, groupEmployee, 7);

```

 

## Java example

 Equivalent to the shorthand above, assigning each attribute explicitly. This mirrors what `RuleApi.permissionGroupIntersection` does internally (verified in the supported releases).

 
```java
    //add a rule on the permission definition saying if you are out of the employee group,
    //then put a disabled date on assignments to the permission, or from roles which have the permission
    AttributeAssign attributeAssign = permissionToAssignRule
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();

    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), actAs.getSourceId());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectIdName(), actAs.getId());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckOwnerIdName(), mustBeInGroup.getId());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckTypeName(), RuleCheckType.flattenedMembershipRemove.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleIfConditionEnumName(), RuleIfConditionEnum.thisPermissionDefHasNoEndDateAssignment.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumName(), RuleThenEnum.assignDisabledDaysToOwnerPermissionDefAssignments.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumArg0Name(), "7");

    //should be valid
    String isValidString = attributeValueDelegate.retrieveValueString(
        RuleUtils.ruleValidName());

    if (!StringUtils.equals("T", isValidString)) {
      throw new RuntimeException(isValidString);
    }

```

 

## GSH test cases

 End-to-end GSH transcripts that set up permissions, assign the rule, and show the resulting disabled dates. Timestamps and uuids in the output are from the original test run and will differ in your environment.

 
```java
gsh 0% grouperSession = GrouperSession.startRootSession();
edu.internet2.middleware.grouper.GrouperSession: 1e2e6443d9d34012b66f8d970ec16a1b,'GrouperSystem','application'

//permissions definition
gsh 1% permissionDef = new AttributeDefSave(grouperSession).assignName("stem:permissionDef").assignCreateParentStemsIfNotExist(true).assignAttributeDefType(AttributeDefType.perm).save();
edu.internet2.middleware.grouper.attr.AttributeDef: AttributeDef[name=stem:permissionDef,uuid=65b85a8e4bf74780bec99c04e508853e]
gsh 2% permissionDef.setAssignToEffMembership(true);
gsh 3% permissionDef.setAssignToGroup(true);
gsh 4% permissionDef.store();

//employee group which users must be in
gsh 5% groupEmployee = new GroupSave(grouperSession).assignName("stem:employee").assignCreateParentStemsIfNotExist(true).save();
group: name='stem:employee' displayName='stem:employee' uuid='61581214aef04fd589a5a24338067021'

//roles for permissions
gsh 6% payrollUser = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollUser").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
group: name='apps:payroll:roles:payrollUser' displayName='apps:payroll:roles:payrollUser' uuid='fc738d64b8eb46a1ab78d2e03961129b'
gsh 7% payrollGuest = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollGuest").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
group: name='apps:payroll:roles:payrollGuest' displayName='apps:payroll:roles:payrollGuest' uuid='ac4b956bd8b04e12ac8cc661e104493c'

gsh 8% subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
subject: id='test.subject.0' type='person' source='jdbc' name='my name is test.subject.0'
gsh 9% subject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
subject: id='test.subject.1' type='person' source='jdbc' name='my name is test.subject.1'
gsh 10% subject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
subject: id='test.subject.2' type='person' source='jdbc' name='my name is test.subject.2'

gsh 11% payrollUser.addMember(subject0, false);
true
gsh 12% payrollGuest.addMember(subject1, false);
true

//permission resource
gsh 13% canLogin = new AttributeDefNameSave(grouperSession, permissionDef).assignName("apps:payroll:permissions:canLogin").assignCreateParentStemsIfNotExist(true).save();
edu.internet2.middleware.grouper.attr.AttributeDefName: AttributeDefName[name=apps:payroll:permissions:canLogin,uuid=f9a2001a66c3427287ae2846cd606dd0]

//assign the permission to a role
gsh 14% payrollUser.getPermissionRoleDelegate().assignRolePermission(canLogin);
edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult: edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult@f8f104

//assign the permission directly to a user in a different role
gsh 15% payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject1);
edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult: edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult@1c624e2

gsh 16% member0 = MemberFinder.findBySubject(grouperSession, subject0, false);
member: id='test.subject.0' type='person' source='jdbc' uuid='c24d4c437d0e439397a66659ee36e548'
gsh 17% member1 = MemberFinder.findBySubject(grouperSession, subject1, false);
member: id='test.subject.1' type='person' source='jdbc' uuid='df1329086d6a4ae9aea3d6c9777c68d5'

//permission that user0 has
gsh 18% permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member0.getUuid());
edu.internet2.middleware.grouper.permissions.PermissionEntry: PermissionEntry[roleName=apps:payroll:roles:payrollUser,attributeDefNameName=apps:payroll:permissions:canLogin,action=assign,sourceId=jdbc,subjectId=test.subject.0,imm_mem=true,imm_perm=true,mem_depth=0,role_depth=0,action_depth=0,attrDef_depth=0,perm_type=role]
gsh 19% permissions.size()
1
gsh 20% permissions.iterator().next().getAttributeDefNameName()
apps:payroll:permissions:canLogin

//permission that user1 has
gsh 21% permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member1.getUuid());
edu.internet2.middleware.grouper.permissions.PermissionEntry: PermissionEntry[roleName=apps:payroll:roles:payrollGuest,attributeDefNameName=apps:payroll:permissions:canLogin,action=assign,sourceId=jdbc,subjectId=test.subject.1,imm_mem=true,imm_perm=true,mem_depth=0,role_depth=-1,action_depth=0,attrDef_depth=0,perm_type=role_subject]
gsh 22% permissions.size()
1
gsh 23% permissions.iterator().next().getAttributeDefNameName()
apps:payroll:permissions:canLogin

//assign the rule
gsh 24% RuleApi.permissionGroupIntersection(SubjectFinder.findRootSubject(), permissionDef, groupEmployee, 7);

//add users to employee role
gsh 25% groupEmployee.addMember(subject0);
gsh 26% groupEmployee.addMember(subject1);

//subject2 has no permissions, so this is a no-op
gsh 27% groupEmployee.addMember(subject2);
gsh 28% groupEmployee.deleteMember(subject2);

//this should set some delete dates
gsh 29% groupEmployee.deleteMember(subject0);
gsh 30%  membership = ((Group)payrollUser).getImmediateMembership(Group.getDefaultList(), member0, true, true);
edu.internet2.middleware.grouper.Membership: Membership[createTime=1283882925393,creatorUuid=b0ad34466f1f401ba33c49cba4197cdb,depth=0,listName=members,listType=list,memberUuid=c24d4c437d0e439397a66659ee36e548,groupId=fc738d64b8eb46a1ab78d2e03961129b,type=immediate,uuid=40a51c707a4048f48fca32bd9d0f52c7:9a7d5f4525fe4bbab7bd89d96abd91f9]
gsh 31% membership.getDisabledTime()
java.sql.Timestamp: 2010-09-14 14:10:55.171
gsh 35% new java.sql.Timestamp(System.currentTimeMillis());
java.sql.Timestamp: 2010-09-07 14:11:53.141

gsh 36% permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member0.getUuid());
edu.internet2.middleware.grouper.permissions.PermissionEntry: PermissionEntry[roleName=apps:payroll:roles:payrollUser,attributeDefNameName=apps:payroll:permissions:canLogin,action=assign,sourceId=jdbc,subjectId=test.subject.0,imm_mem=true,imm_perm=true,mem_depth=0,role_depth=0,action_depth=0,attrDef_depth=0,perm_type=role]
gsh 37% permissions.size()
1
gsh 38% permissions.iterator().next().getAttributeDefNameName()
apps:payroll:permissions:canLogin
gsh 39% GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member1.getUuid()).size()
1

//delete subject1 from employee group
gsh 40% groupEmployee.deleteMember(subject1);

//this causes the delete date to be applied to the permission assignment to that user, not to the role assignment
gsh 41% payrollGuest.hasMember(subject1)
true
gsh 42% membership = ((Group)payrollGuest).getImmediateMembership(Group.getDefaultList(), member1, true, true);
edu.internet2.middleware.grouper.Membership: Membership[createTime=1283882930256,creatorUuid=b0ad34466f1f401ba33c49cba4197cdb,depth=0,listName=members,listType=list,memberUuid=df1329086d6a4ae9aea3d6c9777c68d5,groupId=ac4b956bd8b04e12ac8cc661e104493c,type=immediate,uuid=8f27f66ad3884c9a89f686a096a43342:3660b4d6c0894c938d24bc7ba2f9d5a7]
gsh 44% membership.getDisabledTime() == null
true
gsh 45% permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member1.getUuid());
edu.internet2.middleware.grouper.permissions.PermissionEntry: PermissionEntry[roleName=apps:payroll:roles:payrollGuest,attributeDefNameName=apps:payroll:permissions:canLogin,action=assign,sourceId=jdbc,subjectId=test.subject.1,imm_mem=true,imm_perm=true,mem_depth=0,role_depth=-1,action_depth=0,attrDef_depth=0,perm_type=role_subject]
gsh 46% permissions.size()
1
gsh 47% permissions.iterator().next().getAttributeDefNameName()
apps:payroll:permissions:canLogin
gsh 48% permissions.iterator().next().getDisabledTime()
java.sql.Timestamp: 2010-09-14 14:12:45.946

```

 
```java
gsh 0% grouperSession = GrouperSession.startRootSession();
edu.internet2.middleware.grouper.GrouperSession: b7dabce2a6f940128760c2a93a60b40b,'GrouperSystem','application'
gsh 1% permissionDef = new AttributeDefSave(grouperSession).assignName("stem:permissionDef").assignCreateParentStemsIfNotExist(true).assignAttributeDefType(AttributeDefType.perm).save();
edu.internet2.middleware.grouper.attr.AttributeDef: AttributeDef[name=stem:permissionDef,uuid=73fedb4157d34cf7a7a659f4f8ef3ef2]
gsh 2% permissionDef.setAssignToEffMembership(true);
gsh 3% permissionDef.setAssignToGroup(true);
gsh 4% permissionDef.store();

//employee that subjects must be in
gsh 5% groupEmployee = new GroupSave(grouperSession).assignName("stem:employee").assignCreateParentStemsIfNotExist(true).save();
group: name='stem:employee' displayName='stem:employee' uuid='0c534753d63f43ae9c91430b14f7f79d'

//user has permissions in role
gsh 6% payrollUser = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollUser").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
group: name='apps:payroll:roles:payrollUser' displayName='apps:payroll:roles:payrollUser' uuid='18b530bf87ca4b419c9e4145b3b5d510'

//guest has no permissions, and must be assigned per user
gsh 7% payrollGuest = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollGuest").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
group: name='apps:payroll:roles:payrollGuest' displayName='apps:payroll:roles:payrollGuest' uuid='d9c8e5231e41442797b129a2c4c60500'

//subject0 and 1 need permissions end dates, for role, and direct permissions
gsh 8% subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
subject: id='test.subject.0' type='person' source='jdbc' name='my name is test.subject.0'
gsh 9% subject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
subject: id='test.subject.1' type='person' source='jdbc' name='my name is test.subject.1'

//subject3 and 4 are employees, dont edit the date of these
gsh 10% subject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
subject: id='test.subject.2' type='person' source='jdbc' name='my name is test.subject.2'
gsh 11% subject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
subject: id='test.subject.3' type='person' source='jdbc' name='my name is test.subject.3'
gsh 12% subject4 = SubjectFinder.findByIdAndSource("test.subject.4", "jdbc", true);
subject: id='test.subject.4' type='person' source='jdbc' name='my name is test.subject.4'

//subject5 and 6 already have delete dates, so dont edit these, even though not employees
gsh 13% subject5 = SubjectFinder.findByIdAndSource("test.subject.5", "jdbc", true);
subject: id='test.subject.5' type='person' source='jdbc' name='my name is test.subject.5'
gsh 14% subject6 = SubjectFinder.findByIdAndSource("test.subject.6", "jdbc", true);
subject: id='test.subject.6' type='person' source='jdbc' name='my name is test.subject.6'

//all users need a role to get permissions
gsh 15% payrollUser.addMember(subject0, false);
true
gsh 16% payrollGuest.addMember(subject1, false);
true
gsh 17% payrollUser.addMember(subject3, false);
true
gsh 18% payrollGuest.addMember(subject4, false);
true
gsh 19% payrollUser.addMember(subject5, false);
true
gsh 20% payrollGuest.addMember(subject6, false);
true

//permission resource
gsh 21% canLogin = new AttributeDefNameSave(grouperSession, permissionDef).assignName("apps:payroll:permissions:canLogin").assignCreateParentStemsIfNotExist(true).save();
edu.internet2.middleware.grouper.attr.AttributeDefName: AttributeDefName[name=apps:payroll:permissions:canLogin,uuid=208ad5241a6241179d110d8818930411]

//user has this resource
gsh 22% payrollUser.getPermissionRoleDelegate().assignRolePermission(canLogin);
edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult: edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult@8920dc

//guest needs individual assignments
gsh 23% payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject1);
edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult: edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult@af2a50
gsh 24% payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject4);
edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult: edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult@91a0c3

//for subject6, add assignment with end date
gsh 25% attributeAssign = payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject6).getAttributeAssign();
edu.internet2.middleware.grouper.attr.assign.AttributeAssign: AttributeAssign[id=8cce0e3c2b5b4e3da77b21594fd5d932,action=assign,attributeDefName=apps:payroll:permissions:canLogin,
  group=Group[name=apps:payroll:roles:payrollGuest,uuid=d9c8e5231e41442797b129a2c4c60500],
  subjectId='test.subject.6'/'person'/'jdbc']
gsh 27% attributeAssign.setDisabledTime(new java.sql.Timestamp(System.currentTimeMillis() + (3 * 24 * 60 * 60 * 1000)));
gsh 28% attributeAssign.saveOrUpdate();

gsh 29% member0 = MemberFinder.findBySubject(grouperSession, subject0, false);
member: id='test.subject.0' type='person' source='jdbc' uuid='0ae163cbea4648d5b0c76bef232af200'
gsh 30% member1 = MemberFinder.findBySubject(grouperSession, subject1, false);
member: id='test.subject.1' type='person' source='jdbc' uuid='f134f1544b9843528c2a14d98cb36071'
gsh 31% member2 = MemberFinder.findBySubject(grouperSession, subject2, true);
member: id='test.subject.2' type='person' source='jdbc' uuid='2715a01a4c6d4b548938c745a129ae2e'
gsh 32% member3 = MemberFinder.findBySubject(grouperSession, subject3, false);
member: id='test.subject.3' type='person' source='jdbc' uuid='fcccb4aab2f14b2487dee145fbcd6b9f'
gsh 33% member4 = MemberFinder.findBySubject(grouperSession, subject4, false);
member: id='test.subject.4' type='person' source='jdbc' uuid='7947dd875ce64dd09da5ca297d6f84c1'
gsh 34% member5 = MemberFinder.findBySubject(grouperSession, subject5, false);
member: id='test.subject.5' type='person' source='jdbc' uuid='262dde39e72749b796cac885ab48d87c'
gsh 35% member6 = MemberFinder.findBySubject(grouperSession, subject6, false);
member: id='test.subject.6' type='person' source='jdbc' uuid='2279d0c547a04a3486f54598e2386059'

//subject5 has an end date on the role membership
gsh 36% membership = ((Group)payrollUser).getImmediateMembership(Group.getDefaultList(), member5, true, true);
edu.internet2.middleware.grouper.Membership: Membership[createTime=1285560929805,creatorUuid=7a06fd612353403dafe003630a5205a7,depth=0,listName=members,listType=list,memberUuid=262dde39e72749b796cac885ab48d87c,groupId=18b530bf87ca4b419c9e4145b3b5d510,type=immediate,uuid=40457f55bfd04ac1b3262d2c1b43d3c1:c19e178c7b4945cb942fcd17da3219bb]
gsh 39% membership.setDisabledTime(new java.sql.Timestamp(System.currentTimeMillis() + (3 * 24 * 60 * 60 * 1000)));
gsh 40% GrouperDAOFactory.getFactory().getMembership().update(membership);

//currently all the permissions are there
gsh 41% permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member0.getUuid());
edu.internet2.middleware.grouper.permissions.PermissionEntry: PermissionEntry[roleName=apps:payroll:roles:payrollUser,attributeDefNameName=apps:payroll:permissions:canLogin,action=assign,sourceId=jdbc,subjectId=test.subject.0,imm_mem=true,imm_perm=true,mem_depth=0,role_depth=0,action_depth=0,attrDef_depth=0,perm_type=role]
gsh 42% permissions.size()
1
gsh 43% permissions.iterator().next().getAttributeDefNameName()
apps:payroll:permissions:canLogin
gsh 44% permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member1.getUuid());
edu.internet2.middleware.grouper.permissions.PermissionEntry: PermissionEntry[roleName=apps:payroll:roles:payrollGuest,attributeDefNameName=apps:payroll:permissions:canLogin,action=assign,sourceId=jdbc,subjectId=test.subject.1,imm_mem=true,imm_perm=true,mem_depth=0,role_depth=-1,action_depth=0,attrDef_depth=0,perm_type=role_subject]
gsh 45% permissions.size()
1
gsh 46% permissions.iterator().next().getAttributeDefNameName()
apps:payroll:permissions:canLogin

//add the rule
gsh 47% RuleApi.permissionGroupIntersection(SubjectFinder.findRootSubject(), permissionDef, groupEmployee, 7);
edu.internet2.middleware.grouper.attr.assign.AttributeAssign: AttributeAssign[id=2943cc4d8a844845963e5be5ba15b1df,action=assign,attributeDefName=etc:attribute:rules:rule,
  attributeDef=AttributeDef[name=stem:permissionDef,uuid=73fedb4157d34cf7a7a659f4f8ef3ef2]]

//these are employees
gsh 48% groupEmployee.addMember(subject2);
gsh 49% groupEmployee.addMember(subject3);
gsh 50% groupEmployee.addMember(subject4);

//member5 has an end date, shouldnt be changed
gsh 51% membership = ((Group)payrollUser).getImmediateMembership(Group.getDefaultList(), member5, true, true);
edu.internet2.middleware.grouper.Membership: Membership[createTime=1285560929805,creatorUuid=7a06fd612353403dafe003630a5205a7,depth=0,listName=members,listType=list,memberUuid=262dde39e72749b796cac885ab48d87c,groupId=18b530bf87ca4b419c9e4145b3b5d510,type=immediate,uuid=40457f55bfd04ac1b3262d2c1b43d3c1:c19e178c7b4945cb942fcd17da3219bb]
gsh 54% membership.setDisabledTime(new java.sql.Timestamp(System.currentTimeMillis() + (3 * 24 * 60 * 60 * 1000)));
gsh 55% GrouperDAOFactory.getFactory().getMembership().update(membership);

//run the daemon
gsh 56% status = GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);
loader ran successfully: Ran rules daemon, changed 0 records

//subject0 gets an end date in 7 days
gsh 57% membership = ((Group)payrollUser).getImmediateMembership(Group.getDefaultList(), member0, true, true);
edu.internet2.middleware.grouper.Membership: Membership[createTime=1285560907707,creatorUuid=7a06fd612353403dafe003630a5205a7,depth=0,listName=members,listType=list,memberUuid=0ae163cbea4648d5b0c76bef232af200,groupId=18b530bf87ca4b419c9e4145b3b5d510,type=immediate,uuid=6f32bf25d4ab4a5fa519cc2492d8b6b9:c19e178c7b4945cb942fcd17da3219bb]
gsh 58% membership.getDisabledTime()
java.sql.Timestamp: 2010-10-04 00:19:15.237

//member3 shouldnt get an end date
gsh 59% membership = ((Group)payrollUser).getImmediateMembership(Group.getDefaultList(), member3, true, true);
edu.internet2.middleware.grouper.Membership: Membership[createTime=1285560917319,creatorUuid=7a06fd612353403dafe003630a5205a7,depth=0,listName=members,listType=list,memberUuid=fcccb4aab2f14b2487dee145fbcd6b9f,groupId=18b530bf87ca4b419c9e4145b3b5d510,type=immediate,uuid=c93b4f495e054dde8a4a590b22ccf10d:c19e178c7b4945cb942fcd17da3219bb]
gsh 60% membership.getDisabledTime()

//member5's end date should not be edited
gsh 61% membership = ((Group)payrollUser).getImmediateMembership(Group.getDefaultList(), member5, true, true);
edu.internet2.middleware.grouper.Membership: Membership[createTime=1285560929805,creatorUuid=7a06fd612353403dafe003630a5205a7,depth=0,listName=members,listType=list,memberUuid=262dde39e72749b796cac885ab48d87c,groupId=18b530bf87ca4b419c9e4145b3b5d510,type=immediate,uuid=40457f55bfd04ac1b3262d2c1b43d3c1:c19e178c7b4945cb942fcd17da3219bb]
gsh 62% membership.getDisabledTime()
java.sql.Timestamp: 2010-09-30 00:19:01.783

//subject3 should not have an end date
gsh 64% permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member3.getUuid());
edu.internet2.middleware.grouper.permissions.PermissionEntry: PermissionEntry[roleName=apps:payroll:roles:payrollUser,attributeDefNameName=apps:payroll:permissions:canLogin,action=assign,sourceId=jdbc,subjectId=test.subject.3,imm_mem=true,imm_perm=true,mem_depth=0,role_depth=0,action_depth=0,attrDef_depth=0,perm_type=role]
gsh 65% permissions.size()
1
gsh 66% permissions.iterator().next().getAttributeDefNameName()
apps:payroll:permissions:canLogin

//member4 should not have an end date
gsh 67% permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member4.getUuid());
edu.internet2.middleware.grouper.permissions.PermissionEntry: PermissionEntry[roleName=apps:payroll:roles:payrollGuest,attributeDefNameName=apps:payroll:permissions:canLogin,action=assign,sourceId=jdbc,subjectId=test.subject.4,imm_mem=true,imm_perm=true,mem_depth=0,role_depth=-1,action_depth=0,attrDef_depth=0,perm_type=role_subject]
gsh 68% permissions.size()
1
gsh 69% permissions.iterator().next().getAttributeDefNameName()
apps:payroll:permissions:canLogin

//all roles still have all users
gsh 70% payrollUser.hasMember(subject0)
true
gsh 71% payrollGuest.hasMember(subject1)
true
gsh 72% payrollUser.hasMember(subject3)
true
gsh 73% payrollGuest.hasMember(subject4)
true
gsh 74% payrollUser.hasMember(subject5)
true
gsh 75% payrollGuest.hasMember(subject6)
true

//subject0 and 1 have 7 day end dates
gsh 76% membership = ((Group)payrollGuest).getImmediateMembership(Group.getDefaultList(), member1, true, true);
edu.internet2.middleware.grouper.Membership: Membership[createTime=1285560912318,creatorUuid=7a06fd612353403dafe003630a5205a7,depth=0,listName=members,listType=list,memberUuid=f134f1544b9843528c2a14d98cb36071,groupId=d9c8e5231e41442797b129a2c4c60500,type=immediate,uuid=0fb0b547eadf485a81e99d1f15719093:6e3ee7c24649479c87d7a877ad807f6e]
gsh 77% membership.getDisabledTime()
gsh 78% permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member0.getUuid());
edu.internet2.middleware.grouper.permissions.PermissionEntry: PermissionEntry[roleName=apps:payroll:roles:payrollUser,attributeDefNameName=apps:payroll:permissions:canLogin,action=assign,sourceId=jdbc,subjectId=test.subject.0,imm_mem=true,imm_perm=true,mem_depth=0,role_depth=0,action_depth=0,attrDef_depth=0,perm_type=role,
  imm_mship_disabled=2010-10-04 00:19:15.237]
gsh 79% permissions.size()
1
gsh 80% permissions.iterator().next().getImmediateMshipDisabledTime()
java.sql.Timestamp: 2010-10-04 00:19:15.237
gsh 81% permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member1.getUuid());
edu.internet2.middleware.grouper.permissions.PermissionEntry: PermissionEntry[roleName=apps:payroll:roles:payrollGuest,attributeDefNameName=apps:payroll:permissions:canLogin,action=assign,sourceId=jdbc,subjectId=test.subject.1,imm_mem=true,imm_perm=true,mem_depth=0,role_depth=-1,action_depth=0,attrDef_depth=0,perm_type=role_subject]
gsh 82% permissions.size()
1
gsh 83% permissions.iterator().next().getAttributeDefNameName()
apps:payroll:permissions:canLogin
gsh 84% permissions.iterator().next().getDisabledTime()
java.sql.Timestamp: 2010-10-04 00:19:15.297

//subject2 has nothing
gsh 85% permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member2.getUuid());
gsh 86% permissions.size()
0

//subject3,4 have no end dates
gsh 87% permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member3.getUuid());
edu.internet2.middleware.grouper.permissions.PermissionEntry: PermissionEntry[roleName=apps:payroll:roles:payrollUser,attributeDefNameName=apps:payroll:permissions:canLogin,action=assign,sourceId=jdbc,subjectId=test.subject.3,imm_mem=true,imm_perm=true,mem_depth=0,role_depth=0,action_depth=0,attrDef_depth=0,perm_type=role]
gsh 88% permissions.size()
1
gsh 89% permissions.iterator().next().getAttributeDefNameName()
apps:payroll:permissions:canLogin
gsh 90% timestamp = permissions.iterator().next().getDisabledTime();
gsh 91% permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member4.getUuid());
edu.internet2.middleware.grouper.permissions.PermissionEntry: PermissionEntry[roleName=apps:payroll:roles:payrollGuest,attributeDefNameName=apps:payroll:permissions:canLogin,action=assign,sourceId=jdbc,subjectId=test.subject.4,imm_mem=true,imm_perm=true,mem_depth=0,role_depth=-1,action_depth=0,attrDef_depth=0,perm_type=role_subject]
gsh 92% permissions.size()
1
gsh 93% permissions.iterator().next().getAttributeDefNameName()
apps:payroll:permissions:canLogin
gsh 94% timestamp = permissions.iterator().next().getDisabledTime();

//subject5 and 6 have their old end dates
gsh 95% permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member5.getUuid());
edu.internet2.middleware.grouper.permissions.PermissionEntry: PermissionEntry[roleName=apps:payroll:roles:payrollUser,attributeDefNameName=apps:payroll:permissions:canLogin,action=assign,sourceId=jdbc,subjectId=test.subject.5,imm_mem=true,imm_perm=true,mem_depth=0,role_depth=0,action_depth=0,attrDef_depth=0,perm_type=role,
  imm_mship_disabled=2010-09-30 00:19:01.783]
gsh 96% permissions.size()
1
gsh 97% permissions.iterator().next().getAttributeDefNameName()
apps:payroll:permissions:canLogin
gsh 98% timestamp = permissions.iterator().next().getImmediateMshipDisabledTime();
java.sql.Timestamp: 2010-09-30 00:19:01.783
gsh 99% permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member6.getUuid());
edu.internet2.middleware.grouper.permissions.PermissionEntry: PermissionEntry[roleName=apps:payroll:roles:payrollGuest,attributeDefNameName=apps:payroll:permissions:canLogin,action=assign,sourceId=jdbc,subjectId=test.subject.6,imm_mem=true,imm_perm=true,mem_depth=0,role_depth=-1,action_depth=0,attrDef_depth=0,perm_type=role_subject]
gsh 100% permissions.size()
1
gsh 101% permissions.iterator().next().getAttributeDefNameName()
apps:payroll:permissions:canLogin
gsh 102% timestamp = permissions.iterator().next().getDisabledTime();
java.sql.Timestamp: 2010-09-30 00:16:24.841
gsh 103%

```
