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

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
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
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * <pre>
 * Filters a set of rule definitions down to the ones a subject is allowed to see.
 *
 * Why: the global "Rules configurations" UI screen (UiV2Rules.viewAllRules) lists every rule in
 * the rule engine.  The rule engine is loaded as root, so without a filter any authenticated user
 * could read rules (and the names of the groups, folders and attribute definitions they reference)
 * on objects the user cannot even find.
 *
 * Who sees what:
 * - wheel, root, and view-only / read-only root see every rule (the full inventory, as before)
 * - anyone else sees a rule only if
 *   1. its owner resolves in the subject's own session with the privilege the per-object rules
 *      screens require: group READ, folder findable (as the folder rules screen), attribute
 *      definition ATTR_READ.  Rules on any other owner type (e.g. an assignment on an
 *      assignment) are dropped.
 *   2. every object the rule names in its check owner and if-condition owner (id or name)
 *      resolves in the subject's session as a group, folder or attribute definition.  So a rule
 *      on a visible group that points at a hidden group is hidden too, instead of rendering
 *      "Group not found: hidden:name".
 * - anything unexpected (lookup throws, no subject) drops the rule: fail closed.
 *
 * This is a new class, not a change to RuleFinder or RuleEngine, so the fix is one version that
 * works on every release from 5.8.0 (when the global screen appeared) through 7.x.
 * </pre>
 */
public class RuleDefinitionVisibility {

  /**
   * no instances
   */
  private RuleDefinitionVisibility() {
  }

