package edu.internet2.middleware.grouper.app.provisioning;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * tuple of group and entity in target system
 * @author mchyzer
 *
 */
public class ProvisioningMembership extends ProvisioningUpdatable {

  public boolean isLoggableHelper() {
    if (this.provisioningEntity != null) {
      if (this.provisioningEntity.isLoggable()) {
        return true;
      }
    }
    if (this.provisioningGroup != null) {
      if (this.provisioningGroup.isLoggable()) {
        return true;
      }
    }
    return false;
  }
  
  public boolean isLoggable() {
    ProvisioningMembershipWrapper provisioningMembershipWrapper = this.getProvisioningMembershipWrapper();
    if (provisioningMembershipWrapper != null) {
      return provisioningMembershipWrapper.getProvisioningStateMembership().isLoggable();
    }
    return isLoggableHelper();
  }

  /**
   * see if this object is empty e.g. after translating if empty then dont keep track of group
   * since the translation might have affected another object
   * @return
   */
  public boolean isEmpty() {
    if (StringUtils.isBlank(this.provisioningEntityId)
        && StringUtils.isBlank(this.provisioningGroupId)
        && super.isEmpty()
        && this.provisioningEntity == null 
        && this.provisioningGroup == null) {
      return true;
    }
    return false;
  }

  private String provisioningGroupId;
  
  private String provisioningEntityId;
  
  /**
   * group of memProvisioningGroup*ProvisioningGroup ProvisioningGroup targetGroup;
  
  /**
   * entity of membership
   */
  private ProvisioningEntity provisioningEntity;
  
  private ProvisioningGroup provisioningGroup;

  private ProvisioningMembershipWrapper provisioningMembershipWrapper;

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(ProvisioningMembership.class);
  
  
  
  public ProvisioningMembershipWrapper getProvisioningMembershipWrapper() {
    return provisioningMembershipWrapper;
  }


  
  public void setProvisioningMembershipWrapper(
      ProvisioningMembershipWrapper provisioningMembershipWrapper) {
    this.provisioningMembershipWrapper = provisioningMembershipWrapper;
  }


  /**
   * id of membership (optional)
   * @return id
   */
  public String getId() {
    return this.retrieveAttributeValueString("id");
  }
  
  /**
   * id of membership (optional)
   * @param id1
   */
  public void setId(String id1) {
    this.assignAttributeValue("id", id1);
  }
  
  public ProvisioningGroup getProvisioningGroup() {
    return provisioningGroup;
  }

  
  public void setProvisioningGroup(ProvisioningGroup provisioningGroup) {
    this.provisioningGroup = provisioningGroup;
  }

  

  
  public ProvisioningEntity getProvisioningEntity() {
    return provisioningEntity;
  }


  
  public void setProvisioningEntity(ProvisioningEntity provisioningEntity) {
    this.provisioningEntity = provisioningEntity;
  }


  public String getProvisioningGroupId() {
    return provisioningGroupId;
  }


  
  public void setProvisioningGroupId(String provisioningGroupId) {
    this.provisioningGroupId = provisioningGroupId;
  }


