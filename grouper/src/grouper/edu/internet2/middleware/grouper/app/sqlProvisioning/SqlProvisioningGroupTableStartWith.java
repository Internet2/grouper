package edu.internet2.middleware.grouper.app.sqlProvisioning;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.ProvisionerStartWithBase;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 */
public class SqlProvisioningGroupTableStartWith extends ProvisionerStartWithBase {
  
  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }
 
  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "provisionerStartWith." + this.getConfigId() + ".";
  }
 
  @Override
  public String getConfigIdRegex() {
    return "^(provisionerStartWith)\\.([^.]+)\\.(.*)$";
  }
   
  @Override
  public String getPropertySuffixThatIdentifiesThisConfig() {
    return "startWith";
  }
 
  @Override
  public String getPropertyValueThatIdentifiesThisConfig() {
    return "sqlGroupTable";
  }

  @Override
  public void validatePreSave(boolean isInsert, List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay) {
    super.validatePreSave(isInsert, errorsToDisplay, validationErrorsToDisplay);
    
    //TODO add more validation
    
  }

  /**
   * return provisioning suffix to value
   * @param startWithSuffixToValue
   * @param provisionerSuffixToValue
   * @return
   */
  @Override
  public void populateProvisionerConfigurationValuesFromStartWith(Map<String, String> startWithSuffixToValue, 
      Map<String, Object> provisionerSuffixToValue) {
    
    String columnNames = startWithSuffixToValue.get("columnNames");
    String[] colNames = GrouperUtil.splitTrim(columnNames, ",");
    provisionerSuffixToValue.put("numberOfGroupAttributes", colNames.length);
    
    for (int i=0; i<colNames.length; i++) {
      provisionerSuffixToValue.put("targetGroupAttribute."+i+".name", colNames[i]);
      if (StringUtils.equalsIgnoreCase(colNames[i], "group_name")) {
        //TODO set the translation value
      }
    }
    
    provisionerSuffixToValue.put("operateOnGrouperGroups", true);
    provisionerSuffixToValue.put("provisioningType", "groupAttributes");
    
  }
}
