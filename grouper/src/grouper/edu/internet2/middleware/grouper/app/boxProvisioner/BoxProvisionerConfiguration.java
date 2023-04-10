package edu.internet2.middleware.grouper.app.boxProvisioner;

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

public class BoxProvisionerConfiguration extends ProvisioningConfiguration {
  
public final static Set<String> startWithConfigClassNames = new LinkedHashSet<String>();
  
  static {
    startWithConfigClassNames.add(BoxProvisioningStartWith.class.getName());
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
    return GrouperBoxProvisioner.class.getName();
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
  public void editConfig(boolean fromUi, StringBuilder message, List<String> errorsToDisplay, Map<String, 
      String> validationErrorsToDisplay, List<String> actionsPerformed) {
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
    
    int numberOfGroupAttributesLength = 0;
    
    boolean groupNameThere = false;
    boolean groupIdThere = false;
    
    if (numberOfGroupAttributes != null) {
      
      numberOfGroupAttributesLength = GrouperUtil.intValue(numberOfGroupAttributes.getValueOrExpressionEvaluation(), 0);
      
      for (int i=0; i<numberOfGroupAttributesLength; i++) {
        
        GrouperConfigurationModuleAttribute attributeName = this.retrieveAttributes().get("targetGroupAttribute."+i+".name");
        String value = attributeName.getValueOrExpressionEvaluation();
        
        if (StringUtils.equals("name", value)) {
          groupNameThere = true;
        }
        if (StringUtils.equals("id", value)) {
          groupIdThere = true;
        } 
      }
    }
    
    if (!groupIdThere) {
      String errorMessage = GrouperTextContainer.textOrNull("grouperBoxProvisionerConfiugrationGroupIdRequired");
      errorsToDisplay.add(errorMessage);
    }

    if (!groupNameThere) {
      String errorMessage = GrouperTextContainer.textOrNull("grouperBoxProvisionerConfiugrationGroupNameRequired");
      errorsToDisplay.add(errorMessage);
    }
    
    
    GrouperConfigurationModuleAttribute numberOfEntityAttributes = this.retrieveAttributes().get("numberOfEntityAttributes");
    
    int numberOfEntityAttributesLength = 0;
    
    boolean entityNameThere = false;
    boolean entityLoginThere = false;
    boolean entityIdThere = false;
    
    if (numberOfEntityAttributes != null) {
      
      numberOfEntityAttributesLength = GrouperUtil.intValue(numberOfEntityAttributes.getValueOrExpressionEvaluation(), 0);
      
      for (int i=0; i<numberOfEntityAttributesLength; i++) {
        
        GrouperConfigurationModuleAttribute attributeName = this.retrieveAttributes().get("targetEntityAttribute."+i+".name");
        String value = attributeName.getValueOrExpressionEvaluation();
        
        if (StringUtils.equals("name", value)) {
          entityNameThere = true;
        }
        if (StringUtils.equals("id", value)) {
          entityIdThere = true;
        } 
        if (StringUtils.equals("login", value)) {
          entityLoginThere = true;
        } 
      }
    }
    
    if (!entityIdThere) {
      String errorMessage = GrouperTextContainer.textOrNull("grouperBoxProvisionerConfiugrationEntityIdRequired");
      errorsToDisplay.add(errorMessage);
    }

    if (!entityNameThere) {
      String errorMessage = GrouperTextContainer.textOrNull("grouperBoxProvisionerConfiugrationEntityNameRequired");
      errorsToDisplay.add(errorMessage);
    }
    
    if (!entityLoginThere) {
      String errorMessage = GrouperTextContainer.textOrNull("grouperBoxProvisionerConfiugrationEntityNameRequired");
      errorsToDisplay.add(errorMessage);
    }
    
  }
}
