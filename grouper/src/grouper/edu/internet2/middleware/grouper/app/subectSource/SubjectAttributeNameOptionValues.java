package edu.internet2.middleware.grouper.app.subectSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.cfg.dbConfig.OptionValueDriver;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;

public class SubjectAttributeNameOptionValues implements OptionValueDriver {

  private Map<String, GrouperConfigurationModuleAttribute> configSuffixToConfigModuleAttribute;
  
  
  @Override
  public void setConfigSuffixToConfigModuleAttribute(
      Map<String, GrouperConfigurationModuleAttribute> configSuffixToConfigModuleAttribute) {
    
    this.configSuffixToConfigModuleAttribute = configSuffixToConfigModuleAttribute;
    
  }



  @Override
  public List<MultiKey> retrieveKeysAndLabels() {
    
    GrouperConfigurationModuleAttribute grouperConfigurationModuleAttribute = configSuffixToConfigModuleAttribute.get("numberOfAttributes");
    int countOfAttributes = GrouperUtil.intValue(grouperConfigurationModuleAttribute.getValueOrExpressionEvaluation(), 0);
    
    List<MultiKey> result = new ArrayList<MultiKey>();
    
    for (int i=0; i<countOfAttributes; i++) {
      GrouperConfigurationModuleAttribute attribute = configSuffixToConfigModuleAttribute.get("attribute."+i+".name");
      if (attribute != null) {
        String name = attribute.getValueOrExpressionEvaluation();
        if (StringUtils.isNotBlank(name)) {
          result.add(new MultiKey(name, name));
        }
        
      }
    }
    
    return result;
  }

}
