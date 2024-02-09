package edu.internet2.middleware.grouper.app.membershipRequire;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperDaemonUtils;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEvent;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEventContainer;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEventType;
import edu.internet2.middleware.grouper.esb.listener.EsbListenerBase;
import edu.internet2.middleware.grouper.esb.listener.ProvisioningSyncConsumerResult;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncHeartbeat;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;



/**
 * real time provisioning listener
 * @author mchyzer
 *
 */
public class MembershipRequireEsbListener extends EsbListenerBase {

  private static final Log LOG = GrouperUtil.getLog(MembershipRequireEsbListener.class);

  @Override
  public final boolean dispatchEvent(String eventJsonString, String consumerName) {
    throw new RuntimeException("Not implemented");
  }

  @Override
  public Integer getBatchSize() {
    return 100000;
  }

  @Override
  public ProvisioningSyncConsumerResult dispatchEventList(
      List<EsbEventContainer> esbEventContainers) {
    
    if (GrouperUtil.length(esbEventContainers) == 0) {
      return null;
    }
    
    if (!GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.membershipRequirement.changeLogEnable", true)) {
      ProvisioningSyncConsumerResult provisioningSyncConsumerResult = new ProvisioningSyncConsumerResult();
      provisioningSyncConsumerResult.setLastProcessedSequenceNumber(esbEventContainers.get(esbEventContainers.size()-1).getSequenceNumber());
      return provisioningSyncConsumerResult;
    }
    
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName("membershipRequire");
    
    gcGrouperSync.setSyncEngine(GcGrouperSync.MEMBERSHIP_REQUIRE);
    gcGrouperSync.getGcGrouperSyncDao().store();
    GcGrouperSyncJob gcGrouperSyncJob = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveOrCreateBySyncType("incremental");
    gcGrouperSyncJob.waitForRelatedJobsToFinishThenRun(false);
    
    GcGrouperSyncHeartbeat gcGrouperSyncHeartbeat = new GcGrouperSyncHeartbeat();
    gcGrouperSyncHeartbeat.setGcGrouperSyncJob(gcGrouperSyncJob);
    gcGrouperSyncHeartbeat.setFullSync(false);
    gcGrouperSyncHeartbeat.addHeartbeatLogic(new Runnable() {
      @Override
      public void run() {
        
      }
    });
    if (!gcGrouperSyncHeartbeat.isStarted()) {
      gcGrouperSyncHeartbeat.runHeartbeatThread();
    }

    long millisLastFullStart = 0;
    {
      GcGrouperSyncJob gcGrouperSyncFullJob = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveOrCreateBySyncType("full");
      if (gcGrouperSyncFullJob != null && gcGrouperSyncFullJob.getLastSyncStart() != null) {
        millisLastFullStart = gcGrouperSyncFullJob.getLastSyncStart().getTime();
      }
    }
    Map<String, Object> debugMapOverall = this.getEsbConsumer().getDebugMapOverall();
    
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = this.getChangeLogProcessorMetadata().getHib3GrouperLoaderLog();

    Set<MultiKey> attributeNameTypeOwnerIds = new HashSet<MultiKey>();
    Set<MultiKey> groupNameMemberIds = new HashSet<MultiKey>();
    
    RuntimeException runtimeException = null;
    try {
      
      for (EsbEventContainer esbEventContainer : GrouperUtil.nonNull(esbEventContainers)) {
        EsbEvent esbEvent = esbEventContainer.getEsbEvent();
        EsbEventType esbEventType = esbEventContainer.getEsbEventType();
    
        // this cant be null
        long createdOnMillis = esbEvent.getCreatedOnMicros()/1000;
        
        // if this is before last full then ignore
        if (createdOnMillis < millisLastFullStart) {
          continue;
        }
        
        boolean syncThisMembership = false;
        Field membersField = Group.getDefaultList();
        switch (esbEventType) {
          
          case ATTRIBUTE_ASSIGN_ADD:
            {
              String attributeAssignType = esbEvent.getAttributeAssignType();
              if (!StringUtils.equals("group", attributeAssignType) && !StringUtils.equals("stem", attributeAssignType)) {
                continue;
              }
              String attributeDefNameName = esbEvent.getAttributeDefNameName();
              Set<MembershipRequireConfigBean> membershipRequireConfigBeans = MembershipRequireEngine.attributeDefNameNameToConfigBean(attributeDefNameName);
              
              if (GrouperUtil.length(membershipRequireConfigBeans) == 0) {
                continue;
              }
              attributeNameTypeOwnerIds.add(new MultiKey(attributeDefNameName, attributeAssignType, esbEvent.getOwnerId()));
            }
            break;
          case MEMBERSHIP_DELETE:
            {
              // skip if wrong source
              if (!StringUtils.isBlank(esbEvent.getSourceId()) && GrouperUtil.equals("g:gsa", esbEvent.getSourceId())) {
                continue;
              }
              if (!StringUtils.equals(membersField.getId(), esbEvent.getFieldId())) {
                continue;
              }
              String groupName = esbEvent.getGroupName();
              Set<MembershipRequireConfigBean> membershipRequireConfigBeans = MembershipRequireEngine.requiredGroupNameToConfigBean(groupName);
              
              if (GrouperUtil.length(membershipRequireConfigBeans) == 0) {
                continue;
              }
              groupNameMemberIds.add(new MultiKey(groupName, esbEvent.getMemberId()));
            }
            break;
          
          default:
        }
      }
      
      for (MultiKey attributeNameTypeOwnerId : attributeNameTypeOwnerIds) {
        GrouperDaemonUtils.stopProcessingIfJobPaused();

        String attributeName = (String)attributeNameTypeOwnerId.getKey(0);
        String attributeType = (String)attributeNameTypeOwnerId.getKey(1);
        String ownerId = (String)attributeNameTypeOwnerId.getKey(2);
        Set<MembershipRequireConfigBean> membershipRequireConfigBeans = MembershipRequireEngine.attributeDefNameNameToConfigBean(attributeName);
        if (GrouperUtil.length(membershipRequireConfigBeans) == 0) {
          continue;
        }
        if (StringUtils.equals("group", attributeType)) {
          Group group = GroupFinder.findByUuid(ownerId, false);
          if (group == null) {
            continue;
          }
          Set<MembershipRequireConfigBean> configBeansAssignedToGroup = MembershipRequireEngine.groupNameToConfigBeanAssigned(group.getName());
          for (MembershipRequireConfigBean membershipRequireConfigBean : GrouperUtil.nonNull(membershipRequireConfigBeans)) {
            // maybe its a loader group or was unassigned or something
            if (!configBeansAssignedToGroup.contains(membershipRequireConfigBean)) {
              continue;
            }
            int removed = MembershipRequireEngine.removeInvalidMembers(group.getName(), membershipRequireConfigBean, null, MembershipRequireEngineEnum.changeLogConsumer);
            hib3GrouperLoaderLog.addDeleteCount(removed);
          }
        } else if (StringUtils.equals("stem", attributeType)) {
          Stem stem = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), ownerId, false);
          if (stem == null) {
            continue;
          }
          Set<MembershipRequireConfigBean> configBeansAssignedToStem = MembershipRequireEngine.stemNameToConfigBeanAssigned(stem.getName());
          Set<String> groupNames = MembershipRequireEngine.groupsInStems(GrouperUtil.toSet(stem.getName()));
          for (MembershipRequireConfigBean membershipRequireConfigBean : GrouperUtil.nonNull(membershipRequireConfigBeans)) {
            // maybe its a loader group or was unassigned or something
            if (!configBeansAssignedToStem.contains(membershipRequireConfigBean)) {
              continue;
            }
            for (String groupName : GrouperUtil.nonNull(groupNames)) {
              int removed = MembershipRequireEngine.
                  removeInvalidMembers(groupName, membershipRequireConfigBean, null, MembershipRequireEngineEnum.changeLogConsumer);
              hib3GrouperLoaderLog.addDeleteCount(removed);
            }
          }
        } else {
          throw new RuntimeException("Not expecting attributeType: " + attributeType);
        }
      }
      for (MultiKey groupNameMemberId : groupNameMemberIds) {
        GrouperDaemonUtils.stopProcessingIfJobPaused();

        String requireGroupName = (String)groupNameMemberId.getKey(0);
        String memberId = (String)groupNameMemberId.getKey(1);
        Set<MembershipRequireConfigBean> membershipRequireConfigBeans = MembershipRequireEngine.requiredGroupNameToConfigBean(requireGroupName);
        if (GrouperUtil.length(membershipRequireConfigBeans) == 0) {
          continue;
        }
        for (MembershipRequireConfigBean membershipRequireConfigBean : GrouperUtil.nonNull(membershipRequireConfigBeans)) {
          Set<String> groupNames = MembershipRequireEngine.attributeDefNameNameToGroupNames(membershipRequireConfigBean.getAttributeName());
          for (String groupName : GrouperUtil.nonNull(groupNames)) {
            int removed = MembershipRequireEngine.removeInvalidMembers(groupName, membershipRequireConfigBean, memberId, MembershipRequireEngineEnum.changeLogConsumer);
            hib3GrouperLoaderLog.addDeleteCount(removed);
          }
        }
      }
      
