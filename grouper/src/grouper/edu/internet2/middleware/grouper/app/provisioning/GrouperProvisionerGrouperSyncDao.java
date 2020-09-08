package edu.internet2.middleware.grouper.app.provisioning;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.app.tableSync.ProvisioningSyncIntegration;
import edu.internet2.middleware.grouper.app.tableSync.ProvisioningSyncResult;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;

public class GrouperProvisionerGrouperSyncDao {

  private GrouperProvisioner grouperProvisioner = null;

  
  public GrouperProvisioner getGrouperProvisioner() {
    return grouperProvisioner;
  }

  
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner) {
    this.grouperProvisioner = grouperProvisioner;
  }


  public void fixSyncObjects() {
    ProvisioningSyncResult provisioningSyncResult = new ProvisioningSyncResult();
    this.grouperProvisioner.setProvisioningSyncResult(provisioningSyncResult);
    ProvisioningSyncIntegration.fullSyncGroups(provisioningSyncResult, this.getGrouperProvisioner().getGcGrouperSync(),
        this.getGrouperProvisioner().getGrouperProvisioningData().getGroupUuidToProvisioningGroupWrapper(),
        this.getGrouperProvisioner().getGrouperProvisioningData().getGroupUuidToSyncGroup());
    ProvisioningSyncIntegration.fullSyncMembers(provisioningSyncResult, this.getGrouperProvisioner().getGcGrouperSync(),
        this.getGrouperProvisioner().getGrouperProvisioningData().getMemberUuidToProvisioningEntityWrapper(),
        this.getGrouperProvisioner().getGrouperProvisioningData().getMemberUuidToSyncMember());
    ProvisioningSyncIntegration.fullSyncMemberships(provisioningSyncResult, this.getGrouperProvisioner().getGcGrouperSync(),
        this.getGrouperProvisioner().getGrouperProvisioningData().getGroupUuidMemberUuidToProvisioningMembershipWrapper(),
        this.getGrouperProvisioner().getGrouperProvisioningData().getGroupUuidMemberUuidToSyncMembership());
    int objectStoreCount = this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncDao().storeAllObjects();
    provisioningSyncResult.setSyncObjectStoreCount(objectStoreCount);
    
    this.grouperProvisioner.getDebugMap().put("fixSyncObjectStoreCount", objectStoreCount);
    
  }


  /**
   * get sync objects from the database
   */
  public Map<String, GcGrouperSyncGroup> retrieveAllSyncGroups() {
    GcGrouperSync gcGrouperSync = this.getGrouperProvisioner().getGcGrouperSync();
    
    List<GcGrouperSyncGroup> gcGrouperSyncGroups = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveAll();
    
    Map<String, GcGrouperSyncGroup> groupUuidToSyncGroup = new HashMap<String, GcGrouperSyncGroup>();
  
    // save these in the data object
    for (GcGrouperSyncGroup gcGrouperSyncGroup : gcGrouperSyncGroups) {
  
      groupUuidToSyncGroup.put(gcGrouperSyncGroup.getGroupId(), gcGrouperSyncGroup);
    }
    return groupUuidToSyncGroup;
  }


  /**
   * get sync objects from the database
   */
  public Map<String, GcGrouperSyncMember> retrieveAllSyncMembers() {
    GcGrouperSync gcGrouperSync = this.getGrouperProvisioner().getGcGrouperSync();
    
    List<GcGrouperSyncMember> gcGrouperSyncMembers = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveAll();
    
    Map<String, GcGrouperSyncMember> memberUuidToSyncMember = new HashMap<String, GcGrouperSyncMember>();
  
    // save these in the data object
    for (GcGrouperSyncMember gcGrouperSyncMember : gcGrouperSyncMembers) {
  
      memberUuidToSyncMember.put(gcGrouperSyncMember.getMemberId(), gcGrouperSyncMember);
    }
    return memberUuidToSyncMember;
  }


  /**
   * get sync objects from the database.  all records correspond to a sync group and sync member or its skipped
   */
  public Map<MultiKey, GcGrouperSyncMembership> retrieveAllSyncMemberships() {
    GcGrouperSync gcGrouperSync = this.getGrouperProvisioner().getGcGrouperSync();
    
    List<GcGrouperSyncMembership> gcGrouperSyncMemberships = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveAll();
    
    Map<MultiKey, GcGrouperSyncMembership> groupUuidMemberUuidToSyncMembership = new HashMap<MultiKey, GcGrouperSyncMembership>();
  
    
    int syncMembershipReferenceMissing = 0;
    
  
    for (GcGrouperSyncMembership gcGrouperSyncMembership : GrouperUtil.nonNull(gcGrouperSyncMemberships)) {
      
      // data is not consistent just ignore for now
      GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveById(gcGrouperSyncMembership.getGrouperSyncGroupId());
      if (gcGrouperSyncGroup == null) {
        syncMembershipReferenceMissing++;
        continue;
      }
      
      GcGrouperSyncMember gcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveById(gcGrouperSyncMembership.getGrouperSyncMemberId());
      
      if (gcGrouperSyncMember == null) {
        syncMembershipReferenceMissing++;
        continue;
      }
  
      groupUuidMemberUuidToSyncMembership.put(new MultiKey(gcGrouperSyncGroup.getGroupId(),
          gcGrouperSyncMember.getMemberId()), gcGrouperSyncMembership);
    }
    if (syncMembershipReferenceMissing > 0) {
      this.getGrouperProvisioner().getDebugMap().put("syncMembershipReferenceMissing", syncMembershipReferenceMissing);
    }
  
    return groupUuidMemberUuidToSyncMembership;
  }


  /**
   * get sync objects from the database
   */
  public Map<String, GcGrouperSyncGroup> retrieveIncrementalSyncGroups() {
    
    Map<String, GcGrouperSyncGroup> groupUuidToSyncGroup = new HashMap<String, GcGrouperSyncGroup>();
  
    Set<String> groupIdsToRetrieve = this.grouperProvisioner.retrieveGrouperDao().incrementalGroupUuids();
  
    if (groupIdsToRetrieve.size() > 0) {
      GcGrouperSync gcGrouperSync = this.getGrouperProvisioner().getGcGrouperSync();
      Map<String, GcGrouperSyncGroup> grouperSyncGroupIdToSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupIds(groupIdsToRetrieve);
      for (GcGrouperSyncGroup gcGrouperSyncGroup : GrouperUtil.nonNull(grouperSyncGroupIdToSyncGroup).values()) {
        groupUuidToSyncGroup.put(gcGrouperSyncGroup.getGroupId(), gcGrouperSyncGroup);
        
      }
    }
    return groupUuidToSyncGroup;
  
  }


  /**
   * get sync objects from the database
   */
  public Map<String, GcGrouperSyncMember> retrieveIncrementalSyncMembers() {
    
    Map<String, GcGrouperSyncMember> memberUuidToSyncMember = new HashMap<String, GcGrouperSyncMember>();
  
    Set<String> memberIdsToRetrieve = this.grouperProvisioner.retrieveGrouperDao().incrementalMemberUuids();
  
    if (memberIdsToRetrieve.size() > 0) {
      GcGrouperSync gcGrouperSync = this.getGrouperProvisioner().getGcGrouperSync();
      Map<String, GcGrouperSyncMember> grouperSyncMemberIdToSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberIds(memberIdsToRetrieve);
      for (GcGrouperSyncMember gcGrouperSyncMember : GrouperUtil.nonNull(grouperSyncMemberIdToSyncMember).values()) {
        memberUuidToSyncMember.put(gcGrouperSyncMember.getMemberId(), gcGrouperSyncMember);
        
      }
    }
    return memberUuidToSyncMember;
  
  }


  /**
   * get sync objects from the database.  all records correspond to a sync group and sync member or its skipped
   */
  public Map<MultiKey, GcGrouperSyncMembership> retrieveIncrementalSyncMemberships() {
    GcGrouperSync gcGrouperSync = this.getGrouperProvisioner().getGcGrouperSync();
    
    Set<MultiKey> groupIdMemberIdsToRetrieve = this.grouperProvisioner.retrieveGrouperDao().incrementalGroupUuidsMemberUuids();
    Map<MultiKey, GcGrouperSyncMembership> groupUuidMemberUuidToSyncMembership = new HashMap<MultiKey, GcGrouperSyncMembership>();
  
    if (groupIdMemberIdsToRetrieve.size() > 0) {
      
      int syncMembershipReferenceMissing = 0;
      
      Map<MultiKey, GcGrouperSyncMembership> gcSyncMemberIdGcSyncGroupIdToSyncMemberships = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveByGroupIdsAndMemberIds(groupIdMemberIdsToRetrieve);
  
      for (GcGrouperSyncMembership gcGrouperSyncMembership : GrouperUtil.nonNull(gcSyncMemberIdGcSyncGroupIdToSyncMemberships).values()) {
        
        // data is not consistent just ignore for now
        GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveById(gcGrouperSyncMembership.getGrouperSyncGroupId());
        if (gcGrouperSyncGroup == null) {
          syncMembershipReferenceMissing++;
          continue;
        }
        
        GcGrouperSyncMember gcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveById(gcGrouperSyncMembership.getGrouperSyncMemberId());
        
        if (gcGrouperSyncMember == null) {
          syncMembershipReferenceMissing++;
          continue;
        }
        groupUuidMemberUuidToSyncMembership.put(new MultiKey(gcGrouperSyncGroup.getGroupId(),
            gcGrouperSyncMember.getMemberId()), gcGrouperSyncMembership);
      }
      if (syncMembershipReferenceMissing > 0) {
        this.getGrouperProvisioner().getDebugMap().put("syncMembershipReferenceMissing", syncMembershipReferenceMissing);
      }
    }
    return groupUuidMemberUuidToSyncMembership;
        
  }


  public void retrieveSyncData(GrouperProvisioningType grouperProvisioningType) {
    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();
  
    {
      long start = System.currentTimeMillis();
      Map<String, GcGrouperSyncGroup> retrieveAllSyncGroups = grouperProvisioningType.retrieveSyncGroups(this.grouperProvisioner);
      this.getGrouperProvisioner().getGrouperProvisioningData().setGroupUuidToSyncGroup(retrieveAllSyncGroups);
  
      debugMap.put("retrieveSyncGroupsMillis", System.currentTimeMillis() - start);
      debugMap.put("syncGroupCount", GrouperUtil.length(this.getGrouperProvisioner().getGrouperProvisioningData()
          .getGroupUuidToSyncGroup()));
    }
    {
      long start = System.currentTimeMillis();
      Map<String, GcGrouperSyncMember> retrieveAllSyncMembers = grouperProvisioningType.retrieveSyncMembers(this.grouperProvisioner);
      this.getGrouperProvisioner().getGrouperProvisioningData().setMemberUuidToSyncMember(retrieveAllSyncMembers);
      debugMap.put("retrieveSyncEntitiesMillis", System.currentTimeMillis() - start);
      debugMap.put("syncEntityCount", GrouperUtil.length(this.getGrouperProvisioner().getGrouperProvisioningData().getMemberUuidToSyncMember()));
    }
    {
      long start = System.currentTimeMillis();
      Map<MultiKey, GcGrouperSyncMembership> retrieveAllSyncMemberships = grouperProvisioningType.retrieveSyncMemberships(this.grouperProvisioner);
      this.getGrouperProvisioner().getGrouperProvisioningData().setGroupUuidMemberUuidToSyncMembership(
          retrieveAllSyncMemberships);
      debugMap.put("retrieveSyncMshipsMillis", System.currentTimeMillis() - start);
      debugMap.put("syncMshipCount", GrouperUtil.length(this.getGrouperProvisioner().getGrouperProvisioningData().getGroupUuidMemberUuidToSyncMembership()));
    }
    
  }

  
}
