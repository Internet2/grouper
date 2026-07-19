---
title: "Grouper rules pattern - Send email due to disabled date"
space: Grouper
pageId: 28554692
version: 12
lastUpdated: 2026-07-01T05:39:51.868Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554692/Grouper+rules+pattern+-+Send+email+due+to+disabled+date
---

[Grouper rules](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545173/Grouper+rules)

If an entity is going to be disabled from an employee group, send an email to the subject and an admin

Assign this rule to the group that has the membership that is disabled.

# Configure rule for v5+

# Configure rule for v4 and previous

### Java example

```
     //add a rule on stem:a saying if you are about to be out of the group by all paths (flattened), then send an email
    AttributeAssign attributeAssign = groupEmployee
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
   
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(),
        RuleCheckType.membershipDisabledDate.name());

    //min days in advance to look for disabled memberships
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckArg0Name(), "6");

    //max number of days in advance to look for disabled memberships
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckArg1Name(), "8");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumName(), RuleThenEnum.sendEmail.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg0Name(), "a@b.c, ${safeSubject.emailAddress}");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg1Name(), "You will be removed from group: ${groupDisplayExtension} on ${ruleElUtils.formatDate(membershipDisabledTimestamp, 'yyyy/MM/dd')}");
 
    //the to, subject, or body could be text with EL variables, or could be a template.  If template, it is
    //read from the classpath from package: grouperRulesEmailTemplates/theTemplateName.txt
    //or you could configure grouper.properties to keep them in an external folder, not in the classpath
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg2Name(), "Hello ${safeSubject.name},\n\nJust letting you know you will be removed from group ${groupDisplayExtension} on ${ruleElUtils.formatDate(membershipDisabledTimestamp, 'yyyy/MM/dd')} in the central Groups management system.  Please do not respond to this email.\n\nRegards.");
   
    //should be valid
    String isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());
    assertEquals("T", isValidString);

```

### GSH shorthand method

```
 RuleApi.emailOnFlattenedDisabledDate(Subject actAsSubject, 
      Group ruleGroup, Integer daysInFutureDisabledDateMin, 
      Integer daysInFutureDisabledDateMax, 
      String emailToValue, String emailSubjectValue, String emailBodyValue) 
```

### GSH test case

