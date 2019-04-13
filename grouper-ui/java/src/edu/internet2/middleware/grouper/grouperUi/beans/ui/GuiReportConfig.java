package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.text.SimpleDateFormat;
import java.util.Date;

import edu.internet2.middleware.grouper.app.reports.GrouperReportConfigurationBean;
import edu.internet2.middleware.grouper.app.reports.GrouperReportInstance;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.subject.Subject;
import net.redhogs.cronparser.CronExpressionDescriptor;

public class GuiReportConfig {
  
  /**
   * config bean
   */
  private GrouperReportConfigurationBean reportConfigBean;
  
  /**
   * most recent report instance run 
   */
  private GrouperReportInstance mostRecentReportInstance;
  
  public GuiReportConfig(GrouperReportConfigurationBean reportConfigBean, GrouperReportInstance mostRecentReportInstance) {
    this.reportConfigBean = reportConfigBean;
    this.mostRecentReportInstance = mostRecentReportInstance;
  }
  
  public String getLastRunTime() {
    if (mostRecentReportInstance == null) {
      return null;
    }
    Date date = new Date(mostRecentReportInstance.getReportInstanceMillisSince1970());
    String lastRunTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date);
    return lastRunTime;
  }

  public String getUserFriendlyCron() {
    try {      
      return CronExpressionDescriptor.getDescription(reportConfigBean.getReportConfigQuartzCron());
    } catch(Exception e) {
      return "";
    }
  }
  
  public GrouperReportConfigurationBean getReportConfigBean() {
    return reportConfigBean;
  }

  public GrouperReportInstance getMostRecentReportInstance() {
    return mostRecentReportInstance;
  }
  
  public boolean isCanRead() {
    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    return reportConfigBean.isCanRead(loggedInSubject);
  }

}
