package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.app.sqlSync.SqlSyncConfiguration;

public class GuiSqlSyncConfiguration {
  
  private SqlSyncConfiguration sqlSyncConfiguration;
  
  public SqlSyncConfiguration getSqlSyncConfiguration() {
    return sqlSyncConfiguration;
  }
  
  private GuiSqlSyncConfiguration(SqlSyncConfiguration sqlSyncConfiguration) {
    this.sqlSyncConfiguration = sqlSyncConfiguration;
  }
  
  public static GuiSqlSyncConfiguration convertFromSqlSyncConfiguration(SqlSyncConfiguration sqlSyncConfiguration) {
    return new GuiSqlSyncConfiguration(sqlSyncConfiguration);
  }
  
  public static List<GuiSqlSyncConfiguration> convertFromSqlSyncConfiguration(List<SqlSyncConfiguration> sqlSyncConfigurations) {
    
    List<GuiSqlSyncConfiguration> guiSqlSyncConfigs = new ArrayList<GuiSqlSyncConfiguration>();
    
    for (SqlSyncConfiguration sqlSyncConfiguration: sqlSyncConfigurations) {
      guiSqlSyncConfigs.add(convertFromSqlSyncConfiguration(sqlSyncConfiguration));
    }
    
    return guiSqlSyncConfigs;
    
  }

}
