package edu.internet2.middleware.grouper.app.provisioning;

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
public class GrouperProvisioningTranslator {

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
      String entityMembershipAttribute = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityAttributeNameForMemberships();
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
      if (this.translateGrouperToTargetAutomatically) {
        grouperTargetMembership = grouperProvisioningMembership.clone();
      }
      
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
          String expressionToUse = getTargetExpressionToUse(false, grouperProvisioningConfigurationAttribute);
          if (StringUtils.isNotBlank(expressionToUse) 
              || StringUtils.isNotBlank(grouperProvisioningConfigurationAttribute.getTranslateFromGroupSyncField()) 
              || StringUtils.isNotBlank(grouperProvisioningConfigurationAttribute.getTranslateFromMemberSyncField())
              || StringUtils.isNotBlank(grouperProvisioningConfigurationAttribute.getTranslateFromGrouperProvisioningGroupField())
              || StringUtils.isNotBlank(grouperProvisioningConfigurationAttribute.getTranslateFromGrouperProvisioningEntityField())) {
            Object result = attributeTranslation( 
                grouperTargetMembership.retrieveAttributeValue(grouperProvisioningConfigurationAttribute.getName()), elVariableMap, false, 
                grouperProvisioningConfigurationAttribute, provisioningGroupWrapper, provisioningEntityWrapper);

            grouperTargetMembership.assignAttributeValue(grouperProvisioningConfigurationAttribute.getName(), result);
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateValue(grouperTargetMembership, grouperProvisioningConfigurationAttribute, null);
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().convertNullsEmpties(grouperTargetMembership, grouperProvisioningConfigurationAttribute, null);

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
                
                GrouperProvisioningConfigurationAttributeValueType valueType = grouperProvisioningConfigurationAttribute.getValueType();
                if (valueType != null) {
                  if (!valueType.correctTypeNonSet(result)) {
                    result = valueType.convert(result);
                  }
                }
                
                grouperTargetGroup.addAttributeValueForMembership(groupMembershipAttribute, result);
              }
            }
          }
        }
        
        if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes) {
          String userMembershipAttribute = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityAttributeNameForMemberships();
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
                if (!grouperProvisioningMembership.getProvisioningEntity().getProvisioningEntityWrapper().isDelete()) {
                  MultiKey validationError = this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().validFieldOrAttributeValue(grouperTargetEntity, grouperProvisioningConfigurationAttribute, result);
                  if (validationError != null) {
                    
                    errorCode = (GcGrouperSyncErrorCode)validationError.getKey(0);
                    errorMessage = (String)validationError.getKey(1);
                    this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().assignMembershipError(provisioningMembershipWrapper, errorCode, errorMessage);

                    continue;
                  }
                }
                  
                GrouperProvisioningConfigurationAttributeValueType valueType = grouperProvisioningConfigurationAttribute.getValueType();
                if (valueType != null) {
                  if (!valueType.correctTypeNonSet(result)) {
                    result = valueType.convert(result);
                  }
                }
                
                grouperTargetEntity.addAttributeValueForMembership(userMembershipAttribute, result);
              }

              }
            }
          }
        
        for (String script: scripts) {

          runScript(script, elVariableMap);
          
        }
        
        if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.membershipObjects &&
            grouperTargetMembership.isEmpty()) {
          
          grouperTargetMembership.setProvisioningEntityId(grouperTargetEntity == null ? null: grouperTargetEntity.getId());
          grouperTargetMembership.setProvisioningEntity(grouperTargetEntity);
          grouperTargetMembership.setProvisioningGroup(grouperTargetGroup);
          grouperTargetMembership.setProvisioningGroupId(grouperTargetGroup == null ? null: grouperTargetGroup.getId());
        } else {
          if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.membershipObjects) {
            if (grouperTargetMembership.getProvisioningEntity() == null) {
              grouperTargetMembership.setProvisioningEntity(grouperTargetEntity);
            }
            if (grouperTargetMembership.getProvisioningGroup() == null) {
              grouperTargetMembership.setProvisioningGroup(grouperTargetGroup);
            }
          }
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
//      if (includeDelete) {
//        grouperTargetMembership.getProvisioningMembershipWrapper().setDelete(true);
//      }

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
      String entityMembershipAttribute = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityAttributeNameForMemberships();
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
      if (this.translateGrouperToTargetAutomatically) {
        grouperTargetEntity = grouperProvisioningEntity.clone();
      }
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
            
            
            if (!grouperProvisioningConfigurationAttribute.isUpdate() && StringUtils.isNotBlank(grouperProvisioningConfigurationAttribute.getTranslateGrouperToMemberSyncField())
                && !GrouperUtil.isBlank(gcGrouperSyncMember.retrieveField(grouperProvisioningConfigurationAttribute.getTranslateGrouperToMemberSyncField()))) {
              
              Object result = gcGrouperSyncMember.retrieveField(grouperProvisioningConfigurationAttribute.getTranslateGrouperToMemberSyncField());
              String attributeOrFieldName = grouperProvisioningConfigurationAttribute.getName();
              grouperTargetEntity.assignAttributeValue(attributeOrFieldName, result);
              this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateValue(grouperTargetEntity, grouperProvisioningConfigurationAttribute, null);
              this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().convertNullsEmpties(grouperTargetEntity, grouperProvisioningConfigurationAttribute, null);
              continue;
            }
            
            
            String expressionToUse = getTargetExpressionToUse(forCreate, grouperProvisioningConfigurationAttribute);
            String staticValuesToUse = getTranslateFromStaticValuesToUse(forCreate, grouperProvisioningConfigurationAttribute);
            String grouperProvisioningEntityField = getTranslateFromGrouperProvisioningEntityField(forCreate, grouperProvisioningConfigurationAttribute);

            if (!StringUtils.isBlank(expressionToUse) || !StringUtils.isBlank(staticValuesToUse) || !StringUtils.isBlank(grouperProvisioningEntityField)
                || !StringUtils.isBlank(grouperProvisioningConfigurationAttribute.getTranslateToMemberSyncField())) {

              Object result = attributeTranslation( 
                  grouperTargetEntity.retrieveAttributeValue(grouperProvisioningConfigurationAttribute.getName()), elVariableMap, forCreate, 
                  grouperProvisioningConfigurationAttribute, null, grouperProvisioningEntity.getProvisioningEntityWrapper());
              
              if (!StringUtils.isBlank(grouperProvisioningConfigurationAttribute.getTranslateGrouperToMemberSyncField())) {
                gcGrouperSyncMember.assignField(grouperProvisioningConfigurationAttribute.getTranslateGrouperToMemberSyncField(), result);
              }
              grouperTargetEntity.assignAttributeValue(grouperProvisioningConfigurationAttribute.getName(), result);
              this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateValue(grouperTargetEntity, grouperProvisioningConfigurationAttribute, null);
              this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().convertNullsEmpties(grouperTargetEntity, grouperProvisioningConfigurationAttribute, null);

              if (required && GrouperUtil.isBlank(result) && gcGrouperSyncMember.isProvisionable()) {
                // short circuit this since other fields might need this field and its not there and invalid anyways
                this.getGrouperProvisioner().retrieveGrouperProvisioningValidation()
                .assignErrorCodeToEntityWrapper(grouperTargetEntity, grouperProvisioningConfigurationAttribute, 
                    grouperProvisioningEntity.getProvisioningEntityWrapper());
                continue PROVISIONING_ENTITY_BLOCK;
              }
            
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
      
      if (this.translateGrouperToTargetAutomatically) {
        grouperTargetGroup = grouperProvisioningGroup.clone();
      }
      
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
            
            if (!grouperProvisioningConfigurationAttribute.isUpdate() && StringUtils.isNotBlank(grouperProvisioningConfigurationAttribute.getTranslateGrouperToGroupSyncField())
                && !GrouperUtil.isBlank(gcGrouperSyncGroup.retrieveField(grouperProvisioningConfigurationAttribute.getTranslateGrouperToGroupSyncField()))) {
              
              Object result = gcGrouperSyncGroup.retrieveField(grouperProvisioningConfigurationAttribute.getTranslateGrouperToGroupSyncField());
              String attributeOrFieldName = grouperProvisioningConfigurationAttribute.getName();
              grouperTargetGroup.assignAttributeValue(attributeOrFieldName, result);
              this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateValue(grouperTargetGroup, grouperProvisioningConfigurationAttribute, null);
              this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().convertNullsEmpties(grouperTargetGroup, grouperProvisioningConfigurationAttribute, null);
              continue;
            }
            
            String expressionToUse = getTargetExpressionToUse(forCreate, grouperProvisioningConfigurationAttribute);
            String staticValuesToUse = getTranslateFromStaticValuesToUse(forCreate, grouperProvisioningConfigurationAttribute);
            String grouperProvisioningGroupField = getTranslateFromGrouperProvisioningGroupField(forCreate, grouperProvisioningConfigurationAttribute);

            if (!StringUtils.isBlank(expressionToUse) || !StringUtils.isBlank(staticValuesToUse) || !StringUtils.isBlank(grouperProvisioningGroupField)
                || !StringUtils.isBlank(grouperProvisioningConfigurationAttribute.getTranslateToGroupSyncField())) {
              Object result = attributeTranslation( 
                  grouperTargetGroup.retrieveAttributeValue(grouperProvisioningConfigurationAttribute.getName()), elVariableMap, forCreate, 
                  grouperProvisioningConfigurationAttribute, grouperProvisioningGroup.getProvisioningGroupWrapper(), null);

              if (!StringUtils.isBlank(grouperProvisioningConfigurationAttribute.getTranslateGrouperToGroupSyncField())) {
                gcGrouperSyncGroup.assignField(grouperProvisioningConfigurationAttribute.getTranslateGrouperToGroupSyncField(), result);
              }
              
              String attributeOrFieldName = grouperProvisioningConfigurationAttribute.getName();
              grouperTargetGroup.assignAttributeValue(attributeOrFieldName, result);
              this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateValue(grouperTargetGroup, grouperProvisioningConfigurationAttribute, null);
              this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().convertNullsEmpties(grouperTargetGroup, grouperProvisioningConfigurationAttribute, null);
              if (required && GrouperUtil.isBlank(result) && gcGrouperSyncGroup.isProvisionable()) {
                // short circuit this since other fields might need this field and its not there and invalid anyways
                this.getGrouperProvisioner().retrieveGrouperProvisioningValidation()
                .assignErrorCodeToGroupWrapper(grouperTargetGroup, grouperProvisioningConfigurationAttribute, 
                    grouperTargetGroup.getProvisioningGroupWrapper());
                continue PROVISIONING_GROUP_BLOCK;
              } 
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
  public Object translateFromGroupSyncField(GcGrouperSyncGroup gcGrouperSyncGroup, String field) {
    
    if (StringUtils.equals("groupId", field)) {
      return gcGrouperSyncGroup.getGroupId();
    }
    if (StringUtils.equals("groupIdIndex", field)) {
      return gcGrouperSyncGroup.getGroupIdIndex();
    }
    if (StringUtils.equals("groupExtension", field)) {
      return GrouperUtil.extensionFromName(gcGrouperSyncGroup.getGroupName());
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
   * translate from gc grouper sync entity and field name to the value
   * @param gcGrouperSyncEntity
   * @param field
   * @return the value
   */
  public Object translateFromGrouperProvisioningEntityField(ProvisioningEntity provisioningEntity, String field) {
    
    if (StringUtils.equals("id", field)) {
      return provisioningEntity.getId();
    }
    if (StringUtils.equals("email", field)) {
      return provisioningEntity.getEmail();
    }
    if (StringUtils.equals("loginid", field)) {
      return GrouperUtil.stringValue(provisioningEntity.getLoginId());
    }
    if (StringUtils.equals("name", field)) {
      return GrouperUtil.stringValue(provisioningEntity.getName());
    }
    if (StringUtils.equals("subjectId", field)) {
      return GrouperUtil.stringValue(provisioningEntity.getSubjectId());
    }
    if (StringUtils.equals("subjectSourceId", field)) {
      return GrouperUtil.stringValue(provisioningEntity.retrieveAttributeValueString("subjectSourceId"));
    }
    if (StringUtils.equals("description", field)) {
      return GrouperUtil.stringValue(provisioningEntity.retrieveAttributeValueString("description"));
    }
    if (StringUtils.equals("subjectIdentifier0", field)) {
      return GrouperUtil.stringValue(provisioningEntity.retrieveAttributeValueString("subjectIdentifier0"));
    }
    if (StringUtils.equals("subjectIdentifier1", field)) {
      return GrouperUtil.stringValue(provisioningEntity.retrieveAttributeValueString("subjectIdentifier1"));
    }
    if (StringUtils.equals("subjectIdentifier2", field)) {
      return GrouperUtil.stringValue(provisioningEntity.retrieveAttributeValueString("subjectIdentifier2"));
    }
    throw new RuntimeException("Not expecting grouperProvisioningEntityField: '" + field + "'");
  }
  

  /**
   * translate from gc grouper sync member and field name to the value
   * @param gcGrouperSyncMember
   * @param field
   * @return the value
   */
  public Object translateFromMemberSyncField(GcGrouperSyncMember gcGrouperSyncMember, String field) {
    
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
  
  public Object attributeTranslation(Object currentValue, Map<String, Object> elVariableMap, boolean forCreate,
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute, 
      ProvisioningGroupWrapper provisioningGroupWrapper, ProvisioningEntityWrapper provisioningEntityWrapper) {
    
    if (grouperProvisioningConfigurationAttribute == null) {
      return currentValue;
    }

    String expressionToUse = getTargetExpressionToUse(forCreate, grouperProvisioningConfigurationAttribute);
    String staticValuesToUse = getTranslateFromStaticValuesToUse(forCreate, grouperProvisioningConfigurationAttribute);
    String grouperProvisioningGroupField = getTranslateFromGrouperProvisioningGroupField(forCreate, grouperProvisioningConfigurationAttribute);
    String grouperProvisioningEntityField = getTranslateFromGrouperProvisioningEntityField(forCreate, grouperProvisioningConfigurationAttribute);

    Object result = null;
    boolean translate = false;
    if (!StringUtils.isBlank(expressionToUse)) {
      result = runScript(expressionToUse, elVariableMap);
      translate = true;
    } else if (!StringUtils.isBlank(staticValuesToUse)) {
      if (grouperProvisioningConfigurationAttribute.isMultiValued()) {
        result = GrouperUtil.splitTrimToSet(staticValuesToUse, ",");
      } else {
        result = staticValuesToUse;
      }
      
      translate = true;
    } else if (provisioningGroupWrapper != null && provisioningGroupWrapper.getGrouperProvisioningGroup() != null && !StringUtils.isBlank(grouperProvisioningGroupField)) {
      result = translateFromGrouperProvisioningGroupField(provisioningGroupWrapper.getGrouperProvisioningGroup(), 
          grouperProvisioningGroupField);
      translate = true;
    } else if (provisioningEntityWrapper != null && provisioningEntityWrapper.getGrouperProvisioningEntity() != null && !StringUtils.isBlank(grouperProvisioningEntityField)) {
      result = translateFromGrouperProvisioningEntityField(provisioningEntityWrapper.getGrouperProvisioningEntity(), 
          grouperProvisioningEntityField);
      translate = true;
    } else if (provisioningGroupWrapper != null && provisioningGroupWrapper.getGcGrouperSyncGroup() != null && !StringUtils.isBlank(grouperProvisioningConfigurationAttribute.getTranslateFromGroupSyncField())) {
      result = translateFromGroupSyncField(provisioningGroupWrapper.getGcGrouperSyncGroup(), 
          grouperProvisioningConfigurationAttribute.getTranslateFromGroupSyncField());
      translate = true;
    } else if (provisioningEntityWrapper != null && provisioningEntityWrapper.getGcGrouperSyncMember() != null && !StringUtils.isBlank(grouperProvisioningConfigurationAttribute.getTranslateFromMemberSyncField())) {
      result = translateFromMemberSyncField(provisioningEntityWrapper.getGcGrouperSyncMember(), 
          grouperProvisioningConfigurationAttribute.getTranslateFromMemberSyncField());
      translate = true;
    } else if (provisioningGroupWrapper != null && provisioningGroupWrapper.getGcGrouperSyncGroup() != null && !StringUtils.isBlank(grouperProvisioningConfigurationAttribute.getTranslateToGroupSyncField())) {
      result = translateFromGroupSyncField(provisioningGroupWrapper.getGcGrouperSyncGroup(), 
          grouperProvisioningConfigurationAttribute.getTranslateToGroupSyncField());
      translate = true;
    } else if (provisioningEntityWrapper != null && provisioningEntityWrapper.getGcGrouperSyncMember() != null && !StringUtils.isBlank(grouperProvisioningConfigurationAttribute.getTranslateToMemberSyncField())) {
      result = translateFromMemberSyncField(provisioningEntityWrapper.getGcGrouperSyncMember(), 
          grouperProvisioningConfigurationAttribute.getTranslateToMemberSyncField());
      translate = true;
    }
    
    if (translate) {
      if (GrouperUtil.isBlank(result) && provisioningEntityWrapper != null && provisioningEntityWrapper.isDelete() && provisioningEntityWrapper.getGcGrouperSyncMember() != null) {
        if (StringUtils.isNotBlank(grouperProvisioningConfigurationAttribute.getTranslateGrouperToMemberSyncField())) {
          result = provisioningEntityWrapper.getGcGrouperSyncMember().retrieveField(grouperProvisioningConfigurationAttribute.getTranslateGrouperToMemberSyncField());
        }
        
        if (StringUtils.isNotBlank(grouperProvisioningConfigurationAttribute.getTranslateToMemberSyncField())) {
          result = provisioningEntityWrapper.getGcGrouperSyncMember().retrieveField(grouperProvisioningConfigurationAttribute.getTranslateToMemberSyncField());
        }
        
      }
      
      if (GrouperUtil.isBlank(result) && provisioningGroupWrapper != null && provisioningGroupWrapper.isDelete() && provisioningGroupWrapper.getGcGrouperSyncGroup() != null) {
        if (StringUtils.isNotBlank(grouperProvisioningConfigurationAttribute.getTranslateGrouperToMemberSyncField())) {
          result = provisioningGroupWrapper.getGcGrouperSyncGroup().retrieveField(grouperProvisioningConfigurationAttribute.getTranslateGrouperToMemberSyncField());
        }
        
        if (StringUtils.isNotBlank(grouperProvisioningConfigurationAttribute.getTranslateToMemberSyncField())) {
          result = provisioningGroupWrapper.getGcGrouperSyncGroup().retrieveField(grouperProvisioningConfigurationAttribute.getTranslateToMemberSyncField());
        }
      }
      
      if (!StringUtils.isBlank(grouperProvisioningConfigurationAttribute.getTranslateGrouperToGroupSyncField())) {
        provisioningGroupWrapper.getGcGrouperSyncGroup().assignField(grouperProvisioningConfigurationAttribute.getTranslateGrouperToGroupSyncField(), result);
      }
      if (!StringUtils.isBlank(grouperProvisioningConfigurationAttribute.getTranslateGrouperToMemberSyncField())) {
        provisioningEntityWrapper.getGcGrouperSyncMember().assignField(grouperProvisioningConfigurationAttribute.getTranslateGrouperToMemberSyncField(), result);
      }
      
      return result;
    }
    return currentValue;
    
  }
  
  private boolean hasMatchingIdStrategyForGroups = false;
  
  
  
  public boolean isHasMatchingIdStrategyForGroups() {
    return hasMatchingIdStrategyForGroups;
  }

  private boolean hasMatchingIdStrategyForEntities = false;
  
  
  
  public boolean isHasMatchingIdStrategyForEntities() {
    return hasMatchingIdStrategyForEntities;
  }

  public void idTargetGroups(List<ProvisioningGroup> targetGroups) {

    if (GrouperUtil.isBlank(targetGroups)) {
      return;
    }

    String groupIdScript = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGroupMatchingIdExpression(); 

    String groupIdAttribute = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGroupMatchingIdAttribute();

    if (StringUtils.isBlank(groupIdScript) && StringUtils.isBlank(groupIdAttribute)) {
      
      for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetGroupAttributeNameToConfig().values()) {
        if (grouperProvisioningConfigurationAttribute.isMatchingId()) {
          groupIdAttribute = grouperProvisioningConfigurationAttribute.getName();
          break;
        }
      }
      
    }
    if (StringUtils.isBlank(groupIdScript) && StringUtils.isBlank(groupIdAttribute)) {
      return;
    }
    hasMatchingIdStrategyForGroups = true;
    for (ProvisioningGroup targetGroup: GrouperUtil.nonNull(targetGroups)) {
      
      Object id = null;
        
      if (!StringUtils.isBlank(groupIdAttribute)) {
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

    if (StringUtils.isBlank(entityIdScript) && StringUtils.isBlank(entityIdAttribute)) {
      
      for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetEntityAttributeNameToConfig().values()) {
        if (grouperProvisioningConfigurationAttribute.isMatchingId()) {
          entityIdAttribute = grouperProvisioningConfigurationAttribute.getName();
          break;
        }
      }
      
    }

    if (StringUtils.isBlank(entityIdScript) && StringUtils.isBlank(entityIdAttribute)) {
      return;
    }
    
    hasMatchingIdStrategyForEntities = true;

    for (ProvisioningEntity targetEntity: GrouperUtil.nonNull(targetEntities)) {
      
      Object id = null;
      if (!StringUtils.isBlank(entityIdAttribute)) {
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

    if (StringUtils.isBlank(membershipIdScript) && StringUtils.isBlank(membershipIdAttribute)) {
      
      for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetMembershipAttributeNameToConfig().values()) {
        if (grouperProvisioningConfigurationAttribute.isMatchingId()) {
          membershipIdAttribute = grouperProvisioningConfigurationAttribute.getName();
          break;
        }
      }
    }

    
    //this is a legacy typo
    if (StringUtils.equals(membershipIdAttribute, "provisioningGroupId,provisioningMembershipId")) {
      membershipIdAttribute = "provisioningGroupId,provisioningEntityId";
    }
    
    if (StringUtils.isBlank(membershipIdScript) && StringUtils.isBlank(membershipIdAttribute)) {
      
      if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.membershipObjects) {
        membershipIdAttribute = "provisioningGroupId,provisioningEntityId";
      } else {
        return;
      }
    }
    
    for (ProvisioningMembership targetMembership: GrouperUtil.nonNull(targetMemberships)) {
      
      Object id = null;
      if (!StringUtils.isBlank(membershipIdAttribute)) {
        if ("provisioningGroupId,provisioningEntityId".equals(membershipIdAttribute)) {
          id = new MultiKey(targetMembership.getProvisioningGroupId(), targetMembership.getProvisioningEntityId());
        } else {
          Object idValue = targetMembership.retrieveAttributeValue(membershipIdAttribute);
          if (idValue instanceof Collection) {
            throw new RuntimeException("Cant have a multivalued matching id attribute: '" + membershipIdAttribute + "', " + targetMembership);
          }
          id = idValue;
        }
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
  
  /**
   * translate from gc grouper sync group and field name to the value
   * @param gcGrouperSyncGroup
   * @param field
   * @return the value
   */
  public Object translateFromGrouperProvisioningGroupField(ProvisioningGroup provisioningGroup, String field) {
    
    if (StringUtils.equals("id", field)) {
      return provisioningGroup.getId();
    }
    if (StringUtils.equals("idIndex", field)) {
      return provisioningGroup.getIdIndex();
    }
    if (StringUtils.equals("idIndexString", field)) {
      return GrouperUtil.stringValue(provisioningGroup.getIdIndex());
    }
    if (StringUtils.equals("displayExtension", field)) {
      return GrouperUtil.stringValue(provisioningGroup.getDisplayExtension());
    }
    if (StringUtils.equals("displayName", field)) {
      return GrouperUtil.stringValue(provisioningGroup.getDisplayName());
    }
    if (StringUtils.equals("extension", field)) {
      return GrouperUtil.stringValue(provisioningGroup.getExtension());
    }
    if (StringUtils.equals("name", field)) {
      return GrouperUtil.stringValue(provisioningGroup.getName());
    }
    if (StringUtils.equals("description", field)) {
      return GrouperUtil.stringValue(provisioningGroup.retrieveAttributeValueString("description"));
    }
    throw new RuntimeException("Not expecting grouperProvisioningGroupField: '" + field + "'");
  }

  private boolean translateGrouperToTargetAutomatically;
  
  public void setTranslateGrouperToTargetAutomatically(boolean translateGrouperToTargetAutomatically) {
    this.translateGrouperToTargetAutomatically = translateGrouperToTargetAutomatically;
  }

  
  public boolean isTranslateGrouperToTargetAutomatically() {
    return translateGrouperToTargetAutomatically;
  }

  private String getTranslateFromGrouperProvisioningGroupField(boolean forCreate, GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute) {
    String expression = grouperProvisioningConfigurationAttribute.getTranslateFromGrouperProvisioningGroupField();
    boolean hasExpression = !StringUtils.isBlank(expression);
    String expressionCreateOnly = grouperProvisioningConfigurationAttribute.getTranslateFromGrouperProvisioningGroupFieldCreateOnly();
    boolean hasExpressionCreateOnly = !StringUtils.isBlank(expressionCreateOnly);
    String expressionToUse = null;
    if (forCreate && hasExpressionCreateOnly) {
      expressionToUse = expressionCreateOnly;
    } else if (hasExpression) {
      expressionToUse = expression;
    }

    return expressionToUse;
  }

  private String getTranslateFromGrouperProvisioningEntityField(boolean forCreate, GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute) {
    String expression = grouperProvisioningConfigurationAttribute.getTranslateFromGrouperProvisioningEntityField();
    boolean hasExpression = !StringUtils.isBlank(expression);
    String expressionCreateOnly = grouperProvisioningConfigurationAttribute.getTranslateFromGrouperProvisioningEntityFieldCreateOnly();
    boolean hasExpressionCreateOnly = !StringUtils.isBlank(expressionCreateOnly);
    String expressionToUse = null;
    if (forCreate && hasExpressionCreateOnly) {
      expressionToUse = expressionCreateOnly;
    } else if (hasExpression) {
      expressionToUse = expression;
    }

    return expressionToUse;
  }
  

  private String getTargetExpressionToUse(boolean forCreate, GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute) {
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

    return expressionToUse;
  }
  
  private String getTranslateFromStaticValuesToUse(boolean forCreate, GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute) {
    String staticValues = grouperProvisioningConfigurationAttribute.getTranslateFromStaticValues();
    boolean hasStaticValues = !StringUtils.isBlank(staticValues);
    String staticValuesCreateOnly = grouperProvisioningConfigurationAttribute.getTranslateFromStaticValuesCreateOnly();
    boolean hasStaticValuesCreateOnly = !StringUtils.isBlank(staticValuesCreateOnly);
    String staticValuesToUse = null;
    if (forCreate && hasStaticValuesCreateOnly) {
      staticValuesToUse = staticValuesCreateOnly;
    } else if (hasStaticValues) {
      staticValuesToUse = staticValues;
    }

    return staticValuesToUse;
  }
}