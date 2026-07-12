---
title: "Grouper rules pattern - Send email membership add due to folder"
space: Grouper
pageId: 28555606
version: 13
lastUpdated: 2026-07-01T05:37:48.787Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555606/Grouper+rules+pattern+-+Send+email+membership+add+due+to+folder
---

[Grouper rules](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545173/Grouper+rules)

If an entity is flattened added (added by a new path, doesnt have an existing path) to a group in a stem, send an email to the employee and an admin

Assign this rule to the folder that has groups where membership is added.

# Configure rule for v5+

# Configure rule for v4 and previous

### Java example

```
    //add a rule on stem:a saying if you are added to a group in the stem by a new paths (flattened), then send an email
    AttributeAssign attributeAssign = ruleStem
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();

    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(),actAsSubject.getSourceId());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), actAsSubject.getId());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(),
        RuleCheckType.flattenedMembershipAddInFolder.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckStemScopeName(),
        Stem.Scope.SUB.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumName(), RuleThenEnum.sendEmail.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg0Name(), emailToValue);
    //the to, subject, or body could be text with EL variables, or could be a template.  If template, it is
    //read from the classpath from package: grouperRulesEmailTemplates/theTemplateName.txt
    //or you could configure grouper.properties to keep them in an external folder, not in the classpath
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg1Name(), emailSubjectValue);

    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg2Name(), emailBodyValue);

    //should be valid
    String isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());
    if (\!StringUtils.equals("T", isValidString)) {
      throw new RuntimeException(isValidString);
    }

```

### GSH shorthand method

```
RuleApi.emailOnFlattenedMembershipAddFromStem(SubjectFinder.findRootSubject(), stem, Stem.Scope.SUB, "mchyzer@isc.upenn.edu, ${safeSubject.emailAddress}", "template: testEmailGroupSubjectFlattenedAddInFolder", "Hello ${safeSubject.name},\n\nJust letting you know you were removed from group ${groupDisplayExtension} in the central Groups management system.&nbsp; Please do not respond to this email.\n\nRegards.");

```

### GSH test case

```
gsh 0% grouperSession = GrouperSession.startRootSession();
edu.internet2.middleware.grouper.GrouperSession: 6fe6c4ab74fb4f61b1a9d0d6aefeb6f3,'GrouperSystem','application'

//this is the parent group

gsh 1% groupEmployee = new GroupSave(grouperSession).assignName("stem:employee").assignCreateParentStemsIfNotExist(true).save();
group: name='stem:employee' displayName='stem:employee' uuid='0f4cf36b7fde40dfb8c23e87115c3ac9'

//two child groups

gsh 2% groupProgrammer = new GroupSave(grouperSession).assignName("stem:programmer").assignCreateParentStemsIfNotExist(true).save();
group: name='stem:programmer' displayName='stem:programmer' uuid='d74baa8c2ddd4210aa63e10cb87ebc65'
gsh 3% groupResearcher = new GroupSave(grouperSession).assignName("stem:researcher").assignCreateParentStemsIfNotExist(true).save();
group: name='stem:researcher' displayName='stem:researcher' uuid='c5d399ef395a496180e7d8e689a58fc5'
gsh 4% groupEmployee.addMember(groupProgrammer.toSubject());
gsh 5% groupEmployee.addMember(groupResearcher.toSubject());

//stem of groups
gsh 6% stem = StemFinder.findByName(grouperSession, "stem", true);
stem: name='stem' displayName='stem' uuid='5757e8b804c84152ad7df876d5daf3f4'
gsh 7% subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
subject: id='test.subject.0' type='person' source='jdbc' name='my name is test.subject.0'
//add the rule
gsh 8% RuleApi.emailOnFlattenedMembershipAddFromStem(SubjectFinder.findRootSubject(), stem, Stem.Scope.SUB, "a@b.c, ${safeSubject.emailAddress}", "template: testEmailGroupSubjectFlattenedAddInFolder", "Hello ${safeSubject.name},\n\nJust letting you know you were removed from group ${groupDisplayExtension} in the central Groups management system.&nbsp; Please do not respond to this email.\n\nRegards.");
//run the change log daemon (if not running in background and wait a minute)
gsh 10% edu.internet2.middleware.grouper.app.loader.GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
loader ran successfully: Ran the changeLogTempToChangeLog daemon
//run the rules change log daemon (if not running in background and wait a minute)
gsh 11% edu.internet2.middleware.grouper.app.loader.GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");
loader ran successfully: null
//add someone to the child group
gsh 12% groupProgrammer.addMember(subject0, false);
true
gsh 13%&nbsp; edu.internet2.middleware.grouper.app.loader.GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
loader ran successfully: Ran the changeLogTempToChangeLog daemon
//this will trigger two emails, one for the child group, one for the parent group
gsh 14% edu.internet2.middleware.grouper.app.loader.GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");
loader ran successfully: null
gsh 15% GrouperEmail.testingEmailCount
java.lang.Long: 2
//add to the other child group
gsh 16% groupResearcher.addMember(subject0, false);
true
gsh 17% edu.internet2.middleware.grouper.app.loader.GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
loader ran successfully: Ran the changeLogTempToChangeLog daemon
gsh 18% edu.internet2.middleware.grouper.app.loader.GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");
loader ran successfully: null
//note, there is only one flat add since already a member of the parent, send one more email
gsh 19% GrouperEmail.testingEmailCount
java.lang.Long: 3
//if you add to the parent, then no additional emails aill go out
gsh 20% groupEmployee.addMember(subject0);
gsh 21% edu.internet2.middleware.grouper.app.loader.GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
loader ran successfully: Ran the changeLogTempToChangeLog daemon
gsh 22% edu.internet2.middleware.grouper.app.loader.GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");
loader ran successfully: null
gsh 23% GrouperEmail.testingEmailCount
java.lang.Long: 3
gsh 24%

```

An example email looks like this (based on configuration and optional templates):

```
From: whatever@configured.grouper.properties
To: a@b.c, test.subject.0@somewhere.someSchool.edu
Subject: You will be removed from group: employee

Body:
Hello my name is test.subject.0,

Just letting you know you were removed from group employee in the central Groups management system.  Please do not respond to this email.

Regards.

```

### GSH daemon

There is no daemon for this email rule

sdf
