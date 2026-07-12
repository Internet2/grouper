---
title: "Grouper rules pattern - Add disabled date on membership"
space: Grouper
pageId: 28554784
version: 12
lastUpdated: 2026-07-01T05:39:37.894Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554784/Grouper+rules+pattern+-+Add+disabled+date+on+membership
---

[Grouper rules](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545173/Grouper+rules)

For reprieve groups (e.g. for status or training or two step), when added to group, maybe you want an automatic 7 day in future end date on membership

Add this rule to a group where the membership is added and should have disabled date.

# Configure rule for v5+

# Configure rule for v4 and previous

### GSH example to assign and test. Set the group name and days to expire at the top

Note: the end of the script adds GrouperSystem to the group, sees the expire date, and then removed GrouperSystem. If you dont want to do that, then remove that part starting at group.addMember().

```
String groupName = "penn:apps:ngss:knowledgeLink:prod:ngssProdHasTakenTrainingReprieve";
String expireAfterDays = "7";
grouperSession = GrouperSession.startRootSession();
group = GroupFinder.findByName(grouperSession, groupName);
AttributeAssign attributeAssign = group.getAttributeDelegate().addAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
AttributeValueDelegate attributeValueDelegate = attributeAssign.getAttributeValueDelegate();
attributeValueDelegate.assignValue(RuleUtils.ruleActAsSubjectSourceIdName(), SubjectFinder.findRootSubject().getSourceId());
attributeValueDelegate.assignValue(RuleUtils.ruleActAsSubjectIdName(), SubjectFinder.findRootSubject().getId());
attributeValueDelegate.assignValue(RuleUtils.ruleCheckTypeName(),RuleCheckType.membershipAdd.name());
attributeValueDelegate.assignValue(RuleUtils.ruleThenEnumName(), RuleThenEnum.assignMembershipDisabledDaysForOwnerGroupId.name());
attributeValueDelegate.assignValue(RuleUtils.ruleThenEnumArg0Name(), expireAfterDays);
attributeValueDelegate.assignValue(RuleUtils.ruleThenEnumArg1Name(), "F");
attributeValueDelegate.assignValue(RuleUtils.ruleValidName(), "T");
String isValidString = attributeValueDelegate.retrieveValueString(RuleUtils.ruleValidName());
if (!"T".equals(isValidString)) {throw new RuntimeException("Not valid! " + isValidString);}
group.addMember(SubjectFinder.findRootSubject());
Member member = MemberFinder.findBySubject(grouperSession, SubjectFinder.findRootSubject(), true);
Membership membership = group.getImmediateMembership(Group.getDefaultList(), member, true, true);
java.sql.Timestamp disabledTime = membership.getDisabledTime();
group.deleteMember(SubjectFinder.findRootSubject());
if (disabledTime == null) {throw new RuntimeException("Rule didnt set the disabled time");}

```
