---
title: "Grouper rules EL variables"
space: Grouper
pageId: 28549057
version: 9
lastUpdated: 2026-11-23T07:26:36.729Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549057/Grouper+rules+EL+variables
---

Expression language (EL) substitutions using variables combined with a JEXL template can be used on either a custom If condition ("Expression language"), or in a Send Email action, in the To addresses, subject, and body. Some variables are available for all rule types. But extra variables will differ depending on the rule check type. As of v7.9.1, v6.5.1 and v4.26.1, a user who is not a Grouper sysadmin (wheel or root) can save a send email rule only if every ${...} in the To addresses, subject and body is one of the predefined variables, e.g. ${groupName}, ${groupDisplayName}, ${groupDisplayExtension}, ${groupDescription}, ${stemName}, ${memberId}, the membership dates, ${safeSubject.name}, ${safeSubject.emailAddress} (and the other safeSubject getters below), or ${safeSubject.getAttributeValue('name')}. Other expressions are refused on save; a sysadmin can still save them.

If logging for edu.internet2.middleware.grouper.rules.RuleEngine, or just the rules package, is set to DEBUG, and grouper.properties value "rules.attributeAssignTypeIdsToLog" includes the id string of a specific rule's marker attribute, it will log the EL key/value pairs as part of it's debugging.

```xml
        <Logger name="edu.internet2.middleware.grouper.rules" level="debug" additivity="false">
            __LOGPIPESTART__<AppenderRef ref="logpipe_grouper_provisioning"/>__LOGPIPEEND__
            __FILESTART__<AppenderRef ref="file_grouper_provisioning"/>__FILEEND__
            __STDERRSTART__<AppenderRef ref="stderr"/>__STDERREND__
        </Logger>
```

## Extended EL API

