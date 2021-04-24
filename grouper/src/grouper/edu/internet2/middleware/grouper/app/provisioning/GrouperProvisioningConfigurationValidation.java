package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;

/**
 * this could be a deep validate though
 * @author mchyzer
 */
public class GrouperProvisioningConfigurationValidation {

  private ProvisionerConfiguration provisionerConfiguration = null;
  
  private GrouperProvisioner grouperProvisioner = null;
  
  public GrouperProvisioner getGrouperProvisioner() {
    return grouperProvisioner;
  }
  
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner) {
    this.grouperProvisioner = grouperProvisioner;
  }

  /**
   * take the config for this provisioner and validate
   * @return error message, and optionally a config suffix that has the problem
   */
  public List<MultiKey> validateFromConfig() {
    
    Map<String, String> suffixToConfigValue = new HashMap<String, String>();
    
    String configId = this.getGrouperProvisioner().getConfigId();
    
    String configPrefix = "provisioner." + configId + ".";
    
    GrouperLoaderConfig grouperLoaderConfig = GrouperLoaderConfig.retrieveConfig();
    for (String key : grouperLoaderConfig.propertyNames()) {
      
      if (key.startsWith(configPrefix)) {
        
        String suffix = GrouperUtil.prefixOrSuffix(key, configPrefix, false);
        String value = grouperLoaderConfig.propertyValueString(key);
        suffixToConfigValue.put(suffix, value);
        
        
      }
      
    }
    
    return validateFromSuffixValueMap(suffixToConfigValue);
  }

  //TODO get the provisioner configuration and validate from there too when running the provisioner (required, types, etc)
  
  /**
   * 
   * @return error message, and optionally a config suffix that has the problem
   */
  public List<MultiKey> validateFromSuffixValueMap(Map<String, String> suffixToConfigValue) {
    
    suffixToConfigValue = GrouperUtil.nonNull(suffixToConfigValue);
    
    List<MultiKey> errorMessagesAndConfigSuffixes = new ArrayList<MultiKey>();

    addToResultsIfNotNull(errorMessagesAndConfigSuffixes, validateConfigBasics(suffixToConfigValue));
    
    // if there are problems with the basics, then other things could throw exceptions
    if (errorMessagesAndConfigSuffixes.size() > 0) {
      return errorMessagesAndConfigSuffixes;
    }

    addToResultsIfNotNull(errorMessagesAndConfigSuffixes, validateDoingSomething(suffixToConfigValue));
    addToResultsIfNotNull(errorMessagesAndConfigSuffixes, validateGroupDeleteHasDeleteType(suffixToConfigValue));
    addToResultsIfNotNull(errorMessagesAndConfigSuffixes, validateMembershipDeleteHasDeleteType(suffixToConfigValue));
    addToResultsIfNotNull(errorMessagesAndConfigSuffixes, validateEntityDeleteHasDeleteType(suffixToConfigValue));
    addToResultsIfNotNull(errorMessagesAndConfigSuffixes, validateGroupLinkHasConfiguration(suffixToConfigValue));
    addToResultsIfNotNull(errorMessagesAndConfigSuffixes, validateEntityLinkHasConfiguration(suffixToConfigValue));
    addToResultsIfNotNull(errorMessagesAndConfigSuffixes, validateGroupLinkOnePerBucket(suffixToConfigValue));
    addToResultsIfNotNull(errorMessagesAndConfigSuffixes, validateEntityLinkOnePerBucket(suffixToConfigValue));
    addToResultsIfNotNull(errorMessagesAndConfigSuffixes, validateNoUnsedConfigs(suffixToConfigValue));
    addToResultsIfNotNull(errorMessagesAndConfigSuffixes, validateAttributeNamesNotReused(suffixToConfigValue));
    addToResultsIfNotNull(errorMessagesAndConfigSuffixes, validateAttributeCount(suffixToConfigValue));
    addToResultsIfNotNull(errorMessagesAndConfigSuffixes, validateGroupIdToProvisionExists(suffixToConfigValue));
    
    return errorMessagesAndConfigSuffixes;
    
  }
  
  // hasTargetGroupLink
  // if target group link then there should be a copy to sync field or a translation

  public MultiKey validateDoingSomething(Map<String, String> suffixToConfigValue) {
    
    boolean operateOnGrouperEntities = GrouperUtil.booleanValue(suffixToConfigValue.get("operateOnGrouperEntities"), false);
    boolean operateOnGrouperGroups = GrouperUtil.booleanValue(suffixToConfigValue.get("operateOnGrouperGroups"), false);
    boolean operateOnGrouperMemberships = GrouperUtil.booleanValue(suffixToConfigValue.get("operateOnGrouperMemberships"), false);
    
    if (!operateOnGrouperEntities && !operateOnGrouperGroups && !operateOnGrouperMemberships) {
      return new MultiKey(new Object[] {GrouperTextContainer.textOrNull("provisioning.configuration.validation.doSomething")});
    }
    return null;
  }

  /**
   * 
   * @param suffixToConfigValue
   * @return
   */
  public MultiKey validateGroupIdToProvisionExists(
      Map<String, String> suffixToConfigValue) {

    String groupIdOfUsersToProvision = suffixToConfigValue.get("groupIdOfUsersToProvision");
    
    if (!StringUtils.isBlank(groupIdOfUsersToProvision)) {
       if (null == GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), groupIdOfUsersToProvision, false)) {
         
         return new MultiKey(GrouperTextContainer.textOrNull("provisioning.configuration.validation.groupIdOfUsersToProvisionNotExist"), "groupIdOfUsersToProvision");
       }
    
    }
    return null;
  }

  /**
   * if there is a group delete, then there must be one delete type
   * @param suffixToConfigValue
   * @return 
   */
  public MultiKey validateGroupDeleteHasDeleteType(Map<String, String> suffixToConfigValue) {
    
    boolean deleteGroups = GrouperUtil.booleanValue(suffixToConfigValue.get("deleteGroups"), false);
    if (deleteGroups) {
      
      boolean deleteGroupsIfNotExistInGrouper = GrouperUtil.booleanValue(suffixToConfigValue.get("deleteGroupsIfNotExistInGrouper"), false);
      boolean deleteGroupsIfGrouperDeleted = GrouperUtil.booleanValue(suffixToConfigValue.get("deleteGroupsIfGrouperDeleted"), false);
      boolean deleteGroupsIfGrouperCreated = GrouperUtil.booleanValue(suffixToConfigValue.get("deleteGroupsIfGrouperCreated"), false);
      
      int deleteTypes = 0;
      if (deleteGroupsIfNotExistInGrouper) {
        deleteTypes++;        
      }
      if (deleteGroupsIfGrouperDeleted) {
        deleteTypes++;        
      }
      if (deleteGroupsIfGrouperCreated) {
        deleteTypes++;        
      }
      
      if (deleteTypes != 1) {
        return new MultiKey(GrouperTextContainer.textOrNull("provisioning.configuration.validation.oneGroupDeleteType"), "deleteGroups");
      }
    }
    return null;
    
  }
  
  /**
   * if there is a group delete, then there must be one delete type
   * @param suffixToConfigValue
   * @return 
   */
  public MultiKey validateMembershipDeleteHasDeleteType(Map<String, String> suffixToConfigValue) {
    
    boolean deleteMemberships = GrouperUtil.booleanValue(suffixToConfigValue.get("deleteMemberships"), false);
    if (deleteMemberships) {
      
      boolean deleteMembershipsIfNotExistInGrouper = GrouperUtil.booleanValue(suffixToConfigValue.get("deleteMembershipsIfNotExistInGrouper"), false);
      boolean deleteMembershipsIfGrouperDeleted = GrouperUtil.booleanValue(suffixToConfigValue.get("deleteMembershipsIfGrouperDeleted"), false);
      boolean deleteMembershipsIfGrouperCreated = GrouperUtil.booleanValue(suffixToConfigValue.get("deleteMembershipsIfGrouperCreated"), false);
      
      int deleteTypes = 0;
      if (deleteMembershipsIfNotExistInGrouper) {
        deleteTypes++;        
      }
      if (deleteMembershipsIfGrouperDeleted) {
        deleteTypes++;        
      }
      if (deleteMembershipsIfGrouperCreated) {
        deleteTypes++;        
      }
      
      if (deleteTypes != 1) {
        return new MultiKey(GrouperTextContainer.textOrNull("provisioning.configuration.validation.oneMembershipDeleteType"), "deleteMemberships");
      }
    }
    return null;
    
  }
  
  public void addToResultsIfNotNull(List<MultiKey> errorMessagesAndConfigSuffixes, MultiKey errorMessageAndConfigSuffix) {
    if (errorMessageAndConfigSuffix != null) {
      errorMessagesAndConfigSuffixes.add(errorMessageAndConfigSuffix);
    }
  }

  public void addToResultsIfNotNull(List<MultiKey> errorMessagesAndConfigSuffixes, List<MultiKey> errorMessageAndConfigSuffixes) {
    if (GrouperUtil.length(errorMessageAndConfigSuffixes) > 0) {
      errorMessagesAndConfigSuffixes.addAll(errorMessageAndConfigSuffixes);
    }
  }

  /**
   * if there is a group delete, then there must be one delete type
   * @param suffixToConfigValue
   * @return 
   */
  public MultiKey validateEntityDeleteHasDeleteType(Map<String, String> suffixToConfigValue) {
    
    boolean deleteEntities = GrouperUtil.booleanValue(suffixToConfigValue.get("deleteEntities"), false);
    if (deleteEntities) {
      
      boolean deleteEntitiesIfNotExistInGrouper = GrouperUtil.booleanValue(suffixToConfigValue.get("deleteEntitiesIfNotExistInGrouper"), false);
      boolean deleteEntitiesIfGrouperDeleted = GrouperUtil.booleanValue(suffixToConfigValue.get("deleteEntitiesIfGrouperDeleted"), false);
      boolean deleteEntitiesIfGrouperCreated = GrouperUtil.booleanValue(suffixToConfigValue.get("deleteEntitiesIfGrouperCreated"), false);
      
      int deleteTypes = 0;
      if (deleteEntitiesIfNotExistInGrouper) {
        deleteTypes++;        
      }
      if (deleteEntitiesIfGrouperDeleted) {
        deleteTypes++;        
      }
      if (deleteEntitiesIfGrouperCreated) {
        deleteTypes++;        
      }
      
      if (deleteTypes != 1) {
        return new MultiKey(GrouperTextContainer.textOrNull("provisioning.configuration.validation.oneEntityDeleteType"), "deleteEntities");
      }
    }
    return null;
    
  }

  // hasTargetGroupLink
  // if target group link then there should be a copy to sync field or a translation
  
  /**
   * if there is a group delete, then there must be one delete type
   * @param suffixToConfigValue
   * @return 
   */
  public MultiKey validateGroupLinkHasConfiguration(Map<String, String> suffixToConfigValue) {
    
    boolean hasTargetGroupLink = GrouperUtil.booleanValue(suffixToConfigValue.get("hasTargetGroupLink"), false);
    if (hasTargetGroupLink) {
      
      // check attributes
      for (int i=0; i< 20; i++) {
  
        Boolean isField = GrouperUtil.booleanObjectValue(suffixToConfigValue.get("targetGroupAttribute."+i+".isFieldElseAttribute"));
        if (isField == null) {
          break;
        }
        
        // there is an attribute here
        String translateToGroupSyncField = suffixToConfigValue.get("targetGroupAttribute."+i+".translateToGroupSyncField");
        
        if (!StringUtils.isBlank(translateToGroupSyncField)) {
          return null;
        }
        
      }
      
      // check scripts
      if (!StringUtils.isBlank(suffixToConfigValue.get("common.groupLink.groupFromId2"))) {
        return null;
      }
      
      if (!StringUtils.isBlank(suffixToConfigValue.get("common.groupLink.groupFromId3"))) {
        return null;
      }

      if (!StringUtils.isBlank(suffixToConfigValue.get("common.groupLink.groupToId2"))) {
        return null;
      }

      if (!StringUtils.isBlank(suffixToConfigValue.get("common.groupLink.groupToId3"))) {
        return null;
      }
      
      return new MultiKey(GrouperTextContainer.textOrNull("provisioning.configuration.validation.targetGroupLinkNeedsConfig"), "hasTargetGroupLink");
    }
    return null;
    
  }

  // hasTargetGroupLink
  // if target group link then there should be a copy to sync field or a translation
  
  /**
   * if there is a entity delete, then there must be one delete type
   * @param suffixToConfigValue
   * @return 
   */
  public MultiKey validateEntityLinkHasConfiguration(Map<String, String> suffixToConfigValue) {
    
    boolean hasTargetEntityLink = GrouperUtil.booleanValue(suffixToConfigValue.get("hasTargetEntityLink"), false);
    if (hasTargetEntityLink) {
      
      // check attributes
      for (int i=0; i< 20; i++) {
  
        Boolean isField = GrouperUtil.booleanObjectValue(suffixToConfigValue.get("targetEntityAttribute."+i+".isFieldElseAttribute"));
        if (isField == null) {
          break;
        }
        
        // there is an attribute here
        String translateToEntitySyncField = suffixToConfigValue.get("targetEntityAttribute."+i+".translateToMemberSyncField");
        
        if (!StringUtils.isBlank(translateToEntitySyncField)) {
          return null;
        }
        
      }
      
      // check scripts
      if (!StringUtils.isBlank(suffixToConfigValue.get("common.entityLink.memberFromId2"))) {
        return null;
      }
      
      if (!StringUtils.isBlank(suffixToConfigValue.get("common.entityLink.memberFromId3"))) {
        return null;
      }
  
      if (!StringUtils.isBlank(suffixToConfigValue.get("common.entityLink.memberToId2"))) {
        return null;
      }
  
      if (!StringUtils.isBlank(suffixToConfigValue.get("common.entityLink.mmeberToId3"))) {
        return null;
      }
      
      return new MultiKey(GrouperTextContainer.textOrNull("provisioning.configuration.validation.targetEntityLinkNeedsConfig"), "hasTargetEntityLink");
    }
    return null;
    
  }

  /**
   * if there is a group link, then make sure multiple things arent going to the same bucket
   * @param suffixToConfigValue
   * @return error messages
   */
  public List<MultiKey> validateGroupLinkOnePerBucket(Map<String, String> suffixToConfigValue) {

    boolean hasTargetGroupLink = GrouperUtil.booleanValue(suffixToConfigValue.get("hasTargetGroupLink"), false);
    if (!hasTargetGroupLink) {
      return null;
    }
    List<MultiKey> result = new ArrayList<MultiKey>();

    String errorMessage = GrouperTextContainer.textOrNull("provisioning.configuration.validation.targetGroupLinkMultipleToSameBucket");
    
    for (String bucket : new String[] {"groupFromId2", "groupFromId3", "groupToId2", "groupToId3"}) {
      
      List<MultiKey> currentErrors = new ArrayList<MultiKey>();

      // check attributes
      for (int i=0; i< 20; i++) {
  
        Boolean isField = GrouperUtil.booleanObjectValue(suffixToConfigValue.get("targetGroupAttribute."+i+".isFieldElseAttribute"));
        if (isField == null) {
          break;
        }
        
        // there is an attribute here
        String suffix = "targetGroupAttribute."+i+".translateToGroupSyncField";
        String translateToGroupSyncField = suffixToConfigValue.get(suffix);
        
        if (StringUtils.equals(bucket, translateToGroupSyncField)) {
          currentErrors.add(new MultiKey(errorMessage, suffix));
        }
        
      }

      String suffix = "common.groupLink." + bucket;
      if (!StringUtils.isBlank(suffixToConfigValue.get(suffix))) {
        currentErrors.add(new MultiKey(errorMessage, suffix));
      }
      
      if (currentErrors.size() > 1) {
        result.addAll(currentErrors);
      }
    }
    return result;
    
  }

  /**
   * 
   * @return
   */
  public ProvisionerConfiguration retrieveProvisionerConfiguration(Map<String, String> suffixToConfigValue) {
    if (this.provisionerConfiguration == null) {
      String configSuffixThatIdentifiesThisProvisioner = suffixToConfigValue.get("class");

      provisionerConfiguration = StringUtils.isBlank(configSuffixThatIdentifiesThisProvisioner) ? null 
          : ProvisionerConfiguration.retrieveConfigurationByConfigSuffix(configSuffixThatIdentifiesThisProvisioner);
      if (this.provisionerConfiguration != null) {
        String configId = this.getGrouperProvisioner().getConfigId();
        if (StringUtils.isBlank(configId)) {
          configId = "someConfigIdThatWontConflict";
        }
        
        provisionerConfiguration.setConfigId(configId);

        for (GrouperConfigurationModuleAttribute grouperConfigurationModuleAttribute : provisionerConfiguration.retrieveAttributes().values()) {
          String currentValue = suffixToConfigValue.get(grouperConfigurationModuleAttribute.getConfigSuffix());
          if (currentValue != null) {
            grouperConfigurationModuleAttribute.setValue(currentValue);
          }
        }

      }
    }
    return provisionerConfiguration;
  }


  /**
   * check to make sure all configs are used and none are extraneous
   * @param suffixToConfigValue
   * @return error messages
   */
  public List<MultiKey> validateNoUnsedConfigs(Map<String, String> suffixToConfigValue) {

    List<MultiKey> result = new ArrayList<MultiKey>();

    // remove from this map the valid keys, and the invalid ones will remain
    Set<String> keysUsed = new HashSet<String>(suffixToConfigValue.keySet());
    
    // dont use the real config since it adds non-example configs
    String configSuffixThatIdentifiesThisProvisioner = suffixToConfigValue.get("class");

    ProvisionerConfiguration provisionerConfiguration = StringUtils.isBlank(configSuffixThatIdentifiesThisProvisioner) ? null 
        : ProvisionerConfiguration.retrieveConfigurationByConfigSuffix(configSuffixThatIdentifiesThisProvisioner);
    if (this.provisionerConfiguration != null) {
      String configId = "someConfigIdThatWontConflict";
      
      provisionerConfiguration.setConfigId(configId);

    }

    
    if (provisionerConfiguration == null) {
      String errorMessage = GrouperTextContainer.textOrNull("provisioning.configuration.validation.invalidClass");
      result.add(new MultiKey(errorMessage, "class"));
      return result;
    }
    keysUsed.removeAll(provisionerConfiguration.retrieveAttributes().keySet());

    for (String keyNotUsed : keysUsed) {
      
      // provisioning.configuration.validation.extraneousConfigs = Error: there is an extraneous config for this provisioner: ${extraneousConfig}
      GrouperTextContainer.assignThreadLocalVariable("extraneousConfig", keyNotUsed);
      String errorMessage = GrouperTextContainer.textOrNull("provisioning.configuration.validation.extraneousConfigs");
      result.add(new MultiKey(new Object[] {errorMessage}));
      GrouperTextContainer.resetThreadLocalVariableMap();
    }
    
    return result;
    
  }
  
  /**
   * if there is an entity link, then make sure multiple things arent going to the same bucket
   * @param suffixToConfigValue
   * @return error messages
   */
  public List<MultiKey> validateEntityLinkOnePerBucket(Map<String, String> suffixToConfigValue) {
  
    boolean hasTargetEntityLink = GrouperUtil.booleanValue(suffixToConfigValue.get("hasTargetEntityLink"), false);
    if (!hasTargetEntityLink) {
      return null;
    }
    List<MultiKey> result = new ArrayList<MultiKey>();
  
    String errorMessage = GrouperTextContainer.textOrNull("provisioning.configuration.validation.targetEntityLinkMultipleToSameBucket");
    
    for (String bucket : new String[] {"memberFromId2", "memberFromId3", "memberToId2", "memberToId3"}) {
      
      List<MultiKey> currentErrors = new ArrayList<MultiKey>();
  
      // check attributes
      for (int i=0; i< 20; i++) {
  
        Boolean isField = GrouperUtil.booleanObjectValue(suffixToConfigValue.get("targetEntityAttribute."+i+".isFieldElseAttribute"));
        if (isField == null) {
          break;
        }
        
        // there is an attribute here
        String suffix = "targetEntityAttribute."+i+".translateToMemberSyncField";
        String translateToGroupSyncField = suffixToConfigValue.get(suffix);
        
        if (StringUtils.equals(bucket, translateToGroupSyncField)) {
          currentErrors.add(new MultiKey(errorMessage, suffix));
        }
        
      }
  
      String suffix = "common.entityLink." + bucket;
      if (!StringUtils.isBlank(suffixToConfigValue.get(suffix))) {
        currentErrors.add(new MultiKey(errorMessage, suffix));
      }
      
      if (currentErrors.size() > 1) {
        result.addAll(currentErrors);
      }
    }
    return result;
    
  }

  /**
   * make sure things are the right type and required check, list of values, etc
   * @param suffixToConfigValue
   * @return error messages
   */
  public List<MultiKey> validateConfigBasics(Map<String, String> suffixToConfigValue) {
  
    List<MultiKey> result = new ArrayList<MultiKey>();
  
    ProvisionerConfiguration provisionerConfiguration = this.retrieveProvisionerConfiguration(suffixToConfigValue);
    
    if (provisionerConfiguration == null) {
      String errorMessage = GrouperTextContainer.textOrNull("provisioning.configuration.validation.invalidClass");
      result.add(new MultiKey(errorMessage, "class"));
      return result;
    }
    
    List<String> errors = new ArrayList<String>();
    Map<String, String> formElementErrors = new LinkedHashMap<String, String>();
    
    provisionerConfiguration.validatePreSave(false, errors, formElementErrors);
    
    for (String error : errors) { 
      result.add(new MultiKey(new Object[] {error}));
    }
    for (String formElementErrorKey : formElementErrors.keySet()) {
      result.add(new MultiKey(formElementErrors.get(formElementErrorKey), formElementErrorKey));
    }
    
    return result;
    
  }

  // hasTargetGroupLink
  // if target group link then there should be a copy to sync field or a translation
  
  /**
   * make sure attribute names arent re-used
   * @param suffixToConfigValue
   * @return 
   */
  public List<MultiKey> validateAttributeNamesNotReused(Map<String, String> suffixToConfigValue) {
    
    Set<MultiKey> objectTypeAttributeTypeNames = new HashSet<MultiKey>();
    List<MultiKey> result = new ArrayList<MultiKey>();

    String fieldLabel = GrouperTextContainer.textOrNull("config.GenericConfiguration.attribute.option.targetGroupAttribute.i.isFieldElseAttribute.trueLabel").toLowerCase();
    String attributeLabel = GrouperTextContainer.textOrNull("config.GenericConfiguration.attribute.option.targetGroupAttribute.i.isFieldElseAttribute.falseLabel").toLowerCase();
        
    OBJECT_TYPE: for (String objectType: new String[] {"targetGroupAttribute", "targetEntityAttribute", "targetMembershipAttribute"}) {
      
      String objectTypeLabel = null;
      int numberOfAttributes = -1;
      if (StringUtils.equals("targetGroupAttribute", objectType)) {
        objectTypeLabel = GrouperTextContainer.textOrNull("auditsGroup");
        numberOfAttributes = GrouperUtil.intValue(suffixToConfigValue.get("numberOfGroupAttributes"), 0);
      } else if (StringUtils.equals("targetEntityAttribute", objectType)) {
        objectTypeLabel = GrouperTextContainer.textOrNull("auditsEntity");
        numberOfAttributes = GrouperUtil.intValue(suffixToConfigValue.get("numberOfEntityAttributes"), 0);
      } else if (StringUtils.equals("targetMembershipAttribute", objectType)) {
        objectTypeLabel = GrouperTextContainer.textOrNull("auditsMembership");
        numberOfAttributes = GrouperUtil.intValue(suffixToConfigValue.get("numberOfMembershipAttributes"), 0);
      } else {
        throw new RuntimeException("Cant find object type: " + objectType);
      }
      GrouperTextContainer.assignThreadLocalVariable("type", objectTypeLabel);

      
      for (int i=0; i<numberOfAttributes ; i++) {

        Boolean isField = GrouperUtil.booleanObjectValue(suffixToConfigValue.get(objectType + "."+i+".isFieldElseAttribute"));
        if (isField == null) {
          continue OBJECT_TYPE;
        }
        GrouperTextContainer.assignThreadLocalVariable("fieldType", isField ? fieldLabel : attributeLabel);
        String nameConfigKey = objectType + "."+i+(isField ? ".fieldName" : ".name");
        String name = suffixToConfigValue.get(nameConfigKey);
        
        if (StringUtils.isBlank(name)) {
          
          // provisioning.configuration.validation.attributeNameRequired = Error: ${fieldType} name is required
          result.add(new MultiKey(GrouperTextContainer.textOrNull("provisioning.configuration.validation.attributeNameRequired"), objectType + "."+i+".isFieldElseAttribute"));
          
          continue;
        }
        GrouperTextContainer.assignThreadLocalVariable("attributeName", name);
        
        MultiKey objectTypeAttributeTypeName = new MultiKey(objectType, isField, name);
        if (objectTypeAttributeTypeNames.contains(objectTypeAttributeTypeName)) {
          
          // provisioning.configuration.validation.multipleAttributesSameName = Error: two ${type} ${fieldType}s have the same name '${attributeName}'
          result.add(new MultiKey(GrouperTextContainer.textOrNull("provisioning.configuration.validation.multipleAttributesSameName"), nameConfigKey));
        }
        objectTypeAttributeTypeNames.add(objectTypeAttributeTypeName);
      }      
    }
    GrouperTextContainer.resetThreadLocalVariableMap();
    return result;
    
  }

  // hasTargetGroupLink
  // if target group link then there should be a copy to sync field or a translation
  
  /**
   * make sure attribute names arent re-used
   * @param suffixToConfigValue
   * @return 
   */
  public List<MultiKey> validateAttributeCount(Map<String, String> suffixToConfigValue) {
    
    Set<MultiKey> objectTypeAttributeTypeNames = new HashSet<MultiKey>();
    List<MultiKey> result = new ArrayList<MultiKey>();
  
    String fieldLabel = GrouperTextContainer.textOrNull("config.GenericConfiguration.attribute.option.targetGroupAttribute.i.isFieldElseAttribute.trueLabel").toLowerCase();
    String attributeLabel = GrouperTextContainer.textOrNull("config.GenericConfiguration.attribute.option.targetGroupAttribute.i.isFieldElseAttribute.falseLabel").toLowerCase();
        
    OBJECT_TYPE: for (String objectType: new String[] {"targetGroupAttribute", "targetEntityAttribute", "targetMembershipAttribute"}) {
      
      String objectTypeLabel = null;
      
      String numberOfAttributesKey = null;
      if (StringUtils.equals("targetGroupAttribute", objectType)) {
        objectTypeLabel = GrouperTextContainer.textOrNull("auditsGroup");
        numberOfAttributesKey = "numberOfGroupAttributes";
      } else if (StringUtils.equals("targetEntityAttribute", objectType)) {
        objectTypeLabel = GrouperTextContainer.textOrNull("auditsEntity");
        numberOfAttributesKey = "numberOfEntityAttributes";
      } else if (StringUtils.equals("targetMembershipAttribute", objectType)) {
        objectTypeLabel = GrouperTextContainer.textOrNull("auditsMembership");
        numberOfAttributesKey = "numberOfMembershipAttributes";
      } else {
        throw new RuntimeException("Cant find object type: " + objectType);
      }

      int numberOfExpectedAttributes = GrouperUtil.intValue(suffixToConfigValue.get(numberOfAttributesKey), 0);

      GrouperTextContainer.assignThreadLocalVariable("type", objectTypeLabel);
  
      for (int i=0; i< numberOfExpectedAttributes; i++) {
  
        Boolean isField = GrouperUtil.booleanObjectValue(suffixToConfigValue.get(objectType + "."+i+".isFieldElseAttribute"));
        if (isField == null) {
          continue OBJECT_TYPE;
        }
        GrouperTextContainer.assignThreadLocalVariable("fieldType", isField ? fieldLabel : attributeLabel);
        String nameConfigKey = objectType + "."+i+(isField ? ".fieldName" : ".name");
        String name = suffixToConfigValue.get(nameConfigKey);
        
        if (StringUtils.isBlank(name)) {
          
          // provisioning.configuration.validation.attributeNameRequired = Error: ${fieldType} name is required
          result.add(new MultiKey(GrouperTextContainer.textOrNull("provisioning.configuration.validation.attributeNameRequired"), objectType + "."+i+".isFieldElseAttribute"));
          
          continue;
        }
        GrouperTextContainer.assignThreadLocalVariable("attributeName", name);
        
        MultiKey objectTypeAttributeTypeName = new MultiKey(objectType, isField, name);
        if (objectTypeAttributeTypeNames.contains(objectTypeAttributeTypeName)) {
          
          // provisioning.configuration.validation.multipleAttributesSameName = Error: two ${type} ${fieldType}s have the same name '${attributeName}'
          result.add(new MultiKey(GrouperTextContainer.textOrNull("provisioning.configuration.validation.multipleAttributesSameName"), nameConfigKey));
        }
        objectTypeAttributeTypeNames.add(objectTypeAttributeTypeName);
      }      
    }
    GrouperTextContainer.resetThreadLocalVariableMap();
    return result;
    
  }
  
  
}
 