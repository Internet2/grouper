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

import java.util.HashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.subject.Subject;

/**
 * <pre>
 * Rule validation check for objects a rule references besides its own owner: the check owner
 * (ruleCheckOwnerId / ruleCheckOwnerName) and the if owner (ruleIfOwnerId / ruleIfOwnerName).
 *
 * Rules fire and evaluate their conditions in root sessions (RuleIfConditionEnum, RuleCheckType,
 * RuleUtils look up the referenced group, folder or attribute definition as GrouperSystem and read
 * its memberships directly), so a rule on an object a user administers could otherwise watch or
 * test the memberships of any other group: e.g. "veto adds to my group unless the subject is in
 * secret:group" tells the user who is in secret:group, and "when a member is added to
 * secret:group, add them to my group" copies its membership.
 *
 * This class keeps that evaluation as it is and instead refuses, at validation time, a rule
 * whose act-as subject could not read the referenced object itself:
 *   group:                READ
 *   folder:               STEM_ADMIN
 *   attribute definition: ATTR_READ
 * Wheel or GrouperSystem act-as subjects are not checked (that includes every rule saved
 * through the rules UI, which saves act-as GrouperSystem after its own pattern checks).  The
 * rule's own owner is not checked (the author already has attribute update on it).  A reference
 * that does not resolve gets the same message as one that is not allowed, so the validation
 * result is not an existence oracle.
 *
 * Validation runs when a rule attribute is written (GrouperAttributeAssignValueRulesConfigHook)
 * and again in the rules daemon, so a rule whose act-as subject later loses the privilege
 * becomes invalid at the next daemon run.
 *
 * Uses only APIs present in Grouper 2.6 through 7.x so the same file serves every release.
 * </pre>
 */
public class RuleReferencedObjectAuthorization {

  /**
   * one message for not found and not allowed, so the result does not say whether the object exists
   */
  public static final String NOT_ALLOWED_MESSAGE = "The object referenced by the rule check owner or if owner"
      + " was not found, or the act-as subject is not allowed to read it (group READ, folder STEM_ADMIN,"
      + " attribute definition ATTR_READ): ";

  /**
   * no instances
   */
  private RuleReferencedObjectAuthorization() {
  }

  /**
   * validate the check owner and if owner of a rule against the act-as subject's privileges
   * @param ruleDefinition
   * @return the error, or null if ok (or not applicable)
   */
  public static String validate(final RuleDefinition ruleDefinition) {

    if (ruleDefinition == null || ruleDefinition.getActAs() == null) {
      //RuleDefinition.validate reports a missing act as
      return null;
    }

    final Subject actAsSubject;
    try {
      actAsSubject = ruleDefinition.getActAs().subject(false);
    } catch (RuntimeException re) {
      //RuleSubjectActAs.validate reports subjects that cannot be resolved
      return null;
    }
    if (actAsSubject == null) {
      return null;
    }

    //ids of the rule's own owner: references to it are not cross object
    final Set<String> ownerIds = new HashSet<String>();
    AttributeAssign attributeAssignType = ruleDefinition.getAttributeAssignType();
    if (attributeAssignType != null) {
      addIfNotBlank(ownerIds, attributeAssignType.getOwnerGroupId());
      addIfNotBlank(ownerIds, attributeAssignType.getOwnerStemId());
      addIfNotBlank(ownerIds, attributeAssignType.getOwnerAttributeDefId());
    }

    final Set<String> referencedIds = new HashSet<String>();
    final Set<String> referencedNames = new HashSet<String>();
    RuleCheck ruleCheck = ruleDefinition.getCheck();
    if (ruleCheck != null) {
      addIfNotBlank(referencedIds, ruleCheck.getCheckOwnerId());
      addIfNotBlank(referencedNames, ruleCheck.getCheckOwnerName());
    }
    RuleIfCondition ruleIfCondition = ruleDefinition.getIfCondition();
    if (ruleIfCondition != null) {
      addIfNotBlank(referencedIds, ruleIfCondition.getIfOwnerId());
      addIfNotBlank(referencedNames, ruleIfCondition.getIfOwnerName());
    }

    //references to the owner itself are not cross object
    referencedIds.removeAll(ownerIds);

    if (referencedIds.isEmpty() && referencedNames.isEmpty()) {
      return null;
    }

    //wheel and GrouperSystem can read everything
    if (PrivilegeHelper.isWheelOrRoot(actAsSubject)) {
      return null;
    }

    //look the objects up as root (the act-as subject might not even be able to see them),
    //then check the act-as subject's own privilege
    GrouperSession rootSession = GrouperSession.staticGrouperSession().internal_getRootSession();
    return (String)GrouperSession.callbackGrouperSession(rootSession, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        for (String id : referencedIds) {
          if (!allowed(grouperSession, actAsSubject, id, true, ownerIds)) {
            return NOT_ALLOWED_MESSAGE + id;
          }
        }
        for (String name : referencedNames) {
          if (!allowed(grouperSession, actAsSubject, name, false, ownerIds)) {
            return NOT_ALLOWED_MESSAGE + name;
          }
        }
        return null;
      }
    });
  }

  /**
   * see if the act-as subject may reference this object
   * @param rootSession
   * @param actAsSubject
   * @param idOrName
   * @param isId true if id, false if name
   * @param ownerIds ids of the rule owner (allowed without a check)
   * @return true if allowed, false if not allowed or not found
   */
  private static boolean allowed(GrouperSession rootSession, Subject actAsSubject, String idOrName, boolean isId,
      Set<String> ownerIds) {

    Group group = null;
    try {
      group = isId ? GroupFinder.findByUuid(rootSession, idOrName, false)
          : GroupFinder.findByName(rootSession, idOrName, false);
    } catch (RuntimeException re) {
      //not a group, try the other types
    }
    if (group != null) {
      return ownerIds.contains(group.getId()) || PrivilegeHelper.canRead(rootSession, group, actAsSubject);
    }

    Stem stem = null;
    try {
      stem = isId ? StemFinder.findByUuid(rootSession, idOrName, false)
          : StemFinder.findByName(rootSession, idOrName, false);
    } catch (RuntimeException re) {
      //not a folder
    }
    if (stem != null) {
      return ownerIds.contains(stem.getUuid()) || PrivilegeHelper.canStemAdmin(rootSession, stem, actAsSubject);
    }

    AttributeDef attributeDef = null;
    try {
      attributeDef = isId ? AttributeDefFinder.findById(idOrName, false)
          : AttributeDefFinder.findByName(idOrName, false);
    } catch (RuntimeException re) {
      //not an attribute definition
    }
    if (attributeDef != null) {
      return ownerIds.contains(attributeDef.getId()) || PrivilegeHelper.canAttrRead(rootSession, attributeDef, actAsSubject);
    }

    //not found: same answer as not allowed
    return false;
  }

  /**
   * add a trimmed value to the set if it is not blank
   * @param set
   * @param value
   */
  private static void addIfNotBlank(Set<String> set, String value) {
    if (value != null && value.trim().length() > 0) {
      set.add(value.trim());
    }
  }

}
