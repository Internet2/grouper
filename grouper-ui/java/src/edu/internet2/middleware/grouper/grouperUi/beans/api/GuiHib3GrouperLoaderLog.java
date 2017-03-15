/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.grouperUi.beans.api;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperLoaderContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * gui class to display a loader log
 */
public class GuiHib3GrouperLoaderLog {

  /**
   * 
   * @param hib3GrouperLoaderLogs
   * @return hib3GrouperLoaderLogs
   */
  public static List<GuiHib3GrouperLoaderLog> convertFromHib3GrouperLoaderLogs(List<Hib3GrouperLoaderLog> hib3GrouperLoaderLogs) {
    return convertFromHib3GrouperLoaderLogs(hib3GrouperLoaderLogs, null, -1);
  }

  /**
   * gui group of group being loaded if applicable
   */
  private GuiGroup loadedGuiGroup;
  
  /**
   * get the loaded group, not null if there are multiple
   * @return the loaded group
   */
  public GuiGroup getLoadedGuiGroup() {

    if (this.loadedGuiGroup == null) {
    
      String jobName = null;
        
      //if there is a parent than this is a child
      if (!StringUtils.isBlank(this.hib3GrouperLoaderLog.getParentJobId())) {
  
        jobName = this.hib3GrouperLoaderLog.getJobName();
        
      } else {
        
        //see if this is a simple job
        GrouperLoaderType grouperLoaderType = GrouperRequestContainer.retrieveFromRequestOrCreate()
            .getGrouperLoaderContainer().getGrouperLoaderType();
        
        if (grouperLoaderType == GrouperLoaderType.SQL_SIMPLE || grouperLoaderType == GrouperLoaderType.LDAP_SIMPLE) {
          jobName = GrouperRequestContainer.retrieveFromRequestOrCreate()
              .getGrouperLoaderContainer().getJobName();
        }
        
      }
      
      if (jobName == null) { 
        return null;
      }
      
      String groupName = GrouperLoaderContainer.retrieveGroupNameFromJobName(jobName);
      
      //not sure why it would be null, but program defensively
      if (StringUtils.isBlank(groupName)) {
        
        return null;
        
      }
  
      Group group = GroupFinder.findByName(GrouperSession.staticGrouperSession(), groupName, false);
      
      if (group == null) {
        return null;
      }
      this.loadedGuiGroup = new GuiGroup(group);
    }
    
    return this.loadedGuiGroup;
  }
  
  /**
   * 
   * @return status background color
   */
  public String getStatusBackgroundColor() {
    
    GrouperLoaderStatus grouperLoaderStatus = GrouperLoaderStatus.ERROR;
    
    if (!StringUtils.isBlank(this.hib3GrouperLoaderLog.getStatus())) {
      
      grouperLoaderStatus = GrouperLoaderStatus.valueOfIgnoreCase(this.hib3GrouperLoaderLog.getStatus(), false);
      
    }
    
    if (grouperLoaderStatus == null) {
      
      grouperLoaderStatus = GrouperLoaderStatus.ERROR;
      
    }

    switch(grouperLoaderStatus) {
      case ERROR:
      case CONFIG_ERROR:
        return "red";
      case SUBJECT_PROBLEMS:
      case WARNING:
        return "orange";
      case RUNNING:
      case STARTED:
        return "yellow";
      case SUCCESS:
        return "green";
    }
    return "red";
  }
  
  /**
   * 
   * @return status background color
   */
  public String getStatusTextColor() {
    
    GrouperLoaderStatus grouperLoaderStatus = GrouperLoaderStatus.ERROR;
    
    if (!StringUtils.isBlank(this.hib3GrouperLoaderLog.getStatus())) {
      
      grouperLoaderStatus = GrouperLoaderStatus.valueOfIgnoreCase(this.hib3GrouperLoaderLog.getStatus(), false);
      
    }
    
    if (grouperLoaderStatus == null) {
      
      grouperLoaderStatus = GrouperLoaderStatus.ERROR;
      
    }

    switch(grouperLoaderStatus) {
      case ERROR:
      case CONFIG_ERROR:
        return "white";
      case SUBJECT_PROBLEMS:
      case WARNING:
        return "black";
      case RUNNING:
      case STARTED:
        return "black";
      case SUCCESS:
        return "white";
    }
    return "white";
  }
  
  /**
   * 
   * @param hib3GrouperLoaderLogs
   * @param configMax
   * @param defaultMax
   * @return groups
   */
  public static List<GuiHib3GrouperLoaderLog> convertFromHib3GrouperLoaderLogs(List<Hib3GrouperLoaderLog> hib3GrouperLoaderLogs, String configMax, int defaultMax) {
    List<GuiHib3GrouperLoaderLog> tempHib3LoaderLogs = new ArrayList<GuiHib3GrouperLoaderLog>();
    
    Integer max = null;
    
    if (!StringUtils.isBlank(configMax)) {
      max = GrouperUiConfig.retrieveConfig().propertyValueInt(configMax, defaultMax);
    }
    
    int count = 0;
    for (Hib3GrouperLoaderLog hib3GrouperLoaderLog : GrouperUtil.nonNull(hib3GrouperLoaderLogs)) {
      tempHib3LoaderLogs.add(new GuiHib3GrouperLoaderLog(hib3GrouperLoaderLog));
      if (max != null && ++count >= max) {
        break;
      }
    }
    
    return tempHib3LoaderLogs;
    
  }

  /**
   * @param hib3GrouperLoaderLog1
   */
  public GuiHib3GrouperLoaderLog(Hib3GrouperLoaderLog hib3GrouperLoaderLog1) {
    super();
    this.hib3GrouperLoaderLog = hib3GrouperLoaderLog1;
  }

  /**
   * return the encosed object
   * @return the log object
   */
  public Hib3GrouperLoaderLog getHib3GrouperLoaderLog() {
    return this.hib3GrouperLoaderLog;
  }
  
  /**
   * encloses this
   */
  private Hib3GrouperLoaderLog hib3GrouperLoaderLog;
  
  /**
   * 
   */
  public GuiHib3GrouperLoaderLog() {
  }

}
