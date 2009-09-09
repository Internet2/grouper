/*
 * @author mchyzer
 * $Id: GrouperUiCustomizer.java,v 1.2 2009-09-09 15:20:20 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.grouperUi.tags.TagUtils;
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
    
      String grouperUiCustomizerClassname = TagUtils.mediaResourceString("grouperUiCustomizerClassname");
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
