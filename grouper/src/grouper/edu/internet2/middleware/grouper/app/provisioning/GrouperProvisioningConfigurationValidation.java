package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.upgradeTasks.UpgradeTasks;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;

/**
 * this could be a deep validate though
 * @author mchyzer
 */
public class GrouperProvisioningConfigurationValidation {

  private ProvisioningConfiguration provisionerConfiguration = null;
  
  private GrouperProvisioner grouperProvisioner = null;
  
  public GrouperProvisioner getGrouperProvisioner() {
    return grouperProvisioner;
  }
  
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner) {
    this.grouperProvisioner = grouperProvisioner;
  }

  /**
   * override this method to assign which attributes are allowed for groups
   * @return null to not check or a collection to check group attribute names
   */
  public Collection<String> validateGroupAttributeNamesAllowed() {
    return null;
  }
  
  /**
   * override this method to assign which attributes are required for groups
   * @return null to not check or a collection to check group attribute names
   */
  public Collection<String> validateGroupAttributeNamesRequired() {
    return null;
  }
  
  /**
   * override this method return true to require group attributes to be strings
   * @return true
   */
  public boolean validateGroupAttributesRequireString() {
    return false;
  }
  
  /**
   * validate various generic aspects of group attributes
   */
  public void validateGroupAttributes() {
      
    Collection<String> groupAttributeNamesAllowed = validateGroupAttributeNamesAllowed();
    Collection<String> groupAttributeNamesRequired = validateGroupAttributeNamesRequired();
    if (groupAttributeNamesRequired != null) {
      groupAttributeNamesRequired = new HashSet<String>(groupAttributeNamesRequired);
      
    }    
    if (groupAttributeNamesAllowed != null) {
      groupAttributeNamesAllowed = new HashSet<String>(groupAttributeNamesAllowed);
      
      if (groupAttributeNamesRequired != null) {
        groupAttributeNamesAllowed.addAll(groupAttributeNamesRequired);
      }
      
    }    
 
    for (int i=0; i< 20; i++) {

      String nameConfigKey = "targetGroupAttribute."+i+".name"; 
      String name = this.getSuffixToConfigValue().get(nameConfigKey);
      String type = this.getSuffixToConfigValue().get("targetGroupAttribute."+i+".valueType");
      
      if (StringUtils.isBlank(name)) {
        break;
      }

      if (groupAttributeNamesAllowed != null && !groupAttributeNamesAllowed.contains(name)) {

        String errorMessage = GrouperTextContainer.textOrNull("provisioning.configuration.validation.incorrectGroupAttributeConfigured");
        errorMessage = StringUtils.replace(errorMessage, "$$attributeName$$", name);
        this.addErrorMessage(new ProvisioningValidationIssue().assignMessage(errorMessage).assignJqueryHandle(nameConfigKey));

      }

      if (validateGroupAttributesRequireString() && StringUtils.isNotBlank(type) && !StringUtils.equalsIgnoreCase(type, "string")) {

        String errorMessage = GrouperTextContainer.textOrNull("provisioning.configuration.validation.groupAttributeString");
        errorMessage = StringUtils.replace(errorMessage, "$$attributeName$$", name);
        this.addErrorMessage(new ProvisioningValidationIssue().assignMessage(errorMessage).assignJqueryHandle(nameConfigKey));
        
      }
      
      if (groupAttributeNamesRequired != null) {
        groupAttributeNamesRequired.remove(name);
      }
    }
    
    if (groupAttributeNamesRequired != null && groupAttributeNamesRequired.size()>0) {
      for (String name : groupAttributeNamesRequired) {
        String errorMessage = GrouperTextContainer.textOrNull("provisioning.configuration.validation.groupAttributeRequired");
        errorMessage = StringUtils.replace(errorMessage, "$$attributeName$$", name);
        this.addErrorMessage(new ProvisioningValidationIssue().assignMessage(errorMessage));
      }
    }
  }
  
  /**
   * override this method to assign which attributes are allowed for entities
   * @return null to not check or a collection to check entity attribute names
   */
  public Collection<String> validateEntityAttributeNamesAllowed() {
    return null;
  }
  
  /**
   * override this method to assign which attributes are required for entities
   * @return null to not check or a collection to check entity attribute names
   */
  public Collection<String> validateEntityAttributeNamesRequired() {
    return null;
  }
  
  /**
   * override this method return true to require entity attributes to be strings
   * @return true
   */
  public boolean validateEntityAttributesRequireString() {
    return false;
  }
  
  /**
   * validate various generic aspects of entity attributes
   */
  public void validateEntityAttributes() {
      
    Collection<String> entityAttributeNamesAllowed = validateEntityAttributeNamesAllowed();
    Collection<String> entityAttributeNamesRequired = validateEntityAttributeNamesRequired();
    if (entityAttributeNamesRequired != null) {
      entityAttributeNamesRequired = new HashSet<String>(entityAttributeNamesRequired);
    }    
 
    if (entityAttributeNamesAllowed != null) {
      entityAttributeNamesAllowed = new HashSet<String>(entityAttributeNamesAllowed);
      
      if (entityAttributeNamesRequired != null) {
        entityAttributeNamesAllowed.addAll(entityAttributeNamesRequired);
      }
      
    }    

    for (int i=0; i< 20; i++) {

      String nameConfigKey = "targetEntityAttribute."+i+".name"; 
      String name = this.getSuffixToConfigValue().get(nameConfigKey);
      String type = this.getSuffixToConfigValue().get("targetEntityAttribute."+i+".valueType");
      
      if (StringUtils.isBlank(name)) {
        break;
      }

      if (entityAttributeNamesAllowed != null && !entityAttributeNamesAllowed.contains(name)) {

        String errorMessage = GrouperTextContainer.textOrNull("provisioning.configuration.validation.incorrectEntityAttributeConfigured");
        errorMessage = StringUtils.replace(errorMessage, "$$attributeName$$", name);
        this.addErrorMessage(new ProvisioningValidationIssue().assignMessage(errorMessage).assignJqueryHandle(nameConfigKey));

      }

      if (validateEntityAttributesRequireString() && StringUtils.isNotBlank(type) && !StringUtils.equalsIgnoreCase(type, "string")) {

        String errorMessage = GrouperTextContainer.textOrNull("provisioning.configuration.validation.entityAttributeString");
        errorMessage = StringUtils.replace(errorMessage, "$$attributeName$$", name);
        this.addErrorMessage(new ProvisioningValidationIssue().assignMessage(errorMessage).assignJqueryHandle(nameConfigKey));
        
      }
      
      if (entityAttributeNamesRequired != null) {
        entityAttributeNamesRequired.remove(name);
      }
    }
    
    if (entityAttributeNamesRequired != null && entityAttributeNamesRequired.size()>0) {
      for (String name : entityAttributeNamesRequired) {
        String errorMessage = GrouperTextContainer.textOrNull("provisioning.configuration.validation.entityAttributeRequired");
        errorMessage = StringUtils.replace(errorMessage, "$$attributeName$$", name);
        this.addErrorMessage(new ProvisioningValidationIssue().assignMessage(errorMessage));
      }
    }
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
   * error messages and config jquery handle and severity
   */
  private List<ProvisioningValidationIssue> provisioningValidationIssues = new ArrayList<ProvisioningValidationIssue>();

  /**
   * get the error messages and jqeury handles and severity
   * @return the error messages
   */
  public List<ProvisioningValidationIssue> getProvisioningValidationIssues() {
    return this.provisioningValidationIssues;
  }

  /**
   * add an error message and optionally a jquery handle or config suffix
   * @param provisioningValidationIssue
   */
  public void addErrorMessage(ProvisioningValidationIssue provisioningValidationIssue) {
    
    this.provisioningValidationIssues.add(provisioningValidationIssue);
  }

  /**
   * add an error message and optionally a jquery handle or config suffix
   * @param provisioningValidationIssue
   */
  public void addErrorMessages(Collection<ProvisioningValidationIssue> provisioningValidationIssue) {
    
    this.provisioningValidationIssues.addAll(GrouperUtil.nonNull(provisioningValidationIssue));
  }

  /**
   * take the config for this provisioner and validate
   * @return error message, and optionally a jquery handle suffix that has the problem
   */
  public List<ProvisioningValidationIssue> validate() {
    
    this.suffixToConfigValue.clear();
    this.provisioningValidationIssues.clear();
    
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
    if (this.provisioningValidationIssues.size() > 0) {
      return this.provisioningValidationIssues;
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
    return this.provisioningValidationIssues;
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
        this.addErrorMessage(new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisionerConfigurationSaveErrorMetadataNotValidFormat")).assignJqueryHandle("metadata."+i+".name"));
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
          this.addErrorMessage(new ProvisioningValidationIssue().assignMessage(error).assignJqueryHandle("metadata."+i+".defaultValue"));
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
    GrouperProvisioningConfiguration grouperProvisioningConfiguration = grouperProvisioner.retrieveGrouperProvisioningConfiguration();

    // you might be using groups or entities as translations only
//    if (grouperProvisioningConfiguration.isOperateOnGrouperGroups()) {
//      if (!grouperProvisioningConfiguration.isSelectGroups() && !grouperProvisioningConfiguration.isInsertGroups()) {
//        this.addErrorMessageAndJqueryHandle(GrouperTextContainer.textOrNull("provisioning.configuration.validation.mustSelectOrInsertGroups"), "operateOnGrouperGroups");
//      }
//    }
//    if (grouperProvisioningConfiguration.isOperateOnGrouperEntities()) {
//      if (!grouperProvisioningConfiguration.isSelectEntities() && !grouperProvisioningConfiguration.isInsertEntities()) {
//        this.addErrorMessageAndJqueryHandle(GrouperTextContainer.textOrNull("provisioning.configuration.validation.mustSelectOrInsertEntities"), "operateOnGrouperEntities");
//      }
//    }
    if (grouperProvisioningConfiguration.isOperateOnGrouperMemberships() && grouperProvisioningConfiguration.isCustomizeMembershipCrud()) {
      if (!grouperProvisioningConfiguration.isSelectMemberships() && !grouperProvisioningConfiguration.isInsertMemberships()) {
        this.addErrorMessage(new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.mustSelectOrInsertMemberships")).assignJqueryHandle("operateOnGrouperMemberships"));
      }
    }
    
  }

  /**
   * 
   * @return error message, and optionally a config suffix that has the problem
   */
  public void validateFromSuffixValueMap() {
    
    validateConfigBasics();
    
    // if there are problems with the basics, then other things could throw exceptions
    if (this.provisioningValidationIssues.size() > 0) {
      return;
    }

    validateEntityAttributes();
    validateGroupAttributes();
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
    validateFailsafes();
    validateOperateOnEntitiesIfSubjectSourcesToProvision();
    validateSelectAllEntities();
    validateNoFields();
    validateEntityResolverRefactorDontUseOldValues();
    validateCustomizeMembershipCrud();
    validateCustomizeGroupCrud();
    validateCustomizeEntityCrud();
    validateMembershipShowValidation();
    validateGroupShowValidation();
    validateEntityShowValidation();
    validateMembershipShowAttributeCrud();
    validateGroupShowAttributeCrud();

    // if there are problems with the basics, then other things could throw exceptions
    if (this.provisioningValidationIssues.size() > 0) {
      return;
    }

    validateProvisionerConfig();
  }

  /**
   * GRP-3911: subject sources to provision should not be in top config section
   */
  public void validateOperateOnEntitiesIfSubjectSourcesToProvision() {
    
    boolean operateOnGrouperEntities = GrouperUtil.booleanValue(this.suffixToConfigValue.get("operateOnGrouperEntities"), false);

    String subjectSourcesToProvision = this.suffixToConfigValue.get("subjectSourcesToProvision");
    
    if (!StringUtils.isBlank(subjectSourcesToProvision) && !operateOnGrouperEntities) {
      this.addErrorMessage(new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("grouperProvisioningSubjectSourcesToProvisionRequiresEntitiesInvalid")));
    }
    
  }
  

  /**
   * 
   */
  public void validateFailsafes() {
    
    GrouperUtil.booleanValue(this.suffixToConfigValue.get("showFailsafe"), false);
    
    {
      String failsafeUse = this.suffixToConfigValue.get("failsafeUse");
      GrouperUtil.booleanValue(failsafeUse, false);
    }
    
    {
      String failsafeSendEmail = this.suffixToConfigValue.get("failsafeSendEmail");
      GrouperUtil.booleanValue(failsafeSendEmail, false);
    }
    
    try {
      GrouperUtil.intObjectValue(this.suffixToConfigValue.get("failsafeMinGroupSize"), true);
    } catch (Exception e) {
      this.addErrorMessage(new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("grouperLoaderMinGroupSizeInvalid")));
    }
    
    try {
      GrouperUtil.intObjectValue(this.suffixToConfigValue.get("failsafeMaxPercentRemove"), true);
    } catch (Exception e) {
      this.addErrorMessage(new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("grouperLoaderMaxGroupPercentRemoveInvalid")));
    }
    
    try {
      GrouperUtil.intObjectValue(this.suffixToConfigValue.get("failsafeMinManagedGroups"), true);
    } catch (Exception e) {
      this.addErrorMessage(new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("grouperLoaderMinManagedGroupsInvalid")));
    }
    
    try {
      GrouperUtil.intObjectValue(this.suffixToConfigValue.get("failsafeMaxOverallPercentGroupsRemove"), true);
    } catch (Exception e) {
      this.addErrorMessage(new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("grouperLoaderMaxOverallPercentGroupsRemoveInvalid")));
    }
    
    try {
      GrouperUtil.intObjectValue(this.suffixToConfigValue.get("failsafeMaxOverallPercentMembershipsRemove"), true);
    } catch (Exception e) {
      this.addErrorMessage(new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("grouperLoaderMaxOverallPercentMembershipsInvalid")));
    }
    
    try {
      GrouperUtil.intObjectValue(this.suffixToConfigValue.get("failsafeMinOverallNumberOfMembers"), true);
    } catch (Exception e) {
      this.addErrorMessage(new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("grouperLoaderMinOverallNumberOfMembersInvalid")));
    }
    
  }

  public void validateMatchingAttributes() {
    
    GrouperProvisioner grouperProvisioner = this.getGrouperProvisioner();
    GrouperProvisioningConfiguration grouperProvisioningConfiguration = grouperProvisioner.retrieveGrouperProvisioningConfiguration();

    Set<GrouperProvisioningConfigurationAttribute> grouperProvisioningConfigurationAttributes = new HashSet<GrouperProvisioningConfigurationAttribute>();
    grouperProvisioningConfigurationAttributes.addAll(GrouperUtil.nonNull(grouperProvisioningConfiguration.getTargetGroupAttributeNameToConfig()).values());
    grouperProvisioningConfigurationAttributes.addAll(GrouperUtil.nonNull(grouperProvisioningConfiguration.getTargetEntityAttributeNameToConfig()).values());
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
        this.addErrorMessage(new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.mustHaveGroupMatchingId")));
      }
      if (!hasGroupMembershipId) {
        this.addErrorMessage(new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.mustHaveGroupMembershipAttribute")));
      }
    } else if (grouperProvisioningConfiguration.getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes) {
      if (!hasMemberMatchingId) {
        this.addErrorMessage(new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.mustHaveEntityMatchingId")));
      }
      if (!hasMemberMembershipId) {
        this.addErrorMessage(new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.mustHaveEntityMembershipAttribute")));
      }
    }
    
  }


  public void validateDoingSomething() {
    
    boolean operateOnGrouperEntities = GrouperUtil.booleanValue(suffixToConfigValue.get("operateOnGrouperEntities"), false);
    boolean operateOnGrouperGroups = GrouperUtil.booleanValue(suffixToConfigValue.get("operateOnGrouperGroups"), false);
    boolean operateOnGrouperMemberships = GrouperUtil.booleanValue(suffixToConfigValue.get("operateOnGrouperMemberships"), false);
    
    if (!operateOnGrouperEntities && !operateOnGrouperGroups && !operateOnGrouperMemberships) {
      this.addErrorMessage(new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.doSomething")));
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
         this.addErrorMessage(new ProvisioningValidationIssue()
             .assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.groupIdOfUsersToProvisionNotExist"))
             .assignJqueryHandle("groupIdOfUsersToProvision"));
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
        this.addErrorMessage(new ProvisioningValidationIssue()
            .assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.oneGroupDeleteType"))
            .assignJqueryHandle("deleteGroups"));
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
        this.addErrorMessage(new ProvisioningValidationIssue()
            .assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.oneMembershipDeleteType"))
            .assignJqueryHandle("deleteMemberships"));
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
        this.addErrorMessage(new ProvisioningValidationIssue()
            .assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.oneEntityDeleteType"))
            .assignJqueryHandle("deleteEntities"));
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
  
        String name = suffixToConfigValue.get("targetGroupAttribute."+i+".name");
        if (StringUtils.isBlank(name)) {
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
      this.addErrorMessage(new ProvisioningValidationIssue()
          .assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.targetGroupLinkNeedsConfig"))
          .assignJqueryHandle("hasTargetGroupLink"));
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
  
        String name = suffixToConfigValue.get("targetEntityAttribute."+i+".name");
        if (StringUtils.isBlank(name)) {
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
      this.addErrorMessage(new ProvisioningValidationIssue()
          .assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.targetEntityLinkNeedsConfig"))
          .assignJqueryHandle("hasTargetEntityLink"));
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
      
      List<ProvisioningValidationIssue> currentErrors = new ArrayList<ProvisioningValidationIssue>();

      // check attributes
      for (int i=0; i< 20; i++) {
  
        String name = suffixToConfigValue.get("targetGroupAttribute."+i+".name");
        if (StringUtils.isBlank(name)) {
          break;
        }
        
        // there is an attribute here
        String suffix = "targetGroupAttribute."+i+".translateToGroupSyncField";
        String translateToGroupSyncField = suffixToConfigValue.get(suffix);
        
        if (StringUtils.equals(bucket, translateToGroupSyncField)) {
          currentErrors.add(new ProvisioningValidationIssue().assignMessage(errorMessage).assignJqueryHandle(suffix));
        }
        
      }

      String suffix = "common.groupLink." + bucket;
      if (!StringUtils.isBlank(suffixToConfigValue.get(suffix))) {
        currentErrors.add(new ProvisioningValidationIssue().assignMessage(errorMessage).assignJqueryHandle(suffix));
      }
      
      if (currentErrors.size() > 1) {
        this.addErrorMessages(currentErrors);
      }
    }
    
  }

  /**
   * 
   * @return
   */
  public ProvisioningConfiguration retrieveProvisionerConfiguration() {
    if (this.provisionerConfiguration == null) {
      String configSuffixThatIdentifiesThisProvisioner = suffixToConfigValue.get("class");

      provisionerConfiguration = StringUtils.isBlank(configSuffixThatIdentifiesThisProvisioner) ? null 
          : ProvisioningConfiguration.retrieveConfigurationByConfigSuffix(configSuffixThatIdentifiesThisProvisioner);
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

    ProvisioningConfiguration theProvisionerConfiguration = StringUtils.isBlank(configSuffixThatIdentifiesThisProvisioner) ? null 
        : ProvisioningConfiguration.retrieveConfigurationByConfigSuffix(configSuffixThatIdentifiesThisProvisioner);
    if (theProvisionerConfiguration != null) {
      String configId = "someConfigIdThatWontConflict";
      
      theProvisionerConfiguration.setConfigId(configId);

    }

    
    if (theProvisionerConfiguration == null) {
      this.addErrorMessage(new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.invalidClass")));
      return;
    }
    keysUsed.removeAll(theProvisionerConfiguration.retrieveAttributes().keySet());
    if (keysUsed.size() > 0) {
      // provisioning.configuration.validation.extraneousConfigs = Error: there is an extraneous config for this provisioner: ${extraneousConfig}
      GrouperTextContainer.assignThreadLocalVariable("extraneousConfig", StringUtils.join(keysUsed, ", "));
      String errorMessage = GrouperTextContainer.textOrNull("provisioning.configuration.validation.extraneousConfigs");
      this.addErrorMessage(new ProvisioningValidationIssue().assignMessage(errorMessage));
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
      
      List<ProvisioningValidationIssue> currentErrors = new ArrayList<ProvisioningValidationIssue>();
  
      // check attributes
      for (int i=0; i< 20; i++) {
  
        String name = suffixToConfigValue.get("targetEntityAttribute."+i+".name");
        if (StringUtils.isBlank(name)) {
          break;
        }
        
        // there is an attribute here
        String suffix = "targetEntityAttribute."+i+".translateToMemberSyncField";
        String translateToGroupSyncField = suffixToConfigValue.get(suffix);
        
        if (StringUtils.equals(bucket, translateToGroupSyncField)) {
          currentErrors.add(new ProvisioningValidationIssue().assignMessage(errorMessage).assignJqueryHandle(suffix));
        }
        
      }
  
      String suffix = "common.entityLink." + bucket;
      if (!StringUtils.isBlank(suffixToConfigValue.get(suffix))) {
        currentErrors.add(new ProvisioningValidationIssue().assignMessage(errorMessage).assignJqueryHandle(suffix));
      }
      
      if (currentErrors.size() > 1) {
        this.addErrorMessages(currentErrors);
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
      this.addErrorMessage(new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.invalidClass")).assignJqueryHandle("class"));
      return;
    }
    
    List<String> errors = new ArrayList<String>();
    Map<String, String> formElementErrors = new LinkedHashMap<String, String>();
    
    provisionerConfiguration.validatePreSaveNonProvisionerSpecific(false, errors, formElementErrors);
    
    for (String error : errors) { 
      this.addErrorMessage(new ProvisioningValidationIssue().assignMessage(error));
    }
    for (String formElementErrorKey : formElementErrors.keySet()) {
      this.addErrorMessage(new ProvisioningValidationIssue().assignMessage(formElementErrors.get(formElementErrorKey)).assignJqueryHandle(formElementErrorKey));
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
      this.addErrorMessage(new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.invalidClass")).assignJqueryHandle("class"));
      return;
    }
    
    List<String> errors = new ArrayList<String>();
    Map<String, String> formElementErrors = new LinkedHashMap<String, String>();
    
    provisionerConfiguration.validatePreSaveNonProvisionerSpecific(false, errors, formElementErrors);
    
    for (String error : errors) { 
      this.addErrorMessage(new ProvisioningValidationIssue().assignMessage(error));
    }
    for (String formElementErrorKey : formElementErrors.keySet()) {
      this.addErrorMessage(new ProvisioningValidationIssue().assignMessage(formElementErrors.get(formElementErrorKey)).assignJqueryHandle(formElementErrorKey));
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

        String name = suffixToConfigValue.get(objectType+"."+i+".name");
        if (StringUtils.isBlank(name)) {
          continue OBJECT_TYPE;
        }
        String nameConfigKey = objectType + "."+i+".name";
        name = suffixToConfigValue.get(nameConfigKey);
        
        if (StringUtils.isBlank(name)) {
          
          // provisioning.configuration.validation.attributeNameRequired = Error: ${fieldType} name is required
          // TODO
          this.addErrorMessage(new ProvisioningValidationIssue().assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.attributeNameRequired")).assignJqueryHandle(objectType + "."+i+".name"));
          
          continue;
        }
        GrouperTextContainer.assignThreadLocalVariable("attributeName", name);
        
        MultiKey objectTypeAttributeTypeName = new MultiKey(objectType, false, name);
        if (objectTypeAttributeTypeNames.contains(objectTypeAttributeTypeName)) {
          
          // provisioning.configuration.validation.multipleAttributesSameName = Error: two ${type} ${fieldType}s have the same name '${attributeName}'
          this.addErrorMessage(new ProvisioningValidationIssue()
              .assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.multipleAttributesSameName"))
              .assignJqueryHandle(nameConfigKey));
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
  
    String attributeLabel = GrouperTextContainer.textOrNull("grouper.provisioning.attribute").toLowerCase();
        
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
  
        String name = suffixToConfigValue.get(objectType + "."+i+".name");
        if (StringUtils.isBlank(name)) {
          continue OBJECT_TYPE;
        }

        
        GrouperTextContainer.assignThreadLocalVariable("fieldType", attributeLabel);
        String nameConfigKey = objectType + "."+i+".name";
        name = suffixToConfigValue.get(nameConfigKey);
        
        if (StringUtils.isBlank(name)) {
          
          // provisioning.configuration.validation.attributeNameRequired = Error: ${fieldType} name is required
          this.addErrorMessage(new ProvisioningValidationIssue()
              .assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.attributeNameRequired"))
              .assignJqueryHandle("grouper.provisioning.attribute"));
          
          continue;
        }
        GrouperTextContainer.assignThreadLocalVariable("attributeName", name);
        
        MultiKey objectTypeAttributeTypeName = new MultiKey(objectType, false, name);
        if (objectTypeAttributeTypeNames.contains(objectTypeAttributeTypeName)) {
          
          // provisioning.configuration.validation.multipleAttributesSameName = Error: two ${type} ${fieldType}s have the same name '${attributeName}'
          this.addErrorMessage(new ProvisioningValidationIssue()
              .assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.multipleAttributesSameName"))
              .assignJqueryHandle(nameConfigKey));
        }
        objectTypeAttributeTypeNames.add(objectTypeAttributeTypeName);
      }      
    }
    GrouperTextContainer.resetThreadLocalVariableMap();
    
  }
  
  /**
   */
  public void validateNoFields() {

    // GRP-3931: change ldap DN from field name to attribute ldap_dn
    for (String objectType : new String[] {"Entity", "Group", "Membership" }) {
      for (int i=0;i<20;i++) {
        String fieldNameKey = "target" + objectType + "Attribute." + i + ".fieldName";
        String isFieldElseAttributeKey = "target" + objectType + "Attribute." + i + ".isFieldElseAttribute";
        if (!StringUtils.isBlank(suffixToConfigValue.get(fieldNameKey)) || !StringUtils.isBlank(suffixToConfigValue.get(isFieldElseAttributeKey))) {
          this.addErrorMessage(new ProvisioningValidationIssue()
              .assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.noFields"))
              .assignRuntimeError(true));
        }
      }
    }
  }      
  
  /**
   */
  public void validateSelectAllEntities() {
    // GRP-3938: provisioning selectAllEntities should not have a default
    if (GrouperUtil.booleanValue(suffixToConfigValue.get("operateOnGrouperEntities"), false) && 
        GrouperUtil.booleanValue(suffixToConfigValue.get("selectEntities"), false) && StringUtils.isBlank(suffixToConfigValue.get("selectAllEntities"))) {
      this.addErrorMessage(new ProvisioningValidationIssue()
          .assignMessage(GrouperTextContainer.textOrNull("provisioning.configuration.validation.selectAllEntitiesRequired"))
          .assignRuntimeError(true));
    }
  }
    
  
  public void validateEntityResolverRefactorDontUseOldValues() {
    
    // FROM provisioner.genericProvisioner.entityAttributesNotInSubjectSource
    // TO provisioner.genericProvisioner.entityResolver.entityAttributesNotInSubjectSource
    
    // GRP-3939: Refactor entity attribute resolver config
    for (String suffixToRefactor : UpgradeTasks.v8_entityResolverSuffixesToRefactor) {
      
      String resolverValue = suffixToConfigValue.get(suffixToRefactor);
      if (StringUtils.isBlank(resolverValue)) {
        continue;
      }
      String errorMessage = GrouperTextContainer.textOrNull("provisioning.configuration.validation.upgradeTask");
      errorMessage = StringUtils.replace(errorMessage, "$$attributeName$$", suffixToRefactor);

      this.addErrorMessage(new ProvisioningValidationIssue()
          .assignMessage(errorMessage)
          .assignRuntimeError(true));
      
    }
    
  }

  /**
   * 
   */
  public void validateCustomizeMembershipCrud() {
    // GRP-3953: add provisioning customizeMembershipCrud
    boolean operateOnGrouperMemberships = GrouperUtil.booleanValue(suffixToConfigValue.get("operateOnGrouperMemberships"), false);
    boolean customizeCrud = operateOnGrouperMemberships && GrouperUtil.booleanValue(suffixToConfigValue.get("customizeMembershipCrud"), false);
    boolean anythingSet = false;
    for (String key : new String[] { "insertMemberships", "selectMemberships", "deleteMemberships", "deleteMembershipsIfNotExistInGrouper", 
        "deleteMembershipsIfGrouperDeleted", "deleteMembershipsIfGrouperCreated"}) {
      if (!customizeCrud) {

          if (!StringUtils.isBlank(suffixToConfigValue.get(key))) {
            String errorMessage = GrouperTextContainer.textOrNull("provisioning.configuration.validation.upgradeTask");
            errorMessage = StringUtils.replace(errorMessage, "$$attributeName$$", key);

            this.addErrorMessage(new ProvisioningValidationIssue()
                .assignMessage(errorMessage)
                .assignRuntimeError(true));

          }
      } else {
        if (!StringUtils.isBlank(suffixToConfigValue.get(key))) {
          anythingSet = true;
        }
      }
    }
    
    if (customizeCrud && !anythingSet) {
      String errorMessage = GrouperTextContainer.textOrNull("provisioning.configuration.validation.customizeMembershipCrudButNothingSet");

      this.addErrorMessage(new ProvisioningValidationIssue()
          .assignMessage(errorMessage)
          .assignRuntimeError(false));
    }
  }

  /**
   * 
   */
  public void validateCustomizeGroupCrud() {
    // GRP-3954: add provisioning customizeGroupCrud
    boolean operateOnGrouperGroups = GrouperUtil.booleanValue(suffixToConfigValue.get("operateOnGrouperGroups"), false);
    boolean customizeCrud = operateOnGrouperGroups 
        && GrouperUtil.booleanValue(suffixToConfigValue.get("customizeGroupCrud"), false);
    boolean anythingSet = false;
    for (String key : new String[] { "insertGroups", "selectGroups", "updateGroups", "deleteGroups", "deleteGroupsIfNotExistInGrouper", 
        "deleteGroupsIfGrouperDeleted", "deleteGroupsIfGrouperCreated"}) {
      if (!customizeCrud) {

          if (!StringUtils.isBlank(suffixToConfigValue.get(key))) {
            String errorMessage = GrouperTextContainer.textOrNull("provisioning.configuration.validation.upgradeTask");
            errorMessage = StringUtils.replace(errorMessage, "$$attributeName$$", key);

            this.addErrorMessage(new ProvisioningValidationIssue()
                .assignMessage(errorMessage)
                .assignRuntimeError(true));

          }
      } else {
        if (!StringUtils.isBlank(suffixToConfigValue.get(key))) {
          anythingSet = true;
        }
      }
    }
    
    if (customizeCrud && !anythingSet) {
      String errorMessage = GrouperTextContainer.textOrNull("provisioning.configuration.validation.customizeGroupCrudButNothingSet");

      this.addErrorMessage(new ProvisioningValidationIssue()
          .assignMessage(errorMessage)
          .assignRuntimeError(false));
    }
  }
  
  /**
   * 
   */
  public void validateMembershipShowValidation() {
    // GRP-3957: provisioning membership show validation settings
    for (int i=0;i<20;i++) {
      
      String requiredKey = "targetMembershipAttribute." + i + ".required";
      String maxlengthKey = "targetMembershipAttribute." + i + ".maxlength";
      String validExpressionKey = "targetMembershipAttribute." + i + ".validExpression";
      String showAttributeValidationKey = "targetMembershipAttribute." + i + ".showAttributeValidation";
      
      if (GrouperUtil.booleanValue(suffixToConfigValue.get(showAttributeValidationKey), false)) {
        // already done
        continue;
      }
      // cannot contain any of these if now showing membership attribute validation
      for (String key : new String[] {requiredKey, maxlengthKey, validExpressionKey}) {
        
        if (suffixToConfigValue.containsKey(key)) {
          String errorMessage = GrouperTextContainer.textOrNull("provisioning.configuration.validation.upgradeTask");
          errorMessage = StringUtils.replace(errorMessage, "$$attributeName$$", key);

          this.addErrorMessage(new ProvisioningValidationIssue()
              .assignMessage(errorMessage)
              .assignRuntimeError(true));
        }
      }
    }      
  }

  /**
   * 
   */
  public void validateMembershipShowAttributeCrud() {
    // GRP-3960: provisioning membership attribute customize CRUD
    for (int i=0;i<20;i++) {
      
      String insertKey = "targetMembershipAttribute." + i + ".insert";
      String selectKey = "targetMembershipAttribute." + i + ".select";
      String showAttributeCrudKey = "targetMembershipAttribute." + i + ".showAttributeCrud";
      
      if (GrouperUtil.booleanValue(suffixToConfigValue.get(showAttributeCrudKey), false)) {
        // already done
        continue;
      }
      // cannot contain any of these if now showing membership attribute validation
      for (String key : new String[] {insertKey, selectKey}) {
        
        if (suffixToConfigValue.containsKey(key)) {
          String errorMessage = GrouperTextContainer.textOrNull("provisioning.configuration.validation.upgradeTask");
          errorMessage = StringUtils.replace(errorMessage, "$$attributeName$$", key);

          this.addErrorMessage(new ProvisioningValidationIssue()
              .assignMessage(errorMessage)
              .assignRuntimeError(true));
        }
      }
    }      
  }
  /**
   * 
   */
  public void validateGroupShowAttributeCrud() {
    // GRP-3961: provisioning group attribute customize CRUD
    for (int i=0;i<20;i++) {
      
      String insertKey = "targetGroupAttribute." + i + ".insert";
      String selectKey = "targetGroupAttribute." + i + ".select";
      String showAttributeCrudKey = "targetGroupAttribute." + i + ".showAttributeCrud";
      
      if (GrouperUtil.booleanValue(suffixToConfigValue.get(showAttributeCrudKey), false)) {
        // already done
        continue;
      }
      // cannot contain any of these if now showing Group attribute validation
      for (String key : new String[] {insertKey, selectKey}) {
        
        if (suffixToConfigValue.containsKey(key)) {
          String errorMessage = GrouperTextContainer.textOrNull("provisioning.configuration.validation.upgradeTask");
          errorMessage = StringUtils.replace(errorMessage, "$$attributeName$$", key);

          this.addErrorMessage(new ProvisioningValidationIssue()
              .assignMessage(errorMessage)
              .assignRuntimeError(true));
        }
      }
    }      
  }


  /**
   * 
   */
  public void validateGroupShowValidation() {
    // GRP-3958: provisioning group show validation settings
    for (int i=0;i<20;i++) {
      
      String requiredKey = "targetGroupAttribute." + i + ".required";
      String maxlengthKey = "targetGroupAttribute." + i + ".maxlength";
      String validExpressionKey = "targetGroupAttribute." + i + ".validExpression";
      String showAttributeValidationKey = "targetGroupAttribute." + i + ".showAttributeValidation";
      
      if (GrouperUtil.booleanValue(suffixToConfigValue.get(showAttributeValidationKey), false)) {
        // already done
        continue;
      }
      // cannot contain any of these if now showing Group attribute validation
      for (String key : new String[] {requiredKey, maxlengthKey, validExpressionKey}) {
        
        if (suffixToConfigValue.containsKey(key)) {
          String errorMessage = GrouperTextContainer.textOrNull("provisioning.configuration.validation.upgradeTask");
          errorMessage = StringUtils.replace(errorMessage, "$$attributeName$$", key);

          this.addErrorMessage(new ProvisioningValidationIssue()
              .assignMessage(errorMessage)
              .assignRuntimeError(true));
        }
      }
    }      
  }

  /**
   * 
   */
  public void validateEntityShowValidation() {
    // GRP-3959: provisioning entity show validation settings
    for (int i=0;i<20;i++) {
      
      String requiredKey = "targetEntityAttribute." + i + ".required";
      String maxlengthKey = "targetEntityAttribute." + i + ".maxlength";
      String validExpressionKey = "targetEntityAttribute." + i + ".validExpression";
      String showAttributeValidationKey = "targetEntityAttribute." + i + ".showAttributeValidation";
      
      if (GrouperUtil.booleanValue(suffixToConfigValue.get(showAttributeValidationKey), false)) {
        // already done
        continue;
      }
      // cannot contain any of these if now showing Entity attribute validation
      for (String key : new String[] {requiredKey, maxlengthKey, validExpressionKey}) {
        
        if (suffixToConfigValue.containsKey(key)) {
          String errorMessage = GrouperTextContainer.textOrNull("provisioning.configuration.validation.upgradeTask");
          errorMessage = StringUtils.replace(errorMessage, "$$attributeName$$", key);

          this.addErrorMessage(new ProvisioningValidationIssue()
              .assignMessage(errorMessage)
              .assignRuntimeError(true));
        }
      }
    }      
  }

  
  /**
   * 
   */
  public void validateCustomizeEntityCrud() {
    // GRP-3955: add provisioning customizeEntityCrud
    boolean operateOnGrouperEntities = GrouperUtil.booleanValue(suffixToConfigValue.get("operateOnGrouperEntities"), false);
    boolean customizeCrud = operateOnGrouperEntities 
        && GrouperUtil.booleanValue(suffixToConfigValue.get("customizeEntityCrud"), false);
    
    boolean makeChangesToEntities = operateOnGrouperEntities 
        && GrouperUtil.booleanValue(suffixToConfigValue.get("makeChangesToEntities"), false);
    
    boolean anythingSet = false;
    for (String key : new String[] { "insertEntities", "selectEntities", "updateEntities", "deleteEntities", "deleteEntitiesIfNotExistInGrouper", 
        "deleteEntitiesIfGrouperDeleted", "deleteEntitiesIfGrouperCreated"}) {
      if (!customizeCrud || (!makeChangesToEntities && !StringUtils.equals("selectEntities", key))) {

          if (!StringUtils.isBlank(suffixToConfigValue.get(key))) {
            String errorMessage = GrouperTextContainer.textOrNull("provisioning.configuration.validation.upgradeTask");
            errorMessage = StringUtils.replace(errorMessage, "$$attributeName$$", key);

            this.addErrorMessage(new ProvisioningValidationIssue()
                .assignMessage(errorMessage)
                .assignRuntimeError(true));

          }
      } else {
        if (!StringUtils.isBlank(suffixToConfigValue.get(key))) {
          anythingSet = true;
        }
      }
      
    }
    
    if (customizeCrud && !anythingSet) {
      String errorMessage = GrouperTextContainer.textOrNull("provisioning.configuration.validation.customizeEntityCrudButNothingSet");

      this.addErrorMessage(new ProvisioningValidationIssue()
          .assignMessage(errorMessage)
          .assignRuntimeError(false));
    }
  }

}
 