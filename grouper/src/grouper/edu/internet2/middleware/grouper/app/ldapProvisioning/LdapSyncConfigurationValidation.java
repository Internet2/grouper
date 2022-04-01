package edu.internet2.middleware.grouper.app.ldapProvisioning;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttribute;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationValidation;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningValidationIssue;
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
    GrouperProvisioningConfiguration grouperProvisioningConfiguration = grouperProvisioner.retrieveGrouperProvisioningConfiguration();

    if (grouperProvisioningConfiguration.isOperateOnGrouperGroups()) {
      if (grouperProvisioningConfiguration.isSelectGroups() || grouperProvisioningConfiguration.isUpdateGroups() || grouperProvisioningConfiguration.isDeleteGroups() || grouperProvisioningConfiguration.isInsertGroups()) {
        GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = grouperProvisioningConfiguration.getTargetGroupAttributeNameToConfig().get(LdapProvisioningTargetDao.ldap_dn);
        if (grouperProvisioningConfigurationAttribute == null) {
          this.addErrorMessage(new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.mustHaveGroupDn")));
        }
        if (grouperProvisioningConfiguration.isSelectGroups() && grouperProvisioningConfigurationAttribute != null && !grouperProvisioningConfigurationAttribute.isSelect()) {
          this.addErrorMessage(new ProvisioningValidationIssue()
              .assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.mustSelectDn"))
              .assignJqueryHandle(grouperProvisioningConfigurationAttribute.configKey("select")));
        }
      }
    }
    if (grouperProvisioningConfiguration.isOperateOnGrouperEntities()) {
      if (grouperProvisioningConfiguration.isSelectEntities() || grouperProvisioningConfiguration.isUpdateEntities() || grouperProvisioningConfiguration.isDeleteEntities() || grouperProvisioningConfiguration.isInsertEntities()) {
        GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = grouperProvisioningConfiguration.getTargetEntityAttributeNameToConfig().get(LdapProvisioningTargetDao.ldap_dn);
        if (grouperProvisioningConfigurationAttribute == null) {
          this.addErrorMessage(new ProvisioningValidationIssue()
              .assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.mustHaveEntityDn")));
        }
        if (grouperProvisioningConfiguration.isSelectEntities() && grouperProvisioningConfigurationAttribute != null && !grouperProvisioningConfigurationAttribute.isSelect()) {
          this.addErrorMessage(new ProvisioningValidationIssue()
              .assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.mustSelectDn"))
              .assignJqueryHandle(grouperProvisioningConfigurationAttribute.configKey("select")));
        }
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
    GrouperProvisioningConfiguration grouperProvisioningConfiguration = grouperProvisioner.retrieveGrouperProvisioningConfiguration();

    if (grouperProvisioningConfiguration.isOperateOnGrouperGroups() && grouperProvisioningConfiguration.isInsertGroups()) {
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = grouperProvisioningConfiguration.getTargetGroupAttributeNameToConfig().get(LdapProvisioningTargetDao.ldap_dn);
      if (grouperProvisioningConfigurationAttribute != null && !grouperProvisioningConfigurationAttribute.isInsert()) {
        this.addErrorMessage(new ProvisioningValidationIssue()
            .assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.mustInsertDnIfInsertingGroups"))
            .assignJqueryHandle(grouperProvisioningConfigurationAttribute.configKey("insert")));
      }
    }
    if (grouperProvisioningConfiguration.isOperateOnGrouperEntities() && grouperProvisioningConfiguration.isInsertEntities()) {
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = grouperProvisioningConfiguration.getTargetEntityAttributeNameToConfig().get(LdapProvisioningTargetDao.ldap_dn);
      if (grouperProvisioningConfigurationAttribute != null && !grouperProvisioningConfigurationAttribute.isInsert()) {
        this.addErrorMessage(new ProvisioningValidationIssue()
            .assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.mustInsertDnIfInsertingEntities"))
            .assignJqueryHandle(grouperProvisioningConfigurationAttribute.configKey("insert")));
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
      
      if (StringUtils.equals(objectType, "targetGroupAttribute") && !GrouperUtil.booleanValue(this.getSuffixToConfigValue().get("selectGroups"), false)) {
        continue;
      }
      if (StringUtils.equals(objectType, "targetEntityAttribute") && !GrouperUtil.booleanValue(this.getSuffixToConfigValue().get("selectEntities"), false)) {
        continue;
      }
      
      if (StringUtils.equals("targetGroupAttribute", objectType)) {
        objectTypeLabel = GrouperTextContainer.textOrNull("auditsGroup");
      } else if (StringUtils.equals("targetEntityAttribute", objectType)) {
        objectTypeLabel = GrouperTextContainer.textOrNull("auditsEntity");
      } else {
        throw new RuntimeException("Cant find object type: " + objectType);
      }
      GrouperTextContainer.assignThreadLocalVariable("type", objectTypeLabel);

      for (int i=0; i< 20; i++) {
        // TODO validate DN
        //        if (i>0) {
        //          this.addErrorMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.dnRequired"));
        //        }
        //        continue OBJECT_TYPE;
          
//        String name = this.getSuffixToConfigValue().get(nameConfigKey);
//        String type = this.getSuffixToConfigValue().get(objectType + "."+i+".valueType");
//        
//        // all good, field with name LdapProvisioningTargetDao.ldap_dn and type string
//        if (StringUtils.equals(name, LdapProvisioningTargetDao.ldap_dn)) {
//          if (!StringUtils.isBlank(type) && !StringUtils.equalsIgnoreCase(type, "string")) {
//            this.addErrorMessageAndJqueryHandle(GrouperTextContainer.textOrNull("provisioning.configuration.validation.dnString"), nameConfigKey);
//          }
//          continue OBJECT_TYPE;
//        }
        
      }      
    }
    GrouperTextContainer.resetThreadLocalVariableMap();
    
  }
 
}
