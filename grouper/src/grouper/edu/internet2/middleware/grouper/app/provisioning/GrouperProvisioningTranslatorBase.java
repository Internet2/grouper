package edu.internet2.middleware.grouper.app.provisioning;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncErrorCode;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;

/**
 * @author shilen
 */
public class GrouperProvisioningTranslatorBase {

  /**
   * reference back up to the provisioner
   */
  private GrouperProvisioner grouperProvisioner = null;

  /**
   * reference back up to the provisioner
   * @return the provisioner
   */
  public GrouperProvisioner getGrouperProvisioner() {
    return this.grouperProvisioner;
  }

  /**
   * reference back up to the provisioner
   * @param grouperProvisioner1
   */
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner1) {
    this.grouperProvisioner = grouperProvisioner1;
  }

  /**
   * @param targetGroups
   * @param targetEntities
   * @param targetMemberships
   * @return translated objects from grouper to target
   */
  public void translateGrouperToTarget() {
    {
      List<ProvisioningGroup> grouperProvisioningGroups = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperProvisioningGroups();
      List<ProvisioningGroup> grouperTargetGroups = translateGrouperToTargetGroups(grouperProvisioningGroups, false, false);
      this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouperTarget().getGrouperTargetObjects().setProvisioningGroups(grouperTargetGroups);
    }
    
    {
      List<ProvisioningEntity> grouperProvisioningEntities = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperProvisioningEntities();
      List<ProvisioningEntity> grouperTargetEntities = translateGrouperToTargetEntities(
          grouperProvisioningEntities, false, false);
      this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouperTarget().getGrouperTargetObjects().setProvisioningEntities(grouperTargetEntities);
    }    

    {
      List<ProvisioningMembership> grouperProvisioningMemberships = this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperProvisioningMemberships();
      List<ProvisioningMembership> grouperTargetMemberships = translateGrouperToTargetMemberships(
          grouperProvisioningMemberships, false);
      this.getGrouperProvisioner().retrieveGrouperProvisioningDataGrouperTarget().getGrouperTargetObjects().setProvisioningMemberships(grouperTargetMemberships);
    }    

    
  }

  /**
   * keep a reference to the membership wrapper so attributes can register with membership
   */
  private static ThreadLocal<ProvisioningMembershipWrapper> provisioningMembershipWrapperThreadLocal = new InheritableThreadLocal<ProvisioningMembershipWrapper>();
  
  
  /**
   * keep a reference to the membership wrapper so attributes can register with membership
   * @return membership wrapper
   */
  public static ProvisioningMembershipWrapper retrieveProvisioningMembershipWrapper() {
    return provisioningMembershipWrapperThreadLocal.get();
  }

  public List<ProvisioningMembership> translateGrouperToTargetMemberships(
      List<ProvisioningMembership> grouperProvisioningMemberships, boolean includeDelete) {
    
    int invalidMembershipsDuringTranslation = 0;
    
    // clear out the membership attribute, it might have a default value in there
    if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes) {
      String groupMembershipAttribute = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupAttributeNameForMemberships();
      if (!StringUtils.isBlank(groupMembershipAttribute)) {
        for (ProvisioningGroup provisioningGroup : this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetGroups()) {
          provisioningGroup.clearAttribute(groupMembershipAttribute);
        }
      }
    }
    if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes) {
      String entityMembershipAttribute = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getUserAttributeNameForMemberships();
      if (!StringUtils.isBlank(entityMembershipAttribute)) {
        for (ProvisioningEntity provisioningEntity : this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetEntities()) {
          provisioningEntity.clearAttribute(entityMembershipAttribute);
        }
      }
    }
    
    List<ProvisioningMembership> grouperTargetMemberships = new ArrayList<ProvisioningMembership>();
    List<String> scripts = GrouperUtil.nonNull(GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGrouperProvisioningToTargetTranslation()).get("Membership"));

    for (ProvisioningMembership grouperProvisioningMembership: GrouperUtil.nonNull(grouperProvisioningMemberships)) {

      ProvisioningMembershipWrapper provisioningMembershipWrapper = grouperProvisioningMembership.getProvisioningMembershipWrapper();
      GcGrouperSyncMembership gcGrouperSyncMembership = provisioningMembershipWrapper.getGcGrouperSyncMembership();

      ProvisioningGroupWrapper provisioningGroupWrapper = grouperProvisioningMembership.getProvisioningGroup().getProvisioningGroupWrapper();
      ProvisioningGroup grouperTargetGroup = provisioningGroupWrapper.getGrouperTargetGroup();

      GcGrouperSyncErrorCode errorCode = provisioningGroupWrapper.getErrorCode();
      String errorMessage = provisioningGroupWrapper.getGcGrouperSyncGroup().getErrorMessage();
          
      ProvisioningEntityWrapper provisioningEntityWrapper = grouperProvisioningMembership.getProvisioningEntity().getProvisioningEntityWrapper();
      ProvisioningEntity grouperTargetEntity = provisioningEntityWrapper.getGrouperTargetEntity();

      if (errorCode == null) {
        errorCode = provisioningEntityWrapper.getErrorCode();
        errorMessage = provisioningEntityWrapper.getGcGrouperSyncMember().getErrorMessage();
      }
      
      if (errorCode != null) {
        this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().assignMembershipError(provisioningMembershipWrapper, errorCode, errorMessage);
        continue;
      }


      ProvisioningMembership grouperTargetMembership = new ProvisioningMembership();
      grouperTargetMembership.setProvisioningMembershipWrapper(provisioningMembershipWrapper);
 
      provisioningMembershipWrapperThreadLocal.set(provisioningMembershipWrapper);
      try {

        Map<String, Object> elVariableMap = new HashMap<String, Object>();
        
        elVariableMap.put("grouperProvisioningGroup", grouperProvisioningMembership.getProvisioningGroup());
        elVariableMap.put("provisioningGroupWrapper", provisioningGroupWrapper);
        elVariableMap.put("grouperTargetGroup", grouperTargetGroup);
        elVariableMap.put("gcGrouperSyncGroup", provisioningGroupWrapper.getGcGrouperSyncGroup());
 
        elVariableMap.put("grouperProvisioningEntity", grouperProvisioningMembership.getProvisioningEntity());
        elVariableMap.put("provisioningEntityWrapper", provisioningEntityWrapper);
        elVariableMap.put("grouperTargetEntity", grouperTargetEntity);
        elVariableMap.put("gcGrouperSyncMember", provisioningEntityWrapper.getGcGrouperSyncMember());
        
        elVariableMap.put("grouperProvisioningMembership", grouperProvisioningMembership);
        elVariableMap.put("provisioningMembershipWrapper", provisioningMembershipWrapper);
        elVariableMap.put("grouperTargetMembership", grouperTargetMembership);
        elVariableMap.put("gcGrouperSyncMembership", gcGrouperSyncMembership);

        // attribute translations
        for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetMembershipAttributeNameToConfig().values()) {
          String expression = grouperProvisioningConfigurationAttribute.getTranslateExpression();
          boolean hasExpression = !StringUtils.isBlank(expression);
          String expressionToUse = null;
          if (hasExpression) {
            expressionToUse = expression;
          }
          if (!StringUtils.isBlank(expressionToUse)) {
            Object result = runScript(expressionToUse, elVariableMap);
            
            grouperTargetMembership.assignAttributeValue(grouperProvisioningConfigurationAttribute.getName(), result);
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateValue(grouperTargetMembership, grouperProvisioningConfigurationAttribute, null);

          }
        }
        
        if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes) {
          String groupMembershipAttribute = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupAttributeNameForMemberships();
          if (!StringUtils.isEmpty(groupMembershipAttribute)) {
            GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetGroupAttributeNameToConfig().get(groupMembershipAttribute);
            if (grouperProvisioningConfigurationAttribute != null) {
              
              Object result = null;
              
              if (!StringUtils.isBlank(grouperProvisioningConfigurationAttribute.getTranslateExpressionFromMembership())) {
                result = runScript(grouperProvisioningConfigurationAttribute.getTranslateExpressionFromMembership(), elVariableMap);
              } else if (!StringUtils.isBlank(grouperProvisioningConfigurationAttribute.getTranslateFromMemberSyncField())) {
                result = translateFromMemberSyncField(provisioningEntityWrapper.getGcGrouperSyncMember(), 
                    grouperProvisioningConfigurationAttribute.getTranslateFromMemberSyncField());
              }
              if (result != null) {
                
                MultiKey validationError = this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().validFieldOrAttributeValue(grouperTargetGroup, grouperProvisioningConfigurationAttribute, result);
                if (validationError != null) {
                  
                  errorCode = (GcGrouperSyncErrorCode)validationError.getKey(0);
                  errorMessage = (String)validationError.getKey(1);
                  this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().assignMembershipError(provisioningMembershipWrapper, errorCode, errorMessage);

                  continue;
                }
                
                grouperTargetGroup.addAttributeValueForMembership(groupMembershipAttribute, result);
                this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateValue(grouperTargetGroup, grouperProvisioningConfigurationAttribute, null);
              }
            }
          }
        }
        
        if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes) {
          String userMembershipAttribute = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getUserAttributeNameForMemberships();
          if (!StringUtils.isEmpty(userMembershipAttribute)) {
            GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetEntityAttributeNameToConfig().get(userMembershipAttribute);

            if (grouperProvisioningConfigurationAttribute != null) {
              
              Object result = null;
              
              if (!StringUtils.isBlank(grouperProvisioningConfigurationAttribute.getTranslateExpressionFromMembership())) {
                result = runScript(grouperProvisioningConfigurationAttribute.getTranslateExpressionFromMembership(), elVariableMap);
              } else if (!StringUtils.isBlank(grouperProvisioningConfigurationAttribute.getTranslateFromGroupSyncField())) {
                result = translateFromGroupSyncField(provisioningGroupWrapper.getGcGrouperSyncGroup(), 
                    grouperProvisioningConfigurationAttribute.getTranslateFromGroupSyncField());
              }
              if (result != null) {
                
                MultiKey validationError = this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().validFieldOrAttributeValue(grouperTargetEntity, grouperProvisioningConfigurationAttribute, result);
                if (validationError != null) {
                  
                  errorCode = (GcGrouperSyncErrorCode)validationError.getKey(0);
                  errorMessage = (String)validationError.getKey(1);
                  this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().assignMembershipError(provisioningMembershipWrapper, errorCode, errorMessage);

                  continue;
                }

                grouperTargetEntity.addAttributeValueForMembership(userMembershipAttribute, result);
                this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateValue(grouperTargetEntity, grouperProvisioningConfigurationAttribute, null);
              }
            }
          }
        }
        
        // field configurations
        grouperTargetMembership.setId(GrouperUtil.stringValue(fieldTranslation( 
            grouperTargetMembership.getId(), elVariableMap, false, 
            this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetMembershipFieldNameToConfig().get("id"), 
            provisioningGroupWrapper.getGcGrouperSyncGroup(), provisioningEntityWrapper.getGcGrouperSyncMember())));
        grouperTargetMembership.setProvisioningEntityId(GrouperUtil.stringValue(fieldTranslation( 
            grouperTargetMembership.getProvisioningEntityId(), elVariableMap, false, 
            this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetMembershipFieldNameToConfig().get("provisioningEntityId"), 
            provisioningGroupWrapper.getGcGrouperSyncGroup(), provisioningEntityWrapper.getGcGrouperSyncMember())));
        grouperTargetMembership.setProvisioningGroupId(GrouperUtil.stringValue(fieldTranslation( 
            grouperTargetMembership.getProvisioningGroupId(), elVariableMap, false, 
            this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetMembershipFieldNameToConfig().get("provisioningGroupId"), 
            provisioningGroupWrapper.getGcGrouperSyncGroup(), provisioningEntityWrapper.getGcGrouperSyncMember())));

        for (String script: scripts) {

          runScript(script, elVariableMap);
          
        }
      } finally {
        provisioningMembershipWrapperThreadLocal.remove();
      }
      if (grouperTargetMembership.isRemoveFromList() || grouperTargetMembership.isEmpty()) {
        continue;
      }
      if (grouperTargetGroup != null) {
        if (!StringUtils.equals(grouperTargetGroup.getId(), grouperTargetMembership.getProvisioningGroupId())) {
          grouperTargetMembership.setProvisioningGroupId(grouperTargetGroup.getId());
          grouperTargetMembership.setProvisioningGroup(grouperTargetGroup);
        }
      }
      
      if (grouperTargetEntity != null) {
        if (!StringUtils.equals(grouperTargetEntity.getId(), grouperTargetMembership.getProvisioningEntityId())) {
          grouperTargetMembership.setProvisioningEntityId(grouperTargetEntity.getId());
          grouperTargetMembership.setProvisioningEntity(grouperTargetEntity);
        }
      } 

      grouperTargetMembership.getProvisioningMembershipWrapper().setGrouperTargetMembership(grouperTargetMembership);
      if (includeDelete) {
        grouperTargetMembership.getProvisioningMembershipWrapper().setDelete(true);
      }

      grouperTargetMemberships.add(grouperTargetMembership); 
    }
    
    // set default for membership attribute, it might be blank and have a default value in there
    if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes) {
      String groupMembershipAttribute = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupAttributeNameForMemberships();
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetGroupAttributeNameToConfig().get(groupMembershipAttribute);

      if (!StringUtils.isBlank(groupMembershipAttribute) && grouperProvisioningConfigurationAttribute != null && !StringUtils.isBlank(grouperProvisioningConfigurationAttribute.getDefaultValue())) {
        this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().assignDefaultsForGroups(this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetGroups(), grouperProvisioningConfigurationAttribute);
      }
    }
    if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes) {
      String entityMembershipAttribute = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getUserAttributeNameForMemberships();
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetEntityAttributeNameToConfig().get(entityMembershipAttribute);

      if (!StringUtils.isBlank(entityMembershipAttribute) && grouperProvisioningConfigurationAttribute != null && !StringUtils.isBlank(grouperProvisioningConfigurationAttribute.getDefaultValue())) {
        this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().assignDefaultsForEntities(this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetEntities(), grouperProvisioningConfigurationAttribute);
      }
    }
    
    if (invalidMembershipsDuringTranslation > 0) {
      GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "invalidMembershipsDuringTranslation", invalidMembershipsDuringTranslation);
    }

    return grouperTargetMemberships;
  }

  public List<ProvisioningEntity> translateGrouperToTargetEntities(
      List<ProvisioningEntity> grouperProvisioningEntities, boolean includeDelete, boolean forCreate) {
    
    List<ProvisioningEntity> grouperTargetEntities = new ArrayList<ProvisioningEntity>();

    List<String> scripts = GrouperUtil.nonNull(GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGrouperProvisioningToTargetTranslation()).get("Entity"));
    
    if (forCreate) {
      scripts.addAll(GrouperUtil.nonNull(GrouperUtil.nonNull(
          this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGrouperProvisioningToTargetTranslation()).get("EntityCreateOnly")));
    }

    PROVISIONING_ENTITY_BLOCK: for (ProvisioningEntity grouperProvisioningEntity: GrouperUtil.nonNull(grouperProvisioningEntities)) {
      
      ProvisioningEntity grouperTargetEntity = new ProvisioningEntity();
      grouperTargetEntity.setProvisioningEntityWrapper(grouperProvisioningEntity.getProvisioningEntityWrapper());

      Map<String, Object> elVariableMap = new HashMap<String, Object>();
      elVariableMap.put("grouperProvisioningEntity", grouperProvisioningEntity);
      elVariableMap.put("provisioningEntityWrapper", grouperProvisioningEntity.getProvisioningEntityWrapper());
      GcGrouperSyncMember gcGrouperSyncMember = grouperProvisioningEntity.getProvisioningEntityWrapper().getGcGrouperSyncMember();
      elVariableMap.put("gcGrouperSyncMember", gcGrouperSyncMember);
      elVariableMap.put("grouperTargetEntity", grouperTargetEntity);

      // do the required's first
      for (boolean required : new boolean[] {true, false}) {
        // attribute translations
        for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetEntityAttributeNameToConfig().values()) {
          if (grouperProvisioningConfigurationAttribute.isRequired() == required) {
            String expression = grouperProvisioningConfigurationAttribute.getTranslateExpression();
            boolean hasExpression = !StringUtils.isBlank(expression);
            String expressionCreateOnly = grouperProvisioningConfigurationAttribute.getTranslateExpressionCreateOnly();
            boolean hasExpressionCreateOnly = !StringUtils.isBlank(expressionCreateOnly);
            String expressionToUse = null;
            if (forCreate && hasExpressionCreateOnly) {
              expressionToUse = expressionCreateOnly;
            } else if (hasExpression) {
              expressionToUse = expression;
            }
            Object result = null;
            if (!StringUtils.isBlank(expressionToUse) || !StringUtils.isBlank(grouperProvisioningConfigurationAttribute.getTranslateFromMemberSyncField())) {
              if (!StringUtils.isBlank(expressionToUse)) {
                result = runScript(expressionToUse, elVariableMap);
              } else if (!StringUtils.isBlank(grouperProvisioningConfigurationAttribute.getTranslateFromMemberSyncField())) {
                result = translateFromMemberSyncField(gcGrouperSyncMember, 
                    grouperProvisioningConfigurationAttribute.getTranslateFromMemberSyncField());
              }
              grouperTargetEntity.assignAttributeValue(grouperProvisioningConfigurationAttribute.getName(), result);
              this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateValue(grouperTargetEntity, grouperProvisioningConfigurationAttribute, null);
              if (required && GrouperUtil.isBlank(result)) {
                // short circuit this since other fields might need this field and its not there and invalid anyways
                continue PROVISIONING_ENTITY_BLOCK;
              }
            
            }
          }
        }
        
        // field configurations
        {
          GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttributeId = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetEntityFieldNameToConfig().get("id");
          if (grouperProvisioningConfigurationAttributeId != null && grouperProvisioningConfigurationAttributeId.isRequired() == required) {
            grouperTargetEntity.setId(GrouperUtil.stringValue(fieldTranslation( 
                grouperTargetEntity.getId(), elVariableMap, forCreate, 
                grouperProvisioningConfigurationAttributeId, null, gcGrouperSyncMember)));
            if (required && StringUtils.isBlank(grouperProvisioningEntity.getId())) {
              continue PROVISIONING_ENTITY_BLOCK;
            }
          }
        }
        
        {
          GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttributeName = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetEntityFieldNameToConfig().get("name");
          if (grouperProvisioningConfigurationAttributeName != null && grouperProvisioningConfigurationAttributeName.isRequired() == required) {
            grouperTargetEntity.setName(GrouperUtil.stringValue(fieldTranslation( 
                grouperTargetEntity.getName(), elVariableMap, forCreate, 
                grouperProvisioningConfigurationAttributeName, null, gcGrouperSyncMember)));
            if (required && StringUtils.isBlank(grouperProvisioningEntity.getName())) {
              continue PROVISIONING_ENTITY_BLOCK;
            }
          }
        }
        
        {
          GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttributeEmail = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetEntityFieldNameToConfig().get("email");
          if (grouperProvisioningConfigurationAttributeEmail != null && grouperProvisioningConfigurationAttributeEmail.isRequired() == required) {
            grouperTargetEntity.setEmail(GrouperUtil.stringValue(fieldTranslation( 
                grouperTargetEntity.getEmail(), elVariableMap, forCreate, 
                grouperProvisioningConfigurationAttributeEmail, null, gcGrouperSyncMember)));
            if (required && StringUtils.isBlank(grouperProvisioningEntity.getEmail())) {
              continue PROVISIONING_ENTITY_BLOCK;
            }
          }
          
        }
        
        {
          GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttributeLoginId = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetEntityFieldNameToConfig().get("loginId");
          if (grouperProvisioningConfigurationAttributeLoginId != null && grouperProvisioningConfigurationAttributeLoginId.isRequired() == required) {
            grouperTargetEntity.setLoginId(GrouperUtil.stringValue(fieldTranslation( 
                grouperTargetEntity.getLoginId(), elVariableMap, forCreate, 
                grouperProvisioningConfigurationAttributeLoginId, null, gcGrouperSyncMember)));
            if (required && StringUtils.isBlank(grouperProvisioningEntity.getLoginId())) {
              continue PROVISIONING_ENTITY_BLOCK;
            }
          }
        }
      }
      
      for (String script: scripts) {
               
        runScript(script, elVariableMap);
        
      }

      if (grouperTargetEntity.isRemoveFromList() || grouperTargetEntity.isEmpty()) {
        continue;
      }
      
      grouperTargetEntities.add(grouperTargetEntity);
      
      grouperProvisioningEntity.getProvisioningEntityWrapper().setGrouperTargetEntity(grouperTargetEntity);
      if (includeDelete) {
        grouperProvisioningEntity.getProvisioningEntityWrapper().setDelete(true);
      } else if (forCreate) {
        grouperProvisioningEntity.getProvisioningEntityWrapper().setCreate(true);
      }

    }
    return grouperTargetEntities;
  }

  public List<ProvisioningGroup> translateGrouperToTargetGroups(List<ProvisioningGroup> grouperProvisioningGroups, boolean includeDelete, boolean forCreate) {

    List<String> scripts = GrouperUtil.nonNull(GrouperUtil.nonNull(
        this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGrouperProvisioningToTargetTranslation()).get("Group"));

    if (forCreate) {
      scripts.addAll(GrouperUtil.nonNull(GrouperUtil.nonNull(
          this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGrouperProvisioningToTargetTranslation()).get("GroupCreateOnly")));
    }
    List<ProvisioningGroup> grouperTargetGroups = new ArrayList<ProvisioningGroup>();

    PROVISIONING_GROUP_BLOCK: for (ProvisioningGroup grouperProvisioningGroup: GrouperUtil.nonNull(grouperProvisioningGroups)) {
      
      ProvisioningGroup grouperTargetGroup = new ProvisioningGroup();
      grouperTargetGroup.setProvisioningGroupWrapper(grouperProvisioningGroup.getProvisioningGroupWrapper());

      Map<String, Object> elVariableMap = new HashMap<String, Object>();
      elVariableMap.put("grouperProvisioningGroup", grouperProvisioningGroup);
      elVariableMap.put("provisioningGroupWrapper", grouperProvisioningGroup.getProvisioningGroupWrapper());
      elVariableMap.put("grouperTargetGroup", grouperTargetGroup);
      GcGrouperSyncGroup gcGrouperSyncGroup = grouperProvisioningGroup.getProvisioningGroupWrapper().getGcGrouperSyncGroup();
      elVariableMap.put("gcGrouperSyncGroup", gcGrouperSyncGroup);

      // do the required's first
      for (boolean required : new boolean[] {true, false}) {
        // attribute translations
        for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetGroupAttributeNameToConfig().values()) {
          if (grouperProvisioningConfigurationAttribute.isRequired() == required) {
            String expression = grouperProvisioningConfigurationAttribute.getTranslateExpression();
            boolean hasExpression = !StringUtils.isBlank(expression);
            String expressionCreateOnly = grouperProvisioningConfigurationAttribute.getTranslateExpressionCreateOnly();
            boolean hasExpressionCreateOnly = !StringUtils.isBlank(expressionCreateOnly);
            String expressionToUse = null;
            if (forCreate && hasExpressionCreateOnly) {
              expressionToUse = expressionCreateOnly;
            } else if (hasExpression) {
              expressionToUse = expression;
            }
            Object result = null;
            if (!StringUtils.isBlank(expressionToUse) || !StringUtils.isBlank(grouperProvisioningConfigurationAttribute.getTranslateFromGroupSyncField())) {
              if (!StringUtils.isBlank(expressionToUse)) {
                result = runScript(expressionToUse, elVariableMap);
              } else if (!StringUtils.isBlank(grouperProvisioningConfigurationAttribute.getTranslateFromGroupSyncField())) {
                result = translateFromGroupSyncField(gcGrouperSyncGroup, grouperProvisioningConfigurationAttribute.getTranslateFromGroupSyncField());
              }
              grouperTargetGroup.assignAttributeValue(grouperProvisioningConfigurationAttribute.getName(), result);
              this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateValue(grouperTargetGroup, grouperProvisioningConfigurationAttribute, null);
              if (required && GrouperUtil.isBlank(result)) {
                // short circuit this since other fields might need this field and its not there and invalid anyways
                continue PROVISIONING_GROUP_BLOCK;
              }
            }
          }
        }        
        {
          // field configurations
          GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttributeId = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetGroupFieldNameToConfig().get("id");
          if (grouperProvisioningConfigurationAttributeId != null && grouperProvisioningConfigurationAttributeId.isRequired() == required) {
            grouperTargetGroup.setId(GrouperUtil.stringValue(fieldTranslation( 
                grouperTargetGroup.getId(), elVariableMap, forCreate, 
                grouperProvisioningConfigurationAttributeId, gcGrouperSyncGroup, null)));
            if (required && StringUtils.isBlank(grouperTargetGroup.getId())) {
              continue PROVISIONING_GROUP_BLOCK;
            }
          }
        }
        {
          GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttributeName = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetGroupFieldNameToConfig().get("name");
          if (grouperProvisioningConfigurationAttributeName != null && grouperProvisioningConfigurationAttributeName.isRequired() == required) {
            grouperTargetGroup.setName(GrouperUtil.stringValue(fieldTranslation( 
                grouperTargetGroup.getName(), elVariableMap, forCreate, 
                grouperProvisioningConfigurationAttributeName, gcGrouperSyncGroup, null)));
            if (required && StringUtils.isBlank(grouperTargetGroup.getName())) {
              continue PROVISIONING_GROUP_BLOCK;
            }
          }
        }
        {
          GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttributeIdIndex = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetGroupFieldNameToConfig().get("idIndex");
          if (grouperProvisioningConfigurationAttributeIdIndex != null && grouperProvisioningConfigurationAttributeIdIndex.isRequired() == required) {
            grouperTargetGroup.setIdIndex(GrouperUtil.longObjectValue(fieldTranslation( 
                grouperTargetGroup.getIdIndex(), elVariableMap, forCreate, 
                grouperProvisioningConfigurationAttributeIdIndex, gcGrouperSyncGroup, null), true));
            if (required && null == grouperTargetGroup.getIdIndex()) {
              continue PROVISIONING_GROUP_BLOCK;
            }
          }
        }
        {
          GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttributeDisplayName = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetGroupFieldNameToConfig().get("displayName");
          if (grouperProvisioningConfigurationAttributeDisplayName != null && grouperProvisioningConfigurationAttributeDisplayName.isRequired() == required) {
            grouperTargetGroup.setDisplayName(GrouperUtil.stringValue(fieldTranslation( 
                grouperTargetGroup.getDisplayName(), elVariableMap, forCreate, 
                grouperProvisioningConfigurationAttributeDisplayName, gcGrouperSyncGroup, null)));
            if (required && StringUtils.isBlank(grouperTargetGroup.getDisplayName())) {
              continue PROVISIONING_GROUP_BLOCK;
            }
          }
        }
      }      
      
      for (String script: scripts) {

        
        runScript(script, elVariableMap);
        
      }

      if (grouperTargetGroup.isRemoveFromList() || grouperTargetGroup.isEmpty()) {
        continue;
      }

      grouperProvisioningGroup.getProvisioningGroupWrapper().setGrouperTargetGroup(grouperTargetGroup);
      if (includeDelete) {
        grouperProvisioningGroup.getProvisioningGroupWrapper().setDelete(true);
      } else if (forCreate) {
        grouperProvisioningGroup.getProvisioningGroupWrapper().setCreate(true);
      }
      grouperTargetGroups.add(grouperTargetGroup);
        
    }
    return grouperTargetGroups;
  }

  /**
   * translate from gc grouper sync group and field name to the value
   * @param gcGrouperSyncGroup
   * @param field
   * @return the value
   */
  public static Object translateFromGroupSyncField(GcGrouperSyncGroup gcGrouperSyncGroup, String field) {
    
    if (StringUtils.equals("groupId", field)) {
      return gcGrouperSyncGroup.getGroupId();
    }
    if (StringUtils.equals("groupIdIndex", field)) {
      return gcGrouperSyncGroup.getGroupIdIndex();
    }
    if (StringUtils.equals("groupName", field)) {
      return gcGrouperSyncGroup.getGroupName();
    }
    if (StringUtils.equals("groupFromId2", field)) {
      return gcGrouperSyncGroup.getGroupFromId2();
    }
    if (StringUtils.equals("groupFromId3", field)) {
      return gcGrouperSyncGroup.getGroupFromId3();
    }
    if (StringUtils.equals("groupToId2", field)) {
      return gcGrouperSyncGroup.getGroupToId2();
    }
    if (StringUtils.equals("groupToId3", field)) {
      return gcGrouperSyncGroup.getGroupToId3();
    }
    throw new RuntimeException("Not expecting groupSyncField: '" + field + "'");
  }
  

  /**
   * translate from gc grouper sync member and field name to the value
   * @param gcGrouperSyncMember
   * @param field
   * @return the value
   */
  public static Object translateFromMemberSyncField(GcGrouperSyncMember gcGrouperSyncMember, String field) {
    
    if (StringUtils.equals("memberId", field)) {
      return gcGrouperSyncMember.getMemberId();
    }
    if (StringUtils.equals("subjectId", field)) {
      return gcGrouperSyncMember.getSubjectId();
    }
    if (StringUtils.equals("subjectIdentifier", field)) {
      return gcGrouperSyncMember.getSubjectIdentifier();
    }
    if (StringUtils.equals("memberFromId2", field)) {
      return gcGrouperSyncMember.getMemberFromId2();
    }
    if (StringUtils.equals("memberFromId3", field)) {
      return gcGrouperSyncMember.getMemberFromId3();
    }
    if (StringUtils.equals("memberToId2", field)) {
      return gcGrouperSyncMember.getMemberToId2();
    }
    if (StringUtils.equals("memberToId3", field)) {
      return gcGrouperSyncMember.getMemberToId3();
    }
    throw new RuntimeException("Not expecting memberSyncField: '" + field + "'");
  }
  
  public Object fieldTranslation(Object currentValue, Map<String, Object> elVariableMap, boolean forCreate,
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute, 
      GcGrouperSyncGroup gcGrouperSyncGroup, GcGrouperSyncMember gcGrouperSyncMember) {
    if (grouperProvisioningConfigurationAttribute == null) {
      return currentValue;
    }
    String expression = grouperProvisioningConfigurationAttribute.getTranslateExpression();
    boolean hasExpression = !StringUtils.isBlank(expression);
    String expressionCreateOnly = grouperProvisioningConfigurationAttribute.getTranslateExpressionCreateOnly();
    boolean hasExpressionCreateOnly = !StringUtils.isBlank(expressionCreateOnly);
    String expressionToUse = null;
    if (forCreate && hasExpressionCreateOnly) {
      expressionToUse = expressionCreateOnly;
    } else if (hasExpression) {
      expressionToUse = expression;
    }

    Object result = null;
    boolean translate = false;
    if (!StringUtils.isBlank(expressionToUse)) {
      result = runScript(expressionToUse, elVariableMap);
      translate = true;
    } else if (gcGrouperSyncGroup != null && !StringUtils.isBlank(grouperProvisioningConfigurationAttribute.getTranslateFromGroupSyncField())) {
      result = translateFromGroupSyncField(gcGrouperSyncGroup, 
          grouperProvisioningConfigurationAttribute.getTranslateFromGroupSyncField());
      translate = true;
    } else if (gcGrouperSyncMember != null && !StringUtils.isBlank(grouperProvisioningConfigurationAttribute.getTranslateFromMemberSyncField())) {
      result = translateFromMemberSyncField(gcGrouperSyncMember, 
          grouperProvisioningConfigurationAttribute.getTranslateFromMemberSyncField());
      translate = true;
    }
    
    if (translate) {
      return result;
    }
    return currentValue;
  }

  public void idTargetGroups(List<ProvisioningGroup> targetGroups) {

    if (GrouperUtil.isBlank(targetGroups)) {
      return;
    }

    String groupIdScript = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGroupMatchingIdExpression(); 

    String groupIdAttribute = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGroupMatchingIdAttribute();

    String groupIdField = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGroupMatchingIdField();

    if (StringUtils.isBlank(groupIdScript) && StringUtils.isBlank(groupIdAttribute) && StringUtils.isBlank(groupIdField)) {
      
      for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetGroupAttributeNameToConfig().values()) {
        if (grouperProvisioningConfigurationAttribute.isMatchingId()) {
          groupIdAttribute = grouperProvisioningConfigurationAttribute.getName();
          break;
        }
      }
      for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetGroupFieldNameToConfig().values()) {
        if (grouperProvisioningConfigurationAttribute.isMatchingId()) {
          groupIdField = grouperProvisioningConfigurationAttribute.getName();
          break;
        }
      }
      
    }
    if (StringUtils.isBlank(groupIdScript) && StringUtils.isBlank(groupIdAttribute) && StringUtils.isBlank(groupIdField)) {
      return;
    }
    
    for (ProvisioningGroup targetGroup: GrouperUtil.nonNull(targetGroups)) {
      
      Object id = null;
      if (!StringUtils.isBlank(groupIdField)) {
        if ("id".equals(groupIdField)) {
          id = targetGroup.getId();
        } else if ("idIndex".equals(groupIdField)) {
          id = targetGroup.getIdIndex();
        } else if ("name".equals(groupIdField)) {
          id = targetGroup.getName();
        } else {
          throw new RuntimeException("Invalid groupMatchingIdField, expecting id, idIndex, or name: '" + groupIdField + "'");
        }
        
      } else if (!StringUtils.isBlank(groupIdAttribute)) {
        Object idValue = targetGroup.retrieveAttributeValue(groupIdAttribute);
        if (idValue instanceof Collection) {
          throw new RuntimeException("Cant have a multivalued matching id attribute: '" + groupIdAttribute + "', " + targetGroup);
        }
        id = idValue;

      } else if (!StringUtils.isBlank(groupIdScript)) {
        Map<String, Object> elVariableMap = new HashMap<String, Object>();
        elVariableMap.put("targetGroup", targetGroup);
        
        id = runScript(groupIdScript, elVariableMap);

      } else {
        throw new RuntimeException("Must have groupMatchingIdField, groupMatchingIdAttribute, or groupMatchingIdExpression");
      }
      id = massageToString(id, 2);
      if (!GrouperUtil.isBlank(id) && targetGroup.getProvisioningGroupWrapper() != null) {
        targetGroup.getProvisioningGroupWrapper().setMatchingId(id);
      }
      targetGroup.setMatchingId(id);

    }
  }

  public void idTargetEntities(List<ProvisioningEntity> targetEntities) {

    if (GrouperUtil.isBlank(targetEntities)) {
      return;
    }

    String entityIdScript = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getEntityMatchingIdExpression(); 

    String entityIdAttribute = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getEntityMatchingIdAttribute();

    String entityIdField = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getEntityMatchingIdField();

    if (StringUtils.isBlank(entityIdScript) && StringUtils.isBlank(entityIdAttribute) && StringUtils.isBlank(entityIdField)) {
      
      for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetEntityAttributeNameToConfig().values()) {
        if (grouperProvisioningConfigurationAttribute.isMatchingId()) {
          entityIdAttribute = grouperProvisioningConfigurationAttribute.getName();
          break;
        }
      }
      for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetEntityFieldNameToConfig().values()) {
        if (grouperProvisioningConfigurationAttribute.isMatchingId()) {
          entityIdField = grouperProvisioningConfigurationAttribute.getName();
          break;
        }
      }
      
    }

    if (StringUtils.isBlank(entityIdScript) && StringUtils.isBlank(entityIdAttribute) && StringUtils.isBlank(entityIdField)) {
      return;
    }
    
    for (ProvisioningEntity targetEntity: GrouperUtil.nonNull(targetEntities)) {
      
      Object id = null;
      if (!StringUtils.isBlank(entityIdField)) {
        if ("id".equals(entityIdField)) {
          id = targetEntity.getId();
        } else if ("subjectId".equals(entityIdField)) {
          id = targetEntity.getSubjectId();
        } else if ("loginId".equals(entityIdField)) {
          id = targetEntity.getLoginId();
        } else {
          throw new RuntimeException("Invalid entityMatchingIdField, expecting id, subjectId, or loginId '" + entityIdField + "'");
        }
      } else if (!StringUtils.isBlank(entityIdAttribute)) {
        Object idValue = targetEntity.retrieveAttributeValue(entityIdAttribute);
        if (idValue instanceof Collection) {
          throw new RuntimeException("Cant have a multivalued matching id attribute: '" + entityIdAttribute + "', " + targetEntity);
        }
        id = idValue;
      } else if (!StringUtils.isBlank(entityIdScript)) {
        Map<String, Object> elVariableMap = new HashMap<String, Object>();
        elVariableMap.put("targetEntity", targetEntity);
        
        id = runScript(entityIdScript, elVariableMap);
                
      } else {
        throw new RuntimeException("Must have entityMatchingIdField, entityMatchingIdAttribute, or entityMatchingIdExpression");
      }

      id = massageToString(id, 2);
      if (!GrouperUtil.isBlank(id) && targetEntity.getProvisioningEntityWrapper() != null) {
        targetEntity.getProvisioningEntityWrapper().setMatchingId(id);
      }

      targetEntity.setMatchingId(id);

      
    }
  }

  public Object massageToString(Object id, int timeToLive) {
    if (timeToLive-- < 0) {
      throw new RuntimeException("timeToLive expired?????  why????");
    }
    if (id == null) {
      return null;
    }
    if (id instanceof String) {
      return id;
    }
    if (id instanceof Number) {
      return id.toString();
    }
    if (id instanceof MultiKey) {
      MultiKey idMultiKey = (MultiKey)id;
      Object[] newMultiKey = new Object[idMultiKey.size()];
      for (int i=0;i<newMultiKey.length;i++) {
        newMultiKey[i] = massageToString(idMultiKey.getKey(i), timeToLive);
      }
      return new MultiKey(newMultiKey);
    }
    // uh...
    throw new RuntimeException("matching ids should be string, number, or multikey of string and number! " + id.getClass() + ", " + id);
  }

  public void idTargetMemberships(List<ProvisioningMembership> targetMemberships) {

    if (GrouperUtil.isBlank(targetMemberships)) {
      return;
    }
    String membershipIdScript = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getMembershipMatchingIdExpression(); 

    String membershipIdAttribute = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getMembershipMatchingIdAttribute();

    String membershipIdField = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getMembershipMatchingIdField();

    if (StringUtils.isBlank(membershipIdScript) && StringUtils.isBlank(membershipIdAttribute) && StringUtils.isBlank(membershipIdField)) {
      
      for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetMembershipAttributeNameToConfig().values()) {
        if (grouperProvisioningConfigurationAttribute.isMatchingId()) {
          membershipIdAttribute = grouperProvisioningConfigurationAttribute.getName();
          break;
        }
      }
      for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetMembershipFieldNameToConfig().values()) {
        if (grouperProvisioningConfigurationAttribute.isMatchingId()) {
          membershipIdField = grouperProvisioningConfigurationAttribute.getName();
          break;
        }
      }
      
    }

    if (StringUtils.isBlank(membershipIdScript) && StringUtils.isBlank(membershipIdAttribute) && StringUtils.isBlank(membershipIdField)) {
      return;
    }
    
    for (ProvisioningMembership targetMembership: GrouperUtil.nonNull(targetMemberships)) {
      
      Object id = null;
      if (!StringUtils.isBlank(membershipIdField)) {
        if ("id".equals(membershipIdField)) {
          id = targetMembership.getId();
        } else if ("provisioningGroupId,provisioningMembershipId".equals(membershipIdField)) {
          id = new MultiKey(targetMembership.getProvisioningGroupId(), targetMembership.getProvisioningEntityId());
        } else {
          throw new RuntimeException("Invalid membershipMatchingIdField, expecting id or 'provisioningGroupId,provisioningMembershipId' '" + membershipIdField + "'");
        }
      } else if (!StringUtils.isBlank(membershipIdAttribute)) {
        Object idValue = targetMembership.retrieveAttributeValue(membershipIdAttribute);
        if (idValue instanceof Collection) {
          throw new RuntimeException("Cant have a multivalued matching id attribute: '" + membershipIdAttribute + "', " + targetMembership);
        }
        id = idValue;
      } else if (!StringUtils.isBlank(membershipIdScript)) {
        Map<String, Object> elVariableMap = new HashMap<String, Object>();
        elVariableMap.put("targetMembership", targetMembership);
        
        id = runScript(membershipIdScript, elVariableMap);

                
      } else {
        throw new RuntimeException("Must have membershipMatchingIdField, membershipMatchingIdAttribute, or membershipMatchingIdExpression");
      }
      id = massageToString(id, 2);
      if (!GrouperUtil.isBlank(id) && targetMembership.getProvisioningMembershipWrapper() != null) {
        targetMembership.getProvisioningMembershipWrapper().setMatchingId(id);
      }

      targetMembership.setMatchingId(id);

    }

  }

  public Object runScript(String script, Map<String, Object> elVariableMap) {
    try {
      if (!script.contains("${")) {
        script = "${" + script + "}";
      }
      return GrouperUtil.substituteExpressionLanguageScript(script, elVariableMap, true, false, false);
    } catch (RuntimeException re) {
      GrouperUtil.injectInException(re, ", script: '" + script + "', ");
      GrouperUtil.injectInException(re, GrouperUtil.toStringForLog(elVariableMap));
      throw re;
    }
  }

  public Object runExpression(String script, Map<String, Object> elVariableMap) {
    try {
      if (!script.contains("${")) {
        script = "${" + script + "}";
      }
      return GrouperUtil.substituteExpressionLanguage(script, elVariableMap, true, false, false);
    } catch (RuntimeException re) {
      GrouperUtil.injectInException(re, ", script: '" + script + "', ");
      GrouperUtil.injectInException(re, GrouperUtil.toStringForLog(elVariableMap));
      throw re;
    }
  }

  public void matchingIdTargetObjects() {
    idTargetGroups(this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveTargetProvisioningGroups());
    idTargetEntities(this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveTargetProvisioningEntities());
    idTargetMemberships(this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveTargetProvisioningMemberships());
  }
  
  public void matchingIdGrouperObjects() {
    idTargetGroups(this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperTargetGroups());
    idTargetEntities(this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperProvisioningEntities());
    idTargetMemberships(this.getGrouperProvisioner().retrieveGrouperProvisioningData().retrieveGrouperProvisioningMemberships());

  }

}