package edu.internet2.middleware.grouper.app.provisioning;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncErrorCode;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class GrouperProvisioningValidation {

  
  /**
   * reference back up to the provisioner
   */
  private GrouperProvisioner grouperProvisioner = null;
  
  private int entitiesMissingRequiredData = 0;
  private int entitiesViolateMaxLength = 0;
  private int entitiesViolateValidExpression = 0;
  
  private int groupsMissingRequiredData = 0;
  private int groupsViolateMaxLength = 0;
  private int groupsViolateValidExpression = 0;

  public GrouperProvisioningValidation() {
  }

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
   * see if a group has a matching ID
   * @param provisioningGroup
   * @return true if has matching id
   */
  public boolean validateGroupHasMatchingId(ProvisioningGroup provisioningGroup, boolean forInsert) {

    if (GrouperUtil.length(provisioningGroup.getMatchingIdAttributeNameToValues()) > 0) {
      return true;
    }

    // cant have one
    if (GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGroupMatchingAttributes())==0) {
      return true;
    }
    
    if (forInsert) {
      for (GrouperProvisioningConfigurationAttribute matchingAttribute : this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGroupMatchingAttributes()) {
        // if there is no translation, then thats ok... its retrieved from the target so it will be null until after create
        if (matchingAttribute.getTranslateExpressionType() == null && matchingAttribute.getTranslateExpressionTypeCreateOnly() == null) {
          return true;
        }
      }
    }
    return false;
  }
  
  /**
   * see if an entity has a matching ID
   * @param provisioningEntity
   * @return true if has matching id
   */
  public boolean validateEntityHasMatchingId(ProvisioningEntity provisioningEntity, boolean forInsert) {

    if (GrouperUtil.length(provisioningEntity.getMatchingIdAttributeNameToValues()) > 0) {
      return true;
    }
    
    // cant have one
    if (GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getEntityMatchingAttributes())==0) {
      return true;
    }

    if (forInsert) {
      for (GrouperProvisioningConfigurationAttribute matchingAttribute : this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getEntityMatchingAttributes()) {
        // if there is no translation, then thats ok... its retrieved from the target so it will be null until after create
        if (matchingAttribute.getTranslateExpressionType() == null && matchingAttribute.getTranslateExpressionTypeCreateOnly() == null) {
          return true;
        }
      }
    }
    return false;
  }
  
  /**
   * validate groups based on attribute constraints and set the error code in the sync
   * object and the wrapper object
   * @param provisioningGroups
   * @param removeInvalid
   */
  public Set<ProvisioningGroup> validateGroups(Collection<ProvisioningGroup> provisioningGroups, boolean removeInvalid, Boolean forMembershipAttribute, boolean forInsert) {
    
    Set<ProvisioningGroup> invalidGroups = new LinkedHashSet<ProvisioningGroup>();

    String membershipAttributeName =  null;
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes) {
      membershipAttributeName =  this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGroupMembershipAttributeName();
    }
    
    //check for required attributes
    Iterator<ProvisioningGroup> iterator = provisioningGroups.iterator();
    GROUPS: while (iterator.hasNext()) {
      ProvisioningGroup provisioningGroup = iterator.next();
      ProvisioningGroupWrapper provisioningGroupWrapper = provisioningGroup.getProvisioningGroupWrapper();
      if (provisioningGroupWrapper.getErrorCode() != null) {
      if (removeInvalid) {
        iterator.remove();
          continue;
      	}
      }
      GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper == null ? null : provisioningGroupWrapper.getGcGrouperSyncGroup();
      
      //if we're not provisioning this group, maybe this is used in membership that needs to be removed so we shouldn't validate.
      if (gcGrouperSyncGroup != null && !gcGrouperSyncGroup.isProvisionable()) {
        continue;
      }

      // matching ID must be there
      // lets see which attribute is the matching id
      if (!validateGroupHasMatchingId(provisioningGroup, forInsert)) {
        this.assignGroupError(provisioningGroupWrapper, GcGrouperSyncErrorCode.MAT, "matching ID is required and missing");
        invalidGroups.add(provisioningGroup);
        if (removeInvalid) {
          iterator.remove();
          continue;
        }
      }
      
      for (Collection<GrouperProvisioningConfigurationAttribute> grouperProvisioningConfigurationAttributes : 
        new Collection[] {
          this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getTargetGroupAttributeNameToConfig().values(),
          this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getTargetGroupAttributeNameToConfig().values()}) {
        // look for required fields
        for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : 
            grouperProvisioningConfigurationAttributes) {
          
          boolean isMembershipAttribute = StringUtils.equals(membershipAttributeName, grouperProvisioningConfigurationAttribute.getName());
          
          if (forMembershipAttribute != null && isMembershipAttribute != forMembershipAttribute) {
            continue;
          }
          
          boolean hasErrorCode = assignErrorCodeToGroupWrapper(provisioningGroup, grouperProvisioningConfigurationAttribute, provisioningGroupWrapper);
          if (hasErrorCode) {
            invalidGroups.add(provisioningGroup);
          }
          if (hasErrorCode && removeInvalid) {
            iterator.remove();
          }
          continue GROUPS;
        }
      }
    }
      
    if (groupsMissingRequiredData > 0) {
      GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "groupsViolateMaxLength", groupsViolateMaxLength);
    }
    if (groupsMissingRequiredData > 0) {
      GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "groupsMissingRequiredData", groupsMissingRequiredData);
    }
    if (groupsViolateValidExpression > 0) {
      GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "groupsViolateValidExpression", groupsViolateValidExpression);
    }
    if (GrouperUtil.length(invalidGroups) > 0) {
      this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.validateGrouperGroups, invalidGroups);
    }
    return invalidGroups;
  }
  
  /**
   * validate entities based on attribute constraints and set the error code in the sync
   * object and the wrapper object
   * @param provisioningEntities
   * @param removeInvalid
   */
  public Set<ProvisioningEntity> validateEntities(Collection<ProvisioningEntity> provisioningEntities, boolean removeInvalid, Boolean forMembershipAttribute, boolean forInsert) {
    
    Set<ProvisioningEntity> invalidEntities = new LinkedHashSet<ProvisioningEntity>();
    String membershipAttributeName =  null;
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes) {
      membershipAttributeName =  this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getEntityMembershipAttributeName();
    }
    
    //check for required attributes
    Iterator<ProvisioningEntity> iterator = provisioningEntities.iterator();
    ENTITIES: while (iterator.hasNext()) {
      ProvisioningEntity provisioningEntity = iterator.next();
      ProvisioningEntityWrapper provisioningEntityWrapper = provisioningEntity.getProvisioningEntityWrapper();
      if (provisioningEntityWrapper.getErrorCode() != null) {
        continue;
      }
      GcGrouperSyncMember gcGrouperSyncMember = provisioningEntityWrapper == null ? null : provisioningEntityWrapper.getGcGrouperSyncMember();

      //if we're not provisioning this entity, maybe this is used in membership that needs to be removed so we shouldn't validate.  
      if (gcGrouperSyncMember != null && !gcGrouperSyncMember.isProvisionable() && (!gcGrouperSyncMember.isInTarget() || provisioningEntityWrapper.getProvisioningStateEntity().isDelete())) {
        continue;
      }
      
      // matching ID must be there
      if (!validateEntityHasMatchingId(provisioningEntity, forInsert)) {
        this.assignEntityError(provisioningEntityWrapper, GcGrouperSyncErrorCode.MAT, "matching ID is required and missing");
        invalidEntities.add(provisioningEntity);
        if (removeInvalid) {
          iterator.remove();
          continue;
        }
      }
      
      for (Collection<GrouperProvisioningConfigurationAttribute> grouperProvisioningConfigurationAttributes : 
        new Collection[] {
          this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getTargetEntityAttributeNameToConfig().values(),
          this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getTargetEntityAttributeNameToConfig().values()}) {
        // look for required fields
        for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : 
            grouperProvisioningConfigurationAttributes) {
          
          boolean isMembershipAttribute = StringUtils.equals(membershipAttributeName, grouperProvisioningConfigurationAttribute.getName());
          
          if (forMembershipAttribute != null && isMembershipAttribute != forMembershipAttribute) {
            continue;
          }
          
          boolean hasError = assignErrorCodeToEntityWrapper(provisioningEntity, grouperProvisioningConfigurationAttribute, provisioningEntityWrapper);
          if (hasError) {
            invalidEntities.add(provisioningEntity);
          }
          if (hasError && removeInvalid) {
            iterator.remove();
          }
          continue ENTITIES;
        }
      }
    }
      
    if (entitiesMissingRequiredData > 0) {
      GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "entitiesViolateMaxLength", entitiesViolateMaxLength);
    }
    if (entitiesMissingRequiredData > 0) {
      GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "entitiesMissingRequiredData", entitiesMissingRequiredData);
    }
    if (entitiesViolateValidExpression > 0) {
      GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "entitiesViolateValidExpression", entitiesViolateValidExpression);
    }
    if (GrouperUtil.length(invalidEntities) > 0) {
      this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.validateGrouperEntities, invalidEntities);
    }
    return invalidEntities;
  }
  
