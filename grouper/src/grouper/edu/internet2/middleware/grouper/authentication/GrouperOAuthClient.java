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

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.tableIndex.TableIndex;
import edu.internet2.middleware.grouper.tableIndex.TableIndexType;
import edu.internet2.middleware.grouperClient.jdbc.GcPersist;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableClass;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableField;
import edu.internet2.middleware.grouperClient.jdbc.GcSqlAssignPrimaryKey;

/**
 * Entity representing a dynamically registered OAuth client.
 * Stored in the grouper_oauth_client table.
 * Uses GcDbAccess/GcPersistable for database access instead of Hibernate.
 * @author mchyzer
 */
@GcPersistableClass(tableName="grouper_oauth_client", defaultFieldPersist=GcPersist.doPersist)
public class GrouperOAuthClient implements GcSqlAssignPrimaryKey {

  /** table name */
  public static final String TABLE_GROUPER_OAUTH_CLIENT = "grouper_oauth_client";

  /** column */
  public static final String COLUMN_INTERNAL_ID = "internal_id";

  /** column */
  public static final String COLUMN_CLIENT_ID = "client_id";

  /** column */
  public static final String COLUMN_CLIENT_NAME = "client_name";

  /** column */
  public static final String COLUMN_REDIRECT_URIS = "redirect_uris";

  /** column */
  public static final String COLUMN_CLIENT_SECRET = "client_secret";

  /** column */
  public static final String COLUMN_REGISTERED_MICROS = "registered_micros";

  /** column */
  public static final String COLUMN_MEMBER_INTERNAL_ID = "member_internal_id";

  /** column */
  public static final String COLUMN_CODE_COUNT = "code_count";

  /** column */
  public static final String COLUMN_LAST_CODE_MICROS = "last_code_micros";

  /** internal id (auto-incrementing bigint primary key) */
  @GcPersistableField(primaryKey=true, primaryKeyManuallyAssigned=true, columnName="internal_id")
  private long internalId = -1;

  /** client_id - the OAuth client identifier */
  @GcPersistableField(columnName="client_id")
  private String clientId;

  /** client display name */
  @GcPersistableField(columnName="client_name")
  private String clientName;

  /** comma-separated redirect URIs (DB column) */
  @GcPersistableField(columnName="redirect_uris")
  private String redirectUrisDb;

  /** client secret (encrypted with Morph) */
  @GcPersistableField(columnName="client_secret")
  private String clientSecret;

  /** when registered (micros since epoch) */
  @GcPersistableField(columnName="registered_micros")
  private Long registeredMicros;

  /** member internal id of user who registered the client (nullable, set on first code issuance) */
  @GcPersistableField(columnName="member_internal_id")
  private Long memberInternalId;

  /** number of authorization codes issued for this client */
  @GcPersistableField(columnName="code_count")
  private Long codeCount;

  /** when the last authorization code was issued (micros since epoch) */
  @GcPersistableField(columnName="last_code_micros")
  private Long lastCodeMicros;

  public GrouperOAuthClient() {
  }

  /**
   * @see GcSqlAssignPrimaryKey#gcSqlAssignNewPrimaryKeyForInsert()
   */
  @Override
  public boolean gcSqlAssignNewPrimaryKeyForInsert() {
    if (this.internalId != -1) {
      return false;
    }
    this.internalId = TableIndex.reserveId(TableIndexType.oauthClient);
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
   * @return the clientId
   */
  public String getClientId() {
    return this.clientId;
  }

  /**
   * @param clientId the clientId to set
   */
  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  /**
   * @return the clientName
   */
  public String getClientName() {
    return this.clientName;
  }

  /**
   * @param clientName the clientName to set
   */
  public void setClientName(String clientName) {
    this.clientName = clientName;
  }

  /**
   * @return the redirectUrisDb (comma-separated)
   */
  public String getRedirectUrisDb() {
    return this.redirectUrisDb;
  }

  /**
   * @param redirectUrisDb the redirectUrisDb to set (comma-separated)
   */
  public void setRedirectUrisDb(String redirectUrisDb) {
    this.redirectUrisDb = redirectUrisDb;
  }

  /**
   * get redirect URIs as a Set
   * @return the redirect URIs
   */
  public Set<String> getRedirectUris() {
    Set<String> result = new LinkedHashSet<String>();
    if (StringUtils.isNotBlank(this.redirectUrisDb)) {
      for (String uri : this.redirectUrisDb.split(",")) {
        String trimmed = uri.trim();
        if (StringUtils.isNotBlank(trimmed)) {
          result.add(trimmed);
        }
      }
    }
    return result;
  }

  /**
   * set redirect URIs from a Set
   * @param redirectUris
   */
  public void setRedirectUris(Set<String> redirectUris) {
    if (redirectUris == null || redirectUris.isEmpty()) {
      this.redirectUrisDb = null;
    } else {
      this.redirectUrisDb = StringUtils.join(redirectUris, ",");
    }
  }

  /**
   * @return the clientSecret
   */
  public String getClientSecret() {
    return this.clientSecret;
  }

  /**
   * @param clientSecret the clientSecret to set
   */
  public void setClientSecret(String clientSecret) {
    this.clientSecret = clientSecret;
  }

  /**
   * @return the registeredMicros
   */
  public Long getRegisteredMicros() {
    return this.registeredMicros;
  }

  /**
   * @param registeredMicros the registeredMicros to set
   */
  public void setRegisteredMicros(Long registeredMicros) {
    this.registeredMicros = registeredMicros;
  }

  /**
   * @return the memberInternalId
   */
  public Long getMemberInternalId() {
    return this.memberInternalId;
  }

  /**
   * @param memberInternalId the memberInternalId to set
   */
  public void setMemberInternalId(Long memberInternalId) {
    this.memberInternalId = memberInternalId;
  }

  /**
   * @return the codeCount
   */
  public Long getCodeCount() {
    return this.codeCount;
  }

  /**
   * @param codeCount the codeCount to set
   */
  public void setCodeCount(Long codeCount) {
    this.codeCount = codeCount;
  }

  /**
   * @return the lastCodeMicros
   */
  public Long getLastCodeMicros() {
    return this.lastCodeMicros;
  }

  /**
   * @param lastCodeMicros the lastCodeMicros to set
   */
  public void setLastCodeMicros(Long lastCodeMicros) {
    this.lastCodeMicros = lastCodeMicros;
  }
}
