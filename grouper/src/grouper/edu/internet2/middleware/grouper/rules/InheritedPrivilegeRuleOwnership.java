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

import java.util.Collection;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * <pre>
 * Binds an inherited privilege rule to the folder it is managed from.
 *
 * The RuleFinder.find*PrivilegeInheritRules(stem) finders return every rule that APPLIES at a
 * folder, including rules owned by ancestor folders (an ancestor SUB rule reaches the child).
 * That is right for display, but a folder admin may only change or remove rules whose rule
 * marker is assigned to that exact folder.  Without this check, a STEM_ADMIN of a child folder
 * could submit an ancestor rule's attribute assign id (UiV2Stem.removeInheritedPrivileges, or
 * Privilege*InheritanceSave on the child) and delete or edit the ancestor's policy as root.
 *
 * This class only uses APIs that exist in every Grouper release from 5.0.0 on (no commons-lang
 * StringUtils, which moved from lang to lang3 between 5.x and 6.x) so the same file ships in
 * every patch set.
 * </pre>
 */
public class InheritedPrivilegeRuleOwnership {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(InheritedPrivilegeRuleOwnership.class);

  /**
   * no instances
   */
  private InheritedPrivilegeRuleOwnership() {
  }

  /**
   * see if a rule's marker attribute assignment is on this exact folder
   * @param attributeAssign rule marker assignment (may be null)
   * @param stemId the folder the caller is authorized on
   * @return true if owned by that folder, false if not (or if anything is missing)
   */
  public static boolean isAttributeAssignOwnedByStem(AttributeAssign attributeAssign, String stemId) {
    // fail closed on missing data
    if (attributeAssign == null || stemId == null || stemId.length() == 0) {
      return false;
    }
    return stemId.equals(attributeAssign.getOwnerStemId());
  }

  /**
   * see if a rule is owned by (assigned to) this exact folder
   * @param ruleDefinition rule (may be null)
   * @param stemId the folder the caller is authorized on
   * @return true if the rule marker is assigned to that folder
   */
  public static boolean isOwnedByStem(RuleDefinition ruleDefinition, String stemId) {
    if (ruleDefinition == null) {
      return false;
    }
    return isAttributeAssignOwnedByStem(ruleDefinition.getAttributeAssignType(), stemId);
  }

  /**
   * count requested rule ids that are not in the allowed set (rules owned by the folder).
   * Callers reject the whole request if this is not zero, so a request that mixes an owned
   * rule id with an ancestor (or unknown) rule id removes nothing.
   * @param requestedRuleIds attribute assign ids from the request
   * @param allowedRuleIds attribute assign ids of rules owned by the folder
   * @param stemId folder id from the request, for the log
   * @param loggedInSubject user, for the log
   * @return the number of requested ids that are not allowed
   */
  public static int countUnownedRuleIds(Collection<String> requestedRuleIds,
      Collection<String> allowedRuleIds, String stemId, Subject loggedInSubject) {
    int count = 0;
    for (String requestedRuleId : GrouperUtil.nonNull(requestedRuleIds)) {
      if (allowedRuleIds == null || !allowedRuleIds.contains(requestedRuleId)) {
        // could be an ancestor folder's rule, another object's rule, a stale id, or garbage
        LOG.warn("Inherited privilege rule id is not a rule owned by folder: " + requestedRuleId
            + ", folder id: " + stemId + ", user: " + GrouperUtil.subjectToString(loggedInSubject));
        count++;
      }
    }
    return count;
  }

  /**
   * reload the rule marker assignment from the database (no cache) and make sure it is still
   * on this folder, right before it is deleted.  Call inside the same root callback as the
   * delete so a stale pre-check cannot authorize a different object.
   * @param attributeAssignId rule marker assignment id
   * @param stemId the folder the caller is authorized on
   * @throws RuntimeException if not found or not owned by the folder
   */
  public static void assertAttributeAssignIdOwnedByStem(String attributeAssignId, String stemId) {
    AttributeAssign attributeAssign = attributeAssignId == null ? null
        : GrouperDAOFactory.getFactory().getAttributeAssign().findById(attributeAssignId, false, false);
    if (!isAttributeAssignOwnedByStem(attributeAssign, stemId)) {
      throw new RuntimeException("Inherited privilege rule '" + attributeAssignId
          + "' is not owned by folder '" + stemId + "'");
    }
  }
}
