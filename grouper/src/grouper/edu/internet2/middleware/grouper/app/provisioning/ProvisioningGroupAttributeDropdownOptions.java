package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.cfg.dbConfig.OptionValueDriver;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;

public class ProvisioningGroupAttributeDropdownOptions implements OptionValueDriver {

  private Map<String, GrouperConfigurationModuleAttribute> configSuffixToConfigModuleAttribute;
  
  
  @Override
  public void setConfigSuffixToConfigModuleAttribute(
      Map<String, GrouperConfigurationModuleAttribute> configSuffixToConfigModuleAttribute) {
    
    this.configSuffixToConfigModuleAttribute = configSuffixToConfigModuleAttribute;
    
  }



  @Override
  public List<MultiKey> retrieveKeysAndLabels() {
    
    GrouperConfigurationModuleAttribute grouperConfigurationModuleAttribute = configSuffixToConfigModuleAttribute.get("numberOfGroupAttributes");
    int countOfAttributes = GrouperUtil.intValue(grouperConfigurationModuleAttribute.getValueOrExpressionEvaluation(), 0);
    
    List<MultiKey> result = new ArrayList<MultiKey>();
    
    for (int i=0; i<countOfAttributes; i++) {
      Boolean isFieldElseAttribute = false;
      GrouperConfigurationModuleAttribute isFieldElseAttributeAttribute = configSuffixToConfigModuleAttribute.get("targetGroupAttribute."+i+".isFieldElseAttribute");
      if (isFieldElseAttributeAttribute != null) {
        isFieldElseAttribute = GrouperUtil.booleanObjectValue(isFieldElseAttributeAttribute.getValueOrExpressionEvaluation());
      }
      if (isFieldElseAttribute == null) {
        continue;
      }
      GrouperConfigurationModuleAttribute nameAttribute = configSuffixToConfigModuleAttribute.get("targetGroupAttribute."+i+"." + (isFieldElseAttribute ? "fieldName" : "name"));
      if (nameAttribute == null) {
        continue;
      }
      String name = nameAttribute.getValueOrExpressionEvaluation();
      
      if (StringUtils.isNotBlank(name)) {
        result.add(new MultiKey(name, name));
      }
    }
    
    return result;
  }

}
