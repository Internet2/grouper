package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.Collection;
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
   * config suffix to string value
   */
  private Map<String, String> suffixToConfigValue = new HashMap<String, String>();
  
  /**
   * @return the map
   */
  public Map<String, String> getSuffixToConfigValue() {
    return suffixToConfigValue;
  }

  /**
   * error messages and config jquery handle
   */
  private List<MultiKey> errorMessagesAndJqueryHandles = new ArrayList<MultiKey>();

  /**
   * get the error messages and jqeury handles
   * @return the error messages
   */
  public List<MultiKey> getErrorMessagesAndJqueryHandles() {
    return errorMessagesAndJqueryHandles;
  }

  /**
   * add an error message and optionally a jquery handle or config suffix
   * @param errorMessage
   * @param jqueryHandleOrConfigSuffix
   */
  public void addErrorMessage(String errorMessage) {
    addErrorMessageAndJqueryHandle(errorMessage, null);
  }

  /**
   * add an error message and optionally a jquery handle or config suffix
   * @param errorMessage
   * @param jqueryHandleOrConfigSuffix
   */
  public void addErrorMessageAndJqueryHandle(String errorMessage, String jqueryHandleOrConfigSuffix) {
    jqueryHandleOrConfigSuffix = htmlJqueryHandle(jqueryHandleOrConfigSuffix);
    this.errorMessagesAndJqueryHandles.add(new MultiKey(errorMessage, jqueryHandleOrConfigSuffix));
  }
  
  /**
   * add error messages
   * @param multiKeys
   */
  public void addAllErrorMessageAndJqueryHandle(Collection<MultiKey> multiKeys) {
    
    for (MultiKey multiKey : GrouperUtil.nonNull(multiKeys)) {
      if (multiKey.size() > 1) {
        String jqueryHandle = (String)multiKey.getKey(1);
        String newJqueryHandle = htmlJqueryHandle(jqueryHandle);
        if (!StringUtils.equals(jqueryHandle, newJqueryHandle)) {
          multiKey = new MultiKey(multiKey.getKey(0), newJqueryHandle);
        }
      }
      this.errorMessagesAndJqueryHandles.add(multiKey);
    }
  }
  
  /**
   * take the config for this provisioner and validate
   * @return error message, and optionally a jquery handle suffix that has the problem
   */
  public List<MultiKey> validate() {
    
    this.suffixToConfigValue.clear();
    this.errorMessagesAndJqueryHandles.clear();
    
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
    validateFromSuffixValueMap();
    
    // if there are problems with the basics, then other things could throw exceptions
    if (this.errorMessagesAndJqueryHandles.size() > 0) {
      return errorMessagesAndJqueryHandles;
    }

    // we need a type and configure
    GrouperProvisioningType originalType = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningType();
    try {

      if (originalType == null) {
        grouperProvisioner.retrieveGrouperProvisioningBehavior().setGrouperProvisioningType(GrouperProvisioningType.fullProvisionFull);
      }
      
      grouperProvisioner.retrieveGrouperProvisioningConfiguration().configureProvisioner();


      validateFromObjectModel();

    } finally {
      if (originalType == null) {
        grouperProvisioner.retrieveGrouperProvisioningBehavior().setGrouperProvisioningType(null);
      }
    }
    return errorMessagesAndJqueryHandles;
  }

  /**
   * validate from the grouper provisioner
   * @return the 
   */
  public void validateFromObjectModel() {
    validateOperateImpliesSelectOrInsert();
    validateMatchingAttributes();
  }

  
  public void validateMetadata() {
    int numberOfMetadatas = GrouperUtil.intValue(suffixToConfigValue.get("numberOfMetadata"), 0);
    
    for (int i=0; i<numberOfMetadatas; i++) {
      String nameAttributeValue = suffixToConfigValue.get("metadata."+i+".name");
      if (!nameAttributeValue.startsWith("md_") || !nameAttributeValue.matches("^[a-zA-Z0-9_]+$")) {
        this.addErrorMessageAndJqueryHandle(GrouperTextContainer.textOrNull("provisionerConfigurationSaveErrorMetadataNotValidFormat"), "metadata."+i+".name");
      }
      
      String defaultValue = suffixToConfigValue.get("metadata."+i+".defaultValue");
      if (StringUtils.isNotBlank(defaultValue)) {
        String theType = suffixToConfigValue.get("metadata."+i+".valueType");
        GrouperProvisioningObjectMetadataItemValueType valueType = null;
        if (StringUtils.isBlank(theType)) {
          valueType = GrouperProvisioningObjectMetadataItemValueType.STRING;
        } else {
          valueType = GrouperProvisioningObjectMetadataItemValueType.valueOfIgnoreCase(theType, true);
        }
        
        if (!valueType.canConvertToCorrectType(defaultValue)) { 
          String error = GrouperTextContainer.textOrNull("provisionerConfigurationSaveErrorMetadataDefaultValueNotCorrectType");
          error = GrouperUtil.replace(error, "$$defaultValue$$", GrouperUtil.xmlEscape(defaultValue));
          error = GrouperUtil.replace(error, "$$selectedType$$", valueType.name().toLowerCase());
          this.addErrorMessageAndJqueryHandle(error, "metadata."+i+".defaultValue");
          return;
        }
        
      }
    }        
  }
  
  /**
   * if operating on group/entity/membership
   * @param suffixToConfigValue
   * @return the errors
   */
  public void validateOperateImpliesSelectOrInsert() {
    
    GrouperProvisioner grouperProvisioner = this.getGrouperProvisioner();
    GrouperProvisioningConfigurationBase grouperProvisioningConfiguration = grouperProvisioner.retrieveGrouperProvisioningConfiguration();
    
    if (grouperProvisioningConfiguration.isOperateOnGrouperGroups()) {
      if (!grouperProvisioningConfiguration.isSelectGroups() && !grouperProvisioningConfiguration.isInsertGroups()) {
        this.addErrorMessageAndJqueryHandle(GrouperTextContainer.textOrNull("provisioning.configuration.validation.mustSelectOrInsertGroups"), "operateOnGrouperGroups");
      }
    }
    if (grouperProvisioningConfiguration.isOperateOnGrouperEntities()) {
      if (!grouperProvisioningConfiguration.isSelectEntities() && !grouperProvisioningConfiguration.isInsertEntities()) {
        this.addErrorMessageAndJqueryHandle(GrouperTextContainer.textOrNull("provisioning.configuration.validation.mustSelectOrInsertEntities"), "operateOnGrouperEntities");
      }
    }
    if (grouperProvisioningConfiguration.isOperateOnGrouperMemberships()) {
      if (!grouperProvisioningConfiguration.isSelectMemberships() && !grouperProvisioningConfiguration.isInsertMemberships()) {
        this.addErrorMessageAndJqueryHandle(GrouperTextContainer.textOrNull("provisioning.configuration.validation.mustSelectOrInsertMemberships"),"operateOnGrouperMemberships");
      }
    }
    
  }

  /**
   * 
   * @param suffix
   * @return html jquery handle
   */
  public String htmlJqueryHandle(String suffix) {
    if (suffix != null && !suffix.startsWith("#") && !StringUtils.equals("class", suffix)) {
      suffix = "#config_" + suffix + "_spanid";
    }
    return suffix;
  }

  /**
   * 
   * @return error message, and optionally a config suffix that has the problem
   */
  public void validateFromSuffixValueMap() {
    
    validateConfigBasics();
    
    // if there are problems with the basics, then other things could throw exceptions
    if (this.errorMessagesAndJqueryHandles.size() > 0) {
      return;
    }

    validateDoingSomething();
    validateGroupDeleteHasDeleteType();
    validateMembershipDeleteHasDeleteType();
    validateEntityDeleteHasDeleteType();
    validateGroupLinkHasConfiguration();
    validateEntityLinkHasConfiguration();
    validateGroupLinkOnePerBucket();
    validateEntityLinkOnePerBucket();
    validateNoUnsedConfigs();
    validateAttributeNamesNotReused();
    validateAttributeCount();
    validateGroupIdToProvisionExists();
    validateMetadata();


    // if there are problems with the basics, then other things could throw exceptions
    if (this.errorMessagesAndJqueryHandles.size() > 0) {
      return;
    }

    validateProvisionerConfig();
  }

  public void validateMatchingAttributes() {
    
    GrouperProvisioner grouperProvisioner = this.getGrouperProvisioner();
    GrouperProvisioningConfigurationBase grouperProvisioningConfiguration = grouperProvisioner.retrieveGrouperProvisioningConfiguration();

    Set<GrouperProvisioningConfigurationAttribute> grouperProvisioningConfigurationAttributes = new HashSet<GrouperProvisioningConfigurationAttribute>();
    grouperProvisioningConfigurationAttributes.addAll(GrouperUtil.nonNull(grouperProvisioningConfiguration.getTargetGroupFieldNameToConfig()).values());
    grouperProvisioningConfigurationAttributes.addAll(GrouperUtil.nonNull(grouperProvisioningConfiguration.getTargetGroupAttributeNameToConfig()).values());
    grouperProvisioningConfigurationAttributes.addAll(GrouperUtil.nonNull(grouperProvisioningConfiguration.getTargetEntityFieldNameToConfig()).values());
    grouperProvisioningConfigurationAttributes.addAll(GrouperUtil.nonNull(grouperProvisioningConfiguration.getTargetEntityAttributeNameToConfig()).values());
    grouperProvisioningConfigurationAttributes.addAll(GrouperUtil.nonNull(grouperProvisioningConfiguration.getTargetMembershipFieldNameToConfig()).values());
    grouperProvisioningConfigurationAttributes.addAll(GrouperUtil.nonNull(grouperProvisioningConfiguration.getTargetMembershipAttributeNameToConfig()).values());

    boolean hasGroupMatchingId = false;
    boolean hasMemberMatchingId = false;
    boolean hasMembershipMatchingId = false;

    boolean hasGroupMembershipId = false;
    boolean hasMemberMembershipId = false;

    for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : grouperProvisioningConfigurationAttributes) {
      //validate the matching attributes come from gcSync objects
      
      if (grouperProvisioningConfigurationAttribute.isMatchingId()) {

        if (grouperProvisioningConfigurationAttribute.getGrouperProvisioningConfigurationAttributeType() == GrouperProvisioningConfigurationAttributeType.entity) {
          hasMemberMatchingId = true;
        }
        if (grouperProvisioningConfigurationAttribute.getGrouperProvisioningConfigurationAttributeType() == GrouperProvisioningConfigurationAttributeType.group) {
          hasGroupMatchingId = true;
        }
        if (grouperProvisioningConfigurationAttribute.getGrouperProvisioningConfigurationAttributeType() == GrouperProvisioningConfigurationAttributeType.membership) {
          hasMembershipMatchingId = true;
        }
        
      }
      
      
      if (grouperProvisioningConfigurationAttribute.isMembershipAttribute()) {
        
        if (StringUtils.isBlank(grouperProvisioningConfigurationAttribute.getTranslateFromGroupSyncField())
            && StringUtils.isBlank(grouperProvisioningConfigurationAttribute.getTranslateFromMemberSyncField())) {

//          throw new RuntimeException(grouperProvisioningConfigurationAttribute.getGrouperProvisioningConfigurationAttributeType()
//              + " " + (grouperProvisioningConfigurationAttribute.isAttribute() ? "attribute" : "field") + " '" + grouperProvisioningConfigurationAttribute.getName() 
//              + "' is a membership attribute but does not have a translation from a sync field.  It must have a translation from a sync field! " + grouperProvisioningConfigurationAttribute);
        }

        if (grouperProvisioningConfigurationAttribute.getGrouperProvisioningConfigurationAttributeType() == GrouperProvisioningConfigurationAttributeType.entity) {
          hasMemberMembershipId = true;
        }
        if (grouperProvisioningConfigurationAttribute.getGrouperProvisioningConfigurationAttributeType() == GrouperProvisioningConfigurationAttributeType.group) {
          hasGroupMembershipId = true;
        }
        
      }
      
    }

    if (grouperProvisioningConfiguration.getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes) {
      if (!hasGroupMatchingId) {
        this.addErrorMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.mustHaveGroupMatchingId"));
      }
      if (!hasGroupMembershipId) {
        this.addErrorMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.mustHaveGroupMembershipAttribute"));
      }
    } else if (grouperProvisioningConfiguration.getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes) {
      if (!hasMemberMatchingId) {
        this.addErrorMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.mustHaveEntityMatchingId"));
      }
      if (!hasMemberMembershipId) {
        this.addErrorMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.mustHaveEntityMembershipAttribute"));
      }
    }
    
  }


  public void validateDoingSomething() {
    
    boolean operateOnGrouperEntities = GrouperUtil.booleanValue(suffixToConfigValue.get("operateOnGrouperEntities"), false);
    boolean operateOnGrouperGroups = GrouperUtil.booleanValue(suffixToConfigValue.get("operateOnGrouperGroups"), false);
    boolean operateOnGrouperMemberships = GrouperUtil.booleanValue(suffixToConfigValue.get("operateOnGrouperMemberships"), false);
    
    if (!operateOnGrouperEntities && !operateOnGrouperGroups && !operateOnGrouperMemberships) {
      this.addErrorMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.doSomething"));
    }
  }

  /**
   * 
   * @param suffixToConfigValue
   * @return
   */
  public void validateGroupIdToProvisionExists() {

    String groupIdOfUsersToProvision = suffixToConfigValue.get("groupIdOfUsersToProvision");
    
    if (!StringUtils.isBlank(groupIdOfUsersToProvision)) {
       if (null == GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), groupIdOfUsersToProvision, false)) {
         
         this.addErrorMessageAndJqueryHandle(GrouperTextContainer.textOrNull("provisioning.configuration.validation.groupIdOfUsersToProvisionNotExist"), htmlJqueryHandle("groupIdOfUsersToProvision"));
       }
    
    }
  }

  /**
   * if there is a group delete, then there must be one delete type
   * @param suffixToConfigValue
   * @return 
   */
  public void validateGroupDeleteHasDeleteType() {
    
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
        this.addErrorMessageAndJqueryHandle(GrouperTextContainer.textOrNull("provisioning.configuration.validation.oneGroupDeleteType"), htmlJqueryHandle("deleteGroups"));
      }
    }
  }
  
  /**
   * if there is a group delete, then there must be one delete type
   * @param suffixToConfigValue
   * @return 
   */
  public void validateMembershipDeleteHasDeleteType() {
    
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
        this.addErrorMessageAndJqueryHandle(GrouperTextContainer.textOrNull("provisioning.configuration.validation.oneMembershipDeleteType"), htmlJqueryHandle("deleteMemberships"));
      }
    }
    
  }
  
  /**
   * if there is a group delete, then there must be one delete type
   * @param suffixToConfigValue
   * @return 
   */
  public void validateEntityDeleteHasDeleteType() {
    
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
        this.addErrorMessageAndJqueryHandle(GrouperTextContainer.textOrNull("provisioning.configuration.validation.oneEntityDeleteType"), htmlJqueryHandle("deleteEntities"));
      }
    }
    
  }

  // hasTargetGroupLink
  // if target group link then there should be a copy to sync field or a translation
  
  /**
   * if there is a group delete, then there must be one delete type
   * @param suffixToConfigValue
   * @return 
   */
  public void validateGroupLinkHasConfiguration() {
    
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
          return;
        }
        
      }
      
      // check scripts
      if (!StringUtils.isBlank(suffixToConfigValue.get("common.groupLink.groupFromId2"))) {
        return;
      }
      
      if (!StringUtils.isBlank(suffixToConfigValue.get("common.groupLink.groupFromId3"))) {
        return;
      }

      if (!StringUtils.isBlank(suffixToConfigValue.get("common.groupLink.groupToId2"))) {
        return;
      }

      if (!StringUtils.isBlank(suffixToConfigValue.get("common.groupLink.groupToId3"))) {
        return;
      }
      
      this.addErrorMessageAndJqueryHandle(GrouperTextContainer.textOrNull("provisioning.configuration.validation.targetGroupLinkNeedsConfig"), htmlJqueryHandle("hasTargetGroupLink"));
    }
    
  }

  // hasTargetGroupLink
  // if target group link then there should be a copy to sync field or a translation
  
  /**
   * if there is a entity delete, then there must be one delete type
   * @param suffixToConfigValue
   * @return 
   */
  public void validateEntityLinkHasConfiguration() {
    
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
          return;
        }
        
      }
      
      // check scripts
      if (!StringUtils.isBlank(suffixToConfigValue.get("common.entityLink.memberFromId2"))) {
        return;
      }
      
      if (!StringUtils.isBlank(suffixToConfigValue.get("common.entityLink.memberFromId3"))) {
        return;
      }
  
      if (!StringUtils.isBlank(suffixToConfigValue.get("common.entityLink.memberToId2"))) {
        return;
      }
  
      if (!StringUtils.isBlank(suffixToConfigValue.get("common.entityLink.mmeberToId3"))) {
        return;
      }
      
      this.addErrorMessageAndJqueryHandle(GrouperTextContainer.textOrNull("provisioning.configuration.validation.targetEntityLinkNeedsConfig"), htmlJqueryHandle("hasTargetEntityLink"));
    }
    return;
    
  }

  /**
   * if there is a group link, then make sure multiple things arent going to the same bucket
   * @param suffixToConfigValue
   * @return error messages
   */
  public void validateGroupLinkOnePerBucket() {

    boolean hasTargetGroupLink = GrouperUtil.booleanValue(suffixToConfigValue.get("hasTargetGroupLink"), false);
    if (!hasTargetGroupLink) {
      return;
    }

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
        this.addAllErrorMessageAndJqueryHandle(currentErrors);
      }
    }
    
  }

  /**
   * 
   * @return
   */
  public ProvisionerConfiguration retrieveProvisionerConfiguration() {
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
  public void validateNoUnsedConfigs() {

    // remove from this map the valid keys, and the invalid ones will remain
    Set<String> keysUsed = new HashSet<String>(suffixToConfigValue.keySet());
    
    // dont use the real config since it adds non-example configs
    String configSuffixThatIdentifiesThisProvisioner = suffixToConfigValue.get("class");

    ProvisionerConfiguration theProvisionerConfiguration = StringUtils.isBlank(configSuffixThatIdentifiesThisProvisioner) ? null 
        : ProvisionerConfiguration.retrieveConfigurationByConfigSuffix(configSuffixThatIdentifiesThisProvisioner);
    if (theProvisionerConfiguration != null) {
      String configId = "someConfigIdThatWontConflict";
      
      theProvisionerConfiguration.setConfigId(configId);

    }

    
    if (theProvisionerConfiguration == null) {
      this.addErrorMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.invalidClass"));
      return;
    }
    keysUsed.removeAll(theProvisionerConfiguration.retrieveAttributes().keySet());

    for (String keyNotUsed : keysUsed) {
      
      // provisioning.configuration.validation.extraneousConfigs = Error: there is an extraneous config for this provisioner: ${extraneousConfig}
      GrouperTextContainer.assignThreadLocalVariable("extraneousConfig", keyNotUsed);
      String errorMessage = GrouperTextContainer.textOrNull("provisioning.configuration.validation.extraneousConfigs");
      this.addErrorMessage(errorMessage);
      GrouperTextContainer.resetThreadLocalVariableMap();
    }
    
  }
  
  /**
   * if there is an entity link, then make sure multiple things arent going to the same bucket
   * @param suffixToConfigValue
   * @return error messages
   */
  public void validateEntityLinkOnePerBucket() {
  
    boolean hasTargetEntityLink = GrouperUtil.booleanValue(suffixToConfigValue.get("hasTargetEntityLink"), false);
    if (!hasTargetEntityLink) {
      return;
    }
  
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
        this.addAllErrorMessageAndJqueryHandle(currentErrors);
      }
    }
    
  }

  /**
   * make sure things are the right type and required check, list of values, etc
   * @param suffixToConfigValue
   * @return error messages
   */
  public void validateConfigBasics() {
  
    this.retrieveProvisionerConfiguration();
    if (provisionerConfiguration == null) {
      this.addErrorMessageAndJqueryHandle(GrouperTextContainer.textOrNull("provisioning.configuration.validation.invalidClass"), "class");
      return;
    }
    
    List<String> errors = new ArrayList<String>();
    Map<String, String> formElementErrors = new LinkedHashMap<String, String>();
    
    provisionerConfiguration.validatePreSaveNonProvisionerSpecific(false, errors, formElementErrors);
    
    for (String error : errors) { 
      this.addErrorMessage(error);
    }
    for (String formElementErrorKey : formElementErrors.keySet()) {
      this.addErrorMessageAndJqueryHandle(formElementErrors.get(formElementErrorKey), formElementErrorKey);
    }
    
  }

  /**
   * provisioner config
   * @param suffixToConfigValue
   * @return error messages
   */
  public void validateProvisionerConfig() {
  
    this.retrieveProvisionerConfiguration();
    if (provisionerConfiguration == null) {
      this.addErrorMessageAndJqueryHandle(GrouperTextContainer.textOrNull("provisioning.configuration.validation.invalidClass"), "class");
      return;
    }
    
    List<String> errors = new ArrayList<String>();
    Map<String, String> formElementErrors = new LinkedHashMap<String, String>();
    
    provisionerConfiguration.validatePreSaveNonProvisionerSpecific(false, errors, formElementErrors);
    
    for (String error : errors) { 
      this.addErrorMessage(error);
    }
    for (String formElementErrorKey : formElementErrors.keySet()) {
      this.addErrorMessageAndJqueryHandle(formElementErrors.get(formElementErrorKey), formElementErrorKey);
    }
    
  }

  // hasTargetGroupLink
  // if target group link then there should be a copy to sync field or a translation
  
  /**
   * make sure attribute names arent re-used
   * @param suffixToConfigValue
   * @return 
   */
  public void validateAttributeNamesNotReused() {
    
    Set<MultiKey> objectTypeAttributeTypeNames = new HashSet<MultiKey>();

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
          this.addErrorMessageAndJqueryHandle(GrouperTextContainer.textOrNull("provisioning.configuration.validation.attributeNameRequired"), objectType + "."+i+".isFieldElseAttribute");
          
          continue;
        }
        GrouperTextContainer.assignThreadLocalVariable("attributeName", name);
        
        MultiKey objectTypeAttributeTypeName = new MultiKey(objectType, isField, name);
        if (objectTypeAttributeTypeNames.contains(objectTypeAttributeTypeName)) {
          
          // provisioning.configuration.validation.multipleAttributesSameName = Error: two ${type} ${fieldType}s have the same name '${attributeName}'
          this.addErrorMessageAndJqueryHandle(GrouperTextContainer.textOrNull("provisioning.configuration.validation.multipleAttributesSameName"), nameConfigKey);
        }
        objectTypeAttributeTypeNames.add(objectTypeAttributeTypeName);
      }      
    }
    GrouperTextContainer.resetThreadLocalVariableMap();
    
  }

  // hasTargetGroupLink
  // if target group link then there should be a copy to sync field or a translation
  
  /**
   * make sure attribute names arent re-used
   * @param suffixToConfigValue
   * @return 
   */
  public void validateAttributeCount() {
    
    Set<MultiKey> objectTypeAttributeTypeNames = new HashSet<MultiKey>();
  
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
          this.addErrorMessageAndJqueryHandle(GrouperTextContainer.textOrNull("provisioning.configuration.validation.attributeNameRequired"), objectType + "."+i+".isFieldElseAttribute");
          
          continue;
        }
        GrouperTextContainer.assignThreadLocalVariable("attributeName", name);
        
        MultiKey objectTypeAttributeTypeName = new MultiKey(objectType, isField, name);
        if (objectTypeAttributeTypeNames.contains(objectTypeAttributeTypeName)) {
          
          // provisioning.configuration.validation.multipleAttributesSameName = Error: two ${type} ${fieldType}s have the same name '${attributeName}'
          this.addErrorMessageAndJqueryHandle(GrouperTextContainer.textOrNull("provisioning.configuration.validation.multipleAttributesSameName"), nameConfigKey);
        }
        objectTypeAttributeTypeNames.add(objectTypeAttributeTypeName);
      }      
    }
    GrouperTextContainer.resetThreadLocalVariableMap();
    
  }
  
  
}
 