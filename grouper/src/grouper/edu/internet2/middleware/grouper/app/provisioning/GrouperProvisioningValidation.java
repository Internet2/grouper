package edu.internet2.middleware.grouper.app.provisioning;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncErrorCode;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;

public class GrouperProvisioningValidation {

  
  /**
   * reference back up to the provisioner
   */
  private GrouperProvisioner grouperProvisioner = null;

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
   * validate groups based on attribute constraints and set the error code in the sync
   * object and the wrapper object
   * @param provisioningGroups
   * @param removeInvalid
   */
  public void validateGroups(Collection<ProvisioningGroup> provisioningGroups, boolean removeInvalid) {
    int groupsMissingRequiredData = 0;
    int groupsViolateMaxLength = 0;
    int groupsViolateValidExpression = 0;
    //check for required attributes
    Iterator<ProvisioningGroup> iterator = provisioningGroups.iterator();
    GROUPS: while (iterator.hasNext()) {
      ProvisioningGroup provisioningGroup = iterator.next();
      ProvisioningGroupWrapper provisioningGroupWrapper = provisioningGroup.getProvisioningGroupWrapper();
      if (provisioningGroupWrapper.getErrorCode() != null) {
        continue;
      }
      GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper == null ? null : provisioningGroupWrapper.getGcGrouperSyncGroup();
      
      for (Collection<GrouperProvisioningConfigurationAttribute> grouperProvisioningConfigurationAttributes : 
        new Collection[] {
          this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getTargetGroupFieldNameToConfig().values(),
          this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getTargetGroupAttributeNameToConfig().values()}) {
        // look for required fields
        for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : 
            grouperProvisioningConfigurationAttributes) {
          MultiKey validationError = this.validFieldOrAttributeValue(provisioningGroup, grouperProvisioningConfigurationAttribute);
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
            if (removeInvalid) {
              iterator.remove();
            }
            continue GROUPS;
          }
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

  }
  
  /**
   * validate entities based on attribute constraints and set the error code in the sync
   * object and the wrapper object
   * @param provisioningEntities
   * @param removeInvalid
   */
  public void validateEntities(Collection<ProvisioningEntity> provisioningEntities, boolean removeInvalid) {
    int entitiesMissingRequiredData = 0;
    int entitiesViolateMaxLength = 0;
    int entitiesViolateValidExpression = 0;
    //check for required attributes
    Iterator<ProvisioningEntity> iterator = provisioningEntities.iterator();
    ENTITIES: while (iterator.hasNext()) {
      ProvisioningEntity provisioningEntity = iterator.next();
      ProvisioningEntityWrapper provisioningEntityWrapper = provisioningEntity.getProvisioningEntityWrapper();
      if (provisioningEntityWrapper.getErrorCode() != null) {
        continue;
      }
      GcGrouperSyncMember gcGrouperSyncMember = provisioningEntityWrapper == null ? null : provisioningEntityWrapper.getGcGrouperSyncMember();
      
      for (Collection<GrouperProvisioningConfigurationAttribute> grouperProvisioningConfigurationAttributes : 
        new Collection[] {
          this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getTargetEntityFieldNameToConfig().values(),
          this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getTargetEntityAttributeNameToConfig().values()}) {
        // look for required fields
        for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : 
            grouperProvisioningConfigurationAttributes) {
          MultiKey validationError = this.validFieldOrAttributeValue(provisioningEntity, grouperProvisioningConfigurationAttribute);
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
            if (removeInvalid) {
              iterator.remove();
            }
            continue ENTITIES;
          }
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

  }
  
  /**
   * validate memberships based on attribute constraints and set the error code in the sync
   * object and the wrapper object
   * @param provisioningEntities
   * @param removeInvalid
   */
  public void validateMemberships(Collection<ProvisioningMembership> provisioningEntities, boolean removeInvalid) {
    int membershipsMissingRequiredData = 0;
    int membershipsViolateMaxLength = 0;
    int membershipsViolateValidExpression = 0;
    //check for required attributes
    Iterator<ProvisioningMembership> iterator = provisioningEntities.iterator();
    MEMBERSHIPS: while (iterator.hasNext()) {
      ProvisioningMembership provisioningMembership = iterator.next();
      ProvisioningMembershipWrapper provisioningMembershipWrapper = provisioningMembership.getProvisioningMembershipWrapper();
      if (provisioningMembershipWrapper.getErrorCode() != null) {
        continue;
      }
      GcGrouperSyncMembership gcGrouperSyncMembership = provisioningMembershipWrapper == null ? null : provisioningMembershipWrapper.getGcGrouperSyncMembership();
      
      for (Collection<GrouperProvisioningConfigurationAttribute> grouperProvisioningConfigurationAttributes : 
        new Collection[] {
          this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getTargetMembershipFieldNameToConfig().values(),
          this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getTargetMembershipAttributeNameToConfig().values()}) {
        // look for required fields
        for (GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute : 
            grouperProvisioningConfigurationAttributes) {
          MultiKey validationError = this.validFieldOrAttributeValue(provisioningMembership, grouperProvisioningConfigurationAttribute);
          if (validationError != null) {
            GcGrouperSyncErrorCode errorCode = (GcGrouperSyncErrorCode)validationError.getKey(0);
            String errorMessage = (String)validationError.getKey(1);
            this.assignMembershipError(provisioningMembershipWrapper, errorCode, errorMessage);
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
    }
      
    if (membershipsMissingRequiredData > 0) {
      GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "membershipsViolateMaxLength", membershipsViolateMaxLength);
    }
    if (membershipsMissingRequiredData > 0) {
      GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "membershipsMissingRequiredData", membershipsMissingRequiredData);
    }
    if (membershipsViolateValidExpression > 0) {
      GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "membershipsViolateValidExpression", membershipsViolateValidExpression);
    }
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
      fieldOrAttributeValueOrig = ((ProvisioningGroup)provisioningUpdatable).retrieveFieldOrAttributeValue(grouperProvisioningConfigurationAttribute);
      wrapperKey = "provisioningGroupWrapper";
      wrapperValue = ((ProvisioningGroup)provisioningUpdatable).getProvisioningGroupWrapper();
    } else if (provisioningUpdatable instanceof ProvisioningMembership) {
      fieldOrAttributeValueOrig = ((ProvisioningMembership)provisioningUpdatable).retrieveFieldOrAttributeValue(grouperProvisioningConfigurationAttribute);
      wrapperKey = "provisioningMembershipWrapper";
      wrapperValue = ((ProvisioningMembership)provisioningUpdatable).getProvisioningMembershipWrapper();
    } else if (provisioningUpdatable instanceof ProvisioningEntity) {
      fieldOrAttributeValueOrig = ((ProvisioningEntity)provisioningUpdatable).retrieveFieldOrAttributeValue(grouperProvisioningConfigurationAttribute);
      wrapperKey = "provisioningEntityWrapper";
      wrapperValue = ((ProvisioningEntity)provisioningUpdatable).getProvisioningEntityWrapper();
    } else {
      throw new RuntimeException("Not expecitng provisioningUpdatable type: " + (provisioningUpdatable == null ? null : provisioningUpdatable.getClass()));
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
      throw new RuntimeException("Not expecitng provisioningUpdatable type: " + (provisioningUpdatable == null ? null : provisioningUpdatable.getClass()));
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
      Object result = this.getGrouperProvisioner().retrieveGrouperTranslator().runScript(grouperProvisioningConfigurationAttribute.getValidExpression(), variableMap);
      boolean valid = GrouperUtil.booleanValue(result, false);
      
      if (!valid) {
        return new MultiKey(GcGrouperSyncErrorCode.INV, grouperProvisioningConfigurationAttribute.getName() + " is invalid based on expression '" 
              + grouperProvisioningConfigurationAttribute.getValidExpression() + "': '" + fieldOrAttributeValue + "'");
      }
    }
    return null;
  }
  
}