      // if we didnt throw an exception, then we processed all of them
      // get the last sequence before the provisioner re-orders them
      Long lastSequenceNumber = -1L;
      if (GrouperUtil.length(esbEventContainers) > 0) {
        EsbEventContainer lastEvent = esbEventContainers.get(esbEventContainers.size()-1);
        lastSequenceNumber = lastEvent.getSequenceNumber();
      }
  
      hib3GrouperLoaderLog.store();
  
      ProvisioningSyncConsumerResult provisioningSyncConsumerResult = new ProvisioningSyncConsumerResult();
      provisioningSyncConsumerResult.setLastProcessedSequenceNumber(lastSequenceNumber);
      return provisioningSyncConsumerResult;
    } catch (RuntimeException re) {
      runtimeException = re;
      debugMapOverall.put("exception", GrouperClientUtils.getFullStackTrace(re));
    } finally {
      GcGrouperSyncHeartbeat.endAndWaitForThread(gcGrouperSyncHeartbeat);
      debugMapOverall.put("finalLog", true);
      synchronized (MembershipRequireEsbListener.class) {
        try {
          if (gcGrouperSyncJob != null) {
            gcGrouperSyncJob.assignHeartbeatAndEndJob();
          }
        } catch (RuntimeException re2) {
          debugMapOverall.put("exception2", GrouperClientUtils.getFullStackTrace(re2));
          if (runtimeException == null) {
            throw re2;
          }
          
        }
      }
      
      if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperUtil.mapToString(debugMapOverall));
      }
      
      if (runtimeException != null) {
        throw runtimeException;
      }

    }
    throw new RuntimeException("Shouldnt get here");
  }

  @Override
  public void disconnect() {

  }

}
