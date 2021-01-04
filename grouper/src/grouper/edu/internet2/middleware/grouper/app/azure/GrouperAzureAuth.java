package edu.internet2.middleware.grouper.app.azure;

import java.sql.Types;

import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

import edu.internet2.middleware.grouper.ddl.DdlUtilsChangeDatabase;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ddl.GrouperTestDdl;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class GrouperAzureAuth {

  public GrouperAzureAuth() {
  }

  /**
   * @param ddlVersionBean
   * @param database
   */
  public static void createTableAzureAuth(DdlVersionBean ddlVersionBean, Database database) {

    final String tableName = "mock_azure_auth";

    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      
      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "config_id", Types.VARCHAR, "1024", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "access_token", Types.VARCHAR, "40", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "expires_on_seconds", Types.BIGINT, "12", false, true);
          
    }
    
  }

  private String configId;
  
  private String accessToken;
  
  private long expiresOnSeconds;

  
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

  
  public long getExpiresOnSeconds() {
    return expiresOnSeconds;
  }

  
  public void setExpiresOnSeconds(long expiresOnSeconds) {
    this.expiresOnSeconds = expiresOnSeconds;
  }
  
}
