package edu.internet2.middleware.grouper.app.ldapProvisioning;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBehavior;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttribute;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttributeType;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttributeValueType;
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
    validateOnlyDnOverrideHasDnMatching();
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
    GrouperProvisioningBehavior grouperProvisioningBehavior = grouperProvisioner.retrieveGrouperProvisioningBehavior();

    if (grouperProvisioningBehavior.isSelectGroups() || grouperProvisioningBehavior.isUpdateGroups() || grouperProvisioningBehavior.isDeleteGroups() || grouperProvisioningBehavior.isInsertGroups()) {
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
    if (grouperProvisioningBehavior.isSelectEntities() || grouperProvisioningBehavior.isUpdateEntities() || grouperProvisioningBehavior.isDeleteEntities() || grouperProvisioningBehavior.isInsertEntities()) {
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

  
  
  /**
   * make sure attribute names arent re-used
   * @param suffixToConfigValue
   * @return 
   */
  public void validateDnInsertIfInsertObject() {
    
    GrouperProvisioner grouperProvisioner = this.getGrouperProvisioner();
    GrouperProvisioningConfiguration grouperProvisioningConfiguration = grouperProvisioner.retrieveGrouperProvisioningConfiguration();
    GrouperProvisioningBehavior grouperProvisioningBehavior = grouperProvisioner.retrieveGrouperProvisioningBehavior();

    if (grouperProvisioningBehavior.isInsertGroups()) {
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = grouperProvisioningConfiguration.getTargetGroupAttributeNameToConfig().get(LdapProvisioningTargetDao.ldap_dn);
      if (grouperProvisioningConfigurationAttribute != null && !grouperProvisioningConfigurationAttribute.isInsert()) {
        this.addErrorMessage(new ProvisioningValidationIssue()
            .assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.mustInsertDnIfInsertingGroups"))
            .assignJqueryHandle(grouperProvisioningConfigurationAttribute.configKey("insert")));
      }
    }
    
    if (grouperProvisioningBehavior.isInsertEntities()) {
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
   */
  public void validateDnExistsAndString() {
    
    GrouperProvisioningBehavior grouperProvisioningBehavior = this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior();
    GrouperProvisioningConfiguration grouperProvisioningConfiguration = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    
    String objectTypeLabel = null;
    
    if (grouperProvisioningBehavior.isSelectGroups()) {
      objectTypeLabel = GrouperTextContainer.textOrNull("auditsGroup");
      GrouperTextContainer.assignThreadLocalVariable("type", objectTypeLabel);

      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = grouperProvisioningConfiguration.getTargetGroupAttributeNameToConfig()
          .get(LdapProvisioningTargetDao.ldap_dn);
      if (grouperProvisioningConfigurationAttribute == null) {
        this.addErrorMessage(new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.dnRequired")));
      } else {
        if (grouperProvisioningConfigurationAttribute.getValueType() != GrouperProvisioningConfigurationAttributeValueType.STRING) {
          this.addErrorMessage(new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.dnString"))
              .assignJqueryHandle(grouperProvisioningConfigurationAttribute.configKey("name")));
        }
      }
    }

    if (grouperProvisioningBehavior.isSelectEntities()) {
      objectTypeLabel = GrouperTextContainer.textOrNull("auditsEntity");
      GrouperTextContainer.assignThreadLocalVariable("type", objectTypeLabel);

      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = grouperProvisioningConfiguration.getTargetEntityAttributeNameToConfig()
          .get(LdapProvisioningTargetDao.ldap_dn);
      if (grouperProvisioningConfigurationAttribute == null) {
        this.addErrorMessage(new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.dnRequired")));
      } else {
        if (grouperProvisioningConfigurationAttribute.getValueType() != GrouperProvisioningConfigurationAttributeValueType.STRING) {
          this.addErrorMessage(new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.dnString"))
              .assignJqueryHandle(grouperProvisioningConfigurationAttribute.configKey("name")));
        }
      }
    }
      
    GrouperTextContainer.resetThreadLocalVariableMap();
    
  }

  /**
   * make sure attribute names arent re-used
   */
  public void validateOnlyDnOverrideHasDnMatching() {

    GrouperProvisioningConfiguration grouperProvisioningConfiguration = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

    boolean onlyLdapGroupDnOverride = GrouperUtil.booleanValue(this.getSuffixToConfigValue().get("onlyLdapGroupDnOverride"), false);
    if (onlyLdapGroupDnOverride) {
      boolean groupMatchingAttributeSameAsSearchAttribute = GrouperUtil.booleanValue(this.getSuffixToConfigValue().get("groupMatchingAttributeSameAsSearchAttribute"), true);
      if (!groupMatchingAttributeSameAsSearchAttribute) {
        this.addErrorMessage(new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.onlyDnOverrideMatchingSameAsSearch"))
            .assignJqueryHandle("groupMatchingAttributeSameAsSearchAttribute"));
      } else {
        int groupMatchingAttributeCount = GrouperUtil.intValue(this.getSuffixToConfigValue().get("groupMatchingAttributeCount"), 0);
        if (groupMatchingAttributeCount != 1) {
          this.addErrorMessage(new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.onlyDnOverrideHasOneMatchingAttribute"))
              .assignJqueryHandle("groupMatchingAttributeCount"));
        } else {
          String groupMatchingAttribute0name = this.getSuffixToConfigValue().get("groupMatchingAttribute0name");
          if (!StringUtils.equals("ldap_dn", groupMatchingAttribute0name)) {
            this.addErrorMessage(new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.onlyDnOverrideHasLdapDnMatchingAttribute"))
                .assignJqueryHandle("groupMatchingAttribute0name"));
          }
        }
      }
    }
      
    GrouperTextContainer.resetThreadLocalVariableMap();
    
  }
}
