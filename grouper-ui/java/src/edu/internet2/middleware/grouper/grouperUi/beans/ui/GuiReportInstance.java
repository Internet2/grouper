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

import edu.internet2.middleware.morphString.Morph;

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
  
  /**
   * @param reportConfigBean
   * @param reportInstances
   * @return list of gui report instances
   */
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

  /**
   * @return user friendly run time
   */
  public String getRunTime() {
    Date date = new Date(reportInstance.getReportInstanceMillisSince1970());
    String runTimeString = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date);
    return runTimeString;
  }

  /**
   * @return get human friendly unencrypted report file size
   */
  public String getUnencryptedReportFileSize() {
    if (reportInstance.getReportInstanceSizeBytes() != null) {      
      return FileUtils.byteCountToDisplaySize(reportInstance.getReportInstanceSizeBytes());
    }
    return null;
  }

  /**
   * @return report config bean associated with the current object
   */
  public GrouperReportConfigurationBean getReportConfigBean() {
    return reportConfigBean;
  }

  /**
   * @return report instance associated with the current object
   */
  public GrouperReportInstance getReportInstance() {
    return reportInstance;
  }

  /**
   * report config bean associated with the current object
   * @param reportConfigBean
   */
  public void setReportConfigBean(GrouperReportConfigurationBean reportConfigBean) {
    this.reportConfigBean = reportConfigBean;
  }

  /**
   * report instance associated with the current object
   * @param reportInstance
   */
  public void setReportInstance(GrouperReportInstance reportInstance) {
    this.reportInstance = reportInstance;
  }
  
  /**
   * @return report instance encryption key with masking
   */
  public String getReportInstanceEncryptionKey() {
    if (StringUtils.isNotBlank(reportInstance.getReportInstanceEncryptionKey())) {
      String encryptionKey = Morph.decrypt(reportInstance.getReportInstanceEncryptionKey());
      final String overlay = StringUtils.repeat("X", encryptionKey.length() - 3);
      String masked = StringUtils.overlay(encryptionKey, overlay, 3, encryptionKey.length());
      return masked;
    }
    return null;
  }
  
  
    
}
