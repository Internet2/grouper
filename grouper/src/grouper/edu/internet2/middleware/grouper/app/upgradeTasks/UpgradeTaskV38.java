/**
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
 */
package edu.internet2.middleware.grouper.app.upgradeTasks;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
import edu.internet2.middleware.grouper.authentication.GrouperOAuthSigningKey;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

/**
 * Upgrade task to create OAuth tables (grouper_oauth_client, grouper_oauth_code,
 * grouper_oauth_pend_authz_req) and MCP audit log table (grouper_mcp_tool_log)
 * and their indexes.
 * @author mchyzer
 */
public class UpgradeTaskV38 implements UpgradeTasksInterface {

  @Override
  public boolean doesUpgradeTaskHaveDdlWorkToDo() {
    if (!GrouperDdlUtils.assertTableThere(true, "grouper_oauth_client")) {
      return true;
    }
    if (!GrouperDdlUtils.assertIndexExists("grouper_oauth_client", "grouper_oauth_client_idx")) {
      return true;
    }
    if (!GrouperDdlUtils.assertTableThere(true, "grouper_oauth_code")) {
      return true;
    }
    if (!GrouperDdlUtils.assertIndexExists("grouper_oauth_code", "grouper_oauth_code_idx")) {
      return true;
    }
    if (!GrouperDdlUtils.assertIndexExists("grouper_oauth_code", "grouper_oauth_code_exp_idx")) {
      return true;
    }
    if (!GrouperDdlUtils.assertIndexExists("grouper_oauth_code", "grp_oauth_code_client_idx")) {
      return true;
    }
    if (!GrouperDdlUtils.assertTableThere(true, "grouper_oauth_pend_authz_req")) {
      return true;
    }
    if (!GrouperDdlUtils.assertIndexExists("grouper_oauth_pend_authz_req", "grp_oauth_pend_req_idx")) {
      return true;
    }
    if (!GrouperDdlUtils.assertIndexExists("grouper_oauth_pend_authz_req", "grp_oauth_pend_exp_idx")) {
      return true;
    }
    if (!GrouperDdlUtils.assertIndexExists("grouper_oauth_pend_authz_req", "grp_oauth_pend_client_idx")) {
      return true;
    }
    if (!GrouperDdlUtils.assertTableThere(true, "grouper_mcp_tool_log")) {
      return true;
    }
    if (!GrouperDdlUtils.assertIndexExists("grouper_mcp_tool_log", "grp_mcp_tool_log_member_idx")) {
      return true;
    }
    if (!GrouperDdlUtils.assertIndexExists("grouper_mcp_tool_log", "grp_mcp_tool_log_started_idx")) {
      return true;
    }
    if (!GrouperDdlUtils.assertIndexExists("grouper_mcp_tool_log", "grp_mcp_tool_log_name_idx")) {
      return true;
    }
    if (!GrouperDdlUtils.assertIndexExists("grouper_mcp_tool_log", "grp_mcp_tool_log_oauth_idx")) {
      return true;
    }
    if (!oauthSigningKeysExist()) {
      return true;
    }
    return false;
  }

  /**
   * check if the OAuth RSA signing key pair is already stored in config
   * @return true if both keys exist
   */
  private static boolean oauthSigningKeysExist() {
    String privateKey = GrouperConfig.retrieveConfig()
        .propertyValueString("grouper.oauth.signingKey.privateKey");
    String publicKey = GrouperConfig.retrieveConfig()
        .propertyValueString("grouper.oauth.signingKey.publicKey");
    return StringUtils.isNotBlank(privateKey) && StringUtils.isNotBlank(publicKey);
  }

  @Override
  public boolean upgradeTaskIsDdl() {
    return true;
  }

  @Override
  public GrouperVersion versionIntroduced() {
    return GrouperVersion.valueOfIgnoreCase("6.1.0");
  }

