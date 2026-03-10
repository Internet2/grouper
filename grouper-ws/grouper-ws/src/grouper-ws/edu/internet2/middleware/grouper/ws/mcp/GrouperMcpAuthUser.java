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

import java.util.List;

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

  /**
   * the resolved Grouper subject for the authenticated user
   */
  private Subject subject;

  /**
   * whether this user authenticated via OAuth JWT (as opposed to WS auth)
   */
  private boolean oAuthAuthenticated;

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
   * readwrite scope restriction: subject IDs/identifiers the user consented to.
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
   * internal id of the OAuth client that was used for authentication,
   * or null if not authenticated via OAuth JWT.
   * This is a soft reference to grouper_oauth_client.internal_id
   * (not a foreign key so audits survive client deletion).
   */
  private Long oauthClientInternalId;

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
   * readwrite scope restriction: subject IDs/identifiers the user consented to.
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
   * If no folder/group restrictions are set: when consentReadwriteScopeRestricted
   * is false all groups are in scope; when true, groups are in scope only if
   * at least one other dimension (subjects) has values (meaning this dimension
   * is simply unscoped, not blocked).
   * A group is in scope if it matches a consented group path, or if its name
   * starts with a consented folder path followed by ":".
   * @param groupName the full group name (ID path)
   * @return true if the group is in the readwrite scope
   */
  public boolean isGroupInReadwriteScope(String groupName) {
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

    // check specific group list
    if (hasGroups) {
      for (String consentedGroup : this.consentReadwriteGroups) {
        if (consentedGroup.equals(groupName)) {
          return true;
        }
      }
    }

    // check folder containment
    if (hasFolders) {
      for (String consentedFolder : this.consentReadwriteFolders) {
        if (groupName.startsWith(consentedFolder + ":")) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Check if a stem (folder) name is within the readwrite scope restriction.
   * If no folder restrictions are set: when consentReadwriteScopeRestricted
   * is false all stems are in scope; when true, stems are in scope only if
   * at least one other dimension has values (meaning this dimension
   * is simply unscoped, not blocked).
   * A stem is in scope if it matches a consented folder path, or if its name
   * starts with a consented folder path followed by ":".
   * @param stemName the full stem name (ID path)
   * @return true if the stem is in the readwrite scope
   */
  public boolean isStemInReadwriteScope(String stemName) {
    if (this.consentReadwriteFolders == null
        || this.consentReadwriteFolders.isEmpty()) {
      // if scope restrictions are not active, all stems are allowed
      // if scope restrictions are active, stems are allowed only if
      // at least one other dimension has values (this dimension is unscoped)
      return !this.consentReadwriteScopeRestricted || hasAnyReadwriteScopeValues();
    }

    for (String consentedFolder : this.consentReadwriteFolders) {
      // stem is a consented folder or under a consented folder
      if (consentedFolder.equals(stemName) || stemName.startsWith(consentedFolder + ":")) {
        return true;
      }
    }

    return false;
  }

  /**
   * Check if a subject ID or identifier is within the readwrite scope restriction.
   * If no subject restrictions are set: when consentReadwriteScopeRestricted
   * is false all subjects are in scope; when true, subjects are in scope only if
   * at least one other dimension has values (meaning this dimension
   * is simply unscoped, not blocked).
   * @param subjectIdOrIdentifier the subject ID or identifier to check
   * @return true if the subject is in the readwrite scope
   */
  public boolean isSubjectInReadwriteScope(String subjectIdOrIdentifier) {
    if (this.consentReadwriteSubjects == null
        || this.consentReadwriteSubjects.isEmpty()) {
      // if scope restrictions are not active, all subjects are allowed
      // if scope restrictions are active, subjects are allowed only if
      // at least one other dimension has values (this dimension is unscoped)
      return !this.consentReadwriteScopeRestricted || hasAnyReadwriteScopeValues();
    }

    for (String consentedSubject : this.consentReadwriteSubjects) {
      if (consentedSubject.equals(subjectIdOrIdentifier)) {
        return true;
      }
    }
    return false;
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
