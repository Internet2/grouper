package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.tableIndex.TableIndex;
import edu.internet2.middleware.grouper.tableIndex.TableIndexType;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDependencyGroupGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDependencyGroupUser;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncErrorCode;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * @author shilen
 */
public class GrouperProvisioningTranslator {
  
  /**
   * of the groups used in translation (user or group), this is a mapping of group name to group id.  
   * if the group id is null and it was deleted then should return no privileges/memberships
   * if the group id is null and never existed then it is probably misconfigured and will return no members and will log the group with issue
   */
  private Map<String, String> groupNameToGroupId = new HashMap<>();
  
  /**
   * delete user dependencies no longer needed in full sync (if not removed, delete them).  value is id index to delete
   */
  private Map<MultiKey, Long> groupIdFieldIdUserDependenciesToDelete = new HashMap<>();
  
  /**
   * delete group dependencies no longer needed in full sync (if not removed, delete them).  value is id index to delete
   */
  private Map<MultiKey, Long> groupIdFieldIdDependentGroupIdGroupDependenciesToDelete = new HashMap<>();
  
  
  public void retrieveAllDependenciesForFullSync() {

    List<GcGrouperSyncDependencyGroupUser> gcGrouperSyncDependencyGroupUsers = 
        this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncDependencyGroupUserDao().internal_dependencyGroupUserRetrieveFromDbAll();
    
    if (GrouperUtil.length(gcGrouperSyncDependencyGroupUsers) > 0) {
      
      for (GcGrouperSyncDependencyGroupUser gcGrouperSyncDependencyGroupUser : gcGrouperSyncDependencyGroupUsers) {
        
        MultiKey groupIdFieldId = new MultiKey(gcGrouperSyncDependencyGroupUser.getGroupId(), gcGrouperSyncDependencyGroupUser.getFieldId());
        groupIdFieldIdUserDependenciesToDelete.put(groupIdFieldId, gcGrouperSyncDependencyGroupUser.getIdIndex());
        
        groupIdFieldIdLookedUpForGroupUserDependencies.put(groupIdFieldId, false);
      }
      
    }
    
    List<GcGrouperSyncDependencyGroupGroup> gcGrouperSyncDependencyGroupGroups = 
        this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncDependencyGroupGroupDao().internal_dependencyGroupGroupRetrieveFromDbAll();

    if (GrouperUtil.length(gcGrouperSyncDependencyGroupGroups) > 0) {
      
      for (GcGrouperSyncDependencyGroupGroup gcGrouperSyncDependencyGroupGroup : gcGrouperSyncDependencyGroupGroups) {
        
        MultiKey groupIdFieldIdDependentGroupId = new MultiKey(gcGrouperSyncDependencyGroupGroup.getGroupId(), gcGrouperSyncDependencyGroupGroup.getFieldId(), 
            gcGrouperSyncDependencyGroupGroup.getProvisionableGroupId());
        groupIdFieldIdDependentGroupIdGroupDependenciesToDelete.put(
            groupIdFieldIdDependentGroupId, gcGrouperSyncDependencyGroupGroup.getIdIndex());
        
        MultiKey groupIdFieldId = new MultiKey(gcGrouperSyncDependencyGroupGroup.getGroupId(), gcGrouperSyncDependencyGroupGroup.getFieldId());
        groupIdFieldIdLookedUpForGroupGroupDependencies.put(groupIdFieldId, false);
        
      }
      
    }
    
    initGroupsMembershipsOrCache();    
  }
  
  /**
   * this is for group translation, group id and field id to their identifiers
   */
  private Map<MultiKey, Set<MultiKey>> groupIdFieldIdToSubjectIdAndEmailAndIdentifiers = new HashMap<>();

  /**
   * this is for user translation, group id and field id to if has been looked up.  if false then it needs a look up
   */
  private Map<MultiKey, Boolean> groupIdFieldIdLookedUpForGroupUserDependencies = new HashMap<>();
  
  /**
   * this is for group translation, group id and field id to if has been looked up.  if false then it needs a look up
   */
  private Map<MultiKey, Boolean> groupIdFieldIdLookedUpForGroupGroupDependencies = new HashMap<>();

  /**
   * this is for user translation, group id and field id to if has been looked up.  if false then it needs a look up
   * @return the map
   */
  public Map<MultiKey, Boolean> getGroupIdFieldIdLookedUpForGroupUserDependencies() {
    return groupIdFieldIdLookedUpForGroupUserDependencies;
  }


  /**
   * this is for group translation, group id and field id to if has been looked up.  if false then it needs a look up
   * @return
   */
  public Map<MultiKey, Boolean> getGroupIdFieldIdLookedUpForGroupGroupDependencies() {
    return groupIdFieldIdLookedUpForGroupGroupDependencies;
  }

  /**
   * this is for user translation, group id and field id to member id
   */
  private Map<MultiKey, Set<String>> groupIdFieldIdToMemberId = new HashMap<>();
  
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
   * 
   * Get memberships for user cached groups
   * Loop through fields
   * Batch up groups
   * Get memberships for certain users
   * 
   * Get memberships for group cached groups
   * Loop through fields
   * Batch up groups
   * Get memberships for all users
   * 
   * Init if not inited
   * Dont have static caches, do this in provisioner
   * Based user stuff on fields
   * Have a arbitrary group method for groups, and privs (4 methods)
   * Update the dependencies as new ones found
   * Read the dependencies if needed (all in full, certain ones for users)
   * Cache in provisioner if retrieved, retrieve if not and update tables
   */
  
  public boolean isInGroup(String groupName, String memberId) {
    return isHasPrivilege(groupName, "members", memberId);
  }

  private static Map<String, Integer> memberFieldToIndex = new HashMap<>();
  
  static {
    memberFieldToIndex.put("subjectid", 0);
    memberFieldToIndex.put("email", 1);
    memberFieldToIndex.put("subjectidentifier0", 2);
    memberFieldToIndex.put("subjectidentifier1", 3);
    memberFieldToIndex.put("subjectidentifier2", 4);
    memberFieldToIndex.put("memberid", 5);
    memberFieldToIndex.put("entityattributevaluecache0", 6);
    memberFieldToIndex.put("entityattributevaluecache1", 7);
    memberFieldToIndex.put("entityattributevaluecache2", 8);
    memberFieldToIndex.put("entityattributevaluecache3", 9);
  }

  /**
   * int of memberField in the set of attributes to pick
   * @param memberField
   * @return the index
   */
  private static int memberFieldToIndex(String memberField) {
    String memberFieldLower = memberField == null ? null : memberField.toLowerCase();
    if (!memberFieldToIndex.containsKey(memberFieldLower)) {
      throw new RuntimeException("Invalid memberField '" + memberField 
          + "', should be one of: subjectId, email, subjectIdentifier0, subjectIdentifier1, subjectIdentifier2, memberId, entityAttributeValueCache0, entityAttributeValueCache1, entityAttributeValueCache2, entityAttributeValueCache3");
    }
    return memberFieldToIndex.get(memberFieldLower);
  }
  