  public String getProvisioningEntityId() {
    return provisioningEntityId;
  }


  
  public void setProvisioningEntityId(String provisioningEntityId) {
    this.provisioningEntityId = provisioningEntityId;
  }
  
//  /**
//   * if you are processing memberships, and you need to stuff those into a group
//   * call this to get that group or create a new one
//   * @return the common provision to target group
//   */
//  public ProvisioningGroup retrieveCommonProvisionToTargetGroup() {
//    try {
//      ProvisioningMembershipWrapper provisioningMembershipWrapper = this.getProvisioningMembershipWrapper();
//      if (provisioningMembershipWrapper == null) {
//        throw new NullPointerException("Cant find provisioningMembershipWrapper: " + this);
//      }
//      
//      ProvisioningMembership commonMembership = GrouperUtil.defaultIfNull(
//          provisioningMembershipWrapper.getGrouperCommonMembership(), 
//          provisioningMembershipWrapper.getTargetCommonMembership());
//      if (commonMembership == null || StringUtils.isBlank(commonMembership.getProvisioningGroupId())) {
//        throw new NullPointerException("Cant find commonMembership: " + this + ", " + commonMembership);
//      }
//      
//      // from common membership there should be a common group wrapper
//      GrouperProvisioner grouperProvisioner = provisioningMembershipWrapper.getGrouperProvisioner();
//      
//      Map<String, ProvisioningGroupWrapper> commonGroupIdToGroupWrapper = grouperProvisioner
//          .getGrouperProvisioningData().getCommonGroupIdToGroupWrapper();
//      if (commonGroupIdToGroupWrapper == null) {
//        commonGroupIdToGroupWrapper = new HashMap<String, ProvisioningGroupWrapper>();
//        grouperProvisioner
//        .getGrouperProvisioningData().setCommonGroupIdToGroupWrapper(commonGroupIdToGroupWrapper);
//      }
//      ProvisioningGroupWrapper provisioningGroupWrapper = commonGroupIdToGroupWrapper.get(commonMembership.getProvisioningGroupId());
//      
//      if (provisioningGroupWrapper == null) {
//        throw new NullPointerException("Cant find provisioningGroupWrapper: " + this);
//      }
//      
//      ProvisioningGroup commonProvisionToTargetGroup = provisioningGroupWrapper.getCommonProvisionToTargetGroup();
//      
//      if (commonProvisionToTargetGroup == null) {
//        // hmmm, we need one :)
//        ProvisioningGroup commonGroup = provisioningGroupWrapper.getCommonGroup();
//        if (commonGroup == null) {
//          throw new NullPointerException("Cant find commonGroup: " + this);
//        }
//        
//        //we need to translate this common group to target
//        List<ProvisioningGroup> commonToTargetGroups = grouperProvisioner.retrieveTranslator().translateCommonToTargetGroups("update", GrouperUtil.toList(commonGroup));
//        if (GrouperUtil.length(commonToTargetGroups) != 1) {
//          throw new RuntimeException("Cant translate common to target group: " + commonGroup + ", " + GrouperUtil.length(commonToTargetGroups));
//        }
//        List<ProvisioningGroup> provisioningGroups = grouperProvisioner.getGrouperProvisioningData().getCommonObjectUpdates().getProvisioningGroups();
//        if (provisioningGroups == null) {
//          provisioningGroups = new ArrayList<ProvisioningGroup>();
//          grouperProvisioner.getGrouperProvisioningData().getCommonObjectUpdates().setProvisioningGroups(provisioningGroups);
//        }
//        commonProvisionToTargetGroup = commonToTargetGroups.get(0);
//        provisioningGroupWrapper.setCommonProvisionToTargetGroup(commonProvisionToTargetGroup);
//        provisioningGroups.add(commonToTargetGroups.get(0));
//      }
//      return commonProvisionToTargetGroup;
//    } catch (RuntimeException re) {
//      LOG.error("error in: " + this, re);
//      throw re;
//    }
//  }
  
//  /**
//   * if you are processing memberships, and you need to stuff those into an entity
//   * call this to get that entity or create a new one
//   * @return the common provision to target entity
//   */
//  public ProvisioningEntity retrieveCommonProvisionToTargetEntity() {
//    try {
//      ProvisioningMembershipWrapper provisioningMembershipWrapper = this.getProvisioningMembershipWrapper();
//      if (provisioningMembershipWrapper == null) {
//        throw new NullPointerException("Cant find provisioningMembershipWrapper: " + this);
//      }
//      
//      ProvisioningMembership commonMembership = GrouperUtil.defaultIfNull(
//          provisioningMembershipWrapper.getGrouperCommonMembership(), 
//          provisioningMembershipWrapper.getTargetCommonMembership());
//      if (commonMembership == null || StringUtils.isBlank(commonMembership.getProvisioningEntityId())) {
//        throw new NullPointerException("Cant find commonMembership: " + this + ", " + commonMembership);
//      }
//      
//      // from common membership there should be a common group wrapper
//      GrouperProvisioner grouperProvisioner = provisioningMembershipWrapper.getGrouperProvisioner();
//      
//      Map<String, ProvisioningEntityWrapper> commonEntityIdToEntityWrapper = grouperProvisioner
//          .getGrouperProvisioningData().getCommonEntityIdToEntityWrapper();
//      if (commonEntityIdToEntityWrapper == null) {
//        commonEntityIdToEntityWrapper = new HashMap<String, ProvisioningEntityWrapper>();
//        grouperProvisioner.getGrouperProvisioningData().setCommonEntityIdToEntityWrapper(commonEntityIdToEntityWrapper);
//      }
//      ProvisioningEntityWrapper provisioningEntityWrapper = commonEntityIdToEntityWrapper.get(commonMembership.getProvisioningEntityId());
//      
//      if (provisioningEntityWrapper == null) {
//        throw new NullPointerException("Cant find provisioningEntityWrapper: " + this);
//      }
//      
//      ProvisioningEntity commonProvisionToTargetEntity = provisioningEntityWrapper.getCommonProvisionToTargetEntity();
//      
//      if (commonProvisionToTargetEntity == null) {
//        // hmmm, we need one :)
//        ProvisioningEntity commonEntity = provisioningEntityWrapper.getCommonEntity();
//        if (commonEntity == null) {
//          throw new NullPointerException("Cant find commonEntity: " + this);
//        }
//        
//        //we need to translate this common group to target
//        List<ProvisioningEntity> commonToTargetEntities = grouperProvisioner.retrieveTranslator().translateCommonToTargetEntities("update", GrouperUtil.toList(commonEntity));
//        if (GrouperUtil.length(commonToTargetEntities) != 1) {
//          throw new RuntimeException("Cant translate common to target entity: " + commonEntity + ", " + GrouperUtil.length(commonToTargetEntities));
//        }
//        List<ProvisioningEntity> provisioningEntitys = grouperProvisioner.getGrouperProvisioningData().getCommonObjectUpdates().getProvisioningEntities();
//        if (provisioningEntitys == null) {
//          provisioningEntitys = new ArrayList<ProvisioningEntity>();
//          grouperProvisioner.getGrouperProvisioningData().getCommonObjectUpdates().setProvisioningEntities(provisioningEntitys);
//        }
//        commonProvisionToTargetEntity = commonToTargetEntities.get(0);
//        provisioningEntityWrapper.setCommonProvisionToTargetEntity(commonProvisionToTargetEntity);
//        provisioningEntitys.add(commonToTargetEntities.get(0));
//      }
//      return commonProvisionToTargetEntity;
//    } catch (RuntimeException re) {
//      LOG.error("error in: " + this, re);
//      throw re;
//    }
//      
//  }
  