```
gsh 0% grouperSession = GrouperSession.startRootSession();
edu.internet2.middleware.grouper.GrouperSession: a6ca2976e1a74c2295a8e9bb3e14746f,'GrouperSystem','application'

//two groups, one in the other
gsh 1% groupEmployee = new GroupSave(grouperSession).assignName("stem:employee").assignCreateParentStemsIfNotExist(true).save();
group: name='stem:employee' displayName='stem:employee' uuid='788af52ea4a3426f840714cd22227b9c'
gsh 2% groupProgrammer = new GroupSave(grouperSession).assignName("stem:programmer").assignCreateParentStemsIfNotExist(true).save();
group: name='stem:programmer' displayName='stem:programmer' uuid='eb3f81f6cf09426f9a72a655091e8111'
gsh 3% groupEmployee.addMember(groupProgrammer.toSubject());
gsh 4% subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
subject: id='test.subject.0' type='person' source='jdbc' name='my name is test.subject.0'

//add a rule to email on memberships going to expire in one week
gsh 6% RuleApi.emailOnFlattenedDisabledDate(SubjectFinder.findRootSubject(), groupEmployee, 6, 8, "a@b.c, ${safeSubject.emailAddress}", "You will be removed from group: ${groupDisplayExtension} on ${ruleElUtils.formatDate(membershipDisabledTimestamp, 'yyyy/MM/dd')}", "Hello ${safeSubject.name},\n\nJust letting you know you will be removed from group ${groupDisplayExtension} on ${ruleElUtils.formatDate(membershipDisabledTimestamp, 'yyyy/MM/dd')} in the central Groups management system.  Please do not respond to this email.\n\nRegards.");
edu.internet2.middleware.grouper.attr.assign.AttributeAssign: AttributeAssign[id=4341245100cf4c95b30a212586b3b2cd,action=assign,attributeDefName=etc:attribute:rules:rule,
  group=Group[name=stem:employee,uuid=788af52ea4a3426f840714cd22227b9c]]

//no emails yet
gsh 7% initialEmailCount = GrouperEmail.testingEmailCount;
java.lang.Long: 0

gsh 8% groupEmployee.addMember(subject0, false);
true
gsh 10% edu.internet2.middleware.grouper.app.loader.GrouperLoader.runOnceByJobName(grouperSession, "MAINTENANCE__rules");
loader ran successfully: Ran rules daemon, changed 0 records
gsh 11% member0 = MemberFinder.findBySubject(grouperSession, subject0, false);
member: id='test.subject.0' type='person' source='jdbc' uuid='a603033b08d2403185153984bd524c89'

//set the disabled date to 1 week in the future
gsh 12% membership = groupEmployee.getImmediateMembership(Group.getDefaultList(), member0, true, true);
edu.internet2.middleware.grouper.Membership: Membership[createTime=1284179017233,creatorUuid=e568ae12357840198f98fa30d4ed0838,depth=0,listName=members,listType=list,memberUuid=a603033b08d2403185153984bd524c89,groupId=788af52ea4a3426f840714cd22227b9c,type=immediate,uuid=3cc204ff8bf24825b123f2837f65dbaa:baa0f45ed8ca44658f7672b5a013354e]
gsh 14% membership.setDisabledTime(new java.sql.Timestamp(System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000)));
gsh 15% membership.update();

//run the daemon
gsh 17% edu.internet2.middleware.grouper.app.loader.GrouperLoader.runOnceByJobName(grouperSession, "MAINTENANCE__rules");
loader ran successfully: Ran rules daemon, changed 0 records

//one email sent out
gsh 18% GrouperEmail.testingEmailCount
java.lang.Long: 1

//set it 5 days int he future (out of range)
gsh 19% membership.setDisabledTime(new java.sql.Timestamp(System.currentTimeMillis() + (5 * 24 * 60 * 60 * 1000)));
gsh 20% membership.update();
gsh 21% edu.internet2.middleware.grouper.app.loader.GrouperLoader.runOnceByJobName(grouperSession, "MAINTENANCE__rules");
loader ran successfully: Ran rules daemon, changed 0 records

//no more emails
gsh 21% GrouperEmail.testingEmailCount
java.lang.Long: 1

//nine days in the future
gsh 22% membership.setDisabledTime(new java.sql.Timestamp(System.currentTimeMillis() + (9 * 24 * 60 * 60 * 1000)));
gsh 23% membership.update();
gsh 24% edu.internet2.middleware.grouper.app.loader.GrouperLoader.runOnceByJobName(grouperSession, "MAINTENANCE__rules");
loader ran successfully: Ran rules daemon, changed 0 records

//did not email
gsh 25% GrouperEmail.testingEmailCount
java.lang.Long: 1

//7 days again, it emails
gsh 26% membership.setDisabledTime(new java.sql.Timestamp(System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000)));
gsh 27% membership.update();
gsh 28% edu.internet2.middleware.grouper.app.loader.GrouperLoader.runOnceByJobName(grouperSession, "MAINTENANCE__rules");
loader ran successfully: Ran rules daemon, changed 0 records
gsh 29% GrouperEmail.testingEmailCount
java.lang.Long: 2

//if there is a non disabled dated membership by another path, then dont send another email
gsh 30% groupProgrammer.addMember(subject0);
gsh 31% edu.internet2.middleware.grouper.app.loader.GrouperLoader.runOnceByJobName(grouperSession, "MAINTENANCE__rules");
loader ran successfully: Ran rules daemon, changed 0 records
gsh 32% GrouperEmail.testingEmailCount
java.lang.Long: 2
gsh 33%

```

### Example email

```
From: noreply@example.com [mailto:noreply@example.com]

Sent: Friday, September 10, 2010 11:44 PM
To: a@b.c; test.subject.0@example.com
Subject: TEST:You will be removed from group: employee on 2010/09/17

Hello my name is test.subject.0,

Just letting you know you will be removed from group employee on 2010/09/17 in the central Groups management system.  Please do not respond to this email.

Regards.

```

safd
