---
title: "Grouper rules pattern - Send email after membership remove"
space: Grouper
pageId: 28554715
version: 14
lastUpdated: 2026-07-01T05:39:48.824Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554715/Grouper+rules+pattern+-+Send+email+after+membership+remove
---

[Grouper rules](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545173/Grouper+rules)

If an entity is no longer a member of the employee group, send an email to the employee and an admin

Assign this rule to the group that has the membership to be removed.

# Configure rule for v5+

# Configure rule for v4 and previous

### Java example

```
    //add a rule on stem:a saying if you are out of the group by all paths (flattened), then send an email
    AttributeAssign attributeAssign = ruleGroup
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();

    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), actAsSubject.getSourceId());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), actAsSubject.getId());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(),
        RuleCheckType.flattenedMembershipRemove.name());
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
RuleApi.emailOnFlattenedMembershipRemove(SubjectFinder.findRootSubject(), groupEmployee, "mchyzer@example.com, ${safeSubject.emailAddress}", "You will be removed from group: ${groupDisplayExtension}", "template: testEmailGroupBodyFlattenedRemove");

```

### GSH test case

```
gsh 0% grouperSession = GrouperSession.startRootSession();
edu.internet2.middleware.grouper.GrouperSession: 7e02ec4d078c4edcac5ba92f257c3eeb,'GrouperSystem','application'
gsh 1% groupEmployee = new GroupSave(grouperSession).assignName("stem:employee").assignCreateParentStemsIfNotExist(true).save();
group: name='stem:employee' displayName='stem:employee' uuid='b336b02622ff421394de1c70e6f4fc12'
gsh 2% groupProgrammer = new GroupSave(grouperSession).assignName("stem:programmer").assignCreateParentStemsIfNotExist(true).save();
group: name='stem:programmer' displayName='stem:programmer' uuid='d14cdd6d96cd4822b74b7dfae89ba2da'
gsh 3% groupResearcher = new GroupSave(grouperSession).assignName("stem:researcher").assignCreateParentStemsIfNotExist(true).save();
group: name='stem:researcher' displayName='stem:researcher' uuid='8d140e4054f34cf08c1ca1ce7ef43f3c'
gsh 4% groupEmployee.addMember(groupProgrammer.toSubject());
gsh 5% groupEmployee.addMember(groupResearcher.toSubject());
gsh 6% subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
subject: id='test.subject.0' type='person' source='jdbc' name='my name is test.subject.0'
gsh 7% groupProgrammer.addMember(subject0, false);
true
gsh 8% groupResearcher.addMember(subject0, false);
true
gsh 9% RuleApi.emailOnFlattenedMembershipRemove(SubjectFinder.findRootSubject(), groupEmployee, "a@example.com, ${safeSubject.emailAddress}", "You will be removed from group: ${groupDisplayExtension}", "template: testEmailGroupBodyFlattenedRemove");
gsh 10% groupProgrammer.deleteMember(subject0);
gsh 18% edu.internet2.middleware.grouper.app.loader.GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
gsh 19%  edu.internet2.middleware.grouper.app.loader.GrouperLoader.GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");
gsh 21% groupResearcher.deleteMember(subject0);
gsh 22% edu.internet2.middleware.grouper.app.loader.GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
gsh 23%  edu.internet2.middleware.grouper.app.loader.GrouperLoader.GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");

```

### GSH daemon

There is no daemon for this email rule
