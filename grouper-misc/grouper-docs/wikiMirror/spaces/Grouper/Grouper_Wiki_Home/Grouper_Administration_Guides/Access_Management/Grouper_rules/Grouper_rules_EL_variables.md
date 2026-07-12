---
title: "Grouper rules EL variables"
space: Grouper
pageId: 28549057
version: 6
lastUpdated: 2025-04-10T18:34:52.344Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549057/Grouper+rules+EL+variables
---

Expression language (EL) substitutions using variables combined with a JEXL template can be used on either a custom If condition ("Expression language"), or in a Send Email action, in the To addresses, subject, and body. Some variables are available for all rule types. But extra variables will differ depending on the rule check type.

If logging for edu.internet2.middleware.grouper.rules.RuleEngine, or just the rules package, is set to DEBUG, and grouper.properties value "rules.attributeAssignTypeIdsToLog" includes the id string of a specific rule's marker attribute, it will log the EL key/value pairs as part of it's debugging.

```xml
        <Logger name="edu.internet2.middleware.grouper.rules" level="debug" additivity="false">
            __LOGPIPESTART__<AppenderRef ref="logpipe_grouper_provisioning"/>__LOGPIPEEND__
            __FILESTART__<AppenderRef ref="file_grouper_provisioning"/>__FILEEND__
            __STDERRSTART__<AppenderRef ref="stderr"/>__STDERREND__
        </Logger>
```

## Extended EL API

There is a special group which has access to more objects in EL:

```
# any actAs subject in this group has access to more objects when the EL fires on
# the IF or THEN EL clause
rules.accessToApiInEl.group =

```

This is because the RuleUtils class might be too limiting in some cases, but if everyone had access to the API, it might not be secure. So if you need this, configure a group here, put in trusted admins/users, then act as those users in your rule. Since rules normally run as GrouperSystem, you would include GrouperSystem as a member of this group.

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

For a check type of flattenedMembershipAdd, with no special EL privileges for the GrouperSystem subject, these variables represent the keys and values that would be available to a JEXL template.

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

When the rule runs as a user with extra EL privileges, these additional variables are included.

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

Variables that are always set (* indicates only available for the privileged access group)

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
| *grouperUtil | Reference to class edu.internet2.middleware.grouper.util.GrouperUtil |
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