/**
 * 
 * @param grouperTargetEntity
 * @param grouperProvisioningConfigurationAttribute
 * @param provisioningEntityWrapper
 * @return true for error and false for no error
 */
  public boolean assignErrorCodeToEntityWrapper(ProvisioningEntity grouperTargetEntity, 
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute,
      ProvisioningEntityWrapper provisioningEntityWrapper) {
    
    MultiKey validationError = this.validFieldOrAttributeValue(grouperTargetEntity, grouperProvisioningConfigurationAttribute);
    if (validationError != null) {
      GcGrouperSyncErrorCode errorCode = (GcGrouperSyncErrorCode)validationError.getKey(0);
      String errorMessage = (String)validationError.getKey(1);
      this.assignEntityError(provisioningEntityWrapper, errorCode, errorMessage);
      switch (errorCode) {
        case INV:
          entitiesViolateValidExpression++;
          break;
        case LEN:
          entitiesViolateMaxLength++;
          break;
        case REQ:
          entitiesMissingRequiredData++;
          break;
        default:
          throw new RuntimeException("Not expecting error code: " + errorCode);
      }
      return true;
    }
    
    return false;
    
  }
  
  /**
   * 
   * @param grouperTargetGroup
   * @param grouperProvisioningConfigurationAttribute
   * @param provisioningGroupWrapper
   * @return true for error and false for no error
   */
    public boolean assignErrorCodeToGroupWrapper(ProvisioningGroup grouperTargetGroup, 
        GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute,
        ProvisioningGroupWrapper provisioningGroupWrapper) {
      
      MultiKey validationError = this.validFieldOrAttributeValue(grouperTargetGroup, grouperProvisioningConfigurationAttribute);
      if (validationError != null) {
        GcGrouperSyncErrorCode errorCode = (GcGrouperSyncErrorCode)validationError.getKey(0);
        String errorMessage = (String)validationError.getKey(1);
        this.assignGroupError(provisioningGroupWrapper, errorCode, errorMessage);
        switch (errorCode) {
          case INV:
            groupsViolateValidExpression++;
            break;
          case LEN:
            groupsViolateMaxLength++;
            break;
          case REQ:
            groupsMissingRequiredData++;
            break;
          default:
            throw new RuntimeException("Not expecting error code: " + errorCode);
        }
        return true;
      }
      
      return false;
      
    }
  
  
  /**
   * validate memberships based on attribute constraints and set the error code in the sync
   * object and the wrapper object
   * @param provisioningMemberships
   * @param removeInvalid
   */
  public Set<ProvisioningMembership> validateMemberships(Collection<ProvisioningMembership> provisioningMemberships, boolean removeInvalid) {

    Set<ProvisioningMembership> invalidMemberships = new LinkedHashSet<ProvisioningMembership>();
    int membershipsMissingRequiredData = 0;
    int membershipsDoNotExist = 0;
    int membershipsViolateMaxLength = 0;
    int membershipsViolateValidExpression = 0;
    //check for required attributes
    Iterator<ProvisioningMembership> iterator = provisioningMemberships.iterator();
    MEMBERSHIPS: while (iterator.hasNext()) {
      ProvisioningMembership provisioningMembership = iterator.next();
      ProvisioningMembershipWrapper provisioningMembershipWrapper = provisioningMembership.getProvisioningMembershipWrapper();
      if (provisioningMembershipWrapper.getErrorCode() != null) {
        continue;
      }
      
      for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : 
          this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getTargetMembershipAttributeNameToConfig().values()) {
        MultiKey validationError = this.validFieldOrAttributeValue(provisioningMembership, grouperProvisioningConfigurationAttribute);
        if (validationError != null) {
          GcGrouperSyncErrorCode errorCode = (GcGrouperSyncErrorCode)validationError.getKey(0);
          String errorMessage = (String)validationError.getKey(1);
          this.assignMembershipError(provisioningMembershipWrapper, errorCode, errorMessage);
          invalidMemberships.add(provisioningMembership);
          switch (errorCode) {
            case INV:
              membershipsViolateValidExpression++;
              break;
            case LEN:
              membershipsViolateMaxLength++;
              break;
            case REQ:
              membershipsMissingRequiredData++;
              break;
            case DNE:
              membershipsDoNotExist++;
              break;
            default:
              throw new RuntimeException("Not expecting error code: " + errorCode);
          }
          if (removeInvalid) {
            iterator.remove();
          }
          continue MEMBERSHIPS;
        }
      }
      
    }
      
    if (membershipsMissingRequiredData > 0) {
      GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "membershipsViolateMaxLength", membershipsViolateMaxLength);
    }
    if (membershipsMissingRequiredData > 0) {
      GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "membershipsMissingRequiredData", membershipsMissingRequiredData);
    }
    if (membershipsDoNotExist > 0) {
      GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "membershipsDoNotExist", membershipsDoNotExist);
    }
    if (membershipsViolateValidExpression > 0) {
      GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "membershipsViolateValidExpression", membershipsViolateValidExpression);
    }
    if (GrouperUtil.length(invalidMemberships) > 0) {
      this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.validateGrouperMemberships, invalidMemberships);
    }
    return invalidMemberships;
  }  

  /**
   * assign error message to wrapper and sync object
   * @param provisioningMembershipWrapper
   * @param errorCode
   * @param errorMessage
   */
  public void assignMembershipError(ProvisioningMembershipWrapper provisioningMembershipWrapper, GcGrouperSyncErrorCode errorCode, String errorMessage) {
    if (provisioningMembershipWrapper.getErrorCode() == null) {
      provisioningMembershipWrapper.setErrorCode(errorCode);
    }
    
    GcGrouperSyncMembership gcGrouperSyncMembership = provisioningMembershipWrapper.getGcGrouperSyncMembership();
    
    if (gcGrouperSyncMembership.getErrorCode() != errorCode) {
      gcGrouperSyncMembership.setErrorCode(errorCode);
      gcGrouperSyncMembership.setErrorMessage(errorMessage);
      gcGrouperSyncMembership.setErrorTimestamp(new Timestamp(System.currentTimeMillis()));
    }

  }
  
  /**
   * assign error message to wrapper and sync object
   * @param provisioningGroupWrapper
   * @param errorCode
   * @param errorMessage
   */
  public void assignGroupError(ProvisioningGroupWrapper provisioningGroupWrapper, GcGrouperSyncErrorCode errorCode, String errorMessage) {
    if (provisioningGroupWrapper.getErrorCode() == null) {
      provisioningGroupWrapper.setErrorCode(errorCode);
    }
    
    GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper.getGcGrouperSyncGroup();
    
    if (gcGrouperSyncGroup.getErrorCode() != errorCode) {
      gcGrouperSyncGroup.setErrorCode(errorCode);
      gcGrouperSyncGroup.setErrorMessage(errorMessage);
      gcGrouperSyncGroup.setErrorTimestamp(new Timestamp(System.currentTimeMillis()));
    }

  }
  
  /**
   * assign error message to wrapper and sync object
   * @param provisioningMemberWrapper
   * @param errorCode
   * @param errorMessage
   */
  public void assignEntityError(ProvisioningEntityWrapper provisioningMemberWrapper, GcGrouperSyncErrorCode errorCode, String errorMessage) {
    if (provisioningMemberWrapper.getErrorCode() == null) {
      provisioningMemberWrapper.setErrorCode(errorCode);
    }
    
    GcGrouperSyncMember gcGrouperSyncMember = provisioningMemberWrapper.getGcGrouperSyncMember();
    
    if (gcGrouperSyncMember.getErrorCode() != errorCode) {
      gcGrouperSyncMember.setErrorCode(errorCode);
      gcGrouperSyncMember.setErrorMessage(errorMessage);
      gcGrouperSyncMember.setErrorTimestamp(new Timestamp(System.currentTimeMillis()));
    }

  }
  
  /**
   * check an attribute and make sure all values are valid
   * @param provisioningUpdatable
   * @param grouperProvisioningConfigurationAttribute
   * @param fieldOrAttributeValueOrig
   * @return GcGrouperSyncErrorCode, error string description or null
   */
  public MultiKey validFieldOrAttributeValue(ProvisioningUpdatable provisioningUpdatable, GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute) {

    Object fieldOrAttributeValueOrig = null;
    String wrapperKey = null;
    Object wrapperValue = null;
    if (provisioningUpdatable instanceof ProvisioningGroup) {
      fieldOrAttributeValueOrig = ((ProvisioningGroup)provisioningUpdatable).retrieveAttributeValue(grouperProvisioningConfigurationAttribute);
      wrapperKey = "provisioningGroupWrapper";
      wrapperValue = ((ProvisioningGroup)provisioningUpdatable).getProvisioningGroupWrapper();
    } else if (provisioningUpdatable instanceof ProvisioningMembership) {
      fieldOrAttributeValueOrig = ((ProvisioningMembership)provisioningUpdatable).retrieveAttributeValue(grouperProvisioningConfigurationAttribute);
      wrapperKey = "provisioningMembershipWrapper";
      wrapperValue = ((ProvisioningMembership)provisioningUpdatable).getProvisioningMembershipWrapper();
    } else if (provisioningUpdatable instanceof ProvisioningEntity) {
      fieldOrAttributeValueOrig = ((ProvisioningEntity)provisioningUpdatable).retrieveAttributeValue(grouperProvisioningConfigurationAttribute);
      wrapperKey = "provisioningEntityWrapper";
      wrapperValue = ((ProvisioningEntity)provisioningUpdatable).getProvisioningEntityWrapper();
    } else {
      throw new RuntimeException("Not expecting provisioningUpdatable type: " + (provisioningUpdatable == null ? null : provisioningUpdatable.getClass()));
    }
    Collection fieldOrAttributeValueCollection = null;
        
    if (fieldOrAttributeValueOrig instanceof Collection) {
      fieldOrAttributeValueCollection = ((Collection)fieldOrAttributeValueOrig);
    } else {
      fieldOrAttributeValueCollection = new HashSet<Object>();
      fieldOrAttributeValueCollection.add(fieldOrAttributeValueOrig);
    }
    
    // if this is multiValued and nothing there, then check null
    if (GrouperUtil.length(fieldOrAttributeValueCollection) == 0) {
      return validFieldOrAttributeValueHelper(provisioningUpdatable, grouperProvisioningConfigurationAttribute, null,
          fieldOrAttributeValueCollection, wrapperKey, wrapperValue);
    } else {
      for (Object fieldOrAttributeValue : fieldOrAttributeValueCollection) {
        MultiKey validationError = validFieldOrAttributeValueHelper(provisioningUpdatable, grouperProvisioningConfigurationAttribute, fieldOrAttributeValue,
            fieldOrAttributeValueCollection, wrapperKey, wrapperValue);
        if (validationError != null) {
          return validationError;
        }
      }
    }
    return null;
  }
  
  /**
   * validate a specific value for a field
   * @param provisioningUpdatable
   * @param grouperProvisioningConfigurationAttribute
   * @param fieldOrAttributeValueOrig
   * @return GcGrouperSyncErrorCode, error string description or null
   */
  public MultiKey validFieldOrAttributeValue(ProvisioningUpdatable provisioningUpdatable, GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute, Object fieldOrAttributeValue) {

    String wrapperKey = null;
    Object wrapperValue = null;
    if (provisioningUpdatable instanceof ProvisioningGroup) {
      wrapperKey = "provisioningGroupWrapper";
      wrapperValue = ((ProvisioningGroup)provisioningUpdatable).getProvisioningGroupWrapper();
    } else if (provisioningUpdatable instanceof ProvisioningMembership) {
      wrapperKey = "provisioningMembershipWrapper";
      wrapperValue = ((ProvisioningMembership)provisioningUpdatable).getProvisioningMembershipWrapper();
    } else if (provisioningUpdatable instanceof ProvisioningEntity) {
      wrapperKey = "provisioningEntityWrapper";
      wrapperValue = ((ProvisioningEntity)provisioningUpdatable).getProvisioningEntityWrapper();
    } else {
      throw new RuntimeException("Not expecting provisioningUpdatable type: " + (provisioningUpdatable == null ? null : provisioningUpdatable.getClass()));
    }
    return validFieldOrAttributeValueHelper(provisioningUpdatable, grouperProvisioningConfigurationAttribute, fieldOrAttributeValue,
          null, wrapperKey, wrapperValue);
  }
  
  /**
   * internal common method
   * @param provisioningUpdatable
   * @param grouperProvisioningConfigurationAttribute
   * @param fieldOrAttributeValueOrig
   * @return GcGrouperSyncErrorCode, error string description or null
   */
  private MultiKey validFieldOrAttributeValueHelper(ProvisioningUpdatable provisioningUpdatable, 
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute, Object fieldOrAttributeValue,
      Object fieldOrAttributeValueCollection, String wrapperKey, Object wrapperValue) {
    if (grouperProvisioningConfigurationAttribute.isRequired()) {
      if (null == fieldOrAttributeValue) {
        return new MultiKey(GcGrouperSyncErrorCode.REQ, grouperProvisioningConfigurationAttribute.getName() + " is required and missing");
      }
    }
    if (grouperProvisioningConfigurationAttribute.getMaxlength() != null) {
      String fieldOrAttributeValueString = GrouperUtil.stringValue(fieldOrAttributeValue);
      if (!StringUtils.isEmpty(fieldOrAttributeValueString) && fieldOrAttributeValueString.length() > grouperProvisioningConfigurationAttribute.getMaxlength()) {
        return new MultiKey(GcGrouperSyncErrorCode.LEN, grouperProvisioningConfigurationAttribute.getName() + " is length " 
              + fieldOrAttributeValueString.length() + " which is longer than maxlength " + grouperProvisioningConfigurationAttribute.getMaxlength()
              + ": '" + fieldOrAttributeValueString + "'");
      }
    }
    if (!StringUtils.isBlank(grouperProvisioningConfigurationAttribute.getValidExpression())) {
      // Validate value with jexl to see if valid for provisioning, the variable 'value' represents the current value.  return true if valid and false if invalid
      Map<String, Object> variableMap = new HashMap<String, Object>();
      variableMap.put("value", fieldOrAttributeValue);
      if (grouperProvisioningConfigurationAttribute.isMultiValued() && fieldOrAttributeValueCollection!= null) {
        variableMap.put("valueMultiple", fieldOrAttributeValueCollection);
      }
      variableMap.put(wrapperKey, wrapperValue);
      Object result = this.getGrouperProvisioner().retrieveGrouperProvisioningTranslator().runScript(grouperProvisioningConfigurationAttribute.getValidExpression(), variableMap);
      boolean valid = GrouperUtil.booleanValue(result, false);
      
      if (!valid) {
        return new MultiKey(GcGrouperSyncErrorCode.INV, grouperProvisioningConfigurationAttribute.getName() + " is invalid based on expression '" 
              + grouperProvisioningConfigurationAttribute.getValidExpression() + "': '" + fieldOrAttributeValue + "'");
      }
    }
    return null;
  }

  /**
   * validate groups have members if required
   * object and the wrapper object
   * @param provisioningGroups
   * @param removeInvalid
   */
  public Set<ProvisioningGroup> validateGroupsHaveMembers(Collection<ProvisioningGroup> provisioningGroups, boolean removeInvalid) {

    Set<ProvisioningGroup> invalidGroups = new LinkedHashSet<ProvisioningGroup>();

    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isGroupsRequireMembers()) {
      return invalidGroups;
    }

    Map<String, ProvisioningGroupWrapper> groupIdToGroupWrapperToCheck = new HashMap<String, ProvisioningGroupWrapper>();
    
    //check for required attributes
    Iterator<ProvisioningGroup> iterator = provisioningGroups.iterator();
    while (iterator.hasNext()) {
      ProvisioningGroup provisioningGroup = iterator.next();
      ProvisioningGroupWrapper provisioningGroupWrapper = provisioningGroup.getProvisioningGroupWrapper();

      GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper == null ? null : provisioningGroupWrapper.getGcGrouperSyncGroup();
      
      //if we're not provisioning this group, maybe this is used in membership that needs to be removed so we shouldn't validate.
      if (gcGrouperSyncGroup != null && !gcGrouperSyncGroup.isProvisionable()) {
        continue;
      }
      
      ProvisioningGroup grouperProvisioningGroup = provisioningGroupWrapper.getGrouperProvisioningGroup();
      
      groupIdToGroupWrapperToCheck.put(grouperProvisioningGroup.getId(), provisioningGroupWrapper);
      
    }      

    if (GrouperUtil.length(groupIdToGroupWrapperToCheck) == 0) {
      return invalidGroups;
    }

    List<String> groupIdsList = new ArrayList<String>(groupIdToGroupWrapperToCheck.keySet());
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(groupIdsList, 900);
    
    Map<String, Integer> groupIdToCount = new HashMap<String, Integer>();
    
    
    for (int i=0;i<numberOfBatches;i++) { 
      
      List<String> groupIdsBatch = GrouperUtil.batchList(groupIdsList, 900, i);
      //TODO make this more efficient
      List<Object[]> groupIdCounts = new GcDbAccess().sql("select gmlv.group_id, count(*) from grouper_memberships_lw_v gmlv where gmlv.list_name = 'members' " 
          + " and gmlv.group_id in (" + GrouperClientUtils.appendQuestions(groupIdsBatch.size()) + ") group by group_id").addBindVars(groupIdsBatch).selectList(Object[].class);

      for (Object[] groupIdCount : groupIdCounts) {
        String groupId = (String)groupIdCount[0];
        int count = GrouperUtil.intValue(groupIdCount[1]);
        groupIdToCount.put(groupId, count);
      }
    }
    
    iterator = provisioningGroups.iterator();
    while (iterator.hasNext()) {
      ProvisioningGroup provisioningGroup = iterator.next();
      ProvisioningGroupWrapper provisioningGroupWrapper = provisioningGroup.getProvisioningGroupWrapper();
      ProvisioningGroup grouperProvisioningGroup = provisioningGroupWrapper.getGrouperProvisioningGroup();
      Integer count = groupIdToCount.get(grouperProvisioningGroup.getId());
      if (count == null || count == 0) {
        groupsMissingMembers++;
        assignGroupError(provisioningGroupWrapper, GcGrouperSyncErrorCode.MEM, "Group has no members and members are required");
        invalidGroups.add(provisioningGroup);
        if (removeInvalid) {
          iterator.remove();
        } else {
          // remove the target group
          if (provisioningGroupWrapper != null) {
            provisioningGroupWrapper.getProvisioningStateGroup().setDelete(true);
          }
        }
      }
    }    

    if (groupsMissingMembers > 0) {
      GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "groupsMissingMembers", groupsMissingMembers);
    }
    if (GrouperUtil.length(invalidGroups) > 0) {
      this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.validateGrouperGroups, invalidGroups);
    }
    return invalidGroups;
  }

  private int groupsMissingMembers = 0;

}