As of v7.10.0, v6.6.0 and v4.27.0 ([GRP-7596](https://todos.grouper.internet2.edu/browse/GRP-7596)), only a Grouper sysadmin (wheel or root) can save, change or delete a rule that has if or then expression language, through the UI, WS, GSH or the API. Rule EL runs in a restricted sandbox by default, with the variables below that have no *. To give it the extended API (constructors with new(...), class objects, and the variables marked *), set:

```
rules.allowUnrestrictedEl = true
```

- Most sites do not need it. Without it, a rule that needs more fails with an error that names this property, saving one in the rules UI (v6 and v7) shows a warning, and the v7 configuration review screen lists such rules.
- Send email rules that use only the predefined variables always run in the sandbox. One that uses anything else, or a "template: name" subject or body, runs unrestricted when the property is true.
- Before turning it on, run ruleElCheck.gsh (attached to [GRP-7596](https://todos.grouper.internet2.edu/browse/GRP-7596)) and sendEmailRuleTemplateCheck.gsh in GSH as an administrator. Both are read-only. Rules saved before only sysadmins could save them also run unrestricted, so delete any they mark REVIEW that nobody you trust wrote.

Before v7.10.0, v6.6.0 and v4.27.0, the extended API was given when the rule's act-as subject (usually GrouperSystem) was a member of the group in `rules.accessToApiInEl.group`. That property is no longer used: clear it, and set `rules.allowUnrestrictedEl = true` if your rules need the extended API. In v7.9.1, v6.5.1 and v4.26.1 rule EL runs in the sandbox even with that group, so rules that use new(...) or class objects fail there.

## Safe subject variable

The reference variable to the subject (if applies for the rule) is wrapped in a safeSubject variable. This allows specific getter methods on the subject without having full access to the real subject.

Methods callable on the safeSubject are:

- getAttributeValue(String attributeName)
- getAttributeValueOrCommaSeparated(String attributeName)
- getDescription()
- getEmailAddress()
- getId()
- getName()
- getSourceId()
- getTypeName()

## Example variables

For a check type of flattenedMembershipAdd, with the default sandbox, these variables represent the keys and values that would be available to a JEXL template.

```
checkOwnerName = "test:testGroup"
groupDescription = ""
groupDisplayExtension = "testGroup"
groupDisplayName = "test:testGroup"
groupExtension = "testGroup"
groupId = "f3045eb53ca14e3cb73c3efaedb092e8"
groupName = "test:testGroup"
memberId = "a780cbc803c247a09402ffc24e8d9185"
ownerGroupId = "f3045eb53ca14e3cb73c3efaedb092e8"
ruleElUtils = {RuleElUtils class} 
safeSubject = {SafeSubject class}
```

With the extended API (`rules.allowUnrestrictedEl = true`; before v7.10.0, v6.6.0 and v4.27.0, an act-as subject in `rules.accessToApiInEl.group`), these additional variables are included.

```
attributeAssignType = {AttributeAssign for the rule marker on the object}
group = {Group}
grouperUtil = {GrouperUtil class}
member = {Member object for the subject added}
ruleDefinition = {RuleDefinition object for the rule}
ruleUtils = {RuleUtils class}
subject = {Subject object for the user added}
```

## EL variable reference

Variables that are always set (* indicates only available with the extended API, see above)

| Variable | Notes |
| --- | --- |
| checkOwnerId | (may be null if rule checkOwner id is null) |
| checkOwnerName | (may be null if rule checkOwner id is null) |
| checkStemScope | (null for groups) |
| ownerAttributeAssignId |  |
| ownerAttributeDefId |  |
| ownerGroupId |  |
| ownerMemberId |  |
| ownerMembershipId |  |
| ownerStemId |  |
| ruleElUtils | Reference to class edu.internet2.middleware.grouper.rules.RuleElUtils.  Methods for formatDate(), hasMembershipByGroupId(), etc. |
| {custom util classes} | If class names are defined in grouper.properties: rules.customElClasses, these are the simple  class names for them |
| *attributeAssignType |  |
| *ruleDefinition |  |
| *grouperUtil | Reference to class edu.internet2.middleware.grouper.util.GrouperUtil. As of v7.9.1, v6.5.1 and v4.26.1, rule EL (if condition, then, and send email) can call only an allowlist of GrouperUtil helper methods and constructors (new), static class access and reflection are not allowed. A refused call fails like an unknown method and logs a WARN from GrouperJexlSandbox. As of v7.10.0, v6.6.0 and v4.27.0, set rules.allowUnrestrictedEl = true to let rule EL saved by a sysadmin run unrestricted (GRP-7596). |
| *ruleUtils | Reference to class edu.internet2.middleware.grouper.rules.RuleUtils |

Additional variables available for check types:

- flattenedMembershipAdd
- flattenedMembershipAddInFolder
- flattenedMembershipRemove
- flattenedMembershipRemoveInFolder
- membershipAdd
- membershipAddInFolder
- membershipDisabledDate
- membershipRemove
- membershipRemoveInFolder
- subjectAssignInStem (If an instance of a membership, not a permissions framework assignment)

| Variable | Notes |
| --- | --- |
| groupId |  |
| groupName |  |
| groupDisplayName |  |
| groupExtension |  |
| groupDisplayExtension |  |
| groupDescription |  |
| *group |  |
| memberId |  |
| *member |  |
| *membership |  |
| membershipId |  |
| membershipDisabledTimestamp |  |
| membershipEnabledTimestamp |  |
| *subject |  |
| safeSubject |  |

Additional variables available for check types:

- groupCreate

| Variable | Notes |
| --- | --- |
| groupId |  |
| groupName |  |
| *group |  |

Additional variables available for check types:

- stemCreate

| Variable | Notes |
| --- | --- |
| stemId |  |
| stemName |  |
| *stem |  |

Additional variables available for check types:

- attributeDefCreate

| Variable | Notes |
| --- | --- |
| attributeDefId |  |
| attributeDefName |  |
| *attributeDef |  |

Additional variables available for check types:

- permissionDisabledDate
- permissionAssignToSubject
- subjectAssignInStem (If an instance of a permissions framework assignment, not a membership)

| Variable | Notes |
| --- | --- |
| roleId |  |
| roleName |  |
| roleDisplayName |  |
| roleExtension |  |
| roleDisplayExtension |  |
| roleDescription |  |
| memberId |  |
| safeSubject |  |
| *member |  |
| *role |  |
| *subject |  |
| action |  |
| nameOfAttributeDef |  |
| attributeDefExtension |  |
| attributeDefId |  |
| *attributeDef |  |
| attributeDefNameName |  |
| attributeDefNameId |  |
| attributeDefNameExtension |  |
| attributeDefNameDisplayName |  |
| attributeDefNameDescription |  |
| attributeDefNameDisplayExtension |  |
| *attributeDefName |  |
| attributeAssignId |  |
| permissionDisabledTimestamp |  |
| permissionEnabledTimestamp |  |
| *attributeAssign |  |

## Custom EL classes

You can configure custom EL classes to help with logic you need if not in the Grouper API. Here is an example:

```
# put in fully qualified classes to add to the EL context.  Note that they need a default constructor
# comma separated.  The alias will be the simple class name without a first cap.
# e.g. if the class is test.Test the alias is "test"
rules.customElClasses = edu.internet2.middleware.grouper.rules.MyRuleUtils

```

Make a class:

```
/**
 * @author mchyzer
 * $Id: MyRuleUtils.java 6947 2010-08-23 15:33:36Z mchyzer $
 */
package edu.internet2.middleware.grouper.rules;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 *
 */
public class MyRuleUtils {

  /**
   * remove a member of a group
   * @param groupId
   * @param memberId
   * @return true if removed, false if not
   */
  public static boolean removeMemberFromGroupId(String groupId, String memberId) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Removing member: " + memberId + ", from group: " + groupId);
    }
    Group group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), groupId, true);
    Member member = MemberFinder.findByUuid(GrouperSession.startRootSession(), memberId, true);
    boolean result = group.deleteMember(member, false);
    if (LOG.isDebugEnabled()) {
      LOG.debug("Removing subject: " + member.getSubjectId()
          + ", from group: " + group.getName() + ", result: " + result);
    }
    return result;
  }
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(MyRuleUtils.class);

}

```

Use that in an EL:

```
etc:attribute:rules:ruleThenEl = ${myRuleUtils.removeMemberFromGroupId(ownerGroupId, memberId)}
```
