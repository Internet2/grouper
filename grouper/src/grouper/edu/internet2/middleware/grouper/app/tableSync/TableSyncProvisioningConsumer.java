package edu.internet2.middleware.grouper.app.tableSync;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningProcessingResult;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEvent;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEventContainer;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEventType;
import edu.internet2.middleware.grouper.esb.listener.ProvisioningSyncConsumer;
import edu.internet2.middleware.grouper.esb.listener.ProvisioningSyncConsumerResult;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncHeartbeat;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncSubtype;



/**
 * real time provisioning listener
 * @author mchyzer
 *
 */
public class TableSyncProvisioningConsumer extends ProvisioningSyncConsumer {

  public TableSyncProvisioningConsumer() {
  }

  @Override
  public ProvisioningSyncConsumerResult dispatchEventList(
      List<EsbEventContainer> esbEventContainers,
      GrouperProvisioningProcessingResult grouperProvisioningProcessingResult) {
    
    ProvisioningSyncConsumerResult provisioningSyncConsumerResult = new ProvisioningSyncConsumerResult();

    Map<String, Object> debugMapOverall = this.getEsbConsumer().getDebugMapOverall();
    
    long sequenceProcessed = -1;

    GcTableSync gcTableSync = new GcTableSync();
    gcTableSync.setGcGrouperSyncJob(grouperProvisioningProcessingResult.getGcGrouperSyncJob());
    gcTableSync.setGcGrouperSync(grouperProvisioningProcessingResult.getGcGrouperSync());
    gcTableSync.setGcGrouperSyncLog(grouperProvisioningProcessingResult.getGcGrouperSyncLog());
    GcGrouperSyncHeartbeat gcGrouperSyncHeartbeat = new GcGrouperSyncHeartbeat();
    gcGrouperSyncHeartbeat.addHeartbeatLogic(new Runnable() {

      @Override
      public void run() {
        TableSyncProvisioningConsumer.this.getChangeLogProcessorMetadata().getHib3GrouperLoaderLog().store();
      }
      
    });
    gcTableSync.setGcGrouperSyncHeartbeat(gcGrouperSyncHeartbeat);
    for (EsbEventContainer esbEventContainer : GrouperUtil.nonNull(esbEventContainers)) {
      EsbEvent esbEvent = esbEventContainer.getEsbEvent();
      EsbEventType esbEventType = esbEventContainer.getEsbEventType();
      
      switch (esbEventType) {
        
        case PROVISIONING_SYNC_FULL:
          
          GcTableSyncSubtype gcTableSyncSubtype = GcTableSyncSubtype.fullSyncFull;
          if (!StringUtils.isBlank(esbEvent.getProvisionerSyncType())) {
            gcTableSyncSubtype = GcTableSyncSubtype.valueOfIgnoreCase(esbEvent.getProvisionerSyncType(), true);
          }
          
          GcTableSyncSubtype.runEmbeddedFullSync(debugMapOverall, grouperProvisioningProcessingResult.getGcGrouperSyncJob(), grouperProvisioningProcessingResult.getGcGrouperSyncLog(), gcTableSyncSubtype);
          break;
        
        case PROVISIONING_SYNC_GROUP:
          
          if (!StringUtils.isBlank(esbEvent.getGroupId())) {
            if (gcTableSync.getGroupIdsToSync() == null) {
              gcTableSync.setGroupIdsToSync((Collection<Object>)(Object)new LinkedHashSet<String>());
            }
            gcTableSync.getGroupIdsToSync().add(esbEvent.getGroupId());
          }
          break;

        case PROVISIONING_SYNC_USER:
        
          if (!StringUtils.isBlank(esbEvent.getMemberId())) {
            if (gcTableSync.getMemberIdsToSync() == null) {
              gcTableSync.setMemberIdsToSync((Collection<Object>)(Object)new LinkedHashSet<String>());
            }
            gcTableSync.getMemberIdsToSync().add(esbEvent.getMemberId());
          }
          break;

        case PROVISIONING_SYNC_MEMBERSHIP:
        case MEMBERSHIP_ADD:
        case MEMBERSHIP_DELETE:
        case MEMBERSHIP_UPDATE:
          
          // group_id, member_id, field_id
          Field field = FieldFinder.find(esbEvent.getFieldName(), true);
          MultiKey membershipFields = new MultiKey(esbEvent.getGroupId(), esbEvent.getMemberId(), field.getId());
          
          if (gcTableSync.getPrimaryKeysToSyncFromMemberships() == null) {
            gcTableSync.setPrimaryKeysToSyncFromMemberships(new LinkedHashSet<MultiKey>());
          }
          gcTableSync.getPrimaryKeysToSyncFromMemberships().add(membershipFields);

          break;
        
        default:
      }
      
      sequenceProcessed = esbEventContainer.getSequenceNumber();
      provisioningSyncConsumerResult.setLastProcessedSequenceNumber(sequenceProcessed);
    }
    
    GcTableSyncSubtype gcTableSyncSubtype = GcTableSyncSubtype.valueOfIgnoreCase(
        this.getEsbConsumer().getGrouperProvisioningProcessingResult().getGcGrouperSyncJob().getSyncType(), false);
    if (gcTableSyncSubtype == null) {
      gcTableSyncSubtype = GcTableSyncSubtype.incrementalFromIdentifiedPrimaryKeys;
    }
    
    gcTableSync.sync(this.getEsbConsumer().getGrouperProvisioningProcessingResult().getGcGrouperSync().getProvisionerName(), 
        gcTableSyncSubtype);
    
    // hmmm, no error handling here...
    
    return provisioningSyncConsumerResult;
  }

  @Override
  public void disconnect() {

  }

}
