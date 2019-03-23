package edu.internet2.middleware.grouper.app.reports;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperReportSettings {
  
  /**
   * if grouper reporting is enabled
   * @return if grouper reporting is enabled
   */
  public static boolean grouperReportsEnabled() {
    return GrouperConfig.retrieveConfig().propertyValueBoolean("grouperReporting.enable", true);        
  }
  
  /**
   * 
   * @return the stem name with no last colon
   */
  public static String reportConfigStemName() {
    return GrouperUtil.stripEnd(GrouperConfig.retrieveConfig().propertyValueString("reportConfig.systemFolder", 
        GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects") + ":reportConfig"), ":");
  }

}
