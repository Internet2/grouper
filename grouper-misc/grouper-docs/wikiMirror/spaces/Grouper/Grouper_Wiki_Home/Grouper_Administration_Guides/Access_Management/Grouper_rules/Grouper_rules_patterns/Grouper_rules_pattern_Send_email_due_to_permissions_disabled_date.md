---
title: "Grouper rules pattern - Send email due to permissions disabled date"
space: Grouper
pageId: 28554686
version: 9
lastUpdated: 2026-07-01T05:39:52.869Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554686/Grouper+rules+pattern+-+Send+email+due+to+permissions+disabled+date
---

[Grouper rules](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545173/Grouper+rules)

If an entity is going to be disabled from permissions, send an email to the employee and an admin

Assign this rule to the permission definition of the permission that is disabled.

### Java example

```
    //add a rule on the permission definition saying if you are about to lose a permission by all paths (flattened), then send an email
    AttributeAssign attributeAssign = permissionDef
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();

    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), actAsSubject.getSourceId());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), actAsSubject.getId());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(),
        RuleCheckType.permissionDisabledDate.name());

    //will find memberships with a disabled date at least 6 days from now.  blank means no min
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckArg0Name(), daysInFutureDisabledDateMin == null ? null : daysInFutureDisabledDateMin.toString());

    //will find memberships with a disabled date at most 8 days from now.  blank means no max
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckArg1Name(), daysInFutureDisabledDateMax == null ? null : daysInFutureDisabledDateMax.toString());

    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumName(), RuleThenEnum.sendEmail.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg0Name(), emailToValue);
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg1Name(), emailSubjectValue);

    //the to, subject, or body could be text with EL variables, or could be a template.  If template, it is
    //read from the classpath from package: grouperRulesEmailTemplates/theTemplateName.txt
    //or you could configure grouper.properties to keep them in an external folder, not in the classpath
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg2Name(), emailBodyValue);

    //should be valid
    String isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());

    if (!StringUtils.equals("T", isValidString)) {
      throw new RuntimeException(isValidString);
    }

```

### GSH shorthand method

```
RuleApi.emailOnFlattenedPermissionDisabledDate(SubjectFinder.findRootSubject(), permissionDef, 6, 8, GrouperConfig.getProperty("mail.test.address") + ", ${safeSubject.emailAddress}", "You will have this permission unassigned: ${attributeDefNameDisplayExtension} in role ${roleDisplayExtension}, removed on ${ruleElUtils.formatDate(permissionDisabledTimestamp, 'yyyy/MM/dd')}", "Hello ${safeSubject.name},\n\nJust letting you know you will have this permission removed ${attributeDefNameDisplayExtension} in role ${roleDisplayExtension}, removed on ${ruleElUtils.formatDate(permissionDisabledTimestamp, 'yyyy/MM/dd')} in the central Groups / Permissions management system.  Please do not respond to this email.\n\nRegards.");

```

### GSH test case

