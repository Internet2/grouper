package edu.internet2.middleware.grouper.ui.util;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;


public class GrouperUiUserData {

  /**
   * the group name the ui uses for user data
   * @return the group name the ui uses for user data
   */
  public static String grouperUiGroupNameForUserData() {
    return GrouperConfig.retrieveConfig().propertyValueString("grouper.rootStemForBuiltinObjects") + ":grouperUi:grouperUiUserData";
  }
  
}
