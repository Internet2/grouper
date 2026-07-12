---
title: "Grouper rules pattern - Inherited privileges on groups"
space: Grouper
pageId: 28555391
version: 21
lastUpdated: 2026-07-01T05:38:18.894Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555391/Grouper+rules+pattern+-+Inherited+privileges+on+groups
---

[Grouper rules](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545173/Grouper+rules)

If a group is created under folder a:b, then apply privileges to the group of READ,UPDATE to group a:security:admins

You should use the inherited privileges screen to control this. This rule is on the folder where groups are created (or ancestor folder).

# Configure rule for v5+

# Configure rule for v4 and previous

### Penn example

Penn has Atlassian groups in Grouper. Any group created in Grouper in the jira/confluence folder will be available in jira/confluence. However, the proper privileges need to be assigned to the groups. The Atlassian admins need admin, updaters need update, and readers need read. These assignments are done by 3 people, and it is error prone (assign the wrong thing), sometimes forgotten, and time consuming.

We assigned rules on the atlassian folder (in test and prod) to automatically make these assignments. Here is the GSH script to install these 6 rules (admin/update/read for test/prod)

```
grouperSession = GrouperSession.startRootSession();
atlassian = StemFinder.findByName(grouperSession, "penn:isc:ait:apps:atlassian")
atlassianReaders = GroupFinder.findByName(grouperSession, "penn:isc:ait:apps:atlassian:admin:readers");
RuleApi.inheritGroupPrivileges(SubjectFinder.findRootSubject(), atlassian, Stem.Scope.SUB, atlassianReaders.toSubject(), Privilege.getInstances("read"));
atlassianAdmins = GroupFinder.findByName(grouperSession, "penn:isc:ait:apps:atlassian:admin:admins");
RuleApi.inheritGroupPrivileges(SubjectFinder.findRootSubject(), atlassian, Stem.Scope.SUB, atlassianAdmins.toSubject(), Privilege.getInstances("admin"));
atlassianUpdaters = GroupFinder.findByName(grouperSession, "penn:isc:ait:apps:atlassian:admin:updaters");
RuleApi.inheritGroupPrivileges(SubjectFinder.findRootSubject(), atlassian, Stem.Scope.SUB, atlassianUpdaters.toSubject(), Privilege.getInstances("update"));
RuleApi.runRulesForOwner(atlassian);

atlassian = StemFinder.findByName(grouperSession, "test:isc:ait:apps:atlassian")
atlassianReaders = GroupFinder.findByName(grouperSession, "test:isc:ait:apps:atlassian:admin:readers");
RuleApi.inheritGroupPrivileges(SubjectFinder.findRootSubject(), atlassian, Stem.Scope.SUB, atlassianReaders.toSubject(), Privilege.getInstances("read"));
atlassianAdmins = GroupFinder.findByName(grouperSession, "test:isc:ait:apps:atlassian:admin:admins");
RuleApi.inheritGroupPrivileges(SubjectFinder.findRootSubject(), atlassian, Stem.Scope.SUB, atlassianAdmins.toSubject(), Privilege.getInstances("admin"));
atlassianUpdaters = GroupFinder.findByName(grouperSession, "test:isc:ait:apps:atlassian:admin:updaters");
RuleApi.inheritGroupPrivileges(SubjectFinder.findRootSubject(), atlassian, Stem.Scope.SUB, atlassianUpdaters.toSubject(), Privilege.getInstances("update"));
RuleApi.runRulesForOwner(atlassian);

```

### Another Penn example

If you want groups and folders in a folder to have admin assigned to a group, and to run the rule initially, do this

```
grouperSession = GrouperSession.startRootSession();
stemToAssign = StemFinder.findByName(grouperSession, "penn:someFolder")
admins = GroupFinder.findByName(grouperSession, "penn:someFolder:security:someFolderAdmins");
RuleApi.inheritGroupPrivileges(SubjectFinder.findRootSubject(), stemToAssign, Stem.Scope.SUB, admins.toSubject(), Privilege.getInstances("admin"));
RuleApi.inheritFolderPrivileges(SubjectFinder.findRootSubject(), stemToAssign, Stem.Scope.SUB, admins.toSubject(), Privilege.getInstances("stem, create"));
RuleApi.runRulesForOwner(stemToAssign);

```

### Java example

