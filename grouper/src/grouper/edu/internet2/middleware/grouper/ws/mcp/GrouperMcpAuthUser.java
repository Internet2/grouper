/*******************************************************************************
 * Copyright 2024 Internet2
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.authentication.GrouperOAuthSubjectScope;
import edu.internet2.middleware.grouper.mcp.GrouperToolEntryPath;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup;
import edu.internet2.middleware.subject.Subject;

/**
 * holds the authenticated MCP user identity, regardless of whether
 * they authenticated via OAuth JWT or via normal WS authentication
 * (HTTP Basic, container auth, etc.).
 *
 * <p>When the user authenticated via OAuth JWT, the consent scope booleans
 * reflect what the user approved on the consent page.  When the user
 * authenticated via normal WS auth (HTTP Basic, container, etc.), the
 * consent scope booleans are all false and {@link #isOAuthAuthenticated()}
 * returns false, meaning consent scopes are not applicable and only
 * group membership is used for authorization.</p>
 *
 * @author mchyzer
 */
public class GrouperMcpAuthUser {

  private static final Log LOG = GrouperUtil.getLog(GrouperMcpAuthUser.class);

  /**
   * the resolved Grouper subject for the authenticated user
   */
  private Subject subject;

  /**
   * whether this user authenticated via OAuth JWT (as opposed to WS auth)
   */
  private boolean oAuthAuthenticated;

  /**
   * which front door this call came through.  defaults to mcp, since that is the only one which
   * existed when this class was written and an unset value should not read as the UI
   */
  private GrouperToolEntryPath entryPath = GrouperToolEntryPath.mcp;

  /**
   * whether the user consented to the readonly scope in their OAuth token
   */
  private boolean consentScopeReadonly;

  /**
   * whether the user consented to the readwrite scope in their OAuth token
   */
  private boolean consentScopeReadwrite;

  /**
   * whether the user consented to the sqlReadonly scope in their OAuth token
   */
  private boolean consentScopeSqlReadonly;

  /**
   * whether the user consented to the adminReadonly scope in their OAuth token
   */
  private boolean consentScopeAdminReadonly;

  /**
   * whether the user consented to the adminReadwrite scope in their OAuth token
   */
  private boolean consentScopeAdminReadwrite;

  /**
   * readwrite scope restriction: folder paths the user consented to.
   * null or empty means no folder restriction (all folders in scope),
   * unless consentReadwriteScopeRestricted is true (then empty means nothing allowed).
   */
  private List<String> consentReadwriteFolders;

  /**
   * readwrite scope restriction: group paths the user consented to.
   * null or empty means no group restriction (all groups in scope),
   * unless consentReadwriteScopeRestricted is true (then empty means nothing allowed).
   */
  private List<String> consentReadwriteGroups;

  /**
   * readwrite scope restriction: subjects the user consented to, as sourceId||subjectId keys
   * (see GrouperOAuthSubjectScope).  bare ids or identifiers, from tokens issued before the keys,
   * match nothing.
   * null or empty means no subject restriction (all subjects in scope),
   * unless consentReadwriteScopeRestricted is true (then empty means nothing allowed).
   */
  private List<String> consentReadwriteSubjects;

  /**
   * whether readwrite data-scope restrictions are active.
   * when true, empty restriction lists mean "nothing is allowed" for that category.
   * when false (default), empty lists mean "no restriction" (all allowed).
   * this is set to true when the OAuth consent flow included readwrite data-scope
   * restrictions (i.e., the config property grouper.mcp.oauth.requireReadwriteDataScope
   * is enabled and the user consented to readwrite).
   */
  private boolean consentReadwriteScopeRestricted;

  /**
   * member internal id from grouper_members for the authenticated user
   */
  private long memberInternalId;

  /**
   * member id (uuid) from grouper_members for the authenticated user, or null if not looked up
   */
  private String memberId;

  /**
   * internal id of the OAuth client that was used for authentication,
   * or null if not authenticated via OAuth JWT.
   * This is a soft reference to grouper_oauth_client.internal_id
   * (not a foreign key so audits survive client deletion).
   */
  private Long oauthClientInternalId;

  /**
   * when the JWT was issued (from the "iat" claim), or null if not OAuth authenticated
   */
  private Date jwtIssuedAt;

  /**
   * constructor
   * @param subject the resolved Grouper subject
   */
  public GrouperMcpAuthUser(Subject subject) {
    this.subject = subject;
  }

