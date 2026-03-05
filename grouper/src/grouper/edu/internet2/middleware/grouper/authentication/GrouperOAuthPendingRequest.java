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
package edu.internet2.middleware.grouper.authentication;

import edu.internet2.middleware.grouper.tableIndex.TableIndex;
import edu.internet2.middleware.grouper.tableIndex.TableIndexType;
import edu.internet2.middleware.grouperClient.jdbc.GcPersist;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableClass;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableField;
import edu.internet2.middleware.grouperClient.jdbc.GcSqlAssignPrimaryKey;

/**
 * Entity representing a pending OAuth authorization request.
 * Created when the client sends the user to the authorize endpoint,
 * consumed when the user approves or denies.
 * Stored in the grouper_oauth_pend_authz_req table.
 * Uses GcDbAccess/GcPersistable for database access instead of Hibernate.
 * @author mchyzer
 */
@GcPersistableClass(tableName="grouper_oauth_pend_authz_req", defaultFieldPersist=GcPersist.doPersist)
public class GrouperOAuthPendingRequest implements GcSqlAssignPrimaryKey {

  /** table name */
  public static final String TABLE_GROUPER_OAUTH_PEND_AUTHZ_REQ = "grouper_oauth_pend_authz_req";

  /** column */
  public static final String COLUMN_INTERNAL_ID = "internal_id";

  /** column */
  public static final String COLUMN_REQUEST_ID = "request_id";

  /** column */
  public static final String COLUMN_OAUTH_CLIENT_INTERNAL_ID = "oauth_client_internal_id";

  /** column */
  public static final String COLUMN_REDIRECT_URI = "redirect_uri";

  /** column */
  public static final String COLUMN_CODE_CHALLENGE = "code_challenge";

  /** column */
  public static final String COLUMN_CODE_CHALLENGE_METHOD = "code_challenge_method";

  /** column */
  public static final String COLUMN_STATE = "state";

  /** column */
  public static final String COLUMN_SCOPE = "scope";

  /** column */
  public static final String COLUMN_CREATED_MICROS = "created_micros";

  /** column */
  public static final String COLUMN_EXPIRES_MICROS = "expires_micros";

  /** internal id (auto-incrementing bigint primary key) */
  @GcPersistableField(primaryKey=true, primaryKeyManuallyAssigned=true, columnName="internal_id")
  private long internalId = -1;

  /** public-facing request id */
  @GcPersistableField(columnName="request_id")
  private String requestId;

  /** oauth client internal id (from grouper_oauth_client) */
  @GcPersistableField(columnName="oauth_client_internal_id")
  private long oauthClientInternalId;

  /** redirect URI */
  @GcPersistableField(columnName="redirect_uri")
  private String redirectUri;

  /** PKCE code challenge */
  @GcPersistableField(columnName="code_challenge")
  private String codeChallenge;

  /** PKCE code challenge method (S256) */
  @GcPersistableField(columnName="code_challenge_method")
  private String codeChallengeMethod;

  /** OAuth state parameter */
  private String state;

  /** requested scope */
  private String scope;

  /** when created (micros since epoch) */
  @GcPersistableField(columnName="created_micros")
  private Long createdMicros;

  /** when it expires (micros since epoch) */
  @GcPersistableField(columnName="expires_micros")
  private Long expiresMicros;

  public GrouperOAuthPendingRequest() {
  }

  /**
   * @see GcSqlAssignPrimaryKey#gcSqlAssignNewPrimaryKeyForInsert()
   */
  @Override
  public boolean gcSqlAssignNewPrimaryKeyForInsert() {
    if (this.internalId != -1) {
      return false;
    }
    this.internalId = TableIndex.reserveId(TableIndexType.oauthPendingRequest);
    return true;
  }

  /**
   * @return the internalId
   */
  public long getInternalId() {
    return this.internalId;
  }

  /**
   * @param internalId the internalId to set
   */
  public void setInternalId(long internalId) {
    this.internalId = internalId;
  }

  /**
   * @return the requestId
   */
  public String getRequestId() {
    return this.requestId;
  }

  /**
   * @param requestId the requestId to set
   */
  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  /**
   * @return the oauthClientInternalId
   */
  public long getOauthClientInternalId() {
    return this.oauthClientInternalId;
  }

  /**
   * @param oauthClientInternalId the oauthClientInternalId to set
   */
  public void setOauthClientInternalId(long oauthClientInternalId) {
    this.oauthClientInternalId = oauthClientInternalId;
  }

  /**
   * @return the redirectUri
   */
  public String getRedirectUri() {
    return this.redirectUri;
  }

  /**
   * @param redirectUri the redirectUri to set
   */
  public void setRedirectUri(String redirectUri) {
    this.redirectUri = redirectUri;
  }

  /**
   * @return the codeChallenge
   */
  public String getCodeChallenge() {
    return this.codeChallenge;
  }

  /**
   * @param codeChallenge the codeChallenge to set
   */
  public void setCodeChallenge(String codeChallenge) {
    this.codeChallenge = codeChallenge;
  }

  /**
   * @return the codeChallengeMethod
   */
  public String getCodeChallengeMethod() {
    return this.codeChallengeMethod;
  }

  /**
   * @param codeChallengeMethod the codeChallengeMethod to set
   */
  public void setCodeChallengeMethod(String codeChallengeMethod) {
    this.codeChallengeMethod = codeChallengeMethod;
  }

  /**
   * @return the state
   */
  public String getState() {
    return this.state;
  }

  /**
   * @param state the state to set
   */
  public void setState(String state) {
    this.state = state;
  }

  /**
   * @return the scope
   */
  public String getScope() {
    return this.scope;
  }

  /**
   * @param scope the scope to set
   */
  public void setScope(String scope) {
    this.scope = scope;
  }

  /**
   * @return the createdMicros
   */
  public Long getCreatedMicros() {
    return this.createdMicros;
  }

  /**
   * @param createdMicros the createdMicros to set
   */
  public void setCreatedMicros(Long createdMicros) {
    this.createdMicros = createdMicros;
  }

  /**
   * @return the expiresMicros
   */
  public Long getExpiresMicros() {
    return this.expiresMicros;
  }

  /**
   * @param expiresMicros the expiresMicros to set
   */
  public void setExpiresMicros(Long expiresMicros) {
    this.expiresMicros = expiresMicros;
  }

}
