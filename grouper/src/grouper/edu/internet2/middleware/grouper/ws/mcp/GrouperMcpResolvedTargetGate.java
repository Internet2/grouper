/*******************************************************************************
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
 ******************************************************************************/
package edu.internet2.middleware.grouper.ws.mcp;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup;
import edu.internet2.middleware.subject.Subject;

/**
 * <p>Runs the MCP protected-resource rules (GrouperMcpProtectedResources) and the OAuth
 * read-write consent scope (GrouperMcpAuthUser) on the object an MCP write tool will really
 * change, not on the string the caller sent.</p>
 *
 * <p>Why: a group or folder name also matches its alternate (old) name, an attribute assignment
 * can be addressed by its uuid, and a template runs on an owner the caller picks.  Checking the
 * typed string let a caller reach an object outside the consent through another way of naming
 * it.  So every MCP write tool resolves its target (as root, only to find out what it is: the
 * operation itself still runs as the caller, so Grouper privileges stay authoritative) and gates
 * that object here, by its real name and uuid.</p>
 *
 * <p>Rules:</p>
 * <ul>
 *   <li>Fail closed: anything that cannot be resolved (null object, lookup error, unknown owner
 *   type, an assignment on an assignment on an assignment) is denied.</li>
 *   <li>The consent scope is checked when {@link GrouperMcpAuthUser#isConsentScopeEnforced()},
 *   the protected-resource rules for every caller.</li>
 *   <li>Denial messages never echo a name or subject id the caller did not send, unless the
 *   caller can see that object.  Otherwise they use {@link #GENERIC_DENIED_MESSAGE}, which also
 *   covers "not found" so a denial does not confirm that an object exists.</li>
 * </ul>
 *
 * <p>Each check returns null when allowed, or the error message for the tool to return.</p>
 */