  /**
   * get a set of members from another group
   * @param provisionableGroupId
   * @param groupName is the group name to check
   * @param listName admins, updaters, members, etc
   * @param memberField subjectId, subjectIdentifier0, subjectIdentifier1, subjectIdentifier2, email
   * @return set of values of subjects in the subject source of the provisioner
   */
  public Set<String> groupPrivilegeHolders(String groupName, String listName, String memberField, String provisionableGroupId) {
    this.initGroupsMembershipsOrCache();

    Field field = FieldFinder.find(listName, true);
    GrouperUtil.assertion(field.isGroupAccessField() || field.isGroupListField(), "Field must be a group privilege: '" + listName + "'");
    String fieldId = field.getId();
    
    String groupId = this.groupNameToGroupId.get(groupName);
    if (StringUtils.isBlank(groupId) && !this.groupNameToGroupId.containsKey(groupName)) {
      
      this.initGroupNameToGroupId(GrouperUtil.toSet(groupName));

      groupId = this.groupNameToGroupId.get(groupName);
      
    }
    HashSet<String> result = new HashSet<>();
    
    if (groupId == null) {
      return result;
    }
    
    MultiKey groupIdFieldIdProvisionableGroupId = new MultiKey(groupId, fieldId, provisionableGroupId);

    MultiKey groupIdFieldId = new MultiKey(groupId, fieldId);
    
    // keep track of old ones
    // TODO delete old ones
    this.groupIdFieldIdDependentGroupIdGroupDependenciesToDelete.remove(groupIdFieldIdProvisionableGroupId);
    
    Boolean retrieved = this.groupIdFieldIdLookedUpForGroupGroupDependencies.get(groupIdFieldId);
    
    if (retrieved == null || !retrieved) {
      
      Map<MultiKey, GcGrouperSyncDependencyGroupGroup> groupIdFieldIdProvisionableGroupIdToDependency = this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncDependencyGroupGroupDao()
          .internal_dependencyGroupGroupRetrieveFromDbByGroupIdsFieldIdsProvisionableGroupIds(GrouperUtil.toSet(new MultiKey(groupId, fieldId, provisionableGroupId)));
      
      if (groupIdFieldIdProvisionableGroupIdToDependency.size() == 0) {
        GcGrouperSyncDependencyGroupGroup gcGrouperSyncDependencyGroupGroup = new GcGrouperSyncDependencyGroupGroup();
        gcGrouperSyncDependencyGroupGroup.setFieldId(fieldId);
        gcGrouperSyncDependencyGroupGroup.setGroupId(groupId);
        gcGrouperSyncDependencyGroupGroup.setProvisionableGroupId(provisionableGroupId);
        gcGrouperSyncDependencyGroupGroup.assignIdIndexForInsert(TableIndex.reserveId(TableIndexType.syncDepGroup));
        this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncDependencyGroupGroupDao().internal_dependencyGroupGroupStore(gcGrouperSyncDependencyGroupGroup);
      }

      this.initGroupGroupMemberships(GrouperUtil.toSet(groupIdFieldId));
    }
    Set<MultiKey> subjectIdAndEmailAndIdentifiers = this.groupIdFieldIdToSubjectIdAndEmailAndIdentifiers.get(groupIdFieldId);
    int memberFieldIndex = memberFieldToIndex(memberField);
    if (GrouperUtil.length(subjectIdAndEmailAndIdentifiers) > 0) {
      for (MultiKey subjectIdAndEmailAndIdentifier : subjectIdAndEmailAndIdentifiers) {
        String memberFieldValue = (String)subjectIdAndEmailAndIdentifier.getKey(memberFieldIndex);
        if (!StringUtils.isBlank(memberFieldValue)) {
          result.add(memberFieldValue);
        }
      }
    }
    return result;


  }

  /**
   * list name can be admins, updaters, readers, etc
   * @param groupName
   * @param listName admins, updaters, members, etc
   * @param memberId
   * @return true if has privilege
   */
  public boolean isHasPrivilege(String groupName, String listName, String memberId) {

    this.initGroupsMembershipsOrCache();
    
    Field field = FieldFinder.find(listName, true);
    GrouperUtil.assertion(field.isGroupAccessField() || field.isGroupListField(), "Field must be a group privilege: '" + listName + "'");
    String fieldId = field.getId();
    
    String groupId = this.groupNameToGroupId.get(groupName);
    if (StringUtils.isBlank(groupId) && !this.groupNameToGroupId.containsKey(groupName)) {
      
      this.initGroupNameToGroupId(GrouperUtil.toSet(groupName));

      groupId = this.groupNameToGroupId.get(groupName);
      
    }

    if (groupId == null) {
      return false;
    }
    
    MultiKey groupIdFieldId = new MultiKey(groupId, fieldId);
    
    // keep track of old ones
    this.groupIdFieldIdUserDependenciesToDelete.remove(groupIdFieldId);
    
    Boolean retrieved = this.groupIdFieldIdLookedUpForGroupUserDependencies.get(groupIdFieldId);
    
    if (retrieved == null || !retrieved) {
      
      Map<MultiKey, GcGrouperSyncDependencyGroupUser> groupIdFieldIdToDependency = this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncDependencyGroupUserDao()
          .dependencyGroupUserRetrieveFromDbOrCacheByGroupIdsFieldIds(GrouperUtil.toSet(new MultiKey(groupId, fieldId)));
      
      if (groupIdFieldIdToDependency.size() == 0) {
        GcGrouperSyncDependencyGroupUser gcGrouperSyncDependencyGroupUser = new GcGrouperSyncDependencyGroupUser();
        gcGrouperSyncDependencyGroupUser.setFieldId(fieldId);
        gcGrouperSyncDependencyGroupUser.setGroupId(groupId);
        gcGrouperSyncDependencyGroupUser.assignIdIndexForInsert(TableIndex.reserveId(TableIndexType.syncDepUser));
        this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncDependencyGroupUserDao().internal_dependencyGroupUserStore(gcGrouperSyncDependencyGroupUser);
      }

      this.initGroupUserMemberships(GrouperUtil.toSet(groupIdFieldId));
      
    }

    Set<String> memberIds = this.groupIdFieldIdToMemberId.get(groupIdFieldId);
    return GrouperUtil.nonNull(memberIds).contains(memberId);
  }

