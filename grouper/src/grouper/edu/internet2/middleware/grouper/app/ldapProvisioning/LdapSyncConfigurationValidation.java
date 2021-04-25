package edu.internet2.middleware.grouper.app.ldapProvisioning;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttribute;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationBase;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationValidation;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;


public class LdapSyncConfigurationValidation
    extends GrouperProvisioningConfigurationValidation {

  public LdapSyncConfigurationValidation() {
  }

  @Override
  public List<MultiKey> validateFromSuffixValueMap(
      Map<String, String> suffixToConfigValue) {
    List<MultiKey> errorMessagesAndConfigSuffixes = GrouperUtil.nonNull(super.validateFromSuffixValueMap(suffixToConfigValue));
    
    addToResultsIfNotNull(errorMessagesAndConfigSuffixes, validateDnExistsAndString(suffixToConfigValue));

    return errorMessagesAndConfigSuffixes;
  }

  /**
   * validate from the grouper provisioner
   * @return the 
   */
  public List<MultiKey> validateFromObjectModel() {
    
    List<MultiKey> errorMessagesAndConfigSuffixes = GrouperUtil.nonNull(super.validateFromObjectModel());
    addToResultsIfNotNull(errorMessagesAndConfigSuffixes, validateDnSelect());
    return errorMessagesAndConfigSuffixes;

  }

  /**
   * make sure attribute names arent re-used
   * @param suffixToConfigValue
   * @return 
   */
  public List<MultiKey> validateDnSelect() {
    
    List<MultiKey> result = new ArrayList<MultiKey>();

    GrouperProvisioner grouperProvisioner = this.getGrouperProvisioner();
    GrouperProvisioningConfigurationBase grouperProvisioningConfiguration = grouperProvisioner.retrieveGrouperProvisioningConfiguration();

    if (grouperProvisioningConfiguration.isOperateOnGrouperGroups()) {
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = grouperProvisioningConfiguration.getTargetGroupFieldNameToConfig().get("name");
      if (grouperProvisioningConfigurationAttribute == null) {
        result.add(new MultiKey(new Object[] {GrouperTextContainer.textOrNull("provisioning.configuration.validation.mustHaveGroupDn")}));
      }
      if (grouperProvisioningConfigurationAttribute != null && !grouperProvisioningConfigurationAttribute.isSelect()) {
        result.add(new MultiKey(new Object[] {GrouperTextContainer.textOrNull("provisioning.configuration.validation.mustSelectDn"), htmlJqueryHandle(grouperProvisioningConfigurationAttribute.configKey("select"))}));
      }
    }
    if (grouperProvisioningConfiguration.isOperateOnGrouperEntities()) {
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = grouperProvisioningConfiguration.getTargetEntityFieldNameToConfig().get("name");
      if (grouperProvisioningConfigurationAttribute == null) {
        result.add(new MultiKey(new Object[] {GrouperTextContainer.textOrNull("provisioning.configuration.validation.mustHaveEntityDn")}));
      }
      if (grouperProvisioningConfigurationAttribute != null && !grouperProvisioningConfigurationAttribute.isSelect()) {
        result.add(new MultiKey(new Object[] {GrouperTextContainer.textOrNull("provisioning.configuration.validation.mustSelectDn"), htmlJqueryHandle(grouperProvisioningConfigurationAttribute.configKey("select"))}));
      }
    }
    
    return result;
    
  }

  /**
   * make sure attribute names arent re-used
   * @param suffixToConfigValue
   * @return 
   */
  public List<MultiKey> validateDnExistsAndString(Map<String, String> suffixToConfigValue) {
    
    List<MultiKey> result = new ArrayList<MultiKey>();

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

        Boolean isField = GrouperUtil.booleanObjectValue(suffixToConfigValue.get(objectType + "."+i+".isFieldElseAttribute"));
        if (isField == null) {
          if (i>0) {
            result.add(new MultiKey(new Object[] {GrouperTextContainer.textOrNull("provisioning.configuration.validation.dnRequired")}));
          }
          continue OBJECT_TYPE;
          
        }
        if (!isField) {
          continue;
        }

        String nameConfigKey = objectType + "."+i+".fieldName";
        String name = suffixToConfigValue.get(nameConfigKey);
        String type = suffixToConfigValue.get(objectType + "."+i+".valueType");
        
        // all good, field with name "name" and type string
        if (StringUtils.equals(name, "name")) {
          if (!StringUtils.isBlank(type) && !StringUtils.equalsIgnoreCase(type, "string")) {
            result.add(new MultiKey(new Object[] {GrouperTextContainer.textOrNull("provisioning.configuration.validation.dnString"), htmlJqueryHandle(nameConfigKey)}));
          }
          continue OBJECT_TYPE;
        }
        
      }      
    }
    GrouperTextContainer.resetThreadLocalVariableMap();
    return result;
    
  }
 
}