  /**
   * 
   * @param name
   * @param value
   */
  public String retrieveAttributeValueString(GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute) {
    
    return GrouperUtil.stringValue(this.retrieveAttributeValue(grouperProvisioningConfigurationAttribute));
    
  }

  
  /**
   * base on attribute get the value
   * @param grouperProvisioningConfigurationAttribute
   * @return the value
   */
  public Object retrieveAttributeValue(
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute) {
    if (grouperProvisioningConfigurationAttribute == null) {
      throw new NullPointerException("attribute is null: " + this);
    }
    return this.retrieveAttributeValueString(grouperProvisioningConfigurationAttribute.getName());
  }

  public String toString() {
    StringBuilder result = new StringBuilder("Mship(");
    boolean firstField = true;
    
    String group = this.provisioningGroup == null ? null : this.provisioningGroup.getName();
    if (group != null) {
      group = GrouperUtil.extensionFromName(group);
    } else if (this.provisioningGroup != null) {
      group = GrouperUtil.stringValue(this.provisioningGroup.getIdIndex());
    }
    firstField = toStringAppendField(result, firstField, "group", group);
    
    String entity = null;
    if (this.provisioningEntity != null) {
      entity = this.provisioningEntity.getLoginId();
      if (StringUtils.isBlank(entity) ) {
        entity = this.provisioningEntity.getEmail();
      }
      if (StringUtils.isBlank(entity) ) {
        entity = this.provisioningEntity.getSubjectId();
      }
      if (StringUtils.isBlank(entity) ) {
        entity = this.provisioningEntity.getName();
      }
    } 
    firstField = toStringAppendField(result, firstField, "entity", entity);
    
    firstField = toStringAppendField(result, firstField, "groupId", this.provisioningGroupId);
    firstField = toStringAppendField(result, firstField, "entityId", this.provisioningEntityId);
    firstField = this.toStringProvisioningUpdatable(result, firstField);
    if (this.provisioningMembershipWrapper != null) {

      if (this.provisioningMembershipWrapper.getProvisioningStateMembership().getGrouperIncrementalDataAction() != null) {
        firstField = toStringAppendField(result, firstField, "action", this.provisioningMembershipWrapper.getProvisioningStateMembership().getGrouperIncrementalDataAction());
      }
      firstField = toStringAppendField(result, firstField, "recalcObject", this.provisioningMembershipWrapper.getProvisioningStateMembership().isRecalcObject());

      if (this.provisioningMembershipWrapper.getProvisioningStateMembership().isCreate()) {
        firstField = toStringAppendField(result, firstField, "create", this.provisioningMembershipWrapper.getProvisioningStateMembership().isCreate());
      }
      if (this.provisioningMembershipWrapper.getProvisioningStateMembership().isInsertResultProcessed()) {
        firstField = toStringAppendField(result, firstField, "createProcessed", this.provisioningMembershipWrapper.getProvisioningStateMembership().isInsertResultProcessed());
      }
      if (this.provisioningMembershipWrapper.getProvisioningStateMembership().isDelete()) {
        firstField = toStringAppendField(result, firstField, "delete", this.provisioningMembershipWrapper.getProvisioningStateMembership().isDelete());
      }
      if (this.provisioningMembershipWrapper.getProvisioningStateMembership().isDeleteResultProcessed()) {
        firstField = toStringAppendField(result, firstField, "deleteProcessed", this.provisioningMembershipWrapper.getProvisioningStateMembership().isDeleteResultProcessed());
      }

      if (this.provisioningMembershipWrapper.getErrorCode() != null) {
        firstField = toStringAppendField(result, firstField, "errorCode", this.provisioningMembershipWrapper.getErrorCode().name());
      }
      if (this.provisioningMembershipWrapper.getProvisioningStateMembership().getMillisSince1970() != null) {
        firstField = toStringAppendField(result, firstField, "millis1970", this.provisioningMembershipWrapper.getProvisioningStateMembership().getMillisSince1970());
      }
      if (this.provisioningMembershipWrapper.getProvisioningStateMembership().isSelectResultProcessed()) {
        firstField = toStringAppendField(result, firstField, "selectProcessed", this.provisioningMembershipWrapper.getProvisioningStateMembership().isSelectResultProcessed());
      }
      if (this.provisioningMembershipWrapper.getProvisioningStateMembership().isUpdate()) {
        firstField = toStringAppendField(result, firstField, "update", this.provisioningMembershipWrapper.getProvisioningStateMembership().isUpdate());
      }
      if (this.provisioningMembershipWrapper.getProvisioningStateMembership().isUpdateResultProcessed()) {
        firstField = toStringAppendField(result, firstField, "updateProcessed", this.provisioningMembershipWrapper.getProvisioningStateMembership().isUpdateResultProcessed());
      }

    }
    return result.append(")").toString();
  }

