package edu.internet2.middleware.grouper.cfg.dbConfig;

import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouperClient.collections.MultiKey;

public interface OptionValueDriver {
  
  public default void setConfigSuffixToConfigModuleAttribute(Map<String, GrouperConfigurationModuleAttribute> configSuffixToConfigModuleAttribute) {
    
  }
  
  List<MultiKey> retrieveKeysAndLabels();

}
