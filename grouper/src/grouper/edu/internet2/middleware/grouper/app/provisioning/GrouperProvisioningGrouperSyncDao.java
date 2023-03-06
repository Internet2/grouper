package edu.internet2.middleware.grouper.app.provisioning;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.SubjectFinder;
<<<<<<< HEAD
import edu.internet2.middleware.grouper.app.tableSync.GrouperProvisioningSyncIntegration;
import edu.internet2.middleware.grouper.app.tableSync.ProvisioningSyncResult;
=======
>>>>>>> cde0848eaefb94061e688bf390c1349ca347f98b
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;
import edu.internet2.middleware.subject.Subject;

public class GrouperProvisioningGrouperSyncDao {

  private GrouperProvisioner grouperProvisioner = null;

  
  public GrouperProvisioner getGrouperProvisioner() {
    return grouperProvisioner;
  }

  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner) {
    this.grouperProvisioner = grouperProvisioner;
  }

  /**
   * add / update / delete sync objects based on real data
   */
  public void fixSyncObjects() {
    this.getGrouperProvisioner().retrieveGrouperProvisioningSyncIntegration().fullSyncMembersForInitialize();
    
    this.getGrouperProvisioner().retrieveGrouperProvisioningSyncIntegration().fullSyncMemberships();
    
  }

  /**
   * get sync objects from the database
   */
  public List<GcGrouperSyncGroup> retrieveAllSyncGroups() {
    GcGrouperSync gcGrouperSync = this.getGrouperProvisioner().getGcGrouperSync();

    List<GcGrouperSyncGroup> gcGrouperSyncGroups = gcGrouperSync
        .getGcGrouperSyncGroupDao().groupRetrieveAll();

    clearErrorsGroup(gcGrouperSyncGroups);
    return gcGrouperSyncGroups;
  }

  public void clearErrorsGroup(Collection<GcGrouperSyncGroup> gcGrouperSyncGroups) {
    for (GcGrouperSyncGroup gcGrouperSyncGroup : GrouperUtil.nonNull(gcGrouperSyncGroups)) {
      gcGrouperSyncGroup.setErrorCode(null);
      gcGrouperSyncGroup.setErrorMessage(null);
      gcGrouperSyncGroup.setErrorTimestamp(null);
    }
  }
  
  public void clearErrorsMember(Collection<GcGrouperSyncMember> gcGrouperSyncMembers) {
    for (GcGrouperSyncMember gcGrouperSyncMember : GrouperUtil.nonNull(gcGrouperSyncMembers)) {
      gcGrouperSyncMember.setErrorCode(null);
      gcGrouperSyncMember.setErrorMessage(null);
      gcGrouperSyncMember.setErrorTimestamp(null);
    }
  }
  
  public void clearErrorsMembership(Collection<GcGrouperSyncMembership> gcGrouperSyncMemberships) {
    for (GcGrouperSyncMembership gcGrouperSyncMembership : GrouperUtil.nonNull(gcGrouperSyncMemberships)) {
      gcGrouperSyncMembership.setErrorCode(null);
      gcGrouperSyncMembership.setErrorMessage(null);
      gcGrouperSyncMembership.setErrorTimestamp(null);
    }
  }
  
  /**
   * get sync objects from the database
   */
  public List<GcGrouperSyncMember> retrieveAllSyncMembers() {
    GcGrouperSync gcGrouperSync = this.getGrouperProvisioner().getGcGrouperSync();

    List<GcGrouperSyncMember> gcGrouperSyncMembers = gcGrouperSync
        .getGcGrouperSyncMemberDao().memberRetrieveAll();
    clearErrorsMember(gcGrouperSyncMembers);

    return gcGrouperSyncMembers;
  }

  /**
   * get sync objects from the database.  all records correspond to a sync group and sync member or its skipped
   */
  public List<GcGrouperSyncMembership> retrieveAllSyncMemberships() {
    GcGrouperSync gcGrouperSync = this.getGrouperProvisioner().getGcGrouperSync();

    List<GcGrouperSyncMembership> gcGrouperSyncMemberships = gcGrouperSync
        .getGcGrouperSyncMembershipDao().membershipRetrieveAll();

    clearErrorsMembership(gcGrouperSyncMemberships);

    return gcGrouperSyncMemberships;
  }
  
  
<<<<<<< GROUPER_5_BRANCH
=======
  /**
   * get sync objects from the database
   * @param logLabel
   */
  public void retrieveIncrementalSyncGroups(String logLabel) {

    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();

    long start = System.currentTimeMillis();

    Set<String> groupIdsToRetrieve = new HashSet<String>();
    Set<String> groupIdsToIgnore = new HashSet<String>();

    for (ProvisioningGroupWrapper provisioningGroupWrapper : this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()) {
      if (provisioningGroupWrapper.getGcGrouperSyncGroup() == null) {
        groupIdsToRetrieve.add(provisioningGroupWrapper.getGroupId());
      } else {
        groupIdsToIgnore.add(provisioningGroupWrapper.getGroupId());
      }
    }
    
    for (ProvisioningMembershipWrapper provisioningMembershipWrapper : this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
      if (provisioningMembershipWrapper.getGroupIdMemberId() != null) {
        String groupId = (String)provisioningMembershipWrapper.getGroupIdMemberId().getKey(0);
        if (!groupIdsToIgnore.contains(groupId)) {
          ProvisioningGroupWrapper provisioningGroupWrapper = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex()
              .getGroupUuidToProvisioningGroupWrapper().get(groupId);
          if (provisioningGroupWrapper == null || provisioningGroupWrapper.getGcGrouperSyncGroup() == null) {
            groupIdsToRetrieve.add(groupId);
          }
<<<<<<< HEAD
        }
      }
    }

    debugMap.put("syncGroupsToQuery_"+logLabel,
        GrouperUtil.length(groupIdsToRetrieve));
    
    int syncGroupCount = 0; 
    if (groupIdsToRetrieve.size() > 0) {
      GcGrouperSync gcGrouperSync = this.getGrouperProvisioner().getGcGrouperSync();
      Map<String, GcGrouperSyncGroup> grouperSyncGroupIdToSyncGroup = gcGrouperSync
          .getGcGrouperSyncGroupDao().groupRetrieveByGroupIds(groupIdsToRetrieve);
      
      for (String groupId: groupIdsToRetrieve) {
        GcGrouperSyncGroup grouperSyncGroup = grouperSyncGroupIdToSyncGroup.get(groupId);
        if (grouperSyncGroup == null) {
          continue;
        }

        ProvisioningGroupWrapper provisioningGroupWrapper = this.grouperProvisioner.retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper().get(groupId);
        if (provisioningGroupWrapper == null) {
          provisioningGroupWrapper = new ProvisioningGroupWrapper();
          provisioningGroupWrapper.setGrouperProvisioner(this.getGrouperProvisioner());
          provisioningGroupWrapper.setGroupId(groupId);
          provisioningGroupWrapper.setGcGrouperSyncGroup(grouperSyncGroup);

          this.getGrouperProvisioner().retrieveGrouperProvisioningData().addAndIndexGroupWrapper(provisioningGroupWrapper);
        } else {
        
          provisioningGroupWrapper.setGcGrouperSyncGroup(grouperSyncGroup);
          this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex()
            .getGrouperSyncGroupIdToProvisioningGroupWrapper().put(grouperSyncGroup.getId(), provisioningGroupWrapper);
        }
        syncGroupCount++;
      }
      
      clearErrorsGroup(grouperSyncGroupIdToSyncGroup.values());
    }
    
    debugMap.put("syncGroupCount_"+logLabel, syncGroupCount);
    debugMap.put("retrieveSyncGroupsMillis_"+logLabel, System.currentTimeMillis() - start);
  }

>>>>>>> 3c25747 Provisioning related changes - make incremental sync more robust
  /**
   * get sync objects from the database
   * @param logLabel
   */
