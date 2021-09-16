package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.internet2.middleware.grouper.app.sqlSync.SqlSyncConfiguration;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.subject.Subject;

public class SqlSyncConfigurationContainer {

  /**
   * sql sync config user is currently viewing/editing 
   */
  private GuiSqlSyncConfiguration guiSqlSyncConfiguration;
  
  private List<GuiSqlSyncConfiguration> guiSqlSyncConfigurations = new ArrayList<GuiSqlSyncConfiguration>();
  
  /**
   * @return true if can view sql sync configs
   */
  public boolean isCanViewSqlSyncConfigs() {
    
    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    if (PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
      return true;
    }
    return false;
  }

  
  public GuiSqlSyncConfiguration getGuiSqlSyncConfiguration() {
    return guiSqlSyncConfiguration;
  }

  
  public void setGuiSqlSyncConfiguration(GuiSqlSyncConfiguration guiSqlSyncConfiguration) {
    this.guiSqlSyncConfiguration = guiSqlSyncConfiguration;
  }

  
  public List<GuiSqlSyncConfiguration> getGuiSqlSyncConfigurations() {
    return guiSqlSyncConfigurations;
  }

  
  public void setGuiSqlSyncConfigurations(List<GuiSqlSyncConfiguration> guiSqlSyncConfigurations) {
    this.guiSqlSyncConfigurations = guiSqlSyncConfigurations;
  }
  
  public List<SqlSyncConfiguration> getAllSqlSyncTypes() {
    return Arrays.asList(new SqlSyncConfiguration());
  }

}
