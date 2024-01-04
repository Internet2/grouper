package edu.internet2.middleware.grouper.app.teamDynamix;

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


public class TeamDynamixProvisionerConfiguration extends ProvisioningConfiguration {
  
  public final static Set<String> startWithConfigClassNames = new LinkedHashSet<String>();
  
  static {
    startWithConfigClassNames.add(TeamDynamixProvisioningStartWith.class.getName());
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
    return TeamDynamixProvisioner.class.getName();
  }

  @Override
  public void validatePreSave(boolean isInsert, List<String> errorsToDisplay,
      Map<String, String> validationErrorsToDisplay) {
    
    super.validatePreSave(isInsert, errorsToDisplay, validationErrorsToDisplay);
    
    if (errorsToDisplay.size() > 0 || validationErrorsToDisplay.size() > 0) {
      return;
    } 
    
    GrouperConfigurationModuleAttribute entityMatchingAttributeSameAsSearchAttribute = this.retrieveAttributes().get("entityMatchingAttributeSameAsSearchAttribute");
    
    if (entityMatchingAttributeSameAsSearchAttribute != null) {
      boolean error = true;
      boolean entitySameAsSearchAttribute = GrouperUtil.booleanValue(entityMatchingAttributeSameAsSearchAttribute.getValueOrExpressionEvaluation(), true);
      if (entitySameAsSearchAttribute) {
        GrouperConfigurationModuleAttribute entityMatchingAttributeCountAttribute = this.retrieveAttributes().get("entityMatchingAttributeCount");
        if (entityMatchingAttributeCountAttribute != null) {
          int matchingSearchAttributeCount = GrouperUtil.intValue(entityMatchingAttributeCountAttribute.getValueOrExpressionEvaluation());
          
          for (int i=0; i<matchingSearchAttributeCount; i++) {
            
            GrouperConfigurationModuleAttribute attributeName = this.retrieveAttributes().get("entityMatchingAttribute"+i+"name");
            String value = attributeName.getValueOrExpressionEvaluation();
            
            if (StringUtils.equals("id", value) || StringUtils.equals("ExternalID", value)) {
              error = false;
            }
          
          }
        }
        
      } else {
        
        GrouperConfigurationModuleAttribute entitySearchAttributeCountAttribute = this.retrieveAttributes().get("entitySearchAttributeCount");
        if (entitySearchAttributeCountAttribute != null) {
          int matchingSearchAttributeCount = GrouperUtil.intValue(entitySearchAttributeCountAttribute.getValueOrExpressionEvaluation());
          
          for (int i=0; i<matchingSearchAttributeCount; i++) {
            
            GrouperConfigurationModuleAttribute attributeName = this.retrieveAttributes().get("entitySearchAttribute"+i+"name");
            String value = attributeName.getValueOrExpressionEvaluation();
            
            if (StringUtils.equals("id", value) || StringUtils.equals("ExternalID", value)) {
              error = false;
            }
          
          }
        }
        
      }
      
      if (error) {
        String errorMessage = GrouperTextContainer.textOrNull("grouperTeamDynamixProvisionerConfiugrationEntitySearchAttribute");
        errorsToDisplay.add(errorMessage);
      }
    }
    
  }

}
