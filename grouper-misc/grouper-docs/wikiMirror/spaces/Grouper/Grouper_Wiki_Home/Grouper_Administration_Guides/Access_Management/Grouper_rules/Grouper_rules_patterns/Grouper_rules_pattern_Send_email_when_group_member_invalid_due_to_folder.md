---
title: "Grouper rules pattern - Send email when group member invalid due to folder"
space: Grouper
pageId: 28554772
version: 10
lastUpdated: 2026-07-01T05:39:39.866Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554772/Grouper+rules+pattern+-+Send+email+when+group+member+invalid+due+to+folder
---

There is a WWW content editors group. Each of these content editors has access to edit WWW content. The admins of the WWW system want to know when editors are no longer at penn or change orgs. This rule will email the admins when a content editor is removed from a group in the org folder.

Assign this rule to the group that has the membership to be checked. Todo: Change this page so that the rule is assigned to the group not a folder.

### GSH example

```
grouperSession = GrouperSession.startRootSession();
groupWwwContentEditors = GroupFinder.findByName(grouperSession, "penn:isc:communications:www:wwwContentEditors", true);
group9147org = GroupFinder.findByName(grouperSession, "penn:community:employee:org:9147:9147_personorg", true);
stemOrg = StemFinder.findByName(grouperSession, "penn:community:employee:org", true);
subjectMchyzer = SubjectFinder.findByIdentifierAndSource("mchyzer", "pennperson", true);
subjectBwh = SubjectFinder.findByIdentifierAndSource("bwh", "pennperson", true);
subjectRoot = SubjectFinder.findRootSubject();
attributeAssign = stemOrg.getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleActAsSubjectSourceIdName(), subjectRoot.getSourceId());
attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleActAsSubjectIdName(), subjectRoot.getId());
attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleCheckTypeName(), RuleCheckType.membershipRemoveInFolder.name());
attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleCheckStemScopeName(),Stem.Scope.SUB.name());
attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleIfConditionEnumName(), RuleIfConditionEnum.thisGroupHasImmediateEnabledMembership.name());
attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleIfOwnerNameName(), groupWwwContentEditors.getName());
attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleThenEnumName(), RuleThenEnum.sendEmail.name());
attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleThenEnumArg0Name(), "mchyzer@example.com");
attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleThenEnumArg1Name(), "Member in WWW content editors changed orgs");
attributeAssign.getAttributeValueDelegate().assignValue(RuleUtils.ruleThenEnumArg2Name(), "Hello, ${safeSubject.name} was removed from an org group (${groupName}) and is a WWW content editor, might want to see if they should still be a content editor.\n\n${safeSubject.description}\n\nRegards.");

attributeAssign.getAttributeValueDelegate().retrieveValueString(RuleUtils.ruleValidName());
groupWwwContentEditors.addMember(subjectMchyzer);
groupWwwContentEditors.addMember(subjectBwh);
group9147org.deleteMember(subjectMchyzer);
group9147org.deleteMember(subjectBwh);
group9147org.addMember(subjectMchyzer);
group9147org.addMember(subjectBwh);

//NOTE only remove if wasnt already a member
groupWwwContentEditors.deleteMember(subjectMchyzer);
groupWwwContentEditors.deleteMember(subjectBwh);

```

### Rule configuration in attribute editor

### Sample email

```
-----Original Message-----
From: noreply
Sent: Sunday, April 27, 2014 1:41 PM
To: www_content_admins@example.com
Subject: Member in WWW content editors changed orgs

Hello, Chris Hyzer was removed from an org group (penn:community:employee:org:9147:9147_personorg) and is a WWW content editor, might want to see if they should still be a content editor.

Chris Hyzer (mchyzer, 10021368) (active) Staff - Astt And Information Security - Application Architect (also: Alumni)

Regards.

```
