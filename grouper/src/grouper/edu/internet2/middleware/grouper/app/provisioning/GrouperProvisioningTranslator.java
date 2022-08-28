package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

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
    
    Collection<Object> changedMemberships = new HashSet<Object>();

    int invalidMembershipsDuringTranslation = 0;
    
    // clear out the membership attribute, it might have a default value in there
    if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes) {
      String groupMembershipAttribute = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupMembershipAttributeName();
      if (!StringUtils.isBlank(groupMembershipAttribute)) {
        for (ProvisioningGroup provisioningGroup : this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetGroups()) {
          provisioningGroup.clearAttribute(groupMembershipAttribute);
        }
      }
    }
    if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes) {
      String entityMembershipAttribute = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityMembershipAttributeName();
      if (!StringUtils.isBlank(entityMembershipAttribute)) {
        for (ProvisioningEntity provisioningEntity : this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetEntities()) {
          provisioningEntity.clearAttribute(entityMembershipAttribute);
        }
      }
    }
    
    List<ProvisioningMembership> grouperTargetMemberships = new ArrayList<ProvisioningMembership>();
    List<ProvisioningMembership> grouperTargetMembershipsTranslated = new ArrayList<ProvisioningMembership>();

    List<String> scripts = GrouperUtil.nonNull(GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGrouperProvisioningToTargetTranslation()).get("Membership"));

    for (ProvisioningMembership grouperProvisioningMembership: GrouperUtil.nonNull(grouperProvisioningMemberships)) {

      ProvisioningMembershipWrapper provisioningMembershipWrapper = grouperProvisioningMembership.getProvisioningMembershipWrapper();
      
      GcGrouperSyncMembership gcGrouperSyncMembership = provisioningMembershipWrapper.getGcGrouperSyncMembership();

      ProvisioningGroupWrapper provisioningGroupWrapper = grouperProvisioningMembership.getProvisioningGroup().getProvisioningGroupWrapper();
      ProvisioningGroup grouperTargetGroup = provisioningGroupWrapper.getGrouperTargetGroup();

      GcGrouperSyncErrorCode errorCode = provisioningGroupWrapper.getErrorCode();
      String errorMessage = provisioningGroupWrapper.getGcGrouperSyncGroup().getErrorMessage();
      if (errorCode == null && !StringUtils.isBlank(errorMessage)) {
        errorCode = GcGrouperSyncErrorCode.ERR;
      }
          
      ProvisioningEntityWrapper provisioningEntityWrapper = grouperProvisioningMembership.getProvisioningEntity().getProvisioningEntityWrapper();
      ProvisioningEntity grouperTargetEntity = provisioningEntityWrapper.getGrouperTargetEntity();

      if (errorCode == null) {
        errorCode = provisioningEntityWrapper.getErrorCode();
        errorMessage = provisioningEntityWrapper.getGcGrouperSyncMember().getErrorMessage();
        if (errorCode == null && !StringUtils.isBlank(errorMessage)) {
          errorCode = GcGrouperSyncErrorCode.ERR;
        }
      }
      if (errorCode != null) {
        this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().assignMembershipError(provisioningMembershipWrapper, errorCode, errorMessage);
        continue;
      }


      ProvisioningMembership grouperTargetMembership = new ProvisioningMembership();
      if (this.translateGrouperToTargetAutomatically) {
        grouperTargetMembership = grouperProvisioningMembership.clone();
      }
      if (provisioningMembershipWrapper.getGrouperTargetMembership() == null) {
        grouperTargetMembershipsTranslated.add(grouperTargetMembership);
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
          String expressionToUse = getTargetExpressionToUse(!gcGrouperSyncMembership.isInTarget(), grouperProvisioningConfigurationAttribute);
          if (StringUtils.isNotBlank(expressionToUse) 
              || StringUtils.isNotBlank(grouperProvisioningConfigurationAttribute.getTranslateFromGrouperProvisioningGroupField())
              || StringUtils.isNotBlank(grouperProvisioningConfigurationAttribute.getTranslateFromGrouperProvisioningEntityField())) {
            Object result = attributeTranslation( 
                grouperTargetMembership.retrieveAttributeValue(grouperProvisioningConfigurationAttribute.getName()), elVariableMap, !gcGrouperSyncMembership.isInTarget(), 
                grouperProvisioningConfigurationAttribute, provisioningGroupWrapper, provisioningEntityWrapper);

            grouperTargetMembership.assignAttributeValue(grouperProvisioningConfigurationAttribute.getName(), result);
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateValue(changedMemberships, grouperTargetMembership, grouperProvisioningConfigurationAttribute, null);
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().convertNullsEmpties(changedMemberships, grouperTargetMembership, grouperProvisioningConfigurationAttribute, null);

          }
        }
        
        // if the group is missing, has an invalid attribute don't bother setting the membership
        if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes 
            && provisioningGroupWrapper.getGrouperTargetGroup() != null) {
          String groupMembershipAttribute = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupMembershipAttributeName();
          if (!StringUtils.isEmpty(groupMembershipAttribute)) {
            GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetGroupAttributeNameToConfig().get(groupMembershipAttribute);
            if (grouperProvisioningConfigurationAttribute != null) {
              
              Object result = null;
              
              if (!StringUtils.isBlank(this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupMembershipAttributeValue())) {
                result = translateFromGrouperProvisioningEntityField(provisioningEntityWrapper, 
                    this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupMembershipAttributeValue());
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
        
        // if the entity is missing, has an invalid attribute don't bother setting the membership
        if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes
            && provisioningEntityWrapper.getGrouperTargetEntity() != null) {
          String userMembershipAttribute = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityMembershipAttributeName();
          if (!StringUtils.isEmpty(userMembershipAttribute)) {
            GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetEntityAttributeNameToConfig().get(userMembershipAttribute);

            if (grouperProvisioningConfigurationAttribute != null) {
              
              Object result = null;
              
              if (!StringUtils.isBlank(this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityMembershipAttributeValue())) {
                result = translateFromGrouperProvisioningGroupField(provisioningGroupWrapper, 
                    this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityMembershipAttributeValue());
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
      String groupMembershipAttribute = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupMembershipAttributeName();
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetGroupAttributeNameToConfig().get(groupMembershipAttribute);

      if (!StringUtils.isBlank(groupMembershipAttribute) && grouperProvisioningConfigurationAttribute != null && !StringUtils.isBlank(grouperProvisioningConfigurationAttribute.getDefaultValue())) {
        this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().assignDefaultsForGroups(this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetGroups(), grouperProvisioningConfigurationAttribute);
      }
    }
    if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes) {
      String entityMembershipAttribute = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityMembershipAttributeName();
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetEntityAttributeNameToConfig().get(entityMembershipAttribute);

      if (!StringUtils.isBlank(entityMembershipAttribute) && grouperProvisioningConfigurationAttribute != null && !StringUtils.isBlank(grouperProvisioningConfigurationAttribute.getDefaultValue())) {
        this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().assignDefaultsForEntities(this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetEntities(), grouperProvisioningConfigurationAttribute);
      }
    }
    
    if (invalidMembershipsDuringTranslation > 0) {
      GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "invalidMembershipsDuringTranslation", invalidMembershipsDuringTranslation);
    }
    if (GrouperUtil.length(grouperTargetMembershipsTranslated) > 0) {
      this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.translateGrouperMembershipsToTarget, grouperTargetMembershipsTranslated);
    }


    return grouperTargetMemberships;
  }

  public List<ProvisioningEntity> translateGrouperToTargetEntities(
      List<ProvisioningEntity> grouperProvisioningEntities, boolean includeDelete, boolean forCreate) {
    
    Collection<Object> changedEntities = new HashSet<Object>();
    List<ProvisioningEntity> grouperTargetEntities = new ArrayList<ProvisioningEntity>();

    List<String> scripts = GrouperUtil.nonNull(GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGrouperProvisioningToTargetTranslation()).get("Entity"));
    
    if (forCreate) {
      scripts.addAll(GrouperUtil.nonNull(GrouperUtil.nonNull(
          this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGrouperProvisioningToTargetTranslation()).get("EntityCreateOnly")));
    }

    List<ProvisioningEntity> grouperTargetEntitiesTranslated = new ArrayList<ProvisioningEntity>();

    PROVISIONING_ENTITY_BLOCK: for (ProvisioningEntity grouperProvisioningEntity: GrouperUtil.nonNull(grouperProvisioningEntities)) {
      
      ProvisioningEntity grouperTargetEntity = new ProvisioningEntity();
      if (this.translateGrouperToTargetAutomatically) {
        grouperTargetEntity = grouperProvisioningEntity.clone();
      }
      ProvisioningEntityWrapper provisioningEntityWrapper = grouperProvisioningEntity.getProvisioningEntityWrapper();
      
      if (provisioningEntityWrapper.getGrouperTargetEntity() == null) {
        grouperTargetEntitiesTranslated.add(grouperTargetEntity);
      }

      grouperTargetEntity.setProvisioningEntityWrapper(provisioningEntityWrapper);

      Map<String, Object> elVariableMap = new HashMap<String, Object>();
      elVariableMap.put("grouperProvisioningEntity", grouperProvisioningEntity);
      elVariableMap.put("provisioningEntityWrapper", provisioningEntityWrapper);
      GcGrouperSyncMember gcGrouperSyncMember = provisioningEntityWrapper.getGcGrouperSyncMember();
      elVariableMap.put("gcGrouperSyncMember", gcGrouperSyncMember);
      elVariableMap.put("grouperTargetEntity", grouperTargetEntity);

      // do the required's first
      for (boolean required : new boolean[] {true, false}) {
        // attribute translations
        for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : entityTargetAttributesInTranslationOrder()) {
          if (grouperProvisioningConfigurationAttribute.isRequired() == required) {
            
            if (!grouperProvisioningConfigurationAttribute.isUpdate() && StringUtils.isNotBlank(grouperProvisioningConfigurationAttribute.getTranslateFromGrouperProvisioningEntityField())
                && !GrouperUtil.isBlank(translateFromGrouperProvisioningEntityField(provisioningEntityWrapper, grouperProvisioningConfigurationAttribute.getTranslateFromGrouperProvisioningEntityField()))) {
              
              Object result = translateFromGrouperProvisioningEntityField(provisioningEntityWrapper, grouperProvisioningConfigurationAttribute.getTranslateFromGrouperProvisioningEntityField());
              String attributeOrFieldName = grouperProvisioningConfigurationAttribute.getName();
              grouperTargetEntity.assignAttributeValue(attributeOrFieldName, result);
              this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateValue(changedEntities, grouperTargetEntity, grouperProvisioningConfigurationAttribute, null);
              this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().convertNullsEmpties(changedEntities, grouperTargetEntity, grouperProvisioningConfigurationAttribute, null);
              continue;
            }
            
            
            String expressionToUse = getTargetExpressionToUse(forCreate, grouperProvisioningConfigurationAttribute);
            String staticValuesToUse = getTranslateFromStaticValuesToUse(forCreate, grouperProvisioningConfigurationAttribute);
            String grouperProvisioningEntityField = getTranslateFromGrouperProvisioningEntityField(forCreate, grouperProvisioningConfigurationAttribute);

            if (!StringUtils.isBlank(expressionToUse) || !StringUtils.isBlank(staticValuesToUse) || !StringUtils.isBlank(grouperProvisioningEntityField)
                || this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isEntityAttributeNameHasCache(grouperProvisioningConfigurationAttribute.getName())) {

              Object result = attributeTranslation( 
                  grouperTargetEntity.retrieveAttributeValue(grouperProvisioningConfigurationAttribute.getName()), elVariableMap, forCreate, 
                  grouperProvisioningConfigurationAttribute, null, provisioningEntityWrapper);
              
              if (grouperProvisioningConfigurationAttribute.getSyncMemberCacheAttribute() != null
                  && grouperProvisioningConfigurationAttribute.getSyncMemberCacheAttribute().getSource() == GrouperProvisioningConfigurationAttributeDbCacheSource.grouper) {
                gcGrouperSyncMember.assignField(grouperProvisioningConfigurationAttribute.getSyncMemberCacheAttribute().getCacheName(), result);
              }
              grouperTargetEntity.assignAttributeValue(grouperProvisioningConfigurationAttribute.getName(), result);
              this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateValue(changedEntities, grouperTargetEntity, grouperProvisioningConfigurationAttribute, null);
              this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().convertNullsEmpties(changedEntities, grouperTargetEntity, grouperProvisioningConfigurationAttribute, null);

              if (required && GrouperUtil.isBlank(result) && gcGrouperSyncMember.isProvisionable()) {
                // short circuit this since other fields might need this field and its not there and invalid anyways
                this.getGrouperProvisioner().retrieveGrouperProvisioningValidation()
                .assignErrorCodeToEntityWrapper(grouperTargetEntity, grouperProvisioningConfigurationAttribute, 
                    provisioningEntityWrapper);
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
      
      provisioningEntityWrapper.setGrouperTargetEntity(grouperTargetEntity);
      if (includeDelete) {
        provisioningEntityWrapper.setDelete(true);
      } else if (forCreate) {
        provisioningEntityWrapper.setCreate(true);
      }

    }
    if (GrouperUtil.length(grouperTargetEntitiesTranslated) > 0) {
      this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.translateGrouperEntitiesToTarget, grouperTargetEntitiesTranslated);
    }

    return grouperTargetEntities;
  }

  public Collection<GrouperProvisioningConfigurationAttribute> entityTargetAttributesInTranslationOrder() {
    return this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetEntityAttributeNameToConfig().values();
  }

  public List<ProvisioningGroup> translateGrouperToTargetGroups(List<ProvisioningGroup> grouperProvisioningGroups, boolean includeDelete, boolean forCreate) {

    Collection<Object> changedEntities = new HashSet<Object>();

    List<String> scripts = GrouperUtil.nonNull(GrouperUtil.nonNull(
        this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGrouperProvisioningToTargetTranslation()).get("Group"));

    if (forCreate) {
      scripts.addAll(GrouperUtil.nonNull(GrouperUtil.nonNull(
          this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGrouperProvisioningToTargetTranslation()).get("GroupCreateOnly")));
    }
    List<ProvisioningGroup> grouperTargetGroups = new ArrayList<ProvisioningGroup>();
    List<ProvisioningGroup> grouperTargetGroupsTranslated = new ArrayList<ProvisioningGroup>();

    PROVISIONING_GROUP_BLOCK: for (ProvisioningGroup grouperProvisioningGroup: GrouperUtil.nonNull(grouperProvisioningGroups)) {
      
      ProvisioningGroup grouperTargetGroup = new ProvisioningGroup();
      
      if (this.translateGrouperToTargetAutomatically) {
        grouperTargetGroup = grouperProvisioningGroup.clone();
      }
      
      ProvisioningGroupWrapper provisioningGroupWrapper = grouperProvisioningGroup.getProvisioningGroupWrapper();
      
      if (provisioningGroupWrapper.getGrouperTargetGroup() == null) {
        grouperTargetGroupsTranslated.add(grouperTargetGroup);
      }
      
      grouperTargetGroup.setProvisioningGroupWrapper(provisioningGroupWrapper);

      Map<String, Object> elVariableMap = new HashMap<String, Object>();
      elVariableMap.put("grouperProvisioningGroup", grouperProvisioningGroup);
      elVariableMap.put("provisioningGroupWrapper", provisioningGroupWrapper);
      elVariableMap.put("grouperTargetGroup", grouperTargetGroup);
      GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper.getGcGrouperSyncGroup();
      elVariableMap.put("gcGrouperSyncGroup", gcGrouperSyncGroup);

      // do the required's first
      for (boolean required : new boolean[] {true, false}) {
        // attribute translations
        for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : groupAttributesInTranslationOrder()) {
          if (grouperProvisioningConfigurationAttribute.isRequired() == required) {
            
            if (!grouperProvisioningConfigurationAttribute.isUpdate() && StringUtils.isNotBlank(grouperProvisioningConfigurationAttribute.getTranslateFromGrouperProvisioningGroupField())
                && !GrouperUtil.isBlank(translateFromGrouperProvisioningGroupField(provisioningGroupWrapper, grouperProvisioningConfigurationAttribute.getTranslateFromGrouperProvisioningGroupField()))) {
              
              Object result = translateFromGrouperProvisioningGroupField(provisioningGroupWrapper, grouperProvisioningConfigurationAttribute.getTranslateFromGrouperProvisioningGroupField());
              String attributeOrFieldName = grouperProvisioningConfigurationAttribute.getName();
              grouperTargetGroup.assignAttributeValue(attributeOrFieldName, result);
              this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateValue(changedEntities, grouperTargetGroup, grouperProvisioningConfigurationAttribute, null);
              this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().convertNullsEmpties(changedEntities, grouperTargetGroup, grouperProvisioningConfigurationAttribute, null);
              continue;
            }
            
            String expressionToUse = getTargetExpressionToUse(forCreate, grouperProvisioningConfigurationAttribute);
            String staticValuesToUse = getTranslateFromStaticValuesToUse(forCreate, grouperProvisioningConfigurationAttribute);
            String grouperProvisioningGroupField = getTranslateFromGrouperProvisioningGroupField(forCreate, grouperProvisioningConfigurationAttribute);

            if (!StringUtils.isBlank(expressionToUse) || !StringUtils.isBlank(staticValuesToUse) || !StringUtils.isBlank(grouperProvisioningGroupField)
                || this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isGroupAttributeNameHasCache(grouperProvisioningConfigurationAttribute.getName())) { 
              Object result = attributeTranslation( 
                  grouperTargetGroup.retrieveAttributeValue(grouperProvisioningConfigurationAttribute.getName()), elVariableMap, forCreate, 
                  grouperProvisioningConfigurationAttribute, provisioningGroupWrapper, null);

              if (grouperProvisioningConfigurationAttribute.getSyncGroupCacheAttribute() != null
                  && grouperProvisioningConfigurationAttribute.getSyncGroupCacheAttribute().getSource() == GrouperProvisioningConfigurationAttributeDbCacheSource.grouper) {
                gcGrouperSyncGroup.assignField(grouperProvisioningConfigurationAttribute.getSyncGroupCacheAttribute().getCacheName(), result);
              }
              String attributeOrFieldName = grouperProvisioningConfigurationAttribute.getName();
              grouperTargetGroup.assignAttributeValue(attributeOrFieldName, result);
              this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateValue(changedEntities, grouperTargetGroup, grouperProvisioningConfigurationAttribute, null);
              this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().convertNullsEmpties(changedEntities, grouperTargetGroup, grouperProvisioningConfigurationAttribute, null);
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

      provisioningGroupWrapper.setGrouperTargetGroup(grouperTargetGroup);
      if (includeDelete) {
        provisioningGroupWrapper.setDelete(true);
      } else if (forCreate) {
        provisioningGroupWrapper.setCreate(true);
      }
      grouperTargetGroups.add(grouperTargetGroup);
        
    }
    if (GrouperUtil.length(grouperTargetGroupsTranslated) > 0) {
      this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.translateGrouperGroupsToTarget, grouperTargetGroupsTranslated);
    }

    return grouperTargetGroups;
  }

  public Collection<GrouperProvisioningConfigurationAttribute> groupAttributesInTranslationOrder() {
    return this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetGroupAttributeNameToConfig().values();
  }

  /**
   * translate from gc grouper sync entity and field name to the value
   * @param provisioningEntityWrapper
   * @param field
   * @return the value
   */
  public Object translateFromGrouperProvisioningEntityField(ProvisioningEntityWrapper provisioningEntityWrapper, String field) {
    
    // "id", "email", "loginid", "memberId", "entityAttributeValueCache0", "entityAttributeValueCache1", "entityAttributeValueCache2", "entityAttributeValueCache3", "name", "subjectId", "subjectSourceId", "description", "subjectIdentifier0", "subjectIdentifier1", "subjectIdentifier2"
    if (provisioningEntityWrapper == null) { 
      return null;
    }
    
    ProvisioningEntity provisioningEntity = provisioningEntityWrapper.getGrouperProvisioningEntity();
    
    if (provisioningEntity != null) {

      if (StringUtils.equals("id", field) && !StringUtils.isBlank(provisioningEntity.getId())) {
        return provisioningEntity.getId();
      }
      if (StringUtils.equals("email", field)) {
        return provisioningEntity.getEmail();
      }
      if (StringUtils.equals("loginid", field)) {
        return provisioningEntity.getLoginId();
      }
      if (StringUtils.equals("name", field)) {
        return provisioningEntity.getName();
      }
      if (StringUtils.equals("subjectId", field) && !StringUtils.isBlank(provisioningEntity.getSubjectId())) {
        return provisioningEntity.getSubjectId();
      }
      if (StringUtils.equals("subjectSourceId", field) && !StringUtils.isBlank(provisioningEntity.getSubjectSourceId())) {
        return provisioningEntity.getSubjectSourceId();
      }
      if (StringUtils.equals("description", field)) {
        return provisioningEntity.getDescription();
      }
      if (StringUtils.equals("subjectIdentifier0", field) && !StringUtils.isBlank(provisioningEntity.getSubjectIdentifier0())) {
        return provisioningEntity.getSubjectIdentifier0();
      }
      if (StringUtils.equals("subjectIdentifier1", field)) {
        return provisioningEntity.getSubjectIdentifier1();
      }
      if (StringUtils.equals("subjectIdentifier2", field)) {
        return provisioningEntity.getSubjectIdentifier2();
      }
    }
    
    GcGrouperSyncMember gcGrouperSyncMember = provisioningEntityWrapper.getGcGrouperSyncMember();
    
    if (gcGrouperSyncMember != null) {
      if (StringUtils.equals("id", field)) {
        return gcGrouperSyncMember.getId();
      }
      if (StringUtils.equals("subjectId", field)) {
        return gcGrouperSyncMember.getSubjectId();
      }
      if (StringUtils.equals("subjectSourceId", field)) {
        return gcGrouperSyncMember.getSourceId();
      }
      if (StringUtils.equals("subjectIdentifier", field)) {
        return gcGrouperSyncMember.getSubjectIdentifier();
      }
      if (StringUtils.equals("entityAttributeValueCache0", field)) {
        String cacheValue = gcGrouperSyncMember.getEntityAttributeValueCache0();
        if (GrouperUtil.isBlank(cacheValue) || !cacheValue.contains("{")) {
          return cacheValue;
        }
        GrouperProvisioningConfigurationAttributeDbCache cache = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityAttributeDbCaches()[0];
        if (cache == null || cache.getType() != GrouperProvisioningConfigurationAttributeDbCacheType.object) {
          return cacheValue;
        }
        return this.grouperProvisioner.retrieveGrouperProvisioningData().parseJsonCacheEntity(cacheValue);
      }
      if (StringUtils.equals("entityAttributeValueCache1", field)) {
        String cacheValue = gcGrouperSyncMember.getEntityAttributeValueCache1();
        if (GrouperUtil.isBlank(cacheValue) || !cacheValue.contains("{")) {
          return cacheValue;
        }
        GrouperProvisioningConfigurationAttributeDbCache cache = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityAttributeDbCaches()[1];
        if (cache == null || cache.getType() != GrouperProvisioningConfigurationAttributeDbCacheType.object) {
          return cacheValue;
        }
        return this.grouperProvisioner.retrieveGrouperProvisioningData().parseJsonCacheEntity(cacheValue);
      }
      if (StringUtils.equals("entityAttributeValueCache2", field)) {
        String cacheValue = gcGrouperSyncMember.getEntityAttributeValueCache2();
        if (GrouperUtil.isBlank(cacheValue) || !cacheValue.contains("{")) {
          return cacheValue;
        }
        GrouperProvisioningConfigurationAttributeDbCache cache = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityAttributeDbCaches()[2];
        if (cache == null || cache.getType() != GrouperProvisioningConfigurationAttributeDbCacheType.object) {
          return cacheValue;
        }
        return this.grouperProvisioner.retrieveGrouperProvisioningData().parseJsonCacheEntity(cacheValue);
      }
      if (StringUtils.equals("entityAttributeValueCache3", field)) {
        String cacheValue = gcGrouperSyncMember.getEntityAttributeValueCache3();
        if (GrouperUtil.isBlank(cacheValue) || !cacheValue.contains("{")) {
          return cacheValue;
        }
        GrouperProvisioningConfigurationAttributeDbCache cache = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityAttributeDbCaches()[3];
        if (cache == null || cache.getType() != GrouperProvisioningConfigurationAttributeDbCacheType.object) {
          return cacheValue;
        }
        return this.grouperProvisioner.retrieveGrouperProvisioningData().parseJsonCacheEntity(cacheValue);
      }
    }

    //if we couldnt find the data but the field was ok, its just null
    if (StringUtils.equalsAny(field, "id", "email", "loginid", "memberId", "entityAttributeValueCache0", 
        "entityAttributeValueCache1", "entityAttributeValueCache2", "entityAttributeValueCache3", "name", 
        "subjectId", "subjectSourceId", "description", "subjectIdentifier", "subjectIdentifier0", "subjectIdentifier1", "subjectIdentifier2")) {
      return null;
    }
    
    throw new RuntimeException("Not expecting grouperProvisioningEntityField: '" + field + "'");
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
      result = translateFromGrouperProvisioningGroupField(provisioningGroupWrapper, 
          grouperProvisioningGroupField);
      translate = true;
    } else if (provisioningEntityWrapper != null && provisioningEntityWrapper.getGrouperProvisioningEntity() != null && !StringUtils.isBlank(grouperProvisioningEntityField)) {
      result = translateFromGrouperProvisioningEntityField(provisioningEntityWrapper, 
          grouperProvisioningEntityField);
      translate = true;
    } else {
      if (provisioningGroupWrapper != null && provisioningGroupWrapper.getGcGrouperSyncGroup() != null 
          && grouperProvisioningConfigurationAttribute.getGrouperProvisioningConfigurationAttributeType() == GrouperProvisioningConfigurationAttributeType.group) {
        // look for grouper source first, then target
        for (GrouperProvisioningConfigurationAttributeDbCache groupCache : this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupAttributeDbCaches()) {
          if (groupCache != null && StringUtils.equals(groupCache.getAttributeName(), grouperProvisioningConfigurationAttribute.getName()) 
              && groupCache.getType() == GrouperProvisioningConfigurationAttributeDbCacheType.attribute) {
            result = translateFromGrouperProvisioningGroupField(provisioningGroupWrapper, 
                "groupAttributeValueCache" + groupCache.getIndex());
            translate = true;
            break;
          }
        }
      }
      if (provisioningEntityWrapper != null && provisioningEntityWrapper.getGcGrouperSyncMember() != null
          && grouperProvisioningConfigurationAttribute.getGrouperProvisioningConfigurationAttributeType() == GrouperProvisioningConfigurationAttributeType.entity) {
        // look for grouper source first, then target
        for (GrouperProvisioningConfigurationAttributeDbCache entityCache : this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityAttributeDbCaches()) {
          if (entityCache != null && StringUtils.equals(entityCache.getAttributeName(), grouperProvisioningConfigurationAttribute.getName()) 
              && entityCache.getType() == GrouperProvisioningConfigurationAttributeDbCacheType.attribute) {
            result = translateFromGrouperProvisioningEntityField(provisioningEntityWrapper, 
                "entityAttributeValueCache" + entityCache.getIndex());
            translate = true;
            break;
          }
        }
      }
    }
    
    // TODO still translate if in cache, maybe have both values?
    if (translate) {
      if (GrouperUtil.isBlank(result) && provisioningEntityWrapper != null && provisioningEntityWrapper.isDelete() 
          && provisioningEntityWrapper.getGcGrouperSyncMember() != null
          && grouperProvisioningConfigurationAttribute.getGrouperProvisioningConfigurationAttributeType() == GrouperProvisioningConfigurationAttributeType.entity) {

        for (GrouperProvisioningConfigurationAttributeDbCache entityCache : this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityAttributeDbCaches()) {
          if (entityCache != null && StringUtils.equals(entityCache.getAttributeName(), grouperProvisioningConfigurationAttribute.getName()) 
              && entityCache.getType() == GrouperProvisioningConfigurationAttributeDbCacheType.attribute) {
            result = translateFromGrouperProvisioningEntityField(provisioningEntityWrapper, 
                "entityAttributeValueCache" + entityCache.getIndex());
            break;
          }
        }
        
      }
      
      if (GrouperUtil.isBlank(result) && provisioningGroupWrapper != null && provisioningGroupWrapper.isDelete() 
          && provisioningGroupWrapper.getGcGrouperSyncGroup() != null
          && grouperProvisioningConfigurationAttribute.getGrouperProvisioningConfigurationAttributeType() == GrouperProvisioningConfigurationAttributeType.group) {
        for (GrouperProvisioningConfigurationAttributeDbCache groupCache : this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupAttributeDbCaches()) {
          if (groupCache != null && StringUtils.equals(groupCache.getAttributeName(), grouperProvisioningConfigurationAttribute.getName()) 
              && groupCache.getType() == GrouperProvisioningConfigurationAttributeDbCacheType.attribute) {
            result = translateFromGrouperProvisioningGroupField(provisioningGroupWrapper, 
                "groupAttributeValueCache" + groupCache.getIndex());
            break;
          }
        }
      }
      
      return result;
    }
    return currentValue;
    
  }
  
  /**
   * get the matching and search ids for a target group (could be grouperTargetGroup or targetProvisioningGroup)
   * @param targetGroups
   */
  public void idTargetGroups(List<ProvisioningGroup> targetGroups) {

    if (GrouperUtil.isBlank(targetGroups)) {
      return;
    }

    for (ProvisioningGroup targetGroup: GrouperUtil.nonNull(targetGroups)) {
      
      Set<ProvisioningUpdatableAttributeAndValue> provisioningUpdatableAttributeAndValues = new LinkedHashSet<ProvisioningUpdatableAttributeAndValue>();
      targetGroup.setMatchingIdAttributeNameToValues(provisioningUpdatableAttributeAndValues);

      if (this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isGroupMatchingAttributeSameAsSearchAttribute()) {
        targetGroup.setSearchIdAttributeNameToValues(provisioningUpdatableAttributeAndValues);
      }

      // first do all current values, then do all past values
      for (GrouperProvisioningConfigurationAttribute matchingAttribute : this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGroupMatchingAttributes()) {
        String matchingAttributeName = matchingAttribute.getName();
        
        // dont worry if dupes... oh well
        Object targetCurrentValue = massageToString(targetGroup.retrieveAttributeValue(matchingAttributeName), 2);

        if(!GrouperUtil.isBlank(targetCurrentValue)) {
          
          ProvisioningUpdatableAttributeAndValue provisioningUpdatableAttributeAndValue = new ProvisioningUpdatableAttributeAndValue(matchingAttributeName, targetCurrentValue);
          provisioningUpdatableAttributeAndValue.setCurrentValue(true);
          provisioningUpdatableAttributeAndValues.add(provisioningUpdatableAttributeAndValue);
        }
      }
      
      // dont get old values for target side objects
      if (!targetGroup.isGrouperTargetObject()) {
        continue;
      }
      
      //old values
      for (GrouperProvisioningConfigurationAttribute matchingAttribute : this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGroupMatchingAttributes()) {
        String matchingAttributeName = matchingAttribute.getName();

        Set<Object> cachedValues = GrouperProvisioningConfigurationAttributeDbCache.cachedValuesForGroup(targetGroup, matchingAttributeName);

        for (Object cachedValue : GrouperUtil.nonNull(cachedValues)) {
          
          if (!GrouperUtil.isEmpty(cachedValue)) {
            cachedValue = massageToString(cachedValue, 2);
            ProvisioningUpdatableAttributeAndValue provisioningUpdatableAttributeAndValue = new ProvisioningUpdatableAttributeAndValue(matchingAttributeName, cachedValue);
            provisioningUpdatableAttributeAndValue.setCurrentValue(false);
            
            // keep the order so see if its there before adding
            if (!provisioningUpdatableAttributeAndValues.contains(provisioningUpdatableAttributeAndValue)) {
              provisioningUpdatableAttributeAndValues.add(provisioningUpdatableAttributeAndValue);
            }
          }
        }
      }
    }
    // search attributes
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isGroupMatchingAttributeSameAsSearchAttribute()) {
      for (ProvisioningGroup targetGroup: GrouperUtil.nonNull(targetGroups)) {
        
        Set<ProvisioningUpdatableAttributeAndValue> provisioningUpdatableAttributeAndValues = new LinkedHashSet<ProvisioningUpdatableAttributeAndValue>();
        targetGroup.setSearchIdAttributeNameToValues(provisioningUpdatableAttributeAndValues);

        // first do all current values, then do all past values
        for (GrouperProvisioningConfigurationAttribute searchAttribute : this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGroupSearchAttributes()) {
          String searchAttributeName = searchAttribute.getName();
          
          // dont worry if dupes... oh well
          Object targetCurrentValue = massageToString(targetGroup.retrieveAttributeValue(searchAttributeName), 2);

          if(!GrouperUtil.isBlank(targetCurrentValue)) {
            
            ProvisioningUpdatableAttributeAndValue provisioningUpdatableAttributeAndValue = new ProvisioningUpdatableAttributeAndValue(searchAttributeName, targetCurrentValue);
            provisioningUpdatableAttributeAndValue.setCurrentValue(true);
            provisioningUpdatableAttributeAndValues.add(provisioningUpdatableAttributeAndValue);
          }
        }
        
        // dont get old values for target side objects
        if (!targetGroup.isGrouperTargetObject()) {
          continue;
        }
        
        //old values
        for (GrouperProvisioningConfigurationAttribute searchAttribute : this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGroupSearchAttributes()) {
          String searchAttributeName = searchAttribute.getName();

          Set<Object> cachedValues = GrouperProvisioningConfigurationAttributeDbCache.cachedValuesForGroup(targetGroup, searchAttributeName);

          for (Object cachedValue : GrouperUtil.nonNull(cachedValues)) {
            
            if (!GrouperUtil.isEmpty(cachedValue)) {
              cachedValue = massageToString(cachedValue, 2);
              ProvisioningUpdatableAttributeAndValue provisioningUpdatableAttributeAndValue = new ProvisioningUpdatableAttributeAndValue(searchAttributeName, cachedValue);
              provisioningUpdatableAttributeAndValue.setCurrentValue(false);
              // keep the order so see if its there before adding
              if (!provisioningUpdatableAttributeAndValues.contains(provisioningUpdatableAttributeAndValue)) {
                provisioningUpdatableAttributeAndValues.add(provisioningUpdatableAttributeAndValue);
              }
            }
          }
        }
      }
      
    }
  }

  public void idTargetEntities(List<ProvisioningEntity> targetEntities) {

    if (GrouperUtil.isBlank(targetEntities)) {
      return;
    }

    for (ProvisioningEntity targetEntity: GrouperUtil.nonNull(targetEntities)) {
      
      Set<ProvisioningUpdatableAttributeAndValue> provisioningUpdatableAttributeAndValues = new LinkedHashSet<ProvisioningUpdatableAttributeAndValue>();
      targetEntity.setMatchingIdAttributeNameToValues(provisioningUpdatableAttributeAndValues);

      if (this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isEntityMatchingAttributeSameAsSearchAttribute()) {
        targetEntity.setSearchIdAttributeNameToValues(provisioningUpdatableAttributeAndValues);
      }

      boolean grouperTargetObject = targetEntity.isGrouperTargetObject();

      // first do all current values, then do all past values
      for (GrouperProvisioningConfigurationAttribute matchingAttribute : this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getEntityMatchingAttributes()) {
        String matchingAttributeName = matchingAttribute.getName();
        
        // dont worry if dupes... oh well
        Object targetCurrentValue = massageToString(targetEntity.retrieveAttributeValue(matchingAttributeName), 2);

        if(!GrouperUtil.isBlank(targetCurrentValue)) {
          
          ProvisioningUpdatableAttributeAndValue provisioningUpdatableAttributeAndValue = new ProvisioningUpdatableAttributeAndValue(matchingAttributeName, targetCurrentValue);
          provisioningUpdatableAttributeAndValue.setCurrentValue(true);
          provisioningUpdatableAttributeAndValues.add(provisioningUpdatableAttributeAndValue);
        }
      }
      
      // dont get old values for target side objects
      if (!grouperTargetObject) {
        continue;
      }
      
      //old values
      for (GrouperProvisioningConfigurationAttribute matchingAttribute : this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getEntityMatchingAttributes()) {
        String matchingAttributeName = matchingAttribute.getName();

        Set<Object> cachedValues = GrouperProvisioningConfigurationAttributeDbCache.cachedValuesForEntity(targetEntity, matchingAttributeName);

        for (Object cachedValue : GrouperUtil.nonNull(cachedValues)) {
          
          if (!GrouperUtil.isEmpty(cachedValue)) {
            cachedValue = massageToString(cachedValue, 2);
            ProvisioningUpdatableAttributeAndValue provisioningUpdatableAttributeAndValue = new ProvisioningUpdatableAttributeAndValue(matchingAttributeName, cachedValue);
            provisioningUpdatableAttributeAndValue.setCurrentValue(false);
            
            // keep the order so see if its there before adding
            if (!provisioningUpdatableAttributeAndValues.contains(provisioningUpdatableAttributeAndValue)) {
              provisioningUpdatableAttributeAndValues.add(provisioningUpdatableAttributeAndValue);
            }
          }
        }
      }
    }
    // search attributes
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isEntityMatchingAttributeSameAsSearchAttribute()) {
      for (ProvisioningEntity targetEntity: GrouperUtil.nonNull(targetEntities)) {
        
        Set<ProvisioningUpdatableAttributeAndValue> provisioningUpdatableAttributeAndValues = new LinkedHashSet<ProvisioningUpdatableAttributeAndValue>();
        targetEntity.setSearchIdAttributeNameToValues(provisioningUpdatableAttributeAndValues);

        // first do all current values, then do all past values
        for (GrouperProvisioningConfigurationAttribute searchAttribute : this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getEntitySearchAttributes()) {
          String searchAttributeName = searchAttribute.getName();
          
          // dont worry if dupes... oh well
          Object targetCurrentValue = massageToString(targetEntity.retrieveAttributeValue(searchAttributeName), 2);

          if(!GrouperUtil.isBlank(targetCurrentValue)) {
            
            ProvisioningUpdatableAttributeAndValue provisioningUpdatableAttributeAndValue = new ProvisioningUpdatableAttributeAndValue(searchAttributeName, targetCurrentValue);
            provisioningUpdatableAttributeAndValue.setCurrentValue(true);

            provisioningUpdatableAttributeAndValues.add(provisioningUpdatableAttributeAndValue);
          }
        }
        
        // dont get old values for target side objects
        if (!targetEntity.isGrouperTargetObject()) {
          continue;
        }
        
        //old values
        for (GrouperProvisioningConfigurationAttribute searchAttribute : this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getEntitySearchAttributes()) {
          String searchAttributeName = searchAttribute.getName();

          Set<Object> cachedValues = GrouperProvisioningConfigurationAttributeDbCache.cachedValuesForEntity(targetEntity, searchAttributeName);

          for (Object cachedValue : GrouperUtil.nonNull(cachedValues)) {
            
            if (!GrouperUtil.isEmpty(cachedValue)) {
              cachedValue = massageToString(cachedValue, 2);
              ProvisioningUpdatableAttributeAndValue provisioningUpdatableAttributeAndValue = new ProvisioningUpdatableAttributeAndValue(searchAttributeName, cachedValue);
              provisioningUpdatableAttributeAndValue.setCurrentValue(false);
              
              // keep the order so see if its there before adding
              if (!provisioningUpdatableAttributeAndValues.contains(provisioningUpdatableAttributeAndValue)) {
                provisioningUpdatableAttributeAndValues.add(provisioningUpdatableAttributeAndValue);
              }
            }
          }
        }
      }
      
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

    String membershipIdAttribute = null; //this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getMembershipMatchingIdAttribute();

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
        throw new RuntimeException("Must have membershipMatchingIdAttribute, or membershipMatchingIdExpression");
      }
      id = massageToString(id, 2);

      // just hard code to "id" since memberships just have one matching id
      ProvisioningUpdatableAttributeAndValue provisioningUpdatableAttributeAndValue = new ProvisioningUpdatableAttributeAndValue("id", id);
      provisioningUpdatableAttributeAndValue.setCurrentValue(true);

      targetMembership.setMatchingIdAttributeNameToValues(GrouperUtil.toSet(provisioningUpdatableAttributeAndValue));

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
   * @param provisioningGroupWrapper
   * @param field
   * @return the value
   */
  public Object translateFromGrouperProvisioningGroupField(ProvisioningGroupWrapper provisioningGroupWrapper, String field) {
    
    // "id", "idIndex", "idIndexString", "displayExtension", "displayName", "extension", "groupAttributeValueCache0", "groupAttributeValueCache1", "groupAttributeValueCache2", "groupAttributeValueCache3", "name", "description"
    if (provisioningGroupWrapper == null) { 
      return null;
    }
    
    ProvisioningGroup provisioningGroup = provisioningGroupWrapper.getGrouperProvisioningGroup();
    
    if (provisioningGroup != null) {
      if (StringUtils.equals("id", field) && !StringUtils.isBlank(provisioningGroup.getId())) {
        return provisioningGroup.getId();
      }
      if (StringUtils.equals("idIndex", field) && provisioningGroup.getIdIndex() != null) {
        return provisioningGroup.getIdIndex();
      }
      if (StringUtils.equals("idIndexString", field) && provisioningGroup.getIdIndex() != null) {
        return GrouperUtil.stringValue(provisioningGroup.getIdIndex());
      }
      if (StringUtils.equals("displayExtension", field)) {
        return GrouperUtil.stringValue(provisioningGroup.getDisplayExtension());
      }
      if (StringUtils.equals("displayName", field)) {
        return GrouperUtil.stringValue(provisioningGroup.getDisplayName());
      }
      if (StringUtils.equals("extension", field) && !StringUtils.isBlank(provisioningGroup.getExtension())) {
        return GrouperUtil.stringValue(provisioningGroup.getExtension());
      }
      if (StringUtils.equals("name", field) && !StringUtils.isBlank(provisioningGroup.getName())) {
        return GrouperUtil.stringValue(provisioningGroup.getName());
      }
      if (StringUtils.equals("description", field)) {
        return GrouperUtil.stringValue(provisioningGroup.retrieveAttributeValueString("description"));
      }
      
    }
    
    GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper.getGcGrouperSyncGroup();
    
    if (gcGrouperSyncGroup != null) {
      if (StringUtils.equals("id", field)) {
        return gcGrouperSyncGroup.getGroupId();
      }
      if (StringUtils.equals("idIndex", field)) {
        return gcGrouperSyncGroup.getGroupIdIndex();
      }
      if (StringUtils.equals("idIndexString", field)) {
        return GrouperUtil.stringValue(gcGrouperSyncGroup.getGroupIdIndex());
      }
      if (StringUtils.equals("extension", field)) {
        return GrouperUtil.extensionFromName(gcGrouperSyncGroup.getGroupName());
      }
      if (StringUtils.equals("name", field)) {
        return gcGrouperSyncGroup.getGroupName();
      }
      if (StringUtils.equals("groupAttributeValueCache0", field)) {
        String cacheValue = gcGrouperSyncGroup.getGroupAttributeValueCache0();
        if (GrouperUtil.isBlank(cacheValue) || !cacheValue.contains("{")) {
          return cacheValue;
        }
        GrouperProvisioningConfigurationAttributeDbCache cache = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupAttributeDbCaches()[0];
        if (cache == null || cache.getType() != GrouperProvisioningConfigurationAttributeDbCacheType.object) {
          return cacheValue;
        }
        return this.grouperProvisioner.retrieveGrouperProvisioningData().parseJsonCacheGroup(cacheValue);
      }
      if (StringUtils.equals("groupAttributeValueCache1", field)) {
        String cacheValue = gcGrouperSyncGroup.getGroupAttributeValueCache1();
        if (GrouperUtil.isBlank(cacheValue) || !cacheValue.contains("{")) {
          return cacheValue;
        }
        GrouperProvisioningConfigurationAttributeDbCache cache = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupAttributeDbCaches()[1];
        if (cache == null || cache.getType() != GrouperProvisioningConfigurationAttributeDbCacheType.object) {
          return cacheValue;
        }
        return this.grouperProvisioner.retrieveGrouperProvisioningData().parseJsonCacheGroup(cacheValue);
      }
      if (StringUtils.equals("groupAttributeValueCache2", field)) {
        String cacheValue = gcGrouperSyncGroup.getGroupAttributeValueCache2();
        if (GrouperUtil.isBlank(cacheValue) || !cacheValue.contains("{")) {
          return cacheValue;
        }
        GrouperProvisioningConfigurationAttributeDbCache cache = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupAttributeDbCaches()[2];
        if (cache == null || cache.getType() != GrouperProvisioningConfigurationAttributeDbCacheType.object) {
          return cacheValue;
        }
        return this.grouperProvisioner.retrieveGrouperProvisioningData().parseJsonCacheGroup(cacheValue);
      }
      if (StringUtils.equals("groupAttributeValueCache3", field)) {
        String cacheValue = gcGrouperSyncGroup.getGroupAttributeValueCache3();
        if (GrouperUtil.isBlank(cacheValue) || !cacheValue.contains("{")) {
          return cacheValue;
        }
        GrouperProvisioningConfigurationAttributeDbCache cache = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupAttributeDbCaches()[3];
        if (cache == null || cache.getType() != GrouperProvisioningConfigurationAttributeDbCacheType.object) {
          return cacheValue;
        }
        return this.grouperProvisioner.retrieveGrouperProvisioningData().parseJsonCacheGroup(cacheValue);
      }
      
    }
    
    //if we couldnt find the data but the field was ok, its just null
    if (StringUtils.equalsAny(field, "id", "idIndex", "idIndexString", "displayExtension", "displayName", "extension", 
        "groupAttributeValueCache0", "groupAttributeValueCache1", "groupAttributeValueCache2", "groupAttributeValueCache3", 
        "name", "description")) {
      return null;
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

  public String getTranslateFromGrouperProvisioningGroupField(boolean forCreate, GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute) {
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

  public String getTranslateFromGrouperProvisioningEntityField(boolean forCreate, GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute) {
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
  

  public String getTargetExpressionToUse(boolean forCreate, GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute) {
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
  
  public String getTranslateFromStaticValuesToUse(boolean forCreate, GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute) {
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