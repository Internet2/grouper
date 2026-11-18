/**
 * Copyright 2026 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.internet2.middleware.grouper.rules;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;

/**
 * SECURITY: binds a rule id from a request to the group or folder the request is about.
 *
 * The rules UI checks the user's privilege on the group or folder in the request, then passes the
 * submitted rule id (an attribute assign id) to RuleService.saveOrUpdateRuleAttributes, which runs
 * in a root session and overwrites that assign.  Without this check a user who administers one
 * object could overwrite a rule on another object (rules run as GrouperSystem, so that is a
 * privilege escalation), or the values of an attribute assign that is not a rule at all.
 *
 * This is its own class, not a method on RuleService, so the fix can be dropped into older releases
 * as whole files: RuleService differs from release to release (newer ones carry the GRP-7359 rules
 * lockdown), and this class only uses APIs that have been stable since 5.8.0.  RuleService only
 * gets a few lines that call it.
 */
public class RuleAssignOwnerCheck {

  /**
   * Find the rule assign with this id, but only if it is a rule (an assign of the rule attribute def
   * name) and it is owned by the given group or folder.  The lookup runs as root, so the result does
   * not depend on the caller's privileges on the attribute def.
   * @param grouperObject the group or folder the rule is being saved on
   * @param attributeAssignId the rule id from the request
   * @return the rule assign, or null if the id is blank, not found, not a rule, or owned by another object
   */
  public static AttributeAssign ruleAssignOwnedBy(final GrouperObject grouperObject, final String attributeAssignId) {

    if (grouperObject == null || StringUtils.isBlank(attributeAssignId)) {
      return null;
    }

    // rules are only assigned directly to groups and folders
    if (!(grouperObject instanceof Group) && !(grouperObject instanceof Stem)) {
      return null;
    }

    return (AttributeAssign)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      public Object callback(GrouperSession rootSession) {

        AttributeAssign attributeAssign = AttributeAssignFinder.findById(attributeAssignId, false);
        if (attributeAssign == null) {
          return null;
        }

        // must be a rule, not some other attribute assign whose values a rule save would overwrite
        AttributeDefName ruleAttributeDefName = RuleUtils.ruleAttributeDefName();
        if (ruleAttributeDefName == null
            || !StringUtils.equals(ruleAttributeDefName.getId(), attributeAssign.getAttributeDefNameId())) {
          return null;
        }

        // must be owned by the object whose privilege the caller checked
        if (grouperObject instanceof Group) {
          if (!StringUtils.equals(((Group)grouperObject).getId(), attributeAssign.getOwnerGroupId())) {
            return null;
          }
        } else {
          if (!StringUtils.equals(((Stem)grouperObject).getUuid(), attributeAssign.getOwnerStemId())) {
            return null;
          }
        }
        return attributeAssign;
      }
    });
  }

  /**
   * Error message when a rule id is not a rule on the object being saved.  Externalized text
   * grouperRuleConfigAddEditInvalidRuleId, with a fallback so the fix does not need a properties file.
   * @return the message
   */
  public static String invalidRuleIdError() {
    String error = null;
    try {
      error = GrouperTextContainer.textOrNull("grouperRuleConfigAddEditInvalidRuleId");
    } catch (RuntimeException re) {
      // text container not available in this context, use the fallback
      error = null;
    }
    if (StringUtils.isBlank(error)) {
      error = "Invalid ruleId: that rule is not on this group or folder.";
    }
    return error;
  }

}
