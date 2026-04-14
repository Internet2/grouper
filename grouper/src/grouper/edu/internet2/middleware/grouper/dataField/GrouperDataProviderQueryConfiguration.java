package edu.internet2.middleware.grouper.dataField;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleBase;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperDataProviderQueryConfiguration extends GrouperConfigurationModuleBase {

  
  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_PROPERTIES;
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "grouperDataProviderQuery." + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(grouperDataProviderQuery)\\.([^.]+)\\.(.*)$";
  }

  @Override
  protected String getConfigurationTypePrefix() {
    return "grouperDataProviderQuery";
  }
  
  @Override
  public String getConfigIdThatIdentifiesThisConfig() {
    return "dataProviderQueryConfigId";
  }
  
  /**
   * list of configured data provider query configs
   * @return
   */
  public static List<GrouperDataProviderQueryConfiguration> retrieveAllDataProviderQueryConfigurations() {
   Set<String> classNames = new HashSet<String>();
   classNames.add(GrouperDataProviderQueryConfiguration.class.getName());
   return (List<GrouperDataProviderQueryConfiguration>) (Object) retrieveAllConfigurations(classNames);
  }

  @Override
  public void validatePreSave(boolean isInsert, List<String> errorsToDisplay,
      Map<String, String> validationErrorsToDisplay) {

    super.validatePreSave(isInsert, errorsToDisplay, validationErrorsToDisplay);
    
    if (errorsToDisplay.size() > 0 || validationErrorsToDisplay.size() > 0) {
      return;
    } 

    GrouperConfigurationModuleAttribute numberOfQueryAttributes = this.retrieveAttributes().get("providerQueryNumberOfDataFields");
    
    int numberOfQueryAttributesLength = 0;
    
    if (numberOfQueryAttributes != null) {
      
      numberOfQueryAttributesLength = GrouperUtil.intValue(numberOfQueryAttributes.getValueOrExpressionEvaluationValue(), 0);
      
      Set<String> attributeNames = new HashSet<String>();

      GrouperDataEngine dataEngine = new GrouperDataEngine();
      dataEngine.loadFieldsAndRows(null);

      for (int i=0; i<numberOfQueryAttributesLength; i++) {

        GrouperConfigurationModuleAttribute attribute = this.retrieveAttributes().get("providerQueryDataField."+i+".providerDataFieldConfigId");
        String attributeName = attribute.getValueOrExpressionEvaluationValue();

        if (!StringUtils.isBlank(attributeName)) {

          if (attributeNames.contains(attributeName)) {
            String errorMessage = GrouperTextContainer.textOrNull("providerQueryDataFieldErrorUsingDuplicateAttributeNames") + " " + attributeName;
            errorsToDisplay.add(errorMessage);
            return;
          }

          attributeNames.add(attributeName);

          // validate that the data field config exists
          if (!dataEngine.getFieldConfigByConfigId().containsKey(attributeName)) {
            errorsToDisplay.add("Data field config 'grouperDataField." + attributeName + ".*' not found, referenced by grouperDataProviderQuery." + this.getConfigId() + ".providerQueryDataField." + i + ".providerDataFieldConfigId");
            return;
          }

        }
      }
    }
    
  }
  
}
