package edu.internet2.middleware.grouper.app.provisioning;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEvent;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEventContainer;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEventType;
import edu.internet2.middleware.grouper.esb.listener.ProvisioningSyncConsumer;
import edu.internet2.middleware.grouper.esb.listener.ProvisioningSyncConsumerResult;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncHeartbeat;



/**
 * real time provisioning listener
 * @author mchyzer
 *
 */
public class ProvisioningConsumer extends ProvisioningSyncConsumer {

  public ProvisioningConsumer() {
  }

  @Override
  public ProvisioningSyncConsumerResult dispatchEventList(
      List<EsbEventContainer> esbEventContainers,
      GrouperProvisioningProcessingResult grouperProvisioningProcessingResult) {
    
    ProvisioningSyncConsumerResult provisioningSyncConsumerResult = new ProvisioningSyncConsumerResult();

    Map<String, Object> debugMapOverall = this.getEsbConsumer().getDebugMapOverall();
    
    long sequenceProcessed = -1;
    
    final GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner(this.getEsbConsumer().getGrouperProvisioningProcessingResult().getGcGrouperSync().getProvisionerName());
    grouperProvisioner.setProvisioningConsumer(this);
    grouperProvisioner.setDebugMap(debugMapOverall);
    grouperProvisioner.setGcGrouperSync(this.getEsbConsumer().getGrouperProvisioningProcessingResult().getGcGrouperSync());
    grouperProvisioner.setGcGrouperSyncJob(this.getEsbConsumer().getGrouperProvisioningProcessingResult().getGcGrouperSyncJob());
    grouperProvisioner.setGcGrouperSyncLog(this.getEsbConsumer().getGrouperProvisioningProcessingResult().getGcGrouperSyncLog());
    GcGrouperSyncHeartbeat gcGrouperSyncHeartbeat = grouperProvisioningProcessingResult.getGcGrouperSyncHeartbeat();
    
    if (gcGrouperSyncHeartbeat == null) {
      gcGrouperSyncHeartbeat = new GcGrouperSyncHeartbeat();
      gcGrouperSyncHeartbeat.setGcGrouperSyncJob(grouperProvisioner.getGcGrouperSyncJob());
      gcGrouperSyncHeartbeat.addHeartbeatLogic(this.getEsbConsumer().provisioningHeartbeatLogic());

    }
    final Hib3GrouperLoaderLog hib3GrouperLoaderLog = ProvisioningConsumer.this.getChangeLogProcessorMetadata().getHib3GrouperLoaderLog();
    gcGrouperSyncHeartbeat.insertHeartbeatLogic(new Runnable() {

      @Override
      public void run() {
        
        
        GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.getGrouperProvisioningOutput();
        if (grouperProvisioningOutput != null) {
          grouperProvisioningOutput.copyToHib3LoaderLog(hib3GrouperLoaderLog);
        }
      }
      
    });
    grouperProvisioner.setGcGrouperSyncHeartbeat(gcGrouperSyncHeartbeat);

    //look for full sync before we configure the real time
    boolean ranFullSync = false;
    GrouperProvisioningOutput grouperProvisioningOutput = null;
    for (EsbEventContainer esbEventContainer : GrouperUtil.nonNull(esbEventContainers)) {
      EsbEvent esbEvent = esbEventContainer.getEsbEvent();
      EsbEventType esbEventType = esbEventContainer.getEsbEventType();
            
      switch (esbEventType) {
        
        case PROVISIONING_SYNC_FULL:
          
          GrouperProvisioningType grouperProvisioningType = null;
          
          if (!StringUtils.isBlank(esbEvent.getProvisionerSyncType())) {
            grouperProvisioningType = GrouperProvisioningType.valueOfIgnoreCase(esbEvent.getProvisionerSyncType(), true);
          } else {
            grouperProvisioningType = GrouperProvisioningType.fullProvisionFull;
          }
          
          grouperProvisioningOutput = grouperProvisioner.provision(grouperProvisioningType); 
          ranFullSync = true;
          break;
        default: 
          break;
      }
    }

    if (!ranFullSync) {
      GrouperProvisioningType grouperProvisioningType = GrouperProvisioningType.valueOfIgnoreCase(
          this.getEsbConsumer().getGrouperProvisioningProcessingResult().getGcGrouperSyncJob().getSyncType(), false);
      if (grouperProvisioningType == null) {
        grouperProvisioningType = GrouperProvisioningType.incrementalProvisionChangeLog;
      }
      grouperProvisioner.retrieveGrouperProvisioningBehavior().setGrouperProvisioningType(grouperProvisioningType);
      grouperProvisioner.retrieveGrouperProvisioningConfiguration().configureProvisioner();
    }
    
    // see if we are getting memberships or privs
    GrouperProvisioningMembershipFieldType membershipFieldType = grouperProvisioner.retrieveGrouperProvisioningConfiguration().getGrouperProvisioningMembershipFieldType();

    GrouperIncrementalDataToProcess grouperIncrementalUuidsToRetrieveFromGrouper 
      = grouperProvisioner.retrieveGrouperProvisioningDataIncrementalInput().getGrouperIncrementalDataToProcessWithoutRecalc();
    
    // these events are already filtered
    for (EsbEventContainer esbEventContainer : GrouperUtil.nonNull(esbEventContainers)) {
      EsbEvent esbEvent = esbEventContainer.getEsbEvent();
      EsbEventType esbEventType = esbEventContainer.getEsbEventType();
      
      if (!ranFullSync) {
        
        boolean syncThisMembership = false;
        
        switch (esbEventType) {
          
          case PROVISIONING_SYNC_GROUP:
            
            if (!StringUtils.isBlank(esbEvent.getGroupId())) {
              grouperIncrementalUuidsToRetrieveFromGrouper.getGroupUuidsForGroupMembershipSync().add(esbEvent.getGroupId());
              grouperIncrementalUuidsToRetrieveFromGrouper.getGroupUuidsForGroupOnly().add(esbEvent.getGroupId());
            }
            break;
  
          case PROVISIONING_SYNC_USER:
          
            if (!StringUtils.isBlank(esbEvent.getMemberId())) {
              grouperIncrementalUuidsToRetrieveFromGrouper.getMemberUuidsForEntityMembershipSync().add(esbEvent.getMemberId());
              grouperIncrementalUuidsToRetrieveFromGrouper.getMemberUuidsForEntityOnly().add(esbEvent.getMemberId());
            }
            break;

          case PRIVILEGE_ADD:
          case PRIVILEGE_DELETE:
          case PRIVILEGE_UPDATE:
            switch (membershipFieldType) {
              case admin:
                syncThisMembership = StringUtils.equals("admins", esbEvent.getFieldName());
                break;
              case readAdmin:
                syncThisMembership = StringUtils.equals("admins", esbEvent.getFieldName()) || StringUtils.equals("readers", esbEvent.getFieldName()) ;
                
                break;
                
              case updateAdmin:
                syncThisMembership = StringUtils.equals("admins", esbEvent.getFieldName()) || StringUtils.equals("updaters", esbEvent.getFieldName()) ;
                
                break;
              default:
                // skip
            }
            
            break;
            
          case PROVISIONING_SYNC_MEMBERSHIP:
            //always sync this from message
            syncThisMembership = true;
            break;
          case GROUP_UPDATE:
            
            if (!StringUtils.isBlank(esbEvent.getGroupId())) {
              // do we need to update memberships?  hmmm, maybe, so might as well
              grouperIncrementalUuidsToRetrieveFromGrouper.getGroupUuidsForGroupOnly().add(esbEvent.getGroupId());
              grouperIncrementalUuidsToRetrieveFromGrouper.getGroupUuidsForGroupMembershipSync().add(esbEvent.getGroupId());
            }
            
            break;
          case MEMBERSHIP_ADD:
          case MEMBERSHIP_DELETE:
          case MEMBERSHIP_UPDATE:

            if (membershipFieldType == GrouperProvisioningMembershipFieldType.members) {
              syncThisMembership = true;
            }
  
            break;
          
          default:
        }
        if (syncThisMembership) {
          // group_id, member_id, field_id
          Field field = FieldFinder.find(esbEvent.getFieldName(), true);
          MultiKey membershipFields = new MultiKey(esbEvent.getGroupId(), esbEvent.getMemberId(), field.getId());
          
          grouperIncrementalUuidsToRetrieveFromGrouper.getGroupUuidsForGroupOnly().add(esbEvent.getGroupId());
          grouperIncrementalUuidsToRetrieveFromGrouper.getMemberUuidsForEntityOnly().add(esbEvent.getMemberId());
          grouperIncrementalUuidsToRetrieveFromGrouper.getGroupUuidsMemberUuidsFieldIdsForMembershipSync().add(membershipFields);


        }
      }
      
      sequenceProcessed = esbEventContainer.getSequenceNumber();
      provisioningSyncConsumerResult.setLastProcessedSequenceNumber(sequenceProcessed);
    }
    
    if (!ranFullSync) {
      
      // add other events
      for (String groupId : GrouperUtil.nonNull(grouperProvisioningProcessingResult.getGroupIdsToAddToTarget())) {
        grouperIncrementalUuidsToRetrieveFromGrouper.getGroupUuidsForGroupMembershipSync().add(groupId);
        grouperIncrementalUuidsToRetrieveFromGrouper.getGroupUuidsForGroupOnly().add(groupId);

      }
      for (String groupId : GrouperUtil.nonNull(grouperProvisioningProcessingResult.getGroupIdsToRemoveFromTarget())) {
        // this might be overkill, i.e. do we need to retrieve all to remove from target?  might as well at this point
        grouperIncrementalUuidsToRetrieveFromGrouper.getGroupUuidsForGroupMembershipSync().add(groupId);
        grouperIncrementalUuidsToRetrieveFromGrouper.getGroupUuidsForGroupOnly().add(groupId);
      }
      
      
      grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.incrementalProvisionChangeLog); 
    }

    grouperProvisioningOutput = grouperProvisioner.getGrouperProvisioningOutput();
    if (grouperProvisioningOutput != null) {
      grouperProvisioningOutput.copyToHib3LoaderLog(hib3GrouperLoaderLog);
    }

    // TODO handle errors
    
    return provisioningSyncConsumerResult;
  }

  @Override
  public void disconnect() {

  }

}
