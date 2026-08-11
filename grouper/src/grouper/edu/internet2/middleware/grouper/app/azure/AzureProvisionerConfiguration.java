package edu.internet2.middleware.grouper.app.azure;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.provisioning.ProvisionerStartWithBase;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningConfiguration;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;


public class AzureProvisionerConfiguration extends ProvisioningConfiguration {
  
  public final static Set<String> startWithConfigClassNames = new LinkedHashSet<String>();
  
  static {
    startWithConfigClassNames.add(AzureProvisioningStartWith.class.getName());
  }
  
  @Override
  public List<ProvisionerStartWithBase> getStartWithConfigClasses() {
    
    List<ProvisionerStartWithBase> result = new ArrayList<ProvisionerStartWithBase>();
    
    for (String className: startWithConfigClassNames) {
      try {
        Class<ProvisionerStartWithBase> configClass = (Class<ProvisionerStartWithBase>) GrouperUtil.forName(className);
        ProvisionerStartWithBase config = GrouperUtil.newInstance(configClass);
        result.add(config);
      } catch (Exception e) {
        //TODO
      }
    }
    
    return result;
    
  }

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "provisioner." + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(provisioner)\\.([^.]+)\\.(.*)$";
  }
  
  @Override
  public String getPropertySuffixThatIdentifiesThisConfig() {
    return "class";
  }

  @Override
  public String getPropertyValueThatIdentifiesThisConfig() {
    return GrouperAzureProvisioner.class.getName();
  }

  private void assignCacheConfig() {
    
  }
  
  @Override
  public void insertConfig(boolean fromUi, StringBuilder message,
      List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay, List<String> actionsPerformed) {
    assignCacheConfig();
    super.insertConfig(fromUi, message, errorsToDisplay, validationErrorsToDisplay, actionsPerformed);
  }

  @Override
  public void editConfig(boolean fromUi, StringBuilder message,
      List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay,List<String> actionsPerformed) {
    assignCacheConfig();
    super.editConfig(fromUi, message, errorsToDisplay, validationErrorsToDisplay, actionsPerformed);
  }

  @Override
  public void validatePreSave(boolean isInsert, List<String> errorsToDisplay,
      Map<String, String> validationErrorsToDisplay) {

    super.validatePreSave(isInsert, errorsToDisplay, validationErrorsToDisplay);

    if (errorsToDisplay.size() > 0 || validationErrorsToDisplay.size() > 0) {
      return;
    }

    GrouperConfigurationModuleAttribute numberOfGroupAttributes = this.retrieveAttributes().get("numberOfGroupAttributes");

    if (numberOfGroupAttributes == null) {
      return;
    }

    int numberOfGroupAttributesLength = GrouperUtil.intValue(numberOfGroupAttributes.getValueOrExpressionEvaluationValue(), 0);

    for (int i=0; i<numberOfGroupAttributesLength; i++) {

      GrouperConfigurationModuleAttribute attributeName = this.retrieveAttributes().get("targetGroupAttribute."+i+".name");
      if (attributeName == null) {
        continue;
      }

      String value = attributeName.getValueOrExpressionEvaluationValue();

      if (StringUtils.equals("groupOwners", value)) {

        GrouperConfigurationModuleAttribute multiValuedAttribute = this.retrieveAttributes().get("targetGroupAttribute."+i+".multiValued");
        boolean multiValued = multiValuedAttribute != null
            && GrouperUtil.booleanValue(multiValuedAttribute.getValueOrExpressionEvaluationValue(), false);

        if (!multiValued) {
          String errorMessage = GrouperTextContainer.textOrNull("grouperAzureProvisionerConfigurationGroupOwnersMustBeMultiValued");
          validationErrorsToDisplay.put(attributeName.getHtmlForElementIdHandle(), errorMessage);
        }
      }
    }

  }

}
