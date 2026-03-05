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
 * Entity representing an issued OAuth authorization code.
 * Created when the user approves in the UI, consumed when the client exchanges it for a token.
 * Stored in the grouper_oauth_code table.
 * Uses GcDbAccess/GcPersistable for database access instead of Hibernate.
 * @author mchyzer
 */
@GcPersistableClass(tableName="grouper_oauth_code", defaultFieldPersist=GcPersist.doPersist)
public class GrouperOAuthCode implements GcSqlAssignPrimaryKey {

  /** table name */
  public static final String TABLE_GROUPER_OAUTH_CODE = "grouper_oauth_code";

  /** column */
  public static final String COLUMN_INTERNAL_ID = "internal_id";

  /** column */
  public static final String COLUMN_CODE = "code";

  /** column */
  public static final String COLUMN_OAUTH_CLIENT_INTERNAL_ID = "oauth_client_internal_id";

  /** column */
  public static final String COLUMN_REDIRECT_URI = "redirect_uri";

  /** column */
  public static final String COLUMN_CODE_CHALLENGE = "code_challenge";

  /** column */
  public static final String COLUMN_CODE_CHALLENGE_METHOD = "code_challenge_method";

  /** column */
  public static final String COLUMN_MEMBER_INTERNAL_ID = "member_internal_id";

  /** column */
  public static final String COLUMN_IS_USED = "is_used";

  /** column */
  public static final String COLUMN_CREATED_MICROS = "created_micros";

  /** column */
  public static final String COLUMN_EXPIRES_MICROS = "expires_micros";

  /** column */
  public static final String COLUMN_CONSENT_DETAILS = "consent_details";

  /** internal id (auto-incrementing bigint primary key) */
  @GcPersistableField(primaryKey=true, primaryKeyManuallyAssigned=true, columnName="internal_id")
  private long internalId = -1;

  /** the authorization code */
  private String code;

  /** which client (internal_id from grouper_oauth_client) */
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

  /** member internal id of user who approved */
  @GcPersistableField(columnName="member_internal_id")
  private long memberInternalId;

  /** T or F for is_used (DB column) */
  @GcPersistableField(columnName="is_used")
  private String isUsedDb;

  /** whether this code has been used (in-memory only) */
  @GcPersistableField(persist=GcPersist.dontPersist)
  private boolean used;

  /** when created (micros since epoch) */
  @GcPersistableField(columnName="created_micros")
  private Long createdMicros;

  /** when it expires (micros since epoch) */
  @GcPersistableField(columnName="expires_micros")
  private Long expiresMicros;

  /** JSON object with consent details (granted scopes, etc.) */
  @GcPersistableField(columnName="consent_details")
  private String consentDetails;

  public GrouperOAuthCode() {
  }

  /**
   * @see GcSqlAssignPrimaryKey#gcSqlAssignNewPrimaryKeyForInsert()
   */
  @Override
  public boolean gcSqlAssignNewPrimaryKeyForInsert() {
    if (this.internalId != -1) {
      return false;
    }
    this.internalId = TableIndex.reserveId(TableIndexType.oauthCode);
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
   * @return the code
   */
  public String getCode() {
    return this.code;
  }

  /**
   * @param code the code to set
   */
  public void setCode(String code) {
    this.code = code;
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
   * @return the memberInternalId
   */
  public long getMemberInternalId() {
    return this.memberInternalId;
  }

  /**
   * @param memberInternalId the memberInternalId to set
   */
  public void setMemberInternalId(long memberInternalId) {
    this.memberInternalId = memberInternalId;
  }

  /**
   * @return the isUsedDb
   */
  public String getIsUsedDb() {
    return this.isUsedDb;
  }

  /**
   * @param isUsedDb the isUsedDb to set
   */
  public void setIsUsedDb(String isUsedDb) {
    this.isUsedDb = isUsedDb;
    this.used = "T".equals(isUsedDb);
  }

  /**
   * @return the used
   */
  public boolean isUsed() {
    return this.used;
  }

  /**
   * @param used the used to set
   */
  public void setUsed(boolean used) {
    this.used = used;
    this.isUsedDb = used ? "T" : "F";
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

  /**
   * @return the consentDetails JSON string
   */
  public String getConsentDetails() {
    return this.consentDetails;
  }

  /**
   * @param consentDetails the consentDetails JSON string to set
   */
  public void setConsentDetails(String consentDetails) {
    this.consentDetails = consentDetails;
  }

}
