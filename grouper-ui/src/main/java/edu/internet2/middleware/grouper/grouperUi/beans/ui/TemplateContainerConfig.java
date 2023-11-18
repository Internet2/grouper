/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * @author vsachdeva
 *
 */
public class TemplateContainerConfig {
  
  private String configKey; // capturing group from the regex
  
  private String logicClassName; // fully qualified class name.
  
  private GrouperTemplateLogicBase logicInstance;
  
  public GrouperTemplateLogicBase getLogicInstance() {
    
    if (logicInstance == null) {
      
      Class<GrouperTemplateLogicBase> templateLogicSubClass = GrouperClientUtils.forName(logicClassName);
      
      GrouperTemplateLogicBase templateLogic = GrouperUtil.newInstance(templateLogicSubClass);
      
      logicInstance = templateLogic;
      
    } 
    
    return logicInstance;
    
  }

}