  @Override
  public void updateVersionFromPrevious(OtherJobInput otherJobInput) {
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

        // ==================== grouper_oauth_client ====================
        if (!GrouperDdlUtils.assertTableThere(true, "grouper_oauth_client")) {
          if (GrouperDdlUtils.isOracle()) {
            new GcDbAccess().sql(
                "CREATE TABLE grouper_oauth_client ("
                + "  internal_id NUMBER(20) NOT NULL,"
                + "  client_id VARCHAR2(255) NOT NULL,"
                + "  client_name VARCHAR2(255),"
                + "  redirect_uris VARCHAR2(4000) NOT NULL,"
                + "  client_secret VARCHAR2(4000),"
                + "  registered_micros NUMBER(20) NOT NULL,"
                + "  member_internal_id NUMBER(20),"
                + "  code_count NUMBER(20),"
                + "  last_code_micros NUMBER(20),"
                + "  PRIMARY KEY (internal_id)"
                + ")"
            ).executeSql();
          } else {
            new GcDbAccess().sql(
                "CREATE TABLE grouper_oauth_client ("
                + "  internal_id BIGINT NOT NULL,"
                + "  client_id VARCHAR(255) NOT NULL,"
                + "  client_name VARCHAR(255),"
                + "  redirect_uris VARCHAR(4000) NOT NULL,"
                + "  client_secret VARCHAR(4000),"
                + "  registered_micros BIGINT NOT NULL,"
                + "  member_internal_id BIGINT,"
                + "  code_count BIGINT,"
                + "  last_code_micros BIGINT,"
                + "  PRIMARY KEY (internal_id)"
                + ")"
            ).executeSql();
          }
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", created table grouper_oauth_client");
          }
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_oauth_client", "grouper_oauth_client_idx")) {
          new GcDbAccess().sql(
              "CREATE UNIQUE INDEX grouper_oauth_client_idx ON grouper_oauth_client (client_id)"
          ).executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_oauth_client_idx");
          }
        }

        // ==================== grouper_oauth_code ====================
        if (!GrouperDdlUtils.assertTableThere(true, "grouper_oauth_code")) {
          if (GrouperDdlUtils.isOracle()) {
            new GcDbAccess().sql(
                "CREATE TABLE grouper_oauth_code ("
                + "  internal_id NUMBER(20) NOT NULL,"
                + "  code VARCHAR2(255) NOT NULL,"
                + "  oauth_client_internal_id NUMBER(20) NOT NULL,"
                + "  redirect_uri VARCHAR2(4000),"
                + "  code_challenge VARCHAR2(255) NOT NULL,"
                + "  code_challenge_method VARCHAR2(10) NOT NULL,"
                + "  member_internal_id NUMBER(20) NOT NULL,"
                + "  is_used VARCHAR2(1) NOT NULL,"
                + "  created_micros NUMBER(20) NOT NULL,"
                + "  expires_micros NUMBER(20),"
                + "  consent_details VARCHAR2(4000),"
                + "  PRIMARY KEY (internal_id)"
                + ")"
            ).executeSql();
          } else {
            new GcDbAccess().sql(
                "CREATE TABLE grouper_oauth_code ("
                + "  internal_id BIGINT NOT NULL,"
                + "  code VARCHAR(255) NOT NULL,"
                + "  oauth_client_internal_id BIGINT NOT NULL,"
                + "  redirect_uri VARCHAR(4000),"
                + "  code_challenge VARCHAR(255) NOT NULL,"
                + "  code_challenge_method VARCHAR(10) NOT NULL,"
                + "  member_internal_id BIGINT NOT NULL,"
                + "  is_used VARCHAR(1) NOT NULL,"
                + "  created_micros BIGINT NOT NULL,"
                + "  expires_micros BIGINT,"
                + "  consent_details VARCHAR(4000),"
                + "  PRIMARY KEY (internal_id)"
                + ")"
            ).executeSql();
          }
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", created table grouper_oauth_code");
          }
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_oauth_code", "grouper_oauth_code_idx")) {
          new GcDbAccess().sql(
              "CREATE UNIQUE INDEX grouper_oauth_code_idx ON grouper_oauth_code (code)"
          ).executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_oauth_code_idx");
          }
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_oauth_code", "grouper_oauth_code_exp_idx")) {
          new GcDbAccess().sql(
              "CREATE INDEX grouper_oauth_code_exp_idx ON grouper_oauth_code (expires_micros)"
          ).executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grouper_oauth_code_exp_idx");
          }
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_oauth_code", "grp_oauth_code_client_idx")) {
          new GcDbAccess().sql(
              "CREATE INDEX grp_oauth_code_client_idx ON grouper_oauth_code (oauth_client_internal_id)"
          ).executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grp_oauth_code_client_idx");
          }
        }

        // ==================== grouper_oauth_pend_authz_req ====================
        if (!GrouperDdlUtils.assertTableThere(true, "grouper_oauth_pend_authz_req")) {
          if (GrouperDdlUtils.isOracle()) {
            new GcDbAccess().sql(
                "CREATE TABLE grouper_oauth_pend_authz_req ("
                + "  internal_id NUMBER(20) NOT NULL,"
                + "  request_id VARCHAR2(255) NOT NULL,"
                + "  oauth_client_internal_id NUMBER(20) NOT NULL,"
                + "  redirect_uri VARCHAR2(4000),"
                + "  code_challenge VARCHAR2(255) NOT NULL,"
                + "  code_challenge_method VARCHAR2(10) NOT NULL,"
                + "  state VARCHAR2(4000),"
                + "  scope VARCHAR2(4000),"
                + "  created_micros NUMBER(20) NOT NULL,"
                + "  expires_micros NUMBER(20),"
                + "  PRIMARY KEY (internal_id)"
                + ")"
            ).executeSql();
          } else {
            new GcDbAccess().sql(
                "CREATE TABLE grouper_oauth_pend_authz_req ("
                + "  internal_id BIGINT NOT NULL,"
                + "  request_id VARCHAR(255) NOT NULL,"
                + "  oauth_client_internal_id BIGINT NOT NULL,"
                + "  redirect_uri VARCHAR(4000),"
                + "  code_challenge VARCHAR(255) NOT NULL,"
                + "  code_challenge_method VARCHAR(10) NOT NULL,"
                + "  state VARCHAR(4000),"
                + "  scope VARCHAR(4000),"
                + "  created_micros BIGINT NOT NULL,"
                + "  expires_micros BIGINT,"
                + "  PRIMARY KEY (internal_id)"
                + ")"
            ).executeSql();
          }
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", created table grouper_oauth_pend_authz_req");
          }
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_oauth_pend_authz_req", "grp_oauth_pend_req_idx")) {
          new GcDbAccess().sql(
              "CREATE UNIQUE INDEX grp_oauth_pend_req_idx ON grouper_oauth_pend_authz_req (request_id)"
          ).executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grp_oauth_pend_req_idx");
          }
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_oauth_pend_authz_req", "grp_oauth_pend_exp_idx")) {
          new GcDbAccess().sql(
              "CREATE INDEX grp_oauth_pend_exp_idx ON grouper_oauth_pend_authz_req (expires_micros)"
          ).executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grp_oauth_pend_exp_idx");
          }
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_oauth_pend_authz_req", "grp_oauth_pend_client_idx")) {
          new GcDbAccess().sql(
              "CREATE INDEX grp_oauth_pend_client_idx ON grouper_oauth_pend_authz_req (oauth_client_internal_id)"
          ).executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grp_oauth_pend_client_idx");
          }
        }

        // ==================== grouper_mcp_tool_log ====================
        if (!GrouperDdlUtils.assertTableThere(true, "grouper_mcp_tool_log")) {
          if (GrouperDdlUtils.isOracle()) {
            new GcDbAccess().sql(
                "CREATE TABLE grouper_mcp_tool_log ("
                + "  internal_id NUMBER(20) NOT NULL,"
                + "  oauth_client_internal_id NUMBER(20),"
                + "  member_internal_id NUMBER(20) NOT NULL,"
                + "  tool_name VARCHAR2(255) NOT NULL,"
                + "  tool_category VARCHAR2(64) NOT NULL,"
                + "  request VARCHAR2(4000),"
                + "  response_or_error VARCHAR2(4000),"
                + "  is_error VARCHAR2(1) NOT NULL,"
                + "  started_micros NUMBER(20) NOT NULL,"
                + "  duration_micros NUMBER(20),"
                + "  PRIMARY KEY (internal_id)"
                + ")"
            ).executeSql();
          } else {
            new GcDbAccess().sql(
                "CREATE TABLE grouper_mcp_tool_log ("
                + "  internal_id BIGINT NOT NULL,"
                + "  oauth_client_internal_id BIGINT,"
                + "  member_internal_id BIGINT NOT NULL,"
                + "  tool_name VARCHAR(255) NOT NULL,"
                + "  tool_category VARCHAR(64) NOT NULL,"
                + "  request VARCHAR(4000),"
                + "  response_or_error VARCHAR(4000),"
                + "  is_error VARCHAR(1) NOT NULL,"
                + "  started_micros BIGINT NOT NULL,"
                + "  duration_micros BIGINT,"
                + "  PRIMARY KEY (internal_id)"
                + ")"
            ).executeSql();
          }
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", created table grouper_mcp_tool_log");
          }
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_mcp_tool_log", "grp_mcp_tool_log_member_idx")) {
          new GcDbAccess().sql(
              "CREATE INDEX grp_mcp_tool_log_member_idx ON grouper_mcp_tool_log (member_internal_id)"
          ).executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grp_mcp_tool_log_member_idx");
          }
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_mcp_tool_log", "grp_mcp_tool_log_started_idx")) {
          new GcDbAccess().sql(
              "CREATE INDEX grp_mcp_tool_log_started_idx ON grouper_mcp_tool_log (started_micros)"
          ).executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grp_mcp_tool_log_started_idx");
          }
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_mcp_tool_log", "grp_mcp_tool_log_name_idx")) {
          new GcDbAccess().sql(
              "CREATE INDEX grp_mcp_tool_log_name_idx ON grouper_mcp_tool_log (tool_name)"
          ).executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grp_mcp_tool_log_name_idx");
          }
        }

        if (!GrouperDdlUtils.assertIndexExists("grouper_mcp_tool_log", "grp_mcp_tool_log_oauth_idx")) {
          new GcDbAccess().sql(
              "CREATE INDEX grp_mcp_tool_log_oauth_idx ON grouper_mcp_tool_log (oauth_client_internal_id)"
          ).executeSql();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().addInsertCount(1);
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", added index grp_mcp_tool_log_oauth_idx");
          }
        }

        // initialize OAuth RSA signing key pair so all containers share the same key
        if (!oauthSigningKeysExist()) {
          GrouperOAuthSigningKey.initializeIfNeeded();
          if (otherJobInput != null) {
            otherJobInput.getHib3GrouperLoaderLog().appendJobMessage(", initialized OAuth RSA signing key pair");
          }
        }

        return null;
      }
    });
  }
}
