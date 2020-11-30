package edu.internet2.middleware.grouper.app.provisioning;

import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEventContainer;
import edu.internet2.middleware.grouper.esb.listener.ProvisioningSyncConsumer;
import edu.internet2.middleware.grouper.esb.listener.ProvisioningSyncConsumerResult;
import edu.internet2.middleware.grouper.util.GrouperUtil;



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
      List<EsbEventContainer> esbEventContainers) {
    
    // not sure why this would happen...
    if (GrouperUtil.length(esbEventContainers) == 0) {
      return null;
    }
    
    ProvisioningSyncConsumerResult provisioningSyncConsumerResult = new ProvisioningSyncConsumerResult();

    Map<String, Object> debugMapOverall = this.getEsbConsumer().getDebugMapOverall();
    
    final GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner(this.getChangeLogProcessorMetadata().getConsumerName());
    grouperProvisioner.setProvisioningConsumer(this);
    grouperProvisioner.setDebugMap(debugMapOverall);
    final Hib3GrouperLoaderLog hib3GrouperLoaderLog = ProvisioningConsumer.this.getChangeLogProcessorMetadata().getHib3GrouperLoaderLog();
    grouperProvisioner.getGcGrouperSyncHeartbeat().insertHeartbeatLogic(new Runnable() {

      @Override
      public void run() {
        
        GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.getGrouperProvisioningOutput();
        if (grouperProvisioningOutput != null) {
          grouperProvisioningOutput.copyToHib3LoaderLog(hib3GrouperLoaderLog);
          hib3GrouperLoaderLog.store();
        }
      }
      
    });

    GrouperProvisioningType grouperProvisioningType = GrouperProvisioningType.incrementalProvisionChangeLog;
    grouperProvisioner.retrieveGrouperProvisioningDataIncrementalInput().setEsbEventContainers(esbEventContainers);
    GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(grouperProvisioningType);
    grouperProvisioningOutput.copyToHib3LoaderLog(hib3GrouperLoaderLog);
    hib3GrouperLoaderLog.store();

    // if we didnt throw an exception, then we processed all of them
    EsbEventContainer lastEvent = esbEventContainers.get(esbEventContainers.size()-1);
    Long lastSequenceNumber = lastEvent.getSequenceNumber();
    provisioningSyncConsumerResult.setLastProcessedSequenceNumber(lastSequenceNumber);
    return provisioningSyncConsumerResult;
  }

  @Override
  public void disconnect() {

  }

}