```
gsh 0% grouperSession = GrouperSession.startRootSession();
edu.internet2.middleware.grouper.GrouperSession: 755a39e6672d4f60bfca6cc5ed065b5d,'GrouperSystem','application'

//permission definition
gsh 1% permissionDef = new AttributeDefSave(grouperSession).assignName("stem:permissionDef").assignCreateParentStemsIfNotExist(true).assignAttributeDefType(AttributeDefType.perm).save();
edu.internet2.middleware.grouper.attr.AttributeDef: AttributeDef[name=stem:permissionDef,uuid=a1522fe8665443538a4f7a7529c5996d]
gsh 2% permissionDef.setAssignToEffMembership(true);
gsh 3% permissionDef.setAssignToGroup(true);
gsh 4% permissionDef.store();

//run daemon once
gsh 6% RuleApi.emailOnFlattenedPermissionDisabledDate(SubjectFinder.findRootSubject(), permissionDef, 6, 8, "a@b.c, ${safeSubject.emailAddress}", "You will have this permission unassigned: ${attributeDefNameDisplayExtension} in role ${roleDisplayExtension}, removed on ${ruleElUtils.formatDate(permissionDisabledTimestamp, 'yyyy/MM/dd')}", "Hello ${safeSubject.name},\n\nJust letting you know you will have this permission removed ${attributeDefNameDisplayExtension} in role ${roleDisplayExtension}, removed on ${ruleElUtils.formatDate(permissionDisabledTimestamp, 'yyyy/MM/dd')} in the central Groups / Permissions management system.  Please do not respond to this email.\n\nRegards.");
edu.internet2.middleware.grouper.attr.assign.AttributeAssign: AttributeAssign[id=01e759e67c424ded95665ddf0ee0f6b6,action=assign,attributeDefName=etc:attribute:rules:rule,
  attributeDef=AttributeDef[name=stem:permissionDef,uuid=a1522fe8665443538a4f7a7529c5996d]]

//hasnt fired yet
gsh 7% GrouperEmail.testingEmailCount
java.lang.Long: 0

//two roles
gsh 8% payrollUser = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollUser").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
group: name='apps:payroll:roles:payrollUser' displayName='apps:payroll:roles:payrollUser' uuid='bd2872af67bc42b3ada16566985854c4'
gsh 9% payrollGuest = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollGuest").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
group: name='apps:payroll:roles:payrollGuest' displayName='apps:payroll:roles:payrollGuest' uuid='104bc36f602f4dce868eba7196fee11b'

//three users
gsh 10% subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
subject: id='test.subject.0' type='person' source='jdbc' name='my name is test.subject.0'
gsh 11% subject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
subject: id='test.subject.1' type='person' source='jdbc' name='my name is test.subject.1'
gsh 12% subject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
subject: id='test.subject.2' type='person' source='jdbc' name='my name is test.subject.2'

//payroll user has the permission
gsh 13% payrollUser.addMember(subject1, false);
true

//payroll guest requires user to have permission explicitly assigned
gsh 14% payrollGuest.addMember(subject0, false);
true
gsh 15% payrollGuest.addMember(subject2, false);
true

//permission resource
gsh 16% canLogin = new AttributeDefNameSave(grouperSession, permissionDef).assignName("apps:payroll:permissions:canLogin").assignCreateParentStemsIfNotExist(true).save();
edu.internet2.middleware.grouper.attr.AttributeDefName: AttributeDefName[name=apps:payroll:permissions:canLogin,uuid=943475dbdcac45efa2335c6a8c399971]

//assign resource to the user role
gsh 17% payrollUser.getPermissionRoleDelegate().assignRolePermission(canLogin);
edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult: edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult@15e601

//assign subject2 directly to permission
gsh 18% payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject2);
edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult: edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult@1a70476

//assign subject0 to permission, but keep assignment to be able to put disabled date on it
gsh 19% attributeAssign = payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject0).getAttributeAssign();
edu.internet2.middleware.grouper.attr.assign.AttributeAssign: AttributeAssign[id=12c472cea0ce471bba0d05acb3ab167a,action=assign,attributeDefName=apps:payroll:permissions:canLogin,
  group=Group[name=apps:payroll:roles:payrollGuest,uuid=104bc36f602f4dce868eba7196fee11b],
  subjectId='test.subject.0'/'person'/'jdbc']

//run daemon, still shouldnt find it.
gsh 20% GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);
loader ran successfully: Ran rules daemon, changed 0 records

gsh 21% GrouperEmail.testingEmailCount
java.lang.Long: 0

//set disabled time of permission to be 7 days in the future
gsh 23% attributeAssign.setDisabledTime(new java.sql.Timestamp(System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000)));
gsh 24% attributeAssign.saveOrUpdate();

//find that record and send an email
gsh 25% GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);
loader ran successfully: Ran rules daemon, changed 0 records
gsh 26% GrouperEmail.testingEmailCount
java.lang.Long: 1

//set 5 days in advance, and it is not between 6 and 8, so it wont find it
gsh 27% attributeAssign.setDisabledTime(new java.sql.Timestamp(System.currentTimeMillis() + (5 * 24 * 60 * 60 * 1000)));
gsh 28% attributeAssign.saveOrUpdate();
gsh 29% GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);
loader ran successfully: Ran rules daemon, changed 0 records

// still one email sent
gsh 30% GrouperEmail.testingEmailCount
java.lang.Long: 1

//set it 9 days in advance
gsh 31% attributeAssign.setDisabledTime(new java.sql.Timestamp(System.currentTimeMillis() + (9 * 24 * 60 * 60 * 1000)));
gsh 32% attributeAssign.saveOrUpdate();
gsh 33% GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);
loader ran successfully: Ran rules daemon, changed 0 records

//out of bounds
gsh 34% GrouperEmail.testingEmailCount
java.lang.Long: 1
gsh 35% attributeAssign.setDisabledTime(new java.sql.Timestamp(System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000)));
gsh 36% attributeAssign.saveOrUpdate();

//run the daemon and find another record
gsh 37% GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);
loader ran successfully: Ran rules daemon, changed 0 records
gsh 38% GrouperEmail.testingEmailCount
java.lang.Long: 2

//add another path without a disabled date, and it should not find it this time
gsh 39% payrollUser.addMember(subject0, false);
true
gsh 40% GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);
loader ran successfully: Ran rules daemon, changed 0 records

//same number, no new emails
gsh 41% GrouperEmail.testingEmailCount
java.lang.Long: 2
gsh 42%

```

dsaf
