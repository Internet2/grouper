package edu.internet2.middleware.grouper.misc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.logging.Log;
import org.hibernate.type.StringType;

import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEvent;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEventContainer;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEventType;
import edu.internet2.middleware.grouper.esb.listener.EsbListenerBase;
import edu.internet2.middleware.grouper.esb.listener.ProvisioningSyncConsumerResult;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.util.GrouperCallable;
import edu.internet2.middleware.grouper.util.GrouperFuture;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class CompositeMembershipsChangeLogConsumer extends EsbListenerBase {
  
  private static final Log LOG = GrouperUtil.getLog(CompositeMembershipsChangeLogConsumer.class);

  private Map<MultiKey, Set<Membership>> groupIdMemberIdToMembershipSet;
  
  private Set<String> groupIdsWithMembershipChanges;
  
  private boolean useThreads;
  
  private int threadPoolSize;
  
  @SuppressWarnings("rawtypes")
  private List<GrouperFuture> futures;
  @SuppressWarnings("rawtypes")
  private List<GrouperCallable> callablesWithProblems;
  
  @Override
  public boolean dispatchEvent(String eventJsonString, String consumerName) {
    throw new RuntimeException("Not implemented");
  }

  @Override
  public void disconnect() {
    // nothing
  }
    
  @SuppressWarnings("rawtypes")
  @Override
  public ProvisioningSyncConsumerResult dispatchEventList(
      List<EsbEventContainer> esbEventContainers) {
    ProvisioningSyncConsumerResult provisioningSyncConsumerResult = new ProvisioningSyncConsumerResult();
    
    useThreads = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("changeLog.consumer.compositeMemberships.useThreads", true);
    threadPoolSize = GrouperLoaderConfig.retrieveConfig().propertyValueInt("changeLog.consumer.compositeMemberships.threadPoolSize", 20);

    // cache data first
    Map<String, Set<Composite>> findAsFactorResults = new HashMap<String, Set<Composite>>();
    Set<String> groupIdsToLookup = new HashSet<String>();
    
    Map<String, Group> groupIdToGroupMap = new HashMap<String, Group>();
    groupIdMemberIdToMembershipSet = new HashMap<MultiKey, Set<Membership>>();
    groupIdsWithMembershipChanges = new HashSet<String>();
    futures = new ArrayList<GrouperFuture>();
    callablesWithProblems = new ArrayList<GrouperCallable>();
    
    List<EsbEventContainer> esbEventContainersOfInterest = new ArrayList<EsbEventContainer>();
    
    // figure out the changes of interest and the groups to query in bulk
    for (EsbEventContainer esbEventContainer : esbEventContainers) {
      EsbEventType esbEventType = esbEventContainer.getEsbEventType();
      EsbEvent esbEvent = esbEventContainer.getEsbEvent();

      if (esbEventType == EsbEventType.MEMBERSHIP_ADD || esbEventType == EsbEventType.MEMBERSHIP_DELETE) {
        
        if (esbEvent.getSourceId().equals("g:gsa")) {
          continue;
        }
        
        esbEventContainersOfInterest.add(esbEventContainer);

        if (findAsFactorResults.get(esbEvent.getGroupId()) == null) {
          Set<Composite> composites = GrouperDAOFactory.getFactory().getComposite().findAsFactor(esbEvent.getGroupId());
          findAsFactorResults.put(esbEvent.getGroupId(), composites);
          for (Composite composite : composites) {
            groupIdsToLookup.add(composite.getFactorOwnerUuid());
          }
        }        
      } else if (esbEventType == EsbEventType.GROUP_COMPOSITE_ADD) {
        esbEventContainersOfInterest.add(esbEventContainer);
        
        groupIdsToLookup.add(esbEvent.getCompositeOwnerId());
      } else if (esbEventType == EsbEventType.GROUP_COMPOSITE_DELETE) {
        esbEventContainersOfInterest.add(esbEventContainer);
        
        groupIdsToLookup.add(esbEvent.getCompositeOwnerId());
      } else if (esbEventType == EsbEventType.GROUP_ENABLE) {
        esbEventContainersOfInterest.add(esbEventContainer);
        
        groupIdsToLookup.add(esbEvent.getGroupId());
      }
    }
    
    {
      Set<Group> groups = GrouperDAOFactory.getFactory().getGroup().findByUuids(groupIdsToLookup, false);
      for (Group group : groups) {
        if (group != null) {
          groupIdToGroupMap.put(group.getId(), group);
        }
      }
    }
    
    // take another pass through the changes so we can figure out which memberships to query
    Map<String, Set<String>> groupIdToMemberIdSetMembershipsToQuery = new HashMap<String, Set<String>>();
    for (EsbEventContainer esbEventContainer : esbEventContainersOfInterest) {
      EsbEventType esbEventType = esbEventContainer.getEsbEventType();
      EsbEvent esbEvent = esbEventContainer.getEsbEvent();

      if (esbEventType == EsbEventType.MEMBERSHIP_ADD || esbEventType == EsbEventType.MEMBERSHIP_DELETE) {
        
        String memberId = esbEvent.getMemberId();
        
        Set<Composite> composites = findAsFactorResults.get(esbEvent.getGroupId());
        for (Composite composite : composites) {
          Group owner = groupIdToGroupMap.get(composite.getFactorOwnerUuid());
          
          if (owner == null || !owner.isEnabled()) {
            continue;
          }
                    
          if (groupIdToMemberIdSetMembershipsToQuery.get(owner.getUuid()) == null) {
            groupIdToMemberIdSetMembershipsToQuery.put(owner.getUuid(), new HashSet<String>());
          }
          
          if (groupIdToMemberIdSetMembershipsToQuery.get(composite.getRightFactorUuid()) == null) {
            groupIdToMemberIdSetMembershipsToQuery.put(composite.getRightFactorUuid(), new HashSet<String>());
          }
          
          if (groupIdToMemberIdSetMembershipsToQuery.get(composite.getLeftFactorUuid()) == null) {
            groupIdToMemberIdSetMembershipsToQuery.put(composite.getLeftFactorUuid(), new HashSet<String>());
          }
          
          groupIdToMemberIdSetMembershipsToQuery.get(owner.getUuid()).add(memberId);
          groupIdToMemberIdSetMembershipsToQuery.get(composite.getRightFactorUuid()).add(memberId);
          groupIdToMemberIdSetMembershipsToQuery.get(composite.getLeftFactorUuid()).add(memberId);
        }
      }
    }
    
    {
      // query the memberships
      for (String groupId : groupIdToMemberIdSetMembershipsToQuery.keySet()) {
        Set<Membership> memberships = GrouperDAOFactory.getFactory().getMembership().findAllByGroupOwnerAndFieldAndMemberIdsAndType(groupId, Group.getDefaultList(), groupIdToMemberIdSetMembershipsToQuery.get(groupId), null, true);
        for (Membership membership : memberships) {
          MultiKey multiKey = new MultiKey(membership.getOwnerGroupId(), membership.getMemberUuid());
          if (!groupIdMemberIdToMembershipSet.containsKey(multiKey)) {
            groupIdMemberIdToMembershipSet.put(multiKey, new HashSet<Membership>());
          }
          groupIdMemberIdToMembershipSet.get(multiKey).add(membership);
        }
      }
    }
    
    // finally go through the changes to adjust composites as needed
    for (EsbEventContainer esbEventContainer : esbEventContainersOfInterest) {
      EsbEventType esbEventType = esbEventContainer.getEsbEventType();
      EsbEvent esbEvent = esbEventContainer.getEsbEvent();

      if (esbEventType == EsbEventType.MEMBERSHIP_ADD || esbEventType == EsbEventType.MEMBERSHIP_DELETE) {
        
        String memberId = esbEvent.getMemberId();
        
        Set<Composite> composites = findAsFactorResults.get(esbEvent.getGroupId());
        for (Composite composite : composites) {
          Group owner = groupIdToGroupMap.get(composite.getFactorOwnerUuid());
          
          if (owner == null || !owner.isEnabled()) {
            continue;
          }
                    
          boolean ownerHasMember = hasMember(owner.getUuid(), memberId);
          boolean compositeShouldHaveMember = false;

          // check to see if the composite *should* have the member
          if (composite.getType().equals(CompositeType.UNION) && 
              (hasMember(composite.getRightFactorUuid(), memberId) || hasMember(composite.getLeftFactorUuid(), memberId))) {
            compositeShouldHaveMember = true;
          } else if (composite.getType().equals(CompositeType.INTERSECTION) && 
              (hasMember(composite.getRightFactorUuid(), memberId) && hasMember(composite.getLeftFactorUuid(), memberId))) {
            compositeShouldHaveMember = true;
          } else if (composite.getType().equals(CompositeType.COMPLEMENT) && 
              (!hasMember(composite.getRightFactorUuid(), memberId) && hasMember(composite.getLeftFactorUuid(), memberId))) {
            compositeShouldHaveMember = true;
          }
          
          // fix the composite membership if necessary
          if (compositeShouldHaveMember && !ownerHasMember) {
            Membership membership = Composite.internal_createNewCompositeMembershipObject(owner.getUuid(), memberId, composite.getUuid());
            LOG.info("Adding composite membership for groupName=" + owner.getName() + ", groupId=" + owner.getUuid() + ", memberId=" + memberId + ", compositeId=" + composite.getUuid());
            saveMembership(membership);
          } else if (!compositeShouldHaveMember && ownerHasMember) {
            MultiKey multiKey = new MultiKey(owner.getUuid(), memberId);
            Set<Membership> memberships = groupIdMemberIdToMembershipSet.get(multiKey);
            for (Membership membership : memberships) {
              if (membership.getTypeEnum() == MembershipType.COMPOSITE) {
                // just make sure we're deleting the composite membership in case there's bad data here
                LOG.info("Deleting composite membership for groupName=" + owner.getName() + ", groupId=" + owner.getUuid() + ", memberId=" + memberId + ", compositeId=" + composite.getUuid());
                deleteMembership(membership);
                break;
              }
            }
          }
        }
      } else if (esbEventType == EsbEventType.GROUP_COMPOSITE_ADD) {
        resyncComposite(groupIdToGroupMap.get(esbEvent.getCompositeOwnerId()));
      } else if (esbEventType == EsbEventType.GROUP_COMPOSITE_DELETE) {
        resyncComposite(groupIdToGroupMap.get(esbEvent.getCompositeOwnerId()));
      } else if (esbEventType == EsbEventType.GROUP_ENABLE) {
        resyncComposite(groupIdToGroupMap.get(esbEvent.getGroupId()));
      }
      
      Membership.updateLastMembershipChangeDuringMembersListUpdate(groupIdsWithMembershipChanges);
    }
    
    GrouperFuture.waitForJob(futures, 0, callablesWithProblems);
    GrouperCallable.tryCallablesWithProblems(callablesWithProblems);

    provisioningSyncConsumerResult.setLastProcessedSequenceNumber(esbEventContainers.get(esbEventContainers.size()-1).getSequenceNumber());
    return provisioningSyncConsumerResult;
  }
  
  private boolean hasMember(String groupId, String memberId) {
    MultiKey multiKey = new MultiKey(groupId, memberId);
    Set<Membership> memberships = groupIdMemberIdToMembershipSet.get(multiKey);
    
    if (memberships != null && memberships.size() > 0) {
      return true;
    }
    
    return false;
  }
  
  @SuppressWarnings("unchecked")
  private void saveMembership(Membership membership) {
    
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = this.getEsbConsumer().getChangeLogProcessorMetadata().getHib3GrouperLoaderLog();
    GrouperCallable<Void> grouperCallable = new GrouperCallable<Void>("compositeMembershipSaveOrDelete") {
      
      @Override
      public Void callLogic() {
        GrouperDAOFactory.getFactory().getMembership().save(membership);
        hib3GrouperLoaderLog.addInsertCount(1);
        return null;
      }
    };
    
    if (!useThreads){
      grouperCallable.callLogic();
    } else {
      GrouperFuture<Void> future = GrouperUtil.executorServiceSubmit(GrouperUtil.retrieveExecutorService(), grouperCallable, true);
      futures.add(future);          
      GrouperFuture.waitForJob(futures, threadPoolSize, callablesWithProblems);
    }
    
    MultiKey multiKey = new MultiKey(membership.getOwnerGroupId(), membership.getMemberUuid());
    if (!groupIdMemberIdToMembershipSet.containsKey(multiKey)) {
      groupIdMemberIdToMembershipSet.put(multiKey, new HashSet<Membership>());
    }
    
    groupIdMemberIdToMembershipSet.get(multiKey).add(membership);
    
    groupIdsWithMembershipChanges.add(membership.getOwnerGroupId());
  }
  
  @SuppressWarnings("unchecked")
  private void deleteMembership(Membership membership) {
    
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = this.getEsbConsumer().getChangeLogProcessorMetadata().getHib3GrouperLoaderLog();
    GrouperCallable<Void> grouperCallable = new GrouperCallable<Void>("compositeMembershipSaveOrDelete") {
      
      @Override
      public Void callLogic() {
        membership.delete();
        hib3GrouperLoaderLog.addDeleteCount(1);
        return null;
      }
    };
    
    if (!useThreads){
      grouperCallable.callLogic();
    } else {
      GrouperFuture<Void> future = GrouperUtil.executorServiceSubmit(GrouperUtil.retrieveExecutorService(), grouperCallable, true);
      futures.add(future);          
      GrouperFuture.waitForJob(futures, threadPoolSize, callablesWithProblems);
    }
    
    MultiKey multiKey = new MultiKey(membership.getOwnerGroupId(), membership.getMemberUuid());
    if (groupIdMemberIdToMembershipSet.containsKey(multiKey)) {
      groupIdMemberIdToMembershipSet.get(multiKey).remove(membership);
    }
    
    groupIdsWithMembershipChanges.add(membership.getOwnerGroupId());
  }
  
  private void resyncComposite(Group ownerGroup) {
    if (ownerGroup == null) {
      // nothing to do here
      return;
    }
    
    if (!ownerGroup.isEnabled()) {      
      return;
    }
    
    // before we sync, make sure there aren't any pending changes
    GrouperFuture.waitForJob(futures, 0, callablesWithProblems);
    GrouperCallable.tryCallablesWithProblems(callablesWithProblems);
    callablesWithProblems.clear();
    
    Composite composite = ownerGroup.getComposite(false);
    if (composite == null) {
      // delete all composite memberships for this group
      Set<Membership> memberships = GrouperDAOFactory.getFactory().getMembership().findAllMembershipEntriesByGroupOwnerAndFieldAndType(ownerGroup.getId(), Group.getDefaultList(), "composite", false);
      
      for (Membership membership : memberships) {
        LOG.info("Deleting composite membership for groupName=" + ownerGroup.getName() + ", groupId=" + ownerGroup.getUuid() + ", memberId=" + membership.getMemberUuid());
        deleteMembership(membership);
      }
      
      GrouperFuture.waitForJob(futures, 0, callablesWithProblems);
      GrouperCallable.tryCallablesWithProblems(callablesWithProblems);
      callablesWithProblems.clear();
      
      return;
    }
    
    Set<Membership> badMemberships;
    Set<String> missingMembershipMemberIds;
    
    if (composite.getType() == CompositeType.COMPLEMENT) {
      badMemberships = GrouperDAOFactory.getFactory().getMembership().findBadComplementMemberships(composite);
      missingMembershipMemberIds = GrouperDAOFactory.getFactory().getMembership().findMissingComplementMemberships(composite);
    } else if (composite.getType() == CompositeType.INTERSECTION) {
      badMemberships = GrouperDAOFactory.getFactory().getMembership().findBadIntersectionMemberships(composite);
      missingMembershipMemberIds = GrouperDAOFactory.getFactory().getMembership().findMissingIntersectionMemberships(composite);
    } else if (composite.getType() == CompositeType.UNION) {
      badMemberships = GrouperDAOFactory.getFactory().getMembership().findBadUnionMemberships(composite);
      missingMembershipMemberIds = GrouperDAOFactory.getFactory().getMembership().findMissingUnionMemberships(composite);
    } else {
      throw new RuntimeException("Unexpected composite type: " + composite.getTypeDb());
    }
    
    for (Membership badMembership : badMemberships) {
      LOG.info("Deleting composite membership for groupName=" + ownerGroup.getName() + ", groupId=" + ownerGroup.getUuid() + ", memberId=" + badMembership.getMemberUuid());
      deleteMembership(badMembership);
    }
    
    for (String missingMembershipMemberId : missingMembershipMemberIds) {
      Membership membership = Composite.internal_createNewCompositeMembershipObject(ownerGroup.getUuid(), missingMembershipMemberId, composite.getUuid());
      LOG.info("Adding composite membership for groupName=" + ownerGroup.getName() + ", groupId=" + ownerGroup.getUuid() + ", memberId=" + missingMembershipMemberId + ", compositeId=" + composite.getUuid());
      saveMembership(membership);
    }
    
    // make sure via_composite_id is correct
    int updateCount = HibernateSession.bySqlStatic().executeSql("update grouper_memberships set via_composite_id = ? where owner_group_id = ? and field_id = ? and mship_type = ? and (via_composite_id is null or via_composite_id != ?)",
        GrouperUtil.toListObject(composite.getUuid(), composite.getFactorOwnerUuid(), Group.getDefaultList().getUuid(), "composite", composite.getUuid()), HibUtils.listType(StringType.INSTANCE, StringType.INSTANCE, StringType.INSTANCE, StringType.INSTANCE, StringType.INSTANCE));
    
    if (updateCount > 0) {
      LOG.info("Fixed viaCompositeId for groupName=" + ownerGroup.getName() + ", groupId=" + ownerGroup.getUuid() + ", updateCount=" + updateCount);
      this.getEsbConsumer().getChangeLogProcessorMetadata().getHib3GrouperLoaderLog().addUpdateCount(updateCount);
    }
    
    GrouperFuture.waitForJob(futures, 0, callablesWithProblems);
    GrouperCallable.tryCallablesWithProblems(callablesWithProblems);
    callablesWithProblems.clear();
  }
}