  /**
   * deep clone the fields in this object
   */
  @Override
  public ProvisioningMembership clone() {

    ProvisioningMembership provisioningMembership = new ProvisioningMembership();

    this.cloneUpdatable(provisioningMembership, null);
//    provisioningMembership.provisioningEntityId = this.provisioningEntityId;
//    provisioningMembership.provisioningEntity = this.provisioningEntity != null ? this.provisioningEntity.clone(): null;
//    provisioningMembership.provisioningGroupId = this.provisioningGroupId;
//    provisioningMembership.provisioningGroup = this.provisioningGroup != null ? this.provisioningGroup.clone(): null;
    provisioningMembership.provisioningMembershipWrapper = this.provisioningMembershipWrapper;
    return provisioningMembership;
  }

  @Override
  public boolean canInsertAttribute(String name) {
    return this.getProvisioningMembershipWrapper().getGrouperProvisioner().retrieveGrouperProvisioningBehavior().canInsertMembershipAttribute(name);
  }

  @Override
  public boolean canUpdateAttribute(String name) {
    return this.getProvisioningMembershipWrapper().getGrouperProvisioner().retrieveGrouperProvisioningBehavior().canUpdateMembershipAttribute(name);
  }

  @Override
  public boolean canDeleteAttribute(String name) {
    return this.getProvisioningMembershipWrapper().getGrouperProvisioner().retrieveGrouperProvisioningBehavior().canUpdateMembershipAttribute(name);
  }

  @Override
  public boolean canDeleteAttributeValue(String name, Object deleteValue) {
    //if can delete attribute name, then all good, assume that has been checked already
    return true;
  }

  @Override
  public String objectTypeName() {
    return "membership";
  }
}