  /**
   * 
   * @param groupIdFieldIdsInput
   * @return groupNameGroupIdFieldNameSubjectIdEmailSubjectIdentifiersTotal for groups
   */
  public void initGroupGroupMemberships(Set<MultiKey> groupIdFieldIdsInput) {

    Set<MultiKey> groupIdFieldIdsLocal = new HashSet<MultiKey>(GrouperUtil.nonNull(groupIdFieldIdsInput));

    // maybe we are done
    if (groupIdFieldIdsLocal.size() == 0) {
      return;
    }

    // lets loop through the rest
    List<MultiKey> groupIdFieldIdList = new ArrayList<MultiKey>(groupIdFieldIdsLocal);
    
    int batchSizeGroups = 1000;
    int numberOfBatchesGroups = GrouperUtil.batchNumberOfBatches(groupIdFieldIdList, batchSizeGroups, false);
    
    GrouperProvisioningConfiguration grouperProvisioningConfiguration = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    grouperProvisioningConfiguration.getSubjectSourcesToProvision();
    
    for (int i=0;i<numberOfBatchesGroups;i++) {
      
      GcDbAccess gcDbAccess = new GcDbAccess();

      List<MultiKey> groupIdFieldIdBatch = GrouperUtil.batchList(groupIdFieldIdList, batchSizeGroups, i);
      
      StringBuilder sqlBase = new StringBuilder("""
              select gmlv.group_name, gmlv.group_id, gmlv.list_name,
              gm.subject_id, gm.email0, gm.subject_identifier0, gm.subject_identifier1, gm.subject_identifier2, gm.id,
              gsm.member_from_id2, gsm.member_from_id3, gsm.member_to_id2, gsm.member_to_id3
              from grouper_members gm
              join grouper_memberships_lw_v gmlv on gmlv.member_id = gm.id
              left join grouper_sync_member gsm on gsm.member_id = gmlv.member_id
              where gsm.grouper_sync_id = ?
              """);

      gcDbAccess.addBindVar(this.grouperProvisioner.getGcGrouperSync().getId());

      Set<String> subjectSources = GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getSubjectSourcesToProvision());
      
      if (subjectSources.size() > 0) {
        sqlBase.append(" and ");
        if (subjectSources.size() > 1) {
          sqlBase.append(" ( ");
        }
        boolean first = true;
        for (String subjectSource : subjectSources) {
          if (!first) {
            sqlBase.append(" or ");
          }
          sqlBase.append(" gmlv.subject_source = ? ");
          gcDbAccess.addBindVar(subjectSource);
          first = false;
        }
        if (subjectSources.size() > 1) {
          sqlBase.append(" ) ");
        }

      }

      sqlBase.append(" and ( ");
      
      boolean first = true;
      for (MultiKey groupIdFieldId : groupIdFieldIdBatch) {
        if (!first) {
          sqlBase.append(" or ");
        }
        String groupId = (String)groupIdFieldId.getKey(0);
        String fieldId = (String)groupIdFieldId.getKey(1);
        
        Field field = FieldFinder.findById(fieldId, true);
        
        GrouperUtil.assertion(field.isGroupListField() || field.isGroupAccessField(), "Field name must be members or a group privilege field: admins, updaters, readers, etc");
        
        sqlBase.append(" ( group_id = ? and list_name = ? ) ");
        gcDbAccess.addBindVar(groupId);
        gcDbAccess.addBindVar(field.getName());
        first = false;
      }
      
      sqlBase.append(" ) ");
      
      
      List<Object[]> groupNameGroupIdFieldNameSubjectIdEmailSubjectIdentifierss = gcDbAccess.sql(sqlBase.toString()).selectList(Object[].class);
      for (Object[] groupNameGroupIdFieldNameSubjectIdEmailSubjectIdentifiers : groupNameGroupIdFieldNameSubjectIdEmailSubjectIdentifierss) {
        String groupName = (String)groupNameGroupIdFieldNameSubjectIdEmailSubjectIdentifiers[0];
        String groupId = (String)groupNameGroupIdFieldNameSubjectIdEmailSubjectIdentifiers[1];
        String fieldName = (String)groupNameGroupIdFieldNameSubjectIdEmailSubjectIdentifiers[2];
        Field field = FieldFinder.find(fieldName, true);
        String subjectId = (String)groupNameGroupIdFieldNameSubjectIdEmailSubjectIdentifiers[3];
        String email = (String)groupNameGroupIdFieldNameSubjectIdEmailSubjectIdentifiers[4];
        String subjectIdentifier0 = (String)groupNameGroupIdFieldNameSubjectIdEmailSubjectIdentifiers[5];
        String subjectIdentifier1 = (String)groupNameGroupIdFieldNameSubjectIdEmailSubjectIdentifiers[6];
        String subjectIdentifier2 = (String)groupNameGroupIdFieldNameSubjectIdEmailSubjectIdentifiers[7];
        String memberId = (String)groupNameGroupIdFieldNameSubjectIdEmailSubjectIdentifiers[8];

        String entityAttributeValueCache0 = (String)groupNameGroupIdFieldNameSubjectIdEmailSubjectIdentifiers[9];
        String entityAttributeValueCache1 = (String)groupNameGroupIdFieldNameSubjectIdEmailSubjectIdentifiers[10];
        String entityAttributeValueCache2 = (String)groupNameGroupIdFieldNameSubjectIdEmailSubjectIdentifiers[11];
        String entityAttributeValueCache3 = (String)groupNameGroupIdFieldNameSubjectIdEmailSubjectIdentifiers[12];



        this.groupNameToGroupId.put(groupName, groupId);
        Set<MultiKey> subjectIdAndEmailAndIdentifiers = this.groupIdFieldIdToSubjectIdAndEmailAndIdentifiers.get(new MultiKey(groupId, field.getId()));
        if (subjectIdAndEmailAndIdentifiers == null) {
          subjectIdAndEmailAndIdentifiers = new HashSet<>();
          this.groupIdFieldIdToSubjectIdAndEmailAndIdentifiers.put(new MultiKey(groupId, field.getId()), subjectIdAndEmailAndIdentifiers);
        }
        Object[] multikey = GrouperUtil.toArrayObject(subjectId, email, subjectIdentifier0, subjectIdentifier1, subjectIdentifier2, memberId, entityAttributeValueCache0, entityAttributeValueCache1, entityAttributeValueCache2, entityAttributeValueCache3);
        subjectIdAndEmailAndIdentifiers.add(new MultiKey(multikey));
      }
    }
  } 

  private boolean inittedGroups = false;
  
  public void initGroupNameToGroupId(Collection<String> groupNames) {
    if (GrouperUtil.nonNull(groupNames).size() > 0) {
      groupNames.removeAll(this.groupNameToGroupId.keySet());
      // lookup the groups
      int batchSize = 1000;
      List<String> groupNamesList = new ArrayList<String>(groupNames);
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(groupNamesList, batchSize, false);
      for (int batchIndex = 0; batchIndex < numberOfBatches; batchIndex++) {
        
        List<String> groupNamesBatch = GrouperUtil.batchList(groupNamesList, batchSize, batchIndex);
        Set<String> groupNamesBatchSet = new HashSet<String>(groupNamesBatch);
        String sql = "select id, name from grouper_groups where name in ( " + GrouperClientUtils.appendQuestions(groupNamesBatch.size()) + " )";
        
        GcDbAccess gcDbAccess = new GcDbAccess().sql(sql);
        for (String groupId : groupNamesBatch) {
          gcDbAccess.addBindVar(groupId);
        }
        List<Object[]> groupIdGroupNames = gcDbAccess.selectList(Object[].class);
        for (Object[] groupIdGroupName : groupIdGroupNames) {
          String groupId = (String)groupIdGroupName[0];
          String groupName = (String)groupIdGroupName[1];
          groupNamesBatchSet.remove(groupName);

          this.groupNameToGroupId.put(groupName, groupId);
        }
        if (GrouperUtil.length(groupNamesBatchSet) > 0) {
          sql = "select source_id, name from grouper_pit_groups where name in ( " + GrouperClientUtils.appendQuestions(groupNamesBatch.size()) + " )";
          gcDbAccess = new GcDbAccess().sql(sql);
          for (String groupId : groupNamesBatch) {
            gcDbAccess.addBindVar(groupId);
          }
          groupIdGroupNames = gcDbAccess.selectList(Object[].class);
          for (Object[] groupIdGroupName : groupIdGroupNames) {
            String groupId = (String)groupIdGroupName[0];
            String groupName = (String)groupIdGroupName[1];
            if (!this.groupNameToGroupId.containsKey(groupName)) {
              groupNamesBatchSet.remove(groupName);
              this.groupNameToGroupId.put(groupName, null);
            }
          }
        }
        for (String groupName : groupNamesBatchSet) {
          GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "errorPrivilegeGroupNotFound", 1);
          int size = GrouperUtil.intValue(this.getGrouperProvisioner().getDebugMap().get("errorPrivilegeGroupNotFound"));
          if (size < 10) {
            this.getGrouperProvisioner().getDebugMap().put("errorPrivilegeGroupNotFound_" + (size-1), groupName);
          }
          this.groupNameToGroupId.put(groupName, null);
        }
      }
    }

  }
  
  public void initGroupNameToGroupIdFromGroupIds(Collection<String> groupIds) {
    if (GrouperUtil.nonNull(groupIds).size() > 0) {
      groupIds.removeAll(this.groupNameToGroupId.values());
      // lookup the groups
      int batchSize = 1000;
      List<String> groupIdsList = new ArrayList<String>(groupIds);
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(groupIdsList, batchSize, false);
      for (int batchIndex = 0; batchIndex < numberOfBatches; batchIndex++) {
        
        List<String> groupIdsBatch = GrouperUtil.batchList(groupIdsList, batchSize, batchIndex);
        String sql = "select id, name from grouper_groups where id in ( " + GrouperClientUtils.appendQuestions(groupIdsBatch.size()) + " )";
        
        GcDbAccess gcDbAccess = new GcDbAccess().sql(sql);
        for (String groupId : groupIdsBatch) {
          gcDbAccess.addBindVar(groupId);
        }
        List<Object[]> groupIdGroupNames = gcDbAccess.selectList(Object[].class);
        for (Object[] groupIdGroupName : groupIdGroupNames) {
          String groupId = (String)groupIdGroupName[0];
          String groupName = (String)groupIdGroupName[1];

          this.groupNameToGroupId.put(groupName, groupId);
        }
      }
    }

  }
  
  public void initGroupsMembershipsOrCache() {

    if (!inittedGroups) {

      Set<String> groupIds = new HashSet<>();
      
      for (MultiKey groupIdFieldId : this.groupIdFieldIdLookedUpForGroupGroupDependencies.keySet()) {
        
        String groupId = (String)groupIdFieldId.getKey(0);
        groupIds.add(groupId);
      }
      
      for (MultiKey groupIdFieldId : this.groupIdFieldIdLookedUpForGroupUserDependencies.keySet()) {
        
        String groupId = (String)groupIdFieldId.getKey(0);
        groupIds.add(groupId);
      }
      
      initGroupNameToGroupIdFromGroupIds(groupIds);
      
      Set<MultiKey> groupIdFieldIds = new HashSet<>();
      groupIdFieldIds.addAll(this.groupIdFieldIdLookedUpForGroupGroupDependencies.keySet());
      
      for (MultiKey groupIdFieldId : groupIdFieldIds) {
        this.groupIdFieldIdLookedUpForGroupGroupDependencies.put(groupIdFieldId, true);
      }
      
      groupIdFieldIds.addAll(this.groupIdFieldIdLookedUpForGroupUserDependencies.keySet());

      this.initGroupGroupMemberships(groupIdFieldIds);

      groupIdFieldIds.clear();
      groupIdFieldIds.addAll(this.groupIdFieldIdLookedUpForGroupUserDependencies.keySet());
      
      for (MultiKey groupIdFieldId : groupIdFieldIds) {
        this.groupIdFieldIdLookedUpForGroupUserDependencies.put(groupIdFieldId, true);
      }
      
      this.initGroupUserMemberships(groupIdFieldIds);
      
      groupIdFieldIds.addAll(this.groupIdFieldIdLookedUpForGroupUserDependencies.keySet());
      
      inittedGroups = true;
    }
    
  }
  
  /**
   * 
   * @param groupIdFieldIdsInput
   * @param forGroups 
   * @return groupIdFieldIdMemberId for users
   */
  public void initGroupUserMemberships(Set<MultiKey> groupIdFieldIdsInput) {

    Set<MultiKey> groupIdFieldIdsLocal = new HashSet<MultiKey>(GrouperUtil.nonNull(groupIdFieldIdsInput));

    // maybe we are done
    if (groupIdFieldIdsLocal.size() == 0) {
      return;
    }

    // lets loop through the rest
    List<MultiKey> groupIdFieldIdList = new ArrayList<MultiKey>(groupIdFieldIdsLocal);
    
    int batchSizeGroups = 200;
    int numberOfBatchesGroups = GrouperUtil.batchNumberOfBatches(groupIdFieldIdList, batchSizeGroups, false);
    
    GrouperProvisioningConfiguration grouperProvisioningConfiguration = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();
    grouperProvisioningConfiguration.getSubjectSourcesToProvision();
    
    boolean isFullSync = this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningType().isFullSync();
    List<String> memberIdsForIncremental = null;
    
    if (!isFullSync && GrouperUtil.length(this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) < 10000) {
      memberIdsForIncremental = new ArrayList<String>();
      for (ProvisioningEntityWrapper provisioningEntityWrapper : this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) {
        memberIdsForIncremental.add(provisioningEntityWrapper.getMemberId());
      }     
    }
    
    for (int i=0;i<numberOfBatchesGroups;i++) {
      
      GcDbAccess gcDbAccessBase = new GcDbAccess();

      List<MultiKey> groupIdFieldIdBatch = GrouperUtil.batchList(groupIdFieldIdList, batchSizeGroups, i);
      
      StringBuilder sqlBase = new StringBuilder("select gmlv.group_name, gmlv.group_id, gmlv.list_name, gmlv.member_id "
          + " from grouper_members gm, grouper_memberships_lw_v gmlv where gmlv.member_id = gm.id ");

      Set<String> subjectSources = GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getSubjectSourcesToProvision());
      
      if (subjectSources.size() > 0) {
        sqlBase.append(" and ");
        if (subjectSources.size() > 1) {
          sqlBase.append(" ( ");
        }
        boolean first = true;
        for (String subjectSource : subjectSources) {
          if (!first) {
            sqlBase.append(" or ");
          }
          sqlBase.append(" gmlv.subject_source = ? ");
          gcDbAccessBase.addBindVar(subjectSource);
          first = false;
        }
        if (subjectSources.size() > 1) {
          sqlBase.append(" ) ");
        }

      }

      sqlBase.append(" and ( ");
      
      boolean first = true;
      for (MultiKey groupIdFieldId : groupIdFieldIdBatch) {
        if (!first) {
          sqlBase.append(" or ");
        }
        String groupId = (String)groupIdFieldId.getKey(0);
        String fieldId = (String)groupIdFieldId.getKey(1);
        
        Field field = FieldFinder.findById(fieldId, true);
        
        GrouperUtil.assertion(field.isGroupListField() || field.isGroupAccessField(), "Field name must be members or a group privilege field: admins, updaters, readers, etc");
        
        sqlBase.append(" ( group_id = ? and list_name = ? ) ");
        gcDbAccessBase.addBindVar(groupId);
        gcDbAccessBase.addBindVar(field.getName());
        first = false;
      }
      
      sqlBase.append(" ) ");
      
      int batchSizeUsers = 600;
      
      int numberOfBatchesUsers = GrouperUtil.batchNumberOfBatches(memberIdsForIncremental, batchSizeUsers, true);
      for (int j=0;j<numberOfBatchesUsers;j++) {

        StringBuilder sql = new StringBuilder(sqlBase);
        GcDbAccess gcDbAccess = new GcDbAccess();

        for (Object bindVar : gcDbAccessBase.getBindVars()) {
          gcDbAccess.addBindVar(bindVar);
        }
        
        List<String> memberIdBatch = GrouperUtil.batchList(memberIdsForIncremental, batchSizeGroups, i);

        if (GrouperUtil.length(memberIdsForIncremental) > 0) {
          sql.append(" and member_id in ( " + GrouperClientUtils.appendQuestions(memberIdBatch.size()) + " ) ");
          for (String memberId : memberIdBatch) {
            gcDbAccess.addBindVar(memberId);
          }
        }
        
        List<Object[]> groupNameGroupidFieldNameMemberIdss = gcDbAccess.sql(sql.toString()).selectList(Object[].class);
        for (Object[] groupNameGroupidFieldNameMemberIds : groupNameGroupidFieldNameMemberIdss) {
          String groupName = (String)groupNameGroupidFieldNameMemberIds[0];
          String groupId = (String)groupNameGroupidFieldNameMemberIds[1];
          String fieldName = (String)groupNameGroupidFieldNameMemberIds[2];

          Field field = FieldFinder.find(fieldName, true);
          
          MultiKey groupIdFieldId = new MultiKey(groupId, field.getId());

          
          this.groupIdFieldIdLookedUpForGroupUserDependencies.put(groupIdFieldId, true);
          String memberId = (String)groupNameGroupidFieldNameMemberIds[3];
          this.groupNameToGroupId.put(groupName, groupId);
          Set<String> memberIds = this.groupIdFieldIdToMemberId.get(new MultiKey(groupId, field.getId()));
          if (memberIds == null) {
            memberIds = new HashSet<>();
            this.groupIdFieldIdToMemberId.put(groupIdFieldId, memberIds);
          }
          memberIds.add(memberId);
        }
      }
    }
  } 

  /**
   * reference back up to the provisioner
   * @param grouperProvisioner1
   */
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner1) {
    this.grouperProvisioner = grouperProvisioner1;
  }

  public List<ProvisioningMembership> translateGrouperToTargetMemberships(
      List<ProvisioningMembership> grouperProvisioningMemberships, boolean includeDelete) {
    
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isOperateOnGrouperMemberships()
        ) {
      return null;
    }

    Collection<Object> changedMemberships = new HashSet<Object>();

    int invalidMembershipsDuringTranslation = 0; //TODO: looks like we're never updating this value
    int membershipsRemovedDueToGroupRemoved = 0;
    int membershipsRemovedDueToEntityRemoved = 0;
    int membershipsRemovedDueToGroupWrapperNull = 0;
    int membershipsRemovedDueToEntityWrapperNull = 0;

    
    // clear out the membership attribute, it might have a default value in there
    if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes) {
      String groupMembershipAttribute = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupMembershipAttributeName();
      if (!StringUtils.isBlank(groupMembershipAttribute)) {
        for (ProvisioningGroup provisioningGroup : this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetGroups()) {
          ProvisioningGroupWrapper provisioningGroupWrapper = provisioningGroup.getProvisioningGroupWrapper();
          if (provisioningGroupWrapper != null && provisioningGroupWrapper.getProvisioningStateGroup().isTranslatedMemberships()) {
            continue;
          }
          provisioningGroup.clearAttribute(groupMembershipAttribute);
        }
      }
    }
    if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes) {
      String entityMembershipAttribute = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityMembershipAttributeName();
      if (!StringUtils.isBlank(entityMembershipAttribute)) {
        for (ProvisioningEntity provisioningEntity : this.grouperProvisioner.retrieveGrouperProvisioningData().retrieveGrouperTargetEntities()) {
          ProvisioningEntityWrapper provisioningEntityWrapper = provisioningEntity.getProvisioningEntityWrapper();
          if (provisioningEntityWrapper != null && provisioningEntityWrapper.getProvisioningStateEntity().isTranslatedMemberships()) {
            continue;
          }
          provisioningEntity.clearAttribute(entityMembershipAttribute);
        }
      }
    }
    
    // not null if group attributes
    Set<ProvisioningGroupWrapper> groupAttributesTranslated = this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.groupAttributes ? new HashSet<ProvisioningGroupWrapper>() : null;
    Set<ProvisioningEntityWrapper> entityAttributesTranslated = this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() == GrouperProvisioningBehaviorMembershipType.entityAttributes ? new HashSet<ProvisioningEntityWrapper>() : null;
    
    List<ProvisioningMembership> grouperTargetMemberships = new ArrayList<ProvisioningMembership>();
    List<ProvisioningMembership> grouperTargetMembershipsTranslated = new ArrayList<ProvisioningMembership>();

    List<String> scripts = GrouperUtil.nonNull(GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGrouperProvisioningToTargetTranslation()).get("Membership"));

    Iterator<ProvisioningMembership> iterator = GrouperUtil.nonNull(grouperProvisioningMemberships).iterator();
    
    while (iterator.hasNext()) {
      
      ProvisioningMembership grouperProvisioningMembership = iterator.next();
  
      try {

        ProvisioningMembershipWrapper provisioningMembershipWrapper = grouperProvisioningMembership.getProvisioningMembershipWrapper();
        
        GcGrouperSyncMembership gcGrouperSyncMembership = provisioningMembershipWrapper.getGcGrouperSyncMembership();
  
        ProvisioningGroupWrapper provisioningGroupWrapper = grouperProvisioningMembership.getProvisioningGroup().getProvisioningGroupWrapper();
        
        ProvisioningEntityWrapper provisioningEntityWrapper = grouperProvisioningMembership.getProvisioningEntity().getProvisioningEntityWrapper();

        if (provisioningGroupWrapper == null) {
          membershipsRemovedDueToGroupWrapperNull++;
          continue;
        }
        
        if (provisioningEntityWrapper == null) {
          membershipsRemovedDueToEntityWrapperNull++;
          continue;
        }

        if (provisioningGroupWrapper.getProvisioningStateGroup().isGroupRemovedDueToAttribute()) {
          membershipsRemovedDueToGroupRemoved++;
          continue;
        }
        
        if (provisioningEntityWrapper.getProvisioningStateEntity().isEntityRemovedDueToAttribute()) {
          membershipsRemovedDueToEntityRemoved++;
          continue;
        }
  
        if (groupAttributesTranslated != null) {
          groupAttributesTranslated.add(provisioningGroupWrapper);
        }
        if (entityAttributesTranslated != null) {
          entityAttributesTranslated.add(provisioningEntityWrapper);
        }
  
        
        ProvisioningGroup grouperTargetGroup = provisioningGroupWrapper.getGrouperTargetGroup();
        
        GcGrouperSyncErrorCode errorCode = provisioningGroupWrapper.getErrorCode();
        String errorMessage = provisioningGroupWrapper.getGcGrouperSyncGroup().getErrorMessage();
        if (errorCode == null && !StringUtils.isBlank(errorMessage)) {
          errorCode = GcGrouperSyncErrorCode.ERR;
        }
            
        ProvisioningEntity grouperTargetEntity = provisioningEntityWrapper.getGrouperTargetEntity();
        
        if (errorCode == null) {
          errorCode = provisioningEntityWrapper.getErrorCode();
          errorMessage = provisioningEntityWrapper.getGcGrouperSyncMember().getErrorMessage();
          if (errorCode == null && !StringUtils.isBlank(errorMessage)) {
            errorCode = GcGrouperSyncErrorCode.ERR;
          }
        }
        
        // if this is an add, and the user isnt there, then there is a problem
        boolean isDelete = gcGrouperSyncMembership.isInTarget() || provisioningMembershipWrapper.getProvisioningStateMembership().isDelete();
  
        boolean isEntityInTarget = (provisioningEntityWrapper.getGcGrouperSyncMember().getInTarget() != null && provisioningEntityWrapper.getGcGrouperSyncMember().getInTarget()) || provisioningEntityWrapper.getTargetProvisioningEntity() != null;
        
        if (!isDelete && errorCode == null && this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().isOnlyAddMembershipsIfUserExistsInTarget()
            && !isEntityInTarget) {
          errorCode = GcGrouperSyncErrorCode.DNE;
        }
        
        if (errorCode != null) {
          this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().assignMembershipError(provisioningMembershipWrapper, errorCode, errorMessage);
          continue;
        }
  
        ProvisioningMembership grouperTargetMembership = new ProvisioningMembership(false);
        if (this.translateGrouperToTargetAutomatically) {
          grouperTargetMembership = grouperProvisioningMembership.clone();
        }
        if (provisioningMembershipWrapper.getGrouperTargetMembership() == null) {
          grouperTargetMembershipsTranslated.add(grouperTargetMembership);
        }
        
        
        grouperTargetMembership.setProvisioningMembershipWrapper(provisioningMembershipWrapper);
   
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
          
          boolean continueTranslation = continueTranslation(elVariableMap, grouperProvisioningConfigurationAttribute);
          if (!continueTranslation) {
            grouperTargetMembership.assignAttributeValue(grouperProvisioningConfigurationAttribute.getName(), null);
            //throw new RuntimeException("Not continuing translation because the translation continue condition '" + grouperProvisioningConfigurationAttribute.getTranslationContinueCondition()+"'  did not evaluate to be true");
          }
          
          if (continueTranslation && (StringUtils.isNotBlank(expressionToUse) 
              || StringUtils.isNotBlank(grouperProvisioningConfigurationAttribute.getTranslateFromGrouperProvisioningGroupField())
              || StringUtils.isNotBlank(grouperProvisioningConfigurationAttribute.getTranslateFromGrouperTargetGroupField())
              || StringUtils.isNotBlank(grouperProvisioningConfigurationAttribute.getTranslateFromGrouperTargetEntityField())
              || StringUtils.isNotBlank(grouperProvisioningConfigurationAttribute.getTranslateFromGrouperProvisioningEntityField()))) {
            Object result = attributeTranslationOrCache( 
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
                
                grouperTargetGroup.addAttributeValueForMembership(result, provisioningMembershipWrapper, true);
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
                if (!grouperProvisioningMembership.getProvisioningEntity().getProvisioningEntityWrapper().getProvisioningStateEntity().isDelete()) {
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
                
                grouperTargetEntity.addAttributeValueForMembership(result, provisioningMembershipWrapper, true);
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
  
        if (grouperTargetMembership.getProvisioningMembershipWrapper().getGcGrouperSyncMembership().isInTarget() && grouperTargetMembership.getProvisioningMembershipWrapper().getGrouperTargetMembership() == null) {
          grouperTargetMembership.getProvisioningMembershipWrapper().getProvisioningStateMembership().setDelete(true);
        }
        
        if (!grouperTargetMembership.getProvisioningMembershipWrapper().getGcGrouperSyncMembership().isInTarget() && grouperTargetMembership.getProvisioningMembershipWrapper().getGrouperTargetMembership() != null) {
          grouperTargetMembership.getProvisioningMembershipWrapper().getProvisioningStateMembership().setCreate(true);
        }
        
  //      if (!grouperTargetMembership.getProvisioningMembershipWrapper().getProvisioningStateMembership().isRecalcObject() 
  //          && grouperTargetMembership.getProvisioningMembershipWrapper().getProvisioningStateMembership().getGrouperIncrementalDataAction() == GrouperIncrementalDataAction.delete) {
  //        grouperTargetMembership.getProvisioningMembershipWrapper().getProvisioningStateMembership().setDelete(true);
  //      }
  //      if (!grouperTargetMembership.getProvisioningMembershipWrapper().getProvisioningStateMembership().isRecalcObject() 
  //          && grouperTargetMembership.getProvisioningMembershipWrapper().getProvisioningStateMembership().getGrouperIncrementalDataAction() == GrouperIncrementalDataAction.insert) {
  //        grouperTargetMembership.getProvisioningMembershipWrapper().getProvisioningStateMembership().setCreate(true);
  //      }
        
        grouperTargetMemberships.add(grouperTargetMembership); 
      } catch (RuntimeException re) {
        try {
          this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().assignMembershipError(grouperProvisioningMembership.getProvisioningMembershipWrapper(), GcGrouperSyncErrorCode.ERR, 
              grouperProvisioningMembership.getProvisioningMembershipWrapper() + ", " + GrouperUtil.getFullStackTrace(re));
        } catch (RuntimeException e) {
          throw new RuntimeException("Cannot translate membership or log error for: " + grouperProvisioningMembership, re);
        }
      }
        
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
    
    if (membershipsRemovedDueToGroupRemoved > 0) {
      GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "membershipsRemovedDueToGroupRemoved", membershipsRemovedDueToGroupRemoved);
    }
    if (membershipsRemovedDueToEntityRemoved > 0) {
      GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "membershipsRemovedDueToEntityRemoved", membershipsRemovedDueToEntityRemoved);
    }
    if (membershipsRemovedDueToGroupWrapperNull > 0) {
      GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "membershipsRemovedDueToGroupWrapperNull", membershipsRemovedDueToGroupWrapperNull);
    }
    if (membershipsRemovedDueToEntityWrapperNull > 0) {
      GrouperUtil.mapAddValue(this.getGrouperProvisioner().getDebugMap(), "membershipsRemovedDueToEntityWrapperNull", membershipsRemovedDueToEntityWrapperNull);
    }

    for (ProvisioningGroupWrapper provisioningGroupWrapper : GrouperUtil.nonNull(groupAttributesTranslated)) {
      provisioningGroupWrapper.getProvisioningStateGroup().setTranslatedMemberships(true);
    }
    for (ProvisioningEntityWrapper provisioningEntityWrapper : GrouperUtil.nonNull(entityAttributesTranslated)) {
      provisioningEntityWrapper.getProvisioningStateEntity().setTranslatedMemberships(true);
    }

    return grouperTargetMemberships;
  }

  public List<ProvisioningEntity> translateGrouperToTargetEntities(
      List<ProvisioningEntity> grouperProvisioningEntities, boolean includeDelete, boolean forCreate) {
    
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isOperateOnGrouperEntities()) {
      return null;
    }

    Collection<Object> changedEntities = new HashSet<Object>();
    List<ProvisioningEntity> grouperTargetEntities = new ArrayList<ProvisioningEntity>();

    List<String> scripts = GrouperUtil.nonNull(GrouperUtil.nonNull(this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGrouperProvisioningToTargetTranslation()).get("Entity"));
    
    if (forCreate) {
      scripts.addAll(GrouperUtil.nonNull(GrouperUtil.nonNull(
          this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGrouperProvisioningToTargetTranslation()).get("EntityCreateOnly")));
    }

    List<ProvisioningEntity> grouperTargetEntitiesTranslated = new ArrayList<ProvisioningEntity>();

    PROVISIONING_ENTITY_BLOCK: for (ProvisioningEntity grouperProvisioningEntity: GrouperUtil.nonNull(grouperProvisioningEntities)) {
      
      try {
        ProvisioningEntity grouperTargetEntity = new ProvisioningEntity(false);
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
              //TODO call translateFromGrouperProvisioningEntityField once only
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
  
              boolean continueTranslation = continueTranslation(elVariableMap, grouperProvisioningConfigurationAttribute);
              if (!continueTranslation) {
                grouperTargetEntity.assignAttributeValue(grouperProvisioningConfigurationAttribute.getName(), null);
                // throw new RuntimeException("Not continuing translation because the translation continue condition '" + grouperProvisioningConfigurationAttribute.getTranslationContinueCondition()+"'  did not evaluate to be true");
              }
              
              if (continueTranslation && (!StringUtils.isBlank(expressionToUse) || !StringUtils.isBlank(staticValuesToUse) || !StringUtils.isBlank(grouperProvisioningEntityField)
                  || this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isEntityAttributeNameHasCache(grouperProvisioningConfigurationAttribute.getName())
                  || this.shouldTranslateEntityAttribute(provisioningEntityWrapper, grouperProvisioningConfigurationAttribute))) { 
  
                Object result = attributeTranslationOrCache( 
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
          provisioningEntityWrapper.getProvisioningStateEntity().setDelete(true);
        } else if (forCreate) {
          provisioningEntityWrapper.getProvisioningStateEntity().setCreate(true);
        }
      } catch (RuntimeException re) {
        try {
          this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().assignEntityError(grouperProvisioningEntity.getProvisioningEntityWrapper(), GcGrouperSyncErrorCode.ERR, 
              grouperProvisioningEntity.getProvisioningEntityWrapper() + ", " + GrouperUtil.getFullStackTrace(re));
        } catch (RuntimeException e) {
          throw new RuntimeException("Cannot translate entity or log error for: " + grouperProvisioningEntity, re);
        }
      }

    }
    if (GrouperUtil.length(grouperTargetEntitiesTranslated) > 0) {
      this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.translateGrouperEntitiesToTarget, grouperTargetEntitiesTranslated);
    }

    return grouperTargetEntities;
  }

  /**
   * @param elVariableMap
   * @param grouperProvisioningConfigurationAttribute
   * @return true if continue with translation otherwise false
   */
  public static boolean continueTranslation(Map<String, Object> elVariableMap,
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute) {
    
    
    String translationContinueCondition = grouperProvisioningConfigurationAttribute.getTranslationContinueCondition();
    boolean checkForNullsInScript = grouperProvisioningConfigurationAttribute.isCheckForNullsInScript();
    return continueTranslation(elVariableMap, checkForNullsInScript, translationContinueCondition);
  }
  
  /**
   * @param elVariableMap
   * @param checkForNullsInScript
   * @param translationContinueCondition
   * @return true if continue with translation otherwise false
   */
  public static boolean continueTranslation(Map<String, Object> elVariableMap, boolean checkForNullsInScript, String translationContinueCondition) {
    
    if (checkForNullsInScript) {
      if (StringUtils.isNotBlank(translationContinueCondition)) {
        try {
          Object result = GrouperUtil.substituteExpressionLanguageScript(translationContinueCondition, elVariableMap, true, true, true);
          boolean resultBoolean = GrouperUtil.booleanValue(result, false);
          return resultBoolean;
        } catch (RuntimeException re) {
          GrouperUtil.injectInException(re, ", script: '" + translationContinueCondition + "', ");
          GrouperUtil.injectInException(re, GrouperUtil.toStringForLog(elVariableMap));
          throw re;
        }
      }
    }
    
    return true;
  }

  /**
   * 
   * @param provisioningEntityWrapper
   * @param grouperProvisioningConfigurationAttribute
   * @return
   */
  public boolean shouldTranslateEntityAttribute(
      ProvisioningEntityWrapper provisioningEntityWrapper,
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute) {
    return false;
  }

  public Collection<GrouperProvisioningConfigurationAttribute> entityTargetAttributesInTranslationOrder() {
    return this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getTargetEntityAttributeNameToConfig().values();
  }

  public List<ProvisioningGroup> translateGrouperToTargetGroups(List<ProvisioningGroup> grouperProvisioningGroups, boolean includeDelete, boolean forCreate) {

    
    if (!this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isOperateOnGrouperGroups()) {
      return null;
    }
    
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
      
      try {
        ProvisioningGroup grouperTargetGroup = new ProvisioningGroup(false);
        
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
  
              boolean continueTranslation = continueTranslation(elVariableMap, grouperProvisioningConfigurationAttribute);
              if (!continueTranslation) {
                grouperTargetGroup.assignAttributeValue(grouperProvisioningConfigurationAttribute.getName(), null);
                //throw new RuntimeException("Not continuing translation because the translation continue condition '" + grouperProvisioningConfigurationAttribute.getTranslationContinueCondition()+"'  did not evaluate to be true");
              }
              
              if (continueTranslation && (!StringUtils.isBlank(expressionToUse) || !StringUtils.isBlank(staticValuesToUse) || !StringUtils.isBlank(grouperProvisioningGroupField)
                  || this.grouperProvisioner.retrieveGrouperProvisioningBehavior().isGroupAttributeNameHasCache(grouperProvisioningConfigurationAttribute.getName())
                  || this.shouldTranslateGroupAttribute(provisioningGroupWrapper, grouperProvisioningConfigurationAttribute))) { 
                Object result = attributeTranslationOrCache( 
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
          provisioningGroupWrapper.getProvisioningStateGroup().setDelete(true);
        } else if (forCreate) {
          provisioningGroupWrapper.getProvisioningStateGroup().setCreate(true);
        }
        grouperTargetGroups.add(grouperTargetGroup);
      } catch (RuntimeException re) {
        try {
          this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().assignGroupError(grouperProvisioningGroup.getProvisioningGroupWrapper(), GcGrouperSyncErrorCode.ERR, 
              grouperProvisioningGroup.getProvisioningGroupWrapper() + ", " + GrouperUtil.getFullStackTrace(re));
        } catch (RuntimeException e) {
          throw new RuntimeException("Cannot translate group or log error for: " + grouperProvisioningGroup, re);
        }
      }
    }
    if (GrouperUtil.length(grouperTargetGroupsTranslated) > 0) {
      this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.translateGrouperGroupsToTarget, grouperTargetGroupsTranslated);
    }

    return grouperTargetGroups;
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperProvisioningTranslator.class);

  /**
   * if the provisioner might generate a transation
   * @param provisioningGroupWrapper
   * @param grouperProvisioningConfigurationAttribute
   * @return
   */
  public boolean shouldTranslateGroupAttribute(
      ProvisioningGroupWrapper provisioningGroupWrapper,
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute) {
    return false;
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
      if (StringUtils.equals("subjectIdentifier1", field) && !StringUtils.isBlank(provisioningEntity.getSubjectIdentifier1())) {
        return provisioningEntity.getSubjectIdentifier1();
      }
      if (StringUtils.equals("subjectIdentifier2", field) && !StringUtils.isBlank(provisioningEntity.getSubjectIdentifier2())) {
        return provisioningEntity.getSubjectIdentifier2();
      }
      if (StringUtils.equals("subjectIdentifier", field)) {
        return provisioningEntity.getSubjectIdentifier();
      }
      if (StringUtils.equals("idIndex", field)) {
        return provisioningEntity.getIdIndex();
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
        "subjectId", "subjectSourceId", "description", "subjectIdentifier", "subjectIdentifier0", "subjectIdentifier1", "subjectIdentifier2", "idIndex")) {
      return null;
    }
    
    throw new RuntimeException("Not expecting grouperProvisioningEntityField: '" + field + "'");
  }
  

  public Object attributeTranslationOrCache(Object currentValue, Map<String, Object> elVariableMap, boolean forCreate,
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute, 
      ProvisioningGroupWrapper provisioningGroupWrapper, ProvisioningEntityWrapper provisioningEntityWrapper) {
    
    if (grouperProvisioningConfigurationAttribute == null) {
      return currentValue;
    }
    
    boolean[] translate = new boolean[] {false};
    boolean[] shouldRetrieveFromCache = new boolean[] {false};

    Object result = attributeTranslation(elVariableMap, forCreate,
        grouperProvisioningConfigurationAttribute, provisioningGroupWrapper,
        provisioningEntityWrapper, translate, shouldRetrieveFromCache);
    
    if (GrouperUtil.isBlank(result) && translate[0] && shouldRetrieveFromCache[0]) {
      Object cachedResult = attributeTranslationRetrieveFromCache(
          grouperProvisioningConfigurationAttribute, provisioningGroupWrapper,
          provisioningEntityWrapper);
      
      if (cachedResult != null) {
        result = cachedResult;
      }
    }
    
    return result;
    
  }

  /**
   * 
   * @param elVariableMap
   * @param forCreate
   * @param grouperProvisioningConfigurationAttribute
   * @param provisioningGroupWrapper
   * @param provisioningEntityWrapper
   * @param translate
   * @param shouldRetrieveFromCache - Only retrieve from cache if the object is delete or if there's no other translation possibility, e.g. originated from target
   * @return
   */
  public Object attributeTranslation(Map<String, Object> elVariableMap, boolean forCreate,
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute,
      ProvisioningGroupWrapper provisioningGroupWrapper,
      ProvisioningEntityWrapper provisioningEntityWrapper, boolean[] translate, boolean[] shouldRetrieveFromCache) {
    
    String expressionToUse = getTargetExpressionToUse(forCreate, grouperProvisioningConfigurationAttribute);
    String staticValuesToUse = getTranslateFromStaticValuesToUse(forCreate, grouperProvisioningConfigurationAttribute);
    String grouperProvisioningGroupField = getTranslateFromGrouperProvisioningGroupField(forCreate, grouperProvisioningConfigurationAttribute);
    String grouperTargetGroupField = getTranslateFromGrouperTargetGroupField(forCreate, grouperProvisioningConfigurationAttribute);
    String grouperProvisioningEntityField = getTranslateFromGrouperProvisioningEntityField(forCreate, grouperProvisioningConfigurationAttribute);
    String grouperTargetEntityField = getTranslateFromGrouperTargetEntityField(forCreate, grouperProvisioningConfigurationAttribute);

    Object result = null;
    if (!StringUtils.isBlank(expressionToUse)) {
      result = runScript(expressionToUse, elVariableMap);
      translate[0] = true;
    } else if (!StringUtils.isBlank(staticValuesToUse)) {
      if (grouperProvisioningConfigurationAttribute.isMultiValued()) {
        result = GrouperUtil.splitTrimToSet(staticValuesToUse, ",");
      } else {
        result = staticValuesToUse;
      }
      
      translate[0] = true;
    } else if (provisioningGroupWrapper != null && provisioningGroupWrapper.getGrouperProvisioningGroup() != null && !StringUtils.isBlank(grouperProvisioningGroupField)) {
      result = translateFromGrouperProvisioningGroupField(provisioningGroupWrapper, 
          grouperProvisioningGroupField);
      translate[0] = true;
    } else if (provisioningEntityWrapper != null && provisioningEntityWrapper.getGrouperProvisioningEntity() != null && !StringUtils.isBlank(grouperProvisioningEntityField)) {
      result = translateFromGrouperProvisioningEntityField(provisioningEntityWrapper, 
          grouperProvisioningEntityField);
      translate[0] = true;
    } else if (provisioningGroupWrapper != null && provisioningGroupWrapper.getGrouperTargetGroup() != null && !StringUtils.isBlank(grouperTargetGroupField)) {
      
      ProvisioningGroup grouperTargetGroup = provisioningGroupWrapper.getGrouperTargetGroup();
      result = grouperTargetGroup.retrieveAttributeValue(grouperTargetGroupField);
      translate[0] = true;
      
    } else if (provisioningEntityWrapper != null && provisioningEntityWrapper.getGrouperTargetEntity() != null && !StringUtils.isBlank(grouperTargetEntityField)) {
      
      ProvisioningEntity grouperTargetEntity = provisioningEntityWrapper.getGrouperTargetEntity();
      result = grouperTargetEntity.retrieveAttributeValue(grouperTargetEntityField);
      translate[0] = true;
      
    } else {
      if (provisioningGroupWrapper != null && provisioningGroupWrapper.getGcGrouperSyncGroup() != null 
          && grouperProvisioningConfigurationAttribute.getGrouperProvisioningConfigurationAttributeType() == GrouperProvisioningConfigurationAttributeType.group) {
        // look for grouper source first, then target
        for (GrouperProvisioningConfigurationAttributeDbCache groupCache : this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupAttributeDbCaches()) {
          if (groupCache != null && StringUtils.equals(groupCache.getAttributeName(), grouperProvisioningConfigurationAttribute.getName()) 
              && groupCache.getType() == GrouperProvisioningConfigurationAttributeDbCacheType.attribute) {
            shouldRetrieveFromCache[0] = true;
            translate[0] = true;
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
            shouldRetrieveFromCache[0] = true;
            translate[0] = true;
            break;
          }
        }
      }
    }
    
    if (provisioningEntityWrapper != null && provisioningEntityWrapper.getProvisioningStateEntity().isDelete()) {
      shouldRetrieveFromCache[0] = true;
    }
    
    if (provisioningGroupWrapper != null && provisioningGroupWrapper.getProvisioningStateGroup().isDelete()) {
      shouldRetrieveFromCache[0] = true;
    }
    return result;
  }

  public Object attributeTranslationRetrieveFromCache(
      GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute,
      ProvisioningGroupWrapper provisioningGroupWrapper,
      ProvisioningEntityWrapper provisioningEntityWrapper) {
    
    if (provisioningEntityWrapper != null
        && provisioningEntityWrapper.getGcGrouperSyncMember() != null
        && grouperProvisioningConfigurationAttribute.getGrouperProvisioningConfigurationAttributeType() == GrouperProvisioningConfigurationAttributeType.entity) {

      for (GrouperProvisioningConfigurationAttributeDbCache entityCache : this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityAttributeDbCaches()) {
        if (entityCache != null && StringUtils.equals(entityCache.getAttributeName(), grouperProvisioningConfigurationAttribute.getName()) 
            && entityCache.getType() == GrouperProvisioningConfigurationAttributeDbCacheType.attribute) {
          Object result = translateFromGrouperProvisioningEntityField(provisioningEntityWrapper, 
              "entityAttributeValueCache" + entityCache.getIndex());
          return result;
        }
      }
      
    }
    
    if (provisioningGroupWrapper != null
        && provisioningGroupWrapper.getGcGrouperSyncGroup() != null
        && grouperProvisioningConfigurationAttribute.getGrouperProvisioningConfigurationAttributeType() == GrouperProvisioningConfigurationAttributeType.group) {
      for (GrouperProvisioningConfigurationAttributeDbCache groupCache : this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGroupAttributeDbCaches()) {
        if (groupCache != null && StringUtils.equals(groupCache.getAttributeName(), grouperProvisioningConfigurationAttribute.getName()) 
            && groupCache.getType() == GrouperProvisioningConfigurationAttributeDbCacheType.attribute) {
          Object result = translateFromGrouperProvisioningGroupField(provisioningGroupWrapper, 
              "groupAttributeValueCache" + groupCache.getIndex());
          return result;
        }
      }
    }
    
    return null;
    
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
      
      List<ProvisioningUpdatableAttributeAndValue> provisioningUpdatableAttributeAndValues = new ArrayList<ProvisioningUpdatableAttributeAndValue>(1);
      targetGroup.setMatchingIdAttributeNameToValues(provisioningUpdatableAttributeAndValues);

      if (this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isGroupMatchingAttributeSameAsSearchAttribute()) {
        targetGroup.setSearchIdAttributeNameToValues(provisioningUpdatableAttributeAndValues);
      }

      // first do all current values, then do all past values
      for (GrouperProvisioningConfigurationAttribute matchingAttribute : this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGroupMatchingAttributes()) {
        String matchingAttributeName = matchingAttribute.getName();
        
        // dont worry if dupes... oh well
//        Object targetCurrentValue = massageToString(targetGroup.retrieveAttributeValue(matchingAttributeName), 2);
        Object targetCurrentValue = targetGroup.retrieveAttributeValue(matchingAttributeName);

        if(!GrouperUtil.isBlank(targetCurrentValue)) {
          
          ProvisioningUpdatableAttributeAndValue provisioningUpdatableAttributeAndValue = new ProvisioningUpdatableAttributeAndValue(
              this.getGrouperProvisioner(), matchingAttributeName, targetCurrentValue,
              GrouperProvisioningConfigurationAttributeType.group);
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
            
            ProvisioningUpdatableAttributeAndValue provisioningUpdatableAttributeAndValue = new ProvisioningUpdatableAttributeAndValue(
                this.getGrouperProvisioner(), matchingAttributeName, cachedValue, GrouperProvisioningConfigurationAttributeType.group);
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
        
        List<ProvisioningUpdatableAttributeAndValue> provisioningUpdatableAttributeAndValues = new ArrayList<ProvisioningUpdatableAttributeAndValue>(1);
        targetGroup.setSearchIdAttributeNameToValues(provisioningUpdatableAttributeAndValues);

        // first do all current values, then do all past values
        for (GrouperProvisioningConfigurationAttribute searchAttribute : this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGroupSearchAttributes()) {
          String searchAttributeName = searchAttribute.getName();
          
          // dont worry if dupes... oh well
          Object targetCurrentValue = massageToString(targetGroup.retrieveAttributeValue(searchAttributeName), 2);

          if(!GrouperUtil.isBlank(targetCurrentValue)) {
            
            ProvisioningUpdatableAttributeAndValue provisioningUpdatableAttributeAndValue = new ProvisioningUpdatableAttributeAndValue(
                this.getGrouperProvisioner(), searchAttributeName, targetCurrentValue, GrouperProvisioningConfigurationAttributeType.group);
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
              ProvisioningUpdatableAttributeAndValue provisioningUpdatableAttributeAndValue = new ProvisioningUpdatableAttributeAndValue(
                  this.getGrouperProvisioner(), searchAttributeName, cachedValue, GrouperProvisioningConfigurationAttributeType.group);
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
      
      List<ProvisioningUpdatableAttributeAndValue> provisioningUpdatableAttributeAndValues = new ArrayList<ProvisioningUpdatableAttributeAndValue>(1);
      targetEntity.setMatchingIdAttributeNameToValues(provisioningUpdatableAttributeAndValues);

      if (this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().isEntityMatchingAttributeSameAsSearchAttribute()) {
        targetEntity.setSearchIdAttributeNameToValues(provisioningUpdatableAttributeAndValues);
      }

      boolean grouperTargetObject = targetEntity.isGrouperTargetObject();

      // first do all current values, then do all past values
      for (GrouperProvisioningConfigurationAttribute matchingAttribute : this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getEntityMatchingAttributes()) {
        String matchingAttributeName = matchingAttribute.getName();
        
        // dont worry if dupes... oh well
//        Object targetCurrentValue = massageToString(targetEntity.retrieveAttributeValue(matchingAttributeName), 2);
        Object targetCurrentValue = targetEntity.retrieveAttributeValue(matchingAttributeName);

        if(!GrouperUtil.isBlank(targetCurrentValue)) {
          
          ProvisioningUpdatableAttributeAndValue provisioningUpdatableAttributeAndValue = new ProvisioningUpdatableAttributeAndValue(
              this.getGrouperProvisioner(), matchingAttributeName, targetCurrentValue, GrouperProvisioningConfigurationAttributeType.entity);
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
            ProvisioningUpdatableAttributeAndValue provisioningUpdatableAttributeAndValue = new ProvisioningUpdatableAttributeAndValue(
                this.getGrouperProvisioner(), matchingAttributeName, cachedValue, GrouperProvisioningConfigurationAttributeType.entity);
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
        
        List<ProvisioningUpdatableAttributeAndValue> provisioningUpdatableAttributeAndValues = new ArrayList<ProvisioningUpdatableAttributeAndValue>(1);
        targetEntity.setSearchIdAttributeNameToValues(provisioningUpdatableAttributeAndValues);

        // first do all current values, then do all past values
        for (GrouperProvisioningConfigurationAttribute searchAttribute : this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getEntitySearchAttributes()) {
          String searchAttributeName = searchAttribute.getName();
          
          // dont worry if dupes... oh well
          Object targetCurrentValue = massageToString(targetEntity.retrieveAttributeValue(searchAttributeName), 2);

          if(!GrouperUtil.isBlank(targetCurrentValue)) {
            
            ProvisioningUpdatableAttributeAndValue provisioningUpdatableAttributeAndValue = new ProvisioningUpdatableAttributeAndValue(
                this.getGrouperProvisioner(), searchAttributeName, targetCurrentValue, GrouperProvisioningConfigurationAttributeType.entity);
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
              ProvisioningUpdatableAttributeAndValue provisioningUpdatableAttributeAndValue = new ProvisioningUpdatableAttributeAndValue(
                  this.getGrouperProvisioner(), searchAttributeName, cachedValue, GrouperProvisioningConfigurationAttributeType.entity);
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
    
    OUTER: for (ProvisioningMembership targetMembership: GrouperUtil.nonNull(targetMemberships)) {
      
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
        // ${new('edu.internet2.middleware.grouperClient.collections.MultiKey', targetMembership.getProvisioningGroupId(), targetMembership.getProvisioningEntity().retrieveAttributeValueString('userName'))}
        // ${new('edu.internet2.middleware.grouperClient.collections.MultiKey', targetMembership.retrieveAttributeValueString('role'), targetMembership.retrieveAttributeValueString('netID'))}
        id = runScript(membershipIdScript, elVariableMap);

      } else {
        throw new RuntimeException("Must have membershipMatchingIdAttribute, or membershipMatchingIdExpression");
      }
//      id = massageToString(id, 2);
      if (id instanceof MultiKey) {
        
        MultiKey matchingIdMultiKey = (MultiKey)id;
        for (int i=0; i<matchingIdMultiKey.size(); i++) {
          if (matchingIdMultiKey.getKey(i) == null) {
            // if the membership is a delete and not in target then just dont worry about it, its old
            if (targetMembership.getProvisioningMembershipWrapper().getProvisioningStateMembership().isDelete()
                && (targetMembership.getProvisioningMembershipWrapper().getGcGrouperSyncMembership().getInTarget() == null
                || !targetMembership.getProvisioningMembershipWrapper().getGcGrouperSyncMembership().getInTarget())) {
              this.getGrouperProvisioner().retrieveGrouperProvisioningData().removeAndUnindexMembershipWrapper(targetMembership.getProvisioningMembershipWrapper());
              continue OUTER;
            }
            GcGrouperSyncErrorCode errorCode = GcGrouperSyncErrorCode.DNE;
            String errorMessage = "membership multiKey has blank value in index: " + i;
            this.grouperProvisioner.retrieveGrouperProvisioningValidation()
              .assignMembershipError(targetMembership.getProvisioningMembershipWrapper(), errorCode, errorMessage);
            continue OUTER;
          }
        }
        
      }
      if (id == null) {
        // if the membership is a delete and not in target then just dont worry about it, its old
        if (targetMembership.getProvisioningMembershipWrapper().getProvisioningStateMembership().isDelete()
            && (targetMembership.getProvisioningMembershipWrapper().getGcGrouperSyncMembership().getInTarget() == null
            || !targetMembership.getProvisioningMembershipWrapper().getGcGrouperSyncMembership().getInTarget())) {
          this.getGrouperProvisioner().retrieveGrouperProvisioningData().removeAndUnindexMembershipWrapper(targetMembership.getProvisioningMembershipWrapper());
          continue OUTER;
        }
      }
      // just hard code to "id" since memberships just have one matching id
      ProvisioningUpdatableAttributeAndValue provisioningUpdatableAttributeAndValue = new ProvisioningUpdatableAttributeAndValue(
          this.getGrouperProvisioner(), "id", id,
          GrouperProvisioningConfigurationAttributeType.membership);
      provisioningUpdatableAttributeAndValue.setCurrentValue(true);

      List<ProvisioningUpdatableAttributeAndValue> provisioningUpdatableAttributeAndValues = new ArrayList<>(1);
      provisioningUpdatableAttributeAndValues.add(provisioningUpdatableAttributeAndValue);
      targetMembership.setMatchingIdAttributeNameToValues(provisioningUpdatableAttributeAndValues);

    }

  }

  public Object runScript(String script, Map<String, Object> elVariableMap) {
    return runScriptStatic(script, elVariableMap);
  }

  public static Object runScriptStatic(String script, Map<String, Object> elVariableMap) {
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
  
  public String getTranslateFromGrouperTargetGroupField(boolean forCreate, GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute) {
    String expression = grouperProvisioningConfigurationAttribute.getTranslateFromGrouperTargetGroupField();
    return expression;
  }
  
  public String getTranslateFromGrouperTargetEntityField(boolean forCreate, GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute) {
    String expression = grouperProvisioningConfigurationAttribute.getTranslateFromGrouperTargetEntityField();
    return expression;
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


  public void removeUneededDependencyRows() {
    if (this.groupIdFieldIdUserDependenciesToDelete.size() > 0) {
      this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncDependencyGroupUserDao().internal_dependencyGroupUserDeleteBatchByIdIndexes(this.groupIdFieldIdUserDependenciesToDelete.values());
    }
    if (this.groupIdFieldIdDependentGroupIdGroupDependenciesToDelete.size() > 0) {
      this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncDependencyGroupUserDao().internal_dependencyGroupUserDeleteBatchByIdIndexes(this.groupIdFieldIdDependentGroupIdGroupDependenciesToDelete.values());
    }
  }
}