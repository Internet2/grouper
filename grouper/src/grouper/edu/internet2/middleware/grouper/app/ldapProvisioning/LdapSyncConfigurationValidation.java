package edu.internet2.middleware.grouper.app.ldapProvisioning;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttribute;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationBase;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationValidation;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;


public class LdapSyncConfigurationValidation
    extends GrouperProvisioningConfigurationValidation {

  public LdapSyncConfigurationValidation() {
  }

  @Override
  public void validateFromSuffixValueMap() {
    super.validateFromSuffixValueMap();
    
    validateDnExistsAndString();

  }

  /**
   * validate from the grouper provisioner
   * @return the 
   */
  @Override
  public void validateFromObjectModel() {
    
    super.validateFromObjectModel();
    validateDnSelect();
    validateDnInsertIfInsertObject();

  }

  /**
   * make sure attribute names arent re-used
   * @param suffixToConfigValue
   * @return 
   */
  public void validateDnSelect() {
    
    GrouperProvisioner grouperProvisioner = this.getGrouperProvisioner();
    GrouperProvisioningConfigurationBase grouperProvisioningConfiguration = grouperProvisioner.retrieveGrouperProvisioningConfiguration();

    if (grouperProvisioningConfiguration.isOperateOnGrouperGroups()) {
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = grouperProvisioningConfiguration.getTargetGroupFieldNameToConfig().get("name");
      if (grouperProvisioningConfigurationAttribute == null) {
        this.addErrorMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.mustHaveGroupDn"));
      }
      if (grouperProvisioningConfigurationAttribute != null && !grouperProvisioningConfigurationAttribute.isSelect()) {
        this.addErrorMessageAndJqueryHandle(GrouperTextContainer.textOrNull("provisioning.configuration.validation.mustSelectDn"), grouperProvisioningConfigurationAttribute.configKey("select"));
      }
    }
    if (grouperProvisioningConfiguration.isOperateOnGrouperEntities()) {
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = grouperProvisioningConfiguration.getTargetEntityFieldNameToConfig().get("name");
      if (grouperProvisioningConfigurationAttribute == null) {
        this.addErrorMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.mustHaveEntityDn"));
      }
      if (grouperProvisioningConfigurationAttribute != null && !grouperProvisioningConfigurationAttribute.isSelect()) {
        this.addErrorMessageAndJqueryHandle(GrouperTextContainer.textOrNull("provisioning.configuration.validation.mustSelectDn"), grouperProvisioningConfigurationAttribute.configKey("select"));
      }
    }
    
  }

  
  
  /**
   * make sure attribute names arent re-used
   * @param suffixToConfigValue
   * @return 
   */
  public void validateDnInsertIfInsertObject() {
    
    GrouperProvisioner grouperProvisioner = this.getGrouperProvisioner();
    GrouperProvisioningConfigurationBase grouperProvisioningConfiguration = grouperProvisioner.retrieveGrouperProvisioningConfiguration();

    if (grouperProvisioningConfiguration.isOperateOnGrouperGroups() && grouperProvisioningConfiguration.isInsertGroups()) {
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = grouperProvisioningConfiguration.getTargetGroupFieldNameToConfig().get("name");
      if (grouperProvisioningConfigurationAttribute != null && !grouperProvisioningConfigurationAttribute.isInsert()) {
        this.addErrorMessageAndJqueryHandle(GrouperTextContainer.textOrNull("provisioning.configuration.validation.mustInsertDnIfInsertingGroups"), grouperProvisioningConfigurationAttribute.configKey("insert"));
      }
    }
    if (grouperProvisioningConfiguration.isOperateOnGrouperEntities() && grouperProvisioningConfiguration.isInsertEntities()) {
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = grouperProvisioningConfiguration.getTargetEntityFieldNameToConfig().get("name");
      if (grouperProvisioningConfigurationAttribute != null && !grouperProvisioningConfigurationAttribute.isInsert()) {
        this.addErrorMessageAndJqueryHandle(GrouperTextContainer.textOrNull("provisioning.configuration.validation.mustInsertDnIfInsertingEntities"), grouperProvisioningConfigurationAttribute.configKey("insert"));
      }
    }

  }

  /**
   * make sure attribute names arent re-used
   * @param suffixToConfigValue
   * @return 
   */
  public void validateDnExistsAndString() {
    
    OBJECT_TYPE: for (String objectType: new String[] {"targetGroupAttribute", "targetEntityAttribute"}) {
      
      String objectTypeLabel = null;
      
      if (StringUtils.equals("targetGroupAttribute", objectType)) {
        objectTypeLabel = GrouperTextContainer.textOrNull("auditsGroup");
      } else if (StringUtils.equals("targetEntityAttribute", objectType)) {
        objectTypeLabel = GrouperTextContainer.textOrNull("auditsEntity");
      } else {
        throw new RuntimeException("Cant find object type: " + objectType);
      }
      GrouperTextContainer.assignThreadLocalVariable("type", objectTypeLabel);

      for (int i=0; i< 20; i++) {

        Boolean isField = GrouperUtil.booleanObjectValue(this.getSuffixToConfigValue().get(objectType + "."+i+".isFieldElseAttribute"));
        if (isField == null) {
          if (i>0) {
            this.addErrorMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.dnRequired"));
          }
          continue OBJECT_TYPE;
          
        }
        if (!isField) {
          continue;
        }

        String nameConfigKey = objectType + "."+i+".fieldName";
        String name = this.getSuffixToConfigValue().get(nameConfigKey);
        String type = this.getSuffixToConfigValue().get(objectType + "."+i+".valueType");
        
        // all good, field with name "name" and type string
        if (StringUtils.equals(name, "name")) {
          if (!StringUtils.isBlank(type) && !StringUtils.equalsIgnoreCase(type, "string")) {
            this.addErrorMessageAndJqueryHandle(GrouperTextContainer.textOrNull("provisioning.configuration.validation.dnString"), nameConfigKey);
          }
          continue OBJECT_TYPE;
        }
        
      }      
    }
    GrouperTextContainer.resetThreadLocalVariableMap();
    
  }
 
}
