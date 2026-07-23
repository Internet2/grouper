package edu.internet2.middleware.grouper.app.ccure;

import java.sql.Types;

import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

/**
 * Model object for a CCure mock login session (session-id / token pair returned by
 * api/Authenticate/Login, validated by api/Authenticate/Logout).
 *
 * This is persisted (rather than kept in a static field) because the mock service handler is
 * instantiated fresh per request and Grouper's UI/WS/daemon layers can run as multiple nodes -
 * a login and its matching logout are not guaranteed to land on the same JVM.
 */
public class MockCcureAuth {

  private String id;

  private String sessionId;

  private String accessToken;

  private Long createdAtMillis;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getSessionId() {
    return sessionId;
  }

  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public Long getCreatedAtMillis() {
    return createdAtMillis;
  }

  public void setCreatedAtMillis(Long createdAtMillis) {
    this.createdAtMillis = createdAtMillis;
  }

  /**
   * Build a new session row with a freshly generated id, session id, and access token.
   * @return the new (unsaved) auth row
   */
  public static MockCcureAuth newSession() {
    MockCcureAuth grouperCcureAuth = new MockCcureAuth();
    grouperCcureAuth.setId(GrouperUuid.getUuid());
    grouperCcureAuth.setSessionId(GrouperUuid.getUuid());
    grouperCcureAuth.setAccessToken(GrouperUuid.getUuid());
    grouperCcureAuth.setCreatedAtMillis(System.currentTimeMillis());
    return grouperCcureAuth;
  }

  /**
   * DDL for the mock table.
   * @param ddlVersionBean ddl version bean
   * @param database database
   */
  public static void createTableCcureAuth(DdlVersionBean ddlVersionBean, Database database) {

    final String tableName = "mock_ccure_auth";

    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {

      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", Types.VARCHAR, "40", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "session_id", Types.VARCHAR, "40", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "access_token", Types.VARCHAR, "40", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "created_at_millis", Types.BIGINT, "15", false, true);

      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_ccure_auth_sess_idx", true, "session_id");
    }
  }

}