  /**
   * the resolved Grouper subject for the authenticated user
   * @return the subject
   */
  public Subject getSubject() {
    return this.subject;
  }

  /**
   * member internal id from grouper_members for the authenticated user
   * @return the member internal id
   */
  public long getMemberInternalId() {
    return this.memberInternalId;
  }

  /**
   * set the member internal id
   * @param memberInternalId1
   */
  public void setMemberInternalId(long memberInternalId1) {
    this.memberInternalId = memberInternalId1;
  }

  /**
   * member id (uuid) from grouper_members for the authenticated user
   * @return the member id, or null if not looked up
   */
  public String getMemberId() {
    return this.memberId;
  }

  /**
   * set the member id (uuid)
   * @param memberId1
   */
  public void setMemberId(String memberId1) {
    this.memberId = memberId1;
  }

  /**
   * @return which front door this call came through, never null
   */
  public GrouperToolEntryPath getEntryPath() {
    return this.entryPath;
  }

  /**
   * @param entryPath1 which front door this call came through
   */
  public void setEntryPath(GrouperToolEntryPath entryPath1) {
    this.entryPath = entryPath1 == null ? GrouperToolEntryPath.mcp : entryPath1;
  }

  /**
   * whether the consentScope* flags limit what this caller may do, on top of their group
   * membership.  true for an OAuth caller, where they are the scopes the user consented to for
   * that client, and for the AI agent in the UI, where they are the scope the user chose for the
   * session.  false for an MCP caller who authenticated with web service credentials, who is
   * limited by group membership only
   * @return true if the consent scope applies
   */
  public boolean isConsentScopeEnforced() {
    return this.oAuthAuthenticated || this.entryPath == GrouperToolEntryPath.ui;
  }

  /**
   * whether this user authenticated via OAuth JWT
   * @return true if OAuth JWT authenticated
   */
  public boolean isOAuthAuthenticated() {
    return this.oAuthAuthenticated;
  }

  /**
   * set whether this user authenticated via OAuth JWT
   * @param oAuthAuthenticated1
   */
  public void setOAuthAuthenticated(boolean oAuthAuthenticated1) {
    this.oAuthAuthenticated = oAuthAuthenticated1;
  }

  /**
   * whether the user consented to the readonly scope
   * @return true if readonly was consented
   */
  public boolean isConsentScopeReadonly() {
    return this.consentScopeReadonly;
  }

  /**
   * set whether the user consented to the readonly scope
   * @param consentScopeReadonly1
   */
  public void setConsentScopeReadonly(boolean consentScopeReadonly1) {
    this.consentScopeReadonly = consentScopeReadonly1;
  }

  /**
   * whether the user consented to the readwrite scope
   * @return true if readwrite was consented
   */
  public boolean isConsentScopeReadwrite() {
    return this.consentScopeReadwrite;
  }

  /**
   * set whether the user consented to the readwrite scope
   * @param consentScopeReadwrite1
   */
  public void setConsentScopeReadwrite(boolean consentScopeReadwrite1) {
    this.consentScopeReadwrite = consentScopeReadwrite1;
  }

  /**
   * whether the user consented to the sqlReadonly scope
   * @return true if sqlReadonly was consented
   */
  public boolean isConsentScopeSqlReadonly() {
    return this.consentScopeSqlReadonly;
  }

  /**
   * set whether the user consented to the sqlReadonly scope
   * @param consentScopeSqlReadonly1
   */
  public void setConsentScopeSqlReadonly(boolean consentScopeSqlReadonly1) {
    this.consentScopeSqlReadonly = consentScopeSqlReadonly1;
  }

  /**
   * whether the user consented to the adminReadonly scope
   * @return true if adminReadonly was consented
   */
  public boolean isConsentScopeAdminReadonly() {
    return this.consentScopeAdminReadonly;
  }

  /**
   * set whether the user consented to the adminReadonly scope
   * @param consentScopeAdminReadonly1
   */
  public void setConsentScopeAdminReadonly(boolean consentScopeAdminReadonly1) {
    this.consentScopeAdminReadonly = consentScopeAdminReadonly1;
  }

  /**
   * whether the user consented to the adminReadwrite scope
   * @return true if adminReadwrite was consented
   */
  public boolean isConsentScopeAdminReadwrite() {
    return this.consentScopeAdminReadwrite;
  }

