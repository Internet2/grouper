package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperProvisionerDaoCapabilities {

  /**
   * if doing group attributes, if all memberships can be retrieved with group when the input flag is passed to do so
   */
  private boolean canRetrieveMembershipsWithGroup = false;

  /**
   * if doing entity attributes, if all memberships can be retrieved with entity when the input flag is passed to do so
   */
  private boolean canRetrieveMembershipsWithEntity = false;

  /**
   * if doing entity attributes, if all memberships can be retrieved with entity when the input flag is passed to do so
   * @return
   */
  public boolean isCanRetrieveMembershipsWithEntity() {
    return canRetrieveMembershipsWithEntity;
  }

  /**
   * if doing group attributes, if all memberships can be retrieved with group when the input flag is passed to do so
   * @return
   */
  public boolean isCanRetrieveMembershipsWithGroup() {
    return canRetrieveMembershipsWithGroup;
  }

  /**
   * if doing group attributes, if all memberships can be retrieved with group when the input flag is passed to do so
   * @param canRetrieveMembershipWithGroup
   */
  public void setCanRetrieveMembershipsWithGroup(boolean canRetrieveMembershipWithGroup) {
    this.canRetrieveMembershipsWithGroup = canRetrieveMembershipWithGroup;
  }


  /**
   * if doing entity attributes, if all memberships can be retrieved with entity when the input flag is passed to do so
   * @param canRetrieveMembershipWithEntity
   */
  public void setCanRetrieveMembershipsWithEntity(boolean canRetrieveMembershipWithEntity) {
    this.canRetrieveMembershipsWithEntity = canRetrieveMembershipWithEntity;
  }



  @Override
  public String toString() {
    
    StringBuilder result = new StringBuilder();
    
    Set<String> fieldNames = GrouperUtil.fieldNames(GrouperProvisionerDaoCapabilities.class, null, false);
        
    fieldNames = new TreeSet<String>(fieldNames);
    boolean firstField = true;
    for (String fieldName : fieldNames) {
      
      Object value = GrouperUtil.propertyValue(this, fieldName);
      if (!GrouperUtil.isBlank(value)) {
        
        if ((value instanceof Collection) && ((Collection)value).size() == 0) {
          continue;
        }
        if ((value instanceof Map) && ((Map)value).size() == 0) {
          continue;
        }
        if ((value.getClass().isArray()) && Array.getLength(value) == 0) {
          continue;
        }
        
        if (!firstField) {
          result.append(", ");
        }
        firstField = false;
        result.append(fieldName).append(" = '").append(GrouperUtil.toStringForLog(value, false)).append("'");
      }
    }
    
    return result.toString();
  }

  private Boolean canDeleteEntities;
  
  private Boolean canDeleteEntity;
  
  private Boolean canDeleteGroup;
  
  private Boolean canDeleteGroups;
  
  private Boolean canDeleteMembership;
  
  private Boolean canDeleteMemberships;
  
  private Boolean canInsertEntities;
  private Boolean canInsertEntity;
  private Boolean canInsertGroup;
  private Boolean canInsertGroups;
  private Boolean canInsertMembership;
  private Boolean canInsertMemberships;
  private Boolean canRetrieveAllData;
  private Boolean canRetrieveAllEntities;
  private Boolean canRetrieveAllGroups;
  private Boolean canRetrieveAllMemberships;
  private Boolean canRetrieveEntities;
  private Boolean canRetrieveEntity;
  private Boolean canRetrieveGroup;
  private Boolean canRetrieveGroups;
  private Boolean canRetrieveIncrementalData;
  private Boolean canRetrieveMembership;
  private Boolean canRetrieveMemberships;

  private Boolean canSendChangesToTarget;
  private Boolean canSendEntityChangesToTarget;
  private Boolean canSendGroupChangesToTarget;
  private Boolean canSendMembershipChangesToTarget;
  private Boolean canUpdateEntities;
  private Boolean canUpdateEntity;
  private Boolean canUpdateGroup;
  private Boolean canUpdateGroups;
  private Boolean canUpdateEntityMembershipAttribute;
  private Boolean canUpdateGroupMembershipAttribute;
  private Boolean canUpdateMembership;
  private Boolean canUpdateMemberships;
  private Boolean canReplaceGroupMemberships;
  
  public Boolean getCanReplaceGroupMemberships() {
    return canReplaceGroupMemberships;
  }
  
  public void setCanReplaceGroupMemberships(Boolean canReplaceGroupMemberships) {
    this.canReplaceGroupMemberships = canReplaceGroupMemberships;
  }

  public Boolean getCanDeleteEntities() {
    return canDeleteEntities;
  }
  
  public void setCanDeleteEntities(Boolean canDeleteEntities) {
    this.canDeleteEntities = canDeleteEntities;
  }
  
  public Boolean getCanDeleteEntity() {
    return canDeleteEntity;
  }
  
  public void setCanDeleteEntity(Boolean canDeleteEntity) {
    this.canDeleteEntity = canDeleteEntity;
  }
  
  public Boolean getCanDeleteGroup() {
    return canDeleteGroup;
  }
  
  public void setCanDeleteGroup(Boolean canDeleteGroup) {
    this.canDeleteGroup = canDeleteGroup;
  }
  
  /**
   * batch size in dao should correspond to how many items of work can be handled at once which will be batched for threads
   * i.e. for databases its a batch size of 1000, for azure its a batch size of 20, for LDAP its a batch size of 1
   */
  private int deleteMembershipsBatchSize = -1;

  /**
   * batch size in dao should correspond to how many items of work can be handled at once which will be batched for threads
   * i.e. for databases its a batch size of 1000, for azure its a batch size of 20, for LDAP its a batch size of 1
   * @return size
   */
  public int getDeleteMembershipsBatchSize() {
    return this.deleteMembershipsBatchSize == -1 ? this.defaultBatchSize : this.deleteMembershipsBatchSize;
  }

  /**
   * batch size in dao should correspond to how many items of work can be handled at once which will be batched for threads
   * i.e. for databases its a batch size of 1000, for azure its a batch size of 20, for LDAP its a batch size of 1
   * @param deleteMembershipsBatchSize1
   */
  public void setDeleteMembershipsBatchSize(int deleteMembershipsBatchSize1) {
    this.deleteMembershipsBatchSize = deleteMembershipsBatchSize1;
  }

  /**
   * batch size in dao should correspond to how many items of work can be handled at once which will be batched for threads
   * i.e. for databases its a batch size of 1000, for azure its a batch size of 20, for LDAP its a batch size of 1
   */
  private int updateMembershipsBatchSize = -1;

  /**
   * batch size in dao should correspond to how many items of work can be handled at once which will be batched for threads
   * i.e. for databases its a batch size of 1000, for azure its a batch size of 20, for LDAP its a batch size of 1
   * @return size
   */
  public int getUpdateMembershipsBatchSize() {
    return this.updateMembershipsBatchSize == -1 ? this.defaultBatchSize : this.updateMembershipsBatchSize;
  }

  /**
   * batch size in dao should correspond to how many items of work can be handled at once which will be batched for threads
   * i.e. for databases its a batch size of 1000, for azure its a batch size of 20, for LDAP its a batch size of 1
   * @param updateMembershipsBatchSize1
   */
  public void setUpdateMembershipsBatchSize(int updateMembershipsBatchSize1) {
    this.updateMembershipsBatchSize = updateMembershipsBatchSize1;
  }

  /**
   * batch size in dao should correspond to how many items of work can be handled at once which will be batched for threads
   * i.e. for databases its a batch size of 1000, for azure its a batch size of 20, for LDAP its a batch size of 1
   */
  private int insertMembershipsBatchSize = -1;

  /**
   * batch size in dao should correspond to how many items of work can be handled at once which will be batched for threads
   * i.e. for databases its a batch size of 1000, for azure its a batch size of 20, for LDAP its a batch size of 1
   * @return size
   */
  public int getInsertMembershipsBatchSize() {
    return this.insertMembershipsBatchSize == -1 ? this.defaultBatchSize : this.insertMembershipsBatchSize;
  }

  /**
   * batch size in dao should correspond to how many items of work can be handled at once which will be batched for threads
   * i.e. for databases its a batch size of 1000, for azure its a batch size of 20, for LDAP its a batch size of 1
   * @param insertMembershipsBatchSize1
   */
  public void setInsertMembershipsBatchSize(int insertMembershipsBatchSize1) {
    this.insertMembershipsBatchSize = insertMembershipsBatchSize1;
  }

  /**
   * batch size in dao should correspond to how many items of work can be handled at once which will be batched for threads
   * i.e. for databases its a batch size of 1000, for azure its a batch size of 20, for LDAP its a batch size of 1
   */
  private int insertGroupsBatchSize = -1;

  /**
   * batch size in dao should correspond to how many items of work can be handled at once which will be batched for threads
   * i.e. for databases its a batch size of 1000, for azure its a batch size of 20, for LDAP its a batch size of 1
   * @return size
   */
  public int getInsertGroupsBatchSize() {
    return this.insertGroupsBatchSize == -1 ? this.defaultBatchSize : this.insertGroupsBatchSize;
  }

  /**
   * batch size in dao should correspond to how many items of work can be handled at once which will be batched for threads
   * i.e. for databases its a batch size of 1000, for azure its a batch size of 20, for LDAP its a batch size of 1
   * @param insertGroupsBatchSize1
   */
  public void setInsertGroupsBatchSize(int insertGroupsBatchSize1) {
    this.insertGroupsBatchSize = insertGroupsBatchSize1;
  }

  /**
   * batch size in dao should correspond to how many items of work can be handled at once which will be batched for threads
   * i.e. for databases its a batch size of 1000, for azure its a batch size of 20, for LDAP its a batch size of 1
   */
  private int updateGroupsBatchSize = -1;

  /**
   * batch size in dao should correspond to how many items of work can be handled at once which will be batched for threads
   * i.e. for databases its a batch size of 1000, for azure its a batch size of 20, for LDAP its a batch size of 1
   * @return size
   */
  public int getUpdateGroupsBatchSize() {
    return this.updateGroupsBatchSize == -1 ? this.defaultBatchSize : this.updateGroupsBatchSize;
  }

  /**
   * batch size in dao should correspond to how many items of work can be handled at once which will be batched for threads
   * i.e. for databases its a batch size of 1000, for azure its a batch size of 20, for LDAP its a batch size of 1
   * @param updateGroupsBatchSize1
   */
  public void setUpdateGroupsBatchSize(int updateGroupsBatchSize1) {
    this.updateGroupsBatchSize = updateGroupsBatchSize1;
  }

  /**
   * batch size in dao should correspond to how many items of work can be handled at once which will be batched for threads
   * i.e. for databases its a batch size of 1000, for azure its a batch size of 20, for LDAP its a batch size of 1
   */
  private int updateEntitiesBatchSize = -1;
  
  /**
   * batch size in dao should correspond to how many items of work can be handled at once which will be batched for threads
   * i.e. for databases its a batch size of 1000, for azure its a batch size of 20, for LDAP its a batch size of 1
   * @return update entities batch size
   */
  public int getUpdateEntitiesBatchSize() {
    return this.updateEntitiesBatchSize == -1 ? this.defaultBatchSize : this.updateEntitiesBatchSize;
  }

  /**
   * batch size in dao should correspond to how many items of work can be handled at once which will be batched for threads
   * i.e. for databases its a batch size of 1000, for azure its a batch size of 20, for LDAP its a batch size of 1
   * @param updateEntitiesBatchSize1
   */
  public void setUpdateEntitiesBatchSize(int updateEntitiesBatchSize1) {
    this.updateEntitiesBatchSize = updateEntitiesBatchSize1;
  }

  /**
   * batch size in dao should correspond to how many items of work can be handled at once which will be batched for threads
   * i.e. for databases its a batch size of 1000, for azure its a batch size of 20, for LDAP its a batch size of 1
   */
  private int insertEntitiesBatchSize = -1;

  /**
   * batch size in dao should correspond to how many items of work can be handled at once which will be batched for threads
   * i.e. for databases its a batch size of 1000, for azure its a batch size of 20, for LDAP its a batch size of 1
   * @return insert entities batch size
   */
  public int getInsertEntitiesBatchSize() {
    return this.insertEntitiesBatchSize == -1 ? this.defaultBatchSize : this.insertEntitiesBatchSize;
  }

  /**
   * batch size in dao should correspond to how many items of work can be handled at once which will be batched for threads
   * i.e. for databases its a batch size of 1000, for azure its a batch size of 20, for LDAP its a batch size of 1
   * @param insertEntitiesBatchSize1
   */
  public void setInsertEntitiesBatchSize(int insertEntitiesBatchSize1) {
    this.insertEntitiesBatchSize = insertEntitiesBatchSize1;
  }

  /**
   * batch size in dao should correspond to how many items of work can be handled at once which will be batched for threads
   * i.e. for databases its a batch size of 1000, for azure its a batch size of 20, for LDAP its a batch size of 1
   */
  private int retrieveMembershipsBatchSize = -1;

  /**
   * batch size in dao should correspond to how many items of work can be handled at once which will be batched for threads
   * i.e. for databases its a batch size of 1000, for azure its a batch size of 20, for LDAP its a batch size of 1
   * @return size
   */
  public int getRetrieveMembershipsBatchSize() {
    return this.retrieveMembershipsBatchSize == -1 ? this.defaultBatchSize : this.retrieveMembershipsBatchSize;
  }

  /**
   * batch size in dao should correspond to how many items of work can be handled at once which will be batched for threads
   * i.e. for databases its a batch size of 1000, for azure its a batch size of 20, for LDAP its a batch size of 1
   * @param selectMembershipsBatchSize1
   */
  public void setRetrieveMembershipsBatchSize(int selectMembershipsBatchSize1) {
    this.retrieveMembershipsBatchSize = selectMembershipsBatchSize1;
  }

  /**
   * batch size in dao should correspond to how many items of work can be handled at once which will be batched for threads
   * i.e. for databases its a batch size of 1000, for azure its a batch size of 20, for LDAP its a batch size of 1
   */
  private int retrieveGroupsBatchSize = -1;

  /**
   * batch size in dao should correspond to how many items of work can be handled at once which will be batched for threads
   * i.e. for databases its a batch size of 1000, for azure its a batch size of 20, for LDAP its a batch size of 1
   * @return size
   */
  public int getRetrieveGroupsBatchSize() {
    return this.retrieveGroupsBatchSize == -1 ? this.defaultBatchSize : this.retrieveGroupsBatchSize;
  }

  /**
   * batch size in dao should correspond to how many items of work can be handled at once which will be batched for threads
   * i.e. for databases its a batch size of 1000, for azure its a batch size of 20, for LDAP its a batch size of 1
   * @param selectGroupsBatchSize1
   */
  public void setRetrieveGroupsBatchSize(int selectGroupsBatchSize1) {
    this.retrieveGroupsBatchSize = selectGroupsBatchSize1;
  }

  /**
   * batch size in dao should correspond to how many items of work can be handled at once which will be batched for threads
   * i.e. for databases its a batch size of 1000, for azure its a batch size of 20, for LDAP its a batch size of 1
   */
  private int retrieveEntitiesBatchSize = -1;

  /**
   * batch size in dao should correspond to how many items of work can be handled at once which will be batched for threads
   * i.e. for databases its a batch size of 1000, for azure its a batch size of 20, for LDAP its a batch size of 1
   * @return size
   */
  public int getRetrieveEntitiesBatchSize() {
    return this.retrieveEntitiesBatchSize == -1 ? this.defaultBatchSize : this.retrieveEntitiesBatchSize;
  }

  /**
   * batch size in dao should correspond to how many items of work can be handled at once which will be batched for threads
   * i.e. for databases its a batch size of 1000, for azure its a batch size of 20, for LDAP its a batch size of 1
   * @param selectEntitiesBatchSize1
   */
  public void setRetrieveEntitiesBatchSize(int selectEntitiesBatchSize1) {
    this.retrieveEntitiesBatchSize = selectEntitiesBatchSize1;
  }

  /**
   * default batch size will be used for all batch sizes unless overridden
   */
  private int defaultBatchSize = 20;

  /**
   * default batch size will be used for all batch sizes unless overridden
   * @return default batch size
   */
  public int getDefaultBatchSize() {
    return this.defaultBatchSize;
  }

  /**
   * default batch size will be used for all batch sizes unless overridden
   * @param defaultBatchSize1
   */
  public void setDefaultBatchSize(int defaultBatchSize1) {
    this.defaultBatchSize = defaultBatchSize1;
  }

  /**
   * batch size in dao should correspond to how many items of work can be handled at once which will be batched for threads
   * i.e. for databases its a batch size of 1000, for azure its a batch size of 20, for LDAP its a batch size of 1
   */
  private int deleteEntitiesBatchSize = -1;

  /**
   * batch size in dao should correspond to how many items of work can be handled at once which will be batched for threads
   * i.e. for databases its a batch size of 1000, for azure its a batch size of 20, for LDAP its a batch size of 1
   * @return size
   */
  public int getDeleteEntitiesBatchSize() {
    return this.deleteEntitiesBatchSize == -1 ? this.defaultBatchSize : this.deleteEntitiesBatchSize;
  }

  /**
   * batch size in dao should correspond to how many items of work can be handled at once which will be batched for threads
   * i.e. for databases its a batch size of 1000, for azure its a batch size of 20, for LDAP its a batch size of 1
   * @param deleteEntitiesBatchSize1
   */
  public void setDeleteEntitiesBatchSize(int deleteEntitiesBatchSize1) {
    this.deleteEntitiesBatchSize = deleteEntitiesBatchSize1;
  }

  /**
   * batch size in dao should correspond to how many items of work can be handled at once which will be batched for threads
   * i.e. for databases its a batch size of 1000, for azure its a batch size of 20, for LDAP its a batch size of 1
   */
  private int deleteGroupsBatchSize = -1;

  /**
   * batch size in dao should correspond to how many items of work can be handled at once which will be batched for threads
   * i.e. for databases its a batch size of 1000, for azure its a batch size of 20, for LDAP its a batch size of 1
   * @return delete group batch size
   */
  public int getDeleteGroupsBatchSize() {
    return this.deleteGroupsBatchSize == -1 ? this.defaultBatchSize : this.deleteGroupsBatchSize;
  }

  /**
   * batch size in dao should correspond to how many items of work can be handled at once which will be batched for threads
   * i.e. for databases its a batch size of 1000, for azure its a batch size of 20, for LDAP its a batch size of 1
   * @param deleteGroupsBatchSize1
   */
  public void setDeleteGroupsBatchSize(int deleteGroupsBatchSize1) {
    this.deleteGroupsBatchSize = deleteGroupsBatchSize1;
  }

  public Boolean getCanDeleteGroups() {
    return canDeleteGroups;
  }
  
  public void setCanDeleteGroups(Boolean canDeleteGroups) {
    this.canDeleteGroups = canDeleteGroups;
  }
  
  public Boolean getCanDeleteMembership() {
    return canDeleteMembership;
  }
  
  public void setCanDeleteMembership(Boolean canDeleteMembership) {
    this.canDeleteMembership = canDeleteMembership;
  }
  
  public Boolean getCanDeleteMemberships() {
    return canDeleteMemberships;
  }
  
  public void setCanDeleteMemberships(Boolean canDeleteMemberships) {
    this.canDeleteMemberships = canDeleteMemberships;
  }
  
  public Boolean getCanInsertEntities() {
    return canInsertEntities;
  }
  
  public void setCanInsertEntities(Boolean canInsertEntities) {
    this.canInsertEntities = canInsertEntities;
  }
  
  public Boolean getCanInsertEntity() {
    return canInsertEntity;
  }
  
  public void setCanInsertEntity(Boolean canInsertEntity) {
    this.canInsertEntity = canInsertEntity;
  }
  
  public Boolean getCanInsertGroup() {
    return canInsertGroup;
  }
  
  public void setCanInsertGroup(Boolean canInsertGroup) {
    this.canInsertGroup = canInsertGroup;
  }
  
  public Boolean getCanInsertGroups() {
    return canInsertGroups;
  }
  
  public void setCanInsertGroups(Boolean canInsertGroups) {
    this.canInsertGroups = canInsertGroups;
  }
  
  public Boolean getCanInsertMembership() {
    return canInsertMembership;
  }
  
  public void setCanInsertMembership(Boolean canInsertMembership) {
    this.canInsertMembership = canInsertMembership;
  }
  
  public Boolean getCanInsertMemberships() {
    return canInsertMemberships;
  }
  
  public void setCanInsertMemberships(Boolean canInsertMemberships) {
    this.canInsertMemberships = canInsertMemberships;
  }
  
  public Boolean getCanRetrieveAllData() {
    return canRetrieveAllData;
  }
  
  public void setCanRetrieveAllData(Boolean canRetrieveAllData) {
    this.canRetrieveAllData = canRetrieveAllData;
  }
  
  public Boolean getCanRetrieveAllEntities() {
    return canRetrieveAllEntities;
  }
  
  public void setCanRetrieveAllEntities(Boolean canRetrieveAllEntities) {
    this.canRetrieveAllEntities = canRetrieveAllEntities;
  }
  
  public Boolean getCanRetrieveAllGroups() {
    return canRetrieveAllGroups;
  }
  
  public void setCanRetrieveAllGroups(Boolean canRetrieveAllGroups) {
    this.canRetrieveAllGroups = canRetrieveAllGroups;
  }
  
  public Boolean getCanRetrieveAllMemberships() {
    return canRetrieveAllMemberships;
  }
  
  public void setCanRetrieveAllMemberships(Boolean canRetrieveAllMemberships) {
    this.canRetrieveAllMemberships = canRetrieveAllMemberships;
  }
  
  public Boolean getCanRetrieveEntities() {
    return canRetrieveEntities;
  }
  
  public void setCanRetrieveEntities(Boolean canRetrieveEntities) {
    this.canRetrieveEntities = canRetrieveEntities;
  }
  
  public Boolean getCanRetrieveEntity() {
    return canRetrieveEntity;
  }
  
  public void setCanRetrieveEntity(Boolean canRetrieveEntity) {
    this.canRetrieveEntity = canRetrieveEntity;
  }
  
  public Boolean getCanRetrieveGroup() {
    return canRetrieveGroup;
  }
  
  public void setCanRetrieveGroup(Boolean canRetrieveGroup) {
    this.canRetrieveGroup = canRetrieveGroup;
  }
  
  public Boolean getCanRetrieveGroups() {
    return canRetrieveGroups;
  }
  
  public void setCanRetrieveGroups(Boolean canRetrieveGroups) {
    this.canRetrieveGroups = canRetrieveGroups;
  }
  
  public Boolean getCanRetrieveIncrementalData() {
    return canRetrieveIncrementalData;
  }
  
  public void setCanRetrieveIncrementalData(Boolean canRetrieveIncrementalData) {
    this.canRetrieveIncrementalData = canRetrieveIncrementalData;
  }
  
  public Boolean getCanRetrieveMembership() {
    return canRetrieveMembership;
  }
  
  public void setCanRetrieveMembership(Boolean canRetrieveMembership) {
    this.canRetrieveMembership = canRetrieveMembership;
  }
  
  public Boolean getCanRetrieveMemberships() {
    return canRetrieveMemberships;
  }
  
  public void setCanRetrieveMemberships(Boolean canRetrieveMemberships) {
    this.canRetrieveMemberships = canRetrieveMemberships;
  }

  /**
   * if entity attributes, and retrieve memberships can have multiple entities each with one memberships attribute
   */
  private Boolean canRetrieveMembershipOneByEntities;

  /**
   * if entity attributes, and retrieve memberships can have multiple entities each with one memberships attribute
   * @return
   */
  public Boolean getCanRetrieveMembershipOneByEntities() {
    return canRetrieveMembershipOneByEntities;
  }

  /**
   * if entity attributes, and retrieve memberships can have multiple entities each with one memberships attribute
   * @param canRetrieveMembershipByEntities
   */
  public void setCanRetrieveMembershipOneByEntities(Boolean canRetrieveMembershipOneByEntities) {
    this.canRetrieveMembershipOneByEntities = canRetrieveMembershipOneByEntities;
  }

  /**
   * if entity attributes, and retrieve membership can have one entity with one membership attribute
   */
  private Boolean canRetrieveMembershipOneByEntity;
  
  /**
   * if entity attributes, and retrieve membership can have one entity with one membership attribute
   * @return
   */
  public Boolean getCanRetrieveMembershipOneByEntity() {
    return canRetrieveMembershipOneByEntity;
  }

  /**
   * if entity attributes, and retrieve membership can have one entity with one membership attribute
   * @param canRetrieveMembershipByEntity
   */
  public void setCanRetrieveMembershipOneByEntity(Boolean canRetrieveMembershipByEntity) {
    this.canRetrieveMembershipOneByEntity = canRetrieveMembershipByEntity;
  }

  /**
   * if group attributes, and retrieve membership can have one group with one membership attribute
   */
  private Boolean canRetrieveMembershipOneByGroup;

  /**
   * if group attributes, and retrieve membership can have one group with one membership attribute
   * @return
   */
  public Boolean getCanRetrieveMembershipOneByGroup() {
    return canRetrieveMembershipOneByGroup;
  }

  /**
   * if group attributes, and retrieve membership can have one group with one membership attribute
   * @param canRetrieveMembershipByGroup
   */
  public void setCanRetrieveMembershipOneByGroup(Boolean canRetrieveMembershipOneByGroup) {
    this.canRetrieveMembershipOneByGroup = canRetrieveMembershipOneByGroup;
  }

  /**
   * if group attributes, and retrieve memberships can have multiple groups each with one membership attribute
   */
  private Boolean canRetrieveMembershipOneByGroups;
  
  /**
   * if group attributes, and retrieve memberships can have multiple groups each with one membership attribute
   * @return
   */
  public Boolean getCanRetrieveMembershipOneByGroups() {
    return canRetrieveMembershipOneByGroups;
  }

  /**
   * if group attributes, and retrieve memberships can have multiple groups each with one membership attribute
   * @param canRetrieveMembershipByGroups
   */
  public void setCanRetrieveMembershipOneByGroups(Boolean canRetrieveMembershipOneByGroups) {
    this.canRetrieveMembershipOneByGroups = canRetrieveMembershipOneByGroups;
  }

  /**
   * if can get all memberships for one group by implementing method: retrieveMembershipsByGroup()
   */
  private Boolean canRetrieveMembershipsAllByGroup;

  /**
   * if can get all memberships for one group by implementing method: retrieveMembershipsByGroup()
   * @return
   */
  public Boolean getCanRetrieveMembershipsAllByGroup() {
    return canRetrieveMembershipsAllByGroup;
  }

  /**
   * if can get all memberships for one group by implementing method: retrieveMembershipsByGroup()
   * @param canRetrieveMembershipsAllByGroup
   */
  public void setCanRetrieveMembershipsAllByGroup(
      Boolean canRetrieveMembershipsAllByGroup) {
    this.canRetrieveMembershipsAllByGroup = canRetrieveMembershipsAllByGroup;
  }

  /**
   * if can get all memberships for multiple groups by implementing method: retrieveMembershipsByGroups()
   */
  private Boolean canRetrieveMembershipsAllByGroups;

  
  
  /**
   * if can get all memberships for multiple groups by implementing method: retrieveMembershipsByGroups()
   * @return
   */
  public Boolean getCanRetrieveMembershipsAllByGroups() {
    return canRetrieveMembershipsAllByGroups;
  }

  /**
   * if can get all memberships for multiple groups by implementing method: retrieveMembershipsByGroups()
   * @param canRetrieveMembershipsAllByGroups
   */
  public void setCanRetrieveMembershipsAllByGroups(
      Boolean canRetrieveMembershipsAllByGroups) {
    this.canRetrieveMembershipsAllByGroups = canRetrieveMembershipsAllByGroups;
  }

  /**
   * if can get all memberships for multiple entites by implementing method: retrieveMembershipsByEntities()
   */
  private Boolean canRetrieveMembershipsAllByEntities;

  /**
   * if can get all memberships for multiple entites by implementing method: retrieveMembershipsByEntities()
   * @return
   */
  public Boolean getCanRetrieveMembershipsAllByEntities() {
    return canRetrieveMembershipsAllByEntities;
  }

  /**
   * if can get all memberships for multiple entites by implementing method: retrieveMembershipsByEntities()
   * @param canRetrieveMembershipsAllByEntities
   */
  public void setCanRetrieveMembershipsAllByEntities(
      Boolean canRetrieveMembershipsAllByEntities) {
    this.canRetrieveMembershipsAllByEntities = canRetrieveMembershipsAllByEntities;
  }

  /**
   * if can get all memberships for one entity by implementing method: retrieveMembershipsByEntity()
   */
  private Boolean canRetrieveMembershipsAllByEntity;


  /**
   * if can get all memberships for one entity by implementing method: retrieveMembershipsByEntity()
   * @return
   */
  public Boolean getCanRetrieveMembershipsAllByEntity() {
    return canRetrieveMembershipsAllByEntity;
  }

  /**
   * if can get all memberships for one entity by implementing method: retrieveMembershipsByEntity()
   * @param canRetrieveMembershipsAllByEntity
   */
  public void setCanRetrieveMembershipsAllByEntity(
      Boolean canRetrieveMembershipsAllByEntity) {
    this.canRetrieveMembershipsAllByEntity = canRetrieveMembershipsAllByEntity;
  }

  /**
   * if entity attributes, if select memberships can take multiple entities and multiple membership attributes
   */
  private Boolean canRetrieveMembershipsSomeByEntities;

  /**
   * if entity attributes, if select memberships can take multiple entities and multiple membership attributes
   * @return
   */
  public Boolean getCanRetrieveMembershipsSomeByEntities() {
    return canRetrieveMembershipsSomeByEntities;
  }
  
  /**
   * if entity attributes, if select memberships can take multiple entities and multiple membership attributes
   * @param canRetrieveMembershipsByEntities
   */
  public void setCanRetrieveMembershipsSomeByEntities(
      Boolean canRetrieveMembershipsSomeByEntities) {
    this.canRetrieveMembershipsSomeByEntities = canRetrieveMembershipsSomeByEntities;
  }

  /**
   * if entity attributes, if select membership can take one entity and multiple membership attributes
   */
  private Boolean canRetrieveMembershipsSomeByEntity;

  /**
   * if entity attributes, if select membership can take one entity and multiple membership attributes
   * @return
   */
  public Boolean getCanRetrieveMembershipsSomeByEntity() {
    return canRetrieveMembershipsSomeByEntity;
  }

  /**
   * if entity attributes, if select membership can take one entity and multiple membership attributes
   * @param canRetrieveMembershipsByEntity
   */
  public void setCanRetrieveMembershipsSomeByEntity(Boolean canRetrieveMembershipsSomeByEntity) {
    this.canRetrieveMembershipsSomeByEntity = canRetrieveMembershipsSomeByEntity;
  }
  
  /**
   * if group attributes, if select membership can take one group and multiple membership attributes
   */
  private Boolean canRetrieveMembershipsSomeByGroup;

  /**
   * if group attributes, if select membership can take one group and multiple membership attributes
   * @return
   */
  public Boolean getCanRetrieveMembershipsSomeByGroup() {
    return canRetrieveMembershipsSomeByGroup;
  }

  /**
   * if group attributes, if select membership can take one group and multiple membership attributes
   * @param canRetrieveMembershipsByGroup
   */
  public void setCanRetrieveMembershipsSomeByGroup(Boolean canRetrieveMembershipsSomeByGroup) {
    this.canRetrieveMembershipsSomeByGroup = canRetrieveMembershipsSomeByGroup;
  }

  /**
   * if group attributes, if select memberships can take multiple groups and multiple membership attributes
   */
  private Boolean canRetrieveMembershipsSomeByGroups;

  /**
   * if group attributes, if select memberships can take multiple groups and multiple membership attributes
   * @return
   */
  public Boolean getCanRetrieveMembershipsSomeByGroups() {
    return canRetrieveMembershipsSomeByGroups;
  }

  /**
   * if group attributes, if select memberships can take multiple groups and multiple membership attributes
   * @param canRetrieveMembershipsByGroups
   */
  public void setCanRetrieveMembershipsSomeByGroups(Boolean canRetrieveMembershipsSomeByGroups) {
    this.canRetrieveMembershipsSomeByGroups = canRetrieveMembershipsSomeByGroups;
  }
  

  public Boolean getCanSendChangesToTarget() {
    return canSendChangesToTarget;
  }
  
  public void setCanSendChangesToTarget(Boolean canSendChangesToTarget) {
    this.canSendChangesToTarget = canSendChangesToTarget;
  }
  
  public Boolean getCanSendEntityChangesToTarget() {
    return canSendEntityChangesToTarget;
  }
  
  public void setCanSendEntityChangesToTarget(Boolean canSendEntityChangesToTarget) {
    this.canSendEntityChangesToTarget = canSendEntityChangesToTarget;
  }
  
  public Boolean getCanSendGroupChangesToTarget() {
    return canSendGroupChangesToTarget;
  }
  
  public void setCanSendGroupChangesToTarget(Boolean canSendGroupChangesToTarget) {
    this.canSendGroupChangesToTarget = canSendGroupChangesToTarget;
  }
  
  public Boolean getCanSendMembershipChangesToTarget() {
    return canSendMembershipChangesToTarget;
  }
  
  public void setCanSendMembershipChangesToTarget(
      Boolean canSendMembershipChangesToTarget) {
    this.canSendMembershipChangesToTarget = canSendMembershipChangesToTarget;
  }
  
  public Boolean getCanUpdateEntities() {
    return canUpdateEntities;
  }
  
  public void setCanUpdateEntities(Boolean canUpdateEntities) {
    this.canUpdateEntities = canUpdateEntities;
  }
  
  public Boolean getCanUpdateEntity() {
    return canUpdateEntity;
  }
  
  public void setCanUpdateEntity(Boolean canUpdateEntity) {
    this.canUpdateEntity = canUpdateEntity;
  }
  
  public Boolean getCanUpdateGroup() {
    return canUpdateGroup;
  }
  
  public void setCanUpdateGroup(Boolean canUpdateGroup) {
    this.canUpdateGroup = canUpdateGroup;
  }
  
  public Boolean getCanUpdateGroups() {
    return canUpdateGroups;
  }
  
  public void setCanUpdateGroups(Boolean canUpdateGroups) {
    this.canUpdateGroups = canUpdateGroups;
  }
  
  public Boolean getCanUpdateEntityMembershipAttribute() {
    return canUpdateEntityMembershipAttribute;
  }
  
  public void setCanUpdateEntityMembershipAttribute(
      Boolean canUpdateEntityMembershipAttribute) {
    this.canUpdateEntityMembershipAttribute = canUpdateEntityMembershipAttribute;
  }
  
  public Boolean getCanUpdateGroupMembershipAttribute() {
    return canUpdateGroupMembershipAttribute;
  }
  
  public void setCanUpdateGroupMembershipAttribute(
      Boolean canUpdateGroupMembershipAttribute) {
    this.canUpdateGroupMembershipAttribute = canUpdateGroupMembershipAttribute;
  }
  
  public Boolean getCanUpdateMembership() {
    return canUpdateMembership;
  }
  
  public void setCanUpdateMembership(Boolean canUpdateMembership) {
    this.canUpdateMembership = canUpdateMembership;
  }
  
  public Boolean getCanUpdateMemberships() {
    return canUpdateMemberships;
  }
  
  public void setCanUpdateMemberships(Boolean canUpdateMemberships) {
    this.canUpdateMemberships = canUpdateMemberships;
  }
  
  
}
