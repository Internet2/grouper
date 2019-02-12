package edu.internet2.middleware.grouper.app.usdu;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class UsduSettings {
  
  /**
   * if new usdu is enabled
   * @return if new usdu enabled
   */
  public static boolean usduEnabled() {
    return GrouperConfig.retrieveConfig().propertyValueBoolean("usdu.enable", true);
        
  }
  
  /**
   * 
   * @return the stem name with no last colon
   */
  public static String usduStemName() {
    return GrouperUtil.stripEnd(GrouperConfig.retrieveConfig().propertyValueString("usdu.systemFolder", 
        GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects") + ":usdu"), ":");
  }

}
