package edu.internet2.middleware.grouper.app.remedyV2;

import java.sql.Types;

import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class GrouperRemedyAuth {

  public GrouperRemedyAuth() {
  }

  /**
   * @param ddlVersionBean
   * @param database
   */
  public static void createTableRemedyAuth(DdlVersionBean ddlVersionBean, Database database) {

    final String tableName = "mock_remedy_auth";

    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      
      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "jwt_token", Types.VARCHAR, "1024", true, true);
          
    }
    
  }

  
  private String jwtToken;
  
  
  public String getJwtToken() {
    return jwtToken;
  }

  
  public void setJwtToken(String jwtToken) {
    this.jwtToken = jwtToken;
  }

}