public class GrouperMcpResolvedTargetGate {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperMcpResolvedTargetGate.class);

  /** denial when the caller may not learn which object it was or why */
  public static final String GENERIC_DENIED_MESSAGE = "Access denied: the target is protected from "
      + "changes via MCP, is outside your consented read-write scope, or was not found.";

  /** denial when the consent has no group or folder dimension at all */
  public static final String NO_GROUP_OR_FOLDER_SCOPE_MESSAGE =
      "Access denied: your OAuth scope does not include groups or folders.";

  /** denial when the consent has no subject dimension at all */
  public static final String NO_SUBJECT_SCOPE_MESSAGE =
      "Access denied: your OAuth scope does not include subjects.";

  /**
   * no instances
   */
  private GrouperMcpResolvedTargetGate() {
  }

  /**
   * the result of resolving a typed group or folder name and gating what it resolved to
   */
  public static class Target {

    /** error message, or null if allowed */
    private final String error;

    /** uuid of the object the name resolved to, or null if it did not resolve */
    private final String uuid;

    /** current name of the object the name resolved to, or null if it did not resolve */
    private final String name;

    /**
     * @param error1 error or null
     * @param uuid1 uuid or null
     * @param name1 current name or null
     */
    Target(String error1, String uuid1, String name1) {
      this.error = error1;
      this.uuid = uuid1;
      this.name = name1;
    }

    /**
     * @return the error message, or null if the write may go ahead
     */
    public String getError() {
      return this.error;
    }

    /**
     * @return the uuid of the existing object, or null if the name matched nothing (e.g. a create)
     */
    public String getUuid() {
      return this.uuid;
    }

    /**
     * @return the current name of the existing object (not the alternate name the caller may
     * have sent), or null if the name matched nothing
     */
    public String getName() {
      return this.name;
    }

    /**
     * @return true if the typed name matched an existing object
     */
    public boolean isFound() {
      return this.uuid != null;
    }
  }

  // ------------------------------------------------------------------------------------------
  // groups and folders
  // ------------------------------------------------------------------------------------------

  /**
   * resolve a typed group name as root (current or alternate name) and gate the group it
   * resolves to.  If it resolves to nothing (a group about to be created, or a typo), the typed
   * name is gated as it is, as before, since there is no other object it could reach.
   * @param authUser the caller
   * @param typedName the group name the caller sent
   * @return the result; write by its uuid or current name when found, so the object checked is
   * the object changed
   */
  public static Target checkGroupByName(GrouperMcpAuthUser authUser, final String typedName) {
    return checkGroupByName(authUser, typedName, true);
  }

  /**
   * see {@link #checkGroupByName(GrouperMcpAuthUser, String)}
   * @param authUser the caller
   * @param typedName the group name the caller sent
   * @param requireScopeDimension true (save, delete and attribute tools, as in 7.9.0) to deny a
   * scoped token whose scope has no values in this dimension; false (member, privilege and
   * template tools) to leave the dimension open when another dimension is scoped, as
   * isGroupInReadwriteScope / isSubjectInReadwriteScope already decide
   * @return the result
   */
  public static Target checkGroupByName(GrouperMcpAuthUser authUser, final String typedName,
      boolean requireScopeDimension) {
    Group group = findGroupByNameAsRoot(typedName);
    if (group != null) {
      return new Target(checkGroup(authUser, group, typedName, requireScopeDimension),
          group.getUuid(), group.getName());
    }
    return new Target(checkTypedGroupName(authUser, typedName, requireScopeDimension), null, null);
  }

  /**
   * resolve a typed folder name as root (current or alternate name) and gate the folder it
   * resolves to.  See {@link #checkGroupByName(GrouperMcpAuthUser, String)}.
   * @param authUser the caller
   * @param typedName the folder name the caller sent
   * @return the result
   */
  public static Target checkStemByName(GrouperMcpAuthUser authUser, final String typedName) {
    return checkStemByName(authUser, typedName, true);
  }

  /**
   * see {@link #checkStemByName(GrouperMcpAuthUser, String)}
   * @param authUser the caller
   * @param typedName the folder name the caller sent
   * @param requireScopeDimension true (save, delete and attribute tools, as in 7.9.0) to deny a
   * scoped token whose scope has no values in this dimension; false (member, privilege and
   * template tools) to leave the dimension open when another dimension is scoped, as
   * isGroupInReadwriteScope / isSubjectInReadwriteScope already decide
   * @return the result
   */
  public static Target checkStemByName(GrouperMcpAuthUser authUser, final String typedName,
      boolean requireScopeDimension) {
    Stem stem = findStemByNameAsRoot(typedName);
    if (stem != null) {
      return new Target(checkStem(authUser, stem, typedName, requireScopeDimension),
          stem.getUuid(), stem.getName());
    }
    return new Target(checkTypedStemName(authUser, typedName, requireScopeDimension), null, null);
  }

  /**
   * gate a group name that does not resolve to a group (yet): protected rules and consent scope
   * on the string
   * @param authUser the caller
   * @param typedName name the caller sent
   * @return error or null
   */
  private static String checkTypedGroupName(GrouperMcpAuthUser authUser, String typedName,
      boolean requireScopeDimension) {
    if (StringUtils.isBlank(typedName)) {
      return GENERIC_DENIED_MESSAGE;
    }
    if (GrouperMcpProtectedResources.isProtectedGroupName(typedName)) {
      return GrouperMcpProtectedResources.buildProtectedGroupError(typedName);
    }
    if (authUser.isConsentScopeEnforced()) {
      if (requireScopeDimension && !authUser.hasGroupOrFolderReadwriteScope()) {
        return NO_GROUP_OR_FOLDER_SCOPE_MESSAGE;
      }
      // the (name, uuid) form, not the string form: that one looks the name up again
      if (!authUser.isGroupInReadwriteScope(typedName, null)) {
        return authUser.buildReadwriteScopeDeniedError("group", typedName);
      }
    }
    return null;
  }

  /**
   * gate a folder name that does not resolve to a folder (yet)
   * @param authUser the caller
   * @param typedName name the caller sent
   * @return error or null
   */
  private static String checkTypedStemName(GrouperMcpAuthUser authUser, String typedName,
      boolean requireScopeDimension) {
    if (StringUtils.isBlank(typedName)) {
      return GENERIC_DENIED_MESSAGE;
    }
    if (GrouperMcpProtectedResources.isProtectedStemName(typedName)) {
      return GrouperMcpProtectedResources.buildProtectedStemError(typedName);
    }
    if (authUser.isConsentScopeEnforced()) {
      if (requireScopeDimension && !authUser.hasGroupOrFolderReadwriteScope()) {
        return NO_GROUP_OR_FOLDER_SCOPE_MESSAGE;
      }
      if (!authUser.isStemInReadwriteScope(typedName, null)) {
        return authUser.buildReadwriteScopeDeniedError("folder", typedName);
      }
    }
    return null;
  }

  /**
   * gate a resolved group on its real name and uuid
   * @param authUser the caller
   * @param group the group the operation will change, null is denied
   * @param typedName the name the caller sent for it, or null if the caller sent an id
   * @return error or null
   */
  public static String checkGroup(final GrouperMcpAuthUser authUser, final Group group, String typedName) {
    return checkGroup(authUser, group, typedName, true);
  }

  /**
   * see {@link #checkGroup(GrouperMcpAuthUser, Group, String)}
   * @param authUser the caller
   * @param group the group the operation will change, null is denied
   * @param typedName the name the caller sent for it, or null if the caller sent an id
   * @param requireScopeDimension true (save, delete and attribute tools, as in 7.9.0) to deny a
   * scoped token whose scope has no values in this dimension; false (member, privilege and
   * template tools) to leave the dimension open when another dimension is scoped, as
   * isGroupInReadwriteScope / isSubjectInReadwriteScope already decide
   * @return error or null
   */
  public static String checkGroup(final GrouperMcpAuthUser authUser, final Group group, String typedName,
      boolean requireScopeDimension) {
    if (group == null) {
      return GENERIC_DENIED_MESSAGE;
    }
    String realName = group.getName();

    // the typed name is checked too: a name under etc is refused even if it is an old name
    if (GrouperMcpProtectedResources.isProtectedGroupName(realName)
        || (StringUtils.isNotBlank(typedName) && GrouperMcpProtectedResources.isProtectedGroupName(typedName))) {
      String nameToShow = groupNameToShow(authUser, group, typedName);
      return nameToShow == null ? GENERIC_DENIED_MESSAGE
          : GrouperMcpProtectedResources.buildProtectedGroupError(nameToShow);
    }

    if (authUser.isConsentScopeEnforced()) {
      if (requireScopeDimension && !authUser.hasGroupOrFolderReadwriteScope()) {
        return NO_GROUP_OR_FOLDER_SCOPE_MESSAGE;
      }
      if (!authUser.isGroupInReadwriteScope(realName, group.getUuid())) {
        String nameToShow = groupNameToShow(authUser, group, typedName);
        return nameToShow == null ? GENERIC_DENIED_MESSAGE
            : authUser.buildReadwriteScopeDeniedError("group", nameToShow);
      }
    }
    return null;
  }

  /**
   * gate a resolved folder on its real name and uuid
   * @param authUser the caller
   * @param stem the folder the operation will change, null is denied
   * @param typedName the name the caller sent for it, or null if the caller sent an id
   * @return error or null
   */
  public static String checkStem(final GrouperMcpAuthUser authUser, final Stem stem, String typedName) {
    return checkStem(authUser, stem, typedName, true);
  }

  /**
   * see {@link #checkStem(GrouperMcpAuthUser, Stem, String)}
   * @param authUser the caller
   * @param stem the folder the operation will change, null is denied
   * @param typedName the name the caller sent for it, or null if the caller sent an id
   * @param requireScopeDimension true (save, delete and attribute tools, as in 7.9.0) to deny a
   * scoped token whose scope has no values in this dimension; false (member, privilege and
   * template tools) to leave the dimension open when another dimension is scoped, as
   * isGroupInReadwriteScope / isSubjectInReadwriteScope already decide
   * @return error or null
   */
  public static String checkStem(final GrouperMcpAuthUser authUser, final Stem stem, String typedName,
      boolean requireScopeDimension) {
    if (stem == null) {
      return GENERIC_DENIED_MESSAGE;
    }
    String realName = stem.getName();

    if (GrouperMcpProtectedResources.isProtectedStemName(realName)
        || (StringUtils.isNotBlank(typedName) && !StringUtils.equals(typedName, ":")
            && GrouperMcpProtectedResources.isProtectedStemName(typedName))) {
      String nameToShow = stemNameToShow(authUser, stem, typedName);
      return nameToShow == null ? GENERIC_DENIED_MESSAGE
          : GrouperMcpProtectedResources.buildProtectedStemError(nameToShow);
    }

    if (authUser.isConsentScopeEnforced()) {
      if (requireScopeDimension && !authUser.hasGroupOrFolderReadwriteScope()) {
        return NO_GROUP_OR_FOLDER_SCOPE_MESSAGE;
      }
      if (!authUser.isStemInReadwriteScope(realName, stem.getUuid())) {
        String nameToShow = stemNameToShow(authUser, stem, typedName);
        return nameToShow == null ? GENERIC_DENIED_MESSAGE
            : authUser.buildReadwriteScopeDeniedError("folder", nameToShow);
      }
    }
    return null;
  }

  /**
   * gate a resolved attribute definition: on its parent folder, as the tools always have, since
   * the consent scope is by folder
   * @param authUser the caller
   * @param attributeDef the attribute def, null is denied
   * @return error or null
   */
  public static String checkAttributeDef(final GrouperMcpAuthUser authUser, final AttributeDef attributeDef) {
    if (attributeDef == null) {
      return GENERIC_DENIED_MESSAGE;
    }
    Stem parentStem = (Stem) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        try {
          return StemFinder.findByUuid(grouperSession, attributeDef.getStemId(), false);
        } catch (Exception e) {
          LOG.debug("Could not resolve parent folder of attribute def " + attributeDef.getId(), e);
          return null;
        }
      }
    });
    if (parentStem == null) {
      return GENERIC_DENIED_MESSAGE;
    }
    // the parent folder is only named if the caller can see the attribute def
    boolean canViewDef = canViewAttributeDef(authUser, attributeDef);
    String error = checkStem(authUser, parentStem, canViewDef ? parentStem.getName() : null);
    return error;
  }

  // ------------------------------------------------------------------------------------------
  // subjects
  // ------------------------------------------------------------------------------------------

  /**
   * gate a resolved member on its subject source and id (see GrouperOAuthSubjectScope)
   * @param authUser the caller
   * @param member the member, null is denied when the subject scope is limited
   * @param typedValue what the caller sent for the subject, or null if the member was found
   * another way (then the message does not name it)
   * @return error or null
   */
  public static String checkMember(GrouperMcpAuthUser authUser, Member member, String typedValue) {
    if (!authUser.isConsentScopeEnforced()) {
      return null;
    }
    if (!authUser.hasSubjectReadwriteScope()) {
      return NO_SUBJECT_SCOPE_MESSAGE;
    }
    if (!authUser.isSubjectInReadwriteScope(member)) {
      return subjectDeniedMessage(authUser, typedValue);
    }
    return null;
  }

  /**
   * gate the subject a web service subject lookup resolves to.  Pass the lookup the operation
   * will use, so the subject checked is the subject changed
   * @param authUser the caller
   * @param wsSubjectLookup the lookup
   * @param typedValue what the caller sent for the subject, for the message
   * @return error or null
   */
  public static String checkSubjectLookup(GrouperMcpAuthUser authUser, WsSubjectLookup wsSubjectLookup,
      String typedValue) {
    return checkSubjectLookup(authUser, wsSubjectLookup, typedValue, true);
  }

  /**
   * see {@link #checkSubjectLookup(GrouperMcpAuthUser, WsSubjectLookup, String)}
   * @param authUser the caller
   * @param wsSubjectLookup the lookup
   * @param typedValue what the caller sent for the subject, for the message
   * @param requireScopeDimension true (save, delete and attribute tools, as in 7.9.0) to deny a
   * scoped token whose scope has no values in this dimension; false (member, privilege and
   * template tools) to leave the dimension open when another dimension is scoped, as
   * isGroupInReadwriteScope / isSubjectInReadwriteScope already decide
   * @return error or null
   */
  public static String checkSubjectLookup(GrouperMcpAuthUser authUser, WsSubjectLookup wsSubjectLookup,
      String typedValue, boolean requireScopeDimension) {
    if (!authUser.isConsentScopeEnforced()) {
      return null;
    }
    if (requireScopeDimension && !authUser.hasSubjectReadwriteScope()) {
      return NO_SUBJECT_SCOPE_MESSAGE;
    }
    if (!authUser.isSubjectLookupInReadwriteScope(wsSubjectLookup)) {
      return subjectDeniedMessage(authUser, typedValue);
    }
    return null;
  }

  /**
   * @param authUser the caller
   * @param typedValue what the caller sent, or null
   * @return the denial, naming only what the caller sent
   */
  private static String subjectDeniedMessage(GrouperMcpAuthUser authUser, String typedValue) {
    if (StringUtils.isBlank(typedValue)) {
      return "Access denied: the subject is outside your consented read-write scope.";
    }
    return authUser.buildReadwriteScopeDeniedError("subject", typedValue);
  }

  // ------------------------------------------------------------------------------------------
  // memberships and attribute assignments
  // ------------------------------------------------------------------------------------------

  /**
   * gate a membership: its owner (group, folder or attribute def, by the owner ids so a folder or
   * attribute def privilege membership is not skipped) and its member
   * @param authUser the caller
   * @param membership the membership, null is denied
   * @return error or null
   */
  public static String checkMembership(final GrouperMcpAuthUser authUser, final Membership membership) {
    if (membership == null) {
      return GENERIC_DENIED_MESSAGE;
    }
    return (String) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        try {
          String ownerError = null;
          if (StringUtils.isNotBlank(membership.getOwnerGroupId())) {
            Group group = GroupFinder.findByUuid(grouperSession, membership.getOwnerGroupId(), false);
            ownerError = checkGroup(authUser, group, null);
          } else if (StringUtils.isNotBlank(membership.getOwnerStemId())) {
            Stem stem = StemFinder.findByUuid(grouperSession, membership.getOwnerStemId(), false);
            ownerError = checkStem(authUser, stem, null);
          } else if (StringUtils.isNotBlank(membership.getOwnerAttrDefId())) {
            AttributeDef attributeDef = AttributeDefFinder.findById(membership.getOwnerAttrDefId(), false);
            ownerError = checkAttributeDef(authUser, attributeDef);
          } else {
            // no owner: nothing to check against, so deny
            ownerError = GENERIC_DENIED_MESSAGE;
          }
          if (ownerError != null) {
            return ownerError;
          }
          Member member = StringUtils.isBlank(membership.getMemberUuid()) ? null
              : MemberFinder.findByUuid(grouperSession, membership.getMemberUuid(), false);
          if (member == null) {
            return GENERIC_DENIED_MESSAGE;
          }
          return checkMember(authUser, member, null);
        } catch (Exception e) {
          // fail closed, and do not pass the exception text on: it can name the owner
          LOG.debug("Could not resolve membership owner for MCP gate: " + membership.getUuid(), e);
          return GENERIC_DENIED_MESSAGE;
        }
      }
    });
  }

  /**
   * gate an attribute assignment given by id: resolve it as root and gate its real owner.  An
   * assignment on an assignment is followed one level to the assignment it is on (Grouper does
   * not allow deeper), anything deeper or unknown is denied.  Used for both attributeAssignId
   * (the assignment being changed) and ownerAttributeAssignId (the assignment being assigned on)
   * @param authUser the caller
   * @param attributeAssignId the assignment id the caller sent
   * @return error or null
   */
  public static String checkAttributeAssign(final GrouperMcpAuthUser authUser, final String attributeAssignId) {
    if (StringUtils.isBlank(attributeAssignId)) {
      return GENERIC_DENIED_MESSAGE;
    }
    return (String) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        try {
          AttributeAssign attributeAssign = AttributeAssignFinder.findById(attributeAssignId, false);
          return checkAttributeAssignOwner(authUser, attributeAssign, 0);
        } catch (Exception e) {
          // fail closed, without the exception text (it can name the owner)
          LOG.debug("Could not resolve attribute assignment for MCP gate: " + attributeAssignId, e);
          return GENERIC_DENIED_MESSAGE;
        }
      }
    });
  }

  /**
   * gate the owner of an attribute assignment.  Call in a root session
   * @param authUser the caller
   * @param attributeAssign the assignment, null is denied
   * @param depth how many assignment-on-assignment levels were followed already
   * @return error or null
   */
  private static String checkAttributeAssignOwner(GrouperMcpAuthUser authUser,
      AttributeAssign attributeAssign, int depth) {

    if (attributeAssign == null || attributeAssign.getAttributeAssignType() == null) {
      return GENERIC_DENIED_MESSAGE;
    }

    AttributeAssignType type = attributeAssign.getAttributeAssignType();

    // an assignment on an assignment: gate the assignment it is on, one level only
    if (type.isAssignmentOnAssignment()) {
      if (depth > 0) {
        return GENERIC_DENIED_MESSAGE;
      }
      return checkAttributeAssignOwner(authUser, attributeAssign.getOwnerAttributeAssign(), depth + 1);
    }

    switch (type) {
      case group:
        return checkGroup(authUser, attributeAssign.getOwnerGroup(), null);
      case stem:
        return checkStem(authUser, attributeAssign.getOwnerStem(), null);
      case member:
        return checkMemberNotNull(authUser, attributeAssign.getOwnerMember());
      case any_mem: {
        // any_mem has both a group and a member; gate both
        String groupError = checkGroup(authUser, attributeAssign.getOwnerGroup(), null);
        if (groupError != null) {
          return groupError;
        }
        return checkMemberNotNull(authUser, attributeAssign.getOwnerMember());
      }
      case imm_mem:
        return checkMembership(authUser, attributeAssign.getOwnerImmediateMembership());
      case attr_def:
        return checkAttributeDef(authUser, attributeAssign.getOwnerAttributeDef());
      default:
        // an owner type this gate does not know: deny rather than skip
        return GENERIC_DENIED_MESSAGE;
    }
  }

  /**
   * a member owner that cannot be resolved is denied for everyone, not only when the subject
   * scope applies, since nothing else is known about the target
   * @param authUser the caller
   * @param member the member
   * @return error or null
   */
  private static String checkMemberNotNull(GrouperMcpAuthUser authUser, Member member) {
    if (member == null) {
      return GENERIC_DENIED_MESSAGE;
    }
    return checkMember(authUser, member, null);
  }

  // ------------------------------------------------------------------------------------------
  // scope checks on a name, for template inputs
  // ------------------------------------------------------------------------------------------

  /**
   * whether a group name is in the read-write scope, judged on the group it resolves to (current
   * name and uuid) so an alternate name does not count.  A name that resolves to nothing is
   * judged on the string.  Used for template inputs, which have their own protected-folder rule
   * @param authUser the caller
   * @param typedName the name
   * @return true if in scope
   */
  public static boolean isGroupNameInReadwriteScope(GrouperMcpAuthUser authUser, String typedName) {
    Group group = findGroupByNameAsRoot(typedName);
    if (group != null) {
      return authUser.isGroupInReadwriteScope(group.getName(), group.getUuid());
    }
    return authUser.isGroupInReadwriteScope(typedName, null);
  }

  /**
   * whether a folder name is in the read-write scope, judged on the folder it resolves to.  See
   * {@link #isGroupNameInReadwriteScope(GrouperMcpAuthUser, String)}
   * @param authUser the caller
   * @param typedName the name
   * @return true if in scope
   */
  public static boolean isStemNameInReadwriteScope(GrouperMcpAuthUser authUser, String typedName) {
    Stem stem = findStemByNameAsRoot(typedName);
    if (stem != null) {
      return authUser.isStemInReadwriteScope(stem.getName(), stem.getUuid());
    }
    return authUser.isStemInReadwriteScope(typedName, null);
  }

  // ------------------------------------------------------------------------------------------
  // lookups and message wording
  // ------------------------------------------------------------------------------------------

  /**
   * find a group by current or alternate name as root.  Root because whether an object is
   * protected or in scope does not depend on whether the caller can see it
   * @param name the name
   * @return the group or null
   */
  static Group findGroupByNameAsRoot(final String name) {
    if (StringUtils.isBlank(name)) {
      return null;
    }
    return (Group) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        try {
          return GroupFinder.findByName(grouperSession, name, false);
        } catch (Exception e) {
          LOG.debug("Could not resolve group for MCP gate: " + name, e);
          return null;
        }
      }
    });
  }

  /**
   * find a folder by current or alternate name as root.  ":" is the root folder
   * @param name the name
   * @return the folder or null
   */
  static Stem findStemByNameAsRoot(final String name) {
    if (StringUtils.isBlank(name)) {
      return null;
    }
    return (Stem) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        try {
          if (StringUtils.equals(name, ":")) {
            return StemFinder.findRootStem(grouperSession);
          }
          return StemFinder.findByName(grouperSession, name, false);
        } catch (Exception e) {
          LOG.debug("Could not resolve folder for MCP gate: " + name, e);
          return null;
        }
      }
    });
  }

  /**
   * the group name a denial may show: the name the caller sent if it is the real name (nothing
   * new is told), else the real name if the caller can see the group, else null (generic message)
   * @param authUser the caller
   * @param group the group
   * @param typedName what the caller sent, or null
   * @return the name to show, or null
   */
  private static String groupNameToShow(final GrouperMcpAuthUser authUser, final Group group, String typedName) {
    if (StringUtils.equals(typedName, group.getName())) {
      return typedName;
    }
    final Subject subject = authUser.getSubject();
    if (subject == null) {
      return null;
    }
    Boolean canView = (Boolean) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        try {
          return PrivilegeHelper.canView(grouperSession, group, subject);
        } catch (Exception e) {
          LOG.debug("Could not check VIEW for MCP gate message", e);
          return false;
        }
      }
    });
    return canView != null && canView ? group.getName() : null;
  }

  /**
   * the folder name a denial may show, see groupNameToShow
   * @param authUser the caller
   * @param stem the folder
   * @param typedName what the caller sent, or null
   * @return the name to show, or null
   */
  private static String stemNameToShow(GrouperMcpAuthUser authUser, Stem stem, String typedName) {
    if (StringUtils.equals(typedName, stem.getName())
        || (stem.isRootStem() && StringUtils.equals(typedName, ":"))) {
      return typedName;
    }
    Subject subject = authUser.getSubject();
    if (subject == null) {
      return null;
    }
    try {
      return PrivilegeHelper.canViewStemOrObjectInside(subject, stem) ? stem.getName() : null;
    } catch (Exception e) {
      LOG.debug("Could not check folder visibility for MCP gate message", e);
      return null;
    }
  }

  /**
   * whether the caller can see an attribute def (ATTR_VIEW or better)
   * @param authUser the caller
   * @param attributeDef the attribute def
   * @return true if the caller can see it
   */
  private static boolean canViewAttributeDef(GrouperMcpAuthUser authUser, final AttributeDef attributeDef) {
    final Subject subject = authUser.getSubject();
    if (subject == null) {
      return false;
    }
    Boolean canView = (Boolean) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        try {
          return PrivilegeHelper.canAttrView(grouperSession, attributeDef, subject);
        } catch (Exception e) {
          LOG.debug("Could not check ATTR_VIEW for MCP gate message", e);
          return false;
        }
      }
    });
    return canView != null && canView;
  }
}
