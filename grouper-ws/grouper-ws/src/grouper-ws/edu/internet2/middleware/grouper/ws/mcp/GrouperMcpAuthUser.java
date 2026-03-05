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

}
