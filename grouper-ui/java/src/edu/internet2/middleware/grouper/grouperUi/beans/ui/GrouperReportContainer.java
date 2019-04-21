package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.app.reports.GrouperReportConfigurationBean;
import edu.internet2.middleware.grouper.app.reports.GrouperReportSettings;
import edu.internet2.middleware.grouper.app.reports.ReportConfigFormat;
import edu.internet2.middleware.grouper.app.reports.ReportConfigType;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGrouperReportConfig;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.subject.Subject;

public class GrouperReportContainer {
  
  /**
   * list of gui report configs
   */
  private List<GuiGrouperReportConfig> guiGrouperReportConfigs = new ArrayList<GuiGrouperReportConfig>();
  
  /**
   * report config bean user is currently working on
   */
  private GrouperReportConfigurationBean configBean;
  
  /**
   * list of report configs for one stem/group
   */
  private List<GrouperReportConfigurationBean> reportConfigBeans = new ArrayList<GrouperReportConfigurationBean>();
  
  /**
   * report configs for gui
   */
  private List<GuiReportConfig> guiReportConfigs = new ArrayList<GuiReportConfig>();
  
  /**
   * encapsulates config and all instances together
   */
  private GrouperReportConfigInstance grouperReportConfigInstance;
  
  /**
   * one report instance details
   */
  private GuiReportInstance guiReportInstance;
  
  /**
   * can logged in subject make changes to grouper report configs
   * @return
   */
  public boolean isCanWriteGrouperReports() {
    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    return PrivilegeHelper.isWheelOrRoot(loggedInSubject);
  }

  /**
   * @return list of gui report configs
   */
  public List<GuiGrouperReportConfig> getGuiGrouperReportConfigs() {
    return guiGrouperReportConfigs;
  }
  
  /**
   * @return all the report config types (eg: SQL)
   */
  public List<String> getAllReportConfigTypes() {
    
    List<String> reportConfigTypes = new ArrayList<String>();
    
    for (ReportConfigType type: ReportConfigType.values()) {
      reportConfigTypes.add(type.name());
    }
    
    return reportConfigTypes;
  }
  
  /**
   * @return all the report config formats (eg: CSV)
   */
  public List<String> getAllReportConfigFormats() {
    
    List<String> reportConfigFormats = new ArrayList<String>();
    
    for (ReportConfigFormat format: ReportConfigFormat.values()) {
      reportConfigFormats.add(format.name());
    }
    return reportConfigFormats;
  }

  /**
   * @return report config bean user is currently working on
   */
  public GrouperReportConfigurationBean getConfigBean() {
    return configBean;
  }

  /**
   * report config bean user is currently working on
   * @param configBean
   */
  public void setConfigBean(GrouperReportConfigurationBean configBean) {
    this.configBean = configBean;
  }

  /**
   * @return list of report configs for one stem/group
   */
  public List<GrouperReportConfigurationBean> getReportConfigBeans() {
    return reportConfigBeans;
  }

  /**
   * list of report configs for one stem/group
   * @param reportConfigBeans
   */
  public void setReportConfigBeans(List<GrouperReportConfigurationBean> reportConfigBeans) {
    this.reportConfigBeans = reportConfigBeans;
  }

  /**
   * @return report configs for gui
   */
  public List<GuiReportConfig> getGuiReportConfigs() {
    return guiReportConfigs;
  }

  /**
   * report configs for gui
   * @param guiReportConfigs
   */
  public void setGuiReportConfigs(List<GuiReportConfig> guiReportConfigs) {
    this.guiReportConfigs = guiReportConfigs;
  }

  /**
   * @return encapsulates config and all instances together
   */
  public GrouperReportConfigInstance getGrouperReportConfigInstance() {
    return grouperReportConfigInstance;
  }

  /**
   * encapsulates config and all instances together
   * @param grouperReportConfigInstance
   */
  public void setGrouperReportConfigInstance(GrouperReportConfigInstance grouperReportConfigInstance) {
    this.grouperReportConfigInstance = grouperReportConfigInstance;
  }

  /**
   * @return one report instance details
   */
  public GuiReportInstance getGuiReportInstance() {
    return guiReportInstance;
  }

  /**
   * one report instance details
   * @param guiReportInstance
   */
  public void setGuiReportInstance(GuiReportInstance guiReportInstance) {
    this.guiReportInstance = guiReportInstance;
  }

  /**
   * @return should the partial encryption key be shown
   */
  public boolean isShowPartialEncryptionKey() {
    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    return PrivilegeHelper.isWheelOrRoot(loggedInSubject);
  }
  
  /**
   * @return is the global reporting enabled
   */
  public boolean isReportingEnabled() {
    return GrouperReportSettings.grouperReportsEnabled();
  }
    
}