<<<<<<< GROUPER_5_BRANCH
  public void retrieveIncrementalSyncGroups(String logLabel) {
=======
  //TODO remove it after testing
  private void retrieveIncrementalSyncGroups_old() {
>>>>>>> 3c25747 Provisioning related changes - make incremental sync more robust

    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();

    long start = System.currentTimeMillis();

<<<<<<< GROUPER_5_BRANCH
    Set<String> groupIdsToRetrieve = new HashSet<String>();
    Set<String> groupIdsToIgnore = new HashSet<String>();

    for (ProvisioningGroupWrapper provisioningGroupWrapper : this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()) {
      if (provisioningGroupWrapper.getGcGrouperSyncGroup() == null) {
        groupIdsToRetrieve.add(provisioningGroupWrapper.getGroupId());
      } else {
        groupIdsToIgnore.add(provisioningGroupWrapper.getGroupId());
      }
    }
    
    for (ProvisioningMembershipWrapper provisioningMembershipWrapper : this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
      if (provisioningMembershipWrapper.getGroupIdMemberId() != null) {
        String groupId = (String)provisioningMembershipWrapper.getGroupIdMemberId().getKey(0);
        if (!groupIdsToIgnore.contains(groupId)) {
          groupIdsToRetrieve.add(groupId);
=======
>>>>>>> cde0848eaefb94061e688bf390c1349ca347f98b
        }
      }
    }

    debugMap.put("syncGroupsToQuery_"+logLabel,
        GrouperUtil.length(groupIdsToRetrieve));
    
    int syncGroupCount = 0; 
    if (groupIdsToRetrieve.size() > 0) {
      GcGrouperSync gcGrouperSync = this.getGrouperProvisioner().getGcGrouperSync();
      Map<String, GcGrouperSyncGroup> grouperSyncGroupIdToSyncGroup = gcGrouperSync
          .getGcGrouperSyncGroupDao().groupRetrieveByGroupIds(groupIdsToRetrieve);
      
      for (String groupId: groupIdsToRetrieve) {
        GcGrouperSyncGroup grouperSyncGroup = grouperSyncGroupIdToSyncGroup.get(groupId);
        if (grouperSyncGroup == null) {
          continue;
        }

        ProvisioningGroupWrapper provisioningGroupWrapper = this.grouperProvisioner.retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper().get(groupId);
        if (provisioningGroupWrapper == null) {
          provisioningGroupWrapper = new ProvisioningGroupWrapper();
          provisioningGroupWrapper.setGrouperProvisioner(this.getGrouperProvisioner());
          provisioningGroupWrapper.setGroupId(groupId);
          provisioningGroupWrapper.setGcGrouperSyncGroup(grouperSyncGroup);

          this.getGrouperProvisioner().retrieveGrouperProvisioningData().addAndIndexGroupWrapper(provisioningGroupWrapper);
        } else {
        
          provisioningGroupWrapper.setGcGrouperSyncGroup(grouperSyncGroup);
          this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex()
            .getGrouperSyncGroupIdToProvisioningGroupWrapper().put(grouperSyncGroup.getId(), provisioningGroupWrapper);
        }
        syncGroupCount++;
      }
      
      clearErrorsGroup(grouperSyncGroupIdToSyncGroup.values());
    }
    
    debugMap.put("syncGroupCount_"+logLabel, syncGroupCount);
    debugMap.put("retrieveSyncGroupsMillis_"+logLabel, System.currentTimeMillis() - start);
  }

  /**
   * get sync objects from the database
<<<<<<< HEAD
<<<<<<< GROUPER_5_BRANCH
   */
  //TODO remove it after testing
  private void retrieveIncrementalSyncGroups_old() {

    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();

    long start = System.currentTimeMillis();

=======
>>>>>>> 252ebc1 restructure how state is stored in provisioning wrappers
    Set<GcGrouperSyncGroup> gcGrouperSyncGroups = this.grouperProvisioner.retrieveGrouperProvisioningDataSync().getGcGrouperSyncGroups();

    Set<String> groupIdsToRetrieve = new HashSet<String>();

    for (ProvisioningGroupWrapper provisioningGroupWrapper : this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()) {
      if (!StringUtils.isBlank(provisioningGroupWrapper.getGroupId())) {
        groupIdsToRetrieve.add(provisioningGroupWrapper.getGroupId());
      }
    }

    for (ProvisioningMembershipWrapper provisioningMembershipWrapper : this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
      if (provisioningMembershipWrapper.getGroupIdMemberId() != null) {
        groupIdsToRetrieve.add((String)provisioningMembershipWrapper.getGroupIdMemberId().getKey(0));
      }
    }

    this.getGrouperProvisioner().getDebugMap().put("syncGroupsToQuery",
        GrouperUtil.length(groupIdsToRetrieve));

    if (groupIdsToRetrieve.size() > 0) {
      GcGrouperSync gcGrouperSync = this.getGrouperProvisioner().getGcGrouperSync();
      Map<String, GcGrouperSyncGroup> grouperSyncGroupIdToSyncGroup = gcGrouperSync
          .getGcGrouperSyncGroupDao().groupRetrieveByGroupIds(groupIdsToRetrieve);

      gcGrouperSyncGroups
          .addAll(GrouperUtil.nonNull(grouperSyncGroupIdToSyncGroup).values());
    }

    clearErrorsGroup(gcGrouperSyncGroups);
    this.getGrouperProvisioner().getDebugMap().put("syncGroupsFound",
        GrouperUtil.length(gcGrouperSyncGroups));

    debugMap.put("retrieveSyncGroupsMillis", System.currentTimeMillis() - start);
    debugMap.put("syncGroupCount", GrouperUtil.length(gcGrouperSyncGroups));
  }
  
  /**
   * get sync objects from the database
=======
>>>>>>> a6357c0 get rid of sync object data store
=======
>>>>>>> cde0848eaefb94061e688bf390c1349ca347f98b
   * @param logLabel
   */
  public void retrieveIncrementalSyncMembers(String logLabel) {

    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();

    long start = System.currentTimeMillis();

    Set<String> memberIdsToRetrieve = new HashSet<String>();
<<<<<<< GROUPER_5_BRANCH
<<<<<<< GROUPER_5_BRANCH
    Set<String> memberIdsToIgnore = new HashSet<String>();
    
    for (ProvisioningEntityWrapper provisioningEntityWrapper : this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) {
      if (provisioningEntityWrapper.getGcGrouperSyncMember() == null) {
        memberIdsToRetrieve.add(provisioningEntityWrapper.getMemberId());
      } else {
        memberIdsToIgnore.add(provisioningEntityWrapper.getMemberId());
      }
    }
    
    for (ProvisioningMembershipWrapper provisioningMembershipWrapper : 
      this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
<<<<<<< HEAD
      if (provisioningMembershipWrapper.getGroupIdMemberId() != null) {
        String memberId = (String)provisioningMembershipWrapper.getGroupIdMemberId().getKey(1);
        if (!memberIdsToIgnore.contains(memberId)) {
          ProvisioningEntityWrapper provisioningEntityWrapper = this.getGrouperProvisioner()
              .retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper().get(memberId);
          if (provisioningEntityWrapper == null || provisioningEntityWrapper.getGcGrouperSyncMember() == null) {
            memberIdsToRetrieve.add(memberId);
          }
        }
      }
    }

    debugMap.put("syncMembersToQuery_"+logLabel,
        GrouperUtil.length(memberIdsToRetrieve));
    
    int syncMembersCount = 0 ;
    
    if (memberIdsToRetrieve.size() > 0) {
      GcGrouperSync gcGrouperSync = this.getGrouperProvisioner().getGcGrouperSync();
      Map<String, GcGrouperSyncMember> grouperSyncMemberIdToSyncMember = gcGrouperSync
          .getGcGrouperSyncMemberDao().memberRetrieveByMemberIds(memberIdsToRetrieve);
      
      for (String memberId: memberIdsToRetrieve) {
        GcGrouperSyncMember grouperSyncMember = grouperSyncMemberIdToSyncMember.get(memberId);
        if (grouperSyncMember == null) {
          continue;
        }

        ProvisioningEntityWrapper provisioningEntityWrapper = this.grouperProvisioner.retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper().get(memberId);
        if (provisioningEntityWrapper == null) {
          provisioningEntityWrapper = new ProvisioningEntityWrapper();
          provisioningEntityWrapper.setGrouperProvisioner(this.getGrouperProvisioner());
          provisioningEntityWrapper.setMemberId(memberId);
          provisioningEntityWrapper.setGcGrouperSyncMember(grouperSyncMember);
          this.getGrouperProvisioner().retrieveGrouperProvisioningData().addAndIndexEntityWrapper(provisioningEntityWrapper);
        } else {
          provisioningEntityWrapper.setGcGrouperSyncMember(grouperSyncMember);
          this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex()
          .getGrouperSyncMemberIdToProvisioningEntityWrapper().put(grouperSyncMember.getId(), provisioningEntityWrapper);
        }

        syncMembersCount++;
=======
=======
    Set<String> memberIdsToIgnore = new HashSet<String>();
>>>>>>> dad5d51 Provisioning related changes, wip
    
    for (ProvisioningEntityWrapper provisioningEntityWrapper : this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) {
      if (provisioningEntityWrapper.getGcGrouperSyncMember() == null) {
        memberIdsToRetrieve.add(provisioningEntityWrapper.getMemberId());
      } else {
        memberIdsToIgnore.add(provisioningEntityWrapper.getMemberId());
      }
    }
    
    for (ProvisioningMembershipWrapper provisioningMembershipWrapper : this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
=======
>>>>>>> cde0848eaefb94061e688bf390c1349ca347f98b
      if (provisioningMembershipWrapper.getGroupIdMemberId() != null) {
        String memberId = (String)provisioningMembershipWrapper.getGroupIdMemberId().getKey(1);
        if (!memberIdsToIgnore.contains(memberId)) {
          ProvisioningEntityWrapper provisioningEntityWrapper = this.getGrouperProvisioner()
              .retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper().get(memberId);
          if (provisioningEntityWrapper == null || provisioningEntityWrapper.getGcGrouperSyncMember() == null) {
            memberIdsToRetrieve.add(memberId);
          }
        }
      }
    }

    debugMap.put("syncMembersToQuery_"+logLabel,
        GrouperUtil.length(memberIdsToRetrieve));
    
    int syncMembersCount = 0 ;
    
    if (memberIdsToRetrieve.size() > 0) {
      GcGrouperSync gcGrouperSync = this.getGrouperProvisioner().getGcGrouperSync();
      Map<String, GcGrouperSyncMember> grouperSyncMemberIdToSyncMember = gcGrouperSync
          .getGcGrouperSyncMemberDao().memberRetrieveByMemberIds(memberIdsToRetrieve);
      
      for (String memberId: memberIdsToRetrieve) {
        GcGrouperSyncMember grouperSyncMember = grouperSyncMemberIdToSyncMember.get(memberId);
        if (grouperSyncMember == null) {
          continue;
        }

        ProvisioningEntityWrapper provisioningEntityWrapper = this.grouperProvisioner.retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper().get(memberId);
        if (provisioningEntityWrapper == null) {
          provisioningEntityWrapper = new ProvisioningEntityWrapper();
          provisioningEntityWrapper.setGrouperProvisioner(this.getGrouperProvisioner());
          provisioningEntityWrapper.setMemberId(memberId);
          provisioningEntityWrapper.setGcGrouperSyncMember(grouperSyncMember);
          this.getGrouperProvisioner().retrieveGrouperProvisioningData().addAndIndexEntityWrapper(provisioningEntityWrapper);
        } else {
          provisioningEntityWrapper.setGcGrouperSyncMember(grouperSyncMember);
          this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex()
          .getGrouperSyncMemberIdToProvisioningEntityWrapper().put(grouperSyncMember.getId(), provisioningEntityWrapper);
        }
<<<<<<< HEAD
<<<<<<< GROUPER_5_BRANCH
>>>>>>> 3c25747 Provisioning related changes - make incremental sync more robust
=======
        
        
        GcGrouperSyncMember grouperSyncMember = grouperSyncMemberIdToSyncMember.get(provisioningEntityWrapper.getMemberId());
        if (grouperSyncMember == null) {
          continue;
        }
        provisioningEntityWrapper.setGcGrouperSyncMember(grouperSyncMember);
        
=======

>>>>>>> cde0848eaefb94061e688bf390c1349ca347f98b
        syncMembersCount++;
>>>>>>> dad5d51 Provisioning related changes, wip
      }
      
      clearErrorsMember(grouperSyncMemberIdToSyncMember.values());
    }
    
    debugMap.put("syncMemberCount_"+logLabel, syncMembersCount);
    debugMap.put("retrieveSyncMembersMillis_"+logLabel, System.currentTimeMillis() - start);
  }

  /**
   * get sync objects from the database.  all records correspond to a sync group and sync member or its skipped
   */
  public void retrieveIncrementalSyncMemberships() {

    GcGrouperSync gcGrouperSync = this.getGrouperProvisioner().getGcGrouperSync();

    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();

    long start = System.currentTimeMillis();

    Map<MultiKey, GcGrouperSyncMembership> grouperSyncMembershipMap = new HashMap<>();
    
    // ####  memberships by group
    {
      Set<String> groupIdsToRetrieve = new HashSet<String>();

      for (ProvisioningGroupWrapper provisioningGroupWrapper : this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers()) {
        if (!StringUtils.isBlank(provisioningGroupWrapper.getGroupId())) {
          if (provisioningGroupWrapper.getProvisioningStateGroup().isRecalcObjectMemberships()) {
            groupIdsToRetrieve.add(provisioningGroupWrapper.getGroupId());
          }
        }
      }

      if (groupIdsToRetrieve.size() > 0) {
        this.getGrouperProvisioner().getDebugMap().put("syncMembershipsToQueryFromGroup",
            GrouperUtil.length(groupIdsToRetrieve));
        Map<MultiKey, GcGrouperSyncMembership> groupIdMemberIdToGrouperSyncMemberships = gcGrouperSync
            .getGcGrouperSyncMembershipDao()
            .membershipRetrieveByGroupIdsMap(groupIdsToRetrieve);
        this.getGrouperProvisioner().getDebugMap().put("syncMembershipsFromGroup",
            GrouperUtil.length(groupIdMemberIdToGrouperSyncMemberships));
        grouperSyncMembershipMap.putAll(GrouperUtil.nonNull(groupIdMemberIdToGrouperSyncMemberships));
      }
    }

    // ####  memberships by member
    {
      Set<String> memberIdsToRetrieve = new HashSet<String>();

      for (ProvisioningEntityWrapper provisioningEntityWrapper : this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningEntityWrappers()) {
        if (!StringUtils.isBlank(provisioningEntityWrapper.getMemberId())) {
          if (provisioningEntityWrapper.getProvisioningStateEntity().isRecalcObjectMemberships()) {
            memberIdsToRetrieve.add(provisioningEntityWrapper.getMemberId());
          }
        }
      }

      if (memberIdsToRetrieve.size() > 0) {
        this.getGrouperProvisioner().getDebugMap().put("syncMembershipsToQueryFromMember",
            GrouperUtil.length(memberIdsToRetrieve));
        Map<MultiKey, GcGrouperSyncMembership> groupIdMemberIdToGrouperSyncMemberships = gcGrouperSync
            .getGcGrouperSyncMembershipDao()
            .membershipRetrieveByMemberIdsMap(memberIdsToRetrieve);
        this.getGrouperProvisioner().getDebugMap().put("syncMembershipsFromMember",
            GrouperUtil.length(groupIdMemberIdToGrouperSyncMemberships));
        grouperSyncMembershipMap.putAll(GrouperUtil.nonNull(groupIdMemberIdToGrouperSyncMemberships));

      }
    }

    Map<MultiKey, ProvisioningMembershipWrapper> groupUuidMemberUuidToProvisioningMembershipWrapper = this.getGrouperProvisioner()
        .retrieveGrouperProvisioningDataIndex().getGroupUuidMemberUuidToProvisioningMembershipWrapper();
    Map<MultiKey, ProvisioningMembershipWrapper> grouperSyncGroupIdGrouperSyncMemberIdToProvisioningMembershipWrapper = this.getGrouperProvisioner()
        .retrieveGrouperProvisioningDataIndex().getGrouperSyncGroupIdGrouperSyncMemberIdToProvisioningMembershipWrapper();

    for (MultiKey groupIdMemberId : grouperSyncMembershipMap.keySet()) {
      GcGrouperSyncMembership gcGrouperSyncMembership = grouperSyncMembershipMap.get(groupIdMemberId);
      
      ProvisioningMembershipWrapper provisioningMembershipWrapper = groupUuidMemberUuidToProvisioningMembershipWrapper.get(groupIdMemberId);
      
      if (provisioningMembershipWrapper == null) {
        provisioningMembershipWrapper = new ProvisioningMembershipWrapper();
        provisioningMembershipWrapper.setGrouperProvisioner(this.grouperProvisioner);
        provisioningMembershipWrapper.setGroupIdMemberId(groupIdMemberId);
        provisioningMembershipWrapper.setGcGrouperSyncMembership(gcGrouperSyncMembership);

        this.getGrouperProvisioner().retrieveGrouperProvisioningData().addAndIndexMembershipWrapper(provisioningMembershipWrapper);
      } else if (provisioningMembershipWrapper.getGcGrouperSyncMembership() != null) {
        continue;
      } else {
        provisioningMembershipWrapper.setGcGrouperSyncMembership(gcGrouperSyncMembership);
        this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex()
        .getGrouperSyncGroupIdGrouperSyncMemberIdToProvisioningMembershipWrapper().put(new MultiKey(gcGrouperSyncMembership.getGrouperSyncGroupId(), gcGrouperSyncMembership.getGrouperSyncMemberId()), provisioningMembershipWrapper);
        
      }


    }

    // ####  memberships by membership
    {
      Set<MultiKey> groupIdsMemberIdsToRetrieve = new HashSet<MultiKey>();

      for (ProvisioningMembershipWrapper provisioningMembershipWrapper : this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
        if (provisioningMembershipWrapper.getGroupIdMemberId() != null && provisioningMembershipWrapper.getGcGrouperSyncMembership() == null) {
          groupIdsMemberIdsToRetrieve.add(provisioningMembershipWrapper.getGroupIdMemberId());
        }
      }

      if (groupIdsMemberIdsToRetrieve.size() > 0) {
        this.getGrouperProvisioner().getDebugMap().put("syncMembershipsToQuery",
            GrouperUtil.length(groupIdsMemberIdsToRetrieve));
        grouperSyncMembershipMap.putAll(gcGrouperSync
            .getGcGrouperSyncMembershipDao()
            .membershipRetrieveByGroupIdsAndMemberIds(gcGrouperSync.getId(), groupIdsMemberIdsToRetrieve));
        this.getGrouperProvisioner().getDebugMap().put("syncMembershipsFromMembership",
            GrouperUtil.length(grouperSyncMembershipMap));
 
        for (ProvisioningMembershipWrapper provisioningMembershipWrapper : this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {
          if (provisioningMembershipWrapper.getGroupIdMemberId() != null && provisioningMembershipWrapper.getGcGrouperSyncMembership() == null) {
            GcGrouperSyncMembership gcGrouperSyncMembership = grouperSyncMembershipMap.get(provisioningMembershipWrapper.getGroupIdMemberId());
            if (gcGrouperSyncMembership != null) {
              if (provisioningMembershipWrapper.getGcGrouperSyncMembership() != null) {
                continue;
              }
              provisioningMembershipWrapper.setGcGrouperSyncMembership(gcGrouperSyncMembership);
              MultiKey syncGroupIdSyncMemberId = new MultiKey(gcGrouperSyncMembership.getGrouperSyncGroupId(), gcGrouperSyncMembership.getGrouperSyncMemberId());
              provisioningMembershipWrapper.setSyncGroupIdSyncMemberId(syncGroupIdSyncMemberId);
              
              grouperSyncGroupIdGrouperSyncMemberIdToProvisioningMembershipWrapper.put(syncGroupIdSyncMemberId, provisioningMembershipWrapper);
            }
          }
        }
        
      }
    }

    
    clearErrorsMembership(grouperSyncMembershipMap.values());

    debugMap.put("retrieveSyncMembershipsMillis", System.currentTimeMillis() - start);
    debugMap.put("syncMembershipCount", GrouperUtil.length(grouperSyncMembershipMap));

  }
  
  public void retrieveSyncDataFull() {
    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();

    GcGrouperSync gcGrouperSync = this.getGrouperProvisioner().getGcGrouperSync();

    {
      long start = System.currentTimeMillis();
      List<GcGrouperSyncGroup> retrieveAllSyncGroups = grouperProvisioner
          .retrieveGrouperProvisioningSyncDao().retrieveAllSyncGroups();

      for (GcGrouperSyncGroup gcGrouperSyncGroup: retrieveAllSyncGroups) {
        String groupId = gcGrouperSyncGroup.getGroupId();
        ProvisioningGroupWrapper provisioningGroupWrapper = this.grouperProvisioner.retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper().get(groupId);
        if (provisioningGroupWrapper == null) {
          provisioningGroupWrapper = new ProvisioningGroupWrapper();
          provisioningGroupWrapper.setGrouperProvisioner(this.getGrouperProvisioner());
          provisioningGroupWrapper.setGroupId(groupId);
          provisioningGroupWrapper.setGcGrouperSyncGroup(gcGrouperSyncGroup);
          this.getGrouperProvisioner().retrieveGrouperProvisioningData().addAndIndexGroupWrapper(provisioningGroupWrapper);
        } else {
          provisioningGroupWrapper.setGcGrouperSyncGroup(gcGrouperSyncGroup);
          this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex()
          .getGrouperSyncGroupIdToProvisioningGroupWrapper().put(gcGrouperSyncGroup.getId(), provisioningGroupWrapper);
          
        }
        
      }
      
      debugMap.put("retrieveSyncGroupsMillis", System.currentTimeMillis() - start);
      debugMap.put("syncGroupCount",
          GrouperUtil.length(retrieveAllSyncGroups));
    }
    {
      long start = System.currentTimeMillis();
      List<GcGrouperSyncMember> retrieveAllSyncMembers = grouperProvisioner
          .retrieveGrouperProvisioningSyncDao().retrieveAllSyncMembers();
      
      for (GcGrouperSyncMember gcGrouperSyncMember: retrieveAllSyncMembers) {
        
        String memberId = gcGrouperSyncMember.getMemberId();
        
        ProvisioningEntityWrapper provisioningEntityWrapper = this.grouperProvisioner.retrieveGrouperProvisioningDataIndex()
            .getMemberUuidToProvisioningEntityWrapper().get(memberId);
        if (provisioningEntityWrapper == null) {
          provisioningEntityWrapper = new ProvisioningEntityWrapper();
          provisioningEntityWrapper.setGrouperProvisioner(this.getGrouperProvisioner());
          provisioningEntityWrapper.setMemberId(memberId);
          provisioningEntityWrapper.setGcGrouperSyncMember(gcGrouperSyncMember);
          this.getGrouperProvisioner().retrieveGrouperProvisioningData().addAndIndexEntityWrapper(provisioningEntityWrapper);
        } else {
        
          provisioningEntityWrapper.setGcGrouperSyncMember(gcGrouperSyncMember);
          this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex()
            .getGrouperSyncMemberIdToProvisioningEntityWrapper().put(gcGrouperSyncMember.getId(), provisioningEntityWrapper);
        }
        
      }

      
      debugMap.put("retrieveSyncEntitiesMillis", System.currentTimeMillis() - start);
      debugMap.put("syncEntityCount", GrouperUtil.length(retrieveAllSyncMembers));
    }
    {
      long start = System.currentTimeMillis();
      List<GcGrouperSyncMembership> retrieveAllSyncMemberships = grouperProvisioner
          .retrieveGrouperProvisioningSyncDao().retrieveAllSyncMemberships();

      Map<MultiKey, ProvisioningMembershipWrapper> groupUuidMemberUuidToProvisioningMembershipWrapper = this.getGrouperProvisioner()
          .retrieveGrouperProvisioningDataIndex().getGroupUuidMemberUuidToProvisioningMembershipWrapper();
      Map<MultiKey, ProvisioningMembershipWrapper> grouperSyncGroupIdGrouperSyncMemberIdToProvisioningMembershipWrapper = this.getGrouperProvisioner()
          .retrieveGrouperProvisioningDataIndex().getGrouperSyncGroupIdGrouperSyncMemberIdToProvisioningMembershipWrapper();

      for (GcGrouperSyncMembership gcGrouperSyncMembership : retrieveAllSyncMemberships) {
        
        GcGrouperSyncGroup gcGrouperSyncGroup = this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncGroupDao().groupRetrieveById(gcGrouperSyncMembership.getGrouperSyncGroupId());
        GcGrouperSyncMember gcGrouperSyncMember = this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncMemberDao().memberRetrieveById(gcGrouperSyncMembership.getGrouperSyncMemberId());
        
        MultiKey groupIdMemberId = new MultiKey(gcGrouperSyncGroup.getGroupId(), gcGrouperSyncMember.getMemberId());
        
        ProvisioningMembershipWrapper provisioningMembershipWrapper = groupUuidMemberUuidToProvisioningMembershipWrapper.get(gcGrouperSyncMembership);
        
        if (provisioningMembershipWrapper == null) {
          provisioningMembershipWrapper = new ProvisioningMembershipWrapper();
          provisioningMembershipWrapper.setGrouperProvisioner(this.grouperProvisioner);
          provisioningMembershipWrapper.setGroupIdMemberId(groupIdMemberId);
          provisioningMembershipWrapper.setGcGrouperSyncMembership(gcGrouperSyncMembership);
          this.getGrouperProvisioner().retrieveGrouperProvisioningData().addAndIndexMembershipWrapper(provisioningMembershipWrapper);
        } else if (provisioningMembershipWrapper.getGcGrouperSyncMembership() != null) {
          continue;
        } else {
          provisioningMembershipWrapper.setGcGrouperSyncMembership(gcGrouperSyncMembership);
          this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex()
            .getGrouperSyncGroupIdGrouperSyncMemberIdToProvisioningMembershipWrapper().put(new MultiKey(gcGrouperSyncGroup.getId(), gcGrouperSyncMember.getId()), provisioningMembershipWrapper);
        }

      }

      debugMap.put("retrieveSyncMshipsMillis", System.currentTimeMillis() - start);
      debugMap.put("syncMshipCount", GrouperUtil.length(retrieveAllSyncMemberships));
    }

  }

  /**
   * update subject link for these members
   * @param gcGrouperSyncMembersToRefreshSubjectLink
   */
  public void updateSubjectLink(
      List<GcGrouperSyncMember> gcGrouperSyncMembersToRefreshSubjectLink) {
    if (GrouperUtil.length(gcGrouperSyncMembersToRefreshSubjectLink) == 0) {
      return;
    }

    // If using subject attributes and those are not in the member sync object, then resolve the subject, and put in the member sync object
    GrouperProvisioningConfigurationAttributeDbCache grouperProvisioningConfigurationAttributeDbCache0 = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityAttributeDbCaches()[0];
    boolean hasSubjectLinkAttributeValueCache0 = grouperProvisioningConfigurationAttributeDbCache0 != null
        && grouperProvisioningConfigurationAttributeDbCache0.getSource() == GrouperProvisioningConfigurationAttributeDbCacheSource.grouper
        && grouperProvisioningConfigurationAttributeDbCache0.getType() == GrouperProvisioningConfigurationAttributeDbCacheType.subjectTranslationScript
        && !StringUtils.isBlank(grouperProvisioningConfigurationAttributeDbCache0.getTranslationScript());

    GrouperProvisioningConfigurationAttributeDbCache grouperProvisioningConfigurationAttributeDbCache1 = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityAttributeDbCaches()[1];
    boolean hasSubjectLinkAttributeValueCache1 = grouperProvisioningConfigurationAttributeDbCache1 != null
        && grouperProvisioningConfigurationAttributeDbCache1.getSource() == GrouperProvisioningConfigurationAttributeDbCacheSource.grouper
        && grouperProvisioningConfigurationAttributeDbCache1.getType() == GrouperProvisioningConfigurationAttributeDbCacheType.subjectTranslationScript
        && !StringUtils.isBlank(grouperProvisioningConfigurationAttributeDbCache1.getTranslationScript());

    GrouperProvisioningConfigurationAttributeDbCache grouperProvisioningConfigurationAttributeDbCache2 = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityAttributeDbCaches()[2];
    boolean hasSubjectLinkAttributeValueCache2 = grouperProvisioningConfigurationAttributeDbCache2 != null
        && grouperProvisioningConfigurationAttributeDbCache2.getSource() == GrouperProvisioningConfigurationAttributeDbCacheSource.grouper
        && grouperProvisioningConfigurationAttributeDbCache2.getType() == GrouperProvisioningConfigurationAttributeDbCacheType.subjectTranslationScript
        && !StringUtils.isBlank(grouperProvisioningConfigurationAttributeDbCache2.getTranslationScript());

    GrouperProvisioningConfigurationAttributeDbCache grouperProvisioningConfigurationAttributeDbCache3 = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityAttributeDbCaches()[3];
    boolean hasSubjectLinkAttributeValueCache3 = grouperProvisioningConfigurationAttributeDbCache3 != null
        && grouperProvisioningConfigurationAttributeDbCache3.getSource() == GrouperProvisioningConfigurationAttributeDbCacheSource.grouper
        && grouperProvisioningConfigurationAttributeDbCache3.getType() == GrouperProvisioningConfigurationAttributeDbCacheType.subjectTranslationScript
        && !StringUtils.isBlank(grouperProvisioningConfigurationAttributeDbCache3.getTranslationScript());

    if (!hasSubjectLinkAttributeValueCache0 && !hasSubjectLinkAttributeValueCache1
        && !hasSubjectLinkAttributeValueCache2 && !hasSubjectLinkAttributeValueCache3) {
      return;
    }

    int subjectsCannotFindLinkData = 0;

    Set<MultiKey> sourceIdSubjectIds = new HashSet<MultiKey>();

    for (GcGrouperSyncMember gcGrouperSyncMember : gcGrouperSyncMembersToRefreshSubjectLink) {

      this.getGrouperProvisioner().retrieveGrouperProvisioningSyncIntegration().decorateSyncMemberSubjectInformationIfNull(gcGrouperSyncMember, null);
      
      if (!StringUtils.isBlank(gcGrouperSyncMember.getSourceId()) && !StringUtils.isBlank(gcGrouperSyncMember.getSubjectId())) {
        MultiKey sourceIdSubjectId = new MultiKey(gcGrouperSyncMember.getSourceId(),
            gcGrouperSyncMember.getSubjectId());
        sourceIdSubjectIds.add(sourceIdSubjectId);
      }
    }

    Map<MultiKey, Subject> sourceIdSubjectIdToSubject = SubjectFinder
        .findBySourceIdsAndSubjectIds(sourceIdSubjectIds, false, true);

    Set<GcGrouperSyncMember> gcSyncMembersChangedInSubjectLink = new LinkedHashSet<GcGrouperSyncMember>();
    
    for (GcGrouperSyncMember gcGrouperSyncMember : gcGrouperSyncMembersToRefreshSubjectLink) {

      MultiKey sourceIdSubjectId = new MultiKey(gcGrouperSyncMember.getSourceId(),
          gcGrouperSyncMember.getSubjectId());
      Subject subject = sourceIdSubjectIdToSubject.get(sourceIdSubjectId);

      if (subject == null) {
        subjectsCannotFindLinkData++;
        // maybe it didn't get resolved, don't mess with the existing cached data.
        continue;
      }

      Map<String, Object> variableMap = new HashMap<String, Object>();
      variableMap.put("subject", subject);

      if (hasSubjectLinkAttributeValueCache0) {
        String entityAttributeValueCache0Value = GrouperUtil
            .substituteExpressionLanguage(grouperProvisioningConfigurationAttributeDbCache0.getTranslationScript(), variableMap);
        gcGrouperSyncMember.setEntityAttributeValueCache0(entityAttributeValueCache0Value);
      }

      if (hasSubjectLinkAttributeValueCache1) {
        String entityAttributeValueCache1Value = GrouperUtil
            .substituteExpressionLanguage(grouperProvisioningConfigurationAttributeDbCache1.getTranslationScript(), variableMap);
        gcGrouperSyncMember.setEntityAttributeValueCache1(entityAttributeValueCache1Value);
      }

      if (hasSubjectLinkAttributeValueCache2) {
        String entityAttributeValueCache2Value = GrouperUtil
            .substituteExpressionLanguage(grouperProvisioningConfigurationAttributeDbCache2.getTranslationScript(), variableMap);
        gcGrouperSyncMember.setEntityAttributeValueCache2(entityAttributeValueCache2Value);
      }

      if (hasSubjectLinkAttributeValueCache3) {
        String entityAttributeValueCache3Value = GrouperUtil
            .substituteExpressionLanguage(grouperProvisioningConfigurationAttributeDbCache3.getTranslationScript(), variableMap);
        gcGrouperSyncMember.setEntityAttributeValueCache3(entityAttributeValueCache3Value);
      }
      gcSyncMembersChangedInSubjectLink.add(gcGrouperSyncMember);
    }

    if (subjectsCannotFindLinkData > 0) {
      this.grouperProvisioner.getDebugMap().put("subjectsCannotFindLinkData",
          subjectsCannotFindLinkData);
    }
    if (GrouperUtil.length(gcSyncMembersChangedInSubjectLink) > 0) {
      this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.retrieveSubjectLink, gcSyncMembersChangedInSubjectLink);
    }

  }

  /**
   * process the results back to the sync objects
   * @param includeMembershipsIfApplicable true if this group includes memberships if 
   * it is even doing memberships as a group attribute
   * @param grouperTargetGroupsToInsert
   */
  public void processResultsInsertGroups(List<ProvisioningGroup> grouperTargetGroupsToInsert, boolean includeMembershipsIfApplicable) {
    
    for (ProvisioningGroup grouperTargetGroup : GrouperUtil.nonNull(grouperTargetGroupsToInsert)) {
      ProvisioningGroupWrapper provisioningGroupWrapper = grouperTargetGroup.getProvisioningGroupWrapper();
      
      if (provisioningGroupWrapper.getProvisioningStateGroup().isInsertResultProcessed()) {
        continue;
      }
      
      provisioningGroupWrapper.getProvisioningStateGroup().setInsertResultProcessed(true);
      
      GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper.getGcGrouperSyncGroup();
      Timestamp nowTimestamp = new Timestamp(System.currentTimeMillis());

      if (grouperTargetGroup.getException() == null && GrouperUtil.booleanValue(grouperTargetGroup.getProvisioned(), false)) {
        gcGrouperSyncGroup.setInTarget(true);
        gcGrouperSyncGroup.setInTargetStart(nowTimestamp);
        gcGrouperSyncGroup.setInTargetInsertOrExists(true);
        //gcGrouperSyncGroup.setLastGroupMetadataSync(nowTimestamp);
        gcGrouperSyncGroup.setErrorMessage(null);
        gcGrouperSyncGroup.setErrorTimestamp(null);
        if (includeMembershipsIfApplicable) {
          //see if all attributes were synced
          if (GrouperProvisioningBehaviorMembershipType.groupAttributes 
              == this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType()) {

            processResultsInsertUpdateProvisioningUpdatableAttributeMemberships(nowTimestamp,
                grouperTargetGroup);
            
            if (provisioningGroupWrapper.getProvisioningStateGroup().isRecalcGroupMemberships()) {
              gcGrouperSyncGroup.setLastGroupSync(new Timestamp(System.currentTimeMillis()));
              gcGrouperSyncGroup.setLastGroupSyncStart(new Timestamp(this.getGrouperProvisioner().retrieveGrouperProvisioningLogic().getRetrieveDataStartMillisSince1970()));
            }
          }
          
        }
        if (provisioningGroupWrapper.getProvisioningStateGroup().isRecalcObject()) {
          gcGrouperSyncGroup.setLastGroupMetadataSync(new Timestamp(
              this.getGrouperProvisioner().retrieveGrouperProvisioningLogic()
                  .getRetrieveDataStartMillisSince1970()));
        }

        if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningType().isIncrementalSync()) {
          GcGrouperSync gcGrouperSync = this.grouperProvisioner.getGcGrouperSync();
          gcGrouperSync.setGroupCount(GrouperUtil.intValue(gcGrouperSync.getGroupCount(), 0) + 1);
        }
        
      } else {
        gcGrouperSyncGroup.setErrorMessage(GrouperUtil.exception4kZipBase64(GrouperUtil.getFullStackTrace(grouperTargetGroup.getException())));
        gcGrouperSyncGroup.setErrorTimestamp(nowTimestamp);
        this.getGrouperProvisioner().retrieveGrouperProvisioningOutput().addRecordsWithInsertErrors(1);
      }
    }
  }

  public void processResultsInserts(GrouperProvisioningLists targetObjectInserts) {
    if (targetObjectInserts == null) {
      return;
    }
    processResultsInsertGroups(targetObjectInserts.getProvisioningGroups(), true);
    processResultsInsertEntities(targetObjectInserts.getProvisioningEntities(), true);
    processResultsInsertMemberships(targetObjectInserts.getProvisioningMemberships());
  }

  public void processResultsUpdatesFull(GrouperProvisioningLists targetObjectUpdates) {
    if (targetObjectUpdates == null) {
      return;
    }

    processResultsUpdateGroupsFull(targetObjectUpdates.getProvisioningGroups(), true);
    processResultsUpdateEntitiesFull(targetObjectUpdates.getProvisioningEntities(), true);
    processResultsUpdateMemberships(targetObjectUpdates.getProvisioningMemberships());

  }

  public void processResultsDeletes(GrouperProvisioningLists targetObjectDeletes) {
    if (targetObjectDeletes == null) {
      return;
    }
    processResultsDeleteGroups(targetObjectDeletes.getProvisioningGroups(), true);
    processResultsDeleteEntities(targetObjectDeletes.getProvisioningEntities(), true);
    processResultsDeleteMemberships(targetObjectDeletes.getProvisioningMemberships());

  }

  public void processResultsReplaces(
      GrouperProvisioningReplacesObjects targetObjectReplaces) {
    if (targetObjectReplaces == null) {
      return;
    }

    Set<String> groupIds = new HashSet<String>();
    
    for (ProvisioningGroup provisioningGroup : targetObjectReplaces
        .getProvisioningMemberships().keySet()) {
      groupIds.add(provisioningGroup.getProvisioningGroupWrapper().getGroupId());
    }
    
    Map<String, List<ProvisioningMembershipWrapper>> groupIdToListOfExistingMembershipWrappers = new HashMap<>();
    
    // loop through all sync memberships and find the ones for this provisioningGroup
    for (ProvisioningMembershipWrapper provisioningMembershipWrapper :  
      this.getGrouperProvisioner().retrieveGrouperProvisioningData().getProvisioningMembershipWrappers()) {

      if (provisioningMembershipWrapper.getGcGrouperSyncMembership() == null) {
        continue;
      }
      
      String groupId = (String)provisioningMembershipWrapper.getGroupIdMemberId().getKey(0);
      
      if (groupIds.contains(groupId)) {
        
        List<ProvisioningMembershipWrapper> membershipWrappers = groupIdToListOfExistingMembershipWrappers.get(groupId);
        
        if (membershipWrappers == null) {
          membershipWrappers = new ArrayList<>();
          groupIdToListOfExistingMembershipWrappers.put(groupId, membershipWrappers);
        }
            
        membershipWrappers.add(provisioningMembershipWrapper);
        
      }
    }      
      
    
    for (ProvisioningGroup provisioningGroup : targetObjectReplaces.getProvisioningMemberships().keySet()) {

      Set<ProvisioningMembershipWrapper> provisioningMembershipWrappersNewForGroup = new HashSet<>();
      
      List<ProvisioningMembership> provisioningMembershipsNewForGroup = GrouperUtil.nonNull(targetObjectReplaces
          .getProvisioningMemberships().get(provisioningGroup));

      List<ProvisioningMembershipWrapper> membershipWrappersExisting = 
          GrouperUtil.nonNull(groupIdToListOfExistingMembershipWrappers.get(provisioningGroup.getProvisioningGroupWrapper().getGroupId()));

      // handle ones that are in target if this is a state change
      for (ProvisioningMembership grouperTargetMembership : provisioningMembershipsNewForGroup) {
        ProvisioningMembershipWrapper provisioningMembershipWrapper = grouperTargetMembership
            .getProvisioningMembershipWrapper();
        
        provisioningMembershipWrappersNewForGroup.add(provisioningMembershipWrapper);
        
        GcGrouperSyncMembership gcGrouperSyncMembership = provisioningMembershipWrapper
            .getGcGrouperSyncMembership();
 
        if (!gcGrouperSyncMembership.isInTarget()) {
          // insert or update

          Timestamp nowTimestamp = new Timestamp(System.currentTimeMillis());

          if (grouperTargetMembership.getException() == null && GrouperUtil
              .booleanValue(grouperTargetMembership.getProvisioned(), false)) {
            gcGrouperSyncMembership.setInTarget(true);
            gcGrouperSyncMembership.setInTargetStart(nowTimestamp);
            gcGrouperSyncMembership.setInTargetInsertOrExists(true);
            gcGrouperSyncMembership.setErrorMessage(null);
            gcGrouperSyncMembership.setErrorTimestamp(null);
            
            if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningType().isIncrementalSync()) {
              GcGrouperSync gcGrouperSync = this.grouperProvisioner.getGcGrouperSync();
              if (gcGrouperSync.getRecordsCount() != null) {
                gcGrouperSync.setRecordsCount(gcGrouperSync.getRecordsCount() + 1);
              }
            }
            
          } else {
            gcGrouperSyncMembership
                .setErrorMessage(grouperTargetMembership.getException() == null ? null
                    : GrouperUtil
                        .getFullStackTrace(grouperTargetMembership.getException()));
            gcGrouperSyncMembership.setErrorTimestamp(nowTimestamp);
            this.getGrouperProvisioner().retrieveGrouperProvisioningOutput()
                .addRecordsWithInsertErrors(1);
          }
        }

      }

      // look for ones not in target
      for (ProvisioningMembershipWrapper provisioningMembershipWrapper : membershipWrappersExisting) {

        // if it is in the group now, then ignore
        if (provisioningMembershipWrappersNewForGroup.contains(provisioningMembershipWrapper)) {
          continue;
        }
        
        GcGrouperSyncMembership gcGrouperSyncMembership = provisioningMembershipWrapper.getGcGrouperSyncMembership();

        Timestamp nowTimestamp = new Timestamp(System.currentTimeMillis());
        ProvisioningMembership grouperTargetMembership = provisioningMembershipWrapper == null
            ? null
            : provisioningMembershipWrapper.getTargetMembership();

        if ((grouperTargetMembership == null || provisioningMembershipWrapper.getProvisioningStateMembership().isDelete())
            || (grouperTargetMembership.getException() == null && GrouperUtil
                .booleanValue(grouperTargetMembership.getProvisioned(), false))) {
          if (gcGrouperSyncMembership != null) {

            if (gcGrouperSyncMembership.isInTarget()) {
              gcGrouperSyncMembership.setInTarget(false);
              gcGrouperSyncMembership.setInTargetEnd(nowTimestamp);
              gcGrouperSyncMembership.setErrorMessage(null);
              gcGrouperSyncMembership.setErrorTimestamp(null);
              
              if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningType().isIncrementalSync()) {
                GcGrouperSync gcGrouperSync = this.grouperProvisioner.getGcGrouperSync();
                if (gcGrouperSync.getRecordsCount() != null) {
                  gcGrouperSync.setRecordsCount(gcGrouperSync.getRecordsCount() - 1);
                }
              }
              
            }
            
          }
        } else {
          if (gcGrouperSyncMembership != null) {
            gcGrouperSyncMembership
                .setErrorMessage(grouperTargetMembership.getException() == null ? null
                    : GrouperUtil
                        .getFullStackTrace(grouperTargetMembership.getException()));
            gcGrouperSyncMembership.setErrorTimestamp(nowTimestamp);
          }
          this.getGrouperProvisioner().retrieveGrouperProvisioningOutput()
              .addRecordsWithDeleteErrors(1);
        }

      }

    }

  }

  /**
   * process the results back to the sync objects
   * @param includeMembershipsIfApplicable true if this group includes memberships if 
   * it is even doing memberships as a group attribute
   * @param grouperTargetGroupsToInsert
   */
  public void processResultsInsertEntities(
      List<ProvisioningEntity> grouperTargetGroupsToInsert,
      boolean includeMembershipsIfApplicable) {

    for (ProvisioningEntity grouperTargetEntity : GrouperUtil
        .nonNull(grouperTargetGroupsToInsert)) {
      ProvisioningEntityWrapper provisioningEntityWrapper = grouperTargetEntity
          .getProvisioningEntityWrapper();
      
      if (provisioningEntityWrapper.getProvisioningStateEntity().isInsertResultProcessed()) {
        continue;
      }
      
      provisioningEntityWrapper.getProvisioningStateEntity().setInsertResultProcessed(true);
      
      GcGrouperSyncMember gcGrouperSyncMember = provisioningEntityWrapper
          .getGcGrouperSyncMember();
      Timestamp nowTimestamp = new Timestamp(System.currentTimeMillis());

      if (grouperTargetEntity.getException() == null
          && GrouperUtil.booleanValue(grouperTargetEntity.getProvisioned(), false)) {
        gcGrouperSyncMember.setInTarget(true);
        gcGrouperSyncMember.setInTargetStart(nowTimestamp);
        gcGrouperSyncMember.setInTargetInsertOrExists(true);
        //gcGrouperSyncMember.setLastUserMetadataSync(nowTimestamp);
        gcGrouperSyncMember.setErrorMessage(null);
        gcGrouperSyncMember.setErrorTimestamp(null);
        if (includeMembershipsIfApplicable) {
          //see if all attributes were synced
          if (GrouperProvisioningBehaviorMembershipType.entityAttributes == this.grouperProvisioner
              .retrieveGrouperProvisioningBehavior()
              .getGrouperProvisioningBehaviorMembershipType()) {

            processResultsInsertUpdateProvisioningUpdatableAttributeMemberships(
                nowTimestamp,
                grouperTargetEntity);
          }
        }
        
        if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningType().isIncrementalSync()) {
          GcGrouperSync gcGrouperSync = this.grouperProvisioner.getGcGrouperSync();
          gcGrouperSync.setUserCount(GrouperUtil.intValue(gcGrouperSync.getUserCount(), 0) + 1);
        }
        
      } else {
        gcGrouperSyncMember
            .setErrorMessage(grouperTargetEntity.getException() == null ? null
                : GrouperUtil.getFullStackTrace(grouperTargetEntity.getException()));
        gcGrouperSyncMember.setErrorTimestamp(nowTimestamp);
        this.getGrouperProvisioner().retrieveGrouperProvisioningOutput()
            .addRecordsWithInsertErrors(1);
      }
    }
  }

  /**
   * process the results back to the sync objects
   * @param grouperTargetMembershipsToInsert
   */
  public void processResultsInsertMemberships(
      List<ProvisioningMembership> grouperTargetMembershipsToInsert) {

    for (ProvisioningMembership grouperTargetMembership : GrouperUtil
        .nonNull(grouperTargetMembershipsToInsert)) {
      ProvisioningMembershipWrapper provisioningMembershipWrapper = grouperTargetMembership
          .getProvisioningMembershipWrapper();
      
      if (provisioningMembershipWrapper.getProvisioningStateMembership().isInsertResultProcessed()) {
        continue;
      }
      
      provisioningMembershipWrapper.getProvisioningStateMembership().setInsertResultProcessed(true);
      
      GcGrouperSyncMembership gcGrouperSyncMembership = provisioningMembershipWrapper
          .getGcGrouperSyncMembership();
      Timestamp nowTimestamp = new Timestamp(System.currentTimeMillis());

      if (grouperTargetMembership.getException() == null
          && GrouperUtil.booleanValue(grouperTargetMembership.getProvisioned(), false)) {
        gcGrouperSyncMembership.setInTarget(true);
        gcGrouperSyncMembership.setInTargetStart(nowTimestamp);
        gcGrouperSyncMembership.setInTargetInsertOrExists(true);
        gcGrouperSyncMembership.setErrorMessage(null);
        gcGrouperSyncMembership.setErrorTimestamp(null);
        
        if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningType().isIncrementalSync()) {
          GcGrouperSync gcGrouperSync = this.grouperProvisioner.getGcGrouperSync();
          if (gcGrouperSync.getRecordsCount() != null) {
            gcGrouperSync.setRecordsCount(GrouperUtil.intValue(gcGrouperSync.getRecordsCount(), 0) + 1);
          }
        }
        
      } else {
        gcGrouperSyncMembership
            .setErrorMessage(grouperTargetMembership.getException() == null ? null
                : GrouperUtil.getFullStackTrace(grouperTargetMembership.getException()));
        gcGrouperSyncMembership.setErrorTimestamp(nowTimestamp);
        this.getGrouperProvisioner().retrieveGrouperProvisioningOutput()
            .addRecordsWithInsertErrors(1);
      }
    }
  }

  /**
   * process the results back to the sync objects
   * @param includeMembershipsIfApplicable true if this group includes memberships if 
   * it is even doing memberships as a group attribute
   * @param grouperTargetGroupsToInsert
   */
  public void processResultsUpdateEntitiesFull(
      List<ProvisioningEntity> grouperTargetGroupsToInsert,
      boolean includeMembershipsIfApplicable) {

    for (ProvisioningEntity grouperTargetEntity : GrouperUtil
        .nonNull(grouperTargetGroupsToInsert)) {
      ProvisioningEntityWrapper provisioningEntityWrapper = grouperTargetEntity
          .getProvisioningEntityWrapper();
      
      if (provisioningEntityWrapper.getProvisioningStateEntity().isUpdateResultProcessed()) {
        continue;
      }
      
      provisioningEntityWrapper.getProvisioningStateEntity().setUpdateResultProcessed(true);
      
      GcGrouperSyncMember gcGrouperSyncMember = provisioningEntityWrapper
          .getGcGrouperSyncMember();
      Timestamp nowTimestamp = new Timestamp(System.currentTimeMillis());

      if (grouperTargetEntity.getException() == null
          && GrouperUtil.booleanValue(grouperTargetEntity.getProvisioned(), false)) {
        //gcGrouperSyncMember.setLastUserMetadataSync(nowTimestamp);
        gcGrouperSyncMember.setErrorMessage(null);
        gcGrouperSyncMember.setErrorTimestamp(null);
        if (includeMembershipsIfApplicable) {
          //see if all attributes were synced
          if (GrouperProvisioningBehaviorMembershipType.entityAttributes == this.grouperProvisioner
              .retrieveGrouperProvisioningBehavior()
              .getGrouperProvisioningBehaviorMembershipType()) {

            processResultsInsertUpdateProvisioningUpdatableAttributeMemberships(
                nowTimestamp,
                grouperTargetEntity);
          }
        }
      } else {
        gcGrouperSyncMember
            .setErrorMessage(grouperTargetEntity.getException() == null ? null
                : GrouperUtil.getFullStackTrace(grouperTargetEntity.getException()));
        gcGrouperSyncMember.setErrorTimestamp(nowTimestamp);
        this.getGrouperProvisioner().retrieveGrouperProvisioningOutput()
            .addRecordsWithUpdateErrors(1);
      }
    }
  }

  /**
   * process the results back to the sync objects
   * @param includeMembershipsIfApplicable true if this group includes memberships if 
   * it is even doing memberships as a group attribute
   * @param grouperTargetGroupsToInsert
   */
  public void processResultsUpdateGroupsFull(
      List<ProvisioningGroup> grouperTargetGroupsToInsert,
      boolean includeMembershipsIfApplicable) {

    Timestamp nowTimestamp = new Timestamp(System.currentTimeMillis());

    for (ProvisioningGroup grouperTargetGroup : GrouperUtil
        .nonNull(grouperTargetGroupsToInsert)) {
      ProvisioningGroupWrapper provisioningGroupWrapper = grouperTargetGroup
          .getProvisioningGroupWrapper();
      
      if (provisioningGroupWrapper.getProvisioningStateGroup().isUpdateResultProcessed()) {
        continue;
      }
      
      provisioningGroupWrapper.getProvisioningStateGroup().setUpdateResultProcessed(true);
      
      GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper
          .getGcGrouperSyncGroup();

      if (grouperTargetGroup.getException() == null
          && GrouperUtil.booleanValue(grouperTargetGroup.getProvisioned(), false)) {
        //gcGrouperSyncGroup.setLastGroupMetadataSync(nowTimestamp);
        gcGrouperSyncGroup.setErrorMessage(null);
        gcGrouperSyncGroup.setErrorTimestamp(null);
        if (includeMembershipsIfApplicable) {
          //see if all attributes were synced
          if (GrouperProvisioningBehaviorMembershipType.groupAttributes == this.grouperProvisioner
              .retrieveGrouperProvisioningBehavior()
              .getGrouperProvisioningBehaviorMembershipType()) {

            processResultsInsertUpdateProvisioningUpdatableAttributeMemberships(
                nowTimestamp,
                grouperTargetGroup);

            if (provisioningGroupWrapper.getProvisioningStateGroup().isRecalcGroupMemberships()) {
              gcGrouperSyncGroup
                  .setLastGroupSync(new Timestamp(System.currentTimeMillis()));
              gcGrouperSyncGroup.setLastGroupSyncStart(new Timestamp(
                  this.getGrouperProvisioner().retrieveGrouperProvisioningLogic()
                      .getRetrieveDataStartMillisSince1970()));
            }

          }
        }
        if (provisioningGroupWrapper.getProvisioningStateGroup().isRecalcObject()) {
          gcGrouperSyncGroup.setLastGroupMetadataSync(new Timestamp(
              this.getGrouperProvisioner().retrieveGrouperProvisioningLogic()
                  .getRetrieveDataStartMillisSince1970()));
        }

      } else {
        
        if (GrouperProvisioningBehaviorMembershipType.groupAttributes == this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType()) {
            processResultsInsertUpdateProvisioningUpdatableAttributeMemberships(nowTimestamp, grouperTargetGroup);
        }
        
        gcGrouperSyncGroup
            .setErrorMessage(GrouperUtil.exception4kZipBase64(GrouperUtil.getFullStackTrace(grouperTargetGroup.getException())));
        gcGrouperSyncGroup.setErrorTimestamp(nowTimestamp);
        this.getGrouperProvisioner().retrieveGrouperProvisioningOutput()
            .addRecordsWithUpdateErrors(1);
      }
    }
  }

  public void processResultsInsertUpdateProvisioningUpdatableAttributeMemberships(
      Timestamp nowTimestamp,
      ProvisioningUpdatable provisioningUpdatable) {
    boolean fullSyncSuccess = true;
    // see if all attributes were processed
    for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil
        .nonNull(provisioningUpdatable.getInternal_objectChanges())) {
      if (provisioningObjectChange.getException() != null || !GrouperUtil
          .booleanValue(provisioningObjectChange.getProvisioned(), false)) {
        fullSyncSuccess = false;
        break;
      }
    }
    if (fullSyncSuccess) {
      //gcGrouperSyncGroup.setLastGroupSync(nowTimestamp);
    }
    
    String membershipAttributeName = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getAttributeNameForMemberships();
    
    // see if all attributes were processed
    for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil
        .nonNull(provisioningUpdatable.getInternal_objectChanges())) {
      if (provisioningObjectChange.getException() != null || !GrouperUtil
          .booleanValue(provisioningObjectChange.getProvisioned(), false)) {
        continue;
      }
      if (StringUtils.equals(membershipAttributeName, provisioningObjectChange.getAttributeName())) {
        
        ProvisioningAttribute provisioningAttribute = provisioningUpdatable
            .getAttributes().get(provisioningObjectChange.getAttributeName());
        Map<Object, ProvisioningMembershipWrapper> valueToProvisioningMembershipWrapper = provisioningAttribute == null ? null : provisioningAttribute
            .getValueToProvisioningMembershipWrapper();
        if (valueToProvisioningMembershipWrapper != null) {
          if (provisioningObjectChange
              .getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.insert) {
            ProvisioningMembershipWrapper provisioningMembershipWrapper = valueToProvisioningMembershipWrapper
                .get(provisioningObjectChange.getNewValue());
            // if this is a default value there might not be a membership in place
            if (provisioningMembershipWrapper != null) {
              GcGrouperSyncMembership gcGrouperSyncMembership = provisioningMembershipWrapper
                  .getGcGrouperSyncMembership();
              gcGrouperSyncMembership.setErrorMessage(null);
              gcGrouperSyncMembership.setErrorTimestamp(null);
              gcGrouperSyncMembership.setInTarget(true);
              gcGrouperSyncMembership.setInTargetStart(nowTimestamp);
              gcGrouperSyncMembership.setInTargetInsertOrExists(true);
            }
          } else if (provisioningObjectChange
              .getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.delete) {
            ProvisioningMembershipWrapper provisioningMembershipWrapper = valueToProvisioningMembershipWrapper
                .get(provisioningObjectChange.getOldValue());
            // if there is a default untracked value, this might be null
            if (provisioningMembershipWrapper != null) {
              GcGrouperSyncMembership gcGrouperSyncMembership = provisioningMembershipWrapper
                  .getGcGrouperSyncMembership();
              gcGrouperSyncMembership.setErrorMessage(null);
              gcGrouperSyncMembership.setErrorTimestamp(null);
              gcGrouperSyncMembership.setInTarget(false);
              gcGrouperSyncMembership.setInTargetEnd(nowTimestamp);
            }
          }
        }

      }
    }
  }

  /**
   * process the results back to the sync objects
   * @param grouperTargetMembershipsToInsert
   */
  public void processResultsUpdateMemberships(
      List<ProvisioningMembership> grouperTargetMembershipsToInsert) {

    for (ProvisioningMembership grouperTargetMembership : GrouperUtil
        .nonNull(grouperTargetMembershipsToInsert)) {
      ProvisioningMembershipWrapper provisioningMembershipWrapper = grouperTargetMembership
          .getProvisioningMembershipWrapper();
      
      if (provisioningMembershipWrapper.getProvisioningStateMembership().isUpdateResultProcessed()) {
        continue;
      }
      
      provisioningMembershipWrapper.getProvisioningStateMembership().setUpdateResultProcessed(true);
      
      GcGrouperSyncMembership gcGrouperSyncMembership = provisioningMembershipWrapper
          .getGcGrouperSyncMembership();
      Timestamp nowTimestamp = new Timestamp(System.currentTimeMillis());

      if (grouperTargetMembership.getException() == null
          && GrouperUtil.booleanValue(grouperTargetMembership.getProvisioned(), false)) {
        gcGrouperSyncMembership.setErrorMessage(null);
        gcGrouperSyncMembership.setErrorTimestamp(null);
      } else {
        gcGrouperSyncMembership
            .setErrorMessage(grouperTargetMembership.getException() == null ? null
                : GrouperUtil.getFullStackTrace(grouperTargetMembership.getException()));
        gcGrouperSyncMembership.setErrorTimestamp(nowTimestamp);
        this.getGrouperProvisioner().retrieveGrouperProvisioningOutput()
            .addRecordsWithUpdateErrors(1);
      }
    }
  }

  /**
   * process the results back to the sync objects
   * @param includeMembershipsIfApplicable true if this group includes memberships if 
   * it is even doing memberships as a group attribute
   * @param grouperTargetGroupsToInsert
   */
  public void processResultsDeleteEntities(
      List<ProvisioningEntity> grouperTargetGroupsToInsert,
      boolean includeMembershipsIfApplicable) {

    for (ProvisioningEntity grouperTargetEntity : GrouperUtil
        .nonNull(grouperTargetGroupsToInsert)) {
      ProvisioningEntityWrapper provisioningEntityWrapper = grouperTargetEntity
          .getProvisioningEntityWrapper();
      
      if (provisioningEntityWrapper.getProvisioningStateEntity().isDeleteResultProcessed()) {
        continue;
      }
      
      provisioningEntityWrapper.getProvisioningStateEntity().setDeleteResultProcessed(true);
      
      GcGrouperSyncMember gcGrouperSyncMember = provisioningEntityWrapper
          .getGcGrouperSyncMember();

      if (gcGrouperSyncMember == null) {
        this.getGrouperProvisioner().retrieveGrouperProvisioningOutput()
            .debugMapAdd("gcGrouperSyncMemberDeleteMissing", 1);
      }

      Timestamp nowTimestamp = new Timestamp(System.currentTimeMillis());

      if (grouperTargetEntity.getException() == null
          && GrouperUtil.booleanValue(grouperTargetEntity.getProvisioned(), false)) {
        if (gcGrouperSyncMember != null) {
          gcGrouperSyncMember.setInTarget(false);
          gcGrouperSyncMember.setInTargetEnd(nowTimestamp);
          //gcGrouperSyncMember.setLastUserMetadataSync(nowTimestamp);
          gcGrouperSyncMember.setErrorMessage(null);
          gcGrouperSyncMember.setErrorTimestamp(null);
          if (includeMembershipsIfApplicable) {
            //see if all attributes were synced
            if (GrouperProvisioningBehaviorMembershipType.entityAttributes == this.grouperProvisioner
                .retrieveGrouperProvisioningBehavior()
                .getGrouperProvisioningBehaviorMembershipType()) {
              boolean fullSyncSuccess = true;
              // see if all attributes were processed
              for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil
                  .nonNull(grouperTargetEntity.getInternal_objectChanges())) {
                if (provisioningObjectChange.getException() != null || !GrouperUtil
                    .booleanValue(provisioningObjectChange.getProvisioned(), false)) {
                  fullSyncSuccess = false;
                  break;
                }
              }
              if (fullSyncSuccess) {
                //gcGrouperSyncMember.setLastUserSync(nowTimestamp);
              }
            }
          }
          
          if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningType().isIncrementalSync()) {
            GcGrouperSync gcGrouperSync = this.grouperProvisioner.getGcGrouperSync();
            gcGrouperSync.setUserCount(GrouperUtil.intValue(gcGrouperSync.getUserCount(), 0) - 1);
          }
          
        }
      } else {
        if (gcGrouperSyncMember != null) {
          gcGrouperSyncMember
              .setErrorMessage(grouperTargetEntity.getException() == null ? null
                  : GrouperUtil.getFullStackTrace(grouperTargetEntity.getException()));
          gcGrouperSyncMember.setErrorTimestamp(nowTimestamp);
        }
        this.getGrouperProvisioner().retrieveGrouperProvisioningOutput()
            .addRecordsWithDeleteErrors(1);
      }
    }
  }

  /**
   * process the results back to the sync objects
   * @param includeMembershipsIfApplicable true if this group includes memberships if 
   * it is even doing memberships as a group attribute
   * @param grouperTargetGroupsDeleted
   */
  public void processResultsDeleteGroups(
      List<ProvisioningGroup> grouperTargetGroupsDeleted,
      boolean includeMembershipsIfApplicable) {

    for (ProvisioningGroup grouperTargetGroup : GrouperUtil
        .nonNull(grouperTargetGroupsDeleted)) {
      ProvisioningGroupWrapper provisioningGroupWrapper = grouperTargetGroup
          .getProvisioningGroupWrapper();
      
      if (provisioningGroupWrapper.getProvisioningStateGroup().isDeleteResultProcessed()) {
        continue;
      }
      
      provisioningGroupWrapper.getProvisioningStateGroup().setDeleteResultProcessed(true);
      
      GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper
          .getGcGrouperSyncGroup();
      if (gcGrouperSyncGroup == null) {
        this.getGrouperProvisioner().retrieveGrouperProvisioningOutput()
            .debugMapAdd("gcGrouperSyncGroupDeleteMissing", 1);
      }
      Timestamp nowTimestamp = new Timestamp(System.currentTimeMillis());

      if (grouperTargetGroup.getException() == null
          && GrouperUtil.booleanValue(grouperTargetGroup.getProvisioned(), false)) {
        if (gcGrouperSyncGroup != null) {
          gcGrouperSyncGroup.setInTarget(false);
          gcGrouperSyncGroup.setInTargetEnd(nowTimestamp);
          //gcGrouperSyncGroup.setLastGroupMetadataSync(nowTimestamp);
          gcGrouperSyncGroup.setErrorMessage(null);
          gcGrouperSyncGroup.setErrorTimestamp(null);
          if (includeMembershipsIfApplicable) {
            //see if all attributes were synced
            if (GrouperProvisioningBehaviorMembershipType.groupAttributes == this.grouperProvisioner
                .retrieveGrouperProvisioningBehavior()
                .getGrouperProvisioningBehaviorMembershipType()) {
              boolean fullSyncSuccess = true;
              // see if all attributes were processed
              for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil
                  .nonNull(grouperTargetGroup.getInternal_objectChanges())) {
                if (provisioningObjectChange.getException() != null || !GrouperUtil
                    .booleanValue(provisioningObjectChange.getProvisioned(), false)) {
                  fullSyncSuccess = false;
                  break;
                }
              }

              if (fullSyncSuccess) {
                if (provisioningGroupWrapper.getProvisioningStateGroup().isRecalcGroupMemberships()) {
                       gcGrouperSyncGroup
                      .setLastGroupSync(new Timestamp(System.currentTimeMillis()));
                  gcGrouperSyncGroup.setLastGroupSyncStart(new Timestamp(
                      this.getGrouperProvisioner().retrieveGrouperProvisioningLogic()
                          .getRetrieveDataStartMillisSince1970()));
                }
              }
            }
          }
          if (provisioningGroupWrapper.getProvisioningStateGroup().isRecalcObject()) {
            gcGrouperSyncGroup.setLastGroupMetadataSync(new Timestamp(
                this.getGrouperProvisioner().retrieveGrouperProvisioningLogic()
                    .getRetrieveDataStartMillisSince1970()));
          }
          
          if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningType().isIncrementalSync()) {
            GcGrouperSync gcGrouperSync = this.grouperProvisioner.getGcGrouperSync();
            gcGrouperSync.setGroupCount(GrouperUtil.intValue(gcGrouperSync.getGroupCount(), 0) - 1);
          }
          
        }
      } else {
        if (gcGrouperSyncGroup != null) {
          gcGrouperSyncGroup
              .setErrorMessage(GrouperUtil.exception4kZipBase64(GrouperUtil.getFullStackTrace(grouperTargetGroup.getException())));
          gcGrouperSyncGroup.setErrorTimestamp(nowTimestamp);
        }
        this.getGrouperProvisioner().retrieveGrouperProvisioningOutput()
            .addRecordsWithDeleteErrors(1);
      }
    }
    
    Set<ProvisioningMembershipWrapper> membershipWrappers = GrouperUtil.nonNull(this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningMembershipWrappers());
    // look for ones not in target
    for (ProvisioningMembershipWrapper provisioningMembershipWrapper : membershipWrappers) {
      
      boolean groupIsDeleted = provisioningMembershipWrapper.getProvisioningGroupWrapper() == null ? false: provisioningMembershipWrapper.getProvisioningGroupWrapper().getProvisioningStateGroup().isDeleteResultProcessed();
      boolean entityIsDeleted = provisioningMembershipWrapper.getProvisioningEntityWrapper() == null ? false: provisioningMembershipWrapper.getProvisioningEntityWrapper().getProvisioningStateEntity().isDeleteResultProcessed();
      
      if (!groupIsDeleted && !entityIsDeleted) {
        continue;
      }
      
      GcGrouperSyncMembership gcGrouperSyncMembership = provisioningMembershipWrapper.getGcGrouperSyncMembership();

      Timestamp nowTimestamp = new Timestamp(System.currentTimeMillis());
      ProvisioningMembership grouperTargetMembership = provisioningMembershipWrapper == null
          ? null
          : provisioningMembershipWrapper.getTargetMembership();
      
      if (grouperTargetMembership != null && grouperTargetMembership.getException() != null) {
        continue;
      }
      
      if (gcGrouperSyncMembership != null) {

        if (gcGrouperSyncMembership.isInTarget()) {
          gcGrouperSyncMembership.setInTarget(false);
          gcGrouperSyncMembership.setInTargetEnd(nowTimestamp);
          gcGrouperSyncMembership.setErrorMessage(null);
          gcGrouperSyncMembership.setErrorTimestamp(null);
          
          if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningType().isIncrementalSync()) {
            GcGrouperSync gcGrouperSync = this.grouperProvisioner.getGcGrouperSync();
            if (gcGrouperSync.getRecordsCount() != null) {
              gcGrouperSync.setRecordsCount(gcGrouperSync.getRecordsCount() - 1);
            }
          }
          
        }
        
      }
    }
  }

  /**
   * process the results back to the sync objects
   * @param grouperTargetMembershipsToDelete
   */
  public void processResultsDeleteMemberships(
      List<ProvisioningMembership> grouperTargetMembershipsToDelete) {

    for (ProvisioningMembership grouperTargetMembership : GrouperUtil
        .nonNull(grouperTargetMembershipsToDelete)) {
      ProvisioningMembershipWrapper provisioningMembershipWrapper = grouperTargetMembership
          .getProvisioningMembershipWrapper();
      
      if (provisioningMembershipWrapper.getProvisioningStateMembership().isDeleteResultProcessed()) {
        continue;
      }
      
      provisioningMembershipWrapper.getProvisioningStateMembership().setDeleteResultProcessed(true);
      
      GcGrouperSyncMembership gcGrouperSyncMembership = provisioningMembershipWrapper
          .getGcGrouperSyncMembership();

      if (gcGrouperSyncMembership == null) {
        this.getGrouperProvisioner().retrieveGrouperProvisioningOutput()
            .debugMapAdd("gcGrouperSyncMembershipDeleteMissing", 1);
      }

      Timestamp nowTimestamp = new Timestamp(System.currentTimeMillis());

      if (grouperTargetMembership.getException() == null
          && GrouperUtil.booleanValue(grouperTargetMembership.getProvisioned(), false)) {
        if (gcGrouperSyncMembership != null) {
          gcGrouperSyncMembership.setInTarget(false);
          gcGrouperSyncMembership.setInTargetEnd(nowTimestamp);
          gcGrouperSyncMembership.setErrorMessage(null);
          gcGrouperSyncMembership.setErrorTimestamp(null);

          if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningType().isIncrementalSync()) {
            GcGrouperSync gcGrouperSync = this.grouperProvisioner.getGcGrouperSync();
            if (gcGrouperSync.getRecordsCount() != null) {
              gcGrouperSync.setRecordsCount(gcGrouperSync.getRecordsCount() - 1);
            }
          }
          
        }
        
        
      } else {
        if (gcGrouperSyncMembership != null) {
          gcGrouperSyncMembership
              .setErrorMessage(grouperTargetMembership.getException() == null ? null
                  : GrouperUtil
                      .getFullStackTrace(grouperTargetMembership.getException()));
          gcGrouperSyncMembership.setErrorTimestamp(nowTimestamp);
        }
        this.getGrouperProvisioner().retrieveGrouperProvisioningOutput()
            .addRecordsWithDeleteErrors(1);
      }
    }
  }

  /**
   * go through what was selected from full and keep track of whats there and what isnt there
   * @param values
   */
  public void processResultsSelectGroupsFull(
      Collection<ProvisioningGroupWrapper> values) {
    for (ProvisioningGroupWrapper provisioningGroupWrapper : GrouperUtil
        .nonNull(values)) {
      
<<<<<<< GROUPER_5_BRANCH
<<<<<<< GROUPER_5_BRANCH
<<<<<<< GROUPER_5_BRANCH
      if (!provisioningGroupWrapper.getProvisioningStateGroup().isSelectResultProcessed()) {
=======
      if (!provisioningGroupWrapper.getProvisioningStateGroup().isRecalcObject()) {
>>>>>>> 252ebc1 restructure how state is stored in provisioning wrappers
=======
      if (!provisioningGroupWrapper.getProvisioningStateGroup().isSelect()) {
>>>>>>> 3c25747 Provisioning related changes - make incremental sync more robust
=======
      if (!provisioningGroupWrapper.getProvisioningStateGroup().isSelectResultProcessed()) {
>>>>>>> 4cc2654 fix test about tracking target side in grouper
        continue;
      }
      
      ProvisioningGroup targetProvisioningGroup = provisioningGroupWrapper
          .getTargetProvisioningGroup();

      boolean exists = targetProvisioningGroup != null;
      GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper
          .getGcGrouperSyncGroup();

      if (gcGrouperSyncGroup == null) {
        continue;
      }

      if (exists != gcGrouperSyncGroup.isInTarget()) {

        Timestamp nowTimestamp = new Timestamp(System.currentTimeMillis());
        gcGrouperSyncGroup.setInTarget(exists);
        if (exists) {
          gcGrouperSyncGroup.setInTargetStart(nowTimestamp);
          if (StringUtils.isBlank(gcGrouperSyncGroup.getInTargetInsertOrExistsDb())) {
            gcGrouperSyncGroup.setInTargetInsertOrExists(false);
          }

        } else {
          gcGrouperSyncGroup.setInTargetEnd(nowTimestamp);
        }
      }

    }
  }

  /**
   * go through what was selected from full and keep track of whats there and what isnt there
   * @param values
   */
  public void processResultsSelectEntitiesFull(
      Collection<ProvisioningEntityWrapper> values) {
    for (ProvisioningEntityWrapper provisioningEntityWrapper : GrouperUtil
        .nonNull(values)) {
      
<<<<<<< GROUPER_5_BRANCH
<<<<<<< GROUPER_5_BRANCH
<<<<<<< GROUPER_5_BRANCH
      if (!provisioningEntityWrapper.getProvisioningStateEntity().isSelectResultProcessed()) {
=======
      if (!provisioningEntityWrapper.getProvisioningStateEntity().isRecalcObject()) {
>>>>>>> 252ebc1 restructure how state is stored in provisioning wrappers
=======
      if (!provisioningEntityWrapper.getProvisioningStateEntity().isSelect()) {
>>>>>>> 3c25747 Provisioning related changes - make incremental sync more robust
=======
      if (!provisioningEntityWrapper.getProvisioningStateEntity().isSelectResultProcessed()) {
>>>>>>> 4cc2654 fix test about tracking target side in grouper
        continue;
      }
      
      ProvisioningEntity targetProvisioningEntity = provisioningEntityWrapper
          .getTargetProvisioningEntity();

      boolean exists = targetProvisioningEntity != null;
      GcGrouperSyncMember gcGrouperSyncMember = provisioningEntityWrapper
          .getGcGrouperSyncMember();

      if (gcGrouperSyncMember == null) {
        continue;
      }

      if (exists != gcGrouperSyncMember.isInTarget()) {

        Timestamp nowTimestamp = new Timestamp(System.currentTimeMillis());
        gcGrouperSyncMember.setInTarget(exists);
        if (exists) {
          gcGrouperSyncMember.setInTargetStart(nowTimestamp);
          if (StringUtils.isBlank(gcGrouperSyncMember.getInTargetInsertOrExistsDb())) {
            gcGrouperSyncMember.setInTargetInsertOrExists(false);
          }
        } else {
          gcGrouperSyncMember.setInTargetEnd(nowTimestamp);
        }
      }

    }

  }

  /**
   * go through what was selected from full and keep track of whats there and what isnt there
   * @param provisioningMembershipWrappers
   */
  public void processResultsSelectMembershipsFull(
      Collection<ProvisioningGroupWrapper> provisioningGroupWrappers,
      Collection<ProvisioningEntityWrapper> provisioningEntityWrappers,
      Collection<ProvisioningMembershipWrapper> provisioningMembershipWrappers) {

    GrouperProvisioningBehaviorMembershipType grouperProvisioningBehaviorMembershipType = this.grouperProvisioner
        .retrieveGrouperProvisioningBehavior()
        .getGrouperProvisioningBehaviorMembershipType();
    if (grouperProvisioningBehaviorMembershipType == null) {
      grouperProvisioningBehaviorMembershipType = GrouperProvisioningBehaviorMembershipType.membershipObjects;
    }
    switch (grouperProvisioningBehaviorMembershipType) {
      case groupAttributes:

      {
        String groupAttributeNameForMemberships = this.grouperProvisioner
            .retrieveGrouperProvisioningConfiguration()
            .getGroupMembershipAttributeName();

        // get the attribute that holds members
        GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = this.grouperProvisioner
            .retrieveGrouperProvisioningConfiguration()
            .getTargetGroupAttributeNameToConfig().get(groupAttributeNameForMemberships);

        String translateFromMemberAttribute = grouperProvisioningConfigurationAttribute == null
            ? null
            : grouperProvisioningConfigurationAttribute.getTranslateFromGrouperProvisioningEntityField();

        if (StringUtils.isBlank(translateFromMemberAttribute)) {
          this.grouperProvisioner.getDebugMap()
              .put("processResultsSelectMembershipsFullCantUnresolveMemberships", true);

        } else {

          Map<MultiKey, ProvisioningMembershipWrapper> syncGroupIdSyncMemberIdToMembershipWrappersProcessed = new HashMap<MultiKey, ProvisioningMembershipWrapper>(
              this.grouperProvisioner.retrieveGrouperProvisioningDataIndex()
                  .getGrouperSyncGroupIdGrouperSyncMemberIdToProvisioningMembershipWrapper());

          Map<String, GcGrouperSyncMember> provisioningAttributeToMember = new HashMap<String, GcGrouperSyncMember>();

          for (ProvisioningEntityWrapper provisioningEntityWrapper : provisioningEntityWrappers) {
            GcGrouperSyncMember gcGrouperSyncMember = provisioningEntityWrapper
                .getGcGrouperSyncMember();
            if (gcGrouperSyncMember != null) {
              Object provisioningAttribute = this.getGrouperProvisioner()
                  .retrieveGrouperProvisioningTranslator().translateFromGrouperProvisioningEntityField(
                      provisioningEntityWrapper, translateFromMemberAttribute);
              String provisioningAttributeString = GrouperUtil
                  .stringValue(provisioningAttribute);
              provisioningAttributeToMember.put(provisioningAttributeString,
                  gcGrouperSyncMember);
            }
          }

          Timestamp now = new Timestamp(System.currentTimeMillis());

          for (ProvisioningGroupWrapper provisioningGroupWrapper : provisioningGroupWrappers) {

            GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper
                .getGcGrouperSyncGroup();
            if (gcGrouperSyncGroup == null) {
              continue;
            }

            ProvisioningGroup targetProvisioningGroup = provisioningGroupWrapper
                .getTargetProvisioningGroup();
            if (targetProvisioningGroup == null) {
              continue;
            }

            Set<?> membershipAttributes = targetProvisioningGroup
                .retrieveAttributeValueSet(groupAttributeNameForMemberships);

            for (Object object : GrouperUtil.nonNull(membershipAttributes)) {
              String provisioningAttributeString = GrouperUtil.stringValue(object);

              GcGrouperSyncMember gcGrouperSyncMember = provisioningAttributeToMember
                  .get(provisioningAttributeString);
              if (gcGrouperSyncMember == null) {
                continue;
              }

              MultiKey syncGroupIdSyncMemberId = new MultiKey(gcGrouperSyncGroup.getId(),
                  gcGrouperSyncMember.getId());

              ProvisioningMembershipWrapper provisioningMembershipWrapper = syncGroupIdSyncMemberIdToMembershipWrappersProcessed
                  .get(syncGroupIdSyncMemberId);
              if (provisioningMembershipWrapper == null) {
                continue;
              }

              syncGroupIdSyncMemberIdToMembershipWrappersProcessed
                  .remove(syncGroupIdSyncMemberId);

              GcGrouperSyncMembership gcGrouperSyncMembership = provisioningMembershipWrapper
                  .getGcGrouperSyncMembership();

              if (gcGrouperSyncMembership == null) {
                continue;
              }

              // ok, this is in the target :)
              if (!gcGrouperSyncMembership.isInTarget()) {
                gcGrouperSyncMembership.setInTarget(true);
                gcGrouperSyncMembership.setInTargetStart(now);
                // its not an insert
                gcGrouperSyncMembership.setInTargetInsertOrExists(false);
              }
            }

          }

          // remaining ones are not in target
          for (ProvisioningMembershipWrapper provisioningMembershipWrapper : syncGroupIdSyncMemberIdToMembershipWrappersProcessed
              .values()) {
            
<<<<<<< GROUPER_5_BRANCH
<<<<<<< GROUPER_5_BRANCH
<<<<<<< GROUPER_5_BRANCH
            if (!provisioningMembershipWrapper.getProvisioningStateMembership().isSelectResultProcessed()) {
=======
            if (!provisioningMembershipWrapper.getProvisioningStateMembership().isRecalcObject()) {
>>>>>>> 252ebc1 restructure how state is stored in provisioning wrappers
=======
            if (!provisioningMembershipWrapper.getProvisioningStateMembership().isSelect()) {
>>>>>>> 3c25747 Provisioning related changes - make incremental sync more robust
=======
            if (!provisioningMembershipWrapper.getProvisioningStateMembership().isSelectResultProcessed()) {
>>>>>>> 4cc2654 fix test about tracking target side in grouper
              continue;
            }
            
            GcGrouperSyncMembership gcGrouperSyncMembership = provisioningMembershipWrapper
                .getGcGrouperSyncMembership();

            if (gcGrouperSyncMembership == null) {
              continue;
            }

            // ok, this is not in the target :)
            if (gcGrouperSyncMembership.isInTarget()) {
              gcGrouperSyncMembership.setInTarget(false);
              gcGrouperSyncMembership.setInTargetEnd(now);
            }

          }

        }
      }

        break;
      case entityAttributes: {
        String entityAttributeNameForMemberships = this.grouperProvisioner
            .retrieveGrouperProvisioningConfiguration()
            .getEntityMembershipAttributeName();

        // get the attribute that holds members
        GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = this.grouperProvisioner
            .retrieveGrouperProvisioningConfiguration()
            .getTargetGroupAttributeNameToConfig().get(entityAttributeNameForMemberships);

        String translateFromGroupAttribute = grouperProvisioningConfigurationAttribute == null
            ? null
            : grouperProvisioningConfigurationAttribute.getTranslateFromGrouperProvisioningGroupField();

        if (StringUtils.isBlank(translateFromGroupAttribute)) {
          this.grouperProvisioner.getDebugMap()
              .put("processResultsSelectMembershipsFullCantUnresolveMemberships", true);

        } else {

          Map<MultiKey, ProvisioningMembershipWrapper> syncGroupIdSyncMemberIdToMembershipWrappersProcessed = new HashMap<MultiKey, ProvisioningMembershipWrapper>(
              this.grouperProvisioner.retrieveGrouperProvisioningDataIndex()
                  .getGrouperSyncGroupIdGrouperSyncMemberIdToProvisioningMembershipWrapper());

          Map<String, GcGrouperSyncGroup> provisioningAttributeToGroup = new HashMap<String, GcGrouperSyncGroup>();

          for (ProvisioningGroupWrapper provisioningGroupWrapper : provisioningGroupWrappers) {
            GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper
                .getGcGrouperSyncGroup();
            if (gcGrouperSyncGroup != null) {

              Object provisioningAttribute = this.getGrouperProvisioner()
                  .retrieveGrouperProvisioningTranslator()
                  .translateFromGrouperProvisioningGroupField(provisioningGroupWrapper, translateFromGroupAttribute);
              String provisioningAttributeString = GrouperUtil.stringValue(provisioningAttribute);

              provisioningAttributeToGroup.put(provisioningAttributeString,
                  gcGrouperSyncGroup);
            }
          }

          Timestamp now = new Timestamp(System.currentTimeMillis());

          for (ProvisioningEntityWrapper provisioningEntityWrapper : provisioningEntityWrappers) {

            GcGrouperSyncMember gcGrouperSyncMember = provisioningEntityWrapper
                .getGcGrouperSyncMember();
            if (gcGrouperSyncMember == null) {
              continue;
            }

            ProvisioningEntity targetProvisioningEntity = provisioningEntityWrapper
                .getTargetProvisioningEntity();
            if (targetProvisioningEntity == null) {
              continue;
            }

            Set<?> membershipAttributes = targetProvisioningEntity
                .retrieveAttributeValueSet(entityAttributeNameForMemberships);

            for (Object object : GrouperUtil.nonNull(membershipAttributes)) {
              String provisioningAttributeString = GrouperUtil.stringValue(object);

              GcGrouperSyncGroup gcGrouperSyncGroup = provisioningAttributeToGroup
                  .get(provisioningAttributeString);
              if (gcGrouperSyncGroup == null) {
                continue;
              }

              MultiKey syncGroupIdSyncMemberId = new MultiKey(gcGrouperSyncGroup.getId(),
                  gcGrouperSyncMember.getId());

              ProvisioningMembershipWrapper provisioningMembershipWrapper = syncGroupIdSyncMemberIdToMembershipWrappersProcessed
                  .get(syncGroupIdSyncMemberId);
              if (provisioningMembershipWrapper == null) {
                continue;
              }

              syncGroupIdSyncMemberIdToMembershipWrappersProcessed
                  .remove(syncGroupIdSyncMemberId);

              GcGrouperSyncMembership gcGrouperSyncMembership = provisioningMembershipWrapper
                  .getGcGrouperSyncMembership();

              if (gcGrouperSyncMembership == null) {
                continue;
              }

              // ok, this is in the target :)
              if (!gcGrouperSyncMembership.isInTarget()) {
                gcGrouperSyncMembership.setInTarget(true);
                gcGrouperSyncMembership.setInTargetStart(now);
                // its not an insert
                gcGrouperSyncMembership.setInTargetInsertOrExists(false);
              }
            }

          }

          // remaining ones are not in target
          for (ProvisioningMembershipWrapper provisioningMembershipWrapper : syncGroupIdSyncMemberIdToMembershipWrappersProcessed
              .values()) {
            
<<<<<<< GROUPER_5_BRANCH
<<<<<<< GROUPER_5_BRANCH
            if (!provisioningMembershipWrapper.getProvisioningStateMembership().isSelect()) {
=======
            if (!provisioningMembershipWrapper.getProvisioningStateMembership().isRecalcObject()) {
>>>>>>> 252ebc1 restructure how state is stored in provisioning wrappers
=======
            if (!provisioningMembershipWrapper.getProvisioningStateMembership().isSelect()) {
>>>>>>> 3c25747 Provisioning related changes - make incremental sync more robust
              continue;
            }
            
            GcGrouperSyncMembership gcGrouperSyncMembership = provisioningMembershipWrapper
                .getGcGrouperSyncMembership();

            if (gcGrouperSyncMembership == null) {
              continue;
            }

            // ok, this is not in the target :)
            if (gcGrouperSyncMembership.isInTarget()) {
              gcGrouperSyncMembership.setInTarget(false);
              gcGrouperSyncMembership.setInTargetEnd(now);
            }

          }

        }
      }
        break;

      case membershipObjects:

        for (ProvisioningMembershipWrapper provisioningMembershipWrapper : GrouperUtil
            .nonNull(provisioningMembershipWrappers)) {
          
<<<<<<< GROUPER_5_BRANCH
<<<<<<< GROUPER_5_BRANCH
          if (!provisioningMembershipWrapper.getProvisioningStateMembership().isSelect()) {
=======
          if (!provisioningMembershipWrapper.getProvisioningStateMembership().isRecalcObject()) {
>>>>>>> 252ebc1 restructure how state is stored in provisioning wrappers
=======
          if (!provisioningMembershipWrapper.getProvisioningStateMembership().isSelect()) {
>>>>>>> 3c25747 Provisioning related changes - make incremental sync more robust
            continue;
          }
          
          ProvisioningMembership targetProvisioningMembership = provisioningMembershipWrapper
              .getTargetProvisioningMembership();

          boolean exists = targetProvisioningMembership != null;
          GcGrouperSyncMembership gcGrouperSyncMembership = provisioningMembershipWrapper
              .getGcGrouperSyncMembership();

          if (gcGrouperSyncMembership == null) {
            continue;
          }

          if (exists != gcGrouperSyncMembership.isInTarget()) {

            Timestamp nowTimestamp = new Timestamp(System.currentTimeMillis());
            gcGrouperSyncMembership.setInTarget(exists);
            if (exists) {
              gcGrouperSyncMembership.setInTargetStart(nowTimestamp);
              if (StringUtils
                  .isBlank(gcGrouperSyncMembership.getInTargetInsertOrExistsDb())) {
                gcGrouperSyncMembership.setInTargetInsertOrExists(false);
              }

            } else {
              gcGrouperSyncMembership.setInTargetEnd(nowTimestamp);
            }
          }

        }

        break;

      default:
        throw new RuntimeException(
            "Not expecting: " + grouperProvisioningBehaviorMembershipType);
    }

  }

}
