package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.tableSync.ProvisioningSyncIntegration;
import edu.internet2.middleware.grouper.app.tableSync.ProvisioningSyncResult;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;
import edu.internet2.middleware.subject.Subject;

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
        this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGroupUuidToProvisioningGroupWrapper(),
        this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGroupUuidToSyncGroup());
    ProvisioningSyncIntegration.fullSyncMembers(provisioningSyncResult, this.getGrouperProvisioner().getGcGrouperSync(),
        this.getGrouperProvisioner().retrieveGrouperProvisioningData().getMemberUuidToProvisioningEntityWrapper(),
        this.getGrouperProvisioner().retrieveGrouperProvisioningData().getMemberUuidToSyncMember());
    ProvisioningSyncIntegration.fullSyncMemberships(provisioningSyncResult, this.getGrouperProvisioner().getGcGrouperSync(),
        this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGroupUuidMemberUuidToProvisioningMembershipWrapper(),
        this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGroupUuidMemberUuidToSyncMembership());

//    //do we really need to do this now?  maybe just do this at end?
//    int objectStoreCount = this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncDao().storeAllObjects();
//    provisioningSyncResult.setSyncObjectStoreCount(objectStoreCount);
//    
//    this.grouperProvisioner.getDebugMap().put("fixSyncObjectStoreCount", objectStoreCount);
    
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
  
    Set<String> groupIdsToRetrieve = this.grouperProvisioner.retrieveGrouperProvisioningData().getGrouperIncrementalUuidsToRetrieveFromGrouper().getGroupUuidsForGroupOnly();
  
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
  
    Set<String> memberIdsToRetrieve = this.grouperProvisioner.retrieveGrouperProvisioningData().getGrouperIncrementalUuidsToRetrieveFromGrouper().getMemberUuidsForEntityOnly();
  
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
    
    List<GcGrouperSyncMembership> gcGrouperSyncMemberships = new ArrayList<GcGrouperSyncMembership>();

    {
      Set<MultiKey> groupIdMemberIdsToRetrieve = this.grouperProvisioner.retrieveGrouperProvisioningData().getGrouperIncrementalUuidsToRetrieveFromGrouper().getGroupUuidsMemberUuidsFieldIdsForMembershipSync();
      if (GrouperUtil.length(groupIdMemberIdsToRetrieve) > 0) {
        Map<MultiKey, GcGrouperSyncMembership> membershipRetrieveByGroupIdsAndMemberIds = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveByGroupIdsAndMemberIds(groupIdMemberIdsToRetrieve);
        gcGrouperSyncMemberships.addAll(GrouperUtil.nonNull(membershipRetrieveByGroupIdsAndMemberIds).values());
      }
    }    

    {
      Set<String> groupIdsToRetrieveMemberships = this.grouperProvisioner.retrieveGrouperProvisioningData().getGrouperIncrementalUuidsToRetrieveFromGrouper().getGroupUuidsForGroupMembershipSync();
      if (GrouperUtil.length(groupIdsToRetrieveMemberships) > 0) {
        List<GcGrouperSyncMembership> membershipRetrieveByGroupIds = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveByGroupIds(groupIdsToRetrieveMemberships);
        gcGrouperSyncMemberships.addAll(GrouperUtil.nonNull(membershipRetrieveByGroupIds));
      }
    }

    {
      Set<String> memberIdsToRetrieveMemberships = this.grouperProvisioner.retrieveGrouperProvisioningData().getGrouperIncrementalUuidsToRetrieveFromGrouper().getMemberUuidsForEntityMembershipSync();
      if (GrouperUtil.length(memberIdsToRetrieveMemberships) > 0) {
        List<GcGrouperSyncMembership> membershipRetrieveByMemberIds = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveByMemberIds(memberIdsToRetrieveMemberships);
        gcGrouperSyncMemberships.addAll(GrouperUtil.nonNull(membershipRetrieveByMemberIds));
      }
    }
    
    Map<MultiKey, GcGrouperSyncMembership> groupUuidMemberUuidToSyncMembership = new HashMap<MultiKey, GcGrouperSyncMembership>();
  
    if (GrouperUtil.length(gcGrouperSyncMemberships) > 0) {
      
      int syncMembershipReferenceMissing = 0;
      
      for (GcGrouperSyncMembership gcGrouperSyncMembership : gcGrouperSyncMemberships) {
        
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

  public void retrieveSyncDataFull() {
    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();
  
    {
      long start = System.currentTimeMillis();
      Map<String, GcGrouperSyncGroup> retrieveAllSyncGroups = grouperProvisioner.retrieveGrouperSyncDao().retrieveAllSyncGroups();
      this.getGrouperProvisioner().retrieveGrouperProvisioningData().setGroupUuidToSyncGroup(retrieveAllSyncGroups);
  
      debugMap.put("retrieveSyncGroupsMillis", System.currentTimeMillis() - start);
      debugMap.put("syncGroupCount", GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningData()
          .getGroupUuidToSyncGroup()));
    }
    {
      long start = System.currentTimeMillis();
      Map<String, GcGrouperSyncMember> retrieveAllSyncMembers = grouperProvisioner.retrieveGrouperSyncDao().retrieveAllSyncMembers();
      this.getGrouperProvisioner().retrieveGrouperProvisioningData().setMemberUuidToSyncMember(retrieveAllSyncMembers);
      debugMap.put("retrieveSyncEntitiesMillis", System.currentTimeMillis() - start);
      debugMap.put("syncEntityCount", GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getMemberUuidToSyncMember()));
    }
    {
      long start = System.currentTimeMillis();
      Map<MultiKey, GcGrouperSyncMembership> retrieveAllSyncMemberships = grouperProvisioner.retrieveGrouperSyncDao().retrieveAllSyncMemberships();
      this.getGrouperProvisioner().retrieveGrouperProvisioningData().setGroupUuidMemberUuidToSyncMembership(
          retrieveAllSyncMemberships);
      debugMap.put("retrieveSyncMshipsMillis", System.currentTimeMillis() - start);
      debugMap.put("syncMshipCount", GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGroupUuidMemberUuidToSyncMembership()));
    }
    
  }
  

  public void retrieveSyncDataIncremental() {
    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();
  
    {
      long start = System.currentTimeMillis();
      Map<String, GcGrouperSyncGroup> retrieveAllSyncGroups = grouperProvisioner.retrieveGrouperSyncDao().retrieveIncrementalSyncGroups();
      this.getGrouperProvisioner().retrieveGrouperProvisioningData().setGroupUuidToSyncGroup(retrieveAllSyncGroups);
  
      debugMap.put("retrieveSyncGroupsMillis", System.currentTimeMillis() - start);
      debugMap.put("syncGroupCount", GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningData()
          .getGroupUuidToSyncGroup()));
    }
    {
      long start = System.currentTimeMillis();
      Map<String, GcGrouperSyncMember> retrieveAllSyncMembers = grouperProvisioner.retrieveGrouperSyncDao().retrieveIncrementalSyncMembers();
      this.getGrouperProvisioner().retrieveGrouperProvisioningData().setMemberUuidToSyncMember(retrieveAllSyncMembers);
      debugMap.put("retrieveSyncEntitiesMillis", System.currentTimeMillis() - start);
      debugMap.put("syncEntityCount", GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getMemberUuidToSyncMember()));
    }
    {
      long start = System.currentTimeMillis();
      Map<MultiKey, GcGrouperSyncMembership> retrieveAllSyncMemberships = grouperProvisioner.retrieveGrouperSyncDao().retrieveIncrementalSyncMemberships();
      this.getGrouperProvisioner().retrieveGrouperProvisioningData().setGroupUuidMemberUuidToSyncMembership(
          retrieveAllSyncMemberships);
      debugMap.put("retrieveSyncMshipsMillis", System.currentTimeMillis() - start);
      debugMap.put("syncMshipCount", GrouperUtil.length(this.getGrouperProvisioner().retrieveGrouperProvisioningData().getGroupUuidMemberUuidToSyncMembership()));
    }
    
  }

  /**
   * update subject link for these members
   * @param gcGrouperSyncMembersToRefreshSubjectLink
   */
  public void updateSubjectLink(List<GcGrouperSyncMember> gcGrouperSyncMembersToRefreshSubjectLink) {
    if (GrouperUtil.length(gcGrouperSyncMembersToRefreshSubjectLink) == 0) {
      return;
    }
    
    // If using subject attributes and those are not in the member sync object, then resolve the subject, and put in the member sync object
    String subjectLinkMemberFromId2 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getSubjectLinkMemberFromId2();
    boolean hasSubjectLinkMemberFromId2 = !StringUtils.isBlank(subjectLinkMemberFromId2);
    
    String subjectLinkMemberFromId3 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getSubjectLinkMemberFromId3();
    boolean hasSubjectLinkMemberFromId3 = !StringUtils.isBlank(subjectLinkMemberFromId3);

    String subjectLinkMemberToId2 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getSubjectLinkMemberToId2();
    boolean hasSubjectLinkMemberToId2 = !StringUtils.isBlank(subjectLinkMemberToId2);

    String subjectLinkMemberToId3 = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getSubjectLinkMemberToId3();
    boolean hasSubjectLinkMemberToId3 = !StringUtils.isBlank(subjectLinkMemberToId3);

    if (!hasSubjectLinkMemberFromId2 && !hasSubjectLinkMemberFromId3 && !hasSubjectLinkMemberToId2 && !hasSubjectLinkMemberToId3) {
      return;
    }

    int subjectsCannotFindLinkData = 0;

    Set<MultiKey> sourceIdSubjectIds = new HashSet<MultiKey>();
    
    for (GcGrouperSyncMember gcGrouperSyncMember : gcGrouperSyncMembersToRefreshSubjectLink) {
      
      MultiKey sourceIdSubjectId = new MultiKey(gcGrouperSyncMember.getSourceId(), gcGrouperSyncMember.getSubjectId());
      sourceIdSubjectIds.add(sourceIdSubjectId);
      
    }
    
    Map<MultiKey, Subject> sourceIdSubjectIdToSubject = SubjectFinder.findBySourceIdsAndSubjectIds(sourceIdSubjectIds, false);

    for (GcGrouperSyncMember gcGrouperSyncMember : gcGrouperSyncMembersToRefreshSubjectLink) {
      
      MultiKey sourceIdSubjectId = new MultiKey(gcGrouperSyncMember.getSourceId(), gcGrouperSyncMember.getSubjectId());
      Subject subject = sourceIdSubjectIdToSubject.get(sourceIdSubjectId);

      if (subject == null) {
        subjectsCannotFindLinkData++;
        // maybe it didn't get resolved, don't mess with the existing cached data.
        continue;
      }
      
      
      Map<String, Object> variableMap = new HashMap<String, Object>();
      variableMap.put("subject", subject);
      
      if (hasSubjectLinkMemberFromId2) {
        String memberFromId2Value = GrouperUtil.substituteExpressionLanguage(subjectLinkMemberFromId2, variableMap);
        gcGrouperSyncMember.setMemberFromId2(memberFromId2Value);
      }
      
      if (hasSubjectLinkMemberFromId3) {
        String memberFromId3Value = GrouperUtil.substituteExpressionLanguage(subjectLinkMemberFromId3, variableMap);
        gcGrouperSyncMember.setMemberFromId3(memberFromId3Value);
      }
      
      if (hasSubjectLinkMemberToId2) {
        String memberToId2Value = GrouperUtil.substituteExpressionLanguage(subjectLinkMemberToId2, variableMap);
        gcGrouperSyncMember.setMemberToId2(memberToId2Value);
      }
      
      if (hasSubjectLinkMemberFromId3) {
        String memberToId3Value = GrouperUtil.substituteExpressionLanguage(subjectLinkMemberToId3, variableMap);
        gcGrouperSyncMember.setMemberToId3(memberToId3Value);
      }
      
    }

    if (subjectsCannotFindLinkData > 0) {
      this.grouperProvisioner.getDebugMap().put("subjectsCannotFindLinkData", subjectsCannotFindLinkData);
    }
  }

  
}