  /**
   * set whether the user consented to the adminReadwrite scope
   * @param consentScopeAdminReadwrite1
   */
  public void setConsentScopeAdminReadwrite(boolean consentScopeAdminReadwrite1) {
    this.consentScopeAdminReadwrite = consentScopeAdminReadwrite1;
  }

  /**
   * internal id of the OAuth client, or null if not OAuth authenticated
   * @return the oauth client internal id
   */
  public Long getOauthClientInternalId() {
    return this.oauthClientInternalId;
  }

  /**
   * set the oauth client internal id
   * @param oauthClientInternalId1
   */
  public void setOauthClientInternalId(Long oauthClientInternalId1) {
    this.oauthClientInternalId = oauthClientInternalId1;
  }

  /**
   * readwrite scope restriction: folder paths the user consented to.
   * null or empty means no folder restriction, unless
   * consentReadwriteScopeRestricted is true (then empty means nothing allowed).
   * @return the folder paths
   */
  public List<String> getConsentReadwriteFolders() {
    return this.consentReadwriteFolders;
  }

  /**
   * set the readwrite folder scope restriction
   * @param consentReadwriteFolders1
   */
  public void setConsentReadwriteFolders(List<String> consentReadwriteFolders1) {
    this.consentReadwriteFolders = consentReadwriteFolders1;
  }

  /**
   * readwrite scope restriction: group paths the user consented to.
   * null or empty means no group restriction, unless
   * consentReadwriteScopeRestricted is true (then empty means nothing allowed).
   * @return the group paths
   */
  public List<String> getConsentReadwriteGroups() {
    return this.consentReadwriteGroups;
  }

  /**
   * set the readwrite group scope restriction
   * @param consentReadwriteGroups1
   */
  public void setConsentReadwriteGroups(List<String> consentReadwriteGroups1) {
    this.consentReadwriteGroups = consentReadwriteGroups1;
  }

  /**
   * readwrite scope restriction: subjects the user consented to, as sourceId||subjectId keys
   * (see GrouperOAuthSubjectScope).  bare ids or identifiers, from tokens issued before the keys,
   * match nothing.
   * null or empty means no subject restriction, unless
   * consentReadwriteScopeRestricted is true (then empty means nothing allowed).
   * @return the subject IDs
   */
  public List<String> getConsentReadwriteSubjects() {
    return this.consentReadwriteSubjects;
  }

  /**
   * set the readwrite subject scope restriction
   * @param consentReadwriteSubjects1
   */
  public void setConsentReadwriteSubjects(List<String> consentReadwriteSubjects1) {
    this.consentReadwriteSubjects = consentReadwriteSubjects1;
  }

  /**
   * whether readwrite data-scope restrictions are active.
   * when true, empty restriction lists mean "nothing is allowed" for that category.
   * when false, empty lists mean "no restriction" (all allowed).
   * @return true if restrictions are active
   */
  public boolean isConsentReadwriteScopeRestricted() {
    return this.consentReadwriteScopeRestricted;
  }

  /**
   * set whether readwrite data-scope restrictions are active
   * @param consentReadwriteScopeRestricted1
   */
  public void setConsentReadwriteScopeRestricted(boolean consentReadwriteScopeRestricted1) {
    this.consentReadwriteScopeRestricted = consentReadwriteScopeRestricted1;
  }

  /**
   * Check if a group name is within the readwrite scope restriction.
   * First checks the name directly against the scope lists. If no match is found,
   * resolves the group to get its UUID and checks that as well. This allows
   * the scope list to contain either group names or UUIDs.
   * @param groupName the full group name (ID path)
   * @return true if the group is in the readwrite scope
   */
  public boolean isGroupInReadwriteScope(String groupName) {
    // first try with just the name (fast path, no DB lookup)
    if (isGroupInReadwriteScope(groupName, null)) {
      return true;
    }
    // if name didn't match, resolve the group to get the UUID and try that
    try {
      Group group = GroupFinder.findByName(groupName, false);
      if (group != null) {
        return isGroupInReadwriteScope(null, group.getUuid());
      }
    } catch (Exception e) {
      LOG.debug("Could not resolve group for scope check: " + groupName, e);
    }
    return false;
  }