```
//add a rule on stem2 saying if you create a group underneath, then assign a reader and updater group
    AttributeAssign attributeAssign = stem2
      .getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();

    AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();

    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckTypeName(), RuleCheckType.groupCreate.name());

    //can be SUB or ONE for if in this folder, or in this and all subfolders
    attributeValueDelegate.assignValue(
        RuleUtils.ruleCheckStemScopeName(), Stem.Scope.SUB.name());
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumName(), RuleThenEnum.assignGroupPrivilegeToGroupId.name());

    //this is the subject string for the subject to assign to
    //e.g. sourceId :::::: subjectIdentifier
    //or sourceId :::: subjectId
    //or :::: subjectId
    //or sourceId ::::::: subjectIdOrIdentifier
    //etc
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumArg0Name(), "g:gsa :::::: stem1:admins");

    //privileges to assign: read, admin, update, view, optin, optout
    attributeValueDelegate.assignValue(
        RuleUtils.ruleThenEnumArg1Name(), "read, update");

    //should be valid
    String isValidString = attributeValueDelegate.retrieveValueString(
        RuleUtils.ruleValidName());

    if (!StringUtils.equals("T", isValidString)) {
      throw new RuntimeException(isValidString);
    }

```

### GSH shorthand method

```
grouperSession = GrouperSession.startRootSession(); 
stem = StemFinder.findByName(grouperSession, "some:stem:name");
group = GroupFinder.findByName(grouperSession, "some:group:name");
RuleApi.inheritGroupPrivileges(SubjectFinder.findRootSubject(), stem, Stem.Scope.SUB, group.toSubject(), Privilege.getInstances("read, update"));
```

### GSH test case

```
gsh 0% grouperSession = GrouperSession.startRootSession();
edu.internet2.middleware.grouper.GrouperSession: 847e80d5c2d94803b02da4ed3c131475,'GrouperSystem','application'
gsh 1% stem2 = new edu.internet2.middleware.grouper.StemSave(grouperSession).assignName("stem2").assignCreateParentStemsIfNotExist(true).save();
stem: name='stem2' displayName='stem2' uuid='7a6ce531c0654141abdebba87d4f7461'
gsh 2% groupA = new GroupSave(grouperSession).assignName("stem1:admins").assignCreateParentStemsIfNotExist(true).save();
group: name='stem1:admins' displayName='stem1:admins' uuid='2d1aee72df264626831cd4bf166f7342'
gsh 4% addMember("stem1:admins", "test.subject.0");
true
gsh 5% subjectActAs = SubjectFinder.findByIdAndSource("GrouperSystem", "g:isa", true);
subject: id='GrouperSystem' type='application' source='g:isa' name='GrouperSysAdmin'
gsh 6% RuleApi.inheritGroupPrivileges(subjectActAs, stem2, Stem.Scope.SUB, groupA.toSubject(), Privilege.getInstances("read, update"));
gsh 7% groupB = new GroupSave(grouperSession).assignName("stem2:b").assignCreateParentStemsIfNotExist(true).save();
group: name='stem2:b' displayName='stem2:b' uuid='ab4d6d959e51439d8b5a583659c18760'
gsh 10% hasPriv("stem2:b", "test.subject.0", Privilege.getInstance("update"))
true
gsh 11% hasPriv("stem2:b", "test.subject.0", Privilege.getInstance("read"))
true
gsh 12% groupD = new GroupSave(grouperSession).assignName("stem3:d").assignCreateParentStemsIfNotExist(true).save();
group: name='stem3:d' displayName='stem3:d' uuid='d309509da52e4ed2bbca8383246fe3c4'
gsh 13% hasPriv("stem3:d", "test.subject.0", Privilege.getInstance("update"))
false
gsh 14% hasPriv("stem3:d", "test.subject.0", Privilege.getInstance("read"))
true
gsh 15% groupC = new GroupSave(grouperSession).assignName("stem2:sub:c").assignCreateParentStemsIfNotExist(true).save();
group: name='stem2:sub:c' displayName='stem2:sub:c' uuid='d52f784d88284b4b90e0931ad8581ebc'
gsh 16% hasPriv("stem2:sub:c", "test.subject.0", Privilege.getInstance("update"))
true
gsh 17% hasPriv("stem2:sub:c", "test.subject.0", Privilege.getInstance("read"))
true

```

### GSH daemon test case

Run the above GSH commands, and continue below

```
gsh 18% revokePriv("stem2:sub:c", "test.subject.0", Privilege.getInstance("update"))
false
gsh 19% status = GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);
loader ran successfully: Ran rules daemon, changed 0 records
gsh 20% hasPriv("stem2:sub:c", "test.subject.0", Privilege.getInstance("update"))
true

```

### Apply rule to certain groups

If you want the rule to only apply to groups with certain names, an admin can apply this condition

```
attributeValueDelegate.assignValue(
    RuleUtils.ruleIfConditionEnumName(), RuleIfConditionEnum.nameMatchesSqlLikeString.name());
attributeValueDelegate.assignValue(
    RuleUtils.ruleIfConditionEnumArg0Name(), "a:b:%someGroup");
```
