package edu.internet2.middleware.grouper.app.google;

import java.sql.Types;

import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;

import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class GrouperGoogleAuth {
  
  /**
   * @param ddlVersionBean
   * @param database
   */
  public static void createTableGoogleAuth(DdlVersionBean ddlVersionBean, Database database) {

    final String tableName = "mock_google_auth";

    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      
      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "config_id", Types.VARCHAR, "1024", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "access_token", Types.VARCHAR, "40", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "expires_in_seconds", Types.BIGINT, "12", false, true);
          
    }
    
  }

  private String configId;
  
  private String accessToken;
  
  private long expiresInSeconds;

  
  public String getConfigId() {
    return configId;
  }

  
  public void setConfigId(String configId) {
    this.configId = configId;
  }

  
  public String getAccessToken() {
    return accessToken;
  }

  
  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }


  
  public long getExpiresInSeconds() {
    return expiresInSeconds;
  }

  public void setExpiresInSeconds(long expiresInSeconds) {
    this.expiresInSeconds = expiresInSeconds;
  }

}
