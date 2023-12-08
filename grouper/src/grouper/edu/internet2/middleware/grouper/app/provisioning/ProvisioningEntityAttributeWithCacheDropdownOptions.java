package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.cfg.dbConfig.OptionValueDriver;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import org.apache.commons.lang3.StringUtils;

public class ProvisioningEntityAttributeWithCacheDropdownOptions implements OptionValueDriver {

  private Map<String, GrouperConfigurationModuleAttribute> configSuffixToConfigModuleAttribute;
  
  
  @Override
  public void setConfigSuffixToConfigModuleAttribute(
      Map<String, GrouperConfigurationModuleAttribute> configSuffixToConfigModuleAttribute) {
    
    this.configSuffixToConfigModuleAttribute = configSuffixToConfigModuleAttribute;
    
  }



  @Override
  public List<MultiKey> retrieveKeysAndLabels() {
    
    // alpha order
    Set<String> options = new TreeSet<String>();
    options.add("entityAttributeValueCache0");
    options.add("entityAttributeValueCache1");
    options.add("entityAttributeValueCache2");
    options.add("entityAttributeValueCache3");
    options.add("subjectId");
    options.add("subjectIdentifier");
        
    GrouperConfigurationModuleAttribute grouperConfigurationModuleAttribute = configSuffixToConfigModuleAttribute.get("numberOfEntityAttributes");
    if (grouperConfigurationModuleAttribute != null) {
      
      int countOfAttributes = GrouperUtil.intValue(grouperConfigurationModuleAttribute.getValueOrExpressionEvaluation(), 0);
      
      for (int i=0; i<countOfAttributes; i++) {
        GrouperConfigurationModuleAttribute nameAttribute = configSuffixToConfigModuleAttribute.get("targetEntityAttribute."+i+".name");
        String name = nameAttribute == null ? null : nameAttribute.getValueOrExpressionEvaluation();
        if (nameAttribute == null || StringUtils.isBlank(name)) {
          continue;
        }
        
        if (StringUtils.isNotBlank(name)) {
          options.add(name);
        }
      }
    }
    
    List<MultiKey> result = new ArrayList<MultiKey>();
    for (String option : options) {
      result.add(new MultiKey(option, option));
    }

    return result;
  }

}
