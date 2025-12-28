package edu.internet2.middleware.grouper.ui.customizeUi;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class JavaUiCustomizer {

  public static JavaUiCustomizer retrieveInstance() {
    
    String javaUiCustomizerClassName = StringUtils.defaultIfBlank(GrouperUiConfig.retrieveConfig().propertyValueString(
        "uiV2.javaUiCustomizer.class"), JavaUiCustomizer.class.getName());
    
    Class<JavaUiCustomizer> javaUiCustomizerClass = GrouperUtil.forName(javaUiCustomizerClassName);
    JavaUiCustomizer javaUiCustomizer = GrouperUtil.newInstance(javaUiCustomizerClass);    
    
    return javaUiCustomizer;
  }
  
  public void indexMainLogic(IndexMainLogicInput indexMainLogicInput) {
    
  }
  
  public void groupViewLogic(GroupViewLogicInput groupViewLogicInput) {
    
  }
  
}