  /**
   * return the rule definitions the subject may see (see the class javadoc for the rules)
   * @param ruleDefinitions all rule definitions, e.g. from RuleFinder.retrieveRuleDefinitionsForSubject
   * @param subject the logged in subject
   * @return a new set with only the visible rule definitions, never null
   */
  public static Set<RuleDefinition> filterForSubject(final Set<RuleDefinition> ruleDefinitions, final Subject subject) {

    final Set<RuleDefinition> result = new LinkedHashSet<RuleDefinition>();

    // nothing to filter, or no subject to authorize against: return nothing (fail closed)
    if (ruleDefinitions == null || ruleDefinitions.isEmpty() || subject == null) {
      return result;
    }

    // run every lookup in a session of the subject itself, regardless of which session the
    // caller has open, so the secure finders apply the subject's privileges
    GrouperSession subjectSession = GrouperSession.start(subject, false);
    try {
      GrouperSession.callbackGrouperSession(subjectSession, new GrouperSessionHandler() {

        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

          // admins (including view-only / read-only root) keep the full inventory
          if (PrivilegeHelper.isWheelOrRootOrViewonlyRoot(subject)) {
            result.addAll(ruleDefinitions);
            return null;
          }

          // rules often share owners and referenced objects, so cache lookups per call
          Map<String, Boolean> ownerCache = new HashMap<String, Boolean>();
          Map<String, Boolean> referenceCache = new HashMap<String, Boolean>();

          for (RuleDefinition ruleDefinition : ruleDefinitions) {
            try {
              if (ruleDefinition != null
                  && ownerVisible(grouperSession, subject, ruleDefinition, ownerCache)
                  && referencesVisible(grouperSession, ruleDefinition, referenceCache)) {
                result.add(ruleDefinition);
              }
            } catch (RuntimeException re) {
              // e.g. an insufficient privilege or not found from a finder: hide the rule
            }
          }
          return null;
        }
      });
    } finally {
      GrouperSession.stopQuietly(subjectSession);
    }
    return result;
  }

  // ------------------------------------------------------------------ owner

  /**
   * is the owner of the rule visible to the subject with the privilege the per-object rules
   * screens require
   * @param grouperSession session of the subject
   * @param subject subject
   * @param ruleDefinition rule
   * @param ownerCache cache of owner key to result
   * @return true if visible
   */
  private static boolean ownerVisible(GrouperSession grouperSession, Subject subject,
      RuleDefinition ruleDefinition, Map<String, Boolean> ownerCache) {

    AttributeAssign attributeAssign = ruleDefinition.getAttributeAssignType();
    if (attributeAssign == null) {
      return false;
    }

    // only one owner id is set on a rule assignment
    String ownerGroupId = attributeAssign.getOwnerGroupId();
    String ownerStemId = attributeAssign.getOwnerStemId();
    String ownerAttributeDefId = attributeAssign.getOwnerAttributeDefId();

    // assignment on an assignment, member or membership: no rules screen shows these to a
    // normal user, so hide them
    if (!GrouperUtil.isBlank(attributeAssign.getOwnerAttributeAssignId())) {
      return false;
    }

    if (!GrouperUtil.isBlank(ownerGroupId)) {
      String key = "group:" + ownerGroupId;
      Boolean cached = ownerCache.get(key);
      if (cached == null) {
        // secure find needs VIEW; the group rules screen needs READ
        Group group = GroupFinder.findByUuid(grouperSession, ownerGroupId, false);
        cached = group != null && group.canHavePrivilege(subject, AccessPrivilege.READ.getName(), false);
        ownerCache.put(key, cached);
      }
      return cached;
    }

    if (!GrouperUtil.isBlank(ownerStemId)) {
      String key = "stem:" + ownerStemId;
      Boolean cached = ownerCache.get(key);
      if (cached == null) {
        // the folder rules screen shows a folder's rules to anyone who can find the folder
        Stem stem = StemFinder.findByUuid(grouperSession, ownerStemId, false);
        cached = stem != null;
        ownerCache.put(key, cached);
      }
      return cached;
    }

    if (!GrouperUtil.isBlank(ownerAttributeDefId)) {
      String key = "attributeDef:" + ownerAttributeDefId;
      Boolean cached = ownerCache.get(key);
      if (cached == null) {
        // secure find needs ATTR_VIEW; reading the definition's settings needs ATTR_READ
        AttributeDef attributeDef = AttributeDefFinder.findById(ownerAttributeDefId, false);
        cached = attributeDef != null && attributeDef.getPrivilegeDelegate().canAttrRead(subject);
        ownerCache.put(key, cached);
      }
      return cached;
    }

    // member or membership owner, or nothing: hide
    return false;
  }

  // ------------------------------------------------------------------ referenced objects

  /**
   * are all objects named in the check owner and the if-condition owner visible
   * @param grouperSession session of the subject
   * @param ruleDefinition rule
   * @param referenceCache cache
   * @return true if all visible (or none named)
   */
  private static boolean referencesVisible(GrouperSession grouperSession, RuleDefinition ruleDefinition,
      Map<String, Boolean> referenceCache) {

    RuleCheck ruleCheck = ruleDefinition.getCheck();
    if (ruleCheck != null) {
      if (!referenceVisible(grouperSession, ruleCheck.getCheckOwnerId(), true, referenceCache)) {
        return false;
      }
      if (!referenceVisible(grouperSession, ruleCheck.getCheckOwnerName(), false, referenceCache)) {
        return false;
      }
    }

    RuleIfCondition ruleIfCondition = ruleDefinition.getIfCondition();
    if (ruleIfCondition != null) {
      if (!referenceVisible(grouperSession, ruleIfCondition.getIfOwnerId(), true, referenceCache)) {
        return false;
      }
      if (!referenceVisible(grouperSession, ruleIfCondition.getIfOwnerName(), false, referenceCache)) {
        return false;
      }
    }
    return true;
  }

  /**
   * <pre>
   * is a referenced object visible to the subject of the session.  The check and if-condition
   * types decide whether an owner is a group, folder or attribute definition, and there are
   * many of them, so accept the reference if it resolves securely as any of the three.  UUIDs
   * are unique across types, and a name that resolves as a visible object of some type is a name
   * the subject can already see.
   * </pre>
   * @param grouperSession session of the subject
   * @param idOrName id or name, blank means nothing referenced
   * @param isId true if id, false if name
   * @param referenceCache cache
   * @return true if blank or visible
   */
  private static boolean referenceVisible(GrouperSession grouperSession, String idOrName, boolean isId,
      Map<String, Boolean> referenceCache) {

    if (GrouperUtil.isBlank(idOrName)) {
      return true;
    }

    String key = (isId ? "id:" : "name:") + idOrName;
    Boolean cached = referenceCache.get(key);
    if (cached != null) {
      return cached;
    }

    boolean visible;
    if (isId) {
      visible = GroupFinder.findByUuid(grouperSession, idOrName, false) != null
          || StemFinder.findByUuid(grouperSession, idOrName, false) != null
          || AttributeDefFinder.findById(idOrName, false) != null;
    } else {
      visible = GroupFinder.findByName(grouperSession, idOrName, false) != null
          || StemFinder.findByName(grouperSession, idOrName, false) != null
          || AttributeDefFinder.findByName(idOrName, false) != null;
    }
    referenceCache.put(key, visible);
    return visible;
  }

}
