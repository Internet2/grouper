package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.reports.GrouperReportConfigurationBean;
import edu.internet2.middleware.grouper.app.reports.GrouperReportInstance;

public class GuiReportInstance {
  
  /**
   * report config which is associated with this instance
   */
  private GrouperReportConfigurationBean reportConfigBean;
  
  /**
   * details of the report run
   */
  private GrouperReportInstance reportInstance;
  
  public static List<GuiReportInstance> buildGuiReportInstances(GrouperReportConfigurationBean reportConfigBean, List<GrouperReportInstance> reportInstances) {
    
    Collections.sort(reportInstances, new Comparator<GrouperReportInstance>() {
      @Override
      public int compare(GrouperReportInstance o1, GrouperReportInstance o2) {
        return new Long(o2.getReportInstanceMillisSince1970()).compareTo(new Long(o1.getReportInstanceMillisSince1970()));
      }
    });
    
    List<GuiReportInstance> result = new ArrayList<GuiReportInstance>();
    
    for (GrouperReportInstance reportInstance: reportInstances) {
      
      GuiReportInstance guiReportInstance = new GuiReportInstance();
      guiReportInstance.setReportInstance(reportInstance);
      guiReportInstance.setReportConfigBean(reportConfigBean);
      result.add(guiReportInstance);
    }
    
    return result;
  }

  public String getRunTime() {
    Date date = new Date(reportInstance.getReportInstanceMillisSince1970());
    String runTimeString = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date);
    return runTimeString;
  }

  public String getUnencryptedReportFileSize() {
    return FileUtils.byteCountToDisplaySize(reportInstance.getReportInstanceSizeBytes());
  }

  public GrouperReportConfigurationBean getReportConfigBean() {
    return reportConfigBean;
  }

  public GrouperReportInstance getReportInstance() {
    return reportInstance;
  }

  public void setReportConfigBean(GrouperReportConfigurationBean reportConfigBean) {
    this.reportConfigBean = reportConfigBean;
  }

  public void setReportInstance(GrouperReportInstance reportInstance) {
    this.reportInstance = reportInstance;
  }
  
  public String getReportInstanceEncryptionKey() {
    final String overlay = StringUtils.repeat("X", reportInstance.getReportInstanceEncryptionKey().length() - 3);
    String masked = StringUtils.overlay(reportInstance.getReportInstanceEncryptionKey(), overlay, 3, reportInstance.getReportInstanceEncryptionKey().length());
    return masked;
  }
  
  
    
}
