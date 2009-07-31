/*
 * @author mchyzer
 * $Id: GrouperUiCustomizer.java,v 1.1 2009-07-31 14:27:27 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi;

import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * Extend this class and configure to customize the UI
 */
public class GrouperUiCustomizer {
  
  /** grouper ui customizer instance */
  private static GrouperUiCustomizer grouperUiCustomizer = null;
  
  /**
   * get the instance configured.  Note this is cached, any changes require a restart
   * @return the customizer
   */
  @SuppressWarnings("unchecked")
  public static GrouperUiCustomizer instance() {
    
    if (grouperUiCustomizer == null) {
    
      Properties properties = GrouperUtil.propertiesFromResourceName(
          "grouperUiSettings.properties");
      String grouperUiCustomizerClassname = GrouperUtil.propertiesValue(properties, 
          "grouperUiCustomizerClassname");
      grouperUiCustomizerClassname = StringUtils.defaultIfEmpty(grouperUiCustomizerClassname, 
          GrouperUiCustomizer.class.getName());
      Class<GrouperUiCustomizer> grouperUiCustomizerClass = GrouperUtil.forName(grouperUiCustomizerClassname);
      grouperUiCustomizer = GrouperUtil.newInstance(grouperUiCustomizerClass);
    }
    return grouperUiCustomizer;
    
  }
  
  /**
   * logout callback, if you want to do logic on callback
   */
  public void logout() {
    //logout logic
  }
  
}