  /**
   * Check if a group is within the readwrite scope restriction by checking
   * both the group name and UUID against the scope lists.
   * If no folder/group restrictions are set: when consentReadwriteScopeRestricted
   * is false all groups are in scope; when true, groups are in scope only if
   * at least one other dimension (subjects) has values (meaning this dimension
   * is simply unscoped, not blocked).
   * A group is in scope if its name or UUID matches a consented group entry,
   * or if its name starts with a consented folder path followed by ":".
   * @param groupName the full group name (ID path), may be null
   * @param groupUuid the group UUID, may be null
   * @return true if the group is in the readwrite scope
   */
  public boolean isGroupInReadwriteScope(String groupName, String groupUuid) {
    boolean hasFolders = this.consentReadwriteFolders != null
        && !this.consentReadwriteFolders.isEmpty();
    boolean hasGroups = this.consentReadwriteGroups != null
        && !this.consentReadwriteGroups.isEmpty();

    // if no folder/group restrictions:
    // - if scope restrictions are not active, all groups are allowed
    // - if scope restrictions are active, groups are allowed only if
    //   at least one other dimension has values (this dimension is unscoped)
    if (!hasFolders && !hasGroups) {
      return !this.consentReadwriteScopeRestricted || hasAnyReadwriteScopeValues();
    }

    // check group name against specific group list
    if (hasGroups) {
      for (String consentedGroup : this.consentReadwriteGroups) {
        if (StringUtils.isNotBlank(groupName) && consentedGroup.equals(groupName)) {
          return true;
        }
        // also check UUID against the group list (scope may contain UUIDs)
        if (StringUtils.isNotBlank(groupUuid) && consentedGroup.equals(groupUuid)) {
          return true;
        }
      }
    }

    // check folder containment (only applicable to name, not UUID)
    if (hasFolders && StringUtils.isNotBlank(groupName)) {
      for (String consentedFolder : this.consentReadwriteFolders) {
        // strip trailing colon if someone typed "folder:" instead of "folder"
        String folder = StringUtils.stripEnd(consentedFolder, ":");
        if (groupName.startsWith(folder + ":")) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Check if a stem (folder) name is within the readwrite scope restriction.
   * First checks the name directly against the scope lists. If no match is found,
   * resolves the stem to get its UUID and checks that as well. This allows
   * the scope list to contain either stem names or UUIDs.
   * @param stemName the full stem name (ID path)
   * @return true if the stem is in the readwrite scope
   */
  public boolean isStemInReadwriteScope(String stemName) {
    // first try with just the name (fast path, no DB lookup)
    if (isStemInReadwriteScope(stemName, null)) {
      return true;
    }
    // if name didn't match, resolve the stem to get the UUID and try that
    try {
      Stem stem = StemFinder.findByName(stemName, false);
      if (stem != null) {
        return isStemInReadwriteScope(null, stem.getUuid());
      }
    } catch (Exception e) {
      LOG.debug("Could not resolve stem for scope check: " + stemName, e);
    }
    return false;
  }

  /**
   * Check if a stem (folder) is within the readwrite scope restriction
   * by checking both the stem name and UUID against the scope lists.
   * If no folder restrictions are set: when consentReadwriteScopeRestricted
   * is false all stems are in scope; when true, stems are in scope only if
   * at least one other dimension has values (meaning this dimension
   * is simply unscoped, not blocked).
   * A stem is in scope if its name or UUID matches a consented folder entry,
   * or if its name starts with a consented folder path followed by ":".
   * @param stemName the full stem name (ID path), may be null
   * @param stemUuid the stem UUID, may be null
   * @return true if the stem is in the readwrite scope
   */
  public boolean isStemInReadwriteScope(String stemName, String stemUuid) {
    if (this.consentReadwriteFolders == null
        || this.consentReadwriteFolders.isEmpty()) {
      // if scope restrictions are not active, all stems are allowed
      // if scope restrictions are active, stems are allowed only if
      // at least one other dimension has values (this dimension is unscoped)
      return !this.consentReadwriteScopeRestricted || hasAnyReadwriteScopeValues();
    }

    for (String consentedFolder : this.consentReadwriteFolders) {
      // strip trailing colon if someone typed "folder:" instead of "folder"
      String folder = StringUtils.stripEnd(consentedFolder, ":");
      // stem name matches a consented folder or is under a consented folder
      if (StringUtils.isNotBlank(stemName)
          && (folder.equals(stemName) || stemName.startsWith(folder + ":"))) {
        return true;
      }
      // also check UUID against the folder list (scope may contain UUIDs)
      if (StringUtils.isNotBlank(stemUuid) && consentedFolder.equals(stemUuid)) {
        return true;
      }
    }

    return false;
  }

  /**
   * whether the subject dimension of the readwrite scope lets every subject through without
   * looking at which subject it is: no subject list, and either the scope is not restricted or
   * another dimension (folders, groups) has values, so this dimension is simply unscoped.
   * @return true if any subject is in scope
   */
  private boolean isSubjectDimensionOpen() {
    if (this.consentReadwriteSubjects == null
        || this.consentReadwriteSubjects.isEmpty()) {
      // if scope restrictions are not active, all subjects are allowed
      // if scope restrictions are active, subjects are allowed only if
      // at least one other dimension has values (this dimension is unscoped)
      return !this.consentReadwriteScopeRestricted || hasAnyReadwriteScopeValues();
    }
    return false;
  }

  /**
   * whether the readwrite scope limits which subjects may be acted on, so a check has to look at
   * the subject.  false when the subject dimension is open (see isSubjectInReadwriteScope).  Used
   * to decide whether an operation with implicit effects on subjects, such as group_add_member
   * with replaceAllExisting, has to check every subject it would touch
   * @return true if the subject scope is limited
   */
  public boolean isSubjectReadwriteScopeLimited() {
    return !isSubjectDimensionOpen();
  }

  /**
   * Check if a subject, given as a subject id or identifier with no source, is within the
   * readwrite scope restriction.  The scope holds sourceId||subjectId keys (see
   * GrouperOAuthSubjectScope), so the value is resolved to exactly one subject first (by id or
   * identifier, in all sources) and that subject's key is compared.  A value which does not
   * resolve, or matches subjects in more than one source, is not in scope: the typed string is
   * never compared on its own, since an identifier such as an email can be shared across sources.
   * Callers which know the source should use {@link #isSubjectLookupInReadwriteScope(WsSubjectLookup)}
   * with the same lookup the operation uses.
   * @param subjectIdOrIdentifier the subject ID or identifier to check
   * @return true if the subject is in the readwrite scope
   */
  public boolean isSubjectInReadwriteScope(String subjectIdOrIdentifier) {
    if (isSubjectDimensionOpen()) {
      return true;
    }
    if (StringUtils.isBlank(subjectIdOrIdentifier)) {
      return false;
    }
    Subject subject = null;
    try {
      subject = SubjectFinder.findByIdOrIdentifier(subjectIdOrIdentifier, false);
    } catch (Exception e) {
      // not unique across sources, source unavailable, etc: fail closed
      LOG.debug("Could not resolve subject for scope check: " + subjectIdOrIdentifier, e);
      return false;
    }
    return isSubjectInReadwriteScope(subject);
  }

  /**
   * Check if a resolved subject is within the readwrite scope restriction, by its source and id.
   * If no subject restrictions are set: when consentReadwriteScopeRestricted is false all
   * subjects are in scope; when true, subjects are in scope only if at least one other dimension
   * has values (meaning this dimension is simply unscoped, not blocked).
   * @param subject the subject the operation acts on, null means not in scope (when restricted)
   * @return true if the subject is in the readwrite scope
   */
  public boolean isSubjectInReadwriteScope(Subject subject) {
    if (isSubjectDimensionOpen()) {
      return true;
    }
    return GrouperOAuthSubjectScope.isSubjectInScope(this.consentReadwriteSubjects, subject);
  }

  /**
   * Check if a member is within the readwrite scope restriction, by its subject source and id
   * (no subject lookup needed)
   * @param member the member the operation acts on, null means not in scope (when restricted)
   * @return true if the member's subject is in the readwrite scope
   */
  public boolean isSubjectInReadwriteScope(Member member) {
    if (isSubjectDimensionOpen()) {
      return true;
    }
    if (member == null) {
      return false;
    }
    return GrouperOAuthSubjectScope.isSubjectInScope(this.consentReadwriteSubjects,
        member.getSubjectSourceId(), member.getSubjectId());
  }

  /**
   * Check if the subject a web service subject lookup resolves to is within the readwrite scope
   * restriction.  Pass the same lookup object the operation will use: it caches the subject it
   * resolved, so the subject checked here is the subject the operation acts on (same value, id
   * type and source).  A lookup which does not resolve to exactly one subject is not in scope.
   * @param wsSubjectLookup the lookup the operation will use
   * @return true if the subject is in the readwrite scope
   */
  public boolean isSubjectLookupInReadwriteScope(WsSubjectLookup wsSubjectLookup) {
    if (isSubjectDimensionOpen()) {
      return true;
    }
    if (wsSubjectLookup == null) {
      return false;
    }
    Subject subject = null;
    try {
      subject = wsSubjectLookup.retrieveSubject();
    } catch (Exception e) {
      LOG.debug("Could not resolve subject lookup for scope check: " + wsSubjectLookup, e);
      return false;
    }
    return isSubjectInReadwriteScope(subject);
  }

  /**
   * Check if any of the given subject scope keys (sourceId||subjectId, see
   * GrouperOAuthSubjectScope) are within the readwrite scope restriction.  Bare subject ids or
   * identifiers never match (in the list or in the scope): a subject is only identified by its
   * source and id.
   * If no subject restrictions are set: when consentReadwriteScopeRestricted
   * is false all subjects are in scope; when true, subjects are in scope only if
   * at least one other dimension has values (meaning this dimension
   * is simply unscoped, not blocked).
   * @param subjectKeys sourceId||subjectId keys for the subject
   * @return true if any of the keys matches the readwrite scope
   */
  public boolean isSubjectInReadwriteScope(List<String> subjectKeys) {
    if (isSubjectDimensionOpen()) {
      return true;
    }

    for (String candidate : GrouperUtil.nonNull(subjectKeys)) {
      if (GrouperOAuthSubjectScope.isSubjectKey(candidate)
          && this.consentReadwriteSubjects.contains(candidate)) {
        return true;
      }
    }
    return false;
  }

  /**
   * when the JWT was issued (from the "iat" claim), or null if not OAuth authenticated
   * @return the issued-at date
   */
  public Date getJwtIssuedAt() {
    return this.jwtIssuedAt;
  }

  /**
   * set when the JWT was issued
   * @param jwtIssuedAt1
   */
  public void setJwtIssuedAt(Date jwtIssuedAt1) {
    this.jwtIssuedAt = jwtIssuedAt1;
  }

  /**
   * build a scope denial error message that includes when the JWT was issued,
   * so the user can tell if they need to re-authenticate to get updated scopes.
   * @param entityType e.g. "group", "stem", "subject"
   * @param entityName the name of the entity that was denied
   * @return the error message string
   */
  public String buildReadwriteScopeDeniedError(String entityType, String entityName) {
    StringBuilder sb = new StringBuilder();
    sb.append("Access denied: ").append(entityType).append(" '").append(entityName)
        .append("' is outside your consented read-write scope.");
    if (this.jwtIssuedAt != null) {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");
      sb.append(" Token issued at: ").append(sdf.format(this.jwtIssuedAt)).append(".");
    }
    return sb.toString();
  }

  /**
   * check if the user has any group or folder values in the readwrite scope.
   * when scope is restricted and there are no group or folder scope values,
   * returns false, meaning tools like group_save, group_delete, folder_delete
   * should not be available.
   * when scope is not restricted, always returns true.
   * @return true if the user has group or folder scope or is not scope restricted
   */
  public boolean hasGroupOrFolderReadwriteScope() {
    if (!this.consentReadwriteScopeRestricted) {
      return true;
    }
    return (this.consentReadwriteFolders != null && !this.consentReadwriteFolders.isEmpty())
        || (this.consentReadwriteGroups != null && !this.consentReadwriteGroups.isEmpty());
  }

  /**
   * check if the user has any subject values in the readwrite scope.
   * when scope is restricted and there are no subject scope values,
   * returns false, meaning subject-owner operations in attribute_assignment_save
   * should be denied.
   * when scope is not restricted, always returns true.
   * @return true if the user has subject scope or is not scope restricted
   */
  public boolean hasSubjectReadwriteScope() {
    if (!this.consentReadwriteScopeRestricted) {
      return true;
    }
    return this.consentReadwriteSubjects != null && !this.consentReadwriteSubjects.isEmpty();
  }

  /**
   * check if any of the readwrite scope lists (folders, groups, subjects) have values.
   * used to determine if an unscoped dimension should be open (at least one other
   * dimension is scoped) or blocked (nothing is scoped at all).
   * @return true if at least one scope dimension has values
   */
  private boolean hasAnyReadwriteScopeValues() {
    return (this.consentReadwriteFolders != null && !this.consentReadwriteFolders.isEmpty())
        || (this.consentReadwriteGroups != null && !this.consentReadwriteGroups.isEmpty())
        || (this.consentReadwriteSubjects != null && !this.consentReadwriteSubjects.isEmpty());
  }

}
