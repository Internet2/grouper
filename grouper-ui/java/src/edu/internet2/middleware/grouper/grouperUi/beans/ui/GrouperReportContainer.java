package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.app.reports.GrouperReportConfigurationBean;
import edu.internet2.middleware.grouper.app.reports.ReportConfigFormat;
import edu.internet2.middleware.grouper.app.reports.ReportConfigType;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGrouperReportConfig;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.subject.Subject;

public class GrouperReportContainer {
  
  private List<GuiGrouperReportConfig> guiGrouperReportConfigs = new ArrayList<GuiGrouperReportConfig>();
  
  /**
   * report config bean user is currently working on
   */
  private GrouperReportConfigurationBean configBean;
  
  private Set<GrouperReportConfigurationBean> reportConfigBeans = new HashSet<GrouperReportConfigurationBean>();
  
  public boolean isCanReadGrouperReports() {
    //TODO add logic
    return true;
  }
  
  public boolean isCanWriteGrouperReports() {
    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    return PrivilegeHelper.isWheelOrRoot(loggedInSubject);
  }

  public List<GuiGrouperReportConfig> getGuiGrouperReportConfigs() {
    return guiGrouperReportConfigs;
  }
  
  public List<String> getAllReportConfigTypes() {
    
    List<String> reportConfigTypes = new ArrayList<String>();
    
    for (ReportConfigType type: ReportConfigType.values()) {
      reportConfigTypes.add(type.name());
    }
    
    return reportConfigTypes;
  }
  
  public List<String> getAllReportConfigFormats() {
    
    List<String> reportConfigFormats = new ArrayList<String>();
    
    for (ReportConfigFormat format: ReportConfigFormat.values()) {
      reportConfigFormats.add(format.name());
    }
    return reportConfigFormats;
  }

  public GrouperReportConfigurationBean getConfigBean() {
    return configBean;
  }

  public void setConfigBean(GrouperReportConfigurationBean configBean) {
    this.configBean = configBean;
  }

  public Set<GrouperReportConfigurationBean> getReportConfigBeans() {
    return reportConfigBeans;
  }

  public void setReportConfigBeans(Set<GrouperReportConfigurationBean> reportConfigBeans) {
    this.reportConfigBeans = reportConfigBeans;
  }
  
}
