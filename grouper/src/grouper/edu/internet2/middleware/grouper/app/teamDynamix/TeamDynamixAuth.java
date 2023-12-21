package edu.internet2.middleware.grouper.app.teamDynamix;

import java.sql.Types;

import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class TeamDynamixAuth {
  
  public TeamDynamixAuth() {
  }

  /**
   * @param ddlVersionBean
   * @param database
   */
  public static void createTableTeamDynamixAuth(DdlVersionBean ddlVersionBean, Database database) {

    final String tableName = "mock_teamdynamix_auth";

    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      
      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "config_id", Types.VARCHAR, "1024", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "access_token", Types.VARCHAR, "1024", true, true);
    }
    
  }

  private String configId;
  
  private String accessToken;

  
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
  
}
